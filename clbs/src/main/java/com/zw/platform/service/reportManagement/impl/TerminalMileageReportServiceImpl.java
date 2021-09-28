package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.reportManagement.PassCloudMileageDailyDetail;
import com.zw.platform.domain.reportManagement.PassCloudMileageReport;
import com.zw.platform.domain.reportManagement.TerminalMileageDailyDetails;
import com.zw.platform.domain.reportManagement.TerminalMileageStatistics;
import com.zw.platform.domain.reportManagement.TerminalPositional;
import com.zw.platform.domain.reportManagement.TotalMileage;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.service.reportManagement.TerminalMileageReportService;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Administrator
 */
@Service
public class TerminalMileageReportServiceImpl implements TerminalMileageReportService {
    private static final Logger log = LogManager.getLogger(TerminalMileageReportServiceImpl.class);

    /**
     * 行驶
     */
    private static final String RUNNING_STATE = "1";

    /**
     * 停止
     */
    private static final String STOP_STATE = "2";

    /**
     * 判断行驶停止 速度阈值
     */
    private static final double LIMIT_SPEED = 5.0;

    @Autowired
    private UserService userService;


    /**
     * 获得 开始和结束时间之间的每天
     */
    public static List<String> getDayList(String startTime, String endTime) {
        List<String> result = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String localTime = endTime;
        result.add(localTime);
        while (!localTime.equals(startTime)) {
            localTime = df.format(LocalDate.parse(localTime, df).minusDays(1));
            result.add(localTime);
        }
        return result;
    }

    /**
     * 历史每日总里程数据 按VehicleId 分组
     */
    public static Map<String, List<TotalMileage>> buildVehicleIdTotalMileMap(List<TotalMileage> totalMileageList) {
        return totalMileageList.stream().peek(totalMileage -> {
            String vehicleId = UuidUtils.getUUIDFromBytes(totalMileage.getMonitorId()).toString();
            totalMileage.setMonitorIdStr(vehicleId);
        }).collect(Collectors.groupingBy(TotalMileage::getMonitorIdStr));
    }

    /**
     * 组装查询参数
     * @param startTime     开始时间
     * @param endTime       结束时间
     */
    public static List<BigDataReportQuery> getSearchQuery(String startTime, String endTime,
        List<String> vehicleIdList) {
        long startLong = LocalDateUtils.parseDateTime(startTime + " 00:00:00").getTime();
        long endLong = LocalDateUtils.parseDateTime(endTime + " 00:00:00").getTime();
        return BigDataQueryUtil.getBigMonthDataReportQuery(vehicleIdList, startLong, endLong);
    }

    /**
     * 组装 今日 总里程，怠速里程，行驶里程
     */
    @Override
    public JSONObject buildTerminalMileageData(List<TerminalPositional> terminalPositionalList) {
        JSONObject result = new JSONObject();
        result.put("totalMileage", 0.0);
        result.put("runMileage", 0.0);
        result.put("stopMileage", 0.0);
        TerminalPositional terminalPositional;
        TerminalPositional beforePositional;
        TerminalPositional startPositional = new TerminalPositional();
        if (CollectionUtils.isEmpty(terminalPositionalList)) {
            return result;
        }
        //初始化行驶状态
        initStatus(terminalPositionalList.get(0));
        //记录第一个点的里程
        startPositional.setGpsMile(terminalPositionalList.get(0).getGpsMile());
        for (int i = 0; i < terminalPositionalList.size(); i++) {
            // 当前计算的点
            terminalPositional = terminalPositionalList.get(i);
            if (i > 0) {
                // 如果连续两个点的时间差大于5分钟 行驶状态重新初始化 并且不计算 行驶和停止里程
                if (terminalPositionalList.get(i).getVtime() - terminalPositionalList.get(i - 1).getVtime() > 300) {
                    // 初始行驶状态
                    initStatus(terminalPositional);
                    //超过5分钟直接计算 马上计算里程
                    beforePositional = terminalPositionalList.get(i - 1);
                    double mile = Double.parseDouble(beforePositional.getGpsMile()) - Double
                        .parseDouble(startPositional.getGpsMile());
                    if (RUNNING_STATE.equals(beforePositional.getStatus())) {
                        result.put("runMileage", result.getDoubleValue("runMileage") + mile);
                    }
                    if (STOP_STATE.equals(beforePositional.getStatus())) {

                        result.put("stopMileage", result.getDoubleValue("stopMileage") + mile);
                    }
                    //重新记录第一个点
                    startPositional.setGpsMile(terminalPositionalList.get(i).getGpsMile());
                    if (i == terminalPositionalList.size() - 1) {
                        result.put("totalMileage", Double.parseDouble(terminalPositional.getGpsMile()) - Double
                            .parseDouble(terminalPositionalList.get(0).getGpsMile()));
                    }
                }
            }
            buildData(terminalPositionalList, terminalPositional, startPositional, i, result);
            if (i == terminalPositionalList.size() - 1) {
                result.put("totalMileage", Double.parseDouble(terminalPositional.getGpsMile()) - Double
                    .parseDouble(terminalPositionalList.get(0).getGpsMile()));
            }
        }
        return result;
    }

    private void buildData(List<TerminalPositional> terminalPositionalList, TerminalPositional terminalPositional,
        TerminalPositional startPositional, int index, JSONObject result) {
        //行驶开始计算
        if (RUNNING_STATE.equals(terminalPositional.getStatus())) {
            runStateBuild(terminalPositionalList, terminalPositional, startPositional, index, result);

        }
        //停止开始计算
        if (STOP_STATE.equals(terminalPositional.getStatus())) {
            stopStateBuild(terminalPositionalList, terminalPositional, startPositional, index, result);
        }
    }

    private void stopStateBuild(List<TerminalPositional> terminalPositionalList, TerminalPositional terminalPositional,
        TerminalPositional startPositional, int index, JSONObject result) {
        TerminalPositional nextTerminalPositional;
        List<TerminalPositional> subList;//判断后3个是否超出长度
        if (index + 1 < terminalPositionalList.size()) {
            nextTerminalPositional = terminalPositionalList.get(index + 1);
            subList = terminalPositionalList.stream().skip(index + 1).limit(3).collect(Collectors.toList());
            if (subList.size() >= 3) {
                double minSpeedDouble = subList
                    .stream()
                    .mapToDouble(obj -> StringUtils.isBlank(obj.getSpeed()) ? 0.0 : Double.parseDouble(obj.getSpeed()))
                    .min().getAsDouble();
                //最小值都大于5,那么所有的都大于5;
                if (minSpeedDouble > LIMIT_SPEED) {
                    nextTerminalPositional.setStatus(RUNNING_STATE);
                    double stopMileage = Double.parseDouble(nextTerminalPositional.getGpsMile()) - Double
                        .parseDouble(startPositional.getGpsMile());
                    result.put("stopMileage", result.getDoubleValue("stopMileage") + stopMileage);
                    startPositional.setGpsMile(terminalPositionalList.get(index + 1).getGpsMile());
                } else {
                    nextTerminalPositional.setStatus(STOP_STATE);
                }
            } else {
                nextTerminalPositional.setStatus(STOP_STATE);
            }
        } else {
            //最后一个点直接计算
            double stopMileage =
                Double.parseDouble(terminalPositional.getGpsMile()) - Double.parseDouble(startPositional.getGpsMile());
            result.put("stopMileage", result.getDoubleValue("stopMileage") + stopMileage);
        }
    }

    private void runStateBuild(List<TerminalPositional> terminalPositionalList, TerminalPositional terminalPositional,
        TerminalPositional startPositional, int index, JSONObject result) {
        TerminalPositional nextTerminalPositional;
        List<TerminalPositional> subList;
        //判断后5个是否超出长度
        if (index + 1 < terminalPositionalList.size()) {
            nextTerminalPositional = terminalPositionalList.get(index + 1);
            subList = terminalPositionalList.stream().skip(index + 1).limit(5).collect(Collectors.toList());
            if (subList.size() >= 5) {
                double maxSpeedDouble = subList
                    .stream()
                    .mapToDouble(obj -> StringUtils.isBlank(obj.getSpeed()) ? 0.0 : Double.parseDouble(obj.getSpeed()))
                    .max().getAsDouble();
                // 最大值都小于5,那么所有值都小于5;
                if (maxSpeedDouble <= LIMIT_SPEED) {
                    nextTerminalPositional.setStatus(STOP_STATE);
                    double runMileage = Double.parseDouble(nextTerminalPositional.getGpsMile()) - Double
                        .parseDouble(startPositional.getGpsMile());
                    result.put("runMileage", result.getDoubleValue("runMileage") + runMileage);
                    //重新初始化第一个点
                    startPositional.setGpsMile(terminalPositionalList.get(index + 1).getGpsMile());
                } else {
                    nextTerminalPositional.setStatus(RUNNING_STATE);
                }
            } else {
                nextTerminalPositional.setStatus(RUNNING_STATE);
            }
        } else {
            //最后一个点直接计算
            double runMileage =
                Double.parseDouble(terminalPositional.getGpsMile()) - Double.parseDouble(startPositional.getGpsMile());
            result.put("runMileage", result.getDoubleValue("runMileage") + runMileage);
        }
    }

    private void initStatus(TerminalPositional terminalPositional) {
        double speedDouble =
            terminalPositional.getSpeed() != null ? Double.parseDouble(terminalPositional.getSpeed()) : 0.0;
        if (speedDouble > LIMIT_SPEED) {
            terminalPositional.setStatus(RUNNING_STATE);
        } else {
            terminalPositional.setStatus(STOP_STATE);
        }
    }

    /**
     * 查询终端里程统计
     * @param monitorIds 监控对象id
     * @param startTime  开始时间
     * @param endTime    结束时间
     */
    @Override
    public JsonResultBean getTerminalMileageStatistics(String monitorIds, String startTime, String endTime) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_TERMINAL_MILEAGE_STATISTICS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        Map<String, String> queryParam = new HashMap<>(6);
        queryParam.put("startTime", startTime.replaceAll("-", ""));
        queryParam.put("monitorIds", monitorIds);
        queryParam.put("endTime", endTime.replaceAll("-", ""));
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.TERMINAL_MILEAGE_STATISTICS_URL, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
        }
        List<TerminalMileageStatistics> terminalMileageStatisticsList =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), TerminalMileageStatistics.class);
        if (CollectionUtils.isEmpty(terminalMileageStatisticsList)) {
            return new JsonResultBean(terminalMileageStatisticsList);
        }
        List<String> monitorIdList = terminalMileageStatisticsList.stream().map(TerminalMileageStatistics::getMonitorId)
            .collect(Collectors.toList());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIdList);
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> groupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (TerminalMileageStatistics terminalMileageStatistics : terminalMileageStatisticsList) {
            BindDTO bindDTO = bindInfoMap.get(terminalMileageStatistics.getMonitorId());
            if (bindDTO == null) {
                continue;
            }
            Double deviceMile = terminalMileageStatistics.getDeviceMile();
            Double deviceTravelMile = terminalMileageStatistics.getDeviceTravelMile();
            Double deviceIdlingMile = terminalMileageStatistics.getDeviceIdlingMile();
            terminalMileageStatistics.setTotalMile(deviceMile);
            terminalMileageStatistics.setTravelMile(deviceTravelMile);
            terminalMileageStatistics.setIdleSpeedMile(deviceIdlingMile);
            terminalMileageStatistics.setAbnormalMile(
                BigDecimal.valueOf(deviceMile).subtract(BigDecimal.valueOf(deviceTravelMile))
                    .subtract(BigDecimal.valueOf(deviceIdlingMile)).doubleValue());
            terminalMileageStatistics.setMonitorName(bindDTO.getName());
            terminalMileageStatistics.setGroupName(bindDTO.getOrgName());
            String groupIds = bindDTO.getGroupId();
            String groupNames = Arrays.stream(groupIds.split(","))
                .map(groupIdAndNameMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
            terminalMileageStatistics.setAssignmentName(groupNames);
            terminalMileageStatistics.setMonitorType(MonitorUtils.getMonitorTypeName(bindDTO.getMonitorType()));
        }
        RedisHelper.addToList(redisKey, terminalMileageStatisticsList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(terminalMileageStatisticsList);
    }

    /**
     * 查询终端里程明细
     * @param monitorId 监控对象id
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    @Override
    public JsonResultBean getTerminalMileageDailyDetail(String monitorId, String startTime, String endTime) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_TERMINAL_MILEAGE_DAILY_DETAIL_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }
        List<TerminalMileageDailyDetails> terminalMileageDailyDetailsList = new ArrayList<>();
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(monitorId);
        if (bindDTO == null) {
            return new JsonResultBean(terminalMileageDailyDetailsList);
        }
        Map<String, String> queryParam = new HashMap<>(6);
        queryParam.put("monitorIds", monitorId);
        queryParam.put("endTime", endTime.replaceAll("-", ""));
        queryParam.put("startTime", startTime.replaceAll("-", ""));
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.TERMINAL_MILEAGE_DAILY_DETAILS_URL, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
        }
        List<PassCloudMileageReport> passCloudMileageReportList =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), PassCloudMileageReport.class);
        if (CollectionUtils.isEmpty(passCloudMileageReportList)) {
            return new JsonResultBean(terminalMileageDailyDetailsList);
        }

        String carLicense = bindDTO.getName();
        String groupName = bindDTO.getOrgName();
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> groupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        String groupIds = bindDTO.getGroupId();
        String assignmentName = Arrays.stream(groupIds.split(","))
            .map(groupIdAndNameMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(","));
        String monitorType = MonitorUtils.getMonitorTypeName(bindDTO.getMonitorType());
        for (PassCloudMileageReport passCloudMileageReport : passCloudMileageReportList) {
            for (PassCloudMileageDailyDetail passCloudMileageDailyDetail : passCloudMileageReport.getDetail()) {
                TerminalMileageDailyDetails terminalMileageDailyDetails = new TerminalMileageDailyDetails();
                terminalMileageDailyDetails.setMonitorId(monitorId);
                terminalMileageDailyDetails.setMonitorName(carLicense);
                terminalMileageDailyDetails.setGroupName(groupName);
                terminalMileageDailyDetails.setAssignmentName(assignmentName);
                terminalMileageDailyDetails.setMonitorType(monitorType);
                Long day = passCloudMileageDailyDetail.getDay();
                terminalMileageDailyDetails.setDay(day);
                terminalMileageDailyDetails
                    .setDayDate(DateUtil.getLongToDateStr(day * 1000, DateUtil.DATE_Y_M_D_FORMAT));
                Double deviceMile = passCloudMileageDailyDetail.getDeviceMile();
                deviceMile = deviceMile == null ? 0.0 : deviceMile;
                terminalMileageDailyDetails
                    .setTotalMile(BigDecimal.valueOf(deviceMile).setScale(1, BigDecimal.ROUND_DOWN).doubleValue());
                Double deviceTravelMile = passCloudMileageDailyDetail.getDeviceTravelMile();
                deviceTravelMile = deviceTravelMile == null ? 0.0 : deviceTravelMile;
                terminalMileageDailyDetails.setTravelMile(
                    BigDecimal.valueOf(deviceTravelMile).setScale(1, BigDecimal.ROUND_DOWN).doubleValue());
                Double deviceDownMile = passCloudMileageDailyDetail.getDeviceDownMile();
                deviceDownMile = deviceDownMile == null ? 0.0 : deviceDownMile;
                terminalMileageDailyDetails.setIdleSpeedMile(
                    BigDecimal.valueOf(deviceDownMile).setScale(1, BigDecimal.ROUND_DOWN).doubleValue());
                double abnormalMile = BigDecimal.valueOf(deviceMile).subtract(BigDecimal.valueOf(deviceTravelMile))
                    .subtract(BigDecimal.valueOf(deviceDownMile)).doubleValue();
                terminalMileageDailyDetails
                    .setAbnormalMile(BigDecimal.valueOf(abnormalMile).setScale(1, BigDecimal.ROUND_DOWN).doubleValue());
                terminalMileageDailyDetailsList.add(terminalMileageDailyDetails);
            }
        }
        RedisHelper.addToList(redisKey, terminalMileageDailyDetailsList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(terminalMileageDailyDetailsList);
    }

    /**
     * 导出终端里程统计
     * @param response   response
     * @param monitorId  监控对象id
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param queryParam 查询参数
     */
    @Override
    public void exportTerminalMileageStatistics(HttpServletResponse response, String monitorId, String startTime,
        String endTime, String queryParam) throws IOException {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_TERMINAL_MILEAGE_STATISTICS_LIST.of(userUuid);
        List<TerminalMileageStatistics> terminalMileageStatisticsList = new ArrayList<>();
        if (RedisHelper.isContainsKey(redisKey)) {
            terminalMileageStatisticsList = RedisHelper.getList(redisKey, TerminalMileageStatistics.class);
        } else {
            JsonResultBean getTerminalMileageStatisticsResult =
                getTerminalMileageStatistics(monitorId, startTime, endTime);
            if (getTerminalMileageStatisticsResult.isSuccess()) {
                terminalMileageStatisticsList =
                    JSON.parseArray(JSON.toJSONString(getTerminalMileageStatisticsResult.getObj()),
                        TerminalMileageStatistics.class);
            } else {
                log.error("导出终端里程统计查询数据异常！");
            }
        }
        if (StringUtils.isNotBlank(queryParam) && CollectionUtils.isNotEmpty(terminalMileageStatisticsList)) {
            terminalMileageStatisticsList =
                terminalMileageStatisticsList.stream().filter(info -> info.getMonitorName().contains(queryParam))
                    .collect(Collectors.toList());
        }
        ExportExcelUtil.setResponseHead(response, "终端里程报表（里程统计）");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, terminalMileageStatisticsList, TerminalMileageStatistics.class, null,
                response.getOutputStream()));
    }

    /**
     * 导出终端里程每日明细
     * @param response  response
     * @param monitorId 开始时间
     * @param startTime 结束时间
     * @param endTime   查询参数
     */
    @Override
    public void exportTerminalMileageDailyDetail(HttpServletResponse response, String monitorId, String startTime,
        String endTime) throws IOException {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_TERMINAL_MILEAGE_DAILY_DETAIL_LIST.of(userUuid);
        List<TerminalMileageDailyDetails> terminalMileageDailyDetailsList = new ArrayList<>();
        if (RedisHelper.isContainsKey(redisKey)) {
            terminalMileageDailyDetailsList = RedisHelper.getList(redisKey, TerminalMileageDailyDetails.class);
        } else {
            JsonResultBean getTerminalMileageDailyDetailResult =
                getTerminalMileageDailyDetail(monitorId, startTime, endTime);
            if (getTerminalMileageDailyDetailResult.isSuccess()) {
                terminalMileageDailyDetailsList =
                    JSON.parseArray(JSON.toJSONString(getTerminalMileageDailyDetailResult.getObj()),
                        TerminalMileageDailyDetails.class);
            } else {
                log.error("导出终端里程每日明细查询数据异常！");
            }
        }
        ExportExcelUtil.setResponseHead(response, "终端里程报表（每日明细）");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, terminalMileageDailyDetailsList, TerminalMileageDailyDetails.class, null,
                response.getOutputStream()));
    }
}
