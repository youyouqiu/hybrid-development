package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.reportManagement.PassCloudAlarmInfo;
import com.zw.platform.domain.reportManagement.PassCloudAlarmReport;
import com.zw.platform.domain.reportManagement.SpeedReport;
import com.zw.platform.domain.reportManagement.SpeedingInfo;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;
import com.zw.platform.service.reportManagement.SpeedReportService;
import com.zw.platform.util.BigDataQueryUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SpeedReportServiceImpl implements SpeedReportService {
    private static Logger log = LogManager.getLogger(SpeedReportServiceImpl.class);

    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private UserService userService;

    @Override
    public List<SpeedReport> getSpeedAlarm(String vehicleList, String vehicleId, String startTime, String endTime)
        throws Exception {
        //查询数据处理
        Set<String> vehicleIds = Sets.newHashSet(vehicleId.split(","));
        Long start = DateUtil.getMillisecond(DateUtils.parseDate(startTime, DATE_FORMAT).getTime());
        Long end = DateUtil.getMillisecond(DateUtils.parseDate(endTime, DATE_FORMAT).getTime());
        // 根据车牌号、开始时间、结束时间查询超速数据
        List<SpeedReport> speedReports = this.listSpeedAlarm(vehicleIds, start, end);
        Map<String, BindDTO> bindInfos = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
        for (SpeedReport sr : speedReports) {
            UUID uuid = UuidUtils.getUUIDFromBytes(sr.getVehicleId()); // 监控对象id
            BindDTO bindInfo = bindInfos.get(uuid.toString());
            sr.setAssignmentName(bindInfo.getGroupName());
            sr.setProfessionalsName(bindInfo.getProfessionalNames());
            // 最大速度
            if (sr.getMinSpeed() == null) {
                sr.setMinSpeed(0.0);
            }
            // 最小速度
            if (sr.getMaxSpeed() == null) {
                sr.setMaxSpeed(0.0);
            }
            // 平均速度
            if (sr.getAverageSpeed() == null) {
                sr.setAverageSpeed(0.0);
            }
        }
        return speedReports;
    }

    private List<SpeedReport> listSpeedAlarm(Set<String> vehicleIds, Long start, Long end) {
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", JSON.toJSONString(vehicleIds));
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_SPEED_ALARM, params);
        return PaasCloudUrlUtil.getResultListData(str, SpeedReport.class);
    }

    @Override
    public List<SpeedReport> getSpeedReportBigData(String vehicleId, String startTime, String endTime)
        throws Exception {
        if (StringUtils.isNotBlank(vehicleId) && AppParamCheckUtil.checkDate(startTime, 2) && AppParamCheckUtil
            .checkDate(endTime, 2)) {
            /** 初始化查询参数 */
            List<String> vehicleIds = Arrays.asList(vehicleId.split(","));
            List<BigDataReportQuery> queries = BigDataQueryUtil.getBigDataReportQuery(vehicleIds, startTime, endTime);
            /** 查询数据 */
            List<SpeedReport> speedReports = new ArrayList<>();
            for (BigDataReportQuery query : queries) {
                try {
                    speedReports.addAll(getSpeedReportBigData(query));
                } catch (BadSqlGrammarException e) {
                    // 暂时不作处理
                }
            }
            /** 组装处理数据 */
            List<SpeedReport> resultData = new ArrayList<>();
            Map<String, SpeedReport> handleData = new HashMap<>();
            SpeedReport sr;
            SpeedReport handleSr;
            List<String> monitorIds = speedReports.stream()
                .map(speedReport -> UuidUtils.getUUIDFromBytes(speedReport.getVehicleId()).toString()).distinct()
                .collect(Collectors.toList());
            Map<String, BindDTO> configLists = VehicleUtil.batchGetBindInfosByRedis(monitorIds);
            for (int i = 0; i < speedReports.size(); i++) {
                sr = speedReports.get(i);
                Integer totalNum = sr.getTotalNum();
                // 若总记录条数为空或0则跳过这条数据
                if (totalNum == null || totalNum == 0) {
                    continue;
                }
                // 处理多月多天数据，一辆车只显示一条数据
                handleSr = handleData.get(sr.getPlateNumber());
                if (handleSr == null) {
                    handleData.put(sr.getPlateNumber(), sr);
                } else {
                    handleSr.setSpeedNumber(handleSr.getSpeedNumber() + sr.getSpeedNumber());
                    if (sr.getMinSpeed() < handleSr.getMinSpeed()) {
                        handleSr.setMinSpeed(sr.getMinSpeed());
                    }
                    if (sr.getMaxSpeed() > handleSr.getMaxSpeed()) {
                        handleSr.setMaxSpeed(sr.getMaxSpeed());
                    }
                    handleSr.setTotalNum(handleSr.getTotalNum() + sr.getTotalNum());
                    handleSr.setTotalSpeed(handleSr.getTotalSpeed() + sr.getTotalSpeed());
                    handleData.put(sr.getPlateNumber(), handleSr);
                }
            }
            // 计算平均速度
            for (SpeedReport speedReport : handleData.values()) {
                BigDecimal speed = new BigDecimal(speedReport.getTotalSpeed());
                BigDecimal number = new BigDecimal(speedReport.getTotalNum());
                double averageSpeed = speed.divide(number, 1, BigDecimal.ROUND_HALF_UP).doubleValue();
                speedReport.setAverageSpeed(averageSpeed);
                BindDTO configList = configLists.get(UuidUtils.getUUIDFromBytes(speedReport.getVehicleId()).toString());
                speedReport.setAssignmentName(configList.getGroupName());
                resultData.add(speedReport);
            }
            return resultData;
        }
        return null;
    }

    private List<SpeedReport> getSpeedReportBigData(BigDataReportQuery query) {
        Map<String, String> params = new HashMap<>(2);
        params.put("value", JSON.toJSONString(query));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_SPEED_REPORT_BIG_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, SpeedReport.class);
    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res, List<SpeedReport> speedReport)
        throws Exception {
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, speedReport, SpeedReport.class, null, res.getOutputStream()));
    }

    /**
     * 获得超速报表列表
     * @param monitorIds 监控对象id
     * @param startTime  开始时间
     * @param endTime    结束时间
     */
    @Override
    public JsonResultBean getSpeedingReportList(String monitorIds, String startTime, String endTime) {
        List<SpeedingInfo> speedingInfoList = new ArrayList<>();
        RedisKey speedingReportListKey =
            HistoryRedisKeyEnum.VEHICLE_SPEEDING_REPORT.of(userService.getCurrentUserUuid());
        RedisHelper.delete(speedingReportListKey);
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorIds", monitorIds);
        queryParam.put("startTime", startTime.replaceAll("-", ""));
        queryParam.put("endTime", endTime.replaceAll("-", ""));
        queryParam.put("alarmTypes", "1,164");
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.SPEEDING_REPORT_LIST_URL, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
        }
        List<PassCloudAlarmReport> passCloudAlarmReportList =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), PassCloudAlarmReport.class);
        if (CollectionUtils.isEmpty(passCloudAlarmReportList)) {
            return new JsonResultBean(speedingInfoList);
        }
        Map<String, BindDTO> configInfoMap = VehicleUtil.batchGetBindInfosByRedis(Arrays.asList(monitorIds.split(",")));
        for (PassCloudAlarmReport passCloudAlarmReport : passCloudAlarmReportList) {
            String monitorId = passCloudAlarmReport.getMonitorId();
            BindDTO configInfo = configInfoMap.get(monitorId);
            if (configInfo == null) {
                continue;
            }
            SpeedingInfo speedingInfo = new SpeedingInfo();
            speedingInfo.setMonitorId(monitorId);
            speedingInfo.setMonitorName(configInfo.getName());
            speedingInfo.setAssignmentName(configInfo.getGroupName());
            speedingInfo.setEmployeeName(configInfo.getProfessionalNames());
            // 最大速度
            Double maxSpeed = 0.0;
            // 最小速度
            Double minSpeed = 0.0;
            // 超速次数
            Integer overSpeedNum = 0;
            // 总速度
            Double totalSpeed = 0.0;
            for (PassCloudAlarmInfo passCloudAlarmInfo : passCloudAlarmReport.getAlarmInfo()) {
                Double alarmMaxSpeed = passCloudAlarmInfo.getMaxSpeed();
                if (alarmMaxSpeed != null) {
                    maxSpeed = alarmMaxSpeed > maxSpeed ? alarmMaxSpeed : maxSpeed;
                }
                Double alarmMinSpeed = passCloudAlarmInfo.getMinSpeed();
                if (alarmMinSpeed != null) {
                    minSpeed = alarmMinSpeed < minSpeed ? alarmMinSpeed : minSpeed;
                }
                Integer alarmNum = passCloudAlarmInfo.getAlarmNum();
                if (alarmNum != null) {
                    overSpeedNum += alarmNum;
                }
                Double alarmTotalSpeed = passCloudAlarmInfo.getTotalSpeed();
                if (alarmTotalSpeed != null) {
                    totalSpeed += alarmTotalSpeed;
                }
            }
            speedingInfo.setOverSpeedNum(overSpeedNum);
            speedingInfo.setMaxSpeed(maxSpeed);
            speedingInfo.setMinSpeed(minSpeed);
            speedingInfo.setAverageSpeed(
                new BigDecimal(totalSpeed / overSpeedNum).setScale(1, BigDecimal.ROUND_DOWN).doubleValue());
            speedingInfoList.add(speedingInfo);
        }
        RedisHelper.addToList(speedingReportListKey, speedingInfoList);
        RedisHelper.expireKey(speedingReportListKey, 60 * 60);
        return new JsonResultBean(speedingInfoList);
    }

    /**
     * 导出超速报表列表
     */
    @Override
    public void exportSpeedingReportList(HttpServletResponse response, String monitorIds, String startTime,
        String endTime) throws IOException {
        List<SpeedingInfo> speedingInfoList = new ArrayList<>();
        RedisKey speedingReportListKey =
            HistoryRedisKeyEnum.VEHICLE_SPEEDING_REPORT.of(userService.getCurrentUserUuid());
        // 先查询是否有缓存;有的话从缓存取 没有直接查询
        if (RedisHelper.isContainsKey(speedingReportListKey)) {
            speedingInfoList = RedisHelper.getList(speedingReportListKey, SpeedingInfo.class);
        } else {
            JsonResultBean getSpeedingReportListResult = getSpeedingReportList(monitorIds, startTime, endTime);
            if (getSpeedingReportListResult.isSuccess()) {
                speedingInfoList =
                    JSON.parseArray(JSON.toJSONString(getSpeedingReportListResult.getObj()), SpeedingInfo.class);
            } else {
                log.error("导出超速列表查询数据异常！");
            }
        }
        ExportExcelUtil.setResponseHead(response, "超速列表");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, speedingInfoList, SpeedingInfo.class, null, response.getOutputStream()));
    }

}
