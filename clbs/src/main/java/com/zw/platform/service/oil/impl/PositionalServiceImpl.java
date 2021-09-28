package com.zw.platform.service.oil.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.util.page.PassCloudResultBean;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.ObdTripDataInfo;
import com.zw.platform.domain.basicinfo.query.ObdTripDataQuery;
import com.zw.platform.domain.oil.FuelConsumptionStatistics;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.dto.reportManagement.SensorBaseDTO;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.zw.platform.basic.core.RedisHelper.SIX_HOUR_REDIS_EXPIRE;

/**
 * <p> Title: <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月27日 14:36
 */

@Service
@Log4j2
public class PositionalServiceImpl implements PositionalService {

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Value("${no.location}")
    private String unknownLocation;

    /**
     * 根据位置记录计算 行驶时长,怠速时长,总里程,平均速度
     * @param positionals 位置记录计算
     * @param type        里程统计方法 GPS_SENSOR GPS统计 MILE_SENSOR 里程传感器统计
     * @return json
     */
    @Override
    public JSONObject getStatisticalData(List<Positional> positionals, String type) {
        JSONObject msg = new JSONObject();
        if (positionals == null || positionals.size() == 0) {
            return msg;
        }
        List<FuelConsumptionStatistics> list = new ArrayList<>();
        boolean flag = false;// 判断行驶状态开始标识
        FuelConsumptionStatistics mileage = null;
        int acc = 0;
        Double speed = 0.0;
        double ctime = 0;
        String accOpen = "";
        String accClose = "";
        Positional temp = null;
        for (int i = 0, len = positionals.size(); i < len; i++) {
            temp = positionals.get(i);
            if (temp == null) {
                continue;
            }
            if (temp.getMileageSpeed() == null) {
                continue;
            }
            acc = CalculateUtil.getStatus(String.valueOf(temp.getStatus())).getInteger("acc");
            speed = temp.getMileageSpeed();
            String date = Converter.timeStamp2Date(String.valueOf(temp.getVtime()), null);
            // 表示前一次数据开始记录行驶，用于判断行驶状态是否满足2次，如不满足则不记录
            if (flag) {
                if (speed == 0 || acc == 0) {
                    mileage = null;
                }
                flag = false;
            }
            if (acc == 1 && speed != 0 && mileage == null) {
                flag = true;
                mileage = new FuelConsumptionStatistics();
                mileage.setStartTime(date);

            }
            if (acc == 1 && accOpen.equals("")) {
                accOpen = date;
            }
            if (acc == 0) {
                if (accClose.equals("") && !accOpen.equals("")) {
                    accClose = date;
                    double duration = Double.parseDouble(CalculateUtil.toDateTime(accClose, accOpen));
                    ctime += duration;
                    accOpen = "";
                    accClose = "";
                }
            } else if (accClose.equals("") && !accOpen.equals("")) {
                if (i == positionals.size() - 1) {
                    accClose = Converter
                        .timeStamp2Date(String.valueOf(positionals.get(positionals.size() - 1).getVtime()), null);
                    double duration = Double.parseDouble(CalculateUtil.toDateTime(accClose, accOpen));
                    ctime += duration;
                    accOpen = "";
                    accClose = "";
                }
            }
            if (mileage != null && !flag) {
                // 行驶过程，每次更新行驶末尾状态
                if (acc == 1 && speed != 0) {
                    mileage.setEndTime(date);
                    mileage.setSteerTime(
                        String.valueOf(CalculateUtil.toDateTimeS(mileage.getEndTime(), mileage.getStartTime())));
                    mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                    // 如果是最后一条记录，则需要写入list，否则到不符合怠速再写入list已经超过查询时间范围了，就会丢失一段行驶记录
                    if (i == positionals.size() - 1) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                } else { // 行驶结束，写入list
                    // 如果只有开始时间，则舍弃这条数据
                    if (mileage != null && mileage.getEndTime() != null) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                    mileage = null;
                }

            }
        }

        double travelTime = 0;// 行驶时长
        double nullData = 0;// 不记录时长
        if (list.size() > 0) {
            for (FuelConsumptionStatistics fs : list) {
                travelTime += (Double.parseDouble(fs.getSteerTime()) / 60 / 60);
            }
        }
        String idleTimeStr = "0小时0分0秒";
        if (ctime - travelTime - nullData > 0) {
            idleTimeStr = DateUtil.getToHhMmSs(ctime - travelTime - nullData);
        } else if (ctime - travelTime > 0 && ctime - travelTime - nullData < 0) {
            idleTimeStr = DateUtil.getToHhMmSs(ctime - travelTime);
        }
        Double totalMaile = 0.0;
        Double averageVelocity = 0.0;

        Double startMile = 0.0;
        Double endMile = 0.0;
        if ("".equals(type) || "GPS_SENSOR".equals(type)) {
            List<Positional> result = new ArrayList<>();
            for (Positional p : positionals) {
                if (p.getGpsMile() != null) {
                    result.add(p);
                }
            }
            for (int i = 0; i < result.size() - 2; i++) {
                startMile = Double.valueOf(result.get(i).getGpsMile());
                endMile = Double.valueOf(result.get(i + 1).getGpsMile());
                totalMaile += endMile - startMile;
            }
            // startMile = Double.valueOf(positionals.get(0).getGpsMile());
            // endMile = Double.valueOf(positionals.get(positionals.size() - 1).getGpsMile());
        }
        if ("MILE_SENSOR".equals(type)) {
            List<Positional> result = new ArrayList<>();
            for (Positional p : positionals) {
                if (p.getMileageTotal() != null) {
                    result.add(p);
                }
            }
            for (int i = 0; i < result.size() - 2; i++) {
                startMile = Double.valueOf(result.get(i).getMileageTotal());
                endMile = Double.valueOf(result.get(i + 1).getMileageTotal());
                totalMaile += endMile - startMile;
            }
        }
        BigDecimal ntotalMaile = null; // 总里程
        //if (startMile != null && endMile != null) {
        //   totalMaile = endMile - startMile;// 总里程
        ntotalMaile = new BigDecimal((totalMaile)).setScale(2, RoundingMode.UP);
        if (travelTime != 0) {
            BigDecimal bg = new BigDecimal((totalMaile / travelTime)).setScale(2, RoundingMode.UP);
            averageVelocity = bg.doubleValue();
        } else {
            averageVelocity = 0.0;
        }
        //}
        msg.put("travelTime", DateUtil.getToHhMmSs(travelTime));// 行使时长
        msg.put("idleTime", idleTimeStr);// 怠速时长
        msg.put("totalMaile", ntotalMaile + "km");// 总里程
        msg.put("averageVelocity", averageVelocity + "km/h");// 平均速度
        return msg;
    }

    @Override
    public List<Positional> getHistoryInfo(String vehicleId, String startTime, String endTime) throws Exception {
        List<Positional> list = new ArrayList<>();
        List<Positional> result = new ArrayList<>();
        long stime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        //如果查询开始时间大于当前时间 直接返回
        if (stime > (System.currentTimeMillis() / 1000)) {
            return list;
        }
        long ntime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long zero = calendar.getTime().getTime() / 1000;
        if (zero <= ntime) {
            list = getHistoryTrackInfo(vehicleId, stime, ntime);
            for (Positional p : list) {
                int acc = CalculateUtil.getStatus(String.valueOf(p.getStatus())).getInteger("acc");
                if (acc == 0) {
                    acc = 1;
                } else {
                    acc = 0;
                }
                p.setAcc(String.valueOf(acc));
            }
        } else {
            final RedisKey key = HistoryRedisKeyEnum.STATS_MILEAGE_LIST.of(vehicleId, stime, ntime);
            list = RedisHelper.getListObj(key, 1, -1);
            if (CollectionUtils.isEmpty(list)) {
                list = getHistoryTrackInfo(vehicleId, stime, ntime);
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Positional p : list) {
                        int acc = CalculateUtil.getStatus(String.valueOf(p.getStatus())).getInteger("acc");
                        if (acc == 0) {
                            acc = 1;
                        } else {
                            acc = 0;
                        }
                        p.setAcc(String.valueOf(acc));
                    }
                    RedisHelper.delete(key);
                    RedisHelper.addObjectToList(key, list, SIX_HOUR_REDIS_EXPIRE);
                }
            }
        }
        for (Positional p : list) {
            if (p.getMileageTotal() != null && p.getMileageSpeed() != null) {
                result.add(p);
            }
        }
        // 将查询结果存入redis中
        putToRedis(result);
        return list;
    }

    private List<Positional> getHistoryTrackInfo(String vehicleId, long stime, long ntime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(stime));
        params.put("endTime", String.valueOf(ntime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_HISTORY_TRACK_INFO, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public JSONObject getHistoryInfoByPaas(String vehicleId, String startTime,
                                     String endTime, JSONObject msg) throws Exception {
        List<Positional> list = new ArrayList<>();
        List<Positional> result = new ArrayList<>();
        long stime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        //如果查询开始时间大于当前时间 直接返回
        if (stime > (System.currentTimeMillis() / 1000)) {
            msg.put("positionals", ZipUtil.compress(JSON.toJSONString(list)));
            return msg;
        }
        long ntime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        String startTimeStr = DateUtil.getLongToDateStr(stime * 1000, DateUtil.DATE_FORMAT);
        String endTimeStr = DateUtil.getLongToDateStr(ntime * 1000, DateUtil.DATE_FORMAT);
        //组装paas-cloud api入参
        Map<String, String> params = new HashMap<>();
        params.put("startTime", startTimeStr);
        params.put("endTime", endTimeStr);
        params.put("monitorId", vehicleId);
        //调用paas-cloud 接口获取里程传感器详情数据
        String paasMileageStr = HttpClientUtil.send(PaasCloudUrlEnum.SENSOR_MILEAGE_REPORT_URL, params);
        JSONObject paasResultData = JSONObject.parseObject(paasMileageStr).getJSONObject("data");
        String paasPositonStr = paasResultData.getString("positionals");
        List<SensorBaseDTO> positionList = JSON.parseArray(paasPositonStr, SensorBaseDTO.class);
        if (CollectionUtils.isEmpty(positionList)) {
            msg.put("positionals", ZipUtil.compress(JSON.toJSONString(list)));
            return msg;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long zero = calendar.getTime().getTime() / 1000;
        if (zero <= ntime) {
            list = positionList.stream().map(this::convertMileage).collect(Collectors.toList());
        } else {
            final RedisKey key = HistoryRedisKeyEnum.STATS_MILEAGE_LIST.of(vehicleId, stime, ntime);
            RedisHelper.delete(key);
            list = positionList.stream().map(this::convertMileage).collect(Collectors.toList());
            RedisHelper.addObjectToList(key, list, SIX_HOUR_REDIS_EXPIRE);
        }
        for (Positional p : list) {
            if (p.getMileageTotal() != null && p.getMileageSpeed() != null) {
                result.add(p);
            }
        }
        // 将查询结果存入redis中,用于列表接口获取数据
        putToRedis(result);
        //组装返回参数
        String resultStr = JSON.toJSONString(list);
        resultStr = ZipUtil.compress(resultStr);
        msg.put("travelTime", paasResultData.getString("travelTime"));
        msg.put("totalMaile", paasResultData.getString("totalMaile"));
        msg.put("averageVelocity", paasResultData.getString("averageVelocity"));
        msg.put("idleTime", paasResultData.getString("idleTime"));
        msg.put("positionals", resultStr);
        return msg;
    }

    /**
     * 将paas-cloud返回实体转换为原CLBS的实体
     * @param sensorBaseDTO
     * @return
     */
    private Positional convertMileage(SensorBaseDTO sensorBaseDTO) {
        Positional p = new Positional();
        //字段一致的进行属性复制，不一致的进行手动组装
        BeanUtils.copyProperties(sensorBaseDTO, p);
        p.setPlateNumber(sensorBaseDTO.getMonitorName());
        p.setVtime(sensorBaseDTO.getVTime());
        p.setVtimeStr(sensorBaseDTO.getVTimeStr());
        int acc = sensorBaseDTO.getAcc();
        if (acc == 0) {
            acc = 1;
        } else {
            acc = 0;
        }
        p.setAcc(String.valueOf(acc));
        return p;
    }

    @Override
    public void putToRedis(List<Positional> positionalList) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = positionalList.size() - 1; i >= 0; i--) {
            Positional p = positionalList.get(i);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", p.getId());
            map.put("vtime", p.getVtime());
            map.put("status", p.getStatus());
            map.put("longtitude", p.getLongtitude());
            map.put("latitude", p.getLatitude());
            map.put("speed", p.getSpeed());
            map.put("temperture", p.getTemperture());
            map.put("gpsMile", p.getGpsMile());
            map.put("oilTankIdOne", p.getOilTankIdOne());
            map.put("oilTankIdTwo", p.getOilTankIdTwo());
            map.put("fuelTemOne", p.getFuelTemOne());
            map.put("fuelTemTwo", p.getFuelTemTwo());
            map.put("environmentTemOne", p.getEnvironmentTemOne());
            map.put("environmentTemTwo", p.getEnvironmentTemTwo());
            map.put("fuelAmountOne", p.getFuelAmountOne());
            map.put("fuelAmountTwo", p.getFuelAmountTwo());
            map.put("fuelSpillOne", p.getFuelSpillOne());
            map.put("fuelSpillTwo", p.getFuelSpillTwo());
            map.put("oilTankOne", p.getOilTankOne());
            map.put("oilTankTwo", p.getOilTankTwo());
            map.put("oilHeightOne", p.getOilHeightOne());
            map.put("oilHeightTwo", p.getOilHeightTwo());
            map.put("plateNumber", p.getPlateNumber());
            map.put("airConditionStatus", p.getAirConditionStatus());
            map.put("vtimeStr", DateUtil.getDateToString(DateUtil.getLongToDate(p.getVtime() * 1000), null));
            map.put("acc", CalculateUtil.getStatus(String.valueOf(p.getStatus())).getInteger("acc"));
            map.put("mileageTotal", p.getMileageTotal());
            map.put("mileageSpeed", p.getMileageSpeed());
            list.add(map);
        }

        try {
            saveCache(Lists.reverse(list));
        } catch (Exception e) {
            log.error("redis存储异常" + e);
        }
    }

    private void saveCache(List<Map<String, Object>> list) {
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey keyAll = HistoryRedisKeyEnum.STATS_MILEAGE.of(username);
        RedisHelper.delete(keyAll);

        final List<String> valueAll = new ArrayList<>();

        final List<RedisKey> expiringKeys = new ArrayList<>();
        expiringKeys.add(keyAll);

        for (Map<String, Object> positional : list) {
            final String id = positional.get("id").toString();
            valueAll.add(id);

            final RedisKey objectKey = HistoryRedisKeyEnum.STATS_MILEAGE_DATA.of(id);
            final Map<String, String> billMap = RedisUtil.getEncapsulationObject(positional);
            RedisHelper.addToHash(objectKey, billMap);
            expiringKeys.add(objectKey);
        }
        RedisHelper.addToListTop(keyAll, valueAll);

        RedisHelper.expireKeys(expiringKeys, SIX_HOUR_REDIS_EXPIRE);
    }

    @Override
    public String getAddress(String longitude, String latitude) {
        return AddressUtil.inverseAddress(longitude, latitude).getFormattedAddress();
    }

    @Override
    public JsonResultBean getObdTripDataList(String vehicleIds, String startTime, String endTime) throws Exception {
        final RedisKey key = RedisKeyEnum.OBD_TRIP_DATA_EXPORT_KEY.of(SystemHelper.getCurrentUsername());
        RedisHelper.delete(key);
        List<ObdTripDataInfo> result = new ArrayList<>();
        if (StringUtils.isNotBlank(vehicleIds) && StringUtils.isNotBlank(startTime) && StringUtils
            .isNotBlank(endTime)) {

            Map<String, String> param = new HashMap<>();
            param.put("monitorIds", vehicleIds);
            String startTimeQuery = DateUtil.formatDate(startTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT);
            String endTimeQuery = DateUtil.formatDate(endTime, DateUtil.DATE_FORMAT_SHORT, DateUtil.DATE_FORMAT);
            param.put("startTime", startTimeQuery);
            param.put("endTime", endTimeQuery);
            String sendResult = HttpClientUtil.send(PaasCloudUrlEnum.OBD_REPORT_URL, param);
            PassCloudResultBean passCloudResultBean = PassCloudResultBean.getDataInstance(sendResult);
            Object data = passCloudResultBean.getData();
            if (!passCloudResultBean.isSuccess()) {
                return new JsonResultBean(JsonResultBean.FAULT, passCloudResultBean.getMessage());
            }
            if (Objects.isNull(data)) {
                return new JsonResultBean();
            }
            JSONArray jsonArray = JSONObject.parseArray(data.toString());
            if (CollectionUtils.isNotEmpty(jsonArray)) {
                List<String> monitorIds = Arrays.asList(vehicleIds.split(","));
                final Map<String, BindDTO> infoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIds);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    assemblyData(result, infoMap, object);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(result)) {
            List<ObdTripDataInfo> sortedResultList = result.stream()
                    .sorted(Comparator.comparing(ObdTripDataInfo::getTripStartTime).reversed())
                    .collect(Collectors.toList());
            RedisHelper.addObjectToList(key, sortedResultList, SIX_HOUR_REDIS_EXPIRE);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private void assemblyData(List<ObdTripDataInfo> result, Map<String, BindDTO> configLists, JSONObject object) {
        ObdTripDataInfo info = new ObdTripDataInfo();
        String monitorId = object.getString("monitorId");
        BindDTO bindInfo = configLists.get(monitorId);
        info.setPlateNumber(bindInfo.getName());
        info.setGroupName(bindInfo.getOrgName());
        info.setAssignmentName(bindInfo.getGroupName());
        info.setBrakeNumber(object.getInteger("brakeNumber"));
        info.setClutchNumber(object.getInteger("clutchNumber"));
        info.setDriving100KmOilConsumption(object.getString("driving100KmOilConsumption"));
        info.setDrivingDuration(object.getString("drivingDuration"));
        info.setDrivingMileage(object.getString("drivingMileage"));
        info.setDrivingOilConsumption(object.getString("drivingOilConsumption"));
        info.setIdlingDuration(object.getString("idlingDuration"));
        info.setIdlingHourOilConsumption(object.getString("idlingHourOilConsumption"));
        info.setIdlingNumber(object.getInteger("idlingNumber"));
        info.setIdlingOilConsumption(object.getString("idlingOilConsumption"));
        info.setRapidAccelerationNumber(object.getInteger("rapidAccelerationNumber"));
        info.setRapidDecelerationNumber(object.getInteger("rapidDecelerationNumber"));
        info.setSharpTurnNumber(object.getInteger("sharpTurnNumber"));
        info.setTotalOilConsumption(object.getString("totalOilConsumption"));
        info.setTripDuration(object.getString("tripDuration"));
        info.setTripEndTime(object.getString("tripEndTime"));
        info.setTripStartTime(object.getString("tripStartTime"));
        result.add(info);
    }

    @Override
    public PageGridBean getTotalDataFormInfo(ObdTripDataQuery query) throws Exception {
        Page<ObdTripDataInfo> resultPage = new Page<>();
        try {
            final RedisKey key = RedisKeyEnum.OBD_TRIP_DATA_EXPORT_KEY.of(SystemHelper.getCurrentUsername());
            List<ObdTripDataInfo> obdTripDataList = RedisHelper.getListObj(key, 1, -1);
            List<ObdTripDataInfo> obdTripDataResultList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(obdTripDataList)) {
                String simpleQueryParam = query.getSimpleQueryParam();
                obdTripDataResultList.addAll(obdTripDataList);
                if (StringUtils.isNotBlank(simpleQueryParam)) {
                    obdTripDataResultList.clear();
                    List<ObdTripDataInfo> filterObdTripList =
                        obdTripDataList.stream().filter(info -> info.getPlateNumber().contains(simpleQueryParam))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(filterObdTripList)) {
                        obdTripDataResultList.addAll(filterObdTripList);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(obdTripDataResultList)) {
                //每页条数
                Long pageSize = query.getLength();
                //遍历开始条数
                int fromIndex = query.getStart().intValue();
                //页码
                int pageNum = query.getPage().intValue();
                //总条数
                int totalNum = obdTripDataResultList.size();
                //遍历结束条数
                long toIndex = pageSize > (totalNum - fromIndex) ? totalNum : (pageSize * pageNum);
                List<ObdTripDataInfo> subObdTripList = obdTripDataResultList.subList(fromIndex, (int) toIndex);
                resultPage = RedisQueryUtil.getListToPage(subObdTripList, query, totalNum);
            }
            return new PageGridBean(query, resultPage, true);
        } finally {
            resultPage.close();
        }
    }

    @Override
    public void exportObdTripDataList(HttpServletResponse response, String simpleQueryParam) throws Exception {
        ExportExcel export = new ExportExcel(null, ObdTripDataInfo.class, 1);
        final RedisKey key = RedisKeyEnum.OBD_TRIP_DATA_EXPORT_KEY.of(SystemHelper.getCurrentUsername());
        List<ObdTripDataInfo> allExportList = RedisHelper.getListObj(key, 1, -1);
        List<ObdTripDataInfo> exportList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allExportList)) {
            exportList.addAll(allExportList);
            //模糊搜索需要过滤 如果没有搜索则全部导出
            if (org.apache.commons.lang3.StringUtils.isNotBlank(simpleQueryParam)) {
                exportList.clear();
                List<ObdTripDataInfo> filterExportList =
                    allExportList.stream().filter(info -> info.getPlateNumber().contains(simpleQueryParam))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(filterExportList)) {
                    exportList.addAll(filterExportList);
                }
            }
        }
        export.setDataList(exportList);
        OutputStream out = response.getOutputStream();
        export.write(out);
        out.close();
    }

}
