package com.zw.platform.push.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zw.adas.push.cache.AdasSubcibeTable;
import com.zw.platform.util.ConstantUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @version 1.0
 */
@Component
public class SimpMessagingTemplateUtil {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private AdasSubcibeTable adasSubcibeTable;

    /**
     * 推送808状态
     * @param obj       信息
     * @param vehicleId 设备Id
     */
    public void sendT808Status(Object obj, String vehicleId) {
        String msg = JSON.toJSONString(obj);
        final Set<String> sessions = WsSessionManager.INSTANCE.getStatusSessions(vehicleId);
        for (String session : sessions) {
            sendToSession(ConstantUtil.WEB_SOCKET_T808_STATUS, session, msg);
        }
    }

    /**
     * 推送808位置信息
     * @param obj      信息
     * @param deviceId 设备Id
     */
    public void sendT808Position(Object obj, String deviceId) {
        String jsonObj = JSON.toJSONString(obj);
        final Set<String> sessions = WsSessionManager.INSTANCE.getPositionSessions(deviceId);
        for (String session : sessions) {
            sendToSession(ConstantUtil.WEB_SOCKET_T808_LOCATION, session, jsonObj);
        }
    }

    public void sendToSession(String dest, String sessionId, Object msg) {
        SimpMessageHeaderAccessor header = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        header.setSessionId(sessionId);
        header.setLeaveMutable(true);
        final String user = WsSessionManager.INSTANCE.getSessionUser(sessionId);
        simpMessagingTemplate.convertAndSendToUser(user, dest, msg, header.getMessageHeaders());
    }

    /**
     * 推送OBD信息
     * @param obj      信息
     * @param deviceId 设备Id
     */
    public void sendObdInfo(Object obj, String deviceId) {
        List<String> users = WebSubscribeManager.getInstance().getObdUsers(deviceId);
        if (CollectionUtils.isNotEmpty(users)) {
            Map<String, Object> pushInfo = new HashMap<>(16);
            pushInfo.put("status", 1);
            pushInfo.put("obj", obj);
            String sendMessage = JSON.toJSONString(pushInfo);
            for (String user : users) {
                simpMessagingTemplate.convertAndSendToUser(user, ConstantUtil.WEB_SOCKET_OBD_URL, sendMessage);
            }
        }
    }

    /**
     * 推送位置数据
     * @param obj       信息
     * @param vehicleId 设备Id
     */
    public void sendRiskPosition(Object obj, String vehicleId) {
        String msg = JSON.toJSONString(obj);
        final Set<String> sessions = WsSessionManager.INSTANCE.getStatusSessions(vehicleId);
        for (String session : sessions) {
            sendToSession(ConstantUtil.WEB_SOCKET_RISK_LOCATION, session, msg);
        }
    }

    /**
     * 根据用户推送到指定的地址
     * @param user        设备号
     * @param destination 推送地址
     * @param obj         推送信息
     */
    public void sendStatusMsg(String user, String destination, Object obj) {
        simpMessagingTemplate.convertAndSendToUser(user, destination, JSON.toJSONString(obj));
    }

    /**
     * 根据推送到指定的地址
     * @param destination 推送地址
     * @param obj         推送信息
     */
    public void sendStatusMsg(String destination, Object obj) {
        simpMessagingTemplate.convertAndSend(destination, JSON.toJSONString(obj));
    }

    public void sendStatusMsgBySessionId(String sessionId, String destination, Object obj) {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setLeaveMutable(true);
        headerAccessor.setSessionId(sessionId);
        simpMessagingTemplate.convertAndSendToUser(sessionId, destination, obj, headerAccessor.getMessageHeaders());
    }

    /**
     * 推送808报警
     * @param obj       报警信息
     * @param vehicleId 车辆Id
     */
    public void sendT808Alarm(Object obj, String vehicleId) {
        String sendMessage = JSON.toJSONString(obj, SerializerFeature.WriteMapNullValue);
        final Set<String> statusSessions = WsSessionManager.INSTANCE.getStatusSessions(vehicleId);
        for (String statusSession : statusSessions) {
            sendToSession(ConstantUtil.WEB_SOCKET_T808_ALARM, statusSession, sendMessage);
        }
    }

    /**
     * 推送全局报警
     * @param obj       报警信息
     * @param vehicleId 车辆Id
     */
    public void sendGlobalAlarm(Object obj, String vehicleId) {
        String msg = JSON.toJSONString(obj);
        final Set<String> sessions = WsSessionManager.INSTANCE.getStatusSessions(vehicleId);
        for (String session : sessions) {
            sendToSession(ConstantUtil.WEB_SOCKET_GLOBAL_ALARM, session, msg);
            sendToSession(ConstantUtil.WEB_SOCKET_LKYW_GLOBAL_ALARM, session, msg);
        }
    }

    /**
     * 推送全局报警被处理通知
     */
    public void sendGlobalAlarmHandleNotice(String vehicleId) {
        final Set<String> sessions = WsSessionManager.INSTANCE.getStatusSessions(vehicleId);
        for (String session : sessions) {
            sendToSession(ConstantUtil.WEB_SOCKET_GLOBAL_ALARM_HANDLE_NOTICE, session, vehicleId);
        }
    }

    /**
     * 推送特殊报警
     * @param vehicleId 车辆id 通道号
     */
    public void sendSpecialReport(Object obj, String vehicleId) {
        String msg = JSON.toJSONString(obj);
        final Set<String> sessions = WsSessionManager.INSTANCE.getStatusSessions(vehicleId);
        for (String session : sessions) {
            sendToSession(ConstantUtil.WEB_SOCKET_SPECIAL_REPORT, session, msg);
        }
    }

    /**
     * 推送SOS报警
     * @param obj       报警信息
     * @param vehicleId 车辆Id
     */
    public void sendSOSAlarm(Object obj, String vehicleId) {
        String msg = JSON.toJSONString(obj);
        final Set<String> sessions = WsSessionManager.INSTANCE.getStatusSessions(vehicleId);
        for (String session : sessions) {
            sendToSession(ConstantUtil.WEB_SOCKET_SOS_ALARM, session, msg);
        }
    }

    /**
     * 推送远程升级
     */
    public void sendRemoteUpgrade(Message message, String vehicleId) {
        Set<String> userNameSet = adasSubcibeTable.getRemoteUpgradeCache().getIfPresent(vehicleId);
        if (userNameSet != null) {
            for (String user : userNameSet) {
                simpMessagingTemplate.convertAndSendToUser(user, ConstantUtil.WEB_REMOTE_UPGRADE, message);
            }
        }
    }

}
