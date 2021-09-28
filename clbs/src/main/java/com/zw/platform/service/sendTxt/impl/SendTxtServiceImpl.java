package com.zw.platform.service.sendTxt.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.controller.monitoring.CommandParametersController;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.param.CameraParam;
import com.zw.platform.domain.param.CommunicationParam;
import com.zw.platform.domain.param.DeviceParam;
import com.zw.platform.domain.param.EventSetParam;
import com.zw.platform.domain.param.EventSetParamSend;
import com.zw.platform.domain.param.F3SensorParamQuery;
import com.zw.platform.domain.param.GNSSParam;
import com.zw.platform.domain.param.InformationParam;
import com.zw.platform.domain.param.InformationParamSend;
import com.zw.platform.domain.param.PhoneBookParam;
import com.zw.platform.domain.param.PhoneBookParamSend;
import com.zw.platform.domain.param.PhoneParam;
import com.zw.platform.domain.param.PositionParam;
import com.zw.platform.domain.param.SerialPortParam;
import com.zw.platform.domain.param.SetParam;
import com.zw.platform.domain.param.SpeedLimitParam;
import com.zw.platform.domain.param.StationParam;
import com.zw.platform.domain.param.T808Param;
import com.zw.platform.domain.sendTxt.AlarmAck;
import com.zw.platform.domain.sendTxt.DeviceCommand;
import com.zw.platform.domain.sendTxt.F3CommunicationParam;
import com.zw.platform.domain.sendTxt.InformationService;
import com.zw.platform.domain.sendTxt.OBDParam;
import com.zw.platform.domain.sendTxt.OilElectricControl;
import com.zw.platform.domain.sendTxt.OriginalOrder;
import com.zw.platform.domain.sendTxt.RecordCollection;
import com.zw.platform.domain.sendTxt.RecordSend;
import com.zw.platform.domain.sendTxt.SendQuestion;
import com.zw.platform.domain.sendTxt.SendTxt;
import com.zw.platform.domain.sendTxt.SensorParam;
import com.zw.platform.domain.sendTxt.SerialPort;
import com.zw.platform.domain.sendTxt.SerialPortSettingItem;
import com.zw.platform.domain.sendTxt.SetStreamObd;
import com.zw.platform.domain.sendTxt.VehicleCommand;
import com.zw.platform.domain.systems.DeviceUpgrade;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.DirectiveStatusEnum;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.entity.device.DeviceWakeUpEntity;
import com.zw.ws.entity.t808.device.T8080x8105;
import com.zw.ws.entity.t808.location.T808_0x8201;
import com.zw.ws.entity.t808.oil.T808_0x8900;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import com.zw.ws.entity.t808.simcard.T808Msg8106;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by LiaoYuecai on 2017/3/31.
 */
@Service
public class SendTxtServiceImpl implements SendTxtService {
    private final Logger logger = LogManager.getLogger(CommandParametersController.class);

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private FastDFSClient fastDFSClient;

    private static final String DEVICE_TYPE_VALUE_808_2019 = "11";

    private static final String DEVICE_TYPE_VALUE_808_2013 = "1";

    @Autowired
    private ParamSendingCache paramSendingCache;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    @Override
    public void deviceLocationQuery(String deviceId, String mobile, Integer msgSN, BindDTO vehicleInfo) {
        T808_0x8201 t8080x8201 = new T808_0x8201();
        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T808_QUERY_LOCATION_COMMAND, msgSN, t8080x8201, vehicleInfo);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_LOCATION_COMMAND, deviceId);
    }

    @Override
    public void sendTxt(String deviceId, String simcardNumber, SendTxt txt, Integer msgSN, String deviceType) {
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message = MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_SEND_TXT, msgSN, txt, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SEND_TXT, deviceId);
    }

    @Override
    public void sendTxtOnly(String deviceId, String simcardNumber, SendTxt txt, Integer msgSN, String deviceType) {
        T808Message message = MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_SEND_TXT, msgSN, txt, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SEND_TXT, deviceId);
    }

    @Override
    public void sendTextAndSubscribeAnswer(BindDTO configInfo, SendTxt txt, Integer msgSN) {
        String deviceId = configInfo.getDeviceId();
        String simCardNumber = configInfo.getSimCardNumber();
        String type = configInfo.getDeviceType();
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        paramSendingCache
            .put(SystemHelper.getCurrentUsername(), msgSN, simCardNumber, SendTarget.getInstance(SendModule.SEND_TXT));
        T808Message message = MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SEND_TXT, msgSN, txt, type);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SEND_TXT, deviceId);
    }

    @Override
    public void sendDeviceWakeUp(DeviceWakeUpEntity deviceWakeUpEntity, String sessionId) {
        String monitorId = deviceWakeUpEntity.getMonitorId();
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(monitorId);
        final Map<String, String> configInfo =
                RedisHelper.getHashMap(key, "deviceNumber", "deviceType", "deviceId", "simCardNumber");
        if (configInfo == null) {
            // 推送下发失败
            simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_DEVICE_WAKE_UP,
                DirectiveStatusEnum.IS_FAILED.getNum());
            return;
        }
        String deviceNumber = configInfo.get("deviceNumber");
        String deviceType = configInfo.get("deviceType");
        Integer msgSn = DeviceHelper.getRegisterDevice(monitorId, deviceNumber);
        if (msgSn == null) {
            // 推送下发失败
            simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_DEVICE_WAKE_UP,
                DirectiveStatusEnum.IS_FAILED.getNum());
            return;
        }
        DirectiveForm form = new DirectiveForm();
        form.setDownTime(new Date());
        form.setMonitorObjectId(monitorId);
        form.setStatus(4);
        form.setParameterType("0x8300-deviceWakeUp");
        form.setSwiftNumber(msgSn);
        form.setReplyCode(1);
        parameterDao.addDirective(form);
        SendParam sendParam = new SendParam();
        sendParam.setMsgSNACK(msgSn);
        sendParam.setParamId(form.getId());
        sendParam.setVehicleId(monitorId);
        sendParam.setSessionId(sessionId);
        f3SendStatusProcessService.updateSendParam(sendParam, 5);
        Integer wakeUpDuration = deviceWakeUpEntity.getWakeUpDuration();
        SendTxt sendTxt = new SendTxt();
        sendTxt.setSign(0);
        sendTxt.setTxt("WAKEUP" + wakeUpDuration);
        if (Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808_2019_STR).contains(deviceType)) {
            sendTxt.setTextType(1);
            sendTxt.setType(1);
        }
        String deviceId = configInfo.get("deviceId");
        String simCardNumber = configInfo.get("simCardNumber");
        // 订阅推送消息
        SubscibeInfo info = new SubscibeInfo(sessionId, deviceId, msgSn, ConstantUtil.T808_DEVICE_GE_ACK);
        WebSubscribeManager.getInstance().addDeviceWakeUpAckSubRelation(monitorId, msgSn, sessionId);
        SubscibeInfoCache.getInstance().putTable(info);
        paramSendingCache
            .put(SystemHelper.getCurrentUsername(), msgSn, simCardNumber, SendTarget.getInstance(SendModule.SEND_TXT));
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SEND_TXT, msgSn, sendTxt, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SEND_TXT, deviceId);
    }

    @Override
    public void sendQuestion(String deviceId, String simcardNumber, SendQuestion question, Integer msgSN,
        BindDTO bindDTO) {
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_ANSWER);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message =
            MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_SEND_QUIZ, msgSN, question, bindDTO);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SEND_QUIZ, deviceId);
    }

    @Override
    public void vehicleCommand(String deviceId, String simcardNumber, VehicleCommand vehicleCommand, Integer msgSN,
        String deviceType) {
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_VEHICLE_CONTROL_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message = MsgUtil
            .get808Message(simcardNumber, ConstantUtil.T808_VEHICLE_CONTROLLER, msgSN, vehicleCommand, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_VEHICLE_CONTROLLER, deviceId);
    }

    @Override
    public void alarmAck(String deviceId, String mobile, AlarmAck alarmAck, Integer msgSN, String deviceType) {
        T808Message message = MsgUtil.get808Message(mobile, ConstantUtil.T808_ALARM_ACK, msgSN, alarmAck, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_ALARM_ACK, deviceId);
    }

    @Override
    public void deviceCommand(String simCardNumber, DeviceCommand deviceCommand, Integer msgSno, String deviceId,
        String deviceType, SendTarget sendTarget) {
        T8080x8105 t8080x8105 = new T8080x8105();
        t8080x8105.setCw(deviceCommand.getCw());
        t8080x8105.setParam(deviceCommand.getParam());

        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (sendTarget != null) {
            paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSno, simCardNumber, sendTarget);
        }
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_DEVICE_CONTROLLER, msgSno, t8080x8105, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_DEVICE_CONTROLLER, deviceId);
    }

    @Override
    public void devicePropertyQuery(String deviceId, String simCardNumber, Integer msgSno, String deviceType) {
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_ATTR_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_DEVICE_PROPERTY_QUERY, msgSno, null, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_DEVICE_PROPERTY_QUERY, deviceId);
    }

    @Override
    public void setCommunicationParam(String deviceId, String simCardNumber, CommunicationParam param, Integer msgSno,
        String deviceType, SendTarget sendTarget) {
        // 根据设备id查询设备协议类型
        JSONArray array = new JSONArray();
        addStringParamToJSONArray(array, 0x0010, param.getMainServerAPN());
        addStringParamToJSONArray(array, 0x0011, param.getMainServerCallUserName());
        addStringParamToJSONArray(array, 0x0012, param.getMainServerCallUserPwd());
        addStringParamToJSONArray(array, 0x0013, param.getMainServerAddress());
        addStringParamToJSONArray(array, 0x0014, param.getBackupServerAPN());
        addStringParamToJSONArray(array, 0x0015, param.getBackupServerCallUserName());
        addStringParamToJSONArray(array, 0x0016, param.getBackupServerCallUserPwd());
        addStringParamToJSONArray(array, 0x0017, param.getBackupServerAddress());
        if (ProtocolTypeUtil.checkDeviceType2013(deviceType)) {
            addIntegerParamToJSONArray(array, 0x0018, param.getServerTCPPort(), 4);
            addIntegerParamToJSONArray(array, 0x0019, param.getServerUDPPort(), 4);
        } else if (ProtocolTypeUtil.checkDeviceType2019(deviceType)) {
            addStringParamToJSONArray(array, 0x0023, param.getSlaveServerAPN());
            addStringParamToJSONArray(array, 0x0024, param.getSlaveServerCallUserName());
            addStringParamToJSONArray(array, 0x0025, param.getSlaveServerCallUserPwd());
            addStringParamToJSONArray(array, 0x0026, param.getSlaveServerAddress());
        }
        sendParamMsg(deviceId, simCardNumber, array, msgSno, deviceType, sendTarget);
    }

    /**
     * 添加string类型的终端参数到 JSONArray 中;
     * @param array
     * @param id       终端参数设置id
     * @param valueStr 对应的string值
     */
    private void addStringParamToJSONArray(JSONArray array, Integer id, String valueStr) {
        if (StringUtils.isNotBlank(valueStr)) {
            array.add(getStringParam(id, valueStr));
        }
    }

    /**
     * 添加int类型的终端参数到 JSONArray 中;
     * @param array
     * @param id       终端参数设置id
     * @param valueInt 对应的int值
     * @param len      长度
     */
    private void addIntegerParamToJSONArray(JSONArray array, Integer id, Integer valueInt, Integer len) {
        if (valueInt != null) {
            array.add(getIntParam(id, len, valueInt));
        }
    }

    @Override
    public void setDeviceParam(String deviceId, String simCardNumber, DeviceParam param, Integer msgSno,
        String deviceType, SendTarget sendTarget) {
        JSONArray array = new JSONArray();
        addIntegerParamToJSONArray(array, 0x0001, param.getHeartSpace(), 4);
        addIntegerParamToJSONArray(array, 0x0002, param.getTcpAckTimeOut(), 4);
        addIntegerParamToJSONArray(array, 0x0003, param.getTcpReUpTimes(), 4);
        addIntegerParamToJSONArray(array, 0x0004, param.getUdpAckTimeOut(), 4);
        addIntegerParamToJSONArray(array, 0x0005, param.getUdpReUpTimes(), 4);
        addIntegerParamToJSONArray(array, 0x0006, param.getSmsAckTimeOut(), 4);
        addIntegerParamToJSONArray(array, 0x0007, param.getSmsReUpTimes(), 4);
        addIntegerParamToJSONArray(array, 0x0030, param.getInflectionPointAdditional(), 4);
        addIntegerParamToJSONArray(array, 0x0031, param.getElectronicFenceRadius(), 2);
        sendParamMsg(deviceId, simCardNumber, array, msgSno, deviceType, sendTarget);
    }

    @Override
    public void setPositionParam(String deviceId, String simCardNumber, PositionParam param, Integer msgSno,
        String deviceType, SendTarget sendTarget) {
        JSONArray array = new JSONArray();
        Integer positionUpTactics = param.getPositionUpTactics();
        addIntegerParamToJSONArray(array, 0x0020, positionUpTactics, 4);
        addIntegerParamToJSONArray(array, 0x0021, param.getPositionUpScheme(), 4);
        if (positionUpTactics != null) {
            if (positionUpTactics == 0) { // 定时汇报
                addIntegerParamToJSONArray(array, 0x0022, param.getDriverLoggingOutUpTimeSpace(), 4);
                addIntegerParamToJSONArray(array, 0x0027, param.getDormancyUpTimeSpace(), 4);
                addIntegerParamToJSONArray(array, 0x0028, param.getEmergencyAlarmUpTimeSpace(), 4);
                addIntegerParamToJSONArray(array, 0x0029, param.getDefaultTimeUpSpace(), 4);
            } else if (positionUpTactics == 1) { // 定距汇报
                addIntegerParamToJSONArray(array, 0x002C, param.getDefaultDistanceUpSpace(), 4);
                addIntegerParamToJSONArray(array, 0x002D, param.getDriverLoggingOutUpDistanceSpace(), 4);
                addIntegerParamToJSONArray(array, 0x002E, param.getDormancyUpDistanceSpace(), 4);
                addIntegerParamToJSONArray(array, 0x002F, param.getEmergencyAlarmUpDistanceSpace(), 4);
            } else { // 定时和定距汇报
                addIntegerParamToJSONArray(array, 0x0022, param.getDriverLoggingOutUpTimeSpace(), 4);
                addIntegerParamToJSONArray(array, 0x0027, param.getDormancyUpTimeSpace(), 4);
                addIntegerParamToJSONArray(array, 0x0028, param.getEmergencyAlarmUpTimeSpace(), 4);
                addIntegerParamToJSONArray(array, 0x0029, param.getDefaultTimeUpSpace(), 4);
                addIntegerParamToJSONArray(array, 0x002C, param.getDefaultDistanceUpSpace(), 4);
                addIntegerParamToJSONArray(array, 0x002D, param.getDriverLoggingOutUpDistanceSpace(), 4);
                addIntegerParamToJSONArray(array, 0x002E, param.getDormancyUpDistanceSpace(), 4);
                addIntegerParamToJSONArray(array, 0x002F, param.getEmergencyAlarmUpDistanceSpace(), 4);
            }
        }
        sendParamMsg(deviceId, simCardNumber, array, msgSno, deviceType, sendTarget);
    }

    @Override
    public void setPhoneParam(String deviceId, String simCardNumber, PhoneParam param, Integer msgSno,
        String deviceType, SendTarget sendTarget) {
        JSONArray array = new JSONArray();
        addStringParamToJSONArray(array, 0x0040, param.getPlatformPhoneNumber());
        addStringParamToJSONArray(array, 0x0041, param.getResetPhoneNumber());
        addStringParamToJSONArray(array, 0x0042, param.getReInitialPhoneNumber());
        addStringParamToJSONArray(array, 0x0043, param.getPlatformSMSPhoneNumber());
        addStringParamToJSONArray(array, 0x0044, param.getReceiveDeviceSMSTxtAlarmPhoneNumber());
        addIntegerParamToJSONArray(array, 0x0045, param.getDeviceAnswerPhoneType(), 4);
        addIntegerParamToJSONArray(array, 0x0046, param.getTimesMaxCallTime(), 4);
        addIntegerParamToJSONArray(array, 0x0047, param.getMonthlyMaxCallTime(), 4);
        addStringParamToJSONArray(array, 0x0048, param.getListenPhoneNumber());
        addStringParamToJSONArray(array, 0x0049, param.getPlatformPrivilegeSMSNumber());
        sendParamMsg(deviceId, simCardNumber, array, msgSno, deviceType, sendTarget);
    }

    @Override
    public void setT808Param(String mobile, List<T808Param> params, Integer msgSN) {
        sendParamMsg(mobile, params, msgSN);
    }

    @Override
    public void setGNSSParam(String deviceId, String simCardNumber, GNSSParam param, Integer msgSno, String deviceType,
        SendTarget sendTarget) {
        JSONArray array = new JSONArray();
        try {
            int status = param.getGPSFlag() + (param.getBeidouFlag() << 1) + (param.getGLONASSFlag() << 2) + (
                param.getGalileoFlag() << 3);
            array.add(getIntParam(0x0090, 1, status));
        } catch (NullPointerException e) {
            logger.info("GNSS参数-GNSS参数定位模式为空");
        }
        addIntegerParamToJSONArray(array, 0x0091, param.getGNSSBaudRate(), 4);
        addIntegerParamToJSONArray(array, 0x0092, param.getGNSSPositionOutputRate(), 4);
        addIntegerParamToJSONArray(array, 0x0093, param.getGNSSPositionCollectRate(), 4);
        addIntegerParamToJSONArray(array, 0x0094, param.getGNSSPositionUploadType(), 4);
        sendParamMsg(deviceId, simCardNumber, array, msgSno, deviceType, sendTarget);
    }

    @Override
    public void setCameraParam(String deviceId, String simCardNumber, CameraParam param, Integer msgSno,
        String deviceType, SendTarget sendTarget) {
        JSONArray array = new JSONArray();
        try {
            Integer timingSpace = param.getTimingSpace();
            Integer timingUnit = param.getTimingUnit();
            if (Objects.isNull(timingSpace)) {
                // 如果定时时间间隔为空, 并且"定时时间单位"为秒, 则默认为5秒, 如果为分, 则默认1分钟
                timingSpace = timingUnit == 0 ? 5 : 1;
            }
            long timing = (long) (param.getCameraTimerOpenFlag1() + (param.getCameraTimerOpenFlag2() << 1) + (
                param.getCameraTimerOpenFlag3() << 2) + (param.getCameraTimerOpenFlag4() << 3) + (
                param.getCameraTimerOpenFlag5() << 4) + (param.getCameraTimerSaveFlag1() << 8) + (
                param.getCameraTimerSaveFlag2() << 9) + (param.getCameraTimerSaveFlag3() << 10) + (
                param.getCameraTimerSaveFlag4() << 11) + (param.getCameraTimerSaveFlag5() << 12) + (timingUnit << 16)
                + (timingSpace << 17));
            array.add(getIntParam(0x0064, 4, (int) timing));
        } catch (NullPointerException e) {
            logger.info("视频拍照参数-定时拍照控制为null");
        }
        try {
            Integer distanceSpace = param.getDistanceSpace();
            Integer distanceUnit = param.getDistanceUnit();
            if (Objects.isNull(distanceSpace)) {
                // 如果定距距离间隔为空, 并且"定距距离单位"为米, 则默认为100, 如果为千米, 则默认为1千米
                distanceSpace = distanceUnit == 0 ? 100 : 1;
            }
            long space = (long) (param.getCameraDistanceOpenFlag1() + (param.getCameraDistanceOpenFlag2() << 1) + (
                param.getCameraDistanceOpenFlag3() << 2) + (param.getCameraDistanceOpenFlag4() << 3) + (
                param.getCameraDistanceOpenFlag5() << 4) + (param.getCameraDistanceSaveFlag1() << 8) + (
                param.getCameraDistanceSaveFlag2() << 9) + (param.getCameraDistanceSaveFlag3() << 10) + (
                param.getCameraDistanceSaveFlag4() << 11) + (param.getCameraDistanceSaveFlag5() << 12) + (distanceUnit
                << 16) + (distanceSpace << 17));
            array.add(getIntParam(0x0065, 4, (int) space));
        } catch (NullPointerException e) {
            logger.info("视频拍照参数-定距拍照控制为null");
        }
        addIntegerParamToJSONArray(array, 0x0070, param.getPictureQuality(), 4);
        addIntegerParamToJSONArray(array, 0x0071, param.getLuminance(), 4);
        addIntegerParamToJSONArray(array, 0x0072, param.getContrast(), 4);
        addIntegerParamToJSONArray(array, 0x0073, param.getSaturation(), 4);
        addIntegerParamToJSONArray(array, 0x0074, param.getChroma(), 4);
        sendParamMsg(deviceId, simCardNumber, array, msgSno, deviceType, sendTarget);
    }

    @Override
    public void setSpeedMax(String deviceId, String simcardNumber, SpeedLimitParam param, Integer msgSN,
        String deviceType) {
        JSONArray array = new JSONArray();
        array.add(getIntParam(0x0055, 4, param.getMasSpeed()));
        array.add(getIntParam(0x0056, 4, param.getSpeedTime()));
        sendParamMsg(deviceId, simcardNumber, array, msgSN, deviceType, null);
    }

    @Override
    public void recordCollection(String deviceId, String simcardNumber, RecordCollection param, Integer msgSN,
        String deviceType) {
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUId(), deviceId, msgSN, ConstantUtil.T808_DRIVER_RECORD_UPLOAD);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message =
            MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_TRAVEL_RECORD_COLLECT, msgSN, param, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_TRAVEL_RECORD_COLLECT, deviceId);
    }

    @Override
    public void recordSend(String deviceId, String simcardNumber, RecordSend param, Integer msgSN, String deviceType) {
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), simcardNumber, msgSN, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message =
            MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_TRAVEL_RECORD_DOWN, msgSN, param, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_TRAVEL_RECORD_DOWN, deviceId);
    }

    private void sendParamMsg(String deviceId, String simCardNumber, JSONArray array, Integer msgSno, String deviceType,
        SendTarget sendTarget) {
        SetParam setP = new SetParam();
        setP.setParametersCount(array.size());
        setP.setPackageSum(array.size());
        setP.setParamItems(array);

        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (sendTarget != null) {
            paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSno, simCardNumber, sendTarget);
        }
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SET_PARAM, msgSno, setP, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
    }

    private void sendParamMsg(String mobile, List<T808Param> params, Integer msgSN) {
        JSONObject msg = new JSONObject();
        msg.put("parametersCount", params.size());
        msg.put("packageSum", params.size());
        msg.put("paramItems", params);
    }

    private T808Param getIntParam(Integer id, Integer len, Integer value) {
        T808Param param = new T808Param();
        param.setParamId(id);
        param.setParamLength(len);
        param.setParamValue(value);
        return param;
    }

    private T808Param getStringParam(Integer id, String value) {
        T808Param param = new T808Param();
        param.setParamId(id);
        param.setParamLength(value.length());
        param.setParamValue(value);
        return param;
    }

    @Override
    public void setEvent(String deviceId, String simCardNumber, List<EventSetParam> param, Integer msgSno,
        String deviceType, SendTarget sendTarget) {
        EventSetParamSend eventSend = new EventSetParamSend();
        JSONArray array = new JSONArray();
        for (EventSetParam eventSetParam : param) {
            JSONObject obj = new JSONObject();
            String result = eventSetParam.getEventContent();
            if (StringUtils.isNotBlank(result)) {
                int len = result.getBytes(Charset.forName("GBK")).length;
                obj.put("len", len);
            } else {
                obj.put("len", 1);
            }

            obj.put("id", eventSetParam.getEventId());
            obj.put("value",
                StringUtils.isEmpty(eventSetParam.getEventContent()) ? 0 : eventSetParam.getEventContent());
            array.add(obj);
        }
        eventSend.setType(param.get(0).getOperationType());
        eventSend.setEventSum(array.size());
        eventSend.setPackageSum(array.size());
        eventSend.setEventList(array);

        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (sendTarget != null) {
            paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSno, simCardNumber, sendTarget);
        }
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SET_EVENT, msgSno, eventSend, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_EVENT, deviceId);
    }

    @Override
    public void setInformationDemand(String deviceId, String simCardNumber, List<InformationParam> param,
        Integer msgSno, String deviceType, SendTarget sendTarget) {
        InformationParamSend infoSend = new InformationParamSend();
        JSONArray array = new JSONArray();
        for (InformationParam informationParam : param) {
            JSONObject obj = new JSONObject();
            String result = informationParam.getInfoContent();
            if (StringUtils.isNotEmpty(result)) {
                int len = result.getBytes(Charset.forName("GBK")).length;
                obj.put("infoLen", len);
            } else {
                obj.put("infoLen", 0);
            }
            obj.put("infoType", informationParam.getInfoId());
            obj.put("infoValue", result);
            // 删除所有: 不计算到项数中
            if (Objects.nonNull(informationParam.getInfoId())) {
                array.add(obj);
            }
        }
        infoSend.setType(param.get(0).getOperationType());
        infoSend.setInfoSum(array.size());
        infoSend.setPackageSum(array.size());
        infoSend.setInfoList(array);
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (sendTarget != null) {
            paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSno, simCardNumber, sendTarget);
        }
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SET_INFO_MENU, msgSno, infoSend, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_INFO_MENU, deviceId);
    }

    @Override
    public void setPhoneBook(String deviceId, String simCardNumber, List<PhoneBookParam> param, Integer msgSno,
        String deviceType, SendTarget sendTarget) {
        PhoneBookParamSend phoneBookSend = new PhoneBookParamSend();
        JSONArray array = new JSONArray();
        for (PhoneBookParam phoneBook : param) {
            if (phoneBook.getOperationType() == 0) {
                continue;
            }
            JSONObject obj = new JSONObject();
            String phoneNumber = phoneBook.getPhoneNo();
            if (StringUtils.isNotBlank(phoneNumber)) {
                int mobileLen = phoneNumber.getBytes(Charset.forName("GBK")).length;
                obj.put("mobileLen", mobileLen);
            } else {
                obj.put("mobileLen", 0);
            }
            String contact = phoneBook.getContact();
            if (StringUtils.isNotBlank(contact)) {
                int contactLen = contact.getBytes(Charset.forName("GBK")).length;
                obj.put("linkmanLen", contactLen);
            } else {
                obj.put("linkmanLen", 0);
            }
            obj.put("sign", phoneBook.getCallType());
            obj.put("mobile", phoneNumber);
            obj.put("linkman", contact);
            array.add(obj);
        }
        phoneBookSend.setType(param.get(0).getOperationType());
        phoneBookSend.setLinkmanSum(array.size());
        phoneBookSend.setPackageSum(array.size());
        phoneBookSend.setLinkmanList(array);

        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (sendTarget != null) {
            paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSno, simCardNumber, sendTarget);
        }
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SET_PHONE_BOOK, msgSno, phoneBookSend, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PHONE_BOOK, deviceId);
    }

    @Override
    public void originalOrder(String deviceId, String simCardNumber, OriginalOrder originalOrder, Integer msgSno,
        String deviceType) {
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_PENETRATE_DOWN, msgSno, originalOrder, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, deviceId);
    }

    @Override
    public void terminalParameters(String deviceId, String simCardNumber, Integer msgSno, String deviceType) {
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_PARAM_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_DEVICE_PARAMETER_COMMAND, msgSno, null, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_DEVICE_PARAMETER_COMMAND, deviceId);
    }

    @Override
    public void setSensorPolling(String mobile, Integer msgSno, List<SensorParam> sensorParams, String deviceId,
        String deviceType) {
        T808_0x8900 t8080x8900 = new T808_0x8900();
        t8080x8900.setSensorDatas(sensorParams);
        t8080x8900.setType(0xFA);
        t8080x8900.setSum(sensorParams.size());
        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T808_PENETRATE_DOWN, msgSno, t8080x8900, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, deviceId);
    }

    @Override
    public void informationService(String deviceId, String simcardNumber, InformationService informationService,
        Integer msgSN, String deviceType) {
        T808Message message =
            MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_INFO_MSG, msgSN, informationService, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_INFO_MSG, deviceId);
    }

    @Override
    public void queryF3SensorParam(String mobile, List<F3SensorParamQuery> queries, Integer msgSno, String deviceId,
        String deviceType) {
        List<Integer> paramIDs = new ArrayList<>();
        for (F3SensorParamQuery q : queries) {
            paramIDs.add(Integer.parseInt(Integer.toHexString(q.getSign()) + Integer.toHexString(q.getId()), 16));
        }
        T808Msg8106 t8080x8106 = new T808Msg8106();
        t8080x8106.setParamSum(queries.size());
        t8080x8106.setParamIds(paramIDs);

        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T808_QUERY_PARAMS, msgSno, t8080x8106, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_QUERY_PARAMS, deviceId);
    }

    @Override
    public String setF3SetParamByVehicleAndPeopleAndThing(String vehicleId, String parameterName,
        List<ParamItem> params, String paramType, boolean isOvertime, Integer flag) {
        // 获取最后一次下发的编号
        String paramId = getLastSendParamID(vehicleId, parameterName, paramType);
        // 获取车辆及设备信息
        final Map<String, String> monitor = RedisHelper.getHashMap(
                RedisKeyEnum.MONITOR_INFO.of(vehicleId, "deviceId", "simCardNumber", "deviceNumber", "deviceType"));
        String deviceId = null;
        String simcardNumber = null;
        String deviceNumber = null;
        if (monitor != null) {
            deviceId = monitor.get("deviceId");
            simcardNumber = monitor.get("simCardNumber");
            deviceNumber = monitor.get("deviceNumber");
        }
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // 设备已经注册
            // 下发参数
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, 4, vehicleId, paramType, parameterName);
            if (isOvertime) {
                SendParam sendParam = new SendParam();
                sendParam.setMsgSNACK(msgSN);
                sendParam.setParamId(paramId);
                sendParam.setVehicleId(vehicleId);
                f3SendStatusProcessService.updateSendParam(sendParam, 1);
            }

            // 订阅消息
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK,
                    1);
            SubscibeInfoCache.getInstance().putTable(info);
            info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
                ConstantUtil.T808_DATA_PERMEANCE_REPORT);
            SubscibeInfoCache.getInstance().putTable(info);
            // 绑定下发
            T808_0x8103 t8080x8103 = new T808_0x8103();
            t8080x8103.setPackageSum(params.size());
            t8080x8103.setParametersCount(params.size());
            t8080x8103.setParamItems(params);
            T808Message message =
                MsgUtil.get808Message(simcardNumber, ConstantUtil.T808_SET_PARAM, msgSN, t8080x8103, monitor);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
            String userName = SystemHelper.getCurrentUsername();

            flag = flag == null ? -1 : flag;
            switch (flag) {
                case 1:
                    //正反转设置
                    paramSendingCache
                        .put(userName, msgSN, simcardNumber, SendTarget.getInstance(SendModule.FORWARD_AND_BACKWARD));
                    break;
                case 2:
                    //温度监测设置
                    paramSendingCache
                        .put(userName, msgSN, simcardNumber, SendTarget.getInstance(SendModule.TEMPERATURE_MONITORING));
                    break;
                case 3:
                    //里程监测设置
                    paramSendingCache
                        .put(userName, msgSN, simcardNumber, SendTarget.getInstance(SendModule.MILEAGE_MONITORING));
                    break;
                case 10:
                    //湿度监测设置
                    paramSendingCache
                        .put(userName, msgSN, simcardNumber, SendTarget.getInstance(SendModule.HUMIDITY_MONITORING));
                    break;
                default:
                    break;
            }
            return String.valueOf(msgSN);
        } else { // 设备未注册
            msgSN = 0;// 绑定下发
            sendHelper.updateParameterStatus(paramId, msgSN, 5, vehicleId, paramType, parameterName);
        }
        return String.valueOf(msgSN);
    }

    /**
     * 根据车辆、下发参数编号、下发类型获取最后一次下发的编号
     * @param vehicleId 车辆编号
     * @param paramid   下发参数编号
     * @param type      下发类型获
     * @return
     */
    private String getLastSendParamID(String vehicleId, String paramid, String type) {
        List<Directive> paramlist = parameterDao.findParameterByType(vehicleId, paramid, type); // 6:报警
        Directive param = null;
        if (paramlist != null && !paramlist.isEmpty()) {
            param = paramlist.get(0);
            return param.getId();
        }
        return "";
    }

    @Override
    @Deprecated
    public void setF3CommunicationParam(String mobile, F3CommunicationParam param, Integer msgSN, String deviceId) {
        JSONArray array = new JSONArray();
        array.add(param);
        T808_0x8900 t8080x8900 = new T808_0x8900();
        t8080x8900.setType(0xF5);
        t8080x8900.setSum(array.size());
        t8080x8900.setSensorDatas(array);
    }

    @Override
    public void getF3SensorPrivateParam(String mobile, Integer sensorID, String commandStr, Integer msgSN,
        String deviceId, BindDTO monitorConfig) {
        commandStr = commandStr.trim();
        byte[] commandBytes = null;
        if (commandStr.contains(" ")) {
            String[] arr = commandStr.split(" ");
            commandBytes = new byte[arr.length];
            for (int i = 0, m = arr.length; i < m; i++) {
                commandBytes[i] = (byte) Integer.parseInt(arr[i], 16);
            }
        } else {
            commandBytes = new byte[commandStr.length() / 2];
            for (int i = 0, m = commandBytes.length; i < m; i++) {
                commandBytes[i] = (byte) Integer.parseInt(commandStr.substring(i * 2, i * 2 + 2));
            }
        }
        // msg.put("data", res);
        T808_0x8900 t8080x8900 = new T808_0x8900();
        t8080x8900.setType(0xF9);
        List<JSONObject> sensorDatas = new ArrayList<>();
        JSONObject object = new JSONObject();
        object.put("sensorId", sensorID);
        // f3自行组装, 这里不需要再组装.
        object.put("content", commandBytes);
        t8080x8900.setData(commandBytes);
        sensorDatas.add(object);
        t8080x8900.setSensorDatas(sensorDatas);

        T808Message message = MsgUtil
            .get808Message(mobile, ConstantUtil.T808_PENETRATE_DOWN, msgSN, t8080x8900, monitorConfig.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, deviceId);
    }

    @Override
    public void oilElectric(String deviceId, String mobile, OilElectricControl control, Integer msgSN,
        String deviceType) {
        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T808_VEHICLE_CONTROLLER, msgSN, control, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_VEHICLE_CONTROLLER, deviceId);
    }

    @Override
    public void sendOBD(String mobile, Integer msgSN, OBDParam obdParam, String deviceId, String deviceType) {
        SetStreamObd setStreamObd = new SetStreamObd();
        setStreamObd.setVehicleTypeId(obdParam.getVehicleTypeId());
        setStreamObd.setUploadTime(obdParam.getUploadTime());

        T808_0x8103 benchmark = new T808_0x8103();
        ParamItem paramItem = new ParamItem();
        paramItem.setParamLength(64);
        paramItem.setParamId(0xF3E5);
        paramItem.setParamValue(setStreamObd);
        List<ParamItem> paramItems = new ArrayList<>();
        paramItems.add(paramItem);
        benchmark.setParamItems(paramItems);
        benchmark.setParametersCount(1);

        // 订阅
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK, 1);
        SubscibeInfoCache.getInstance().putTable(info);
        SubscibeInfo info1 = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
            ConstantUtil.T808_DATA_PERMEANCE_REPORT);
        SubscibeInfoCache.getInstance().putTable(info1);

        T808Message message = MsgUtil.get808Message(mobile, ConstantUtil.T808_SET_PARAM, msgSN, benchmark, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
    }

    @Override
    public void setStationParam(String deviceId, String simCardNumber, StationParam stationParam, Integer msgSno,
        String deviceType, SendTarget sendTarget) {
        Integer locationNumber = stationParam.getLocationNumber();
        int paramLength = 15;
        // 按按频率上报
        if (stationParam.getRequitePattern() == 0) {
            Integer requiteInterval = stationParam.getRequiteInterval();
            if (requiteInterval == null) {
                stationParam.setLocationNumber(0xFFFFFFFF);
                stationParam.setLocationTimeNum(0);
            } else {
                stationParam.setLocationNumber(requiteInterval);
                //不为0，上传间隔有效，同时把【定点时间个数】置 0，代表定点时 间模式无效；
                if (requiteInterval != 0) {
                    stationParam.setLocationTimeNum(0);
                    stationParam.setLocationTime("");
                    //为 0 时，表示上报间隔和上报起始时间点失效，以定点时间上报 为准
                } else {
                    stationParam.setRequitePattern(1);
                    String locationTime = stationParam.getLocationTime();
                    stationParam.setLocationTime(locationTime != null ? locationTime.replace(";", "") : null);
                    stationParam.setLocationTimeNum(Objects.isNull(locationNumber) ? 0 : locationNumber);
                    paramLength = paramLength + (locationNumber * 3);
                }
            }
            String requiteTime = stationParam.getRequiteTime();
            if (StringUtils.isBlank(requiteTime)) {
                stationParam.setRequiteTime("00:00:01");
            }
        } else {
            //不为零时，需把【上报频率】置 0，代表按频率上报模式无效；
            if (locationNumber != null && locationNumber != 0) {
                stationParam.setLocationNumber(0);
            } else {
                stationParam.setLocationNumber(0xFFFFFFFF);
            }
            stationParam.setRequiteTime("00:00:01");
            String locationTime = stationParam.getLocationTime();
            stationParam.setLocationTime(locationTime != null ? locationTime.replace(";", "") : null);
            stationParam.setLocationTimeNum(Objects.isNull(locationNumber) ? 0 : locationNumber);
            paramLength = paramLength + ((locationNumber == null ? 0 : locationNumber) * 3);
        }
        ParamItem t808Param = new ParamItem();
        t808Param.setParamId(0xF308);
        t808Param.setParamLength(paramLength);
        t808Param.setParamValue(stationParam);
        List<ParamItem> params = new ArrayList<>();
        params.add(t808Param);
        // 绑定下发
        T808_0x8103 t8103 = new T808_0x8103();
        t8103.setPackageSum(1);
        t8103.setParametersCount(1);
        t8103.setParamItems(params);
        // 订阅
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (sendTarget != null) {
            paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSno, simCardNumber, sendTarget);
        }
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SET_PARAM, msgSno, t8103, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
    }

    @Override
    public void setSerialPortParam(String deviceId, String simCardNumber, List<SerialPortParam> serialPortParam,
        Integer msgSno, Integer id, String deviceType, SendTarget sendTarget) {
        //特殊参数设置实体
        List<SerialPort> serialPorts = new ArrayList<>();
        for (SerialPortParam portParam : serialPortParam) {
            if (portParam.getSerialPortNumber() != null) {
                SerialPort serialPort = new SerialPort();
                serialPort.setSum(portParam.getSerialPortNumber());
                // 波特率
                Integer baudRate = portParam.getBaudRate();
                serialPort.setBaudRate((baudRate != null && baudRate != -1) ? baudRate : 0xFF);
                // 数据位
                Integer dataBits = portParam.getDataBits();
                serialPort.setDataPosition((dataBits != null && dataBits != -1) ? dataBits : 0xFF);
                // 停止位
                Integer stopBit = portParam.getStopBit();
                serialPort.setStopPosition((stopBit != null && stopBit != -1) ? stopBit : 0xFF);
                // 校验位
                Integer parityBit = portParam.getParityBit();
                serialPort.setCheckPosition((parityBit != null && parityBit != -1) ? parityBit : 0xFF);
                // 流控
                Integer flowControl = portParam.getFlowControl();
                serialPort.setFlowControl((flowControl != null && flowControl != -1) ? flowControl : 0xFF);
                serialPort.setReceiveTimeOut(portParam.getDataAcceptanceTimeoutTime());
                serialPorts.add(serialPort);
            }
        }
        //终端特殊参数设置项
        SerialPortSettingItem serialPortSettingItem = new SerialPortSettingItem();
        serialPortSettingItem.setId(id);
        serialPortSettingItem.setLen((serialPortParam.size() * 7) + 1);
        serialPortSettingItem.setNumber(serialPortParam.size());
        serialPortSettingItem.setSerialPort(serialPorts);
        //参数设置数据消息体
        ParamItem t808Param = new ParamItem();
        t808Param.setParamId(id);
        t808Param.setParamLength((serialPortParam.size() * 7) + 1);
        t808Param.setParamValue(serialPortSettingItem);
        List<ParamItem> params = new ArrayList<>();
        params.add(t808Param);
        //终端参数通用消息体格式
        T808_0x8103 t8103 = new T808_0x8103();
        t8103.setPackageSum(1);
        t8103.setParametersCount(1);
        t8103.setParamItems(params);
        //订阅
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (sendTarget != null) {
            paramSendingCache.put(SystemHelper.getCurrentUsername(), msgSno, simCardNumber, sendTarget);
        }
        //下发
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_SET_PARAM, msgSno, t8103, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_SET_PARAM, deviceId);
    }

    @Override
    public void setDeviceUpgrade(String vehicleId, String deviceId, String simCardNumber, List<DeviceUpgrade> param,
        Integer msgSno, String deviceType) {

        DeviceUpgrade deviceUpgrade = param.get(0);
        String url = deviceUpgrade.getUrl();
        byte[] bytes = fastDFSClient.downloadFile(url);
        deviceUpgrade.setData(bytes);
        //订阅
        SubscibeInfo subscibeInfo =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSno, ConstantUtil.T808_UPLOAD_ACK);
        SubscibeInfoCache.getInstance().putTable(subscibeInfo);
        //下发
        T808Message message =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_DEVICE_UPLOAD, msgSno, deviceUpgrade, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_DEVICE_UPLOAD, deviceId);
    }
}
