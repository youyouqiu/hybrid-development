package com.zw.platform.push.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.cb.platform.repository.mysqlDao.SpotCheckReportDao;
import com.zw.platform.basic.service.F3MessageService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.push.common.OnlineUserManager;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.push.handler.common.CommonDataOperation;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.OutputControlSettingDO;
import com.zw.ws.entity.common.ClientRequestDescription;
import com.zw.ws.entity.common.ClientWebSocketRequest;
import com.zw.ws.entity.device.DeviceWakeUpEntity;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 客户端实时消息处理控制器
 * @author jiangxiaoqiang 指导者: wangying
 */

@Controller
@Log4j2
public class InstanceMessageController {

    public static String userName = "";

    @Autowired
    private CommonDataOperation commonDataOperation;

    @Autowired
    private ServerParamList serverParamList;

    @Autowired
    private WebSocketMessageDispatchCenter webSocketMessageDispatchCenter;

    @Autowired
    private UserService userService;

    @Autowired
    private SpotCheckReportDao spotCheckReportDao;

    @Autowired
    private F3MessageService f3MessageService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @MessageMapping("/vehicle/unsubscribestatus")
    public void unsubscribeVehicleStatus(SimpMessageHeaderAccessor headerAccessor, String requestContent) {
        //do nothing
    }

    /**
     * 订阅车辆位置信息
     */
    @MessageMapping("/vehicle/location")
    public void subscribeVehicleLocation(String msg, @Header("simpSessionId") String sessionId,
        SimpMessageHeaderAccessor headerAccessor) {
        try {
            ClientWebSocketRequest<List<String>> clientRequest =
                JSON.parseObject(msg, new TypeReference<ClientWebSocketRequest<List<String>>>() {
                });
            ClientRequestDescription desc = clientRequest.getDesc();
            String userName = getUsername(headerAccessor);
            Integer isObd = desc.getIsObd();
            Set<String> set = new HashSet<>(clientRequest.getData());
            //App订阅只推送obd信息
            if (isObd != null && isObd == 1) {
                f3MessageService.pushCacheOboInfo(set, userName);
                if (!set.isEmpty()) {
                    Set<String> deviceIds = monitorService.getDeviceIdByMonitor(set);
                    WebSubscribeManager.getInstance().subscribeObd(userName, deviceIds);
                }
                return;
            }
            List<VehicleSpotCheckInfo> vehicleSpotCheckInfos =
                f3MessageService.getCacheLocation(set, userName, sessionId);
            // App 不加入点击次数统计
            if (!desc.getIsAppFlag() && CollectionUtils.isNotEmpty(vehicleSpotCheckInfos)) {
                spotCheckReportDao.addVehicleSpotCheckInfoByBatch(vehicleSpotCheckInfos);
            }
            if (CollectionUtils.isNotEmpty(set)) {
                Set<String> deviceIds = monitorService.getDeviceIdByMonitor(set);
                WsSessionManager.INSTANCE.addPositions(sessionId, deviceIds);
            }
        } catch (Exception e) {
            log.error("订阅实时位置信息遇到错误", e);
        }
    }

    /**
     * 位置信息查询
     * @author Liubangquan
     */
    @MessageMapping("/vehicle/realLocation")
    public void subscribeVehicleRealTimeLocation(@Header("simpSessionId") String sessionId, String requestContent) {
        try {
            String desc = String.valueOf(JSON.parseObject(requestContent).get("desc"));

            //cmsgSN 不为空代表app的点名下发后订阅应答的0201
            String cmsgSN = String.valueOf(JSON.parseObject(desc).get("cmsgSN"));
            if (StringUtils.isNotEmpty(cmsgSN)) {
                UserCacheA.put(cmsgSN, sessionId);
                return;
            }

            userName = String.valueOf(JSON.parseObject(desc).get("UserName"));
        } catch (Exception e) {
            log.error("订阅车辆位置信息遇到错误", e);
        }
    }

    /**
     * 获取sim卡信息，缓存用户和流水号状态
     */
    @MessageMapping("/vehicle/realLocationP")
    public void subscribeVehicleRealTimeLocationP(String requestContent) {
        try {
            String desc = String.valueOf(JSON.parseObject(requestContent).get("desc"));
            String userN = String.valueOf(JSON.parseObject(desc).get("UserName"));
            String userA = String.valueOf(JSON.parseObject(desc).get("cmsgSN"));
            UserCache.put(userA, userN);
        } catch (Exception e) {
            log.error("订阅车辆位置信息遇到错误", e);
        }
    }

    /**
     * 点名
     */
    @MessageMapping("/vehicle/realLocationS")
    public void subscribeVehicleRealTimeLocationS(String requestContent) {
        setMap(requestContent);
    }

    /**
     * 读取资源列表
     */
    @MessageMapping("/vehicle/resourceList")
    public void subscribeVideo(String requestContent) {
        setMap(requestContent);
    }

    public void setMap(String requestContent) {
        try {
            String desc = String.valueOf(JSON.parseObject(requestContent).get("desc"));
            String userN = String.valueOf(JSON.parseObject(desc).get("UserName"));
            String userA = String.valueOf(JSON.parseObject(desc).get("cmsgSN"));
            UserCacheS.put(userA, userN);
        } catch (Exception e) {
            log.error("订阅车辆位置信息遇到错误", e);
        }
    }

    @MessageMapping("/vehicle/unsubscribeRealLocation")
    public void unsubscribeRealLocation() {
        try {
            userName = "";
        } catch (Exception e) {
            log.error("取消订阅实时状态信息遇到错误", e);
        }
    }

    @MessageMapping("/vehicle/unsubscribelocation")
    public void unsubscribeVehicleLocation(SimpMessageHeaderAccessor headerAccessor, String requestContent) {
        try {
            ClientWebSocketRequest<List<String>> wsRequest =
                JSON.parseObject(requestContent, new TypeReference<ClientWebSocketRequest<List<String>>>() {
                });
            Integer isObd = wsRequest.getDesc().getIsObd();
            String user = wsRequest.getDesc().getUserName();
            Set<String> set = new HashSet<>(wsRequest.getData());
            if (isObd != null && isObd == 1) {
                Set<String> deviceIds = monitorService.getDeviceIdByMonitor(set);
                WebSubscribeManager.getInstance().canSubscribeObd(user, deviceIds);
                return;
            }
            if (!set.isEmpty()) {
                Set<String> deviceIds = monitorService.getDeviceIdByMonitor(set);
                WsSessionManager.INSTANCE.removePositions(headerAccessor.getSessionId(), deviceIds);
            }
        } catch (Exception e) {
            log.error("订阅实时位置信息遇到错误", e);
        }
    }

    /**
     * 订阅缓存状态
     */
    @MessageMapping("/vehicle/subscribeCacheStatusNew")
    public void subscribeCacheStatusNew(String requestContent) {
        try {
            String cacheStatusInfo = commonDataOperation.getCacheStatusInfo(requestContent);
            String desc = String.valueOf(JSON.parseObject(requestContent).get("desc"));
            String subscribeUser = String.valueOf(JSON.parseObject(desc).get("UserName"));
            webSocketMessageDispatchCenter.pushCacheStatusInfo(subscribeUser, cacheStatusInfo);
        } catch (Exception e) {
            log.error("订阅缓存状态信息遇到错误", e);
        }
    }

    /**
     * 地图订阅缓存状态
     */
    @MessageMapping("/vehicle/subVehicleStatusNew")
    public void subVehicleStatusNew(@Header("simpSessionId") String sessionId, String msg) {
        try {
            ClientWebSocketRequest<List<String>> clientWebSocketRequest =
                JSON.parseObject(msg, new TypeReference<ClientWebSocketRequest<List<String>>>() {
                });
            Set<String> vehicleIds = new HashSet<>(clientWebSocketRequest.getData());
            WsSessionManager.INSTANCE.addStatuses(sessionId, vehicleIds);
        } catch (Exception e) {
            log.error("地图订阅缓存状态信息遇到错误", e);
        }
    }

    /**
     * 订阅当前用户状态
     */
    @MessageMapping("/vehicle/subscribeStatus")
    public void subscribeStatus(SimpMessageHeaderAccessor headerAccessor, String requestContent) {
        String userName = headerAccessor.getUser().getName();
        Set<String> vids = userService.getMonitorIdsByUser(userName);
        final String sessionId = headerAccessor.getSessionId();
        WsSessionManager.INSTANCE.addStatuses(sessionId, vids);
    }

    @MessageMapping("/vehicle/check")
    public void check(SimpMessageHeaderAccessor headerAccessor) {
        String user = headerAccessor.getUser().getName();
        WebSubscribeManager.getInstance().startCheck(user);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
            .writeAndFlush(com.zw.platform.util.MsgUtil.getMsg(ConstantUtil.WEB_809_CHECK_SERVER_STATUS_REQ, null));
    }

    @MessageMapping("/vehicle/inspect")
    public void inspect(SimpMessageHeaderAccessor headerAccessor) {
        String user = headerAccessor.getUser().getName();
        WebSubscribeManager.getInstance().startCheck(user);
    }

    @MessageMapping("/vehicle/oil/setting")
    public void subscriOilSetting(SimpMessageHeaderAccessor headerAccessor, String requestContent) {
        //do nothing
    }

    /**
     * 服务端心跳接收
     */
    @MessageMapping("isHealth")
    private void isHealth(SimpMessageHeaderAccessor headerAccessor, String requestContent) {
        String user = headerAccessor.getUser().getName();
        if (!onlineUserManager.isOnline(user)) {
            onlineUserManager.recordOnline(user);
        }
    }

    @MessageMapping("ackHealth")
    private void ackHealth(SimpMessageHeaderAccessor headerAccessor, String requestContent) {
        Principal user = headerAccessor.getUser();
        if (user == null) {
            return;
        }
        onlineUserManager.refresh(user.getName());
    }

    /**
     * 订阅车辆位置信息
     */
    @MessageMapping("/guide/vehicle/location")
    public void subscribeGuideVehicleLocation(@Header("simpSessionId") String sessionId, String requestContent) {
        try {
            ClientWebSocketRequest<List<String>> clientRequest =
                JSON.parseObject(requestContent, new TypeReference<ClientWebSocketRequest<List<String>>>() {
                });
            Set<String> set = new HashSet<>(clientRequest.getData());
            if (CollectionUtils.isNotEmpty(set)) {
                Set<String> deviceIds = monitorService.getDeviceIdByMonitor(set);
                // WebSubscribeManager.getInstance().subscribePosition(userName, deviceIds);
                WsSessionManager.INSTANCE.addPositions(sessionId, deviceIds);
            }
        } catch (Exception e) {
            log.error("订阅实时位置信息遇到错误", e);
        }
    }

    /**
     * 取消订阅缓存状态
     */
    @MessageMapping("/vehicle/unsubscribestatusNew")
    public void unsubscribeVehicleStatusNew(SimpMessageHeaderAccessor headerAccessor, String requestContent) {
        try {
            ClientWebSocketRequest<List<ClientVehicleInfo>> clientRequest =
                JSON.parseObject(requestContent, new TypeReference<ClientWebSocketRequest<List<ClientVehicleInfo>>>() {
                });
            ClientVehicleInfo obj;
            Set<String> set = new HashSet<>();
            for (int i = 0; i < clientRequest.getData().size(); i++) {
                obj = clientRequest.getData().get(i);
                set.add(obj.getVehicleId());
            }
            if (CollectionUtils.isNotEmpty(set)) {
                WsSessionManager.INSTANCE.removeStatuses(headerAccessor.getSessionId(), set);
            }
        } catch (Exception e) {
            log.error("取消缓存状态信息遇到错误", e);
        }
    }

    /**
     * 取消订阅车辆位置信息
     */
    @MessageMapping("/vehicle/unsubscribelocationNew")
    public void unsubscribeVehicleLocationNew(@Header("simpSessionId") String sessionId, String requestContent) {
        try {
            ClientWebSocketRequest<List<ClientVehicleInfo>> wsRequest =
                JSON.parseObject(requestContent, new TypeReference<ClientWebSocketRequest<List<ClientVehicleInfo>>>() {
                });
            ClientVehicleInfo obj;
            Set<String> set = new HashSet<>();
            for (int i = 0; i < wsRequest.getData().size(); i++) {
                obj = wsRequest.getData().get(i);
                set.add(obj.getVehicleId());
            }
            if (CollectionUtils.isNotEmpty(set)) {
                Set<String> deviceIds = monitorService.getDeviceIdByMonitor(set);
                WsSessionManager.INSTANCE.removePositions(sessionId, deviceIds);
            }
        } catch (Exception e) {
            log.error("取消订阅实时位置信息遇到错误", e);
        }
    }

    @MessageExceptionHandler
    @SendToUser(destinations = { "/topic/errors" }, broadcast = false)
    public String handleException(Exception exception) {
        return exception.getMessage();
    }

    /**
     * 存储websocket sessionId
     * APP2.0.0新增
     */
    @MessageMapping("/expire")
    public void expire(SimpMessageHeaderAccessor headerAccessor, String requestContent) {
        try {
            ClientWebSocketRequest<List<ClientVehicleInfo>> request =
                JSON.parseObject(requestContent, new TypeReference<ClientWebSocketRequest<List<ClientVehicleInfo>>>() {
                });
            String userName = request.getDesc().getUserName();
            String sessionId = headerAccessor.getSessionId();
            if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(sessionId)) {
                WebSubscribeManager.getInstance().addUserSessionId(userName, sessionId);
            }
        } catch (Exception e) {
            log.error("存储websocketSessionId遇到错误", e);
        }
    }

    /**
     * 终端唤醒
     */
    @MessageMapping("/device/wakeUp")
    public void sendDeviceWakeUp(@Header("simpSessionId") String sessionId, String requestContent) {
        try {
            ClientWebSocketRequest<DeviceWakeUpEntity> clientRequest =
                JSON.parseObject(requestContent, new TypeReference<ClientWebSocketRequest<DeviceWakeUpEntity>>() {
                });
            DeviceWakeUpEntity deviceWakeUpEntity = clientRequest.getData();
            String monitorId = deviceWakeUpEntity.getMonitorId();
            Integer wakeUpDuration = deviceWakeUpEntity.getWakeUpDuration();
            if (monitorId == null || wakeUpDuration == null || wakeUpDuration < 0 || wakeUpDuration > 1000) {
                log.error("下发终端唤醒指令参数错误:" + JSON.toJSONString(deviceWakeUpEntity));
                return;
            }
            sendTxtService.sendDeviceWakeUp(deviceWakeUpEntity, sessionId);
        } catch (Exception e) {
            log.error("下发终端唤醒指令异常", e);
        }
    }

    /**
     * 油补 发送补发数据请求
     */
    @MessageMapping("/oilSupplement/sendReissueDataRequest")
    public void sendReissueDataRequest(@Header("simpSessionId") String sessionId, String requestContent) {
        try {
            JSONObject data = JSON.parseObject(requestContent).getJSONObject("data");
            if (data == null) {
                return;
            }
            String startTime = data.getString("startTime");
            String endTime = data.getString("endTime");
            JSONArray monitorIdJsonArr = data.getJSONArray("monitorIds");
            if (StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)
                || monitorIdJsonArr == null || monitorIdJsonArr.size() <= 0) {
                return;
            }
            Date startTimeDate = DateUtil.getStringToDate(startTime, null);
            Date endTimeDate = DateUtil.getStringToDate(endTime, null);
            if (startTimeDate == null || endTimeDate == null) {
                return;
            }
            List<String> monitorIds =
                monitorIdJsonArr.stream().map(Object::toString).distinct().collect(Collectors.toList());
            f3MessageService.sendReissueDataRequest(monitorIds, sessionId, startTimeDate, endTimeDate);
        } catch (Exception e) {
            log.error("发送补发数据请求错误", e);
        }
    }

    /**
     * io监测:输出控制(io控制/断油电)
     */
    @MessageMapping("/ioMonitoring/send8500")
    public void saveAndSendOutputControl(@Header("simpSessionId") String sessionId, String requestContent) {
        try {
            OutputControlSettingDO settingDO =
                JSON.parseObject(requestContent).getObject("data", OutputControlSettingDO.class);
            String vehicleId = settingDO.getVehicleId();
            Integer outletSet = settingDO.getOutletSet();
            Integer peripheralId = settingDO.getPeripheralId();
            if (StringUtils.isBlank(vehicleId) || outletSet == null || peripheralId == null) {
                log.error("输出控制-下发8500参数异常:参数为空");
                return;
            }
            f3MessageService.saveAndSendOutputControl(sessionId, settingDO);
        } catch (Exception e) {
            log.error("输出控制请求异常", e);
        }
    }

    private String getUsername(SimpMessageHeaderAccessor headerAccessor) {
        Principal user = headerAccessor.getUser();
        return user == null ? null : user.getName();
    }
}
