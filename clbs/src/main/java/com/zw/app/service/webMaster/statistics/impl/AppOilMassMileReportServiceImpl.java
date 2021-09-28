package com.zw.app.service.webMaster.statistics.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.monitor.OilMassAndMileData;
import com.zw.app.service.personalCenter.AppWorkHourReportService;
import com.zw.app.service.webMaster.statistics.AppOilMassMileReportService;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AppServerVersion
public class AppOilMassMileReportServiceImpl implements AppOilMassMileReportService {
    private Logger log = LogManager.getLogger(AppOilMassMileReportServiceImpl.class);

    @Autowired
    private AppWorkHourReportService appWorkHourReportService;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = "/clbs/app/statistic/oilMassAndMile/getData")
    public JSONObject getOilMassMileData(String monitorIds, String startTime, String endTime) throws Exception {
        if (AppParamCheckUtil.checkDate(startTime, 2) && AppParamCheckUtil.checkDate(endTime, 2)) {
            JSONObject msg = new JSONObject();
            List<String> arrayIds = new ArrayList<>(new HashSet<>(Arrays.asList(monitorIds.split(","))));
            startTime = startTime + " 00:00:00";
            endTime = endTime + " 23:59:59";
            Long queryStartTime = DateUtils.parseDate(startTime, "yyyy-MM-dd hh:mm:ss").getTime();
            Long queryEndTime = DateUtils.parseDate(endTime, "yyyy-MM-dd hh:mm:ss").getTime();
            List<OilMassAndMileData> allData = groupOffAndRealData(arrayIds, queryStartTime, queryEndTime);
            List<OilMassAndMileData> result = countOilMassMileData(allData, arrayIds, queryStartTime, queryEndTime);
            msg.put("result", result);
            return msg;
        }
        return null;
    }

    /**
     * 组合离线油量里程数据和实时油量里程数据
     */
    private List<OilMassAndMileData> groupOffAndRealData(
            List<String> monitorId, Long queryStartTime, Long queryEndTime) {
        List<BigDataReportQuery> bigDataQuery =
            BigDataQueryUtil.getBigMonthDataReportQuery(monitorId, queryStartTime, queryEndTime);
        return getOilMassAndMileDataToOffLine(bigDataQuery);
    }

    /**
     * 从hbase获取油量里程数据-离线
     */
    private List<OilMassAndMileData> getOilMassAndMileDataToOffLine(List<BigDataReportQuery> bigDataQuery) {
        List<OilMassAndMileData> oilMileDate = new ArrayList<>();
        if (bigDataQuery != null && bigDataQuery.size() > 0) {
            for (BigDataReportQuery query : bigDataQuery) {
                try {
                    oilMileDate.addAll(this.listOilMassAndMileData(query));
                } catch (Exception e) {
                    log.error("未发现离线报表MILEAGE_STATISTIC_" + query.getMonth());
                }
            }
        }
        return oilMileDate;
    }

    private List<OilMassAndMileData> listOilMassAndMileData(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_OIL_MASS_AND_MILE_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, OilMassAndMileData.class);
    }

    /**
     * 获取查询时间范围内,每一天的日期
     */
    private List<String> getQueryDay(Long queryStartTime, Long queryEndTime) throws Exception {
        if (queryStartTime == null || queryEndTime == null) {
            return null;
        }
        List<String> everyDay = BigDataQueryUtil.getTwoTimeMiddleEveryDayTime(queryStartTime, queryEndTime);
        List<String> resultEveryDay = new ArrayList<>();
        for (String dayStr : everyDay) {
            Date dayTime = DateUtils.parseDate(dayStr, "yyyyMMdd");
            String resultDayStr = DateFormatUtils.format(dayTime, "yyyy-MM-dd");
            resultEveryDay.add(resultDayStr);
        }
        return resultEveryDay;
    }

    /**
     * 统计油量里程数据
     */
    private List<OilMassAndMileData> countOilMassMileData(List<OilMassAndMileData> offlineResult,
        List<String> monitorIds, Long queryStartTime, Long queryEndTime) throws Exception {
        List<OilMassAndMileData> result = new ArrayList<>();
        final List<RedisKey> keys =
                monitorIds.stream().map(RedisKeyEnum.MONITOR_INFO::of).collect(Collectors.toList());
        final Map<String, String> idNameMap = RedisHelper.batchGetHashMap(keys, "id", "name");
        if (MapUtils.isEmpty(idNameMap)) {
            return result;
        }
        Map<String, List<OilMassAndMileData>> monitorOilData = new HashMap<>();
        if (CollectionUtils.isNotEmpty(offlineResult)) {
            offlineResult.forEach(oil -> oil.setMonitorStrId(UuidUtils.getUUIDStrFromBytes(oil.getMonitorId())));
            monitorOilData = offlineResult.stream().collect(Collectors.groupingBy(OilMassAndMileData::getMonitorStrId));
        }
        // 查询时间范围内,每天的日期
        List<String> everyDay = getQueryDay(queryStartTime, queryEndTime);
        if (CollectionUtils.isEmpty(everyDay)) {
            return result;
        }
        JSONObject dailyGpsMile;
        JSONObject dailyOilTank;
        JSONObject dailyFuelAmount;
        JSONObject dailyFuelSpill;
        String plateNumber;
        String vehicleId;
        OilMassAndMileData oilMassMile;
        final DecimalFormat decimalFormat = new DecimalFormat("#.#"); // 保留一位小数
        for (Map.Entry<String, String> entry : idNameMap.entrySet()) {
            vehicleId = entry.getKey(); // 监控对象id
            if (StringUtils.isBlank(vehicleId)) {
                continue;
            }
            plateNumber = entry.getValue();
            if (plateNumber == null) {
                continue;
            }
            Double sumGpsMile = 0.0; // GPS里程
            Double sumOilTank = 0.0; // 用油量
            Double sumFuelAmount = 0.0; // 加油量
            Double sumFuelSpill = 0.0; // 漏油量
            dailyGpsMile = getHaveDayObj(everyDay);
            dailyOilTank = getHaveDayObj(everyDay);
            dailyFuelAmount = getHaveDayObj(everyDay);
            dailyFuelSpill = getHaveDayObj(everyDay);
            List<OilMassAndMileData> monitorOilMassMileData = monitorOilData.get(vehicleId);
            if (CollectionUtils.isNotEmpty(monitorOilMassMileData)) {
                for (OilMassAndMileData data : monitorOilMassMileData) {
                    Long day = data.getDay();
                    if (day == null) {
                        continue;
                    }
                    String dayStr = DateFormatUtils.format(day * 1000, "yyyy-MM-dd");
                    if (data.getGpsMile() != null) {
                        sumGpsMile += data.getGpsMile();
                    }
                    if (data.getOilTank() != null) {
                        sumOilTank += data.getOilTank();
                    }
                    if (data.getFuelAmount() != null) {
                        sumFuelAmount += data.getFuelAmount();
                    }
                    if (data.getFuelSpill() != null) {
                        sumFuelSpill += data.getFuelSpill();
                    }
                    assembleDailyData(dayStr, data.getGpsMile(), dailyGpsMile);
                    assembleDailyData(dayStr, data.getOilTank(), dailyOilTank);
                    assembleDailyData(dayStr, data.getFuelAmount(), dailyFuelAmount);
                    assembleDailyData(dayStr, data.getFuelSpill(), dailyFuelSpill);
                }
            }
            sumGpsMile = Double.parseDouble(decimalFormat.format(Converter.toDouble(sumGpsMile)));
            sumFuelAmount = Double.parseDouble(decimalFormat.format(Converter.toDouble(sumFuelAmount)));
            sumFuelSpill = Double.parseDouble(decimalFormat.format(Converter.toDouble(sumFuelSpill)));
            sumOilTank = Double.parseDouble(decimalFormat.format(Converter.toDouble(sumOilTank)));
            oilMassMile = new OilMassAndMileData();
            oilMassMile.setMonitorStrId(vehicleId);
            oilMassMile.setGpsMile(sumGpsMile);
            oilMassMile.setFuelAmount(sumFuelAmount);
            oilMassMile.setFuelSpill(sumFuelSpill);
            oilMassMile.setOilTank(sumOilTank);
            oilMassMile.setDailyOilTank(dailyOilTank);
            oilMassMile.setDailyFuelAmount(dailyFuelAmount);
            oilMassMile.setDailyFuelSpill(dailyFuelSpill);
            oilMassMile.setDailyGpsMile(dailyGpsMile);
            oilMassMile.setMonitorName(plateNumber);
            result.add(oilMassMile);
        }
        return result;
    }

    /**
     * 组装数据
     */
    private void assembleDailyData(String dayStr, Double data, JSONObject dailyData) {
        if (data == null) {
            data = 0.0;
        }
        if (dailyData.containsKey(dayStr)) {
            Double value = dailyData.get(dayStr) != null ? dailyData.getDoubleValue(dayStr) : 0.0;
            Double resultGpsMile = value + data;
            dailyData.put(dayStr, resultGpsMile);
        } else {
            dailyData.put(dayStr, data);
        }
    }

    private JSONObject getHaveDayObj(List<String> everyDay) {
        // 初始化为有序的hashMap
        JSONObject obj = new JSONObject(16, true);
        if (CollectionUtils.isEmpty(everyDay)) {
            return obj;
        }
        for (String dayStr : everyDay) {
            obj.put(dayStr, 0.0);
        }
        return obj;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url =
        "/clbs/app/statistic/oilMassAndMile/judgeUserPollingOilMassMonitor")
    public boolean judgeUserPollingOilMassMonitor() throws Exception {
        return appWorkHourReportService.judgeUserIfOwnSendPollsMonitor(Arrays.asList("0x41", "0x42"));
    }


    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SEVEN, url =
        "/clbs/app/statistic/oilMassAndMile/getPollingOilMassMonitor")
    public JSONObject getPollingOilMassMonitorSeven(Long page, Long pageSize, Long defaultSize)
        throws Exception {
        return appWorkHourReportService
            .getSendSensorPollingMonitorInfoSeven(page, pageSize, defaultSize, Arrays.asList("0x41", "0x42"));
    }
}
