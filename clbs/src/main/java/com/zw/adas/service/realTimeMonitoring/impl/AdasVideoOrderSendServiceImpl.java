package com.zw.adas.service.realTimeMonitoring.impl;

import com.zw.adas.push.common.AdasSimpMessagingTemplateUtil;
import com.zw.adas.service.realTimeMonitoring.AdasVideoOrderSendService;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.realTimeVideo.VideoControlSend;
import com.zw.platform.domain.realTimeVideo.VideoRequest;
import com.zw.platform.domain.realTimeVideo.VideoSendForm;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdasVideoOrderSendServiceImpl implements AdasVideoOrderSendService {

    private static final Logger logger = LogManager.getLogger(AdasVideoOrderSendServiceImpl.class);

    @Autowired
    AdasSimpMessagingTemplateUtil adasSimpMessagingTemplateUtil;

    /**
     * 实时监控传输请求下发（0x9101）对讲
     * @param form 下发对象信息
     * @param vo   实时音视频传输请求实体
     * @author lijie
     */
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
                return DateUtil.getLongToDateStr(System.currentTimeMillis(), "yyMMddHHmmss");
            }
        }
        return null;
    }

    @Override
    public boolean sendVideoControl(String vehicleId, VideoControlSend vc, BindDTO bindDTO, String talkStartTime,
        String warningTime, String riskId) {
        String deviceId = bindDTO.getDeviceId(); // 终端id
        String deviceNumber = bindDTO.getDeviceNumber(); // 终端编号
        String simNumber = bindDTO.getSimCardNumber(); // SIM卡编号
        //异步下发9102停止对讲和下发9208上传录音文件
        try {
            //获取流水号
            Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
            //订阅推送消息
            SubscibeInfo info =
                new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN, ConstantUtil.T808_DEVICE_GE_ACK);
            SubscibeInfoCache.getInstance().putTable(info);
            if (msgSN != null) { // 设备未注册
                T808Message message =
                    MsgUtil.get808Message(simNumber, ConstantUtil.VIDEO_CONTROL, msgSN, vc, bindDTO.getDeviceType());
                WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.VIDEO_CONTROL, deviceId);
            }
        } catch (Exception e) {
            logger.error("下发9102关闭对讲通道异常", e);
        }
        return true;
    }
}
