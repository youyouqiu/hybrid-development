package com.zw.app.service.personalCenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.service.personalCenter.AppOilMileageService;
import com.zw.app.service.personalCenter.AppWorkHourReportService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.oil.FuelConsumptionStatistics;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.reportManagement.TerminalPositional;
import com.zw.platform.domain.reportManagement.TotalMileage;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.service.oilmgt.OilStatisticalService;
import com.zw.platform.service.reportManagement.TerminalMileageReportService;
import com.zw.platform.service.reportManagement.impl.TerminalMileageReportServiceImpl;
import com.zw.platform.util.CalculateUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 @Author lijie
 @Date 2019/10/12 10:17
 @Description app综合统计油耗里程统计
 @version 1.0
 **/
@Service
@AppServerVersion
public class AppOilMileageServiceImpl implements AppOilMileageService {

    @Autowired
    OilStatisticalService oilStatisticalService;

    @Autowired
    private AppWorkHourReportService appWorkHourReportService;

    @Autowired
    TerminalMileageReportService terminalMileageReportService;


    /**
     * app油耗里程绑定了传感器的监控对象
     * @param page
     * @param pageSize
     * @param defaultSize(4.1.3)
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url = {
        "/clbs/app/reportManagement/oilMileage/getOilSensorMoniterIds" })
    public JSONObject findReferenceVehicleSeven(Long page, Long pageSize, Long defaultSize) throws Exception {
        return appWorkHourReportService
            .getSendSensorPollingMonitorInfoSeven(page, pageSize, defaultSize, Arrays.asList("0x45", "0x46"));
    }

    /**
     * 获取app油耗里程数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SIX, url = {
        "/clbs/app/reportManagement/oilMileage/list" })
    public JSONObject getOilMileageInfos(String vehicleId, String startTime, String endTime) throws Exception {
        String[] vehicles = vehicleId.split(",");
        List<JSONObject> list1 = new ArrayList<>();
        List<Positional> oilInfo = null;
        double totalFuelConsumption = 0D;
        double totalSteerMileage = 0D;
        oilInfo = oilStatisticalService.getAppOilInfo(Arrays.asList(vehicles), startTime, endTime);
        Map<String, List<Positional>> monitorOilData = new HashMap<>();
        if (CollectionUtils.isNotEmpty(oilInfo)) {
            oilInfo.forEach(oil -> oil.setId(UuidUtils.getUUIDStrFromBytes(oil.getVehicleId())));
            monitorOilData = oilInfo.stream().collect(Collectors.groupingBy(Positional::getId));
        }

        for (String vehicle : vehicles) {
            List<Positional> positionals = monitorOilData.get(vehicle);
            JSONObject jsonObject = getInfoDtails(positionals, vehicle);
            Double mile = getMileage(vehicle, startTime, endTime);
            jsonObject.put("totalSteerMileage", mile);
            totalFuelConsumption = totalFuelConsumption + jsonObject.getDoubleValue("totalFuelConsumption");
            totalSteerMileage = totalSteerMileage + mile;
            list1.add(jsonObject);
        }
        List<JSONObject> list2 = new ArrayList<>(list1);
        Collections.sort(list1, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return o2.getDoubleValue("totalFuelConsumption") - o1.getDoubleValue("totalFuelConsumption") > 0D ? 1 :
                    -1;
            }
        });

        Collections.sort(list2, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return o2.getDoubleValue("totalSteerMileage") - o1.getDoubleValue("totalSteerMileage") > 0D ? 1 : -1;
            }
        });

        JSONObject felConsumption = new JSONObject();
        String[] totalFuelConsumptionBrands = getHighLowVehicle(list1, "totalFuelConsumption");
        felConsumption.put("data", JSON.toJSONString(list1));
        felConsumption.put("totalFuelConsumption", totalFuelConsumption);
        felConsumption.put("highFuelConsumption", list1.get(0).getDoubleValue("totalFuelConsumption"));
        felConsumption.put("highVehicle", totalFuelConsumptionBrands[0]);
        felConsumption.put("lowFuelConsumption", list1.get(list1.size() - 1).getDoubleValue("totalFuelConsumption"));
        felConsumption.put("lowVehicle", totalFuelConsumptionBrands[1]);

        JSONObject steerMileage = new JSONObject();
        String[] totalSteerMileageBrands = getHighLowVehicle(list2, "totalSteerMileage");
        steerMileage.put("data", JSON.toJSONString(list2));
        steerMileage.put("totalSteerMileage", totalSteerMileage);
        steerMileage.put("highSteerMileage", list2.get(0).getDoubleValue("totalSteerMileage"));
        steerMileage.put("highVehicle", totalSteerMileageBrands[0]);
        steerMileage.put("lowSteerMileage", list2.get(list2.size() - 1).getDoubleValue("totalSteerMileage"));
        steerMileage.put("lowVehicle", totalSteerMileageBrands[1]);

        JSONObject re = new JSONObject();
        re.put("felConsumption", felConsumption);
        re.put("steerMileage", steerMileage);
        return re;
    }

    private Double getMileage(String vid, String startTime, String endTime) {
        Date date = new Date();
        String nowDay = LocalDateUtils.dateFormate(date);
        JSONObject object = null;
        final List<String> vehicleIds = Collections.singletonList(vid);
        if (endTime.substring(0, 10).equals(nowDay)) {
            //已经查了今天 历史数据不用再查今天的数据
            //endTime = Date8Utils.getMidnightDayTime(LocalDateTime.now());
            long start = LocalDateUtils.parseDateTime(nowDay + " 00:00:00").getTime() / 1000;
            long end = LocalDateUtils.parseDateTime(endTime).getTime() / 1000;
            List<TerminalPositional> terminalPositionalList = getTerminalPositionalList(vehicleIds, start, end);
            //过滤异常数据
            CommonUtil.filterTerminalPositional(terminalPositionalList);
            //组装今日数据
            object = terminalMileageReportService.buildTerminalMileageData(terminalPositionalList);
        }
        Map<String, List<TotalMileage>> totalMileMap = new HashMap<>();
        //4.包含之前的查询
        if (!startTime.substring(0, 10).equals(nowDay)) {
            //1.组装查询参数
            List<BigDataReportQuery> searchQuerys = TerminalMileageReportServiceImpl
                .getSearchQuery(startTime.substring(0, 10), endTime.substring(0, 10), vehicleIds);
            //历史每日里程
            List<TotalMileage> totalMileageList = new ArrayList<>();
            for (BigDataReportQuery query : searchQuerys) {
                totalMileageList.addAll(getTotalMileList(query));
            }
            //组装每日里程总
            totalMileMap = TerminalMileageReportServiceImpl.buildVehicleIdTotalMileMap(totalMileageList);
        }
        return buildResultData(object, totalMileMap, vid);
    }

    private List<TerminalPositional> getTerminalPositionalList(List<String> vehicleIds, long start, long end) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleIds", JSON.toJSONString(vehicleIds));
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_TERMINAL_POSITIONAL_LIST, params);
        return PaasCloudUrlUtil.getResultListData(str, TerminalPositional.class);
    }

    private List<TotalMileage> getTotalMileList(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_TOTAL_MILE_LIST, params);
        return PaasCloudUrlUtil.getResultListData(str, TotalMileage.class);
    }

    /**
     * 组装终端里程报表返回数据
     */
    private double buildResultData(JSONObject object, Map<String, List<TotalMileage>> totalMileMap, String vid) {

        double totalMile = object != null ? object.getDouble("totalMileage") : 0.0;

        if (totalMileMap.get(vid) != null) {
            totalMile += totalMileMap.get(vid).stream().map(TotalMileage::getGpsMile).reduce(Double::sum).get();
        }
        return totalMile;
    }

    private String[] getHighLowVehicle(List<JSONObject> list, String valueName) {
        String[] brands = new String[2];
        StringBuilder highBrandsBuilder = new StringBuilder();
        StringBuilder lowBrandsBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getDoubleValue(valueName) == list.get(0).getDoubleValue(valueName)) {
                highBrandsBuilder.append(list.get(i).getString("brand") + ",");
            }
            if (list.get(i).getDoubleValue(valueName) == list.get(list.size() - 1).getDoubleValue(valueName)) {
                lowBrandsBuilder.append(list.get(i).getString("brand") + ",");
            }
        }
        brands[0] = highBrandsBuilder.substring(0, highBrandsBuilder.length() - 1);
        brands[1] = lowBrandsBuilder.substring(0, lowBrandsBuilder.length() - 1);
        return brands;
    }

    private JSONObject getInfoDtails(List<Positional> oilInfo, String vehicle) throws Exception {
        JSONObject obj = new JSONObject();
        boolean flag = false;// 判断行驶状态开始标识
        FuelConsumptionStatistics mileage = null;
        double mile;
        int acc;
        double speed;
        Positional temp;
        Boolean flogKey =
            com.zw.platform.basic.core.RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicle));
        Double totalFuelConsumption = 0D;
        Double totalOilwearOne = 0D;
        if (oilInfo != null && oilInfo.size() > 0) {
            for (int i = 0, len = oilInfo.size(); i < len; i++) {
                temp = oilInfo.get(i);
                if (flogKey) {
                    if (temp.getMileageTotal() != null) {
                        mile = temp.getMileageTotal();
                    } else {
                        mile = 0;
                    }
                    if (temp.getMileageSpeed() != null) {
                        speed = temp.getMileageSpeed();
                    } else {
                        speed = 0;
                    }
                } else {
                    mile = Double.parseDouble(temp.getGpsMile());
                    speed = Double.parseDouble(temp.getSpeed());
                }
                acc = CalculateUtil.getStatus(String.valueOf(temp.getStatus())).getInteger("acc");
                String date = null;
                date = Converter.timeStamp2Date(String.valueOf(temp.getVtime()), null);
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
                    mileage.setStartMileage(mile);
                    // mileage.setStartOil(Double.parseDouble(temp.getTransientOilwearOne()));
                    mileage.setStartPositonal(temp.getLongtitude() + "," + temp.getLatitude());
                    mileage.setStartOil(
                        Double.parseDouble(temp.getTotalOilwearOne() == null ? "0" : temp.getTotalOilwearOne()));

                }

                if (mileage != null && !flag) {
                    // 行驶过程，每次更新行驶末尾状态
                    if (acc == 1 && speed != 0
                        && Double.parseDouble(temp.getTotalOilwearOne() == null ? "0" : temp.getTotalOilwearOne())
                        >= totalOilwearOne) {
                        mileage.setEndMileage(mile);
                        mileage.setSteerMileage((mileage.getEndMileage() - mileage.getStartMileage()));
                        // mileage.setMileageCount(Double.parseDouble(df.format(mileage.getEndMileage()
                        // - mileage.getStartMileage())));
                        mileage.setEndPositonal(temp.getLongtitude() + "," + temp.getLatitude());
                        mileage.setEndOil(Double.parseDouble(temp.getTotalOilwearOne()));
                        mileage.setFuelConsumption((mileage.getEndOil() - mileage.getStartOil()));
                        if (mileage.getFuelConsumption() != 0 && mileage.getSteerMileage() != 0) {
                            mileage.setPerHundredKilimeters(
                                (mileage.getFuelConsumption() / mileage.getSteerMileage()) * 100);
                        }
                        // 如果是最后一条记录，则需要写入list，否则到不符合怠速再写入list已经超过查询时间范围了，就会丢失一段行驶记录
                        if (i == oilInfo.size() - 1) {
                            totalFuelConsumption = totalFuelConsumption + mileage.getFuelConsumption();
                            //totalSteerMileage = totalSteerMileage + mileage.getSteerMileage();
                        }
                    } else {
                        // 行驶结束，写入list
                        // 如果只有开始时间，则舍弃这条数据
                        mileage.setPlateNumber(String.valueOf(temp.getPlateNumber()));
                        totalFuelConsumption = totalFuelConsumption + mileage.getFuelConsumption();
                        //将指针往前移一个，将本条从新计算成开始点
                        i = i - 1;
                        //totalSteerMileage = totalSteerMileage + mileage.getSteerMileage();
                        mileage = null;
                    }
                }
                totalOilwearOne =
                    Double.parseDouble(temp.getTotalOilwearOne() == null ? "0" : temp.getTotalOilwearOne());
            }
        }
        String brand = "";
        if (oilInfo != null && oilInfo.size() > 0) {
            brand = oilInfo.get(0).getPlateNumber();
        } else {
            brand = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(vehicle), "name");
        }
        obj.put("brand", brand);
        obj.put("vehicleId", vehicle);
        obj.put("totalFuelConsumption", totalFuelConsumption);
        return obj;
    }

    /**
     * 获取app油耗里程数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SIX, url = {
        "/clbs/app/reportManagement/oilMileage/detail" })
    public JSONArray getOilMileageDetail(String vehicleId, String startTime, String endTime) throws Exception {
        String stime = startTime.substring(0, 10);
        String etime = endTime.substring(0, 10);
        List<String> dayList = TerminalMileageReportServiceImpl.getDayList(stime, etime);
        JSONArray re = new JSONArray();
        for (int i = 0; i < dayList.size(); i++) {
            String start = dayList.get(i) + " 00:00:00";
            String end = dayList.get(i) + " 23:59:59";
            if (i == 0) {
                end = endTime;
            }
            List<Positional> oilInfo =
                oilStatisticalService.getAppOilInfo(Collections.singletonList(vehicleId), start, end);
            JSONObject jsonObject = getInfoDtails(oilInfo, vehicleId);
            Double mile = getMileage(vehicleId, start, end);
            jsonObject.put("totalSteerMileage", mile);
            jsonObject.put("time", dayList.get(i));
            re.add(jsonObject);
        }
        return re;
    }
}
