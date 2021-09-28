package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.reportManagement.PassCloudAlarmInfo;
import com.zw.platform.domain.reportManagement.PassCloudAlarmReport;
import com.zw.platform.domain.reportManagement.SpeedAlarm;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.service.reportManagement.SpeedAlarmService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j2
public class SpeedAlarmServiceImpl implements SpeedAlarmService {

    /**
     * 超速报警类型集合
     */
    private static final List<Integer> AlarmType = Arrays.asList(67, 76, 74, 117, 1, 164);

    @Autowired
    RealTimeServiceImpl realTime;

    @Autowired
    private PositionalService positionalService;

    @Override
    public boolean export(String title, int type, HttpServletResponse res, List<SpeedAlarm> speedAlarm)
        throws IOException {
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, speedAlarm, SpeedAlarm.class, null, res.getOutputStream()));
    }

    @Override
    public List<SpeedAlarm> getSpeedAlarmListByF3Pass(String vehicleId, String startTime, String endTime) {
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorIds", vehicleId);
        startTime = startTime.replaceAll("-", "");
        queryParam.put("startTime", startTime);
        endTime = endTime.replaceAll("-", "");
        queryParam.put("endTime", endTime);
        queryParam.put("alarmTypes", Joiner.on(",").join(AlarmType));
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.SPEED_ALARM_REPORT_URL, queryParam);
        JSONObject object = JSON.parseObject(queryResult);
        if (Objects.isNull(object) || object.getInteger("code") != 10000) {
            return null;
        }
        List<SpeedAlarm> result = new ArrayList<>();
        List<PassCloudAlarmReport> passCloudAlarmReportList =
            JSONObject.parseArray(object.getString("data"), PassCloudAlarmReport.class);
        if (CollectionUtils.isEmpty(passCloudAlarmReportList)) {
            return result;
        }
        Map<String, BindDTO> configInfoMap =
            VehicleUtil.batchGetBindInfosByRedis(Arrays.asList(vehicleId.split(",")));
        for (PassCloudAlarmReport passCloudAlarmReport : passCloudAlarmReportList) {
            BindDTO configInfo = configInfoMap.get(passCloudAlarmReport.getMonitorId());
            if (configInfo == null) {
                continue;
            }
            String monitorName = configInfo.getName();
            String assignmentName = configInfo.getGroupName();
            for (PassCloudAlarmInfo passCloudAlarmInfo : passCloudAlarmReport.getAlarmInfo()) {
                SpeedAlarm speedAlarm = new SpeedAlarm();
                speedAlarm.setPlateNumber(monitorName);
                speedAlarm.setAssignmentName(assignmentName);
                speedAlarm.setAlarmSource(passCloudAlarmInfo.getAlarmSource());
                Long alarmStartTime = passCloudAlarmInfo.getStartTime();
                speedAlarm.setAlarmStartTime(DateUtil.getLongToDateStr(alarmStartTime, null));
                speedAlarm.setStartTime(alarmStartTime);
                String startAddress = passCloudAlarmInfo.getStartAddress();
                String startLocation = passCloudAlarmInfo.getStartLocation();
                if (StringUtils.isBlank(startAddress) && StringUtils.isNotBlank(startLocation)) {
                    startAddress = getCity(startLocation);
                }
                speedAlarm.setAlarmStartLocation(startAddress);
                String endAddress = passCloudAlarmInfo.getEndAddress();
                String endLocation = passCloudAlarmInfo.getEndLocation();
                if (StringUtils.isBlank(endAddress) && StringUtils.isNotBlank(endLocation)) {
                    endAddress = getCity(endLocation);
                }
                speedAlarm.setAlarmEndLocation(endAddress);
                Double startSpeed = passCloudAlarmInfo.getStartSpeed();
                if (startSpeed != null) {
                    speedAlarm.setStartSpeed(String.valueOf(startSpeed));
                }
                Double endSpeed = passCloudAlarmInfo.getEndSpeed();
                if (endSpeed != null) {
                    speedAlarm.setEndSpeed(String.valueOf(endSpeed));
                }
                Long alarmEndTime = passCloudAlarmInfo.getEndTime();
                speedAlarm.setAlarmEndTime(DateUtil.getLongToDateStr(alarmEndTime, null));
                speedAlarm.setEndTime(alarmEndTime);
                Long duration = passCloudAlarmInfo.getDuration();
                if (duration != null) {
                    speedAlarm.setDuration(DateUtil.formatTime(duration));
                }
                result.add(speedAlarm);
            }
        }
        if (CollectionUtils.isNotEmpty(result)) {
            result.sort(Comparator.comparing(SpeedAlarm::getStartTime));
        }
        return result;
    }

    // 逆地址获取城市
    public String getCity(String location) {
        String[] address = location.split(",");
        String longitude = address[0];
        String latitude = address[1];
        if (longitude.length() > 8) {
            longitude = longitude.substring(0, 7);
        }
        if (latitude.length() > 7) {
            latitude = latitude.substring(0, 6);
        }
        return positionalService.getAddress(longitude, latitude);
    }

}
