package com.zw.platform.service.core;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.common.DirectiveStatusEnum;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * F3下发状态处理
 */
@Service
public class F3SendStatusProcessService {
    public static final Logger logger = LogManager.getLogger(F3SendStatusProcessService.class);

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ParamSendingCache paramSendingCache;

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private DelayedEventTrigger trigger;

    /**
     * 更新参数下发表状态，如果3分钟设备没有回应，将状态设为1，即指令未生效
     * @param sendParam 类型 1 处理 0x0001(60秒)和0x0900(90秒)
     *                  类型 2 处理 0x0001 (60秒)
     *                  类型 3 处理 0x1014 (60秒)
     * @param type
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    public void updateSendParam(SendParam sendParam, Integer type) {
        logger.debug("F3SendStatusProcessService-processSomething" + Thread.currentThread() + "......start");
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(sendParam.getVehicleId());
        if (bindDTO == null) {
            return;
        }
        String deviceId = bindDTO.getDeviceId();
        String simCardNumber = bindDTO.getSimCardNumber();
        String paramId = sendParam.getParamId();
        switch (type) {
            case 1:
                //如果60s内没有收到0100或0100返回失败，则是“参数下发失败”
                trigger.addEvent(60, TimeUnit.SECONDS, () -> {
                    int firstStatus = this.getSendStatus(paramId);
                    if (firstStatus == DirectiveStatusEnum.ISSUED.getNum()) {
                        //下发失败
                        parameterDao.updateStatusByMsgSN(sendParam.getMsgSNACK(), sendParam.getVehicleId(),
                                DirectiveStatusEnum.IS_FAILED.getNum());
                        pushClientMsg(sendParam.getMsgSNACK() + "", 0x0001, sendParam.getMsgId(), deviceId,
                                simCardNumber);
                    } else {
                        //按原逻辑，60S判断条件不满足时，另加一个90S的延时事件
                        trigger.addEvent(90, TimeUnit.SECONDS, () -> {
                            int secondStatus = this.getSendStatus(paramId);
                            if (secondStatus == DirectiveStatusEnum.DEVICE_IN_PROCESSING.getNum()
                                    || secondStatus == DirectiveStatusEnum.ISSUED.getNum()) {
                                //下发失败
                                parameterDao.updateStatusByMsgSN(sendParam.getMsgSNACK(),
                                        sendParam.getVehicleId(), DirectiveStatusEnum.IS_NOT_EFFECTED.getNum());
                                pushClientMsg(sendParam.getMsgSNACK() + "", 0x0900, sendParam.getMsgId(), deviceId,
                                        simCardNumber);
                            }
                        });
                    }
                });
                break;
            case 2:
                //如果60s内没有收到0100或0100返回失败，则是“参数下发失败”
                trigger.addEvent(60, TimeUnit.SECONDS, () -> {
                    int status = this.getSendStatus(paramId);
                    if (status == DirectiveStatusEnum.ISSUED.getNum()) {
                        //下发失败
                        parameterDao.updateStatusByMsgSN(sendParam.getMsgSNACK(), sendParam.getVehicleId(),
                                DirectiveStatusEnum.IS_NOT_EFFECTED.getNum());
                        pushClientMsg(sendParam.getMsgSNACK() + "", 0x0001, sendParam.getMsgId(), deviceId,
                                simCardNumber);
                    }
                });
                break;

            case 3:
                //如果10s
                trigger.addEvent(10, TimeUnit.SECONDS, () -> {
                    int status = this.getSendStatus(paramId);
                    if (status == DirectiveStatusEnum.ISSUED.getNum()) {
                        //下发失败
                        parameterDao.updateStatusByMsgSN(sendParam.getMsgSNACK(), sendParam.getVehicleId(),
                                DirectiveStatusEnum.IS_NOT_EFFECTED.getNum());
                        pushClientMsg(sendParam.getMsgSNACK() + "", 0x0104, sendParam.getMsgId(), deviceId,
                                simCardNumber);
                    }
                });
                break;
            //远程升级
            case 4:
                //如果60s内没有收到0100或0100返回失败，则是“参数下发失败”
                trigger.addEvent(60, TimeUnit.SECONDS, () -> {
                    int firstStatus = this.getSendStatus(paramId);
                    if (firstStatus == DirectiveStatusEnum.ISSUED.getNum()) {
                        //下发失败
                        parameterDao.updateStatusByMsgSN(sendParam.getMsgSNACK(), sendParam.getVehicleId(),
                                DirectiveStatusEnum.IS_FAILED.getNum());
                        pushClientMsg(sendParam.getMsgSNACK() + "", 0x0001, sendParam.getMsgId(), deviceId,
                                simCardNumber);
                    } else {
                        //按照原逻辑，60S判断条件不满足时，另加一个20分钟的延时事件
                        trigger.addEvent(20, TimeUnit.MINUTES, () -> {
                            int secondStatus = this.getSendStatus(paramId);
                            if (secondStatus == DirectiveStatusEnum.DEVICE_IN_PROCESSING.getNum()
                                    || secondStatus == DirectiveStatusEnum.ISSUED.getNum()) {
                                //下发失败
                                parameterDao.updateStatusByMsgSN(sendParam.getMsgSNACK(),
                                        sendParam.getVehicleId(), DirectiveStatusEnum.IS_NOT_EFFECTED.getNum());
                                pushClientMsg(sendParam.getMsgSNACK() + "", 0x0900, sendParam.getMsgId(), deviceId,
                                        simCardNumber);
                            }
                        });
                    }
                });
                break;
                // 终端唤醒
            case 5:
                //如果10s内没有收到应答，则是“唤醒失败(指令未生效)”
                trigger.addEvent(60, TimeUnit.SECONDS, () -> {
                    int status = this.getSendStatus(paramId);
                    if (status == DirectiveStatusEnum.ISSUED.getNum()) {
                        WebSubscribeManager.getInstance()
                            .cancleSubDeviceWakeUpAck(sendParam.getVehicleId(), sendParam.getMsgSNACK());
                        //下发失败
                        parameterDao.updateStatusByMsgSN(sendParam.getMsgSNACK(), sendParam.getVehicleId(),
                            DirectiveStatusEnum.IS_NOT_EFFECTED.getNum());
                        sendStatusMsgBySessionId(sendParam.getSessionId(), ConstantUtil.WEBSOCKET_DEVICE_WAKE_UP,
                            DirectiveStatusEnum.IS_NOT_EFFECTED.getNum());
                    }
                });
                break;
            default:
                break;
        }
        logger.debug("F3SendStatusProcessService-processSomething" + Thread.currentThread() + "......end");
    }

    private  void sendStatusMsgBySessionId(String sessionId, String destination, Object obj) {
        final SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setLeaveMutable(true);
        headerAccessor.setSessionId(sessionId);
        simpMessagingTemplate.convertAndSendToUser(sessionId, destination, obj, headerAccessor.getMessageHeaders());
    }

    /**
     * 推送消息
     * @param msgSn     消息编号
     * @param respMsgId 返回消息编号
     * @param sendMsgId 下发消息编号
     */
    private void pushClientMsg(String msgSn, int respMsgId, String sendMsgId, String deviceId, String simCardNumber) {
        if (simpMessagingTemplate == null) {
            return;
        }
        String reviceMsg = createResponseMsg(respMsgId, msgSn, deviceId);
        SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSn, deviceId);
        if (info == null || info.getUserName() == null || "".equals(info.getUserName())) {
            if (respMsgId == 0x0104) {
                simpMessagingTemplate.convertAndSend("/topic/oil" + sendMsgId + "Info", reviceMsg);
            } else {
                simpMessagingTemplate.convertAndSend("/topic/oilF30900SendStatus", reviceMsg);
            }
        } else {
            String userS = info.getUserName();
            if (userS != null && !"".equals(userS)) {
                if (respMsgId == 0x0104) {
                    simpMessagingTemplate.convertAndSendToUser(userS, "/topic/oil" + sendMsgId + "Info", reviceMsg);
                } else if (respMsgId == 0x0001) {
                    simpMessagingTemplate.convertAndSendToUser(userS, "/topic/t808_currency_response", reviceMsg);
                } else if (respMsgId == 0x0900) {
                    simpMessagingTemplate.convertAndSendToUser(userS, "/topic/deviceReportLog", reviceMsg);
                } else {
                    simpMessagingTemplate.convertAndSendToUser(userS, "/topic/oilF30900SendStatus", reviceMsg);
                }
            }
        }

        // 发送到客户端  推送所有状态
        if (respMsgId != 0x0104) {
            //推送通用应答标志
            boolean sendFlag = true;
            if (info != null) {
                String userName = info.getUserName();
                if (StrUtil.areNotBlank(userName, msgSn, simCardNumber)) {
                    String moduleKey = paramSendingCache.getKey(userName, Integer.valueOf(msgSn), simCardNumber);
                    SendTarget sendTarget = paramSendingCache.get(moduleKey);
                    if (sendTarget != null) {
                        reviceMsg = createResponseMsg(respMsgId, msgSn, sendTarget.getSubModule(), deviceId);
                        simpMessagingTemplate
                            .convertAndSendToUser(userName, "/topic/" + sendTarget.getTargetUrl(), reviceMsg);
                        paramSendingCache.remove(moduleKey);
                        //结束当前推送
                        sendFlag = false;
                    }
                }
            }

            if (sendFlag) {
                simpMessagingTemplate.convertAndSend(ConstantUtil.WEBSOCKET_REFRESH_LIST, reviceMsg);
            }

        }
    }

    /**
     * 获取下发状态
     * @param id
     * @return
     */
    private Integer getSendStatus(String id) {
        List<String> ids = new ArrayList<>();
        ids.add(id);
        List<Directive> directives = this.parameterDao.findById(ids);
        if (directives != null && directives.size() > 0) {
            Directive directive = directives.get(0);
            return directive.getStatus();
        }
        return 0;
    }

    private String createResponseMsg(int respMsgId, String msgSn, String deviceId) {
        return createResponseMsgData(respMsgId, msgSn, null, deviceId);

    }

    /**
     * @param respMsgId 返回的消息编号
     * @param msgSn     发话消息办好
     * @return
     */
    private String createResponseMsg(int respMsgId, String msgSn, String subModule, String deviceId) {
        return createResponseMsgData(respMsgId, msgSn, subModule, deviceId);

    }

    /**
     * @param respMsgId 返回的消息编号
     * @param msgSn     发话消息办好
     * @return
     */
    private String createResponseMsgData(int respMsgId, String msgSn, String subModule, String deviceId) {
        JSONObject msgBody = new JSONObject();
        if (respMsgId == 0x0900) {
            msgBody.put("type", 0xF3);
            msgBody.put("data_result", "终端未返回数据");
        }
        msgBody.put("msgSNACK", msgSn);
        msgBody.put("ackMSN", msgSn);
        msgBody.put("result", 1);
        if (StrUtil.isNotBlank(subModule)) {
            msgBody.put("subModule", subModule);
        }
        JSONObject msg = new JSONObject();
        JSONObject desc = new JSONObject();
        desc.put("msgID", respMsgId);
        desc.put("uuid", String.valueOf(UUID.randomUUID()));
        desc.put("monitorId", newConfigDao.getVehicleIdByDeviceId(deviceId));
        msg.put("desc", desc);
        JSONObject data = new JSONObject();
        JSONObject msgHead = new JSONObject();
        msgHead.put("msgID", respMsgId);
        msgHead.put("msgSN", msgSn);
        data.put("msgHead", msgHead);

        data.put("msgBody", msgBody);
        msg.put("data", data);
        return msg.toJSONString();

    }

}
