package com.zw.platform.push.factory;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.VehicleDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.domain.functionconfig.FenceInfo;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.vas.alram.AlarmType;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.repository.modules.FenceConfigDao;
import com.zw.platform.util.DateUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.ws.common.WebSocketMessageType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ???????????? @author Tdz
 * @since 2017-09-25 15:05
 **/
@Component
public class AlarmFactory {
    private static final Logger logger = LogManager.getLogger(AlarmFactory.class);

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Autowired
    private FenceConfigDao fenceConfigDao;

    @Autowired
    private WebSocketMessageDispatchCenter webSocketMessage;

    @Autowired
    private NewVehicleDao newVehicleDao;

    /**
     * ????????????
     */
    public void createAlarm(Message message) {
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
        MonitorInfo monitorInfo = info.getMonitorInfo();
        if (monitorInfo != null) {
            message.getDesc().setType(monitorInfo.getMonitorType() + "");
        }
        String monitorId = message.getDesc().getMonitorId();
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(monitorId, Lists.newArrayList("professionalIds", "userId"));
        String globalAlarmSet = info.getGlobalAlarmSet();
        String pushAlarmSet = info.getPushAlarmSet();
        explainAlarm(info);
        assembleMonitorInfo(monitorInfo, bindDTO);
        String alarmName = info.getAlarmName();
        alarmName = StringUtil.cut(alarmName, ",", "");
        info.setAlarmName(alarmName);
        // ??????info?????????????????????id,????????????????????????
        if (info.getFenceConfigId() != null && !"".equals(info.getFenceConfigId())) {
            softwareFenceAlarm(info);
        }
        boolean isGlobalAlarm = false;
        if (StringUtils.isNotEmpty(globalAlarmSet)) {
            try {
                // ?????????????????????????????????
                this.saveAlarmToRedis(monitorId, Arrays.stream(globalAlarmSet.split(",")).collect(Collectors.toSet()),
                    info.getAlarmStartTimeList());
            } catch (Exception e) {
                logger.error("??????????????????");
            }
            if (pushAlarmSet != null && !"".equals(pushAlarmSet)) {
                globalAlarmSet =
                    globalAlarmSet + (!globalAlarmSet.endsWith(",") && !pushAlarmSet.startsWith(",") ? "," : "")
                        + pushAlarmSet;
            }
            globalAlarmSet = StringUtil.cut(globalAlarmSet, ",", "");
            info.setGlobalAlarmSet(globalAlarmSet);
            setEarlyAlarmStartTime(info);
            t808Message.setMsgBody(info);
            message.setData(t808Message);
            sendAlarm(message, 2);
            isGlobalAlarm = true;
        }
        if (pushAlarmSet != null && !"".equals(pushAlarmSet) && !isGlobalAlarm) {
            pushAlarmSet = StringUtil.cut(pushAlarmSet, ",", "");
            info.setPushAlarmSet(pushAlarmSet);
            info.setGlobalAlarmSet(pushAlarmSet);
            setEarlyAlarmStartTime(info);
            t808Message.setMsgBody(info);
            message.setData(t808Message);
            sendAlarm(message, 1);
        }
        // ??????????????????????????????????????????, ??????????????????
        message.setData(t808Message);
        boolean isIntercomObject = false;
        if (bindDTO != null && bindDTO.getUserId() != null) {
            isIntercomObject = true;
        }
        String dispatchAlarmSet = isGlobalAlarm ? globalAlarmSet : pushAlarmSet;
        if (isIntercomObject && StringUtils.isNotEmpty(dispatchAlarmSet)) {
            List<String> pushAlarms = Arrays.asList(dispatchAlarmSet.split(","));
            List<String> alarmTypes = new ArrayList<>();
            List<String> alarmNames = new ArrayList<>();
            if (pushAlarms.contains("0")) {
                alarmTypes.add("0");
                alarmNames.add("SOS??????");
            }
            if (pushAlarms.contains("152")) {
                alarmTypes.add("152");
                alarmNames.add("???????????????");
            }
            if (pushAlarms.contains("153")) {
                alarmTypes.add("153");
                alarmNames.add("????????????");
            }
            if (pushAlarms.contains("154")) {
                alarmTypes.add("154");
                alarmNames.add("???????????????");
            }
            if (pushAlarms.contains("155")) {
                alarmTypes.add("155");
                alarmNames.add("???????????????");
            }
            if (pushAlarms.contains("156")) {
                alarmTypes.add("156");
                alarmNames.add("????????????");
            }
            if (!alarmNames.isEmpty()) {
                info.setAlarmName(StringUtils.join(alarmNames, ","));
                info.setGlobalAlarmSet(StringUtils.join(alarmTypes, ","));
                t808Message.setMsgBody(info);
                message.setData(t808Message);
                if (isGlobalAlarm) {
                    sendAlarm(message, 4);
                    return;
                }
                sendAlarm(message, 3);
            }
        }
    }

    /**
     * ?????????????????????????????????
     * @param vid               ??????id
     * @param globalAlarmSet    ??????????????????
     * @param typeAndStartTimes type_timestamp
     */
    public void saveAlarmToRedis(String vid, Set<String> globalAlarmSet, List<String> typeAndStartTimes) {
        Set<String> globalAlarmTypeAndStartTimes = new HashSet<>();
        if (CollectionUtils.isNotEmpty(typeAndStartTimes)) {
            final long todayBegin = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.of("+8")) * 1000L;
            globalAlarmTypeAndStartTimes = typeAndStartTimes.stream().filter(o -> {
                if (StringUtils.isEmpty(o)) {
                    return false;
                }
                final String[] arr = o.split("_");
                if (arr.length <= 1) {
                    return false;
                }
                // ?????????????????????
                if (!globalAlarmSet.contains(arr[0])) {
                    return false;
                }
                return Long.parseLong(arr[1]) > todayBegin;
            }).collect(Collectors.toSet());
        }
        if (CollectionUtils.isEmpty(globalAlarmTypeAndStartTimes)) {
            return;
        }
        Set<String> finalGlobalAlarmTypeAndStartTimes = globalAlarmTypeAndStartTimes;
        boolean anyValid = jedisOpsExp(HistoryRedisKeyEnum.UNHANDLED_ALARM.of(vid), secBeforeTomorrow(),
            key -> finalGlobalAlarmTypeAndStartTimes.stream()
                // ??????????????????????????????
                .filter(typeAndStartTime -> !RedisHelper
                    .isContainsKey(HistoryRedisKeyEnum.MONITOR_RECENTLY_HANDLED_ALARM.of(vid, typeAndStartTime)))
                .peek(typeAndStartTime -> RedisHelper.addToSet(key, typeAndStartTime)).count() > 0);

        if (anyValid) {
            final boolean initiating = !RedisHelper.isContainsKey(HistoryRedisKeyEnum.UNHANDLED_VEHICLE.of());
            RedisHelper.addToSet(HistoryRedisKeyEnum.UNHANDLED_VEHICLE.of(), vid);
            if (initiating) {
                RedisHelper.expireKey(HistoryRedisKeyEnum.UNHANDLED_VEHICLE.of(), secBeforeTomorrow());
            }
        }
    }

    /**
     * ??????jedis???????????????key????????????
     *
     * @param key    key
     * @param expire ?????????????????????s
     * @param ops    ??????jedis??????
     * @return ???????????????
     */
    private static <T> T jedisOpsExp(RedisKey key, int expire, Function<RedisKey, T> ops) {
        final boolean initiating = !RedisHelper.isContainsKey(key);
        final T result = ops.apply(key);
        if (initiating) {
            RedisHelper.expireKey(key, expire);
        }
        return result;
    }

    /**
     * ???????????????????????????????????????
     *
     * @return ???
     */
    private static int secBeforeTomorrow() {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime tomorrow = now.toLocalDate().plusDays(1).atStartOfDay();
        final int randomExpire = RANDOM.nextInt(60);
        return (int) now.until(tomorrow, ChronoUnit.SECONDS) + randomExpire;
    }

    private void setEarlyAlarmStartTime(LocationInfo info) {
        Long earlyAlarmStartTime = info.getEarlyAlarmStartTime();
        if (Objects.nonNull(earlyAlarmStartTime)) {
            if (info.getEarlyAlarmStartTime() == 0L) {
                info.setEarlyAlarmStartTimeStr("");
            } else {
                info.setEarlyAlarmStartTimeStr(LocalDateUtils.dateTimeFormat(new Date(earlyAlarmStartTime * 1000)));
            }
        }
    }

    /**
     * ????????????
     */
    public void dealAlarm(HandleAlarms handleAlarms) {
        final String vid = handleAlarms.getVehicleId();
        final LocalDateTime startTime = DateUtil.YMD_HMS.ofDateTime(handleAlarms.getStartTime())
            .orElseThrow(() -> new RuntimeException("????????????????????????"));
        final String startTimeLong = String.valueOf(DateUtil.toTimestamp(startTime));
        final String typeAndStartTime = handleAlarms.getAlarm() + "_" + startTimeLong;
        // ????????????
        LocalDateTime nowDateTime = LocalDateTime.now();
        // ?????? 23:58:59
        LocalDateTime nowMaxDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).plusMinutes(-1);
        // ??????????????? ??????23:58:59 ???????????????
        long durationSeconds = Duration.between(nowDateTime, nowMaxDateTime).toMillis() / 1000;
        if (durationSeconds > 0) {
            RedisHelper.setStringEx(HistoryRedisKeyEnum.MONITOR_RECENTLY_HANDLED_ALARM.of(vid, typeAndStartTime), "",
                (int) durationSeconds);
        }
        RedisHelper.delSetItem(HistoryRedisKeyEnum.UNHANDLED_ALARM.of(vid), typeAndStartTime);
        if (StringUtils.isBlank(RedisHelper.randomGetSetMember(HistoryRedisKeyEnum.UNHANDLED_ALARM.of(vid)))) {
            RedisHelper.delSetItem(HistoryRedisKeyEnum.UNHANDLED_VEHICLE.of(), vid);
        }
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // webSocketMessage.sendGlobalAlarmHandleNotice(vid);
    }

    /**
     * ?????????????????????
     */
    private void softwareFenceAlarm(LocationInfo info) {
        info.setFenceType("");
        String fenceConfigId = info.getFenceConfigId();
        if (StringUtils.isEmpty(fenceConfigId)) {
            return;
        }
        String[] fenceConfigIds = fenceConfigId.split(",");
        List<FenceInfo> fenceInfoList = fenceConfigDao.getFenceInfoByFenceConfigIds(Arrays.asList(fenceConfigIds));
        if (CollectionUtils.isEmpty(fenceInfoList)) {
            return;
        }
        StringBuilder fenceNameStr = new StringBuilder();
        StringBuilder fenceTypeStr = new StringBuilder();
        for (FenceInfo fenceInfo : fenceInfoList) {
            String fenceName = fenceInfo.getFenceName();
            String fenceType = fenceInfo.getType();
            if (StringUtils.isNotEmpty(fenceName) && StringUtils.isNotEmpty(fenceType)) {
                fenceNameStr.append(fenceName).append(",");
                fenceTypeStr.append(buildFenceTypeChinaName(fenceType)).append(",");
            }
        }
        info.setFenceName(StringUtil.cut(fenceNameStr.toString(), "", ","));
        info.setFenceType(StringUtil.cut(fenceTypeStr.toString(), "", ","));
    }

    /**
     * ???????????????????????????
     */
    private String buildFenceTypeChinaName(String fenceType) {
        switch (fenceType) {
            case "zw_m_polygon":
                fenceType = "?????????";
                break;
            case "zw_m_rectangle":
                fenceType = "??????";
                break;
            case "zw_m_line":
                fenceType = "??????";
                break;
            case "zw_m_circle":
                fenceType = "??????";
                break;
            case "zw_m_administration":
                fenceType = "????????????";
                break;
            default:
                fenceType = "";
                break;
        }
        return fenceType;
    }

    /**
     * ??????????????????
     */
    public void explainAlarm(LocationInfo info) {
        String alarmName = info.getAlarmName();
        // ??????????????????????????????
        List<String> alarmNames = new ArrayList<>();
        if (info.getGlobalAlarmSet() != null && !"".equals(info.getGlobalAlarmSet())) {
            // ???????????????????????????????????????????????????
            String[] alarmNumber = info.getGlobalAlarmSet().split(",");
            getAlarmNames(alarmNames, alarmNumber);
        }
        if (info.getPushAlarmSet() != null && !"".equals(info.getPushAlarmSet())) {
            // ???????????????????????????????????????????????????
            String[] alarmNumber = info.getPushAlarmSet().split(",");
            getAlarmNames(alarmNames, alarmNumber);
        }
        if (CollectionUtils.isNotEmpty(alarmNames)) {
            // ???????????????????????????
            String alarmStr = StringUtils.join(alarmNames, ",");
            if (!StringUtil.isNullOrBlank(alarmName)) {
                alarmStr = alarmStr + alarmName;
            }
            info.setAlarmName(alarmStr);
        }
    }

    private void getAlarmNames(List<String> alarmNames, String[] alarmNumbers) {
        for (String alarmNumber : alarmNumbers) {
            if (!StringUtil.isNullOrBlank(alarmNumber) && AlarmTypeUtil.IO_ALARM
                .contains(Integer.parseInt(alarmNumber))) {
                continue;
            }
            RedisKey redisKey = HistoryRedisKeyEnum.ALARM_TYPE_INFO.of(alarmNumber);
            // ????????????????????????????????????
            String redisMessage = RedisHelper.getString(redisKey);
            AlarmType alarmType = JSON.parseObject(redisMessage, AlarmType.class);
            if (alarmType != null) {
                String alarmName = alarmType.getName();
                alarmNames.add(alarmName);
            }
        }
    }

    /**
     * ????????????
     */
    public void sendAlarm(Message message, Integer type) {
        // ??????????????????????????????
        if (!filterDispatchAlarmAndJudgeIsNeedPushAlarm(message, type)) {
            return;
        }
        switch (type) {
            case 1:
                webSocketMessage.pushMessageToAllClient(message.getDesc().getMonitorId(), message,
                    WebSocketMessageType.VEHICLE_ALARM);
                break;
            case 2:
                webSocketMessage.pushMessageToAllClient(message.getDesc().getMonitorId(), message,
                    WebSocketMessageType.VEHICLE_ALARM_GLOBAL);
                webSocketMessage.pushMessageToAllClient(message.getDesc().getMonitorId(), message,
                    WebSocketMessageType.VEHICLE_ALARM);
                break;
            case 3:
                webSocketMessage.pushMessageToAllClient(message.getDesc().getMonitorId(), message,
                    WebSocketMessageType.VEHICLE_SOSALARM);
                break;
            case 4:
                webSocketMessage.pushMessageToAllClient(message.getDesc().getMonitorId(), message,
                    WebSocketMessageType.VEHICLE_ALARM_GLOBAL);
                webSocketMessage.pushMessageToAllClient(message.getDesc().getMonitorId(), message,
                    WebSocketMessageType.VEHICLE_SOSALARM);
                break;
            default:
                break;
        }
    }

    /**
     * ???????????????????????????????????????
     * @return ?????????????????????????????????????????????????????????????????????
     */
    private boolean filterDispatchAlarmAndJudgeIsNeedPushAlarm(Message message, Integer type) {
        if (Objects.equals(1, type) || Objects.equals(2, type)) {
            T808Message t808Message = JSON.parseObject(JSON.toJSONString(message.getData()), T808Message.class);
            LocationInfo locationInfo =
                JSON.parseObject(JSON.toJSONString(t808Message.getMsgBody()), LocationInfo.class);
            String alarmNameStr = locationInfo.getAlarmName();
            locationInfo.setAlarmName(null);
            String[] alarmNameArr = Arrays.stream(alarmNameStr.split(",")).filter(
                alarmName -> StringUtils.isNotBlank(alarmName) && !AlarmTypeUtil.DISPATCH_ALARM_NAME_LIST
                    .contains(alarmName)).toArray(String[]::new);
            if (alarmNameArr.length > 0) {
                locationInfo.setAlarmName(StringUtils.join(alarmNameArr, ","));
            }
            String globalAlarmSet = locationInfo.getGlobalAlarmSet();
            locationInfo.setGlobalAlarmSet(null);
            String[] alarmTypeArr = Arrays.stream(globalAlarmSet.split(",")).filter(
                alarmType -> StringUtils.isNotBlank(alarmType) && !AlarmTypeUtil.DISPATCH_ALARM_LIST
                    .contains(Integer.parseInt(alarmType))).toArray(String[]::new);
            if (alarmTypeArr.length > 0) {
                locationInfo.setGlobalAlarmSet(StringUtils.join(alarmTypeArr, ","));
            }
            t808Message.setMsgBody(locationInfo);
            message.setData(t808Message);
            // ??????????????????????????????,??????????????????,?????????????????????;
            return alarmNameArr.length > 0 || alarmTypeArr.length > 0;
        }
        return true;
    }

    private void assembleMonitorInfo(MonitorInfo monitorInfo, BindDTO bindDTO) {
        if (monitorInfo == null
            || !Objects.equals(monitorInfo.getMonitorType(), Integer.valueOf(MonitorTypeEnum.VEHICLE.getType()))) {
            return;
        }
        VehicleDO vehicleDO = newVehicleDao.getById(monitorInfo.getMonitorId());
        monitorInfo.setVehicleOwner(vehicleDO.getVehicleOwner());
        monitorInfo.setVehicleOwnerPhone(vehicleDO.getVehicleOwnerPhone());
        if (bindDTO == null) {
            return;
        }
        String professionalIds = bindDTO.getProfessionalIds();
        if (StringUtils.isBlank(professionalIds)) {
            return;
        }
        String[] ids = professionalIds.split(",");
        RedisKey redisKey = RedisKeyEnum.PROFESSIONAL_INFO.of(ids[0]);
        Map<String, String> professionalMap =
            RedisHelper.getHashMap(redisKey, Lists.newArrayList("id", "name", "phone", "phoneTwo", "phoneThree"));
        if (!MapUtils.isEmpty(professionalMap)) {
            monitorInfo.setAlarmProfessionalsName(professionalMap.get("name"));
            monitorInfo.setPhone(professionalMap.get("phone"));
            monitorInfo.setPhoneTwo(professionalMap.get("phoneTwo"));
            monitorInfo.setPhoneThree(professionalMap.get("phoneThree"));
        }
    }

}
