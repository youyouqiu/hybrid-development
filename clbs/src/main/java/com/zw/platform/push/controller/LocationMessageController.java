package com.zw.platform.push.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.cb.platform.repository.mysqlDao.SpotCheckReportDao;
import com.zw.platform.basic.service.F3MessageService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.ws.entity.common.ClientRequestDescription;
import com.zw.ws.entity.common.ClientWebSocketRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class LocationMessageController {
    private static final Logger log = LogManager.getLogger(LocationMessageController.class);

    @Autowired
    private SpotCheckReportDao spotCheckReportDao;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private F3MessageService f3MessageService;

    /**
     * 订阅车辆位置信息
     */
    @MessageMapping("/location/subscribe")
    public void subscribe(@Header("simpSessionId") String sessionId, SimpMessageHeaderAccessor headerAccessor,
        String msg) {
        try {
            ClientWebSocketRequest<List<String>> clientRequest =
                JSON.parseObject(msg, new TypeReference<ClientWebSocketRequest<List<String>>>() {
                });
            Set<String> monitorIds = new HashSet<>(clientRequest.getData());
            if (CollectionUtils.isEmpty(monitorIds)) {
                return;
            }
            Set<String> deviceIds = monitorService.getDeviceIdByMonitor(monitorIds);
            WsSessionManager.INSTANCE.addPositions(sessionId, deviceIds);

            ClientRequestDescription desc = clientRequest.getDesc();
            String userName = getUsername(headerAccessor);
            List<VehicleSpotCheckInfo> spots = f3MessageService.getCacheLocation(monitorIds, userName, sessionId);
            // App 不加入点击次数统计
            if (!desc.getIsAppFlag() && CollectionUtils.isNotEmpty(spots)) {
                spotCheckReportDao.addVehicleSpotCheckInfoByBatch(spots);
            }
        } catch (Exception e) {
            log.error("订阅实时位置信息遇到错误", e);
        }
    }

    @MessageMapping("/location/unsubscribe")
    public void unsubscribe(@Header("simpSessionId") String sessionId, String msg) {
        try {
            ClientWebSocketRequest<List<String>> wsRequest =
                JSON.parseObject(msg, new TypeReference<ClientWebSocketRequest<List<String>>>() {
                });
            Set<String> monitorIds = new HashSet<>(wsRequest.getData());
            if (CollectionUtils.isEmpty(monitorIds)) {
                return;
            }
            Set<String> deviceIds = monitorService.getDeviceIdByMonitor(monitorIds);
            WsSessionManager.INSTANCE.removePositions(sessionId, deviceIds);
        } catch (Exception e) {
            log.error("订阅实时位置信息遇到错误", e);
        }
    }

    private String getUsername(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        return user == null ? null : user.getName();
    }
}
