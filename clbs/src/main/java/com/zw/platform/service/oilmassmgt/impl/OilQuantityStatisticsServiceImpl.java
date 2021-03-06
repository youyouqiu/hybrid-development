/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.service.oilmassmgt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oil.FuelConsumptionStatistics;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionlList;
import com.zw.platform.dto.reportManagement.OilStatistics;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.oilmassmgt.OilQuantityStatisticsService;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zw.platform.basic.core.RedisHelper.SIX_HOUR_REDIS_EXPIRE;

/**
 * ????????????Service????????? <p>Title: OilQuantityStatisticsServiceImpl.java</p> <p>Copyright: Copyright (c) 2016</p> <p>Company:
 * ZhongWei</p> <p>team: ZhongWeiTeam</p>
 * @version 1.0
 * @author: wangying
 * @date 2016???11???4?????????4:34:06
 */
@Service
public class OilQuantityStatisticsServiceImpl implements OilQuantityStatisticsService {
    private static Logger log = LogManager.getLogger(OilQuantityStatisticsServiceImpl.class);

    @Autowired
    private PositionalService positionalService;
    /**
     * ??????????????????
     */
    private static final String DATE_FORMAT1 = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_FORMAT2 = "yyyy-MM-dd";

    @Override
    public List<Positional> getOilMassInfo(String brand, String startTime, String endTime) throws Exception {
        List<Positional> list = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        long stime = DateUtils.parseDate(startTime, DATE_FORMAT1).getTime() / 1000;
        long ntime = DateUtils.parseDate(endTime, DATE_FORMAT1).getTime() / 1000;
        //?????????????????????????????????????????? ????????????
        if (stime > (System.currentTimeMillis() / 1000)) {
            return list;
        }
        String startTimeStr = DateUtil.getLongToDateStr(stime * 1000, DateUtil.DATE_FORMAT);
        String endTimeStr = DateUtil.getLongToDateStr(ntime * 1000, DateUtil.DATE_FORMAT);
        //??????paas-cloud api??????
        params.put("startTime", startTimeStr);
        params.put("endTime", endTimeStr);
        params.put("monitorId", brand);
        //??????paas-cloud ??????????????????????????????
        String result = HttpClientUtil.send(PaasCloudUrlEnum.SENSOR_OIL_QUANTITY_REPORT_URL, params);
        if (StringUtils.isBlank(result)) {
            return list;
        }
        String resultData = JSONObject.parseObject(result).getString("data");
        List<OilStatistics> resultList = JSON.parseArray(resultData, OilStatistics.class);
        if (CollectionUtils.isEmpty(resultList)) {
            return list;
        }
        String date = DateFormatUtils.format(new Date(), DATE_FORMAT2);
        if (startTime.contains(date) || endTime.contains(date)) {
            list = resultList.stream().map(this::convertOil).collect(Collectors.toList());
        } else {
            if ("00:00:00".equals(startTime.substring(11)) && "23:59:59".equals(endTime.substring(11))) {
                final RedisKey redisKey = HistoryRedisKeyEnum.STATS_OIL_VOLUME.of(brand, stime, ntime);
                list = RedisHelper.getListObj(redisKey, 1, -1);
                if (CollectionUtils.isEmpty(list)) {
                    list = resultList.stream().map(this::convertOil).collect(Collectors.toList());
                    RedisHelper.addObjectToList(redisKey, list, SIX_HOUR_REDIS_EXPIRE);
                }
            } else {
                list = resultList.stream().map(this::convertOil).collect(Collectors.toList());
            }
        }
        putToRedis(list);
        return list;
    }

    /**
     * ???paas-cloud????????????????????????CLBS?????????
     * @param oilStatistics
     * @return
     */
    private Positional convertOil(OilStatistics oilStatistics) {
        Positional p = new Positional();
        //??????????????????????????????????????????????????????????????????
        BeanUtils.copyProperties(oilStatistics, p);
        p.setPlateNumber(oilStatistics.getMonitorName());
        p.setVtime(oilStatistics.getVTime());
        p.setAcc(oilStatistics.getAcc().toString());
        return p;
    }

    @Override
    public JSONObject getInfoDtails(List<Positional> oilInfo, String vehicleId, Integer[] signal) throws Exception {
        JSONObject obj = new JSONObject();
        List<FuelConsumptionStatistics> list = new ArrayList<>();
        boolean flag = false;// ??????????????????????????????
        FuelConsumptionStatistics mileage = null;
        double mile = 0;
        int acc = 0;
        int air = 0;
        double speed = 0;
        double ctime = 0;
        double airTime = 0; // ????????????
        String accOpen = "";
        String accClose = "";
        String airOpen = "";
        String airClose = "";
        Positional temp = null;

        // ???????????????????????????????????????
        final RedisKey key = HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId);
        final Boolean flogKey = RedisHelper.isContainsKey(key);

        for (int i = 0, len = oilInfo.size(); i < len; i++) {
            temp = oilInfo.get(i);
            if (flogKey) { // ????????????????????????????????????????????????????????????
                if (temp.getMileageTotal() != null) {
                    mile = temp.getMileageTotal();
                }
                if (temp.getMileageSpeed() != null) {
                    speed = temp.getMileageSpeed();
                }
            } else { // ????????????????????????????????????Gps???????????????
                mile = Double.parseDouble(temp.getGpsMile());
                speed = Double.parseDouble(temp.getSpeed());
            }
            // mile = Double.parseDouble(temp.getGpsMile()) / 10;
            acc = CalculateUtil.getStatus(String.valueOf(temp.getStatus())).getInteger("acc");
            if (temp.getAirConditionStatus() != null) {
                air = Converter.toInteger(temp.getAirConditionStatus());
            }
            // air = Converter.toInteger(temp.getAirConditionStatus());
            // speed = Double.parseDouble(temp.getSpeed());
            String date = Converter.timeStamp2Date(String.valueOf(temp.getVtime()), null);
            if (flag) { // ??????????????????????????????????????????????????????????????????????????????2??????????????????????????????
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
                if (i == oilInfo.size() - 1) {
                    accClose =
                        Converter.timeStamp2Date(String.valueOf(oilInfo.get(oilInfo.size() - 1).getVtime()), null);
                    double duration = Double.parseDouble(CalculateUtil.toDateTime(accClose, accOpen));
                    ctime += duration;
                    accOpen = "";
                    accClose = "";
                }
            }
            // -------------??????????????????-start--------------
            if (signal[0] != 0 || signal[1] != 0) {
                air = airStatus(signal, temp);

            }

            if (acc == 1 && air != 0 && airOpen.equals("")) {
                airOpen = date;
            }
            if (acc == 1 && air == 0) { // ?????????
                if (airClose.equals("") && !airOpen.equals("")) {
                    airClose = date;
                    double duration = Converter.toDouble(CalculateUtil.toDateTime(airClose, airOpen));
                    airTime += duration;
                    airOpen = "";
                    airClose = "";
                }
            } else if (airClose.equals("") && !airOpen.equals("")) {
                if (i == oilInfo.size() - 1) {
                    airClose =
                        Converter.timeStamp2Date(String.valueOf(oilInfo.get(oilInfo.size() - 1).getVtime()), null);
                    double duration = Converter.toDouble(CalculateUtil.toDateTime(airClose, airOpen));
                    airTime += duration;
                    airOpen = "";
                    airClose = "";
                }
            }
            // --------------??????????????????-end-------------------
            if (mileage != null && !flag) {
                // ?????????????????????????????????????????????
                if (acc == 1 && speed != 0) {
                    mileage.setEndTime(date);
                    mileage.setSteerTime(
                        String.valueOf(CalculateUtil.toDateTimeS(mileage.getEndTime(), mileage.getStartTime())));
                    mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                    // ?????????????????????????????????????????????list????????????????????????????????????list??????????????????????????????????????????????????????????????????
                    if (i == oilInfo.size() - 1) {
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                } else { // ?????????????????????list
                    // ????????????????????????????????????????????????
                    if (mileage != null && mileage.getEndTime() != null) {
                        mileage.setEndTime(date);
                        mileage.setSteerTime(
                            String.valueOf(CalculateUtil.toDateTimeS(mileage.getEndTime(), mileage.getStartTime())));
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        list.add(mileage);
                    }
                    mileage = null;
                }

            }
        }
        obj.put("totalT", ctime);
        obj.put("totalAirTime", airTime);
        obj.put("infoDtail", list);
        return obj;
    }

    public Integer airStatus(Integer[] signal, Positional temp) {
        Integer airStatus = 0;
        if (signal[0] == 1) {
            if (null != temp.getIoOne()) {
                airStatus = temp.getIoOne();
            } else {
                return 0;
            }
        } else if (signal[0] == 2) {
            if (null != temp.getIoTwo()) {
                airStatus = temp.getIoTwo();
            } else {
                return 0;
            }
        } else if (signal[0] == 3) {
            if (null != temp.getIoThree()) {
                airStatus = temp.getIoThree();
            } else {
                return 0;
            }
        } else if (signal[0] == 4) {
            if (null != temp.getIoFour()) {
                airStatus = temp.getIoFour();
            } else {
                return 0;
            }
        }
        if (signal[1] == 1) {
            if (airStatus == 0) {
                airStatus = 1;
            } else {
                airStatus = 0;
            }
        }
        return airStatus;
    }

    public void putToRedis(List<Positional> positionalList) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Positional positional : positionalList) {
            Map<String, Object> map = new HashMap<>(32);
            map.put("id", positional.getId());
            map.put("vtime", positional.getVtime());
            map.put("status", positional.getStatus());
            map.put("longtitude", positional.getLongtitude());
            map.put("latitude", positional.getLatitude());
            map.put("speed", positional.getSpeed());
            map.put("temperture", positional.getTemperture());
            map.put("gpsMile", positional.getGpsMile());
            map.put("oilTankIdOne", positional.getOilTankIdOne());
            map.put("oilTankIdTwo", positional.getOilTankIdTwo());
            map.put("fuelTemOne", positional.getFuelTemOne());
            map.put("fuelTemTwo", positional.getFuelTemTwo());
            map.put("environmentTemOne", positional.getEnvironmentTemOne());
            map.put("environmentTemTwo", positional.getEnvironmentTemTwo());
            map.put("fuelAmountOne", positional.getFuelAmountOne());
            map.put("fuelAmountTwo", positional.getFuelAmountTwo());
            map.put("fuelSpillOne", positional.getFuelSpillOne());
            map.put("fuelSpillTwo", positional.getFuelSpillTwo());
            map.put("oilTankOne", positional.getOilTankOne());
            map.put("oilTankTwo", positional.getOilTankTwo());
            map.put("oilHeightOne", positional.getOilHeightOne());
            map.put("oilHeightTwo", positional.getOilHeightTwo());
            map.put("plateNumber", positional.getPlateNumber());
            map.put("airConditionStatus", positional.getAirConditionStatus());
            map.put("mileageSpeed", positional.getMileageSpeed());
            map.put("mileageTotal", positional.getMileageTotal());
            map.put("acc",
                    CalculateUtil.getStatus(String.valueOf(positional.getStatus())).getInteger("acc"));
            list.add(map);

        }

        try {
            saveCache(list);
        } catch (Exception e) {
            log.error("redis????????????" + e);
        }

    }

    private void saveCache(List<Map<String, Object>> list) {
        String username = SystemHelper.getCurrentUser().getUsername();
        final RedisKey keyAll = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "");
        final RedisKey keyAdd = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "-a");
        final RedisKey keySub = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "-s");
        RedisHelper.delete(Lists.newArrayList(keyAll, keyAdd, keySub));

        final List<String> valueAll = new ArrayList<>();
        final List<String> valueAdd = new ArrayList<>();
        final List<String> valueSub = new ArrayList<>();

        final List<RedisKey> expiringKeys = new ArrayList<>();
        expiringKeys.add(keyAll);
        expiringKeys.add(keyAdd);
        expiringKeys.add(keySub);

        // 2??????????????????contains?????????list???set???
        final List<String> ignoreList = Arrays.asList("0.0", "0");
        for (Map<String, Object> positional : list) {
            final String id = positional.get("id").toString();
            valueAll.add(id);
            if (!ignoreList.contains(positional.get("fuelAmountOne").toString())
                    || !ignoreList.contains(positional.get("fuelAmountTwo").toString())) {
                valueAdd.add(id);
            }
            if (!ignoreList.contains(positional.get("fuelSpillOne").toString())
                    || !ignoreList.contains(positional.get("fuelSpillTwo").toString())) {
                valueSub.add(id);
            }

            final RedisKey objectKey = HistoryRedisKeyEnum.STATS_OIL_DATA.of(id);
            final Map<String, String> billMap = RedisUtil.getEncapsulationObject(positional);
            RedisHelper.addToHash(objectKey, billMap);
            expiringKeys.add(objectKey);
        }
        RedisHelper.addToListTop(keyAll, valueAll);
        RedisHelper.addToListTop(keyAdd, valueAdd);
        RedisHelper.addToListTop(keySub, valueSub);

        RedisHelper.expireKeys(expiringKeys, SIX_HOUR_REDIS_EXPIRE);
    }

    /**
     * ??????????????????
     * @param response
     * @param type     1:???????????? 2:???????????? 3:????????????
     * @param vehicleId
     * @throws IOException
     */
    @Override
    public void exportOilPagInfoList(HttpServletResponse response, int type, String vehicleId)
        throws IOException {
        final String username = SystemHelper.getCurrentUsername();
        final RedisKey key;
        if (type == 1) {
            key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "");
            ExportExcelUtil.setResponseHead(response, "????????????(????????????)");
        } else if (type == 2) {
            key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "-a");
            ExportExcelUtil.setResponseHead(response, "????????????(????????????)");
        } else if (type == 3) {
            key = HistoryRedisKeyEnum.STATS_OIL_VOLUME_LIST.of(username, "-s");
            ExportExcelUtil.setResponseHead(response, "????????????(????????????)");
        } else {
            key = null;
        }
        final List<String> ids = key == null ? Collections.emptyList() : RedisHelper.getList(key);
        final List<RedisKey> objectKeys =
                ids.stream().map(HistoryRedisKeyEnum.STATS_OIL_DATA::of).collect(Collectors.toList());
        final List<Map<String, String>> exportList = new ArrayList<>(ids.size());
        for (List<RedisKey> redisKeys : Lists.partition(objectKeys, 1000)) {
            exportList.addAll(RedisHelper.batchGetHashMap(redisKeys));
        }

        List<PositionlList> result = new ArrayList<>();
        exportList.forEach(export -> {
            PositionlList data = JSON.parseObject(JSON.toJSONString(export), PositionlList.class);
            if (data == null) {
                return;
            }
            //?????????????????????????????????????????????????????????????????????????????????GPS?????????
            final RedisKey redisKey = HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId);
            final Boolean flogKey = RedisHelper.isContainsKey(redisKey);
            if (flogKey) {
                data.setMileForExport(data.getMileageTotal());
                data.setSpeedForExport(data.getMileageSpeed());
            } else {
                data.setMileForExport(Double.parseDouble(data.getGpsMile()));
                data.setSpeedForExport(Double.parseDouble(data.getSpeed()));
            }
            long time = data.getVtime() * 1000;
            String timeStr = DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
            data.setStime(timeStr);
            String acc = data.getStatus();
            if ("0".equals(acc)) {
                data.setStatus("???");
            } else {
                data.setStatus("???");
            }
            String environmentTemOne = data.getEnvironmentTemOne();
            String environmentTemTwo = data.getEnvironmentTemTwo();
            String fuelTemOne = data.getFuelTemOne();
            String fuelTemTwo = data.getFuelTemTwo();
            if (StringUtils.isBlank(environmentTemOne)) {
                data.setEnvironmentTemOne("0");
            } else if (Double.parseDouble(environmentTemOne) < 0 || Double.parseDouble(environmentTemOne) >= 80) {
                data.setEnvironmentTemOne("-");
            }
            if (StringUtils.isBlank(environmentTemTwo)) {
                data.setEnvironmentTemTwo("0");
            } else if (Double.parseDouble(environmentTemTwo) < 0 || Double.parseDouble(environmentTemTwo) >= 80) {
                data.setEnvironmentTemTwo("-");
            }
            if (StringUtils.isBlank(fuelTemOne)) {
                data.setFuelTemOne("0");
            } else if (Double.parseDouble(fuelTemOne) < 0 || Double.parseDouble(fuelTemOne) >= 80) {
                data.setFuelTemOne("-");
            }
            if (StringUtils.isBlank(fuelTemTwo)) {
                data.setFuelTemTwo("0");
            } else if (Double.parseDouble(fuelTemTwo) < 0 || Double.parseDouble(fuelTemTwo) >= 80) {
                data.setFuelTemTwo("-");
            }
            //???????????????????????????????????????
            String formattedAddress = positionalService.getAddress(data.getLongtitude(), data.getLatitude());
            data.setFormattedAddress(formattedAddress);
            //???????????????
            data.setOilTankTotal(Double.parseDouble(data.getOilTankOne()) + Double.parseDouble(data.getOilTankTwo()));
            result.add(data);
        });
        ExportExcelUtil
            .export(new ExportExcelParam("", 1, result, PositionlList.class, null, response.getOutputStream()));
    }
}
