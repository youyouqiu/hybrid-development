package com.zw.lkyw.service.realTimeMonitoring.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.lkyw.domain.AlarmNumberCount;
import com.zw.lkyw.domain.AlarmQuantity;
import com.zw.lkyw.domain.MonitorAlarmCount;
import com.zw.lkyw.domain.ReportMenu;
import com.zw.lkyw.service.realTimeMonitoring.LkywRealTimeMonitoringService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.alarm.AlarmInfo;
import com.zw.platform.domain.basicinfo.MonitorAccStatus;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.util.alarm.PassCloudAlarmUrlUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.report.PaasCloudAlarmUrlEnum;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.vehicle.VehicleStatus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/***
 @Author zhengjc
 @Date 2019/12/30 10:15
 @Description 两客一危service实现类
 @version 1.0
 **/
@Service
public class LkywRealTimeMonitoringServiceImpl implements LkywRealTimeMonitoringService {

    @Autowired
    private RoleService roleService;

    @Autowired
    private AlarmSearchService alarmSearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private MonitorService monitorService;
    /**
     * 808报警
     */
    private static final String MARK_808_ALARM = "808Alarm";

    @Override
    public Map<String, List<ReportMenu>> getUerReportMenu() {
        return roleService.getUserIntersectionMenu();
    }

    /**
     * 查询报警记录
     * @param latestTime         列表中最新的时间
     * @param latestAlarmDataStr 列表中报警时间为最新时间的报警数据(监控对象id|报警类型|报警时间,监控对象id|报警类型|报警时间)
     * @param oldestTime         列表中最老的时间
     * @param oldestAlarmDataStr 列表中报警时间为最老时间的报警数据(监控对象id|报警类型|报警时间,监控对象id|报警类型|报警时间)
     * @param alarmTypeStr       报警类型逗号分隔(不传就是查询所有需要展示的报警类型)
     * @param mark               页签标识
     */
    @Override
    public JsonResultBean getTodayAlarmRecord(String latestTime, String latestAlarmDataStr, String oldestTime,
        String oldestAlarmDataStr, String alarmTypeStr, String mark) throws Exception {
        List<AlarmInfo> resultAlarmList = new ArrayList<>();
        List<String> needQueryAlarmMonitorIds =
            getNeedQueryAlarmMonitorIds(latestAlarmDataStr, oldestAlarmDataStr, mark);
        if (CollectionUtils.isEmpty(needQueryAlarmMonitorIds)) {
            return new JsonResultBean(resultAlarmList);
        }
        if (StringUtils.isBlank(alarmTypeStr)) {
            alarmTypeStr = StringUtils.join(AlarmQuantity.NEED_COUNT_808_ALARM_TYPES, ",");
        }
        // 如果最新和最老时间都为空就是直接查询最新的50条报警记录
        if (StringUtils.isBlank(latestTime) && StringUtils.isBlank(oldestTime)) {
            List<AlarmInfo> alarmInfoList = alarmSearchService
                .getAlarmInfo(StringUtils.join(needQueryAlarmMonitorIds, ","), alarmTypeStr,
                    DateUtil.getDateToString(DateUtil.todayFirstDate(), null),
                    DateUtil.getDateToString(DateUtil.todayLastDate(), null), null, 0, null, 50, 0, AlarmInfo.class,
                    null);
            return new JsonResultBean(assembleAlarmInfo(alarmInfoList));
        }
        // 如果其余条件都不为空，组装50条报警返回；
        // 有最新的就返回最新的报警，如果最新的报警不足50条，就查询历史报警补足50条返回；
        // 如果数据不足，有多少返回多少；
        if (StringUtils.isNotBlank(latestTime) && StringUtils.isNotBlank(latestAlarmDataStr) && StringUtils
            .isNotBlank(oldestTime) && StringUtils.isNotBlank(oldestAlarmDataStr)) {
            // 差的报警条数
            int differNum = 50;
            // 1.先查询当天报警开始时间大于等于列表中最新的时间的报警记录；如果满足50条返回；
            List<String> listExistLatestMonitorIdAndAlarmTypeList =
                Arrays.stream(latestAlarmDataStr.split(",")).collect(Collectors.toList());
            List<AlarmInfo> latestAlarmInfoList = alarmSearchService
                .getAlarmInfo(StringUtils.join(needQueryAlarmMonitorIds, ","), alarmTypeStr, latestTime,
                    DateUtil.getDateToString(DateUtil.todayLastDate(), null), null, 0, null,
                    differNum + listExistLatestMonitorIdAndAlarmTypeList.size(), 0, AlarmInfo.class, 1);
            if (assembleNeedReturnAlarm(resultAlarmList, differNum, listExistLatestMonitorIdAndAlarmTypeList,
                latestAlarmInfoList)) {
                return new JsonResultBean(assembleAlarmInfo(resultAlarmList));
            }
            // 2.如果不满足50条报警记录，继续查询当天报警开始时间小于等于列表中最老的时间的报警记录；不管是否满足50条都返回；
            differNum = 50 - resultAlarmList.size();
            List<String> listExistOldestMonitorIdAndAlarmTypeList =
                Arrays.stream(oldestAlarmDataStr.split(",")).collect(Collectors.toList());
            List<AlarmInfo> oldestAlarmInfoList = alarmSearchService
                .getAlarmInfo(StringUtils.join(needQueryAlarmMonitorIds, ","), alarmTypeStr,
                    DateUtil.getDateToString(DateUtil.todayFirstDate(), null), oldestTime, null, 0, null,
                    differNum + listExistOldestMonitorIdAndAlarmTypeList.size(), 0, AlarmInfo.class, null);
            assembleNeedReturnAlarm(resultAlarmList, differNum, listExistOldestMonitorIdAndAlarmTypeList,
                oldestAlarmInfoList);
            return new JsonResultBean(assembleAlarmInfo(resultAlarmList));
        }
        return new JsonResultBean(JsonResultBean.FAULT, "参数错误！");
    }

    /**
     * 查询当天报警次数
     */
    @Override
    public JsonResultBean getTodayAlarmQuantity() throws Exception {
        AlarmQuantity alarmQuantity = new AlarmQuantity();
        List<String> needQueryAlarmMonitorIds = getNeedQueryAlarmMonitorIds(null, null, null);
        if (CollectionUtils.isEmpty(needQueryAlarmMonitorIds)) {
            return new JsonResultBean(alarmQuantity);
        }
        Map<String, String> queryParam = new HashMap<>(6);
        queryParam.put("monitorIds", StringUtils.join(needQueryAlarmMonitorIds, ","));
        queryParam.put("alarmTypes", StringUtils.join(AlarmQuantity.getNeedCountQuantityAlarm(), ","));
        queryParam.put("startTime", DateUtil.getDateToString(DateUtil.todayFirstDate(), DateUtil.DATE_FORMAT));
        queryParam.put("endTime", DateUtil.getDateToString(DateUtil.todayLastDate(), DateUtil.DATE_FORMAT));
        String queryResult = HttpClientUtil.send(PaasCloudAlarmUrlEnum.QUERY_ALARM_NUMBER_COUNT, queryParam);
        JSONObject queryResultJsonObj = JSON.parseObject(queryResult);
        if (queryResultJsonObj == null || !Objects
            .equals(queryResultJsonObj.getInteger(PassCloudAlarmUrlUtil.RETURN_RESULT_CODE_KEY),
                PassCloudAlarmUrlUtil.SUCCESS_CODE)) {
            // 监控对象id太多取消打印
            queryParam.remove("monitorIds");
            throw new Exception(
                "调用PassCloud接口查询报警数量统计异常：" + (queryResultJsonObj != null ? queryResultJsonObj.getString("message") :
                    null) + "；参数：" + JSONObject.toJSONString(queryParam));
        }
        List<MonitorAlarmCount> monitorAlarmCountList =
            JSONObject.parseArray(queryResultJsonObj.getString("data"), MonitorAlarmCount.class);
        if (CollectionUtils.isEmpty(monitorAlarmCountList)) {
            return new JsonResultBean(alarmQuantity);
        }
        for (MonitorAlarmCount monitorAlarmCount : monitorAlarmCountList) {
            for (AlarmNumberCount alarmNumberCount : monitorAlarmCount.getNumInfo()) {
                Integer alarmType = alarmNumberCount.getAlarmType();
                Integer num = alarmNumberCount.getTotalNum();
                num = Optional.ofNullable(num).orElse(0);
                Integer handleNum = alarmNumberCount.getProcessedNum();
                handleNum = Optional.ofNullable(handleNum).orElse(0);
                Integer alarmNum = num - handleNum;
                if (alarmNum <= 0 || alarmType == null) {
                    continue;
                }
                String alarmTypeStr = String.valueOf(alarmType);
                if (AlarmQuantity.SPEED_ALARM.contains(alarmTypeStr)) {
                    alarmQuantity.setSpeedAlarmNum(alarmQuantity.getSpeedAlarmNum() + alarmNum);
                    continue;
                }
                if (AlarmQuantity.FATIGUE_DRIVING_ALARM.contains(alarmTypeStr)) {
                    alarmQuantity.setFatigueDrivingAlarmNum(alarmQuantity.getFatigueDrivingAlarmNum() + alarmNum);
                    continue;
                }
                if (AlarmQuantity.DEVIATE_ALARM.contains(alarmTypeStr)) {
                    alarmQuantity.setDeviateAlarmNum(alarmQuantity.getDeviateAlarmNum() + alarmNum);
                    continue;
                }
                if (AlarmQuantity.UNUSUAL_ACTION_ALARM.contains(alarmTypeStr)) {
                    alarmQuantity.setUnusualActionAlarmNum(alarmQuantity.getUnusualActionAlarmNum() + alarmNum);
                    continue;
                }
                if (AlarmQuantity.REGIONAL_ALARM.contains(alarmTypeStr)) {
                    alarmQuantity.setRegionalAlarmNum(alarmQuantity.getRegionalAlarmNum() + alarmNum);
                    continue;
                }
                if (AlarmQuantity.EMERGENCY_ALARM.contains(alarmTypeStr)) {
                    alarmQuantity.setEmergencyAlarmNum(alarmQuantity.getEmergencyAlarmNum() + alarmNum);
                }
            }
        }
        return new JsonResultBean(alarmQuantity);
    }

    /**
     * @param latestAlarmDataStr 列表中报警时间为最新时间的报警数据
     * @param oldestAlarmDataStr 列表中报警时间为最老时间的报警数据
     * @param mark               页签标识
     */
    private List<String> getNeedQueryAlarmMonitorIds(String latestAlarmDataStr, String oldestAlarmDataStr, String mark)
        throws Exception {
        // 808报警查询
        if (MARK_808_ALARM.equals(mark)) {
            // 如果两个参数都为空就是点击808报警查询 需要按照排序规则更新查询的2000个监控对象
            if (StringUtils.isBlank(latestAlarmDataStr) && StringUtils.isBlank(oldestAlarmDataStr)) {
                return initNeedQueryAlarmMonitorIds();
            }
        }
        // 其余报警页签、分页查询和获取报警数量直接取内存中监控对象
        return WebSubscribeManager.getInstance().getLkywUserNeedQueryMonitorIds(SystemHelper.getCurrentUsername());
    }

    /**
     * 查询当前用户下最多2000车的状态，按照报警，在线，离线状态对车id进行排序
     */
    @Override
    public List<String> initNeedQueryAlarmMonitorIds() throws Exception {
        Set<String> userOwnMonitorIdSet = userService.getCurrentUserMonitorIds();
        if (CollectionUtils.isEmpty(userOwnMonitorIdSet)) {
            return new ArrayList<>();
        }
        Map<String, MonitorAccStatus> statusMap = monitorService.getAccAndStatus(userOwnMonitorIdSet, false);
        Set<VehicleStatus> vehicleStatusSet = new HashSet<>();
        VehicleStatus monitorStatus;
        for (String monitorId : userOwnMonitorIdSet) {
            MonitorAccStatus monitorAccStatus = statusMap.get(monitorId);
            Integer status;
            if (Objects.isNull(monitorAccStatus)) {
                status = VehicleStatus.OFFLINE;
            } else {
                status = monitorAccStatus.getStatus();
            }
            monitorStatus = new VehicleStatus(monitorId, status);
            vehicleStatusSet.add(monitorStatus);
        }
        /* 按照监控对象的状态进行排序
           2:未定位; 3:离线; 4:上线; 5:报警; 7:休眠; 8:深度休眠; 9:超速报警; 10:在线行驶; 11:心跳;
           排序规则 5 > 9 > 10 > 4 >7 > 8 > 11 > 2 > 3 */
        List<Integer> sortRuleList = new ArrayList<>();
        sortRuleList.add(VehicleStatus.ALARM);
        sortRuleList.add(VehicleStatus.OVER_SPEED);
        sortRuleList.add(VehicleStatus.ONLINE_RUN);
        sortRuleList.add(VehicleStatus.ONLINE);
        sortRuleList.add(VehicleStatus.SLEEP);
        sortRuleList.add(VehicleStatus.DEEP_SLEEP);
        sortRuleList.add(VehicleStatus.HEART_BEAT);
        sortRuleList.add(VehicleStatus.NOT_LOCATE);
        sortRuleList.add(VehicleStatus.OFFLINE);
        List<String> needQueryAlarmMonitorIds =
            vehicleStatusSet.stream().filter(vehicleStatus -> sortRuleList.contains(vehicleStatus.getVehicleStatus()))
                .sorted(Comparator.comparing(VehicleStatus::getVehicleStatus, (a, b) -> {
                    if (a.equals(b)) {
                        return 0;
                    }
                    for (Integer vehicleStatus : sortRuleList) {
                        if (vehicleStatus.equals(a) || vehicleStatus.equals(b)) {
                            if (vehicleStatus.equals(a)) {
                                return -1;
                            }
                            return 1;
                        }
                    }
                    return 0;
                }).thenComparing(VehicleStatus::getVehicleId)).limit(2000).map(VehicleStatus::getVehicleId)
                .collect(Collectors.toList());
        WebSubscribeManager.getInstance()
            .saveLkywUserNeedQueryMonitorIds(SystemHelper.getCurrentUsername(), needQueryAlarmMonitorIds);
        return needQueryAlarmMonitorIds;
    }

    /**
     * 组装需要返回的报警
     */
    private boolean assembleNeedReturnAlarm(List<AlarmInfo> resultAlarmList, int differNum,
        List<String> listExistMonitorIdAndAlarmTypeList, List<AlarmInfo> alarmInfoList) {
        if (CollectionUtils.isEmpty(alarmInfoList)) {
            return false;
        }
        for (AlarmInfo alarmInfo : alarmInfoList) {
            String monitorIdAndAlarmType =
                alarmInfo.getMonitorId() + "|" + alarmInfo.getAlarmType() + "|" + alarmInfo.getAlarmStartTime();
            if (listExistMonitorIdAndAlarmTypeList.contains(monitorIdAndAlarmType)) {
                continue;
            }
            resultAlarmList.add(alarmInfo);
            differNum--;
            if (differNum > 0) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * 组装报警数据
     */
    private List<AlarmInfo> assembleAlarmInfo(List<AlarmInfo> resultAlarmList) {
        if (CollectionUtils.isEmpty(resultAlarmList)) {
            return new ArrayList<>();
        }
        Set<String> alarmTypeSet = new HashSet<>();
        Set<String> existMonitorIdList = new HashSet<>();
        for (AlarmInfo alarmInfo : resultAlarmList) {
            Integer alarmType = alarmInfo.getAlarmType();
            if (alarmType != null) {
                alarmTypeSet.add(String.valueOf(alarmType));
            }
            existMonitorIdList.add(alarmInfo.getMonitorId());
        }
        List<RedisKey> alarmTypeRedisKeys = HistoryRedisKeyEnum.ALARM_TYPE_INFO.ofs(alarmTypeSet);
        Map<String, String> alarmTypeAndNameMap = RedisHelper.batchGetString(alarmTypeRedisKeys)
            .stream()
            .filter(StringUtils::isNotBlank)
            .map(obj -> JSON.parseObject(obj, AlarmType.class))
            .collect(Collectors.toMap(AlarmType::getPos, AlarmType::getName));
        Map<String, BindDTO> configMap = MonitorUtils.getBindDTOMap(existMonitorIdList);
        List<String> professionalIdsList =
            configMap.values().stream().map(BindDTO::getProfessionalIds).filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Set<String> professionalIdList = new HashSet<>();
        for (String professionalIds : professionalIdsList) {
            professionalIdList.addAll(
                Arrays.stream(professionalIds.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        }
        List<RedisKey> redisKeys = RedisKeyEnum.PROFESSIONAL_INFO.ofs(professionalIdList);
        String[] professionalFields = { "name", "drivingLicenseNo" };
        Map<String, Map<String, String>> professionalInfoMap =
            RedisHelper.batchGetHashMap(redisKeys, "id", professionalFields);
        for (AlarmInfo alarmInfo : resultAlarmList) {
            BindDTO config = configMap.get(alarmInfo.getMonitorId());
            if (config != null) {
                String monitorType = config.getMonitorType();
                alarmInfo.setDeviceType(Integer.valueOf(config.getDeviceType()));
                alarmInfo.setMonitorType(Integer.valueOf(monitorType));
                Integer plateColor = config.getPlateColor();
                if (Objects.equals(monitorType, "0") && plateColor != null) {
                    String plateColorStr = String.valueOf(plateColor);
                    alarmInfo.setPlateColor(plateColorStr);
                    alarmInfo.setPlateColorString(VehicleUtil.getPlateColorStr(plateColorStr));
                }
                alarmInfo.setName(config.getOrgName());
                alarmInfo.setAssignmentName(config.getGroupName());
                alarmInfo.setSimCardNumber(config.getSimCardNumber());
                alarmInfo.setDeviceNumber(config.getDeviceNumber());
                // 组装驾驶员信息
                assembleDriverInfo(professionalInfoMap, alarmInfo, config.getProfessionalIds());
            }
            alarmInfo.setRoadTypeStr(VehicleUtil.getRoadTypeStr(alarmInfo.getRoadType()));
            String alarmStartLocation = alarmInfo.getAlarmStartLocation();
            if (StringUtils.isNotBlank(alarmStartLocation)) {
                String[] alarmStartLocationArr = alarmStartLocation.split(",");
                alarmInfo.setAlarmStartLongitude(alarmStartLocationArr[0]);
                alarmInfo.setAlarmStartLatitude(alarmStartLocationArr[1]);
            }
            alarmInfo.setStartTime(DateUtil.getLongToDateStr(alarmInfo.getAlarmStartTime(), null));
            alarmInfo.setEndTime(DateUtil.getLongToDateStr(alarmInfo.getAlarmEndTime(), null));
            Integer alarmType = alarmInfo.getAlarmType();
            String description = alarmInfo.getDescription();
            if (StringUtils.isBlank(description) && alarmType != null) {
                alarmInfo.setDescription(alarmTypeAndNameMap.get(String.valueOf(alarmType)));
            }
        }
        // 当天最早时间
        long todayFirstDateTimeLong = DateUtil.todayFirstDate().getTime();
        // 过滤当天之前的报警并按先后顺序排序
        return resultAlarmList.stream().filter(alarmInfo -> alarmInfo.getAlarmStartTime() >= todayFirstDateTimeLong)
            .sorted(Comparator.comparingLong(AlarmInfo::getAlarmStartTime)).collect(Collectors.toList());
    }

    /**
     * 组装驾驶员信息
     */
    private void assembleDriverInfo(Map<String, Map<String, String>> professionalInfoMap, AlarmInfo alarmInfo,
        String professionalIds) {
        if (StringUtils.isNotBlank(professionalIds)) {
            List<String> drivingLicenseNoList = new ArrayList<>();
            List<String> employeeNameList = new ArrayList<>();
            Arrays.stream(professionalIds.split(",")).filter(StringUtils::isNotBlank).forEach(professionalId -> {
                Map<String, String> professionalInfo = professionalInfoMap.get(professionalId);
                if (professionalInfo != null) {
                    drivingLicenseNoList.add(professionalInfo.get("drivingLicenseNo"));
                    employeeNameList.add(professionalInfo.get("name"));
                }
            });
            alarmInfo.setDrivingLicenseNo(StringUtils.join(drivingLicenseNoList, ","));
            alarmInfo.setEmployeeName(StringUtils.join(employeeNameList, ","));
        }
    }
}
