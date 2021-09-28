package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.reportManagement.OutAreaReport;
import com.zw.platform.domain.reportManagement.OutAreaTotalTimeReport;
import com.zw.platform.service.reportManagement.OutAreaTotalTimeReportService;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.protocol.msg.t808.body.LocationInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 出区划累计时长统计报表Service
 * @author Administrator
 * @version 3.8.1
 */
@Service
public class OutAreaTotalTimeReportServiceImpl implements OutAreaTotalTimeReportService {
    private static final Logger log = LogManager.getLogger(OutAreaTotalTimeReportServiceImpl.class);

    @Autowired
    private UserService userService;


    /**
     * 查询出区划累计时长统计列表
     * @param monitorIds  监控对象ID
     * @param totalTime 累计时间
     * @param endTime   结束时间
     */
    @Override
    public JsonResultBean getOutAreaDurationStatisticsList(String monitorIds, Integer totalTime, String endTime) {
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_OUT_AREA_DURATION_STATISTICS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            RedisHelper.delete(redisKey);
        }

        List<OutAreaTotalTimeReport> outAreaTotalTimeReportList = new ArrayList<>();

        Map<String, String> queryParam = new HashMap<>(6);
        queryParam.put("endTime", endTime.replaceAll("-", ""));
        queryParam.put("monitorIds", monitorIds);
        queryParam.put("totalTime", String.valueOf(totalTime));
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.OUT_AREA_DURATION_STATISTICS, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || queryResultJsonObj.getInteger("code") != 10000) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询数据异常！");
        }
        List<OutAreaReport> outAreaReportList =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), OutAreaReport.class);
        if (CollectionUtils.isEmpty(outAreaReportList)) {
            return new JsonResultBean(outAreaTotalTimeReportList);
        }
        List<String> reportMonitorIds =
            outAreaReportList.stream().map(OutAreaReport::getMonitorId).collect(Collectors.toList());
        Map<String, BindDTO> bindInfoList = VehicleUtil.batchGetBindInfosByRedis(reportMonitorIds);
        userService.setObjectTypeName(bindInfoList.values());
        Map<String, LocationInfo> monitorLocationInfoMap = MonitorUtils.getLastLocationMap(reportMonitorIds);
        Set<String> locationSet = monitorLocationInfoMap.values()
            .stream()
            .map(obj -> obj.getLongitude() + "," + obj.getLatitude())
            .collect(Collectors.toSet());
        Map<String, String> addressPairMap = AddressUtil.batchInverseAddress(locationSet);
        for (OutAreaReport outAreaReport : outAreaReportList) {
            String monitorId = outAreaReport.getMonitorId();
            BindDTO bindDTO = bindInfoList.get(monitorId);
            if (bindDTO == null) {
                continue;
            }
            // 组装出区划累计时长统计信息
            LocationInfo locationInfo = monitorLocationInfoMap.get(monitorId);
            OutAreaTotalTimeReport outAreaTotalTimeReport = new OutAreaTotalTimeReport();
            outAreaTotalTimeReport.setPlateNumber(bindDTO.getName());
            outAreaTotalTimeReport.setGroupName(bindDTO.getOrgName());
            outAreaTotalTimeReport.setPlateColor(PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor()));
            outAreaTotalTimeReport.setVehicleType(bindDTO.getObjectTypeName());
            outAreaTotalTimeReport.setOutTime(DateUtil.getLongToDateStr(outAreaReport.getOutTime() * 1000, null));
            Integer outDuration = outAreaReport.getOutDuration();
            outAreaTotalTimeReport.setOutTotalTime(outDuration != null ? Long.valueOf(outDuration) : null);
            String address = "未定位";
            if (locationInfo != null) {
                String latitude = String.valueOf(locationInfo.getLatitude());
                String longitude = String.valueOf(locationInfo.getLongitude());
                address = addressPairMap.get(longitude + "," + latitude);
            }
            outAreaTotalTimeReport.setAddress(address);
            outAreaTotalTimeReportList.add(outAreaTotalTimeReport);
        }
        RedisHelper.addToList(redisKey, outAreaTotalTimeReportList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return new JsonResultBean(outAreaTotalTimeReportList);
    }

    /**
     * 导出出区划累计时长统计
     */
    @Override
    public void exportOutAreaDurationStatistics(HttpServletResponse response, String queryParam) throws IOException {
        List<OutAreaTotalTimeReport> outAreaTotalTimeReportList = new ArrayList<>();
        String userUuid = userService.getCurrentUserUuid();
        RedisKey redisKey = HistoryRedisKeyEnum.USER_OUT_AREA_DURATION_STATISTICS_LIST.of(userUuid);
        if (RedisHelper.isContainsKey(redisKey)) {
            outAreaTotalTimeReportList = RedisHelper.getList(redisKey, OutAreaTotalTimeReport.class);
        }
        if (CollectionUtils.isNotEmpty(outAreaTotalTimeReportList) && StringUtils.isNotBlank(queryParam)) {
            outAreaTotalTimeReportList = outAreaTotalTimeReportList
                .stream()
                .filter(info -> StringUtils.isNotBlank(info.getPlateNumber())
                    && info.getPlateNumber().contains(queryParam))
                .collect(Collectors.toList());
        }
        ExportExcelUtil.setResponseHead(response, "出区划累计时长报表");
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, outAreaTotalTimeReportList, OutAreaTotalTimeReport.class, null,
                response.getOutputStream()));
    }
}
