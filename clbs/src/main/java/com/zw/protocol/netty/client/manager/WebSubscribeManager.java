package com.zw.protocol.netty.client.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.ConvertUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.ConcurrentHashSet;
import com.zw.protocol.msg.Message;
import com.zw.protocol.netty.client.server.DefaultQueuedChannel;
import com.zw.protocol.netty.client.server.QueuedChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by LiaoYuecai on 2017/6/26.
 */
@Log4j2
public enum WebSubscribeManager {
    //
    INSTANCE;

    private static final Set<Integer> IGNORE_BODY_MSG_ID =
        ImmutableSet.of(ConstantUtil.T808_DEVICE_UPLOAD, ConstantUtil.T808_MULTIMEDIA_UPLOAD);

    private final Map<String, QueuedChannel> channelMap;

    private final Map<String, QueuedChannel> videoChannelMap;

    private final Map<String, Subscribe> subscribeMap;

    private int threadCount = 0;

    /**
     * 车辆+通道号的流量开始时间记录
     */
    private final Map<String, String> flowStartTime;

    /**
     * 终端唤醒应答订阅对应关系
     * monitorId,msgNo -> sessionId
     */
    private final Map<String, String> subDeviceWakeUpAckMap;

    /**
     * 用户名对应的客户端id
     * APP2.0.0新增
     */
    private final Map<String, String> userNameAndClientId;

    /**
     * 用户名对应的websocket sessionId
     * APP2.0.0新增
     */
    private final Map<String, List<String>> userNameAndSessionIdList;

    /**
     * 调度用户登录信息 key:登录用户名 value登录返回信息
     */
    private final Map<String, JSONObject> dispatchUserLoginInfoMap;

    private final Set<String> subscribeAckSet = new ConcurrentHashSet<>();

    /**
     * key:username  value:监控对象id
     * 两客一危报警数据
     * 查询当前用户下最多2000车的状态，按照报警，在线，离线状态对车id进行排序
     */
    private final Map<String, List<String>> lkywUserQyeryAlarmMonitorIdsMap;

    WebSubscribeManager() {
        channelMap = new ConcurrentHashMap<>();
        subscribeMap = new ConcurrentHashMap<>();
        videoChannelMap = new ConcurrentHashMap<>();
        flowStartTime = new ConcurrentHashMap<>();
        userNameAndClientId = new ConcurrentHashMap<>();
        userNameAndSessionIdList = new ConcurrentHashMap<>();
        dispatchUserLoginInfoMap = new ConcurrentHashMap<>();
        lkywUserQyeryAlarmMonitorIdsMap = new ConcurrentHashMap<>();
        subDeviceWakeUpAckMap = new ConcurrentHashMap<>();
    }

    public static WebSubscribeManager getInstance() {
        return WebSubscribeManager.INSTANCE;
    }

    /**
     * 开启netty队列消费
     * @param taskExecutor  线程池
     * @param consumerCount 并发消费者数量
     */
    public void startHandleQueue(ThreadPoolTaskExecutor taskExecutor, int consumerCount) {
        final ImmutableList<Map<String, QueuedChannel>> channelMaps = ImmutableList.of(channelMap, videoChannelMap);

        // 遍历每个队列时的最大深度，达到此值或队列为空时切换到下一条队列
        final int maxPollCount = 30;
        for (; threadCount < consumerCount; threadCount++) {
            taskExecutor.execute(() -> {
                Thread.currentThread().setName("netty-client-queue-handler");
                int waitChannelTime = 1;
                int[] queueSizes;
                ConcurrentLinkedQueue<Object> queue;
                Object msg;
                // 中断后即停止消费
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final int maxWaitIdleTime = 40;
                        final int sleepTime = 10;
                        final int minWaitChannelTime = 10;
                        final int maxWaitChannelTime = 5000;
                        boolean channelBusy = false;
                        // 记录当前线程空闲比例，0~1，这里没有用时间来统计的原因是网络波动影响太大
                        double idle = 1d;
                        if (log.isDebugEnabled()) {
                            queueSizes = channelMaps.stream().flatMap(o -> o.values().stream())
                                .mapToInt(o -> o.getQueue().size()).toArray();
                            if (IntStream.of(queueSizes).anyMatch(o -> o > 0)) {
                                final String queueSizesStr =
                                    IntStream.of(queueSizes).mapToObj(String::valueOf).collect(Collectors.joining(","));
                                log.debug("Netty client发送队列深度：{}", queueSizesStr);
                            }
                        }
                        for (Map<String, QueuedChannel> channelMap : channelMaps) {
                            for (QueuedChannel channel : channelMap.values()) {
                                queue = channel.getQueue();
                                int j;
                                boolean writable = true;
                                for (
                                    j = 0;
                                    j < maxPollCount && (writable = channel.getDelegate().isWritable()) && null != (
                                        msg = queue.poll()); j++) {
                                    channel.sendNetty(msg);
                                }
                                if (!writable) {
                                    channelBusy = true;
                                }
                                idle *= 1d - (double) j / maxPollCount;
                            }
                        }

                        if (channelBusy) {
                            idle = 0d;
                            waitChannelTime = Math.min(2 * waitChannelTime, maxWaitChannelTime);
                            log.info("Netty client缓冲区已满！额外等待{}ms后重试", waitChannelTime - minWaitChannelTime);
                        } else {
                            waitChannelTime = minWaitChannelTime;
                        }
                        // 负载越高，sleep间隔越短，这样就导致队列加速消费，降低整体负载，从而允许一定的弹性
                        int waitIdleTime = (int) (maxWaitIdleTime * idle);
                        if (waitIdleTime < maxWaitIdleTime) {
                            // 空闲：当前消息可以立即被发出
                            log.info("Netty client空闲率: {}%", String.format("%.2f", idle * 100));
                        }
                        try {
                            Thread.sleep(sleepTime + waitIdleTime + waitChannelTime);
                        } catch (InterruptedException e) {
                            // nothing
                            Thread.currentThread().interrupt();
                        }
                    } catch (Exception e) {
                        log.error("netty发送队列消费线程异常：", e);
                    }
                }
                log.error("Netty发送队列消费线程退出！");
            });
        }
    }

    /**
     * 清空状态订阅信息
     */
    public void clearStatusSubscribe() {
        subscribeMap.clear();
    }

    private Set<String> addPrefix(String prefix, Set<String> deviceIds) {
        Set<String> devices = new HashSet<>(deviceIds.size());
        for (String deviceId : deviceIds) {
            devices.add(prefix + deviceId);
        }
        return devices;
    }

    /**
     * 是否上报报警信息
     * @param monitorId   监控对象id
     * @param deviceId    终端id
     * @param list        list
     * @param defaultType 默认推送取值类型 0：不取值，1：取视频报警默认推送数据，2：取普通报警默认推送数据
     */
    public void subAlarmSetting(String monitorId, String deviceId, List<AlarmParameterSettingForm> list,
        int defaultType) {
        if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(deviceId)) {
            return;
        }
        RedisKey deviceAlarmPushRedisKey = HistoryRedisKeyEnum.ALARM_PUSH_SET_DEVICE_ID.of(deviceId);
        String deviceAlarmPushRedisKeyStr = deviceAlarmPushRedisKey.get();
        RedisKey monitorIdAlarmPushRedisKey = HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.of(monitorId);
        String monitorIdAlarmPushRedisKeyStr = monitorIdAlarmPushRedisKey.get();
        JSONObject deviceJsonObj = new JSONObject();
        JSONObject monitorJsonObj = new JSONObject();
        //根据默认推送取值类型取缓存中报警推送默认值
        JSONObject defaultValueJsonObj = this.getDefaultType(defaultType);
        deviceJsonObj.put(deviceAlarmPushRedisKeyStr, defaultValueJsonObj);
        monitorJsonObj.put(monitorIdAlarmPushRedisKeyStr, defaultValueJsonObj);
        JSONArray alarmList;
        for (AlarmParameterSettingForm parameterSettingForm : list) {
            int alarmPush = parameterSettingForm.getAlarmPush();
            String paramCode = parameterSettingForm.getParamCode();
            // 音视频忽略报警: 如果ignore等于1:则忽略报警, 无需维护缓存3_*缓存
            // Integer ignore = parameterSettingForm.getIgnore();
            if (alarmPush == 0 || (paramCode != null && !"param1".equals(paramCode))) {
                continue;
            }
            String alarmPos = parameterSettingForm.getPos();
            alarmList = defaultValueJsonObj.getJSONArray(alarmPos);
            if (alarmList == null) {
                alarmList = new JSONArray();
            }
            if (alarmList.contains(alarmPush)) {
                continue;
            }
            alarmList.add(alarmPush);
            if (alarmPos != null) {
                defaultValueJsonObj.put(alarmPos, alarmList);
                //若为异动报警标识则存入异动山路、客运推送数据
                if ("77".equals(alarmPos)) {
                    defaultValueJsonObj.put("7702", alarmList);
                    defaultValueJsonObj.put("7703", alarmList);
                }
                //若为传感器超速报警则存入里程传感器异常报警推送数据
                if ("67".equals(alarmPos)) {
                    defaultValueJsonObj.put("14411", alarmList);
                }
            }
        }

        RedisHelper.setString(deviceAlarmPushRedisKey, deviceJsonObj.toJSONString());
        RedisHelper.setString(monitorIdAlarmPushRedisKey, monitorJsonObj.toJSONString());

        sendMsgToAll(deviceJsonObj, ConstantUtil.WEB_ALARM_ADD);
    }

    /**
     * 推送报警设置缓存和保存报警设置值(仅用于报警参数设置)
     * 保存报警值(超速报警参数值;异动报警参数值;长时间下线报警参数值)
     * @param vehicleId          车id
     * @param deviceId           终端id
     * @param defaultType        默认的
     * @param needSendAddMsgList 需要下发消息的集合
     * @param needAddKeyValueMap redis需要新增的
     * @param isSaveAlarmValue   是否需要保存报警参数设置值
     */
    public void pushAlarmSetAndSaveAlarmSetValue(String vehicleId, String deviceId,
        List<AlarmParameterSettingForm> list, JSONObject defaultType, List<JSONObject> needSendAddMsgList,
        Map<RedisKey, String> needAddKeyValueMap, boolean isSaveAlarmValue) {
        RedisKey vehicleRedisKey = HistoryRedisKeyEnum.ALARM_PUSH_SET_MONITOR_ID.of(vehicleId);
        RedisKey deviceRedisKey = HistoryRedisKeyEnum.ALARM_PUSH_SET_DEVICE_ID.of(deviceId);
        String vehicleKey = vehicleRedisKey.get();
        String deviceKey = deviceRedisKey.get();
        JSONObject deviceJsonObj = new JSONObject();
        JSONObject vehicleJsonObj = new JSONObject();
        deviceJsonObj.put(deviceKey, defaultType);
        vehicleJsonObj.put(vehicleKey, defaultType);
        //超速报警参数值
        JSONObject overSpeedValue = new JSONObject();
        //异动报警参数值
        JSONObject exceptionMoveValue = new JSONObject();
        //长时间下线报警参数值
        JSONObject longTimeOffline = new JSONObject();
        // ACC信号异常报警参数值
        JSONObject accSignalExceptionValue = new JSONObject();
        // 位置信息异常报警参数值
        JSONObject locationExceptionValue = new JSONObject();
        // 疑似人为屏蔽报警参数值
        JSONObject suspectedBlockingValue = new JSONObject();
        boolean flag = true;
        for (AlarmParameterSettingForm alarmSetting : list) {
            //设置报警参数值
            if (isSaveAlarmValue) {
                setAlarmSettingValue(overSpeedValue, exceptionMoveValue, longTimeOffline, alarmSetting);
                setAccSignalExceptionSettingValue(accSignalExceptionValue, alarmSetting);
                setLocationExceptionSettingValue(locationExceptionValue, alarmSetting);
                setSuspectedBlocking(suspectedBlockingValue, alarmSetting);
            }
            int alarmPush = alarmSetting.getAlarmPush();
            String paramCode = alarmSetting.getParamCode();
            // 音视频忽略报警: 如果ignore等于1:则忽略报警, 无需维护缓存3_*缓存
            //屏蔽字段只与终端有关，参数下发失败报警依旧推送
            if (alarmPush == 0 || (paramCode != null && !"param1".equals(paramCode))) {
                continue;
            }
            String alarmPos = alarmSetting.getPos();
            JSONArray alarmList = defaultType.getJSONArray(alarmPos);
            if (alarmList == null) {
                alarmList = new JSONArray();
            }
            if (alarmList.contains(alarmPush)) {
                continue;
            }
            alarmList.add(alarmPush);
            if (alarmPos != null) {
                defaultType.put(alarmPos, alarmList);
                //若为异动报警标识则存入异动山路、客运推送数据
                if ("77".equals(alarmPos)) {
                    defaultType.put("7702", alarmList);
                    defaultType.put("7703", alarmList);
                }
                //若为传感器超速报警则存入里程传感器异常报警推送数据
                if ("67".equals(alarmPos)) {
                    defaultType.put("14411", alarmList);
                }
                //若为路线偏离报警（平台）则存入不按规定线路运行报警推送数据
                if ("75".equals(alarmPos)) {
                    defaultType.put("147", alarmList);
                }
            }
            flag = false;
        }
        if (isSaveAlarmValue) {
            String overSpeedValueStr = overSpeedValue.toString();
            if (StringUtils.isNotBlank(overSpeedValueStr)) {
                RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "76");
                needAddKeyValueMap.put(redisKey, overSpeedValueStr);
            }
            String exceptionMoveValueStr = exceptionMoveValue.toString();
            if (StringUtils.isNotBlank(exceptionMoveValueStr)) {
                RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "77");
                needAddKeyValueMap.put(redisKey, exceptionMoveValueStr);
            }
            String longTimeOfflineStr = longTimeOffline.toString();
            if (StringUtils.isNotBlank(longTimeOfflineStr)) {
                RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "82");
                needAddKeyValueMap.put(redisKey, longTimeOfflineStr);
            }
            String accSignalExceptionValueStr = accSignalExceptionValue.toString();
            if (StringUtils.isNotBlank(accSignalExceptionValueStr)) {
                RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "150");
                needAddKeyValueMap.put(redisKey, accSignalExceptionValueStr);
            }
            String locationExceptionValueStr = locationExceptionValue.toString();
            if (StringUtils.isNotBlank(locationExceptionValueStr)) {
                RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "151");
                needAddKeyValueMap.put(redisKey, locationExceptionValueStr);
            }
            String suspectedBlockingValueStr = suspectedBlockingValue.toString();
            if (StringUtils.isNotBlank(suspectedBlockingValueStr)) {
                RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "209");
                needAddKeyValueMap.put(redisKey, suspectedBlockingValueStr);
            }
        }
        // 组装调度报警推送方式
        assembleDispatchAlarmPushType(deviceKey, defaultType, flag, vehicleKey, deviceJsonObj, vehicleJsonObj);
        needAddKeyValueMap.put(deviceRedisKey, deviceJsonObj.toJSONString());
        needAddKeyValueMap.put(vehicleRedisKey, vehicleJsonObj.toJSONString());
        needSendAddMsgList.add(deviceJsonObj);
    }

    private void setSuspectedBlocking(JSONObject suspectedBlockingValue, AlarmParameterSettingForm alarmSetting) {
        if (alarmSetting == null) {
            return;
        }
        String alarmPos = alarmSetting.getPos();
        if (!"209".equals(alarmPos)) {
            return;
        }
        final String key;
        switch (alarmSetting.getParamCode()) {
            case "param1":
                key = "intervalTime";
                break;
            case "param2":
                key = "intervalMile";
                break;
            case "param3":
                key = "intervalNum";
                break;
            default:
                key = null;
        }
        if (key != null) {
            suspectedBlockingValue.put(key, alarmSetting.getParameterValue());
        }
    }

    /**
     * 组装调度报警推送方式
     */
    private void assembleDispatchAlarmPushType(String deviceKey, JSONObject defaultType, boolean flag,
        String vehicleKey, JSONObject deviceJsonObj, JSONObject vehicleJsonObj) {
        JSONObject dispatchAlarmPushType = new JSONObject();
        // 调度报警的推送方式为全局
        for (Integer dispatchAlarmType : ConstantUtil.DISPATCH_ALARM_TYPE_LIST) {
            dispatchAlarmPushType.put(String.valueOf(dispatchAlarmType), Collections.singleton(2));
        }
        if (flag) {
            deviceJsonObj.put(deviceKey, dispatchAlarmPushType);
            vehicleJsonObj.put(vehicleKey, dispatchAlarmPushType);
        } else {
            defaultType.putAll(dispatchAlarmPushType);
        }
    }

    /**
     * 根据默认推送取值类型取缓存中报警推送默认值
     */
    public JSONObject getDefaultType(int defaultType) {
        switch (defaultType) {
            case 1:
                String defaultVideoPush = RedisHelper.getString(HistoryRedisKeyEnum.DEFAULT_VIDEO_PUSH.of());
                return JSON.parseObject(defaultVideoPush).getJSONObject("defaultVideo_push");
            case 2:
                String defaultPush = RedisHelper.getString(HistoryRedisKeyEnum.DEFAULT_PUSH.of());
                return JSON.parseObject(defaultPush).getJSONObject("default_push");
            default:
                return new JSONObject();
        }
    }

    private void setAlarmSettingValue(JSONObject overSpeedValue, JSONObject exceptionMoveValue,
        JSONObject longTimeOffline, AlarmParameterSettingForm alarmSetting) {
        String pos = alarmSetting.getPos();
        String parameterValue = alarmSetting.getParameterValue();
        if (StringUtils.isEmpty(pos)) {
            return;
        }
        switch (pos) {
            //超速报警
            case "76":
                setSpeedAlarmParam(overSpeedValue, alarmSetting);
                break;
            //异动报警
            case "77":
                setMoveAlarmParam(exceptionMoveValue, alarmSetting);
                break;
            //超速预警
            case "81":
                overSpeedValue.put("speedDValue", parameterValue);
                break;
            case "82":
                //长时间下线数值若为空则默认为10分钟
                String longTimeValue = parameterValue;
                if (StringUtils.isBlank(longTimeValue)) {
                    longTimeValue = "10";
                }
                longTimeOffline.put("longTimeOfflineValue", longTimeValue);
                break;
            default:
                break;
        }
    }

    private void setMoveAlarmParam(JSONObject exceptionMoveValue, AlarmParameterSettingForm alarmSetting) {
        switch (alarmSetting.getParamCode()) {
            case "param1":
                exceptionMoveValue.put("calStandard", alarmSetting.getParameterValue());
                break;
            case "param2":
                String timeValue = alarmSetting.getParameterValue();
                String[] startAndEndTime = { "", "" };
                if (!StringUtils.isEmpty(timeValue)) {
                    startAndEndTime = timeValue.split("--");
                }
                exceptionMoveValue.put("startTime", startAndEndTime[0].trim());
                exceptionMoveValue.put("endTime", startAndEndTime[1].trim());
                break;
            case "param3":
                String dateValue = alarmSetting.getParameterValue();
                String[] startAndEndDate = { "", "" };
                if (!StringUtils.isEmpty(dateValue)) {
                    startAndEndDate = dateValue.split("--");
                }
                exceptionMoveValue.put("startDate", startAndEndDate[0].trim());
                exceptionMoveValue.put("endDate", startAndEndDate[1].trim());
                break;

            default:
                break;
        }
    }

    private void setSpeedAlarmParam(JSONObject overSpeedValue, AlarmParameterSettingForm alarmSetting) {
        switch (alarmSetting.getParamCode()) {
            case "param1":
                overSpeedValue.put("calStandard", alarmSetting.getParameterValue());
                break;
            case "param2":
                overSpeedValue.put("topSpeed", alarmSetting.getParameterValue());
                break;
            case "param3":
                overSpeedValue.put("nightPercent", alarmSetting.getParameterValue());
                break;
            case "param4":
                String timeValue = alarmSetting.getParameterValue();
                String[] startAndEndTime = { "", "" };
                if (!StringUtils.isEmpty(timeValue)) {
                    startAndEndTime = timeValue.split("--");
                }
                overSpeedValue.put("nightStartTime", startAndEndTime[0].trim());
                overSpeedValue.put("nightEndTime", startAndEndTime[1].trim());
                break;
            case "param5":
                String dateValue = alarmSetting.getParameterValue();
                String[] startAndEndDate = { "", "" };
                if (!StringUtils.isEmpty(dateValue)) {
                    startAndEndDate = dateValue.split("--");
                }
                overSpeedValue.put("nightStartDate", startAndEndDate[0].trim());
                overSpeedValue.put("nightEndDate", startAndEndDate[1].trim());
                break;
            case "param6":
                overSpeedValue.put("highWaySpeed", alarmSetting.getParameterValue());
                break;
            case "param7":
                overSpeedValue.put("nationalSpeed", alarmSetting.getParameterValue());
                break;
            case "param8":
                overSpeedValue.put("provinceSpeed", alarmSetting.getParameterValue());
                break;
            case "param9":
                overSpeedValue.put("countySpeed", alarmSetting.getParameterValue());
                break;
            case "param10":
                overSpeedValue.put("otherSpeed", alarmSetting.getParameterValue());
                break;
            case "param11":
                overSpeedValue.put("isRoadNetSpeedLimit", alarmSetting.getParameterValue());
                break;

            default:
                break;
        }
    }

    /**
     * 组装ACC信号异常参数
     */
    private void setAccSignalExceptionSettingValue(JSONObject accSignalExceptionValue,
        AlarmParameterSettingForm alarmSetting) {
        if (alarmSetting == null) {
            return;
        }
        String pos = alarmSetting.getPos();
        if (StringUtils.isBlank(pos)) {
            return;
        }
        if (!"150".equals(pos)) {
            return;
        }
        String paramCode = alarmSetting.getParamCode();
        if (StringUtils.isBlank(paramCode)) {
            return;
        }
        if (accSignalExceptionValue == null) {
            accSignalExceptionValue = new JSONObject();
        }
        switch (paramCode) {
            case "param1":
                // 异常持续时长
                accSignalExceptionValue.put("durationTime", alarmSetting.getParameterValue());
                break;
            case "param2":
                // 最高速度
                accSignalExceptionValue.put("maxSpeed", alarmSetting.getParameterValue());
                break;
            default:
                break;
        }

    }

    /**
     * 组装位置信息异常报警参数
     */
    private void setLocationExceptionSettingValue(JSONObject locationExceptionValue,
        AlarmParameterSettingForm alarmSetting) {
        if (alarmSetting == null) {
            return;
        }
        String alarmPos = alarmSetting.getPos();
        if (StringUtils.isBlank(alarmPos)) {
            return;
        }
        if (!"151".equals(alarmPos)) {
            return;
        }
        if (locationExceptionValue == null) {
            locationExceptionValue = new JSONObject();
        }
        locationExceptionValue.put("durationTime", alarmSetting.getParameterValue());
    }

    /**
     * 开启当前用户过检订阅
     */
    public void startCheck(String userName) {
        Subscribe subscribe = subscribeMap.get(userName);
        if (subscribe == null) {
            subscribe = new Subscribe(userName);
            subscribeMap.put(userName, subscribe);
        }
    }

    /**
     * 获取所有订阅了过检信息的用户
     */
    public List<String> getCheckUsers() {
        return new ArrayList<>(subscribeMap.keySet());
    }

    public void resendPositionSubscribe() {
        final Set<String> allPositions = WsSessionManager.INSTANCE.getAllPositions();
        if (CollectionUtils.isNotEmpty(allPositions)) {
            sendMsgToAll(addPrefix(ConstantUtil.PREFIX_POSITION, allPositions), ConstantUtil.WEB_SUBSCRIPTION_ADD);
        }
    }

    /**
     * 获取所有订阅了此设备OBD信息的用户
     */
    public List<String> getObdUsers(String deviceId) {
        List<String> list = new ArrayList<>();
        for (Subscribe subscribe : subscribeMap.values()) {
            if (subscribe.obdSet.contains(deviceId)) {
                list.add(subscribe.userName);
            }
        }
        return list;
    }

    /**
     * 增加当前用户订阅位置信息
     */
    public void subscribeObd(String userName, Set<String> deviceIds) {
        // 注：位置订阅统一由WsSessionManager维护，任何obd的订阅都会伴随着位置订阅
        Subscribe subscribe = subscribeMap.get(userName);
        if (subscribe == null) {
            subscribe = new Subscribe(userName);
            subscribeMap.put(userName, subscribe);
        }
        subscribe.obdSet.addAll(deviceIds);
        Set<String> vehicles = addPrefix(ConstantUtil.PREFIX_STATUS, deviceIds);
        sendMsgToAll(vehicles, ConstantUtil.WEB_SUBSCRIPTION_ADD);
    }

    /**
     * 取消当前用户订阅obd信息
     */
    public void canSubscribeObd(String userName, Set<String> deviceIds) {
        Subscribe subscribe = subscribeMap.get(userName);
        if (null == subscribe) {
            return;
        }
        subscribe.obdSet.removeAll(deviceIds);
    }

    /**
     * 增加当前用户应答
     */
    public void subscribeAck(String deviceId, Integer msgId) {
        final String key = ConstantUtil.PREFIX_MSG_ACK + msgId + "_" + deviceId;
        subscribeAckSet.add(key);
        sendMsgToAll(Collections.singleton(key), ConstantUtil.WEB_SUBSCRIPTION_ADD);
    }

    /**
     * 删除当前用户应答
     */
    public void canSubscribeAck(String deviceId, Integer msgId) {
        final String key = ConstantUtil.PREFIX_MSG_ACK + msgId + "_" + deviceId;
        if (subscribeAckSet.remove(key)) {
            sendMsgToAll(Collections.singleton(key), ConstantUtil.WEB_SUBSCRIPTION_REMOVE);
        }
    }

    private void writeAndFlush(Channel channel, Object msg, String id, String msgData) {
        channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            Throwable cause = future.cause();
            if (cause != null) {
                log.error("下发消息失败, ID:{}", id, cause);
                return;
            }
            log.info("下发消息, ID:{}, 内容:{}", id, msgData);
        });
    }

    /**
     * 广播消息
     */
    public void sendMsgToAll(Object data, int msgId) {
        String id = ConvertUtil.toHexString(msgId);
        String msgData = JSON.toJSONString(data);
        for (Channel channel : channelMap.values()) {
            if (!channel.isWritable()) {
                log.error("Netty下发通道消息队列已满");
                continue;
            }
            Message msg = MsgUtil.getMsg(msgId, data);
            writeAndFlush(channel, msg, id, msgData);
        }
    }

    /**
     * 广播消息
     */
    public void sendMsgToAll(Object data, Integer msgId, String deviceId) {
        String msgData = IGNORE_BODY_MSG_ID.contains(msgId) ? "[二进制文件]" : JSON.toJSONString(data);
        String id = ConvertUtil.toHexString(msgId);
        for (Channel channel : channelMap.values()) {
            if (!channel.isWritable()) {
                log.error("Netty下发通道消息队列已满");
                continue;
            }
            Message msg = MsgUtil.getMsg(msgId, deviceId, data);
            writeAndFlush(channel, msg, id, msgData);
        }
    }

    /**
     * 下发围栏线路调整
     */
    public void sendMsgToAll(Object data, Integer msgId, VehicleInfo vehicleInfo) {
        String id = ConvertUtil.toHexString(msgId);
        String msgData = JSON.toJSONString(data);
        for (Channel channel : channelMap.values()) {
            if (!channel.isWritable()) {
                log.error("Netty下发通道消息队列已满");
                continue;
            }
            Message msg = MsgUtil.getMsg(msgId, data, vehicleInfo);
            writeAndFlush(channel, msg, id, msgData);
        }
    }

    public void putChannel(String key, Channel channel) {
        log.info("添加netty通道key= {}", key);
        channelMap.compute(key, (k, v) -> null == v ? new DefaultQueuedChannel(channel) : v.setDelegate(channel));
    }

    public void removeChannel(String key) {
        log.info("移除netty通道key= {}", key);
        channelMap.computeIfPresent(key, (k, v) -> v.resetDelegate());
    }

    public void putVideoChannel(String key, Channel channel) {
        videoChannelMap.compute(key, (k, v) -> null == v ? new DefaultQueuedChannel(channel) : v.setDelegate(channel));
    }

    public void removeVideoChannel(String key) {
        channelMap.computeIfPresent(key, (k, v) -> v.resetDelegate());
    }

    public Channel getChannel(String key) {
        return channelMap.get(key);
    }

    public String getFlowStartTime(String key) {
        String startTime = null;
        if (StringUtils.isNotBlank(key)) {
            startTime = flowStartTime.get(key);
        }
        return startTime;
    }

    public void setFlowStartTime(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            flowStartTime.put(key, value);
        }
    }

    public void removeFlowStartTime(String key) {
        if (StringUtils.isNotBlank(key)) {
            flowStartTime.remove(key);
        }
    }

    /**
     * 更新状态订阅
     */
    public void updateSubStatus(Set<String> monitorIds) {
        for (String monitorId : monitorIds) {
            WsSessionManager.INSTANCE.removeStatusSessions(monitorId);
        }
    }

    /**
     * 清除订阅
     */
    public void clearSubUser() {
        if (!subscribeMap.isEmpty()) {
            subscribeMap.clear();
        }
    }

    private static class Subscribe {
        private final String userName;

        private final Set<String> obdSet = ConcurrentHashMap.newKeySet();

        Subscribe(String userName) {
            this.userName = userName;
        }
    }

    /**
     * 通过用户名获得客户端id
     * @return APP2.0.0新增
     */
    public String getUserClientId(String userName) {
        if (userNameAndClientId.containsKey(userName)) {
            return userNameAndClientId.get(userName);
        }
        return null;
    }

    /**
     * 新增用户的客户端id
     * @param userClientId APP2.0.0新增
     */
    public void addUserClientId(String userName, String userClientId) {
        userNameAndClientId.put(userName, userClientId);
    }

    /**
     * 删除用户的客户端id
     * @param userName APP2.0.0新增
     */
    public void removeUserClientId(String userName) {
        userNameAndClientId.remove(userName);
    }

    /**
     * 新增用户的websocket sessionId
     * @param userName APP2.0.0新增
     */
    public void addUserSessionId(String userName, String sessionId) {
        List<String> sessionIdList;
        if (userNameAndSessionIdList.containsKey(userName)) {
            sessionIdList = userNameAndSessionIdList.get(userName);
        } else {
            sessionIdList = new ArrayList<>();
        }
        sessionIdList.add(sessionId);
        userNameAndSessionIdList.put(userName, sessionIdList);
    }

    /**
     * 保存调度用户登录信息
     * @param userName                     用户名称
     * @param dispatchUserLoginInfoJsonObj 调度用户登录信息
     */
    public void saveDispatchUserLoginInfo(String userName, JSONObject dispatchUserLoginInfoJsonObj) {
        if (StringUtils.isNotBlank(userName)) {
            dispatchUserLoginInfoMap.put(userName, dispatchUserLoginInfoJsonObj);
        }
        log.info("调度员:" + userName + "登录;" + dispatchUserLoginInfoJsonObj.toJSONString());
    }

    /**
     * 移除调度用户登录信息
     * @param userName 用户名称
     */
    public void removeDispatchUserLoginInfo(String userName) {
        log.info("移除调度员:" + userName + "登录信息");
        if (StringUtils.isBlank(userName) || !dispatchUserLoginInfoMap.containsKey(userName)) {
            return;
        }
        dispatchUserLoginInfoMap.remove(userName);
    }

    /**
     * 获得调度用户登录信息
     * @param userName 用户名称
     * @return JSONObject
     */
    public JSONObject getDispatchUserLoginInfo(String userName) {
        if (StringUtils.isBlank(userName) || !dispatchUserLoginInfoMap.containsKey(userName)) {
            return null;
        }
        return dispatchUserLoginInfoMap.get(userName);
    }

    /**
     * 存储两客一危实时监控页面用户需要查询的报警车辆id
     */
    public void saveLkywUserNeedQueryMonitorIds(String username, List<String> monitorIds) {
        if (StringUtils.isBlank(username)) {
            return;
        }
        lkywUserQyeryAlarmMonitorIdsMap.remove(username);
        lkywUserQyeryAlarmMonitorIdsMap.put(username, monitorIds);
    }

    /**
     * 获取两客一危实时监控页面用户需要查询的报警车辆id
     */
    public List<String> getLkywUserNeedQueryMonitorIds(String username) {
        List<String> monitors = lkywUserQyeryAlarmMonitorIdsMap.get(username);
        return monitors == null ? new ArrayList<>() : monitors;
    }

    /**
     * 添加终端唤醒订阅关系
     */
    public void addDeviceWakeUpAckSubRelation(String monitorId, Integer msgNo, String sessionId) {
        subDeviceWakeUpAckMap.put(monitorId + "," + msgNo, sessionId);
    }

    /**
     * 获得终端唤醒sessionId
     */
    public String getDeviceWakeUpAckSessionId(String monitorId, Integer msgNo) {
        return subDeviceWakeUpAckMap.get(monitorId + "," + msgNo);
    }

    public String cancleSubDeviceWakeUpAck(String monitorId, Integer msgNo) {
        return subDeviceWakeUpAckMap.get(monitorId + "," + msgNo);
    }
}
