package com.zw.platform.domain.oil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * 位置信息表
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PositionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String EMPTY_ARRAY = "[]";

    private static final String EMPTY_OBJECT = "{}";

    private String id = UUID.randomUUID().toString();

    private Float longitude;//经度

    private Float latitude; //纬度

    private String adcode; //区域编码

    private String building;//所在楼/大厦

    private String buildingType;//楼类型

    private String city;//坐标点所在城市名称, 当所在城市为北京、上海、天津、重庆四个直辖市或者属于县级市时，该字段返回为空

    private String cityCode;//城市编码

    private String district;//所在区

    private String neighborhood;//所在社区

    private String neighborhoodType;//社区信息POI类型, 例如：科教文化服务;学校;高等院校

    private String province;//省份名

    private String street;//所在街道

    private String streetNumber;//门牌号
    /**
     * 方向
     */
    private String direction;
    /**
     * 距离
     */
    private String distance;

    private String township;//所在乡镇

    private String townCode;//乡镇街道编码

    private String crosses;//道路路口

    private String formattedAddress;//结构化地址信息,包括：省份＋城市＋区县＋城镇＋乡村＋街道＋门牌号码

    private String pois;//兴趣点基本信息

    private String roads;//道路信息

    private boolean isFormatted = false;

    public static PositionInfo parse(String address, String unknown, String unlocated) {
        PositionInfo info = new PositionInfo();
        JSONObject addressObj = JSONObject.parseObject(address);
        if (addressObj == null) {
            // 解析失败，返回未获取到位置信息
            info.setFormattedAddress(unknown);
            return info;
        }
        Object obj = addressObj.get("regeocode");
        if (!(obj instanceof JSONObject)) {
            info.setFormattedAddress(unknown);
            return info;
        }
        JSONObject regeoObj = (JSONObject) obj;
        String formattedAddress = regeoObj.getString("formatted_address");
        assembleAddress(unlocated, regeoObj, info, formattedAddress);
        info.setFormatted(true);
        return info;
    }

    private static void assembleAddress(String unLocated, JSONObject regeocode, PositionInfo info, String address) {
        if (EMPTY_ARRAY.equals(address) || EMPTY_OBJECT.equals(address)) {
            // 高德返回的数据如果为空，则该字段的值为: '[]'，返回未定位
            info.setFormattedAddress(unLocated);
            return;
        }
        Object obj = regeocode.get("addressComponent");
        if (obj instanceof JSONArray) {
            return;
        }
        JSONObject addressComponent = (JSONObject) obj;
        parseAddressComponent(info, addressComponent);
        JSONObject streetInfo = addressComponent.getJSONObject("streetNumber");
        if (streetInfo != null && streetInfo.get("number") instanceof String) {
            address = parseStreetNumber(info, streetInfo, address);
        } else {
            address = parsePOIs(info, regeocode, address);
        }
        info.setFormattedAddress(address);
    }

    private static void parseAddressComponent(PositionInfo info, JSONObject addressComponent) {
        info.setProvince(addressComponent.getString("province"));
        info.setCity(addressComponent.getString("city"));
        if (EMPTY_ARRAY.equals(info.getCity())) {
            info.setCity(addressComponent.getString("province"));
        }
        info.setCityCode(addressComponent.getString("citycode"));
        info.setDistrict(addressComponent.getString("district"));
        info.setAdcode(addressComponent.getString("adcode"));
        info.setTownship(addressComponent.getString("township"));
        info.setTownCode(addressComponent.getString("towncode"));
    }

    private static String parseStreetNumber(PositionInfo info, JSONObject obj, String address) {
        String street = obj.getString("street");
        info.setStreet(street);
        String number = obj.getString("number");
        info.setStreetNumber(number);
        String direction = obj.getString("direction");
        info.setDirection(direction);
        String distance = obj.getString("distance");
        info.setDistance(distance);
        StringBuilder builder = new StringBuilder(address);
        if (StringUtils.isNotBlank(street) && !street.equals(EMPTY_ARRAY) && !street.equals(EMPTY_OBJECT)) {
            int index = builder.lastIndexOf(street);
            if (index > -1) {
                builder.replace(index, index + street.length(), "");
            }
            builder.append(street);
        }
        if (StringUtils.isNotBlank(number) && !number.equals(EMPTY_ARRAY) && !number.equals(EMPTY_OBJECT)) {
            int index = builder.lastIndexOf(number);
            if (index > -1) {
                builder.replace(index, index + number.length(), "");
            }
            builder.append(number);
        }
        if (StringUtils.isNotBlank(direction) && !direction.equals(EMPTY_ARRAY) && !direction.equals(EMPTY_OBJECT)) {
            builder.append(direction);
        }
        if (StringUtils.isNotBlank(distance) && !distance.equals(EMPTY_ARRAY) && !distance.equals(EMPTY_OBJECT)) {
            builder.append(new Double(distance).longValue()).append("米");
        }
        return builder.toString();
    }

    private static String parsePOIs(PositionInfo info, JSONObject obj,  String address) {
        final JSONArray poiArray = obj.getJSONArray("pois");
        if (poiArray.isEmpty()) {
            return address;
        }
        JSONObject poi = poiArray.getJSONObject(0);
        String pois = poi.getString("name") + poi.getString("direction") + poi.getString("distance") + "米";
        info.setPois(pois);
        return address + pois;
    }

    public PositionInfo(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}
