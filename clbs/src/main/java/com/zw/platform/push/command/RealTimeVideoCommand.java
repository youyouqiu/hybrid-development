package com.zw.platform.push.command;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.push.factory.AlarmChainHandler;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import com.zw.ws.common.WebSocketMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 实时监控跳转
 * @author Chen Feng
 * @version 1.0 2018/1/4
 */

@Slf4j
@Component
public class RealTimeVideoCommand implements AlarmChainHandler {

    /**
     * key: 报警编号, value: 报警名称
     */
    private static final Map<Integer, String> ALARM_CODE_NAME_MAP = new HashMap<>(255);

    @Autowired
    private WebSocketMessageDispatchCenter webSocketMessageDispatchCenter;

    @Override
    public void handle(AlarmMessageDTO alarmMessageDTO) {
        final String monitorId = alarmMessageDTO.getMonitorId();
        final Integer alarmType = alarmMessageDTO.getAlarmType();
        Message message = new Message();
        message.setData(realtimeVideoReminderMessage(monitorId, alarmType));
        final MsgDesc msgDesc = new MsgDesc();
        msgDesc.setMonitorId(monitorId);
        message.setDesc(msgDesc);
        webSocketMessageDispatchCenter.pushMessageToAllClient(monitorId, message, WebSocketMessageType.SPECIAL_REPORT);
    }

    private String realtimeVideoReminderMessage(String monitorId, Integer alarmType) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(monitorId, "name");
        String monitorName = Objects.isNull(bindDTO) ? "" : bindDTO.getName();
        return "监控对象:" + monitorName + "发现" + getAlarmName(alarmType);
    }

    private String getAlarmName(Integer alarmType) {
        return ALARM_CODE_NAME_MAP.computeIfAbsent(alarmType, AlarmTypeUtil::getAlarmType);
    }
}
