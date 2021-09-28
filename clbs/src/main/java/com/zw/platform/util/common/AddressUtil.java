package com.zw.platform.util.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.ParallelWorker;
import com.zw.platform.domain.oil.PositionInfo;
import com.zw.platform.dto.location.AddressDTO;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 逆地址编码接口方法
 * Created by wjy on 2017/6/17.
 * @author wjy
 */
@Component
public class AddressUtil {
    private static final Logger log = LogManager.getLogger(AddressUtil.class);
    private static final double EARTH_RADIUS = 6378137; //地球半径
    private static final int AMAP_BATCH_SIZE = 20;
    private static final String REGEO_URL = "http://restapi.amap.com/v3/geocode/regeo";
    private static String key;
    private static String unknownLocation;
    private static String noLocation;

    private static final String EMPTY_ARRAY = "[]";
    private static final String EMPTY_OBJECT = "{}";

    private static final Float CHAIN_MIN_LONGITUDE = 73.33f;
    private static final Float CHAIN_MAX_LONGITUDE = 135.05f;
    private static final Float CHAIN_MIN_LATITUDE = 3.51f;
    private static final Float CHAIN_MAX_LATITUDE = 53.33f;

    @Value("${api.key.gaode}")
    public void setKey(String key) {
        AddressUtil.key = key;
    }

    @Value("${unknown.location}")
    public void setUnknownLocation(String unknownLocation) {
        AddressUtil.unknownLocation = unknownLocation;
    }

    @Value("${no.location}")
    public void setNoLocation(String noLocation) {
        AddressUtil.noLocation = noLocation;
    }

    /**
     * 逆地理编码
     * @param lat 纬度 latitude
     * @param lng 经度 longitude
     * @return result
     */
    public static PositionInfo inverseAddress(String lng, String lat) {
        return inverseAddress(lng + "," + lat);
    }

    /**
     * 解析逆地址
     * @param longitude 经度
     * @param latitude  纬度
     * @return result
     */
    public static PositionInfo inverseAddress(Double longitude, Double latitude) {
        return inverseAddress(longitude + "," + latitude);
    }

    /**
     * 经纬度解析
     * @param lngLat 经度,纬度
     * @return PositionInfo
     */
    public static PositionInfo inverseAddress(String lngLat) {
        final Map<String, String> requestLngLatMap = filterLngLats(Collections.singletonList(lngLat));
        if (requestLngLatMap.isEmpty()) {
            return new PositionInfo(noLocation);
        }
        final String requestLngLat = requestLngLatMap.values().iterator().next();
        Map<String, String> params = ImmutableMap.of("key", key, "location", requestLngLat, "extensions", "all");
        String address = HttpClientUtil.sendGet(REGEO_URL, params);
        return PositionInfo.parse(address, unknownLocation, noLocation);
    }

    /**
     * 批量查询逆地址（高德）
     *
     * @implNote 这里入参要求传Set类型的用意：1.表明无序 2.表明做了去重
     * @param lngLats 经纬度集合，逗号分隔
     * @return 经纬度 -> 逆地址
     */
    public static Map<String, String> batchInverseAddress(Set<String> lngLats) {
        if (CollectionUtils.isEmpty(lngLats)) {
            return Collections.emptyMap();
        }
        final ArrayList<String> locations = new ArrayList<>(lngLats);
        final BatchAddressTask task = new BatchAddressTask(locations);
        try {
            return ParallelWorker.invokeTask(locations, AMAP_BATCH_SIZE, task);
        } catch (Exception e) {
            log.error("批量从高德查询逆地址异常", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 批量逆地理编码，一次最多20个，精度：101米
     *
     * @param rawLngLats 经纬度列表
     * @return 中国境内逆地址，与入参列表严格一一对应
     */
    private static List<String> batchGetAddress(List<String> rawLngLats) {
        // 坐标为空时，直接返回未定位的信息
        if (rawLngLats == null || rawLngLats.isEmpty()) {
            return Collections.singletonList(noLocation);
        }

        // 过滤掉无效地址，并将有效地址精度降低到100米（小数点后3位）
        final Map<String, String> filteredLngLats = filterLngLats(rawLngLats);

        // list保证请求高德和解析高德时的地址是一一对应的
        final ArrayList<String> requestLngLatList = new ArrayList<>(filteredLngLats.values());
        final String addresses = getAmapAddress(requestLngLatList);
        final Map<String, String> amapAddresses = parseList(requestLngLatList, addresses);

        return rawLngLats.stream().map(rawLngLat -> {
            final String requestLngLat = filteredLngLats.get(rawLngLat);
            return null == requestLngLat
                    ? noLocation
                    : amapAddresses.getOrDefault(requestLngLat, unknownLocation);
        }).collect(Collectors.toList());
    }

    private static Map<String, String> parseList(List<String> requestLocations, String addresses) {
        JSONObject obj = JSON.parseObject(addresses);
        if (obj == null) {
            return Collections.emptyMap();
        }
        JSONArray regeocodes = obj.getJSONArray("regeocodes");
        // 因为 请求-响应 中的地址按顺序一一对应，所以如果数组长度不一致，那就只能认为响应值全都无效
        if (regeocodes == null || regeocodes.size() != requestLocations.size()) {
            log.error("高德API返回值异常，请求地址：{}，响应：{}", String.join(",", requestLocations), addresses);
            return Collections.emptyMap();
        }
        Map<String, String> amapLocations = new HashMap<>(CommonUtil.ofMapCapacity(regeocodes.size()));
        for (int i = 0; i < regeocodes.size(); i++) {
            Object regeocode = regeocodes.get(i);
            final String lngLat = requestLocations.get(i);
            if (!(regeocode instanceof JSONObject)) {
                amapLocations.put(lngLat, unknownLocation);
                continue;
            }
            JSONObject regeocodeJsonObj = (JSONObject) regeocode;
            String formatAddress = regeocodeJsonObj.getString("formatted_address");
            if (EMPTY_ARRAY.equals(formatAddress) || EMPTY_OBJECT.equals(formatAddress)) {
                amapLocations.put(lngLat, noLocation);
                continue;
            }
            JSONObject addressComponentJsonObj = regeocodeJsonObj.getJSONObject("addressComponent");
            String addressComponentJsonStr = addressComponentJsonObj.toJSONString();
            if (EMPTY_ARRAY.equals(addressComponentJsonStr) || EMPTY_OBJECT.equals(addressComponentJsonStr)) {
                amapLocations.put(lngLat, formatAddress);
                continue;
            }
            JSONObject streetNumberJsonObj = addressComponentJsonObj.getJSONObject("streetNumber");
            if (streetNumberJsonObj == null || Objects.equals(streetNumberJsonObj.toJSONString(), EMPTY_OBJECT)) {
                amapLocations.put(lngLat, formatAddress);
                continue;
            }
            StringBuilder builder = new StringBuilder(formatAddress);
            String street = streetNumberJsonObj.getString("street");
            if (StringUtils.isNotBlank(street) && !street.equals(EMPTY_ARRAY) && !street.equals(EMPTY_OBJECT)) {
                int index = builder.lastIndexOf(street);
                if (index > -1) {
                    builder.replace(index, index + street.length(), "");
                }
                builder.append(street);
            }
            String number = streetNumberJsonObj.getString("number");
            if (StringUtils.isNotBlank(number) && !number.equals(EMPTY_ARRAY) && !number.equals(EMPTY_OBJECT)) {
                int index = builder.lastIndexOf(number);
                if (index > -1) {
                    builder.replace(index, index + number.length(), "");
                }
                builder.append(number);
            }
            String direction = streetNumberJsonObj.getString("direction");
            if (StringUtils.isNotBlank(direction) && !direction.equals(EMPTY_ARRAY)
                    && !direction.equals(EMPTY_OBJECT)) {
                builder.append(direction);
            }
            String distance = streetNumberJsonObj.getString("distance");
            if (StringUtils.isNotBlank(distance) && !distance.equals(EMPTY_ARRAY) && !distance.equals(EMPTY_OBJECT)) {
                builder.append(new Double(distance).longValue()).append("米");
            }
            amapLocations.put(lngLat, builder.toString());
        }
        return amapLocations;
    }

    /**
     * 过滤掉异常的经纬度并对经纬度进行处理(过滤掉不在中国范围内的经纬度)
     * @implNote 参考自paas项目
     * @return 有效经纬度的映射：原经纬度 -> 逆地址请求经纬度
     */
    private static Map<String, String> filterLngLats(List<String> rawLngLats) {
        Map<String, String> map = new HashMap<>(CommonUtil.ofMapCapacity(rawLngLats.size()));
        for (String lngLat : rawLngLats) {
            if (null != lngLat) {
                String[] arr = lngLat.split(",");
                if (arr.length == 2 && checkLongitudeAndLatitude(arr[0], arr[1])) {
                    map.putIfAbsent(lngLat, getStandardValue(arr[0]) + "," + getStandardValue(arr[1]));
                }
            }
        }
        return map;
    }

    /**
     * 检查经纬度是否正确
     */
    private static boolean checkLongitudeAndLatitude(String longitude, String latitude) {
        if (StringUtils.isEmpty(longitude) || StringUtils.isEmpty(latitude)) {
            return false;
        }
        float dbLongitude;
        float dbLatitude;
        try {
            dbLongitude = Float.parseFloat(longitude);
            dbLatitude = Float.parseFloat(latitude);
        } catch (NumberFormatException e) {
            // 出现转换异常,说明参数不是正确的经纬度
            return false;
        }
        // 只解析中国范围内的经纬度
        return dbLongitude >= CHAIN_MIN_LONGITUDE && dbLongitude <= CHAIN_MAX_LONGITUDE
                && dbLatitude >= CHAIN_MIN_LATITUDE && dbLatitude <= CHAIN_MAX_LATITUDE;
    }

    /**
     * 转换标准的经纬度值（保留小数后6位）
     */
    private static String getStandardValue(String value) {
        if (value == null) {
            return null;
        }
        return new BigDecimal(value).setScale(6, BigDecimal.ROUND_DOWN).toPlainString();
    }

    /**
     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
     */
    public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(
            Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000.0;
        return s;
    }

    /**
     * 度转换为弧度
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    private static String getAmapAddress(Collection<String> locations) {
        if (CollectionUtils.isEmpty(locations)) {
            return null;
        }
        if (locations.size() > AMAP_BATCH_SIZE) {
            throw new IllegalArgumentException("批量逆地址编码数量超过" + AMAP_BATCH_SIZE);
        }
        String location = String.join("|", locations);
        Map<String, String> params = ImmutableMap.of("key", key, "location", location, "batch", "true");
        return HttpClientUtil.sendGet(REGEO_URL, params);
    }

    /**
     * 校验传入经纬度是否正确，不正确直接返回；正确的进行截取
     */
    public static Pair<String, String> formatLongitudeOrLatitude(String lng, String lat) {
        if (!checkLongitudeAndLatitude(lng, lat)) {
            return Pair.of(lng, lat);
        }
        return Pair.of(getStandardValue(lng), getStandardValue(lat));
    }

    private static class BatchAddressTask extends RecursiveTask<Map<String, String>> {
        private final List<String> locations;

        BatchAddressTask(List<String> locations) {
            this.locations = locations;
        }

        @Override
        protected Map<String, String> compute() {
            if (locations.size() <= AMAP_BATCH_SIZE) {
                final List<String> addresses = batchGetAddress(locations);
                return IntStream.range(0, locations.size())
                    .boxed()
                    .collect(Collectors.toMap(locations::get, addresses::get));
            }
            final List<BatchAddressTask> tasks = Lists.partition(locations, AMAP_BATCH_SIZE).stream()
                .map(BatchAddressTask::new)
                .collect(Collectors.toList());

            return invokeAll(tasks).stream()
                .map(ForkJoinTask::join)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
    }

    /**
     * 从HBase解析逆地址，可选是否从高德获取未命中的经纬度
     * @param isGaoDe 是否从高德获取未命中的经纬度
     */
    public static Map<String, String> batchInverseAddressFromHBase(Map<String, List<String>> locationMap,
                                                                   boolean isGaoDe) {
        Map<String, String> params = new HashMap<>(2);
        params.put("locations", String.join(";", locationMap.keySet()));
        String str = HttpClientUtil.send(PaasCloudUrlEnum.ADDRESS_BATCH_URL, params);
        final List<AddressDTO> addresses = PaasCloudUrlUtil.getResultListData(str, AddressDTO.class);

        //从hbase查询逆地址
        Map<String, String> addressMap = addresses.stream()
                .collect(Collectors.toMap(AddressDTO::getCoordinate, AddressDTO::getCoordinate, (o, p) -> o));

        if (!isGaoDe) {
            return addressMap;
        }

        //hbase未查询到的调用高德API查询
        Set<String> allAddress = locationMap.keySet();
        Set<String> otherAddress = addressMap.keySet();
        allAddress.removeAll(otherAddress);
        if (allAddress.size() > 0) {
            addressMap.putAll(AddressUtil.batchInverseAddress(allAddress));
        }
        return addressMap;
    }

}
