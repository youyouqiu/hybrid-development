package com.zw.platform.service.reportManagement.impl;

import com.cb.platform.util.page.PassCloudResultUtil;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.BigDataReport.BigDataAlarmReport;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.reportManagement.AlarmInformation;
import com.zw.platform.service.reportManagement.AlarmReportService;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
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
public class AlarmReportServiceImpl implements AlarmReportService {

    @Autowired
    private UserService userService;

    @Override
    public List<AlarmInformation> getAlarmInformation(String vehicleId, String start, String end) throws Exception {
        //查询结果集合
        List<AlarmInformation> alarmInformationList = new ArrayList<>();
        // 为了注入报警类型次数。根据车牌号、开始时间和结束时间查询报警数据
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", vehicleId);
        param.put("startTime", start);
        param.put("endTime", end);
        String sendResult = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALARM_REPORT_INFO, param);
        List<AlarmHandle> alarmHandles = PaasCloudUrlUtil.getResultListData(sendResult, AlarmHandle.class);
        Map<String, List<AlarmHandle>> moNameAlarmListMap =
            alarmHandles.stream().collect(Collectors.groupingBy(AlarmHandle::getPlateNumber));
        // 为了得到车牌号，所属分组，从业人员
        List<String> vehicleIds = Arrays.asList(vehicleId.split(","));
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
        userService.setObjectTypeName(bindInfoMap.values());
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> groupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (String moId : vehicleIds) {
            BindDTO bindDTO = bindInfoMap.get(moId);
            if (bindDTO == null) {
                continue;
            }
            String moName = bindDTO.getName();
            // 用于判断是否有报警
            boolean flag = false;
            AlarmInformation alarmInformation = new AlarmInformation();
            alarmInformation.setPlateNumber(moName);
            String groupIds = bindDTO.getGroupId();
            String groupNames = Arrays.stream(groupIds.split(","))
                .map(groupIdAndNameMap::get)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
            alarmInformation.setAssignmentName(groupNames);
            alarmInformation.setPlateColor(PlateColor.getNameOrBlankByCode(bindDTO.getPlateColor()));
            alarmInformation.setVehicleType(bindDTO.getObjectTypeName());
            for (AlarmHandle alarmHandle : moNameAlarmListMap.getOrDefault(moName, new ArrayList<>())) {
                flag = assembleAlarmNumber(flag, alarmInformation, alarmHandle);
            }
            if (flag) {
                alarmInformationList.add(alarmInformation);
            }
        }
        alarmInformationList.sort((a1, a2) -> {
            Integer[] i1 = {a1.getMajorAlarm(), a1.getSpeedAlarm(), a1.getVehicleII(), a1.getTimeoutParking(),
                a1.getRouteDeviation(), a1.getTiredAlarm(), a1.getInOutArea(), a1.getInOutLine()};
            Integer[] i2 = {a2.getMajorAlarm(), a2.getSpeedAlarm(), a2.getVehicleII(), a2.getTimeoutParking(),
                a2.getRouteDeviation(), a2.getTiredAlarm(), a2.getInOutArea(), a2.getInOutLine()};
            Integer l1 = 0;
            Integer l2 = 0;
            for (int k = 0; k < i1.length; k++) {
                if (i1[k] != null) {
                    l1 += i1[k];
                }
                if (i2[k] != null) {
                    l2 += i2[k];
                }
            }
            return l2 - l1;
        });
        return alarmInformationList;
    }

    @Override
    public boolean exportQueryList(String vehicleId, String startTime, String endTime, String simpleQueryParam,
        int exportType) throws Exception {
        List<AlarmInformation> alarmInformationList;
        switch (exportType) {
            case 1:
                alarmInformationList = getAlarmInformation(vehicleId, startTime, endTime);
                break;
            case 2:
                alarmInformationList = getAlarmData(vehicleId, startTime, endTime);
                break;
            default:
                alarmInformationList = new ArrayList<>();
                break;
        }
        if (StringUtils.isNotBlank(simpleQueryParam)) {
            alarmInformationList = alarmInformationList.stream()
                .filter(obj -> StringUtils.isNotBlank(obj.getPlateNumber())
                    && obj.getPlateNumber().contains(simpleQueryParam))
                .collect(Collectors.toList());
        }
        RedisKey redisKey = HistoryRedisKeyEnum.USER_EXPORT_INFORMATION_LIST.of(userService.getCurrentUserUuid());
        RedisHelper.delete(redisKey);
        RedisHelper.addToList(redisKey, alarmInformationList);
        RedisHelper.expireKey(redisKey, RedisHelper.SIX_HOUR_REDIS_EXPIRE);
        return true;
    }

    private boolean assembleAlarmNumber(boolean flag, AlarmInformation alarmInformation, AlarmHandle alarmHandle) {
        int alarmType = alarmHandle.getAlarmType();
        Integer alarmNumber = alarmHandle.getAlarmNumber();
        switch (alarmType) {
            case 0:
                alarmInformation.setMajorAlarm(alarmInformation.getMajorAlarm() + alarmNumber);
                flag = true;
                break;
            case 1:
            case 76:
            case 67:
                alarmInformation.setSpeedAlarm(alarmInformation.getSpeedAlarm() + alarmNumber);
                flag = true;
                break;
            case 2:
            case 79:
                alarmInformation.setTiredAlarm(alarmInformation.getTiredAlarm() + alarmNumber);
                flag = true;
                break;
            case 19:
                alarmInformation.setTimeoutParking(alarmInformation.getTimeoutParking() + alarmNumber);
                flag = true;
                break;
            case 20:
            case 2011:
            case 2012:
            case 7211:
            case 7212:
                alarmInformation.setInOutArea(alarmInformation.getInOutArea() + alarmNumber);
                flag = true;
                break;
            case 21:
            case 2111:
            case 2112:
            case 7311:
            case 7312:
                alarmInformation.setInOutLine(alarmInformation.getInOutLine() + alarmNumber);
                flag = true;
                break;
            case 23:
            case 75:
                alarmInformation.setRouteDeviation(alarmInformation.getRouteDeviation() + alarmNumber);
                flag = true;
                break;
            case 27:
                alarmInformation.setVehicleII(alarmInformation.getVehicleII() + alarmNumber);
                flag = true;
                break;
            default:
                break;
        }
        return flag;
    }

    @Override
    public void export(HttpServletResponse httpResponse) throws Exception {
        ExportExcelUtil.setResponseHead(httpResponse, "报警信息统计列表");
        RedisKey redisKey = HistoryRedisKeyEnum.USER_EXPORT_INFORMATION_LIST.of(userService.getCurrentUserUuid());
        List<AlarmInformation> exportList = RedisHelper.getList(redisKey, AlarmInformation.class);
        ExportExcelUtil.export(
            new ExportExcelParam(null, 1, exportList, AlarmInformation.class, null, httpResponse.getOutputStream()));
    }

    @Override
    public List<AlarmInformation> getAlarmData(String vehicleIds, String startTime, String endTime) throws Exception {
        if (StringUtils.isBlank(vehicleIds) || !AppParamCheckUtil.checkDate(startTime, 2)
            || !AppParamCheckUtil.checkDate(endTime, 2)) {
            return null;
        }
        Map<String, String> param = new HashMap<>();
        param.put("monitorIds", vehicleIds);
        param.put("startTime", DateUtil.getStringToString(startTime, DateUtil.DATE_Y_M_D_FORMAT, DateUtil.DATE_FORMAT));
        param.put("endTime", DateUtil.getStringToString(endTime, DateUtil.DATE_Y_M_D_FORMAT, DateUtil.DATE_FORMAT));
        String sendResult = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_BIG_DATA_ALARM_REPORT_INFO, param);
        List<BigDataAlarmReport> listResult = PassCloudResultUtil.getListResult(sendResult, BigDataAlarmReport.class);
        if (CollectionUtils.isEmpty(listResult)) {
            return new ArrayList<>();
        }
        // 数据处理
        Map<String, AlarmInformation> alarmReport = new HashMap<>(16);
        List<String> monitorIds = listResult.stream()
            .map(alarmData -> UuidUtils.getUUIDFromBytes(alarmData.getVehicleIdByte()).toString())
            .distinct().collect(Collectors.toList());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIds);
        userService.setObjectTypeName(bindInfoMap.values());
        List<GroupDTO> userGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            userGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        for (BigDataAlarmReport alarm : listResult) {
            String monitorId = UuidUtils.getUUIDFromBytes(alarm.getVehicleIdByte()).toString();
            String monitorName = alarm.getMonitorName();
            if (StringUtils.isBlank(monitorName)) {
                continue;
            }
            AlarmInformation alarmInfo;
            if (alarmReport.containsKey(monitorName)) {
                alarmInfo = alarmReport.get(monitorName);
            } else {
                alarmInfo = new AlarmInformation();
                BindDTO bindDTO = bindInfoMap.get(monitorId);
                if (bindDTO != null) {
                    String groupIds = bindDTO.getGroupId();
                    String groupNames = Arrays.stream(groupIds.split(","))
                        .map(userGroupIdAndNameMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(","));
                    alarmInfo.setAssignmentName(groupNames);
                    alarmInfo.setVehicleType(bindDTO.getObjectTypeName());
                }
                alarmInfo.setPlateNumber(monitorName);
                alarmInfo.setPlateColor(alarm.getPlateColor());
                alarmReport.put(monitorName, alarmInfo);
            }
            Integer alarmNum = alarm.getAlarmNum();
            switch (alarm.getAlarmType()) {
                case 0:
                    alarmInfo.setMajorAlarm(alarmInfo.getMajorAlarm() + alarmNum);
                    break;
                case 1:
                case 76:
                case 67:
                case 164:
                    alarmInfo.setSpeedAlarm(alarmInfo.getSpeedAlarm() + alarmNum);
                    break;
                case 2:
                case 79:
                    alarmInfo.setTiredAlarm(alarmInfo.getTiredAlarm() + alarmNum);
                    break;
                case 19:
                    alarmInfo.setTimeoutParking(alarmInfo.getTimeoutParking() + alarmNum);
                    break;
                case 20:
                case 2011:
                case 2012:
                case 7211:
                case 7212:
                    alarmInfo.setInOutArea(alarmInfo.getInOutArea() + alarmNum);
                    break;
                case 21:
                case 2111:
                case 2112:
                case 7311:
                case 7312:
                    alarmInfo.setInOutLine(alarmInfo.getInOutLine() + alarmNum);
                    break;
                case 23:
                case 75:
                    alarmInfo.setRouteDeviation(alarmInfo.getRouteDeviation() + alarmNum);
                    break;
                case 27:
                    alarmInfo.setVehicleII(alarmInfo.getVehicleII() + alarmNum);
                    break;
                default:
                    break;
            }
        }
        if (MapUtils.isEmpty(alarmReport)) {
            return new ArrayList<>();
        }
        return alarmReport.values().stream().filter(obj -> obj.getAlarmNum() > 0).collect(Collectors.toList());
    }

}
