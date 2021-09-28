package com.zw.protocol.netty.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.utils.AdasDirectiveStatusOutTimeUtil;
import com.zw.lkyw.domain.VideoInspectionData;
import com.zw.lkyw.utils.sendMsgCache.SendMsgCache;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewDictionaryDao;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.param.HandleRemoteUpgradeParam;
import com.zw.platform.domain.param.RemoteUpgradeTask;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.share.ShapeUtil;
import com.zw.platform.domain.share.UploadTimeUtil;
import com.zw.platform.domain.statistic.form.FaultCodeForm;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.vas.alram.AlarmParameter;
import com.zw.platform.domain.vas.alram.AlarmParameterDetailsDTO;
import com.zw.platform.domain.vas.alram.AlarmSetting;
import com.zw.platform.domain.vas.alram.form.AlarmParameterSettingForm;
import com.zw.platform.domain.vas.alram.query.ElectricitySet;
import com.zw.platform.domain.vas.alram.query.TerminalSet;
import com.zw.platform.domain.vas.f3.SensorPolling;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.controller.InstanceMessageController;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.repository.modules.OBDVehicleTypeDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.alarm.AlarmSettingService;
import com.zw.platform.service.basicinfo.DeviceService;
import com.zw.platform.service.driverDiscernManage.DriverDiscernManageService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.reportManagement.RidershipService;
import com.zw.platform.service.sensor.RemoteUpgradeInstance;
import com.zw.platform.service.sensor.RemoteUpgradeToWeb;
import com.zw.platform.service.sensor.SensorPollingService;
import com.zw.platform.service.systems.ParameterService;
import com.zw.platform.util.AudioVideoUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.ConvertUtil;
import com.zw.platform.util.RemoteUpgradeUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.AlarmParameterUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.Monitor8104Cache;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.netty.client.service.ClientMessageService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClientMessageServiceImpl implements ClientMessageService {
    private static final Logger logger = LogManager.getLogger(ClientMessageServiceImpl.class);

    private static final String WORK_SETTING = "WORKSETTING";

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private AdasDirectiveStatusOutTimeUtil adasDirectiveStatusOutTimeUtil;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    @Autowired
    private SensorPollingService sensorPollingService;

    @Autowired
    private RidershipService ridershipService;

    @Autowired
    private AlarmSettingService alarmSettingService;

    @Autowired
    private OBDVehicleTypeDao obdVehicleTypeDao;

    @Autowired
    private NewDictionaryDao newDictionaryDao;

    @Autowired
    private WebSocketMessageDispatchCenter webSocketMessageDispatchCenter;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private ParamSendingCache paramSendingCache;

    @Autowired
    private SendMsgCache sendMsgCache;

    @Autowired
    private Monitor8104Cache monitor8104Cache;

    @Autowired
    private DriverDiscernManageService driverDiscernManageService;
    /**
     * 本机ip地址
     */
    private static final String LOCALHOST_IP = getLocalhostIp();

    /**
     * 服务器ip地址
     */
    @Value("${db.host}")
    private String serverIp;

    /**
     * 获取本机ip地址
     * @return ip
     */
    private static String getLocalhostIp() {
        String ip = "";
        try {
            // 获取本机IP
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("获取本机IP地址异常", e);
        }
        return ip;
    }

    /**
     * 查询终端参数应答0x0104
     */
    @Override
    public void saveDevieParamAckLog(Message message) {
        // 根据设备id查询车辆id
        String monitorId = message.getDesc().getMonitorId();
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(monitorId);
        if (bindDTO == null) {
            return;
        }
        String deviceType = bindDTO.getDeviceType();
        String deviceNumber = bindDTO.getDeviceNumber();
        String deviceGroupId = deviceService.findGroupIdByNumber(deviceNumber);
        JSONObject ackJson = JSON.parseObject(message.getData().toString());
        String brand = message.getDesc().getMonitorName();
        JSONObject msgBody = ackJson.getJSONObject("msgBody");
        if (MapUtils.isEmpty(msgBody)) {
            logger.error("查询终端参数应答0x0104: 消息体为空");
            return;
        }
        JSONArray params = msgBody.getJSONArray("params");
        LogSearchForm form = new LogSearchForm();
        Integer msgSnAck = msgBody.getInteger("msgSNAck");
        DirectiveForm lastDirective = parameterDao.getLastDirective(msgSnAck, monitorId, "0x8104-AlarmSettingData");
        // 报警参数设置查询
        Map<String, AlarmSetting> resultAlarmSetting = new HashMap<>(16);
        final String text;
        if (Objects.nonNull(lastDirective)) {
            // 协议类型1: 808-2013; 11: 808-2019
            text = "";
            for (int i = 0; i < params.size(); i++) {
                getAlarmSettingParam(params, i, resultAlarmSetting, deviceType);
            }
            form.setModule("ALARM_SETTING");
        } else if (monitor8104Cache.get(msgSnAck + "_" + message.getDesc().getDeviceId()) != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < params.size(); i++) {
                getLogMessage(params, builder, form, i);
            }
            text = builder.toString();
            form.setModule("MONITORING");
            monitor8104Cache.remove(msgSnAck + "_" + message.getDesc().getDeviceId());
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < params.size(); i++) {
                getLogMessage(params, builder, form, i);
            }
            text = builder.toString();
        }

        addLog(bindDTO, deviceGroupId, brand, text, form);

        if ("OILSETTING".equals(form.getModule()) || ClientMessageServiceImpl.WORK_SETTING
            .equals(form.getModule())) {
            String id = msgBody.getJSONArray("params").getJSONObject(0).getString("id");
            sendMessageToWeb(message, monitorId, msgSnAck, "/topic/oil" + id + "Info");
        }
        // 流水号, 监控对象ID + 类型: 确定唯一指令参数
        if ("RISKDEFINITION".equals(form.getModule())) {
            String id = msgBody.getJSONArray("params").getJSONObject(0).getString("id");
            // String m= JSON.toJSONString(message);
            sendMessageToWeb(message, monitorId, msgSnAck, "/topic/per" + id + "Info");
        }

        if ("VEHICLEOPERATION".equals(form.getModule())) {
            sendMessageToWeb(message, monitorId, msgSnAck, "/topic/operation_" + monitorId + "_Info");
        }
        if ("DRIVINGBEHAVIOR".equals(form.getModule())) {
            sendMessageToWeb(message, monitorId, msgSnAck, "/topic/behavior_" + monitorId + "_Info");
        }
        if ("TERMINALPARAM".equals(form.getModule())) {
            sendMessageToWeb(message, monitorId, msgSnAck, "/topic/terminal_" + monitorId + "_Info");
        }
        if ("MONITORING".equals(form.getModule())) {
            sendMessageToWeb(message, monitorId, msgSnAck, ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_USER);
        }
        //f3高精度
        if ("HIGHPRECISIONALARM".equals(form.getModule())) {
            sendMessageToWeb(message, monitorId, msgSnAck, ConstantUtil.F3_HIGH_PRECISION_ALARM);
        }
        if ("ALARM_SETTING".equals(form.getModule())) {
            List<AlarmParameter> alarmParameterList =
                alarmSettingService.getAlarmParameterByAlarmParameterIds(resultAlarmSetting.keySet());
            Map<String, AlarmParameter> alarmParameterMap =
                alarmParameterList.stream().collect(Collectors.toMap(AlarmParameter::getId, Function.identity()));
            Map<String, List<AlarmParameterSettingForm>> alarmParameterSettingMap = resultAlarmSetting.values()
                .stream().filter(obj -> alarmParameterMap.containsKey(obj.getAlarmParameterId()))
                .map(obj -> {
                    String alarmParameterId = obj.getAlarmParameterId();
                    AlarmParameter alarmParameter = alarmParameterMap.get(alarmParameterId);
                    obj.setPos(alarmParameter.getAlarmType());
                    obj.setName(alarmParameter.getAlarmTypeName());
                    obj.setParamCode(alarmParameter.getParamCode());
                    obj.setType(alarmParameter.getType());
                    return new AlarmParameterSettingForm(obj);
                }).collect(Collectors.groupingBy(AlarmParameterSettingForm::getType));
            AlarmParameterDetailsDTO alarmParameterDetailsDTO =
                AlarmParameterUtil.assemblePageDisplayData(alarmParameterSettingMap);
            String deviceId = message.getDesc().getDeviceId();
            msgBody.put("alarmSettingList", alarmParameterDetailsDTO);
            ackJson.put("msgBody", msgBody);
            message.setData(ackJson);
            parameterService.updateStatusByMsgSN(msgSnAck, monitorId, 0);
            SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSnAck, deviceId);
            if (info != null) {
                if (!"".equals(info.getUserName())) {
                    simpMessagingTemplateUtil.sendStatusMsg(info.getUserName(), "/topic/oil9999Info", message);
                } else {
                    simpMessagingTemplateUtil.sendStatusMsg("/topic/oil9999Info", message);
                }
                SubscibeInfoCache.getInstance().delTable(info);
            } else {
                simpMessagingTemplateUtil.sendStatusMsg("/topic/oil9999Info", message);
            }
        }
    }

    private void addLog(BindDTO bindDTO, String deviceGroupId, String brand, String text, LogSearchForm form) {
        form.setBrand(bindDTO.getName());
        Integer plateColorInt = bindDTO.getPlateColor();
        if (plateColorInt != null) {
            form.setPlateColor(plateColorInt);
        }
        form.setGroupId(deviceGroupId);
        form.setEventDate(new Date());
        // 获取到当前用户的用户名
        form.setLogSource("1");
        form.setMonitoringOperation("监控对象（" + brand + "） 查询终端参数应答");
        form.setMessage(text);
        logSearchService.addLogBean(form);
    }

    private void sendMessageToWeb(Message message, String monitorId, Integer msgSnAck, String destination) {
        String deviceId = message.getDesc().getDeviceId();
        parameterService.updateStatusByMsgSN(msgSnAck, monitorId, 0);
        SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSnAck, deviceId);
        if (info != null) {
            if (!"".equals(info.getUserName())) {
                simpMessagingTemplateUtil.sendStatusMsg(info.getUserName(), destination, message);
            } else {
                simpMessagingTemplateUtil.sendStatusMsg(destination, message);
            }
            SubscibeInfoCache.getInstance().delTable(info);
        } else {
            simpMessagingTemplateUtil.sendStatusMsg(destination, message);
        }
    }

    /**
     * 报警参数查询
     */
    private void getAlarmSettingParam(JSONArray params, int i, Map<String, AlarmSetting> resultAlarmSetting,
        String deviceType) {
        // 根据类型获取报警参数ID
        JSONObject obj = (JSONObject) params.get(i);
        int paramId = (int) obj.get("id");
        String resultValue = String.valueOf(obj.get("value"));
        switch (paramId) {
            case 0x005B:
                resultValue = String.valueOf(Integer.parseInt(resultValue) / 10);
                // 超速预警: checkId
                putAlarmSettingMap(resultAlarmSetting, "0x005B", resultValue);
                break;
            case 0x005C:
                // 疲劳驾驶预警
                putAlarmSettingMap(resultAlarmSetting, "0x005C", resultValue);
                break;
            case 0x005D:
                //碰撞预警
                putCrashData(resultAlarmSetting, deviceType, resultValue);
                break;
            case 0x005E:
                // 侧翻预警
                putAlarmSettingMap(resultAlarmSetting, "0x005E", resultValue);
                break;
            case 0x0055:
                // 超速报警-最高速度
                putAlarmSettingMap(resultAlarmSetting, "0x0055", resultValue);
                break;
            case 0x0056:
                // 超速报警-超速持续时间
                putAlarmSettingMap(resultAlarmSetting, "0x0056", resultValue);
                break;
            case 0x0057:
                // 疲劳驾驶-连续驾驶时间门限
                putAlarmSettingMap(resultAlarmSetting, "0x0057", resultValue);
                break;
            case 0x0059:
                // 疲劳驾驶-最小休息时间(0x0059)
                putAlarmSettingMap(resultAlarmSetting, "0x0059", resultValue);
                break;
            case 0x0058:
                // 疲劳驾驶-最小休息时间(0x0059)
                putAlarmSettingMap(resultAlarmSetting, "0x0058", resultValue);
                break;
            case 0x005A:
                // 超时停车-最长停车时间(0x005A)
                putAlarmSettingMap(resultAlarmSetting, "0x005A", resultValue);
                break;
            case 0x0032:
                JSONObject timeSlotJsonObj = JSON.parseObject(resultValue);
                if (timeSlotJsonObj == null) {
                    break;
                }
                Integer startHour = timeSlotJsonObj.getInteger("startHour");
                Integer startMinute = timeSlotJsonObj.getInteger("startMinute");
                Integer endHour = timeSlotJsonObj.getInteger("endHour");
                Integer endMinute = timeSlotJsonObj.getInteger("endMinute");
                if (startHour == null || startMinute == null || endHour == null || endMinute == null) {
                    break;
                }
                resultValue = startHour + ":" + startMinute + " -- " + endHour + ":" + endMinute;
                // 违规行驶报警-违规行驶时段(0x0032)
                putAlarmSettingMap(resultAlarmSetting, "0x0032", resultValue);
                break;
            case 0x0031:
                // 车辆非法位移-车辆非法位移(0x0031)
                putAlarmSettingMap(resultAlarmSetting, "0x0031", resultValue);
                break;
            case 0x0050:
                Set<String> deviceType2013Set = new HashSet<>(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2013_STR));
                Set<String> deviceType2019Set =
                    new HashSet<>(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR));
                if (deviceType2013Set.contains(deviceType)) {
                    deviceType = "1";
                } else if (deviceType2019Set.contains(deviceType)) {
                    deviceType = "11";
                }
                // 报警屏蔽(0x0050): 根据报警位来解析
                if (obj.get("value") instanceof Integer) {
                    int intValue = (int) obj.get("value");
                    for (int j = 0; j < 32; j++) {
                        Integer binaryValue = ConvertUtil.binaryIntegerWithOne(intValue, j);
                        getBinaryLongAlarm(resultAlarmSetting, j, binaryValue, deviceType);
                    }
                } else {
                    long longValue = (long) obj.get("value");
                    for (int j = 0; j < 32; j++) {
                        Integer binaryValue = ConvertUtil.binaryLongWithOne(longValue, j);
                        getBinaryLongAlarm(resultAlarmSetting, j, binaryValue, deviceType);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void getBinaryLongAlarm(Map<String, AlarmSetting> resultAlarmSetting, int j, Integer binaryValue,
        String deviceType) {
        boolean isShield;
        Map<String, String> deviceTypeAndAlarmParameterIdMap = AlarmTypeUtil.deviceAlarmMap.get(j);
        if (MapUtils.isEmpty(deviceTypeAndAlarmParameterIdMap)) {
            return;
        }
        String alarmParameterIds = deviceTypeAndAlarmParameterIdMap.get(deviceType);
        if (StringUtils.isEmpty(alarmParameterIds)) {
            // 如果报警参数ID不存在, 则继续执行
            return;
        }
        // 是否设置了报警屏蔽, 设置了则设置值为-2, 否则设置为-3, 表示不修改页面原有的参数
        isShield = (binaryValue == 1);
        Arrays.stream(alarmParameterIds.split(",")).forEach(alarmParameterId -> {
            AlarmSetting alarmSetting = resultAlarmSetting.get(alarmParameterId);
            if (Objects.nonNull(alarmSetting)) {
                alarmSetting.setAlarmPush(isShield ? -2 : -3);
            } else {
                alarmSetting = new AlarmSetting();
                alarmSetting.setAlarmParameterId(alarmParameterId);
                alarmSetting.setAlarmPush(isShield ? -2 : -3);
                resultAlarmSetting.put(alarmParameterId, alarmSetting);
            }
        });
    }

    private void putCrashData(Map<String, AlarmSetting> resultAlarmSetting, String deviceType, String resultValue) {
        Set<String> deviceType2013Set = new HashSet<>(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2013_STR));
        Set<String> deviceType2019Set = new HashSet<>(Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR));
        String timeKey;
        String speedKey;
        if (deviceType2013Set.contains(deviceType)) {
            timeKey = "0x005Dtime1";
            speedKey = "0x005Dspeed1";
        } else if (deviceType2019Set.contains(deviceType)) {
            timeKey = "0x005Dtime11";
            speedKey = "0x005Dspeed11";
        } else {
            timeKey = "0x005Dtime" + deviceType;
            speedKey = "0x005Dspeed" + deviceType;
        }
        String binaryString = Integer.toBinaryString(Integer.parseInt(resultValue));
        int valueLength = binaryString.length();
        int timeValue = 0;
        int speedValue = 0;
        if (valueLength >= 8) {
            // b7-b0： 碰撞时间，单位 4ms  binaryString为 1010100100  取 2 - 9
            String time = binaryString.substring(valueLength - 8, valueLength);
            timeValue = Integer.valueOf(time, 2);
            if (valueLength > 8) {
                // b15-b8：碰撞加速度，
                String speed = binaryString.substring(0, valueLength - 8);
                speedValue = Integer.valueOf(speed, 2);
            }
        } else {
            // 只有碰撞时间
            String time = binaryString.substring(0, valueLength);
            timeValue = Integer.valueOf(time, 2);
        }
        // 碰撞报警参数: T808-2013和T808-2011扩展版本, 碰撞时间要乘4
        if ("1".equals(deviceType) || "0".equals(deviceType)) {
            timeValue *= 4;
        }
        putAlarmSettingMap(resultAlarmSetting, timeKey, String.valueOf(timeValue));
        putAlarmSettingMap(resultAlarmSetting, speedKey, String.valueOf(speedValue));
    }

    private void putAlarmSettingMap(Map<String, AlarmSetting> resultAlarmSetting, String key, String resultValue) {
        String alarmParameterId = AlarmTypeUtil.instructAlarmMap.get(key);
        if (StringUtils.isEmpty(alarmParameterId)) {
            return;
        }
        AlarmSetting alarmSetting = resultAlarmSetting.get(alarmParameterId);
        if (Objects.nonNull(alarmSetting)) {
            alarmSetting.setParameterValue(resultValue);
        } else {
            alarmSetting = new AlarmSetting();
            alarmSetting.setAlarmParameterId(alarmParameterId);
            alarmSetting.setParameterValue(resultValue);
            resultAlarmSetting.put(alarmParameterId, alarmSetting);
        }
    }

    private void getLogMessage(JSONArray params, StringBuilder text, LogSearchForm form, int i) {
        Integer intValue;
        String strValue;
        long longValue;
        form.setModule("MONITORING");
        JSONObject obj = (JSONObject) params.get(i);
        Object value = obj == null ? null : obj.get("value");
        int paramId = obj == null ? -1 : obj.getIntValue("id");
        switch (paramId) {
            case 0x0001:// 终端心跳发送间隔，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append("终端心跳发送间隔：").append(intValue).append("秒<br/>");
                break;
            case 0x0002:// TCP 消息应答超时时间，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append("TCP 消息应答超时时间：").append(intValue).append("秒<br/>");
                break;
            case 0x0003:// TCP 消息重传次数
                intValue = value == null ? null : (Integer) value;
                text.append("TCP 消息重传次数：").append(intValue).append("<br/>");
                break;
            case 0x0004:// UDP 消息应答超时时间，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append("UDP 消息应答超时时间：").append(intValue).append("秒<br/>");
                break;
            case 0x0005:// UDP 消息重传次数
                intValue = value == null ? null : (Integer) value;
                text.append(" UDP 消息重传次数：").append(intValue).append("<br/>");
                break;
            case 0x0006:// SMS 消息应答超时时间，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append(" SMS 消息应答超时时间：").append(intValue).append("秒<br/>");
                break;
            case 0x0007:// SMS 消息重传次数
                intValue = value == null ? null : (Integer) value;
                text.append(" SMS 消息重传次数：").append(intValue).append("<br/>");
                break;
            case 0x0010:// 主服务器APN，无线通信拨号访问点。若网络制式为CDMA，则该处为PPP 拨号号码
                strValue = value == null ? null : value.toString();
                text.append(" 主服务器APN，无线通信拨号访问点：").append(strValue).append("<br/>");
                break;
            case 0x0011:// 主服务器无线通信拨号用户名
                strValue = value == null ? null : value.toString();
                text.append(" 主服务器无线通信拨号用户名：").append(strValue).append("<br/>");
                break;
            case 0x0012:// 主服务器无线通信拨号密码
                strValue = value == null ? null : value.toString();
                text.append(" 主服务器无线通信拨号密码：").append(strValue).append("<br/>");
                break;
            case 0x0013:// 主服务器地址,IP 或域名
                strValue = value == null ? null : value.toString();
                text.append(" 主服务器地址：").append(strValue).append("<br/>");
                break;
            case 0x0014:// 备份服务器APN，无线通信拨号访问点
                strValue = value == null ? null : value.toString();
                text.append(" 备份服务器APN，无线通信拨号访问点：").append(strValue).append("<br/>");
                break;
            case 0x0015:// 备份服务器无线通信拨号用户名
                strValue = value == null ? null : value.toString();
                text.append(" 备份服务器无线通信拨号用户名：").append(strValue).append("<br/>");
                break;
            case 0x0016:// 备份服务器无线通信拨号密码
                strValue = value == null ? null : value.toString();
                text.append(" 备份服务器无线通信拨号密码：").append(strValue).append("<br/>");
                break;
            case 0x0017:// 备份服务器地址,IP 或域名
                strValue = value == null ? null : value.toString();
                text.append(" 备份服务器地址：").append(strValue).append("<br/>");
                break;
            case 0x0018:// 服务器TCP 端口
                intValue = value == null ? null : (Integer) value;
                text.append(" 服务器TCP 端口：").append(intValue).append("<br/>");
                break;
            case 0x0019:// 服务器UDP 端口
                intValue = value == null ? null : (Integer) value;
                text.append(" 服务器UDP 端口：").append(intValue).append("<br/>");
                break;
            case 0x0020:// 位置汇报策略，0：定时汇报；1：定距汇报；2：定时和定距汇报
                intValue = value == null ? null : (Integer) value;
                text.append(" 位置汇报策略：").append(intValue).append("<br/>");
                break;
            case 0x0021:// 位置汇报方案，0：根据ACC 状态； 1：根据登录状态和ACC
                // 状态，先判断登录状态，若登录再根据ACC 状态
                intValue = value == null ? null : (Integer) value;
                text.append("位置汇报方案：").append(intValue).append("<br/>");
                break;
            case 0x0022:// 驾驶员未登录汇报时间间隔，单位为秒（s），>0
                intValue = value == null ? null : (Integer) value;
                text.append("驾驶员未登录汇报时间间隔：").append(intValue).append("秒<br/>");
                break;
            case 0x0027:// 休眠时汇报时间间隔，单位为秒（s），>0
                intValue = value == null ? null : (Integer) value;
                text.append("休眠时汇报时间间隔：").append(intValue).append("秒<br/>");
                break;
            case 0x0028:// 紧急报警时汇报时间间隔，单位为秒（s），>0
                intValue = value == null ? null : (Integer) value;
                text.append("紧急报警时汇报时间间隔：").append(intValue).append("秒<br/>");
                break;
            case 0x0029:// 缺省时间汇报间隔，单位为秒（s），>0
                intValue = value == null ? null : (Integer) value;
                text.append("缺省时间汇报间隔：").append(intValue).append("秒<br/>");
                break;
            case 0x002C:// 缺省距离汇报间隔，单位为米（m），>0
                intValue = value == null ? null : (Integer) value;
                text.append("缺省距离汇报间隔：").append(intValue).append("米<br/>");
                break;
            case 0x002D:// 驾驶员未登录汇报距离间隔，单位为米（m），>0
                intValue = value == null ? null : (Integer) value;
                text.append("驾驶员未登录汇报距离间隔：").append(intValue).append("米<br/>");
                break;
            case 0x002E:// 休眠时汇报距离间隔，单位为米（m），>0
                intValue = value == null ? null : (Integer) value;
                text.append("休眠时汇报距离间隔：").append(intValue).append("米<br/>");
                break;
            case 0x002F:// 紧急报警时汇报距离间隔，单位为米（m），>0
                intValue = value == null ? null : (Integer) value;
                text.append("紧急报警时汇报距离间隔：").append(intValue).append("米<br/>");
                break;
            case 0x0030:// 拐点补传角度，<180
                intValue = value == null ? null : (Integer) value;
                text.append("拐点补传角度：").append(intValue).append("度<br/>");
                break;
            case 0x0031:// 电子围栏半径（非法位移阈值），单位为米
                intValue = value == null ? null : (Integer) value;
                text.append("电子围栏半径：").append(intValue).append("米<br/>");
                break;
            case 0x0040:// 监控平台电话号码
                strValue = value == null ? null : value.toString();
                text.append("监控平台电话号码：").append(strValue).append("<br/>");
                break;
            case 0x0041:// 复位电话号码，可采用此电话号码拨打终端电话让终端复位
                strValue = value == null ? null : value.toString();
                text.append("复位电话号码：").append(strValue).append("<br/>");
                break;
            case 0x0042:// 恢复出厂设置电话号码，可采用此电话号码拨打终端电话让终端恢复出厂设置
                strValue = value == null ? null : value.toString();
                text.append("恢复出厂设置电话号码：").append(strValue).append("<br/>");
                break;
            case 0x0043:// 监控平台SMS 电话号码
                strValue = value == null ? null : value.toString();
                text.append("监控平台SMS 电话号码：").append(strValue).append("<br/>");
                break;
            case 0x0044:// 接收终端SMS 文本报警号码
                strValue = value == null ? null : value.toString();
                text.append("接收终端SMS 文本报警号码：").append(strValue).append("<br/>");
                break;
            case 0x0045:// 终端电话接听策略，0：自动接听；1：ACC ON 时自动接听，OFF 时手动接听
                intValue = value == null ? null : (Integer) value;
                text.append("终端电话接听策略：").append(intValue).append("<br/>");
                break;
            case 0x0046:// 每次最长通话时间，单位为秒（s），0 为不允许通话，0xFFFFFFFF 为不限制
                text.append("每次最长通话时间：").append(obj.get("value")).append("秒<br/>");
                break;
            case 0x0047:// 当月最长通话时间，单位为秒（s），0 为不允许通话，0xFFFFFFFF 为不限制
                text.append("当月最长通话时间：").append(obj.get("value")).append("秒<br/>");
                break;
            case 0x0048:// 监听电话号码
                strValue = value == null ? null : value.toString();
                text.append("监听电话号码：").append(strValue).append("<br/>");
                break;
            case 0x0049:// 监管平台特权短信号码
                strValue = value == null ? null : value.toString();
                text.append("监管平台特权短信号码：").append(strValue).append("<br/>");
                break;
            case 0x0050:// 报警屏蔽字，与位置信息汇报消息中的报警标志相对应，相应位为1则相应报警被屏蔽
                if (obj.get("value") instanceof Integer) {
                    intValue = (int) obj.get("value");
                    text.append("报警屏蔽字：").append(intValue).append("<br/>");
                } else {
                    longValue = (long) obj.get("value");
                    text.append("报警屏蔽字：").append(longValue).append("<br/>");
                }
                break;
            case 0x0051:// 报警发送文本SMS 开关，与位置信息汇报消息中的报警标志相对应，相应位为1
                // 则相应报警时发送文本SMS
                intValue = value == null ? null : (Integer) value;
                text.append("报警发送文本SMS 开关：").append(intValue).append("<br/>");
                break;
            case 0x0052:// 报警拍摄开关，与位置信息汇报消息中的报警标志相对应，相应位为1 则相应报警时摄像头拍摄
                intValue = value == null ? null : (Integer) value;
                text.append("报警拍摄开关：").append(intValue).append("<br/>");
                break;
            case 0x0053:// 报警拍摄存储标志，与位置信息汇报消息中的报警标志相对应，相应位为1
                // 则对相应报警时拍的照片进行存储，否则实时上传
                //                    intValue = (int) obj.get("value");
                text.append("报警拍摄存储标志：").append(obj.get("value")).append("<br/>");
                break;
            case 0x0054:// 关键标志，与位置信息汇报消息中的报警标志相对应，相应位为1 则对相应报警为关键报警
                intValue = value == null ? null : (Integer) value;
                text.append("关键标志：").append(intValue).append("<br/>");
                break;
            case 0x0055:// 最高速度，单位为公里每小时（km/h）
                intValue = value == null ? null : (Integer) value;
                text.append("最高速度：").append(intValue).append("km/h<br/>");
                break;
            case 0x0056:// 超速持续时间，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append("超速持续时间：").append(intValue).append("秒<br/>");
                break;
            case 0x0057:// 连续驾驶时间门限，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append("连续驾驶时间门限：").append(intValue).append("秒<br/>");
                break;
            case 0x0058:// 当天累计驾驶时间门限，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append("当天累计驾驶时间门限：").append(intValue).append("秒<br/>");
                break;
            case 0x0059:// 最小休息时间，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append("最小休息时间：").append(intValue).append("秒<br/>");
                break;
            case 0x005A:// 最长停车时间，单位为秒（s）
                intValue = value == null ? null : (Integer) value;
                text.append("最长停车时间：").append(intValue).append("秒<br/>");
                break;
            case 0x0070:// 图像/视频质量，1-10，1 最好
                intValue = value == null ? null : (Integer) value;
                text.append(" 图像/视频质量：").append(intValue).append("<br/>");
                break;
            case 0x0071:// 亮度，0-255
                intValue = value == null ? null : (Integer) value;
                text.append("亮度：").append(intValue).append("<br/>");
                break;
            case 0x0072:// 对比度，0-127
                intValue = value == null ? null : (Integer) value;
                text.append("对比度：").append(intValue).append("<br/>");
                break;
            case 0x0073:// 饱和度，0-127
                intValue = value == null ? null : (Integer) value;
                text.append("饱和度：").append(intValue).append("<br/>");
                break;
            case 0x0074:// 色度，0-255
                intValue = value == null ? null : (Integer) value;
                text.append("色度：").append(intValue).append("<br/>");
                break;
            case 0x0080:// 车辆里程表读数，1/10km
                intValue = value == null ? null : (Integer) value;
                text.append("车辆里程表读数（单位1/10km）：").append(intValue).append("<br/>");
                break;
            case 0x0081:// 车辆所在的省域ID
                intValue = value == null ? null : (Integer) value;
                text.append("车辆所在的省域ID：").append(intValue).append("<br/>");
                break;
            case 0x0082:// 车辆所在的市域ID
                intValue = value == null ? null : (Integer) value;
                text.append("车辆所在的市域ID：").append(intValue).append("<br/>");
                break;
            case 0x0083:// 公安交通管理部门颁发的机动车号牌
                strValue = value == null ? null : value.toString();
                text.append("公安交通管理部门颁发的机动车号牌：").append(strValue).append("<br/>");
                break;
            case 0x0084:// 车牌颜色，按照JT/T415-2006 的5.4.12
                intValue = value == null ? null : (Integer) value;
                text.append("车牌颜色：").append(intValue).append("<br/>");
                break;
            case 0xF841:// 油量车辆设置-基本信息 ，F3协议
                text.append("油量1车辆设置-基本信息<br/>");
                getParamOil(text, form, obj);
                break;
            case 0xF842:// 油量车辆设置-基本信息 ，F3协议
                text.append("油量2车辆设置-基本信息<br/>");
                if (value != null) {
                    JSONObject json2 = JSONObject.parseObject(value.toString());
                    text.append("公司名称:").append(json2.get("companyName")).append("<br/>");
                    text.append("产品编号:").append(json2.get("productCode")).append("<br/>");
                    text.append("客户代码:").append(json2.get("clientCode")).append("<br/>");
                    text.append("设备 ID:").append(json2.get("sensorID")).append("<br/>");
                    text.append("产品编号:").append(json2.get("productCode")).append("<br/>");
                    text.append("硬件版本号:").append(json2.get("hardwareVersionsCode")).append("<br/>");
                    text.append("软件版本号:").append(json2.get("softwareVersionsCode")).append("<br/>");
                }
                form.setModule("OILSETTING");
                break;
            case 0xF8E3:// 油量车辆设置-基本信息 ，F3协议
                text.append("胎压监测设置-基本信息<br/>");
                if (value != null) {
                    JSONObject tyreJson = JSONObject.parseObject(value.toString());
                    text.append("公司名称:").append(tyreJson.get("companyName")).append("<br/>");
                    text.append("产品编号:").append(tyreJson.get("productCode")).append("<br/>");
                    text.append("客户代码:").append(tyreJson.get("clientCode")).append("<br/>");
                    text.append("设备 ID:").append(tyreJson.get("sensorID")).append("<br/>");
                    text.append("产品编号:").append(tyreJson.get("productCode")).append("<br/>");
                    text.append("硬件版本号:").append(tyreJson.get("hardwareVersionsCode")).append("<br/>");
                    text.append("软件版本号:").append(tyreJson.get("softwareVersionsCode")).append("<br/>");
                }
                form.setModule("OILSETTING");
                break;
            case 0xF4E3:// 油量车辆设置-基本信息 ，F3协议
                text.append("胎压监测设置-常规参数-油箱1<br/>");
                if (value != null) {
                    getTyreReportStr(text, value.toString());
                }
                form.setModule("OILSETTING");
                break;
            case 0xF541:// 油量车辆设置-通讯参数 ，F3协议
                text.append("油量车辆1设置-通讯参数<br/>");
                if (value != null) {
                    JSONObject txjson = JSONObject.parseObject(value.toString());
                    text.append("外设ID:")
                            .append(txjson.get("sensorID"))
                            .append("<br/>")
                            .append("波特率:")
                            .append(BaudRateUtil.getBaudRateVal(txjson.getInteger("baudRate")))
                            .append("<br/>")
                            .append("奇偶校验:")
                            .append(ParityCheckUtil.getParityCheckVal(txjson.getInteger("oddEvenCheck")))
                            .append("<br/>");
                }
                form.setModule("OILSETTING");
                break;
            case 0xF542:// 油量车辆设置-通讯参数 ，F3协议
                text.append("油量车辆2设置-通讯参数<br/>");
                if (value != null) {
                    JSONObject txjson2 = JSONObject.parseObject(value.toString());
                    text.append("外设ID:")
                            .append(txjson2.get("sensorID"))
                            .append("<br/>")
                            .append("波特率:")
                            .append(BaudRateUtil.getBaudRateVal(txjson2.getInteger("baudRate")))
                            .append("<br/>")
                            .append("奇偶校验:")
                            .append(ParityCheckUtil.getParityCheckVal(txjson2.getInteger("oddEvenCheck")))
                            .append("<br/>");
                }
                form.setModule("OILSETTING");
                break;
            case 0xF441:// 油量车辆设置-常规参数-油箱1 ，F3协议
                text.append("油量车辆设置-常规参数-油箱1<br/>");
                if (value != null) {
                    getF3F4ReportStr(text, value.toString());
                }
                form.setModule("OILSETTING");
                break;
            case 0xF442:// 油量车辆设置-常规参数-油箱2 ，F3协议
                text.append("油量车辆设置-常规参数-油箱2<br/>");
                if (value != null) {
                    getF3F4ReportStr(text, value.toString());
                }
                form.setModule("OILSETTING");
                break;
            case 0xF641:// 油量车辆设置-标定参数 ，F3协议
                text.append("油量车辆1设置-标定参数：<br/>");
                if (value != null) {
                    JSONObject bdjson = JSONObject.parseObject(value.toString());
                    text.append("外设ID:").append(bdjson.get("sensorID")).append("<br/>");
                    JSONArray jsonArray = bdjson.getJSONArray("list");
                    text.append("标定组数:").append(jsonArray.size()).append("<br/>");
                    for (int index = 0; index < jsonArray.size(); index++) {
                        JSONObject tempb = jsonArray.getJSONObject(index);
                        text.append("标定第").append(index + 1).append("组数:液位高度(mm)").append(tempb.get("key"))
                            .append(" 油量值(升)").append(tempb.get("value")).append("<br/>");
                    }
                }
                form.setModule("OILSETTING");
                break;
            case 0xF642:// 油量车辆设置-标定参数 ，F3协议
                text.append("油量车辆2设置-标定参数：<br/>");
                if (value != null) {
                    JSONObject bdjson2 = JSONObject.parseObject(value.toString());
                    text.append("外设ID:").append(bdjson2.get("sensorID")).append("<br/>");
                    JSONArray jsonArray1 = bdjson2.getJSONArray("list");
                    text.append("标定组数:").append(jsonArray1.size()).append("<br/>");
                    for (int index = 0; index < jsonArray1.size(); index++) {
                        JSONObject tempb = jsonArray1.getJSONObject(index);
                        text.append("标定第").append(index).append(1).append("组数:液位高度(mm)").append(tempb.get("key"))
                            .append(" 油量值(升)").append(tempb.get("value")).append("<br/>");
                    }
                }
                form.setModule("OILSETTING");
                break;
            case 0xF670:// 油量车辆设置-标定参数 ，F3协议
                text.append("载重设置-标定参数：<br/>");
                if (value != null) {
                    JSONObject loadjson = JSONObject.parseObject(value.toString());
                    text.append("外设ID:").append(loadjson.get("sensorID")).append("<br/>");
                    JSONArray loadJsonArray = loadjson.getJSONArray("list");
                    text.append("标定组数:").append(loadJsonArray.size()).append("<br/>");
                    for (int index = 0; index < loadJsonArray.size(); index++) {
                        JSONObject tempb = loadJsonArray.getJSONObject(index);
                        text.append("标定第").append(index).append(1).append("组数:AD值").append(tempb.get("key"))
                            .append(" 实际载重值").append(tempb.get("value")).append("<br/>");
                    }
                }
                form.setModule("OILSETTING");
                break;
            case 0xF671:// 油量车辆设置-标定参数 ，F3协议
                text.append("油量车辆2设置-标定参数：<br/>");
                if (value != null) {
                    JSONObject loadjson2 = JSONObject.parseObject(value.toString());
                    text.append("外设ID:").append(loadjson2.get("sensorID")).append("<br/>");
                    JSONArray loadJsonArray1 = loadjson2.getJSONArray("list");
                    text.append("标定组数:").append(loadJsonArray1.size()).append("<br/>");
                    for (int index = 0; index < loadJsonArray1.size(); index++) {
                        JSONObject tempb = loadJsonArray1.getJSONObject(index);
                        text.append("标定第").append(index).append(1).append("组数:AD值").append(tempb.get("key"))
                            .append(" 实际载重值").append(tempb.get("value")).append("<br/>");
                    }
                }
                form.setModule("OILSETTING");
                break;
            case 0xF445:
                text.append("油耗车辆设置-常规参数<br/>");
                if (value != null) {
                    JSONObject oilParam = JSONObject.parseObject(value.toString());
                    text.append("外设ID:").append(oilParam.get("sensorID")).append("<br/>");
                    JSONObject cgjson = oilParam.getJSONObject("oilParam");
                    text.append("补偿使能:")
                            .append(CompEnUtil.getCompEnVal(cgjson.getInteger("inertiaCompEn")))
                            .append("<br/>")
                            .append("滤波系数:")
                            .append(FilterFactorUtil.getFilterFactorVal(cgjson.getInteger("smoothing")))
                            .append("<br/>")
                            .append("自动上传时间:")
                            .append(UploadTimeUtil.getUploadTimeVal(cgjson.getInteger("autoInterval")))
                            .append("<br/>")
                            .append("输出修正系数 K:")
                            .append(cgjson.get("outputCorrectionK"))
                            .append("<br/>")
                            .append("输出修正常数 B:")
                            .append(cgjson.get("outputCorrectionB"))
                            .append("<br/>");
                }
                form.setModule("OILSETTING");
                break;
            case 0xF545:
                text.append("油耗车辆设置-通讯参数<br/>");
                if (value != null) {
                    JSONObject txjson3 = JSONObject.parseObject(value.toString());
                    text.append("外设ID:")
                            .append(txjson3.get("sensorID"))
                            .append("<br/>")
                            .append("波特率:")
                            .append(BaudRateUtil.getBaudRateVal(txjson3.getInteger("baudRate")))
                            .append("<br/>")
                            .append("奇偶校验:")
                            .append(ParityCheckUtil.getParityCheckVal(txjson3.getInteger("oddEvenCheck")))
                            .append("<br/>");
                }
                form.setModule("OILSETTING");
                break;
            case 0xF845:
                text.append("油耗车辆设置-基本信息<br/>");
                if (value != null) {
                    JSONObject jbjson = JSONObject.parseObject(value.toString());
                    text.append("公司名称:").append(jbjson.get("companyName")).append("<br/>");
                    text.append("产品编号:").append(jbjson.get("productCode")).append("<br/>");
                    text.append("客户代码:").append(jbjson.get("clientCode")).append("<br/>");
                    text.append("设备 ID:").append(jbjson.get("sensorID")).append("<br/>");
                    text.append("产品编号:").append(jbjson.get("productCode")).append("<br/>");
                    text.append("硬件版本号:").append(jbjson.get("hardwareVersionsCode")).append("<br/>");
                    text.append("软件版本号:").append(jbjson.get("softwareVersionsCode")).append("<br/>");
                }
                form.setModule("OILSETTING");
                break;
            case 0xF880:
                text.append("发动机1工时设置-基本信息<br/>");
                getParamBasicInfo(text, form, obj);
                break;
            case 0xF881:
                text.append("发动机2工时设置-基本信息<br/>");
                getParamBasicInfo(text, form, obj);
                break;
            case 0xF580:
                text.append("发动机1工时设置-通讯参数<br/>");
                getParamCommunication(text, form, obj);
                break;
            case 0xF581:
                text.append("发动机2工时设置-通讯参数<br/>");
                getParamCommunication(text, form, obj);
                break;
            case 0xF480:
                text.append("发动机1工时设置-常规参数<br/>");
                getParamRoutine(text, form, obj);
                break;
            case 0xF481:
                text.append("发动机2工时设置-常规参数<br/>");
                getParamRoutine(text, form, obj);
                break;
            case 0xF870:
            case 0xF871:
                text.append("载重传感器设置-基本信息<br/>");
                getParamBasicInfo(text, form, obj);
                break;
            case 0xF570:
            case 0xF571:
                text.append("载重传感器设置-通讯参数<br/>");
                getParamCommunication(text, form, obj);
                break;
            case 0xF470:
            case 0xF471:
                text.append("载重传感器设置-常规参数<br/>");
                getLoadParam(text, obj);
                form.setModule(ClientMessageServiceImpl.WORK_SETTING);
                break;
            case 0xF3E1:
                text.append("风险定义设置-查询高级驾驶辅助系统参数<br/>");
                text.append("高级驾驶辅助系统参数<br/>");
                getParamAssist(text, form, obj);
                break;
            case 0xF3E2:
                text.append("风险定义设置-查询驾驶员状态监测参数<br/>");
                text.append("驾驶员状态监测参数<br/>");
                getParamSurveyInfo(text, form, obj);
                break;
            case 0xF364:
                text.append("风险定义设置-查询前向参数设置<br/>");
                text.append("风险定义设置-查询高级驾驶辅助系统参数(鲁湘粤)<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF365:
                text.append("风险定义设置-查询驾驶员激烈驾驶参数设置<br/>");
                text.append("风险定义设置-查询驾驶员状态监测系统参数(鲁湘粤)<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF366:
                text.append("风险定义设置-查询胎压参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF367:
                text.append("风险定义设置-查询盲区参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF368:
                text.append("风险定义设置-查询不按规定上下课参数设置<br/>");
                text.append("风险定义设置-查询车辆监测(湘)<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF370:
                text.append("风险定义设置-查询高级驾驶辅助系统状态<br/>");
                text.append("风险定义设置-查询激烈驾驶检测功能参数设置(鲁)<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF0E1:
                text.append("风险定义设置-查询前向监测系统参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF0E2:
                text.append("风险定义设置-查询驾驶员监测系统参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF0E3:
                text.append("风险定义设置-查询轮胎气压监控系统参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF0E4:
                text.append("风险定义设置-查询盲区监测系统参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF0E9:
                text.append("风险定义设置-查询驾驶员比对参数设置(鲁湘)<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xE138:
                text.append("风险定义设置-查询驾驶员身份识别参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xE139:
                text.append("风险定义设置-查询车辆运行监测参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xE140:
                text.append("风险定义设置-查询驾驶员驾驶行为参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xE141:
                text.append("风险定义设置-查询设备失效监测参数设置<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF511:
                text.append("驾驶行为报警-疲劳驾驶参数设置<br/>");
                form.setModule("DRIVINGBEHAVIOR");
                break;
            case 0xF512:
                text.append("驾驶行为报警-分神驾驶参数设置<br/>");
                form.setModule("DRIVINGBEHAVIOR");
                break;
            case 0xF513:
                text.append("驾驶行为报警-抽烟参数设置<br/>");
                form.setModule("DRIVINGBEHAVIOR");
                break;
            case 0xF514:
                text.append("驾驶行为报警-接打手持电话参数设置<br/>");
                form.setModule("DRIVINGBEHAVIOR");
                break;
            case 0xF515:
                text.append("驾驶行为报警-双手同时脱离参数设置<br/>");
                form.setModule("DRIVINGBEHAVIOR");
                break;
            case 0xF516:
                text.append("驾驶行为报警-未系安全带参数设置<br/>");
                form.setModule("DRIVINGBEHAVIOR");
                break;
            case 0xF517:
                text.append("驾驶行为报警-驾驶人员异常参数设置<br/>");
                form.setModule("DRIVINGBEHAVIOR");
                break;
            case 0xF521:
                text.append("车辆运行监测报警-前车碰撞参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xF522:
                text.append("车辆运行监测报警-车距过近参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xF523:
                text.append("车辆运行监测报警-车道偏离参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xF524:
                text.append("车辆运行监测报警-行人碰撞参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xF525:
                text.append("车辆运行监测报警-盲区监测参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xF526:
                text.append("车辆运行监测报警-限速参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xF527:
                text.append("车辆运行监测报警-限高参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xF528:
                text.append("车辆运行监测报警-限宽参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xF529:
                text.append("车辆运行监测报警-限重参数设置<br/>");
                form.setModule("VEHICLEOPERATION");
                break;
            case 0xFF00:
                text.append("终端MAC地址<br/>");
                form.setModule("TERMINALPARAM");
                break;
            case 0xFF01:
                text.append("制造商ID<br/>");
                form.setModule("TERMINALPARAM");
                break;
            case 0xFF02:
                text.append("终端型号<br/>");
                form.setModule("TERMINALPARAM");
                break;
            case 0xF1E1:
                text.append("风险定义设置-查询前向监测系统状态<br/>");
                if (value != null) {
                    JSONObject statejson1 = JSONObject.parseObject(value.toString());
                    text.append("外设id").append(statejson1.get("sensorID")).append("<br/>");
                    text.append("工作状态").append(statejson1.get("workStatus")).append("<br/>");
                    text.append("报警状态").append(statejson1.get("alarmStatus")).append("<br/>");
                }
                form.setModule("RISKDEFINITION");
                break;
            case 0xF1E2:
                text.append("风险定义设置-查询驾驶员监测系统状态<br/>");
                if (value != null) {
                    JSONObject statejson2 = JSONObject.parseObject(value.toString());
                    text.append("外设id").append(statejson2.get("sensorID")).append("<br/>");
                    text.append("工作状态").append(statejson2.get("workStatus")).append("<br/>");
                    text.append("报警状态").append(statejson2.get("alarmStatus")).append("<br/>");
                }
                form.setModule("RISKDEFINITION");
                break;
            case 0xF1E3:
                text.append("风险定义设置-查询轮胎气压监测系统状态<br/>");
                if (value != null) {
                    JSONObject statejson3 = JSONObject.parseObject(value.toString());
                    text.append("外设id").append(statejson3.get("sensorID")).append("<br/>");
                    text.append("工作状态").append(statejson3.get("workStatus")).append("<br/>");
                    text.append("报警状态").append(statejson3.get("alarmStatus")).append("<br/>");
                }
                form.setModule("RISKDEFINITION");
                break;
            case 0xF1E4:
                text.append("风险定义设置-查询盲区监测系统状态<br/>");
                if (value != null) {
                    JSONObject statejson4 = JSONObject.parseObject(value.toString());
                    text.append("外设id").append(statejson4.get("sensorID")).append("<br/>");
                    text.append("工作状态").append(statejson4.get("workStatus")).append("<br/>");
                    text.append("报警状态").append(statejson4.get("alarmStatus")).append("<br/>");
                }
                form.setModule("RISKDEFINITION");
                break;
            case 0xF2E1:
                text.append("风险定义设置-查询前向监测系统基本信息<br/>");
                if (value != null) {
                    JSONObject infojson1 = JSONObject.parseObject(value.toString());
                    text.append("公司名称:").append(infojson1.get("companyName")).append("<br/>");
                    text.append("产品代码:").append(infojson1.get("productCode")).append("<br/>");
                    text.append("硬件版本号:").append(infojson1.get("hardwareVersionsCode")).append("<br/>");
                    text.append("软件版本号:").append(infojson1.get("softwareVersionsCode")).append("<br/>");
                    text.append("外设 ID:").append(infojson1.get("sensorID")).append("<br/>");
                    text.append("客户代码:").append(infojson1.get("clientCode")).append("<br/>");
                }
                form.setModule("RISKDEFINITION");
                break;
            case 0xF2E2:
                text.append("风险定义设置-查询驾驶员监测系统基本信息<br/>");
                if (value != null) {
                    JSONObject infojson2 = JSONObject.parseObject(value.toString());
                    text.append("公司名称:").append(infojson2.get("companyName")).append("<br/>");
                    text.append("产品代码:").append(infojson2.get("productCode")).append("<br/>");
                    text.append("硬件版本号:").append(infojson2.get("hardwareVersionsCode")).append("<br/>");
                    text.append("软件版本号:").append(infojson2.get("softwareVersionsCode")).append("<br/>");
                    text.append("外设 ID:").append(infojson2.get("sensorID")).append("<br/>");
                    text.append("客户代码:").append(infojson2.get("clientCode")).append("<br/>");
                }
                form.setModule("RISKDEFINITION");
                break;
            case 0xF2E3:
                text.append("风险定义设置-查询轮胎气压监测系统基本信息<br/>");
                if (value != null) {
                    JSONObject infojson3 = JSONObject.parseObject(value.toString());
                    text.append("公司名称:").append(infojson3.get("companyName")).append("<br/>");
                    text.append("产品代码:").append(infojson3.get("productCode")).append("<br/>");
                    text.append("硬件版本号:").append(infojson3.get("hardwareVersionsCode")).append("<br/>");
                    text.append("软件版本号:").append(infojson3.get("softwareVersionsCode")).append("<br/>");
                    text.append("外设 ID:").append(infojson3.get("sensorID")).append("<br/>");
                    text.append("客户代码:").append(infojson3.get("clientCode")).append("<br/>");
                }
                form.setModule("RISKDEFINITION");
                break;
            case 0xF2E4:
                text.append("风险定义设置-查询盲区监测系统基本信息<br/>");
                if (value != null) {
                    JSONObject infojson4 = JSONObject.parseObject(value.toString());
                    text.append("公司名称:").append(infojson4.get("companyName")).append("<br/>");
                    text.append("产品代码:").append(infojson4.get("productCode")).append("<br/>");
                    text.append("硬件版本号:").append(infojson4.get("hardwareVersionsCode")).append("<br/>");
                    text.append("软件版本号:").append(infojson4.get("softwareVersionsCode")).append("<br/>");
                    text.append("外设 ID:").append(infojson4.get("sensorID")).append("<br/>");
                    text.append("客户代码:").append(infojson4.get("clientCode")).append("<br/>");
                }
                form.setModule("RISKDEFINITION");
                break;
            case 0xF2E7:
                text.append("风险定义设置-查询终端路网地图信息<br/>");
                form.setModule("RISKDEFINITION");
                break;
            case 0xF0E5:
                text.append("OBD基本信息<br/>");
                if (value != null) {
                    JSONObject json = JSONObject.parseObject(value.toString());
                    JSONObject obdjson = JSONObject.parseObject(json.get("setStreamObd").toString());
                    String code16Str = "0x" + Integer.toHexString(obdjson.getInteger("vehicleTypeId"));
                    text.append("车型ID:").append(code16Str).append("<br/>");
                    text.append("数据流上传时间间隔:").append(obdjson.get("uploadTime")).append("<br/>");
                }
                form.setModule("OILSETTING");
                break;
            case 0xF44F:
                text.append("F3高精度报警<br/>");
                if (value != null) {
                    JSONObject highPrecisionAlarm = JSONObject.parseObject(value.toString());
                    ElectricitySet electricitySet =
                        JSON.parseObject(highPrecisionAlarm.getString("electricitySet"), ElectricitySet.class);
                    text.append("电量报警阈值:").append(electricitySet.getDeviceElectricity()).append("<br/>");
                }
                form.setModule("HIGHPRECISIONALARM");
                break;
            case 0xF450:
                if (value != null) {
                    JSONObject highPrecisionAlarm1 = JSONObject.parseObject(value.toString());
                    TerminalSet terminalSet =
                        JSON.parseObject(highPrecisionAlarm1.getString("terminalSet"), TerminalSet.class);
                    text.append("急加速报警阈值:").append(terminalSet.getSpeedUpAlarm()).append("<br/>");
                    text.append("急减速报警阈值:").append(terminalSet.getSpeedCutAlarm()).append("<br/>");
                    text.append("急转弯报警阈值:").append(terminalSet.getSwerveAlarm()).append("<br/>");
                }
                form.setModule("HIGHPRECISIONALARM");
                break;
            default:
                break;
        }
    }

    /**
     * 风险定义设置-查询驾驶员状态监测参数
     * @param text 返回String
     * @param form 表单
     * @param obj  json
     * @return text
     */
    private void getParamSurveyInfo(StringBuilder text, LogSearchForm form, JSONObject obj) {
        if (obj.get("value") != null) {
            JSONObject json1 = JSONObject.parseObject(obj.get("value").toString());
            JSONObject json = JSONObject.parseObject(json1.get("surveyInfo").toString());
            text.append("疲劳驾驶速度阈值:").append(json.get("yawnSpeed")).append("(km/h)<br/>");
            text.append("疲劳驾驶拍照间隔:").append(json.get("yawnCameraTime")).append("秒<br/>");
            text.append("疲劳驾驶拍照张数:").append(json.get("vehicleCollisionCameraNum")).append("<br/>");
            text.append("疲劳驾驶录制时间:").append(json.get("yawnVideoTime")).append("秒<br/>");
            text.append("抽烟速度阈值:").append(json.get("smokingSpeed")).append("(km/h)<br/>");
            text.append("抽烟拍照间隔:").append(json.get("smokingCameraTime")).append("秒<br/>");
            text.append("抽烟拍照张数:").append(json.get("smokingCameraNum")).append("<br/>");
            text.append("抽烟录制时间:").append(json.get("smokingVideoTime")).append("秒<br/>");
            text.append("抽烟时间间隔:").append(json.get("smokingTime")).append("秒<br/>");
            text.append("接打手持电话时间间隔:").append(json.get("pickUpTime")).append("m<br/>");
            text.append("接打手持电话度阈值:").append(json.get("pickUpSpeed")).append("(km/h)<br/>");
            text.append("接打手持电话拍照间隔:").append(json.get("pickUpCameraTime")).append("秒<br/>");
            text.append("接打手持电话拍照张数:").append(json.get("pickUpCameraNum")).append("<br/>");
            text.append("接打手持电话录制时间:").append(json.get("pickUpVideoTime")).append("秒<br/>");
            text.append("长时间不目视前方速度阈值:").append(json.get("postureSpeed")).append("(km/h)<br/>");
            text.append("长时间不目视前方拍照间隔:").append(json.get("postureCameraTime")).append("秒<br/>");
            text.append("长时间不目视前方拍照张数:").append(json.get("postureCameraNum")).append("<br/>");
            text.append("长时间不目视前方录制时间:").append(json.get("postureVideoTime")).append("秒<br/>");
            text.append("未检测到驾驶员拍照间隔:").append(json.get("checkIdentCameraTime")).append("秒<br/>");
            text.append("未检测到驾驶员拍照张数:").append(json.get("checkIdentCameraNum")).append("<br/>");
            text.append("未检测到驾驶员录制时间:").append(json.get("checkIdentVideoTime")).append("秒<br/>");
        }
        form.setModule("RISKDEFINITION");
    }

    /**
     * 风险定义设置-查询高级驾驶辅助系统参数设置
     * @param text 返回文本
     * @param form 表单
     * @param obj  json
     * @return 文本
     */
    private void getParamAssist(StringBuilder text, LogSearchForm form, JSONObject obj) {
        if (obj.get("value") != null) {
            JSONObject json1 = JSONObject.parseObject(obj.get("value").toString());
            JSONObject json = JSONObject.parseObject(json1.get("assist").toString());
            text.append("未检测到驾驶员速度阈值:").append(json.get("vehicleCollisionSpeed")).append("(km/h)<br/>");
            text.append("未检测到驾驶员拍照间隔:").append(json.get("vehicleCollisionCameraTime")).append("秒<br/>");
            text.append("未检测到驾驶员拍照张数:").append(json.get("vehicleCollisionCameraNum")).append("<br/>");
            text.append("未检测到驾驶员录制时间:").append(json.get("vehicleCollisionVideoTime")).append("秒<br/>");
            text.append("车道偏离速度阈值:").append(json.get("deviateSpeed")).append("(km/h)<br/>");
            text.append("车道偏离拍照间隔:").append(json.get("deviateCameraTime")).append("秒<br/>");
            text.append("车道偏离拍照张数:").append(json.get("deviateCameraNum")).append("<br/>");
            text.append("车道偏离录制时间:").append(json.get("deviateVideoTime")).append("秒<br/>");
            text.append("车距过近距离阈值:").append(json.get("distanceMail")).append("m<br/>");
            text.append("车距过近速度阈值:").append(json.get("distanceSpeed")).append("(km/h)<br/>");
            text.append("车距过近拍照间隔:").append(json.get("distanceCameraTime")).append("秒<br/>");
            text.append("车距过近拍照张数:").append(json.get("distanceCameraNum")).append("<br/>");
            text.append("车距过近录制时间:").append(json.get("distanceVideoTime")).append("秒<br/>");
            text.append("行人碰撞时距阈值:").append(json.get("pedestrianCollisionSpeed")).append("m<br/>");
            text.append("行人碰撞速度阈值:").append(json.get("pedestrianCollisionSpeed")).append("(km/h)<br/>");
            text.append("行人碰撞拍照间隔:").append(json.get("pedestrianCollisionCameraTime")).append("秒<br/>");
            text.append("行人碰撞拍照张数:").append(json.get("pedestrianCollisionCameraNum")).append("<br/>");
            text.append("行人碰撞录制时间:").append(json.get("pedestrianCollisionVideoTime")).append("秒<br/>");
            text.append("频繁变道时间段阈值:").append(json.get("laneChangeTime")).append("秒<br/>");
            text.append("频繁变道速度阈值:").append(json.get("laneChangeSpeed")).append("(km/h)<br/>");
            text.append("频繁变道拍照间隔:").append(json.get("laneChangeCameraTime")).append("秒<br/>");
            text.append("频繁变道拍照张数:").append(json.get("laneChangeCameraNum")).append("<br/>");
            text.append("频繁变道录制时间:").append(json.get("laneChangeVideoTime")).append("秒<br/>");
            text.append("频繁变道次数阈值:").append(json.get("laneChangeNum")).append("秒<br/>");
        }
        form.setModule("RISKDEFINITION");
    }

    private void getParamOil(StringBuilder text, LogSearchForm form, JSONObject obj) {
        if (obj.get("value") != null) {
            JSONObject json = JSONObject.parseObject(obj.get("value").toString());
            text.append("公司名称:").append(json.get("companyName")).append("<br/>");
            text.append("产品编号:").append(json.get("productCode")).append("<br/>");
            text.append("客户代码:").append(json.get("clientCode")).append("<br/>");
            text.append("设备 ID:").append(json.get("sensorID")).append("<br/>");
            text.append("产品编号:").append(json.get("productCode")).append("<br/>");
            text.append("硬件版本号:").append(json.get("hardwareVersionsCode")).append("<br/>");
            text.append("软件版本号:").append(json.get("softwareVersionsCode")).append("<br/>");
        }
        form.setModule("OILSETTING");
    }

    /**
     * 基本信息设置
     */
    private void getParamBasicInfo(StringBuilder text, LogSearchForm form, JSONObject obj) {
        if (obj.get("value") != null) {
            JSONObject workJson1 = JSONObject.parseObject(obj.get("value").toString());
            text.append("公司名称:").append(workJson1.get("companyName")).append("<br/>");
            text.append("产品编号:").append(workJson1.get("productCode")).append("<br/>");
            text.append("客户代码:").append(workJson1.get("clientCode")).append("<br/>");
            text.append("设备 ID:").append(workJson1.get("sensorID")).append("<br/>");
            text.append("产品编号:").append(workJson1.get("productCode")).append("<br/>");
            text.append("硬件版本号:").append(workJson1.get("hardwareVersionsCode")).append("<br/>");
            text.append("软件版本号:").append(workJson1.get("softwareVersionsCode")).append("<br/>");
        }
        form.setModule(ClientMessageServiceImpl.WORK_SETTING);
    }

    /**
     * 通讯参数设置
     */
    private void getParamCommunication(StringBuilder text, LogSearchForm form, JSONObject obj) {
        if (obj.get("value") != null) {
            JSONObject workJson4 = JSONObject.parseObject(obj.get("value").toString());
            text.append("外设ID:")
                    .append(workJson4.get("sensorID"))
                    .append("<br/>")
                    .append("波特率:")
                    .append(BaudRateUtil.getBaudRateVal(workJson4.getInteger("baudRate")))
                    .append("<br/>")
                    .append("奇偶校验:")
                    .append(ParityCheckUtil.getParityCheckVal(workJson4.getInteger("oddEvenCheck")))
                    .append("<br/>");
        }
        form.setModule(ClientMessageServiceImpl.WORK_SETTING);
    }

    /**
     * 设置工时常规参数
     */
    private void getParamRoutine(StringBuilder text, LogSearchForm form, JSONObject obj) {
        getWorkHourSettingParam(text, obj);
        form.setModule(ClientMessageServiceImpl.WORK_SETTING);
    }

    private void getWorkHourSettingParam(StringBuilder text, JSONObject obj) {
        if (obj.get("value") != null) {
            JSONObject workParam = JSONObject.parseObject(obj.get("value").toString());
            text.append("外设ID:").append(workParam.get("sensorID")).append("<br/>");
            JSONObject workJson6 = workParam.getJSONObject("workHourSettingParam");
            text.append("补偿使能:").append(CompEnUtil.getCompEnVal(workJson6.getInteger("compensate"))).append("<br/>");
            text.append("持续时长:").append(workJson6.get("lastTime")).append("<br/>");
            // 次字段可能是电压阈值（V）or工作流量阈值
            text.append("工作阈值:").append(workJson6.get("threshOne")).append("<br/>");
            text.append("待机报警阈值:").append(workJson6.get("thresholdStandbyAlarm")).append("<br/>");
        }
    }

    private void getLoadParam(StringBuilder text, JSONObject obj) {
        if (obj.get("value") != null) {
            JSONObject workParam = JSONObject.parseObject(obj.get("value").toString());
            text.append("传感器ID:").append(workParam.get("sensorID")).append("<br/>");
            JSONObject workJson6 = workParam.getJSONObject("loadParam");
            text.append("补偿使能:").append(workJson6.get("compensate")).append("<br/>");
            text.append("滤波系数:").append(workJson6.get("filter_factor")).append("<br/>");
            text.append("载重测量方法:").append(workJson6.get("scheme")).append("<br/>");
            text.append("传感器重量单位:").append(workJson6.get("unit")).append("<br/>");
            text.append("空载阈值:").append(workJson6.get("nullLoadThreshold")).append("<br/>");
            text.append("空载阈值偏差:").append(workJson6.get("nullLoadThresholdOffset")).append("<br/>");
            text.append("轻载阈值:").append(workJson6.get("lightLoadThreshold")).append("<br/>");
            text.append("轻载阈值偏差:").append(workJson6.get("lightLoadThresholdOffset")).append("<br/>");
            text.append("满载阈值:").append(workJson6.get("fullLoadThreshold")).append("<br/>");
            text.append("满载阈值偏差:").append(workJson6.get("fullLoadThresholdOffset")).append("<br/>");
            text.append("超载阈值:").append(workJson6.get("overLoadThreshold")).append("<br/>");
            text.append("超载阈值偏差:").append(workJson6.get("overLoadThresholdOffset")).append("<br/>");
        }
    }

    /**
     * 格式化油箱数据
     */
    private void getF3F4ReportStr(StringBuilder text, String value) {
        JSONObject oilQuantity = JSONObject.parseObject(value);
        JSONObject cgjson = oilQuantity.getJSONObject("oilQuantity");
        text.append("外设ID:")
                .append(oilQuantity.get("sensorID"))
                .append("<br/>")
                .append("补偿使能:")
                .append(CompEnUtil.getCompEnVal(cgjson.getInteger("compensationCanMake")))
                .append("<br/>")
                .append("滤波系数:")
                .append(FilterFactorUtil.getFilterFactorVal(cgjson.getInteger("filteringFactor")))
                .append("<br/>")
                .append("自动上传时间:")
                .append(UploadTimeUtil.getUploadTimeVal(cgjson.getInteger("automaticUploadTime")))
                .append("<br/>")
                .append("输出修正系数 K:")
                .append(cgjson.get("outputCorrectionCoefficientK"))
                .append("<br/>")
                .append("输出修正常数 B:")
                .append(cgjson.get("outputCorrectionCoefficientB"))
                .append("<br/>")
                .append("传感器长度:")
                .append(cgjson.get("sensorLength"))
                .append("<br/>");
        if (cgjson.getInteger("fuelOil") == 1) {
            text.append("燃料选择:柴油<br/>");
        }
        if (cgjson.getInteger("fuelOil") == 2) {
            text.append("燃料选择:汽油<br/>");
        }
        if (cgjson.getInteger("fuelOil") == 3) {
            text.append("燃料选择:LNG<br/>");
        }
        if (cgjson.getInteger("fuelOil") == 4) {
            text.append("燃料选择:CNG<br/>");
        }
        text.append("油箱形状:").append(ShapeUtil.getShapeVal(cgjson.getInteger("shape"))).append("<br/>");
        text.append("油箱尺寸-长:").append(cgjson.get("boxLength")).append("<br/>");
        text.append("油箱尺寸-宽:").append(cgjson.get("width")).append("<br/>");
        text.append("油箱尺寸-高:").append(cgjson.get("height")).append("<br/>");
        text.append("加油时间阈值:").append(cgjson.get("addOilTimeThreshold")).append("<br/>");
        text.append("加油量阈值:").append(cgjson.get("addOilAmountThreshol")).append("<br/>");
        text.append("漏油时间阈值:").append(cgjson.get("seepOilTimeThreshold")).append("<br/>");
        text.append("漏油量阈值:").append(cgjson.get("seepOilAmountThreshol")).append("<br/>");
    }

    /**
     * 格式化油箱数据
     */
    private void getTyreReportStr(StringBuilder text, String value) {
        JSONObject oilQuantity = JSONObject.parseObject(value);
        JSONObject cgjson = oilQuantity.getJSONObject("tyreParam");
        text.append("外设ID:").append(oilQuantity.get("sensorID"))
                .append("<br/>")
                .append("补偿使能:")
                .append(CompEnUtil.getCompEnVal(cgjson.getInteger("compensatingEnable")))
                .append("<br/>")
                .append("滤波系数:")
                .append(FilterFactorUtil.getFilterFactorVal(cgjson.getInteger("smoothing")))
                .append("<br/>")
                .append("自动上传时间:")
                .append(UploadTimeUtil.getUploadTimeVal(cgjson.getInteger("automaticUploadTime")))
                .append("<br/>")
                .append("输出修正系数 K:")
                .append(cgjson.get("compensationFactorK"))
                .append("<br/>")
                .append("输出修正常数 B:")
                .append(cgjson.get("compensationFactorB"))
                .append("<br/>")
                .append("正常胎压值:")
                .append(cgjson.get("pressure"))
                .append("<br/>")
                .append("胎压不平衡门限:")
                .append(ShapeUtil.getShapeVal(cgjson.getInteger("pressureThreshold")))
                .append("<br/>")
                .append("慢漏气门限:")
                .append(cgjson.get("slowLeakThreshold"))
                .append("<br/>")
                .append("低压阈值:")
                .append(cgjson.get("lowPressure"))
                .append("<br/>")
                .append("高压阈值:")
                .append(cgjson.get("heighPressure"))
                .append("<br/>")
                .append("高温阈值:")
                .append(cgjson.get("highTemperature"))
                .append("<br/>")
                .append("传感器电量报警阈值:")
                .append(cgjson.get("electricityThreshold"))
                .append("<br/>");
    }

    /**
     * 保存数据透传上报日志
     */
    @Override
    public void saveDataPermeanceLog(Message message) throws Exception {
        JSONObject json = JSON.parseObject(message.getData().toString());
        JSONObject body = json.getJSONObject("msgBody");
        JSONObject head = json.getJSONObject("msgHead");
        Integer type = body.getInteger("type");
        Integer id = body.getInteger("id");
        MsgDesc desc = message.getDesc();
        String monitorId = desc.getMonitorId();
        String monitorName = desc.getMonitorName();
        String result = "";
        Integer ackMSN;
        if (id != null && id == 0xE5) {
            //用于过滤多客户端存储多条数据问题问题;
            if (LOCALHOST_IP.equals(serverIp)) {
                ackMSN = body.getInteger("ackMSN");
                String deviceId = message.getDesc().getDeviceId();
                SubscibeInfo info =
                    SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(ackMSN == null ? 0 : ackMSN, deviceId);
                if (info == null) {
                    JSONObject sensorF1 = body.getJSONObject("sensorF1");
                    if (sensorF1 != null && !sensorF1.isEmpty()) {
                        this.saveFaultCode(sensorF1, monitorId, body);
                    }
                } else {
                    //取消订阅
                    SubscibeInfoCache.getInstance().delTable(info);
                }
            } else {
                return;
            }
        }
        if (body.containsKey("data")) {
            byte[] dataJson = body.getBytes("data");
            result = new String(dataJson, Charset.forName("GBK"));
        } else if (body.containsKey("ackMSN")) {
            result = body.toJSONString();
            //ackMSN = body.getInteger("ackMSN");
        }
        String content = "";
        if (type == 0x00) {
            content += "透传消息类型:GNSS 模块详细定位数据 <br/>";
        } else if (type == 0xF3) {
            content += "透传消息类型:F3入口协议 <br/>";
            try {
                JSONObject restjson = JSONObject.parseObject(result);
                if (restjson.containsKey("ackMSN")) {
                    result = "消息编号:" + restjson.getString("ackMSN");
                }
                if (restjson.containsKey("result")) {
                    result += "状态:" + ("0".equals(restjson.getString("result")) ? "参数生效" :
                        ("1".equals(restjson.getString("result")) ? "参数未生效" : restjson.getString("result")));
                }
            } catch (Exception ex) {
                logger.error(ex + "保存数据透传上报日志异常");
            }
        } else if (type == 0x0B) {
            content += "透传消息类型:道路运输证IC卡信息 <br/>";
        } else if (type == 0x41) {
            content += "透传消息类型:串口1 透传消息 <br/>";
        } else if (type == 0x42) {
            content += "透传消息类型:串口2 透传消息 <br/>";
        } else if (type == 0xF7) {
            sendMessageToWeb(message, monitorId, head.getInteger("msgSN"),
                "/topic/perF7" + Integer.toHexString(Integer.parseInt(body.getString("id"))) + "_" + monitorId
                    + "_Info");
            content += "透传消息类型:查询外设状态信息 <br/>";
            JSONObject sensorF7 = JSONObject.parseObject(body.getString("sensorF7"));
            result += "工作状态:" + sensorF7.get("workStatus") + "<br/>";
            result += "报警状态:" + sensorF7.get("alarmStatus") + "<br/>";
        } else if (type == 0xF8) {
            //避免和F3扩展协议 远程升级冲突（运程升级不会带id过来） 查询外设传感器基本信息（一定会带id过来）故做非空判断
            if (body.getString("id") != null) {
                sendMessageToWeb(message, monitorId, head.getInteger("msgSN"),
                    "/topic/perF8" + Integer.toHexString(Integer.parseInt(body.getString("id"))) + "_" + monitorId
                        + "_Info");
                content += "透传消息类型:查询外设传感器的基本信息 <br/>";
                JSONObject sensorF8 = JSONObject.parseObject(body.getString("sensorF8"));
                result += "公司名称:" + sensorF8.get("companyName") + "<br/>";
                result += "产品代码:" + sensorF8.get("productCode") + "<br/>";
                result += "硬件版本号:" + sensorF8.get("hardwareVersionsCode") + "<br/>";
                result += "软件版本号:" + sensorF8.get("softwareVersionsCode") + "<br/>";
                result += "外设 ID:" + sensorF8.get("sensorID") + "<br/>";
                result += "客户代码:" + sensorF8.get("clientCode") + "<br/>";
            }
        } else if (type >= 0xF0 && type <= 0xFF) {
            content += "透传消息类型:用户自定义透传消息 <br/>";
        } else {
            content += "透传消息类型:" + type + " <br/>";
        }
        content += "透传消息内容：<br/>" + result + "<br/>";
        // 判断是否为 油箱车辆设置0900状态推送
        if (updateOilSettingStatus(monitorId, monitorName, message)) {
            return;
        }
        LogSearchForm form = new LogSearchForm();
        String[] vehicle = logSearchService.findCarMsg(message.getDesc().getMonitorId());
        form.setBrand(vehicle[0]);
        form.setPlateColor(Integer.valueOf(vehicle[1]));
        if (!"".equals(message.getDesc().getMonitorId())) {
            VehicleDTO vehicleDTO = vehicleService.getById(message.getDesc().getMonitorId());
            String deviceNumber = vehicleDTO.getDeviceNumber();
            if (Objects.nonNull(deviceNumber)) {
                String deviceGroupId = deviceService.findGroupIdByNumber(deviceNumber);
                if (!"".equals(deviceGroupId)) {
                    form.setGroupId(deviceGroupId);
                }
            }
        }
        form.setEventDate(new Date());
        // 获取到当前用户的用户名
        form.setLogSource("1");
        form.setModule("");
        form.setMonitoringOperation("监控对象（" + message.getDesc().getMonitorName() + "）  数据透传上报");
        form.setMessage(content);
        logSearchService.addLogBean(form);

        simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_FENCE_STATUS, json);
        if (id != null && id == 0xE5) {
            webSocketMessageDispatchCenter
                .pushInfoImpl(InstanceMessageController.userName, WebSocketMessageDispatchCenter.WEBSOCKET_OBD_FAULT,
                    json.toString());
        }
    }

    @Override
    public void handleRemoteUpgradePermeanceData(Message message) {
        /* 1.获取远程升级task实体，若不为空则进行下一步处理 */
        String deviceId = message.getDesc().getDeviceId();
        RemoteUpgradeTask task = RemoteUpgradeInstance.getInstance().getRemoteUpgradeTask(deviceId);
        if (Objects.nonNull(task)) {
            /* 2.获取基础必要信息 */
            JSONObject json = JSON.parseObject(message.getData().toString());
            JSONObject body = json.getJSONObject("msgBody");
            JSONObject sensorF8 = body.getJSONObject("sensorF8");
            /* 3.判断外设传感器升级数据是否为null，不为null则进行下一步处理 */
            if (sensorF8 != null) {
                /* 4.建立远程升级参数处理实体 */
                HandleRemoteUpgradeParam param = new HandleRemoteUpgradeParam();
                param.setAnswerType(1);// 应答状态
                param.setStatus(sensorF8.getIntValue("status"));// 标识状态
                param.setSerialNumber(sensorF8.getIntValue("ackMSN"));// 应答流水号
                param.setAllPage(sensorF8.getIntValue("allPage"));
                param.setDownPage(sensorF8.getIntValue("downPage"));
                /* 5.处理远程升级数据 */
                handleRemoteUpgradeDataEx(task, param);
            }
        }
    }

    private void saveFaultCode(JSONObject sensorF1, String monitorId, JSONObject body) {
        JSONArray faultCodeJSONArr = sensorF1.getJSONArray("faultCodeList");
        Integer sum = sensorF1.getInteger("sum");
        if (Objects.nonNull(sum) && sum > 0) {
            List<FaultCodeForm> faultCodeList = new ArrayList<>();
            StringBuilder codes = new StringBuilder();
            StringBuilder descriptions = new StringBuilder();
            for (int i = 0; i < faultCodeJSONArr.size(); i++) {
                JSONObject faultCodeObj = faultCodeJSONArr.getJSONObject(i);
                String faultCode = faultCodeObj.getString("faultCode");
                FaultCodeForm faultCodeForm = new FaultCodeForm();
                faultCodeForm.setFaultCode(faultCode);
                faultCodeForm.setMonitorId(monitorId);
                String faultCodeDescription = newDictionaryDao.getValueByCodeAndType(faultCode, "FAULT_CODE");
                faultCodeForm.setDescription(faultCodeDescription);
                faultCodeForm.setCreateDataUsername(LOCALHOST_IP);
                faultCodeList.add(faultCodeForm);
                codes.append(faultCode);
                descriptions.append(faultCodeDescription != null ? faultCodeDescription : "暂无描述");
                if (i != faultCodeJSONArr.size() - 1) {
                    codes.append(",");
                    descriptions.append(",");
                }
            }
            body.put("faultCodes", codes.toString());
            body.put("faultDescriptions", descriptions.toString());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            body.put("uploadTime", formatter.format(new Date()));
            obdVehicleTypeDao.saveFaultCodes(faultCodeList);
        }
    }

    /**
     * 油箱车辆设置0900状态推送
     */
    private Boolean updateOilSettingStatus(String vid, String veNo, Message message) {
        JSONObject json = JSON.parseObject(message.getData().toString());
        JSONObject body = json.getJSONObject("msgBody");
        JSONObject head = json.getJSONObject("msgHead");
        String result = body.getString("result");
        if (body.containsKey("data")) {
            byte[] dataJson = body.getBytes("data");
            String dataResult = getHexBytes(dataJson);
            body.put("data_result", dataResult);
            json.put("msgBody", body);
            message.setData(json);
            result = "0";
        }
        String ackMSN = body.getString("ackMSN");
        String deviceId = message.getDesc().getDeviceId();
        // 油箱车辆下发-更新数据库
        SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(ackMSN, deviceId);
        if (info == null) {
            return false;
        }
        String userS = info.getUserName();
        if (result != null && !result.equals("")) {
            // 修改油箱车辆下发-状态
            parameterService.updateStatusByMsgSN(Integer.valueOf(ackMSN), vid, Integer.valueOf(result));
            // 油箱车辆下发
            result =
                "处理编号:[" + ackMSN + "]状态:" + ("0".equals(result) ? "参数生效" : ("1".equals(result) ? "参数未生效" : result));
            LogSearchForm form = new LogSearchForm();
            String[] vehicle = logSearchService.findCarMsg(vid);
            form.setBrand(vehicle[0]);
            String plateColor = vehicle[1];
            if (StringUtils.isNotEmpty(plateColor)) {
                form.setPlateColor(Integer.valueOf(plateColor));
            }
            if (!"".equals(vid)) {
                BindDTO vehicleMap = VehicleUtil.getBindInfoByRedis(vid);
                String deviceGroupId = deviceService.findGroupIdByNumber(vehicleMap.getDeviceNumber());
                if (!"".equals(deviceGroupId)) {
                    form.setGroupId(deviceGroupId);
                }
            }
            String type = body.getString("type"); // 透传消息类型
            String sensorId = body.getString("id"); // 传感器类型(油位,油耗...)
            Map<String, String> messageMap = getSensorType(sensorId);
            String infoMessage = messageMap.get("message") + getMessageType(type);
            form.setEventDate(new Date());
            // 获取到当前用户的用户名
            form.setLogSource("1");
            form.setModule("OILSETTING");
            form.setMonitoringOperation("监控对象（" + veNo + "）" + infoMessage);
            String content = "透传消息类型:" + messageMap.get("contentMessage") + "状态 <br/>";
            content += "透传消息内容：" + result + "";
            form.setMessage(content);
            logSearchService.addLogBean(form);
        }

        try {
            String moduleKey =
                paramSendingCache.getKey(info.getUserName(), Integer.parseInt(ackMSN), head.getString("mobile"));
            sendToModuleUser(message, moduleKey, info.getUserName(), json, true);
        } catch (Exception e) {
            logger.info("通用应答推送对应模块消息异常", e);
        }
        SubscibeInfoCache.getInstance().delTable0900(info);
        simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_REFRESH_LIST, message);
        simpMessagingTemplateUtil.sendStatusMsg(userS, ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_USER, message);
        return true;
    }

    /**
     * 透传消息类型
     */
    private String getMessageType(String typeNumber) {
        String message;
        // 其他透传
        if ("243".equals(typeNumber)) { // 0xF3
            message = "参数设置状态上报";
        } else {
            message = "透传消息";
        }
        return message;
    }

    /**
     * 区分0900上报的传感器类型
     */
    private Map<String, String> getSensorType(String sensorId) {
        String message = "";
        String contentMessage = "";
        switch (sensorId) {
            case "8":
                message = "基站定位";
                contentMessage = "基站定位设置";
                break;
            case "9":
                message = "wifi定位";
                contentMessage = "wifi定位设置";
                break;
            case "26":
                message = "终端手机号";
                contentMessage = "SIM卡设置";
                break;
            case "33":
                message = "温度传感器1";
                contentMessage = "温度监测设置";
                break;
            case "34":
                message = "温度传感器2";
                contentMessage = "温度监测设置";
                break;
            case "35":
                message = "温度传感器3";
                contentMessage = "温度监测设置";
                break;
            case "36":
                message = "温度传感器4";
                contentMessage = "温度监测设置";
                break;
            case "37":
                message = "温度传感器5";
                contentMessage = "温度监测设置";
                break;
            case "38":
                message = "湿度传感器1";
                contentMessage = "湿度监测设置";
                break;
            case "39":
                message = "湿度传感器2";
                contentMessage = "湿度监测设置";
                break;
            case "40":
                message = "湿度传感器3";
                contentMessage = "湿度监测设置";
                break;
            case "41":
                message = "湿度传感器4";
                contentMessage = "湿度监测设置";
                break;
            case "65":
                message = "油位传感器";
                contentMessage = "油量车辆设置";
                break;
            case "66":
                message = "双油位传感器";
                contentMessage = "油量车辆设置";
                break;
            case "69":
                message = "油耗传感器";
                contentMessage = "油耗车辆设置";
                break;
            case "70":
                message = "双油耗传感器";
                contentMessage = "油耗车辆设置";
                break;
            case "81":
                message = "正反转传感器";
                contentMessage = "正反转车辆设置";
                break;
            case "83":
                message = "里程传感器";
                contentMessage = "里程监测设置";
                break;
            case "90":
                message = "震动传感器1";
                contentMessage = "工时车辆设置";
                break;
            case "91":
                message = "震动传感器2";
                contentMessage = "工时车辆设置";
                break;
            case "92":
                message = "震动传感器3";
                contentMessage = "工时车辆设置";
                break;
            case "93":
                message = "震动传感器4";
                contentMessage = "工时车辆设置";
                break;
            case "94":
                message = "震动传感器5";
                contentMessage = "工时车辆设置";
                break;
            case "95":
                message = "震动传感器6";
                contentMessage = "工时车辆设置";
                break;
            case "100":
                message = "ADAS";
                contentMessage = "风控定义设置";
                break;
            case "101":
                message = "异常驾驶行为分析";
                contentMessage = "异常驾驶行为分析设置";
                break;
            case "144":
                message = "车机I/O直接控制";
                contentMessage = "外设轮询";
                break;
            default:
                break;
        }
        Map<String, String> map = new HashMap<>();
        map.put("message", message);
        map.put("contentMessage", contentMessage);
        return map;
    }

    private String getHexBytes(byte[] bytes) {
        StringBuilder str = new StringBuilder();
        int m;
        String sub;
        for (byte byteDate : bytes) {
            m = byteDate & 0xFF;
            sub = Integer.toHexString(m);
            if (sub.length() < 2) {
                str.append(0).append(sub).append(" ");
            } else {
                str.append(sub).append(" ");
            }
        }
        return str.toString().toUpperCase();
    }

    @Override
    public void currencyAnswer(Message message) {
        JSONObject json = JSON.parseObject(message.getData().toString());
        JSONObject body = json.getJSONObject("msgBody");
        int msgSNACK = (int) body.get("msgSNACK");
        int status = body.getIntValue("result");
        int answerIDACK = body.getIntValue("answerIDACK"); // 对应应答消息ID
        String deviceId = message.getDesc().getDeviceId();
        String monitorId = message.getDesc().getMonitorId();
        //是否进行通用应答全局推送逻辑
        boolean sendFlag = true;
        boolean isDelCache = true;
        // 根据设备id查询车辆id
        if (msgSNACK != 0 && StringUtils.isNotBlank(deviceId)) {
            if (answerIDACK == ConstantUtil.ISSUE_DEVICE_DRIVER_DISCERN
                || answerIDACK == ConstantUtil.ISSUE_DEVICE_DRIVER_SYNCHRONIZE) {
                driverDiscernManageService.sendIssueAckHandle(message, msgSNACK);
            }
            // 更新数据库
            SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSNACK, deviceId);

            if (info != null) {
                // 终端唤醒
                String sessionId = WebSubscribeManager.getInstance().getDeviceWakeUpAckSessionId(monitorId, msgSNACK);
                if (StringUtils.isNoneBlank(sessionId) && answerIDACK == ConstantUtil.T808_SEND_TXT) {
                    parameterService.updateStatusByMsgSN(msgSNACK, monitorId, status);
                    // 推送状态
                    simpMessagingTemplateUtil
                        .sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_DEVICE_WAKE_UP, status);
                    WebSubscribeManager.getInstance().cancleSubDeviceWakeUpAck(monitorId, msgSNACK);
                    SubscibeInfoCache.getInstance().delTable(info);
                    return;
                }

                if (info.getType() != null && info.getType() == 1) {
                    //需要判断是订阅0900的
                    isDelCache = false;
                    // F3-状态-需要已
                    if (status == 0) {
                        parameterService.updateStatusByMsgSNVid(msgSNACK, monitorId, 7);
                    } else {
                        parameterService.updateStatusByMsgSN(msgSNACK, monitorId, status);

                    }
                    SubscibeInfoCache.getInstance().delTable(info);
                    // 推送
                    if (info.getUserName() != null && !"".equals(info.getUserName())) {
                        simpMessagingTemplateUtil
                            .sendStatusMsg(info.getUserName(), ConstantUtil.WEB_SOCKET_T808_CURRENCY_RESPONSE, message);
                    } else {
                        simpMessagingTemplateUtil
                            .sendStatusMsg(ConstantUtil.WEB_SOCKET_T808_CURRENCY_RESPONSE, message);
                    }
                } else {
                    parameterService.updateStatusByMsgSN(msgSNACK, monitorId, status);
                    // 若是解除围栏下发(圆，矩形，多边形，线)
                    if (answerIDACK == 34305 || answerIDACK == 34307 || answerIDACK == 34309 || answerIDACK == 34311) {
                        if (status == 1) { // 删除失败
                            parameterService.updateFenceConfig(monitorId, msgSNACK);
                        }
                    }
                    // 推送
                    if (StringUtils.isNotEmpty(info.getUserName())) {
                        simpMessagingTemplateUtil
                            .sendStatusMsg(info.getUserName(), ConstantUtil.WEBSOCKET_FENCE_STATUS, message);
                    }
                    //33536代表8300指令应答
                    if (answerIDACK == 33536) {
                        //失败原因
                        String failureReason;
                        //处理下发信息的统计存储
                        switch (status) {
                            case 1:
                                failureReason = "指令未生效";
                                break;
                            case 2:
                                failureReason = "通讯错误";
                                break;
                            case 3:
                                failureReason = "设备不支持";
                                break;
                            case 8:
                                failureReason = "参数下发失败";
                                break;
                            default:
                                failureReason = "";
                        }
                        //修改缓存数据，
                        sendMsgCache.changeMsgCache(monitorId, msgSNACK, status, failureReason);

                    }
                }
                if (status == 0) {
                    // 传感器轮询下发成功
                    List<SensorPolling> spList = sensorPollingService.findByVehicleId(monitorId);
                    RedisKey sensorMessageRedisKey = HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId);
                    boolean flogKey = RedisHelper.isContainsKey(sensorMessageRedisKey);
                    if (flogKey) {
                        RedisHelper.delete(sensorMessageRedisKey);
                    }
                    for (SensorPolling sp : spList) {
                        if ("0x53".equals(sp.getIdentId())) {
                            // 当里面包含里程传感器的时候,将状态存入redis中;
                            RedisHelper.setString(sensorMessageRedisKey, "true", 2);
                        }
                    }
                }
                /* 远程升级终端应答消息处理 */
                RemoteUpgradeTask task = RemoteUpgradeInstance.getInstance().getRemoteUpgradeTask(deviceId);
                if (Objects.nonNull(task)) {
                    /* 建立远程升级参数处理实体 */
                    HandleRemoteUpgradeParam param = new HandleRemoteUpgradeParam();
                    param.setAnswerType(0);// 应答状态
                    param.setStatus(status);// 标识状态
                    param.setSerialNumber(msgSNACK);// 应答流水号
                    /* 处理远程升级数据 */
                    handleRemoteUpgradeDataEx(task, param);
                }
                if (answerIDACK != ConstantUtil.T808_REQ_MEDIA_STORAGE_FTP_9208
                    && answerIDACK != ConstantUtil.T809_DOWN_EXG_MSG_RETURN_END) {
                    SubscibeInfoCache.getInstance().delTable(info);
                }
                String simCard = json.getJSONObject("msgHead").getString("mobile");

                try {
                    String moduleKey = paramSendingCache.getKey(info.getUserName(), msgSNACK, simCard);
                    sendFlag = sendToModuleUser(message, moduleKey, info.getUserName(), json, isDelCache);
                } catch (Exception e) {
                    logger.info("通用应答推送对应模块消息异常", e);
                }

                /*
                 * 视频巡检
                 */
                String key = monitorId + "_" + msgSNACK;
                if (AdasDirectiveStatusOutTimeUtil.isContainVideoInspection(key)) {
                    VideoInspectionData videoInspectionData = AdasDirectiveStatusOutTimeUtil.getVideoInspection(key);
                    videoInspectionData.setStatus(0);
                    AdasDirectiveStatusOutTimeUtil.removeVideoInspection(key);
                    adasDirectiveStatusOutTimeUtil.videoInspectionHandler(null, videoInspectionData);
                }
            }
            if (sendFlag) {
                simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_REFRESH_LIST, message);
            }
        }
    }

    /**
     * 发送消息到对应的模块
     */
    public boolean sendToModuleUser(Message message, String moduleKey, String userName, JSONObject json,
        boolean isDelCache) {
        SendTarget sendTarget = paramSendingCache.get(moduleKey);
        if (sendTarget != null) {
            json.getJSONObject("msgBody").put("subModule", sendTarget.getSubModule());
            message.setData(json);
            simpMessagingTemplateUtil.sendStatusMsg(userName, "/topic/" + sendTarget.getTargetUrl(), message);
            if (isDelCache) {
                paramSendingCache.remove(moduleKey);
            }
            //改变全局推送flag为false
            return false;
        }
        return true;

    }

    /**
     * 处理远程升级应答数据ex
     */
    private void handleRemoteUpgradeDataEx(RemoteUpgradeTask task, HandleRemoteUpgradeParam param) {
        /* 1.判断消息流水号是否对应得上（对应得上则是远程升级应答，否则则不予处理） */
        try {
            handleRemoteUpgradeData(task, param);
        } catch (Exception e) {
            logger.error("处理传感器远程升级消息异常", e);
        }
    }

    /**
     * 处理远程升级应答数据
     */
    private void handleRemoteUpgradeData(RemoteUpgradeTask task, HandleRemoteUpgradeParam param) {
        /* 1.判断消息流水号是否对应得上（对应得上则是远程升级应答，否则则不予处理） */
        if (task.getSerialNumber() != null && Objects.equals(task.getSerialNumber(), param.getSerialNumber())) {
            Integer currentStatus = param.getAnswerType();// 当前升级阶段
            /* 2.判断应答是成功还是失败 */
            Integer answerType = param.getAnswerType();// 应答类型
            Integer status = param.getStatus();// 标识状态
            int stageStatus = task.getStageStatus();// 第一阶段状态
            if (status == null) {
                logger.info("监控对象：" + task.getPlateNumber() + " 远程升级应答数据异常，无标识状态，应答类型为：" + answerType);
                return;
            }

            if ((answerType == 0 && status == 0) || (answerType == 1 && status != 2)) { //成功
                /* 3.判断是否处于第一和第二阶段的中间阶段，是则不予任何处理 */
                if (currentStatus == 0 && stageStatus != 1) {
                    if (stageStatus == 0) {
                        task.setEraseTerminal(true);
                    } else if (stageStatus == 2) {
                        task.setTotalUpgradeDataCheckInstruction(true);
                    } else if (stageStatus == 3) {
                        task.setStartUpgradeCommand(true);
                    }
                    // 通知释放当前循环信号量
                    task.releaseSemaphore();
                    return;
                }
                /* 4.判断当前升级阶段，获取对应进度值 */
                Integer totalPackageSize; //当前状态总包数
                Integer successPackageSize; //当前状态成功包数
                if (currentStatus == 0) { //平台->终端
                    // 该阶段总包数为升级包解析包数初值，成功包数为递增计算值
                    totalPackageSize = task.getTotalPackageSize();
                    successPackageSize = task.getSuccessPackageSize() + 1;
                    task.setSuccessPackageSize(successPackageSize);
                    // 更新状态（web端显示用）
                    task.setPlatformToF3Status(RemoteUpgradeUtil.PLATFORM_STATUS_UNDERWAY);
                } else { //终端->外设传感器
                    // 该阶段总包数、成功包数值都由终端上传数据为准，并且更新task中对应参数
                    totalPackageSize = param.getAllPage();
                    successPackageSize = param.getDownPage();
                    task.setF3ToPeripheralTotalPackageSize(totalPackageSize);
                    task.setF3ToPeripheralSuccessPackageSize(successPackageSize);
                    // 更新状态（web端显示用）
                    task.setSensorUpgradeStatus(RemoteUpgradeUtil.F3_STATUS_UNDERWAY);
                    task.setF3ToPeripheralStatus(RemoteUpgradeUtil.F3_STATUS_UNDERWAY);
                }
                /* 5.判断是否阶段性完成更新 */
                boolean allFinish = false;
                // 如果处于第一阶段（平台->终端）且总包数=成功包数，则认为升级包发送完成，更新状态
                if (currentStatus == 0 && totalPackageSize.intValue() == successPackageSize.intValue()) {
                    task.setPlatformToF3Status(RemoteUpgradeUtil.PLATFORM_STATUS_FINISH);
                } else if (currentStatus == 1) {
                    // 更新外设升级是否成功标识，用于停止时间轮
                    if (status == 0) {
                        // 如果处于第二阶段（终端->外设传感器）且总包数=成功包数，则认为升级完成，更新状态
                        task.setF3ToPeripheralStatus(RemoteUpgradeUtil.F3_STATUS_FINISH);
                        allFinish = true;
                        task.setPeripheralUpgradeSuccess(true);
                    } else if (status == 1) {
                        // 如果在规定的时间内收到外设的应答, 则结束此次超时判断, 并开启新一轮的超时判断
                        task.cancelledAndCreateTimeout();
                    }
                }
                /* 6.向web端发送当前进度状态并通知下发端进行对应处理 */
                task.onMessage(
                    new RemoteUpgradeToWeb(currentStatus, totalPackageSize, successPackageSize, task.getMonitorId()),
                    true, task);
                /* 7.判断该设备是否已经完成了升级 */
                if (allFinish) {
                    // 通知释放当前循环信号量
                    task.releaseSemaphore();
                    // 如果单个设备完成全部升级, 则释放"信号",运行其他的"升级"
                    task.releaseSuperSemaphore(task.getDeviceId());
                    // 更新下发表数据状态
                    sendHelper.updateParameterStatus(task.getParamId(), task.getSerialNumber(), 0, task.getMonitorId(),
                        task.getParamType(), task.getParameterName());
                    // 当前终端升级完成, 则"门闩"的数目递减
                    // task.releaseCountDownLatch();
                    // 升级完成后推送一次数据给前端, 用于判断是否可以开始新的升级
                    task.onMessage(new RemoteUpgradeToWeb(currentStatus, task.getF3ToPeripheralTotalPackageSize(),
                        task.getF3ToPeripheralSuccessPackageSize(), task.getMonitorId()), true, task);
                    RemoteUpgradeInstance.getInstance().removeUpgradeTask(task.getDeviceId());
                    task.setSensorUpgradeStatus(RemoteUpgradeUtil.F3_STATUS_FINISH);
                    task.addOrUpdateSensorUpgrade();
                    this.recordUpgradeLog(task, true);
                } else {
                    if (currentStatus == 0) {
                        // 通知释放当前循环信号量
                        task.releaseSemaphore();
                    }
                }
            } else { //失败
                if (currentStatus == 0) {
                    // 处于第一阶段则通知此次包失败
                    if (stageStatus == 0 || stageStatus == 2) {
                        // 处于第一和第二阶段的中间阶段
                        // 通知此次应答失败
                        task.addFaultTimes();
                    } else {
                        // 通知此次包下发失败（3次包下发失败则此次升级失败）
                        task.timeOutOrSendFault();
                    }
                } else {
                    // 处于第二阶段失败则直接失败
                    // 设备升级失败,释放"信号",运行其他的"升级"
                    task.releaseSuperSemaphore(task.getDeviceId());
                    // 更新下发表数据状态
                    sendHelper.updateParameterStatus(task.getParamId(), task.getSerialNumber(), 9, task.getMonitorId(),
                        task.getParamType(), task.getParameterName());
                    // 更新状态（web端显示用）
                    task.setF3ToPeripheralStatus(RemoteUpgradeUtil.F3_STATUS_FAILED);
                    // 向web端发送当前进度状态并通知下发端进行对应处理
                    task.onMessage(new RemoteUpgradeToWeb(currentStatus, task.getF3ToPeripheralTotalPackageSize(),
                        task.getF3ToPeripheralSuccessPackageSize(), task.getMonitorId()), true, task);
                    // 更新外设升级是否成功标识，用于停止时间轮
                    task.setPeripheralUpgradeSuccess(true);
                    // 当前终端升级完成, 则"门闩"的数目递减
                    //task.releaseCountDownLatch();
                    RemoteUpgradeInstance.getInstance().removeUpgradeTask(task.getDeviceId());
                    task.setSensorUpgradeStatus(RemoteUpgradeUtil.F3_STATUS_FAILED);
                    task.addOrUpdateSensorUpgrade();
                }
                this.recordUpgradeLog(task, false);
            }
        }
    }

    private void recordUpgradeLog(RemoteUpgradeTask task, boolean succeed) {
        LogSearchForm form = new LogSearchForm();
        form.setEventDate(new Date());
        form.setUsername(task.getUserName());
        form.setBrand(task.getPlateNumber());
        form.setGroupId(task.getOrgId());
        form.setIpAddress(task.getIpAddress());
        form.setLogSource("1");
        form.setPlateColor(task.getPlateColor());
        final String content = task.getPeripheralName() + (succeed ? "升级成功" : "升级失败");
        form.setMessage(content);
        form.setMonitoringOperation(content);
        logSearchService.addLogBean(form);
    }

    @Override
    public void saveVideoParamAckLog(Message message) {
        // 取出数据转换为json对象
        JSONObject ackJson = JSON.parseObject(message.getData().toString());
        JSONObject param = ackJson.getJSONObject("msgBody");
        // 获取终端组织id
        String deviceGroupId = deviceService.findGroupIdByNumber(message.getDesc().getDeviceNumber());
        // 创建日志记录实体
        LogSearchForm form = new LogSearchForm();
        String[] vehicle = logSearchService.findCarMsg(message.getDesc().getMonitorId());
        form.setBrand(vehicle[0]);
        if (StringUtils.isNotBlank(vehicle[1])) {
            form.setPlateColor(Integer.valueOf(vehicle[1]));
        }
        // 日志内容组装
        form.setModule("REALTIMEVIDEO");
        String text =
            "输入音频编码方式: " + AudioVideoUtil.getAudioVideoCodeName((Integer) param.get("audioCodeType")) + "<br/>"
                + "输入音频声道数: " + param.get("audioChannelNumber") + "<br/>" + "音频采样率: " + AudioVideoUtil
                .getSamplingrateName((Integer) param.get("samplingrate")) + "<br/>" + "音频采样位数: " + AudioVideoUtil
                .getSamplingBitName((Integer) param.get("samplingBit")) + "<br/>" + "音频帧长度: " + param
                .get("audioFrameLength") + "<br/>" + "是否支持音频输出: " + AudioVideoUtil
                .isContinuousOutput((Integer) param.get("isContinuousOutput")) + "<br/>" + "视频编码方式: " + AudioVideoUtil
                .getAudioVideoCodeName((Integer) param.get("videoCodeType")) + "<br/>" + "终端支持的最大音频物理通道数量: " + param
                .get("maxAudioChannelNum") + "<br/>" + "终端支持的最大视频物理通道数量: " + param.get("maxVideoChannelNum") + "<br/>";
        form.setGroupId(deviceGroupId);
        form.setEventDate(new Date());
        form.setLogSource("1");
        StringBuilder mo = new StringBuilder();
        form.setMonitoringOperation(
            mo.append("监控对象（").append(message.getDesc().getMonitorName()).append("） 查询音视频属性应答").toString());
        form.setMessage(text);
        // 记录日志
        logSearchService.addLogBean(form);
        // 推送至web端
        simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
    }

    @Override
    public void saveRiderShipAckLog(Message message) throws Exception {
        // 取出数据转换为json对象
        JSONObject ackJson = JSON.parseObject(message.getData().toString());
        JSONObject param = ackJson.getJSONObject("msgBody");
        // 获取终端组织id
        String deviceGroupId = deviceService.findGroupIdByNumber(message.getDesc().getDeviceNumber());
        // 创建日志记录实体
        LogSearchForm form = new LogSearchForm();
        String[] vehicle = logSearchService.findCarMsg(message.getDesc().getMonitorId());
        form.setBrand(vehicle[0]);
        form.setPlateColor(Integer.valueOf(vehicle[1]));
        // 日志内容组装
        form.setModule("REALTIMEVIDEO");
        form.setGroupId(deviceGroupId);
        form.setEventDate(new Date());
        form.setLogSource("1");
        StringBuilder mo = new StringBuilder();
        form.setMonitoringOperation(
            mo.append("监控对象（").append(message.getDesc().getMonitorName()).append("） 上传乘客流量").toString());
        String starTime = manageData(param.get("startTime"));
        String endTime = manageData(param.get("endTime"));
        Integer onTheTrain = param.getInteger("onTheTrain");
        Integer getOffTheCar = param.getInteger("getOffTheCar");
        String text =
            "开始时间: " + starTime + "<br/>" + "结束时间: " + endTime + "<br/>" + "上车人数: " + onTheTrain + "<br/>" + "下车人数: "
                + getOffTheCar + "<br/>";
        form.setMessage(text);
        // 记录日志
        logSearchService.addLogBean(form);
        // 记录乘客报表
        ridershipService.add(message.getDesc().getMonitorId(), starTime, endTime, onTheTrain, getOffTheCar);
        // 推送值web端
        simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
    }

    /**
     * 将终端上传的日期格式(如180228202518)转换为标准的日期格式(如2018-02-18 20:25:18)
     */
    private String manageData(Object obj) {
        if (obj != null && !obj.toString().isEmpty()) {
            String date = "20" + obj.toString();
            if (date.length() == 14) {
                int index = 4;
                String year = date.substring(0, index);
                String mouth = date.substring(index, index + 2);
                index = index + 2;
                String day = date.substring(index, index + 2);
                index = index + 2;
                String hour = date.substring(index, index + 2);
                index = index + 2;
                String minute = date.substring(index, index + 2);
                index = index + 2;
                String second = date.substring(index, index + 2);
                return year + "-" + mouth + "-" + day + " " + hour + ":" + minute + ":" + second;
            }
        }
        return "";
    }
}
