package com.zw.platform.service.realTimeVideo.impl;

import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.realTimeVideo.FileUploadControl;
import com.zw.platform.domain.realTimeVideo.SendFileUpload;
import com.zw.platform.domain.realTimeVideo.TalkBackRecord;
import com.zw.platform.domain.realTimeVideo.VideoControl;
import com.zw.platform.domain.realTimeVideo.VideoControlSend;
import com.zw.platform.domain.realTimeVideo.VideoPlaybackControl;
import com.zw.platform.domain.realTimeVideo.VideoRequest;
import com.zw.platform.domain.realTimeVideo.VideoSendForm;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.service.realTimeVideo.VideoOrderSendService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.oil.T808_0x8900;
import com.zw.ws.impl.WsVideoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class VideoOrderSendServiceImpl implements VideoOrderSendService {
    private static final Logger logger = LogManager.getLogger(VideoOrderSendServiceImpl.class);

    @Autowired
    private WsVideoService wsVideoService;

    @Override
    public int sendVideoRequest(VideoSendForm form, VideoRequest vo) {
        //获取流水号
        int msgSN = DeviceHelper.serialNumber(form.getVehicleId());
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), form.getDeviceId(), msgSN,
            ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (msgSN >= 0) {
            //监控对象在线
            T808Message message = MsgUtil.get808Message(form.getSimNumber(), ConstantUtil.VIDEO_REQUEST, msgSN, vo);
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.VIDEO_REQUEST, form.getDeviceId());
        }
        return msgSN;
    }

    @Override
    public void sendVideoControl(VideoSendForm form, VideoControl vc) {
        //获取流水号
        Integer msgSN = DeviceHelper.getRegisterDevice(form.getVehicleId(), form.getDeviceNumber());
        if (msgSN == null) { // 设备未注册
            return;
        }
        //订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), form.getDeviceId(), ConstantUtil.T808_DEVICE_GE_ACK,
                msgSN);
        SubscibeInfoCache.getInstance().putTable(info);
    }

    /**
     * 下发0x9102的下发
     * @param form       终端信息
     * @param control    消息实体
     * @param deviceType 设备类型
     */
    @Override
    public void sendVideoControl(VideoSendForm form, VideoControlSend control, String deviceType) {
        Integer serialNumber = DeviceHelper.serialNumber(form.getVehicleId());
        if (serialNumber < 0) {
            return;
        }
        String deviceId = form.getDeviceId();
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, serialNumber,
            ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message = MsgUtil
            .get808Message(form.getSimNumber(), ConstantUtil.T808_VIDEO_TRANSMIT_CONTROL, serialNumber, control,
                deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_VIDEO_TRANSMIT_CONTROL, deviceId);
    }

    @Override
    public void sendAttributeQuery(VideoSendForm form, String deviceType) {
        //获取流水号
        Integer msgSN = DeviceHelper.getRegisterDevice(form.getVehicleId(), form.getDeviceNumber());
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), form.getDeviceId(), msgSN,
            ConstantUtil.DEVICE_UPLOAD_VIDEO_PARAM);
        SubscibeInfoCache.getInstance().putTable(info);
        if (msgSN != null) { // 设备已经注册
            T808Message message =
                MsgUtil.get808Message(form.getSimNumber(), ConstantUtil.VIDEO_ATTRIBUTE_QUERY, msgSN, null, deviceType);
            WebSubscribeManager.getInstance()
                .sendMsgToAll(message, ConstantUtil.VIDEO_ATTRIBUTE_QUERY, form.getDeviceId());
        }
    }

    @Override
    public void sendVideoParamSetting(VideoSendForm form, Map<String, Object> videoParams) {
        String vehicleId = form.getVehicleId();
        String simNumber = form.getSimNumber();
        String deviceId = form.getDeviceId();
        String deviceType = form.getDeviceType();
        String currentUsername = SystemHelper.getCurrentUsername();
        //获取流水号
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, form.getDeviceNumber());
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(currentUsername, deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        // 设备已经注册
        if (msgSno != null) {
            wsVideoService.sendVideoSetting(videoParams, msgSno, simNumber, deviceId, deviceType);
        }
    }

    @Override
    public void sendVideoSleep(VideoSendForm form, T808_0x8900<?> t8080x8900, String deviceType) {
        //获取流水号
        Integer msgSN = DeviceHelper.serialNumber(form.getVehicleId());
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), form.getDeviceId(), msgSN,
            ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        if (msgSN >= 0) { // 设备在线
            T808Message message = MsgUtil
                .get808Message(form.getSimNumber(), ConstantUtil.T808_PENETRATE_DOWN, msgSN, t8080x8900, deviceType);
            WebSubscribeManager.getInstance()
                .sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, form.getDeviceId());
        }
    }

    @Override
    public String sendTalkBack(VideoSendForm form, VideoRequest vo, String riskNumber) throws Exception {
        //获取流水号
        Integer msgSN = DeviceHelper.getRegisterDevice(form.getVehicleId(), form.getDeviceNumber());
        //订阅推送消息
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), form.getDeviceId(), msgSN,
            ConstantUtil.T808_DEVICE_GE_ACK, ConstantUtil.VIDEO_REQUEST);
        SubscibeInfoCache.getInstance().putTable(info);
        if (msgSN == null) { // 设备未注册
            return null;
        }
        T808Message message = MsgUtil.get808Message(form.getSimNumber(), ConstantUtil.VIDEO_REQUEST, msgSN, vo);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.VIDEO_REQUEST, form.getDeviceId());
        Long startTime = System.currentTimeMillis();
        Long endTime = startTime;
        //等待通用应答10s
        while ((endTime - startTime) < 10000) {
            Thread.sleep(300);
            endTime = System.currentTimeMillis();
            if (SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSN, form.getDeviceId()) == null) {
                String talkStart = DateUtil.getLongToDateStr(System.currentTimeMillis(), "yyMMddHHmmss");
                //9101收到应答后下发8900,开始录音 (原为异步下发)
                try {
                    send8900(form, talkStart, riskNumber);
                } catch (Exception e) {
                    logger.error("对讲下发开始录音异常", e);
                }
                return talkStart;
            }
        }
        return null;
    }

    private void send8900(VideoSendForm form, String talkStart, String riskNumber) {
        TalkBackRecord talkBackRecord = new TalkBackRecord();
        talkBackRecord.setSimcardNumber(form.getSimNumber());
        talkBackRecord.setRecordNumber(1);
        talkBackRecord.setRiskNumber(riskNumber.substring(2));
        talkBackRecord.setRecordStartTime(talkStart);

        T808_0x8900<TalkBackRecord> t8080x8900 = new T808_0x8900<>();
        t8080x8900.setType(0xF0);
        t8080x8900.setSum(1);
        t8080x8900.setSensorDatas(Collections.singletonList(talkBackRecord));
        //订阅推送消息
        Integer msgSN = DeviceHelper.getRegisterDevice(form.getVehicleId(), form.getDeviceNumber());
        if (null == msgSN) {
            return;
        }
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), form.getDeviceId(), msgSN,
            ConstantUtil.T808_DEVICE_GE_ACK, ConstantUtil.T808_PENETRATE_DOWN);
        SubscibeInfoCache.getInstance().putTable(info);
        //下发8900
        T808Message message =
            MsgUtil.get808Message(form.getSimNumber(), ConstantUtil.T808_PENETRATE_DOWN, msgSN, t8080x8900);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_PENETRATE_DOWN, form.getDeviceId());
    }

    /**
     * 下发文件上传指令 (返回0x1206)
     * @param form           终端信息
     * @param sendFileUpload 消息实体
     * @param deviceType     设备类型
     * @param msgSN          流水号
     */
    @Override
    public void sendFileUpload(VideoSendForm form, SendFileUpload sendFileUpload, String deviceType, Integer msgSN) {
        String deviceId = form.getDeviceId();
        String mobile = form.getSimNumber();
        //订阅推送消息(0x1206)
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
            ConstantUtil.ADAS_UP_EXG_MSG_RETURN_END_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_END, msgSN, sendFileUpload, deviceType);
        WebSubscribeManager.INSTANCE.sendMsgToAll(message, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_END, deviceId);
    }

    /**
     * 下发文件上传控制指令(返回通用应答)
     * @param form              终端信息
     * @param fileUploadControl 消息实体
     * @param deviceType        设备类型
     */
    @Override
    public Integer sendFileUploadControl(VideoSendForm form, FileUploadControl fileUploadControl, String deviceType) {
        Integer serialNumber = DeviceHelper.serialNumber(form.getVehicleId());
        if (serialNumber < 0) {
            return serialNumber;
        }
        String deviceId = form.getDeviceId();
        String mobile = form.getSimNumber();
        SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, serialNumber,
            ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message = MsgUtil
            .get808Message(mobile, ConstantUtil.T809_DOWN_EXG_MSG_APPLY_FOR_MONITOR_SARTUP_ACK, serialNumber,
                fileUploadControl, deviceType);
        WebSubscribeManager.getInstance()
            .sendMsgToAll(message, ConstantUtil.T809_DOWN_EXG_MSG_APPLY_FOR_MONITOR_SARTUP_ACK, deviceId);
        return serialNumber;
    }

    @Override
    public void sendPlaybackControl(VideoSendForm form, VideoPlaybackControl control, String deviceType) {
        // 获取流水号
        Integer msgSN = DeviceHelper.serialNumber(form.getVehicleId());
        if (msgSN < 0) {
            return;
        }
        String deviceId = form.getDeviceId();
        String mobile = form.getSimNumber();
        // 订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message message =
            MsgUtil.get808Message(mobile, ConstantUtil.T809_DOWN_EXG_MSG_CAR_LOCATION, msgSN, control, deviceType);
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T809_DOWN_EXG_MSG_CAR_LOCATION, deviceId);
    }

}
