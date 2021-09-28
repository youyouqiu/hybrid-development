package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.BigDataReport.BigDataMonitorDataSum;
import com.zw.platform.domain.BigDataReport.BigDataReportArrivedCity;
import com.zw.platform.domain.BigDataReport.BigDataReportArrivedCityDO;
import com.zw.platform.domain.BigDataReport.BigDataReportPeriodMileData;
import com.zw.platform.domain.BigDataReport.PositionInfo;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.service.reportManagement.BigDataReportService;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BigDataReportServiceImpl implements BigDataReportService {
    private static final Logger logger = LogManager.getLogger(BigDataReportServiceImpl.class);

    @Autowired
    private VehicleService vehicleService;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    private void convertVehicleIds(List<PositionInfo> vehicles) {
        for (PositionInfo vehicle : vehicles) {
            vehicle.setVehicleId(UuidUtils.getUUIDFromBytes(vehicle.getVehicleIdHbase()).toString());
        }
    }

    @Override
    public int findSmallMileCount(Set<String> vehicleList, Double mile, Long startTime, Long endTime) {
        if (mile == null) {
            return 0;
        }
        List<byte[]> moIdByteList = vehicleList.stream()
                .distinct()
                .map(vId -> UuidUtils.getBytesFromUUID(UUID.fromString(vId)))
                .collect(Collectors.toList());
        BigDataReportQuery query = new BigDataReportQuery();
        query.setMonitorIds(moIdByteList);
        query.setMile(mile);
        query.setStartTime(startTime);
        query.setEndTime(endTime);

        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.FIND_SMALL_MILE_COUNT, params);
        return PaasCloudUrlUtil.getResultData(str, Integer.class);
    }

    @Override
    public PositionInfo getMouthSumByVehicle(String vehicleId, String brand, Long startTime, Long endTime) {
        if (StringUtils.isBlank(vehicleId)) {
            return null;
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("vehicleId", vehicleId);
        params.put("brand", brand);
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MONTH_SUM_BY_VEHICLE, params);
        return PaasCloudUrlUtil.getResultData(str, PositionInfo.class);
    }

    @Override
    public List<PositionInfo> getDaysByVehicle(String vehicleId, String brand, Long startTime, Long endTime) {
        if (StringUtils.isBlank(vehicleId)) {
            return null;
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("vehicleId", vehicleId);
        params.put("brand", brand);
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_DAYS_BY_VEHICLE, params);
        return PaasCloudUrlUtil.getResultListData(str, PositionInfo.class);
    }

    /**
     * 根据车辆id查询里程月统计数据
     * @param vehicleId 车辆id
     * @param brand     车牌号
     * @param groupId   组织id
     */
    @Override
    public JsonResultBean getMouthMileDataByVehicleId(String vehicleId, String brand, String groupId) {
        JSONObject objJson = new JSONObject();
        int smallMileCount = 0;
        // 获取当月第一天和最后一天
        long startTime = DateUtil.getFirstDayOfCurrentMonth();
        long endTime = DateUtil.getLastDayOfCurrentMonth();
        // 单车统计,单车一月统计数据
        PositionInfo positionInfo = getMouthSumByVehicle(vehicleId, brand, startTime, endTime);
        // 单车每天里程数据
        List<PositionInfo> positionList = getDaysByVehicle(vehicleId, brand, startTime, endTime);
        // 根据所选企业获取企业下所有组织下的车辆
        Set<String> vehicleList = vehicleService.getVehicleIdsByOrgId(groupId);
        // 查询里程小于当前车辆一个月里程的车辆数量
        if (positionInfo != null) {
            smallMileCount = findSmallMileCount(vehicleList, positionInfo.getGpsMile(), startTime, endTime);
        }
        objJson.put("monthDataByVehicle", positionInfo);
        objJson.put("dayDataByVehicle", positionList);
        objJson.put("smallMileCount", smallMileCount);
        return new JsonResultBean(objJson);
    }

    @Override
    public JSONObject queryBigDataValue(String groupId) {
        JSONObject msg = new JSONObject();
        // 当月里程
        Double totalMile = 0.0;
        // 当月行驶时长
        long totalTravelTime = 0;
        // 停驶时长
        long totalDownTime = 0;
        // 超速次数
        long totalOverSpeedTimes = 0;
        // 上月里程
        Double lastTotalGpsMile = 0.0;
        // 上月行驶时长
        long lastTotalTravelTime = 0;
        // 上月停驶时长
        long lastTotalDownTime = 0;
        // 上月超速次数
        long lastTotalOverSpeedTimes = 0;
        if (StringUtils.isBlank(groupId)) {
            judgeResult(msg);
            msg.put("vehicleCount", 0);
            return msg;
        }
        // 根据所选企业获取企业下所有组织下的车辆
        Set<String> vehicleList = vehicleService.getVehicleIdsByOrgId(groupId);
        if (CollectionUtils.isEmpty(vehicleList)) {
            judgeResult(msg);
            msg.put("vehicleCount", 0);
            return msg;
        }
        List<byte[]> moIdByteList = UuidUtils.batchTransition(vehicleList);
        String nowMonth = "now";
        String lastMonth = "last";
        try {
            // 组装上月周期里程对比数据
            List<BigDataReportPeriodMileData> lastMonthPeriodData = getMonitorPeriodData(moIdByteList, lastMonth);
            // 统计上月组织下的所有监控对象的行驶里程、行驶时长、停驶时长、超速次数
            BigDataMonitorDataSum lastMonthData = getMonitorCountData(moIdByteList, lastMonth);
            if (lastMonthData != null) {
                lastTotalGpsMile = lastMonthData.getTotalGpsMile();
                lastTotalTravelTime = lastMonthData.getTotalTravelTime();
                lastTotalDownTime = lastMonthData.getTotalDownTime();
                lastTotalOverSpeedTimes = lastMonthData.getTotalOverSpeedTimes();
            }
            msg.put("lastDailyMile", lastMonthPeriodData);
            msg.put("lastTotalMile", lastTotalGpsMile);
            msg.put("lastTotalTravelTime", lastTotalTravelTime);
            msg.put("lastTotalDownTime", lastTotalDownTime);
            msg.put("lastTotalOverSpeedTimes", lastTotalOverSpeedTimes);
        } catch (BadSqlGrammarException e) {
            logger.error("请检查大数据月表是否创建", e);
            msg = new JSONObject();
            msg.put("lastDailyMile", new ArrayList<>());
            msg.put("lastTotalMile", lastTotalGpsMile);
            msg.put("lastTotalTravelTime", lastTotalTravelTime);
            msg.put("lastTotalDownTime", lastTotalDownTime);
            msg.put("lastTotalOverSpeedTimes", lastTotalOverSpeedTimes);
        }
        try {
            // 统计当月组织下的所有监控对象的行驶里程、行驶时长、停驶时长、超速次数
            BigDataMonitorDataSum nowMonthData = getMonitorCountData(moIdByteList, nowMonth);
            if (nowMonthData != null) {
                totalMile = nowMonthData.getTotalGpsMile();
                totalTravelTime = nowMonthData.getTotalTravelTime();
                totalDownTime = nowMonthData.getTotalDownTime();
                totalOverSpeedTimes = nowMonthData.getTotalOverSpeedTimes();
            }
            // 组装本月周期里程对比数据
            List<BigDataReportPeriodMileData> nowMonthPeriodData = getMonitorPeriodData(moIdByteList, nowMonth);
            // 组装热点图
            assembleHotspot(msg, moIdByteList, nowMonth);
            // 组装排行榜数据
            rankingList(msg, moIdByteList, nowMonth, vehicleList);
            msg.put("totalMile", totalMile);
            msg.put("totalTravelTime", totalTravelTime);
            msg.put("totalDownTime", totalDownTime);
            msg.put("totalOverSpeedTimes", totalOverSpeedTimes);
            msg.put("dailyMile", nowMonthPeriodData);
        } catch (BadSqlGrammarException e) {
            logger.error("请检查大数据月表是否创建", e);
            msg = new JSONObject();
            judgeResult(msg);
        }
        msg.put("vehicleCount", moIdByteList.size());
        return msg;
    }

    /**
     * 大数据报表发生异常时,组装返回数据
     */
    private void judgeResult(JSONObject msg) {
        msg.put("lastDailyMile", new ArrayList<>());
        msg.put("lastTotalMile", 0.0);
        msg.put("lastTotalTravelTime", 0);
        msg.put("lastTotalDownTime", 0);
        msg.put("lastTotalOverSpeedTimes", 0);
        msg.put("totalMile", 0.0);
        msg.put("totalTravelTime", 0);
        msg.put("totalDownTime", 0);
        msg.put("totalOverSpeedTimes", 0);
        msg.put("dailyMile", new ArrayList<>());
        msg.put("sum", 0);
        msg.put("result", new ArrayList<>());
        msg.put("east", "");
        msg.put("west", "");
        msg.put("north", "");
        msg.put("south", "");
        msg.put("mileCompareBrands", new ArrayList<>());
        msg.put("mileCompareMiles", new ArrayList<>());
        msg.put("vehicleIds", new ArrayList<>());
        msg.put("mostDiligent", "");
        msg.put("mostLazy", "");
        msg.put("mostFar", "");
        msg.put("mintFar", "");
        msg.put("safe", "");
        msg.put("danger", "");
        msg.put("maxMile", 0);
        msg.put("minMile", 0);
        msg.put("validVehicleCount", 0);
    }

    /**
     * 根据指定月份和监控对象id查询所有监控对象一个月的里程、超速时长、停车时长、行驶时长数据
     */
    private BigDataMonitorDataSum getMonitorCountData(List<byte[]> moIdByteList, String month) {
        BigDataReportQuery query = paramDispose(moIdByteList, month);
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_TOTAL_DATA_BY_MONITORS, params);
        return PaasCloudUrlUtil.getResultData(str, BigDataMonitorDataSum.class);
    }

    /**
     * 组装周期对比数据
     */
    private List<BigDataReportPeriodMileData> getMonitorPeriodData(List<byte[]> moIdByteList, String month) {
        BigDataReportQuery query = paramDispose(moIdByteList, month);
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALL_MONITOR_DAILY_MILE, params);
        return PaasCloudUrlUtil.getResultListData(str, BigDataReportPeriodMileData.class);
    }

    /**
     * 组装查询参数
     */
    private BigDataReportQuery paramDispose(List<byte[]> moIdByteList, String month) {
        long startTime;
        long endTime;
        if ("now".equals(month)) {
            // 获取本月第一天和最后一天
            startTime = DateUtil.getFirstDayOfCurrentMonth();
            endTime = DateUtil.getLastDayOfCurrentMonth();
        } else {
            // 获取本月的上一个月的第一天和最后一天
            startTime = DateUtil.getFirstDayOfCurrentLastMonth();
            endTime = DateUtil.getLastDayOfCurrentLastMonth();
        }
        BigDataReportQuery query = new BigDataReportQuery();
        query.setMonitorIds(moIdByteList);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setMonth(DateFormatUtils.format(startTime * 1000, "yyyyMM"));
        return query;
    }

    /**
     * 组装热点图数据
     */
    private void assembleHotspot(JSONObject msg, List<byte[]> monitors, String month) {
        BigDataReportQuery query = paramDispose(monitors, month);
        int number = 0;
        // 查询所有监控对象当月经过的城市数据
        List<BigDataReportArrivedCity> city = getMonitorArrivedCity(query);
        if (CollectionUtils.isNotEmpty(city)) {
            for (BigDataReportArrivedCity data : city) {
                number += data.getCount();
            }
        }
        msg.put("sum", number);
        msg.put("result", city);

        // 查询监控对象到过的城市的坐标，保守估计内存占用<30MB
        final BigDataReportArrivedCityDO lngLats = getCityCoordinate(query);

        if (lngLats == null) {
            msg.put("east", "");
            msg.put("west", "");
            msg.put("south", "");
            msg.put("north", "");
            return;
        }
        try {
            String[] split = lngLats.getLongitudeEast().split("_");
            lngLats.setLongitudeEast(split[0]);
            lngLats.setLatitudeEast(split[1]);
            split = lngLats.getLongitudeWest().split("_");
            lngLats.setLongitudeWest(split[0]);
            lngLats.setLatitudeWest(split[1]);
            split = lngLats.getLongitudeSouth().split("_");
            lngLats.setLongitudeSouth(split[0]);
            lngLats.setLatitudeSouth(split[1]);
            split = lngLats.getLongitudeNorth().split("_");
            lngLats.setLongitudeNorth(split[0]);
            lngLats.setLatitudeNorth(split[1]);
            msg.put("east", getLocation(lngLats.getLongitudeEast(), lngLats.getLatitudeEast()));
            msg.put("west", getLocation(lngLats.getLongitudeWest(), lngLats.getLatitudeWest()));
            msg.put("south", getLocation(lngLats.getLongitudeSouth(), lngLats.getLatitudeSouth()));
            msg.put("north", getLocation(lngLats.getLongitudeNorth(), lngLats.getLatitudeNorth()));
        } catch (Exception e) {
            logger.error("组装热点图数据出错", e);
            msg.put("east", "");
            msg.put("west", "");
            msg.put("south", "");
            msg.put("north", "");
        }
    }

    private List<BigDataReportArrivedCity> getMonitorArrivedCity(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MONITOR_ARRIVED_CITY, params);
        return PaasCloudUrlUtil.getResultListData(str, BigDataReportArrivedCity.class);
    }

    private BigDataReportArrivedCityDO getCityCoordinate(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_CITY_COORDINATE, params);
        return PaasCloudUrlUtil.getResultData(str, BigDataReportArrivedCityDO.class);
    }

    private String getLocation(String lng, String lat) {
        // 9:02
        final String city = AddressUtil.inverseAddress(lng, lat).getCity();
        return StringUtils.isEmpty(city) ? (lng + ", " + lat) : city;
    }

    /**
     * 组装英雄榜
     */
    private void rankingList(JSONObject msg, List<byte[]> monitors, String month, Set<String> monitorList) {
        BigDataReportQuery query = paramDispose(monitors, month);
        // 本月最勤奋的车
        List<PositionInfo> mostDiligent = new ArrayList<>();
        // 本月最懒惰的车
        List<PositionInfo> mostLazy = new ArrayList<>();
        // 本月开得最远的车
        List<PositionInfo> mostFar = new ArrayList<>();
        // 本月几乎没动的车
        List<PositionInfo> mintFar = new ArrayList<>();
        // 本月最守规则的车
        List<PositionInfo> safe = new ArrayList<>();
        // 本月最危险的车
        List<PositionInfo> danger = new ArrayList<>();
        // 里程对比车牌号
        List<String> mileCompareBrands = new ArrayList<>();
        // 里程对比里程数
        List<String> mileCompareMiles = new ArrayList<>();
        // 有数据的车辆数量
        int validVehicleCount = 0;
        // 有有数据的车辆id lists
        List<String> validVehicleList = new ArrayList<>();
        // 没有直接从大数据月表中获取车牌号.原因为监控对象的车牌号是可变的,每天的记录的车牌号都可能不同.
        // 所以从缓存中取最新的,确保显示不会出错,但也增强了大数据报表和redis之间的耦合
        List<PositionInfo> activeVehicleList = getMonitorActiveData(query);
        // 批量获取监控对象绑定信息缓存
        Map<String, BindDTO> bindInfoMap =
                VehicleUtil.batchGetBindInfosByRedis(monitorList, Arrays.asList("id", "name"));
        // 车辆id
        List<String> vehicleIds = new ArrayList<>();
        for (PositionInfo info : activeVehicleList) {
            String veId = UuidUtils.getUUIDFromBytes(info.getVehicleIdHbase()).toString();
            BindDTO bindDTO = bindInfoMap.get(veId);
            if (bindDTO == null) {
                continue;
            }
            if (info.getGpsMile().intValue() != 0) {
                validVehicleCount = validVehicleCount + 1;
            }
            info.setPlateNumber(bindDTO.getName());
            info.setVehicleId(veId);
            mileCompareBrands.add(info.getPlateNumber());
            mileCompareMiles.add(DECIMAL_FORMAT.format(info.getGpsMile()));
            vehicleIds.add(veId);
            // 有里程的数据
            validVehicleList.add(veId);
            if (mostDiligent.isEmpty()) {
                // 将第一条数据都放到各个集中是为了后面方便比较,得到最合适的数据
                // 本月最勤奋的车(activeVehicleList降序排序,第一条数据的actionDay是最大一条)
                mostDiligent.add(info);
                // 本月最懒惰的车
                mostLazy.add(info);
                // 本月开得最远的车
                mostFar.add(info);
                // 本月几乎没动的车
                mintFar.add(info);
                // 本月最守规格的车
                safe.add(info);
                // 本月最危险的车
                danger.add(info);
                continue;
            }
            // 如果当前的数据的actionDay比第一条数据的action大,则将mostDiligent的值替换
            if (info.getActiveDays() > mostDiligent.get(0).getActiveDays()) {
                mostDiligent.clear();
                // 本月最勤奋的车
                mostDiligent.add(info);
                // 如果 当前数据的actionDay跟第一条数据的actionDay相同
            } else if (info.getActiveDays().intValue() == mostDiligent.get(0).getActiveDays().intValue()) {
                // 则比较两个监控对象本月的gps总里程,如果当前数据的gps总里程比mostDiligent集合的数据要大,则替换
                if (info.getGpsMile() > mostDiligent.get(0).getGpsMile()) {
                    mostDiligent.clear();
                    mostDiligent.add(info);
                    // 否则 如果两个监控对象的gps总里程相同,则并列到本月最勤奋的车中
                } else if (info.getGpsMile().intValue() == mostDiligent.get(0).getGpsMile().intValue()) {
                    mostDiligent.add(info);
                }
            }
            if (info.getActiveDays() < mostLazy.get(0).getActiveDays()) {
                mostLazy.clear();
                // 本月最懒惰的车
                mostLazy.add(info);
            } else if (info.getActiveDays().intValue() == mostLazy.get(0).getActiveDays().intValue()) {
                if (info.getGpsMile() < mostLazy.get(0).getGpsMile()) {
                    mostLazy.clear();
                    mostLazy.add(info);
                } else if (info.getGpsMile().equals(mostLazy.get(0).getGpsMile())) {
                    mostLazy.add(info);
                }
            }
            // 开得最多和最少的车
            if (info.getGpsMile() > mostFar.get(0).getGpsMile()) {
                mostFar.clear();
                // 本月开得最远的车
                mostFar.add(info);
            } else if (info.getGpsMile().equals(mostFar.get(0).getGpsMile())) {
                mostFar.add(info);
            }
            if (info.getGpsMile() < mintFar.get(0).getGpsMile()) {
                mintFar.clear();
                mintFar.add(info);
            } else if (info.getGpsMile().equals(mintFar.get(0).getGpsMile())) {
                mintFar.add(info);
            }
            // 最安全的车
            if (safe.get(0).getOverSpeedTimes() != 0) {
                // 若报警次数都不为0，比较单位里程的报警值
                if (info.getOverSpeedTimes() != 0) {
                    if (info.getOverSpeedTimes() / info.getGpsMile() < safe.get(0).getOverSpeedTimes() / safe.get(0)
                        .getGpsMile()) {
                        safe.clear();
                        safe.add(info);
                    } else if (info.getOverSpeedTimes() / info.getGpsMile() == safe.get(0).getOverSpeedTimes() / safe
                        .get(0).getGpsMile()) {
                        safe.add(info);
                    }
                    // 若后一个报警次数为空，则为最安全的车
                } else {
                    safe.clear();
                    safe.add(info);
                }
            } else {
                // 后一车辆报警也为0
                if (info.getOverSpeedTimes() == 0) {
                    // 比较里程，里程大的为最安全
                    if (info.getGpsMile() > safe.get(0).getGpsMile()) {
                        safe.clear();
                        safe.add(info);
                    } else if (info.getGpsMile().equals(safe.get(0).getGpsMile())) {
                        safe.add(info);
                    }
                }
            }
            // 最危险的车
            if (danger.get(0).getOverSpeedTimes() != 0) {
                // 若报警次数都不为0，比较单位里程的报警值
                if (info.getOverSpeedTimes() != 0) {
                    if (info.getOverSpeedTimes() / info.getGpsMile() > danger.get(0).getOverSpeedTimes() / danger.get(0)
                        .getGpsMile()) {
                        danger.clear();
                        danger.add(info);
                    } else if (info.getOverSpeedTimes() / info.getGpsMile()
                        == danger.get(0).getOverSpeedTimes() / danger.get(0).getGpsMile()) {
                        danger.add(info);
                    }
                }
            } else {
                // 后一车辆报警也为0
                if (info.getOverSpeedTimes() == 0) {
                    // 比较里程，里程小的最危险
                    if (info.getGpsMile() < danger.get(0).getGpsMile()) {
                        danger.clear();
                        danger.add(info);
                    } else if (info.getGpsMile().equals(danger.get(0).getGpsMile())) {
                        danger.add(info);
                    }
                } else {
                    danger.clear();
                    danger.add(info);
                }
            }
        }
        // 最懒惰的车包括没有数据的车（没有出车）
        List<String> inValidVehicleIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(monitorList)) {
            inValidVehicleIdList.addAll(monitorList);
        }
        inValidVehicleIdList.removeAll(validVehicleList);
        // 没有数据的车
        List<PositionInfo> inValidPositionList = new ArrayList<>();
        for (String ve : inValidVehicleIdList) {
            BindDTO bindDTO = bindInfoMap.get(ve);
            if (bindDTO != null) {
                String id = bindDTO.getId();
                String name = bindDTO.getName();
                PositionInfo pos = new PositionInfo();
                // 监控对象id
                pos.setVehicleId(id);
                // 监控对象车牌号
                pos.setPlateNumber(name);
                inValidPositionList.add(pos);
                vehicleIds.add(id);
                // 里程对比，没有数据的车也要显示
                mileCompareBrands.add(name);
                mileCompareMiles.add(DECIMAL_FORMAT.format(pos.getGpsMile()));
            }
        }
        if (CollectionUtils.isNotEmpty(inValidPositionList)) {
            // 组装最懒惰的车
            if (!mostLazy.isEmpty()) {
                // 若有行驶天数或者有行驶里程，最懒惰的车为没有出车的车
                if (mostLazy.get(0).getActiveDays() > 0 || mostLazy.get(0).getGpsMile() > 0) {
                    mostLazy.clear();
                }
            }
            mostLazy.addAll(inValidPositionList);

            // 组装几乎没动的车
            if (!mintFar.isEmpty()) {
                // 若有行驶里程，机会没动的车为没有出车的车
                if (mintFar.get(0).getGpsMile() > 0) {
                    mintFar.clear();
                }
            }
            mintFar.addAll(inValidPositionList);

        }
        msg.put("mileCompareBrands", mileCompareBrands);
        msg.put("mileCompareMiles", mileCompareMiles);
        msg.put("vehicleIds", vehicleIds);
        StringBuilder mostDiligentStr = new StringBuilder();
        // 本月最勤奋的车
        if (!mostDiligent.isEmpty()) {
            // 若小于三辆车
            if (mostDiligent.size() <= 3) {
                for (PositionInfo pos : mostDiligent) {
                    mostDiligentStr.append(pos.getPlateNumber()).append(",");
                }
                mostDiligentStr = new StringBuilder(mostDiligentStr.substring(0, mostDiligentStr.length() - 1));
                // 若大于三辆车，只显示三辆
            } else {
                mostDiligentStr = new StringBuilder(
                    mostDiligent.get(0).getPlateNumber() + "," + mostDiligent.get(1).getPlateNumber() + ","
                        + mostDiligent.get(2).getPlateNumber() + "...");
            }
        }
        msg.put("mostDiligent", mostDiligentStr.toString());

        // 最懒惰的车
        StringBuilder mostLazyStr = new StringBuilder();
        if (!mostLazy.isEmpty()) {
            // 当最懒惰的车小于3时，显示查询
            if (mostLazy.size() <= 3) {
                for (PositionInfo pos : mostLazy) {
                    mostLazyStr.append(pos.getPlateNumber()).append(",");
                }
                mostLazyStr = new StringBuilder(mostLazyStr.substring(0, mostLazyStr.length() - 1));
            } else {
                mostLazyStr = new StringBuilder(
                    mostLazy.get(0).getPlateNumber() + "," + mostLazy.get(1).getPlateNumber() + "," + mostLazy.get(2)
                        .getPlateNumber() + "...");
            }
        }
        msg.put("mostLazy", mostLazyStr.toString());

        // 跑得最远的车
        StringBuilder mostFarStr = new StringBuilder();
        if (!mostFar.isEmpty()) {
            if (mostFar.size() <= 3) {
                for (PositionInfo pos : mostFar) {
                    mostFarStr.append(pos.getPlateNumber()).append(",");
                }
                mostFarStr = new StringBuilder(mostFarStr.substring(0, mostFarStr.length() - 1));
            } else {
                mostFarStr = new StringBuilder(
                    mostFar.get(0).getPlateNumber() + "," + mostFar.get(1).getPlateNumber() + "," + mostFar.get(2)
                        .getPlateNumber() + "...");
            }
        }
        msg.put("mostFar", mostFarStr.toString());
        // 几乎没动的车
        StringBuilder mintFarStr = new StringBuilder();
        if (!mintFar.isEmpty()) {
            if (mintFar.size() <= 3) {
                for (PositionInfo pos : mintFar) {
                    mintFarStr.append(pos.getPlateNumber()).append(",");
                }
                mintFarStr = new StringBuilder(mintFarStr.substring(0, mintFarStr.length() - 1));
            } else {
                mintFarStr = new StringBuilder(
                    mintFar.get(0).getPlateNumber() + "," + mintFar.get(1).getPlateNumber() + "," + mintFar.get(2)
                        .getPlateNumber() + "...");
            }
        }
        msg.put("mintFar", mintFarStr.toString());
        // 最安全的车
        StringBuilder safeStr = new StringBuilder();
        if (!safe.isEmpty()) {
            if (safe.size() <= 3) {
                for (PositionInfo pos : safe) {
                    safeStr.append(pos.getPlateNumber()).append(",");
                }
                safeStr = new StringBuilder(safeStr.substring(0, safeStr.length() - 1));
            } else {
                safeStr = new StringBuilder(
                    safe.get(0).getPlateNumber() + "," + safe.get(1).getPlateNumber() + "," + safe.get(2)
                        .getPlateNumber() + "...");
            }
        }
        msg.put("safe", safeStr.toString());
        // 最危险的车
        String dangerStr = "";
        if (!danger.isEmpty()) {
            for (int d = 0; d < danger.size(); d++) {
                PositionInfo ps = danger.get(d);
                String brand = ps.getPlateNumber();
                if (danger.size() <= 3) {
                    dangerStr += brand + ",";
                    if (d == danger.size() - 1) {
                        dangerStr = dangerStr.substring(0, dangerStr.length() - 1);
                    }
                } else {
                    dangerStr = dangerStr + "...";
                    break;
                }
            }
        }
        msg.put("danger", dangerStr);
        // 最大里程
        msg.put("maxMile", !mostFar.isEmpty() ? DECIMAL_FORMAT.format(mostFar.get(0).getGpsMile()) : 0);
        // 最小里程
        msg.put("minMile", !mintFar.isEmpty() ? DECIMAL_FORMAT.format(mintFar.get(0).getGpsMile()) : 0);
        msg.put("validVehicleCount", validVehicleCount);
    }

    private List<PositionInfo> getMonitorActiveData(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MONITOR_ACTIVE_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, PositionInfo.class);
    }

    /**
     * 里程月统计数据
     */
    @Override
    public JSONObject queryMonitorMile(String vehicleId, String groupId) {
        JSONObject objJson = new JSONObject();
        int smallMileCount = 0;
        BigDataReportQuery query = paramDispose(null, "now");
        query.setMonitor(UuidUtils.getBytesFromStr(vehicleId));
        // 单车统计,单车一月统计数据
        PositionInfo positionInfo = getMonitorMouthSum(query);
        // 单车每天里程数据
        List<PositionInfo> positionList = getMonthDaysData(query);
        // 根据所选企业获取企业下所有组织下的车辆
        Set<String> vehicleList = vehicleService.getVehicleIdsByOrgId(groupId);
        if (vehicleList.size() > 0) {
            query.setMonitorIds(UuidUtils.batchTransition(vehicleList));
            // 查询里程小于当前车辆一个月里程的车辆数量
            if (positionInfo != null) {
                query.setMile(positionInfo.getGpsMile());
                smallMileCount = getSmallMileCount(query);
            }
        }
        objJson.put("monthDataByVehicle", positionInfo);
        objJson.put("dayDataByVehicle", positionList);
        objJson.put("smallMileCount", smallMileCount);
        return objJson;
    }

    private PositionInfo getMonitorMouthSum(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MONITOR_MOUTH_SUM, params);
        return PaasCloudUrlUtil.getResultData(str, PositionInfo.class);
    }

    private List<PositionInfo> getMonthDaysData(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MONTH_DAYS_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, PositionInfo.class);
    }

    private int getSmallMileCount(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_SMALL_MILE_COUNT, params);
        return PaasCloudUrlUtil.getResultData(str, Integer.class);
    }

}
