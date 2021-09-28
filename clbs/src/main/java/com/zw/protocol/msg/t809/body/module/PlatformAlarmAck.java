package com.zw.protocol.msg.t809.body.module;

import com.zw.platform.domain.reportManagement.T809AlarmForwardInfoMiddleQuery;
import com.zw.platform.domain.reportManagement.Zw809MessageDO;
import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/**
 * 报警督办应答消息体
 */
@Data
public class PlatformAlarmAck implements T809MsgBody {
    private Integer supervisionId;
    private Integer result;
    private Integer sourceDataType;
    private Integer sourceMsgSn;
    private Integer msgSn;

    public static PlatformAlarmAck getInstance(String handleTpe, T809AlarmForwardInfoMiddleQuery alarmInfo) {
        PlatformAlarmAck ack = new PlatformAlarmAck();
        // 报警编号
        ack.supervisionId = alarmInfo.getMsgSn();
        ack.result = getResultFromHandleType(handleTpe);
        ack.sourceMsgSn = alarmInfo.getMsgSn();
        ack.sourceDataType = alarmInfo.getMsgId();
        return ack;
    }

    public static PlatformAlarmAck getInstance(PlatformAlarmInfo info) {
        PlatformAlarmAck ack = new PlatformAlarmAck();
        ack.supervisionId = info.getSourceMsgSn();
        // 处理结果
        ack.result = info.getAlarmHandle();
        ack.sourceDataType = info.getSourceDataType();
        ack.sourceMsgSn = info.getSourceMsgSn();
        ack.msgSn = info.getMsgSn();
        return ack;
    }

    public static PlatformAlarmAck getInstance(Zw809MessageDO zw809MessageDO, PlatformAlarmInfo info) {
        PlatformAlarmAck ack = new PlatformAlarmAck();
        ack.supervisionId = zw809MessageDO.getSupervisionId();
        // 处理结果
        ack.result = info.getAlarmHandle();
        ack.sourceDataType = zw809MessageDO.getSourceDataType();
        ack.sourceMsgSn = zw809MessageDO.getSourceMsgSn();
        ack.msgSn = zw809MessageDO.getMsgSn();
        return ack;
    }

    /**
     * 非川冀标协议
     * @param handleType
     * @return
     */
    protected static Integer getResultFromHandleType(String handleType) {
        switch (handleType) {
            case "拍照":
            case "下发短信":
                return 0;
            case "人工确认报警":
                return 1;
            case "不做处理":
                return 2;
            case "将来处理":
                return 3;
            default:
                try {
                    return Integer.parseInt(handleType);
                } catch (Exception e) {
                    return 0;
                }
        }
    }
}
