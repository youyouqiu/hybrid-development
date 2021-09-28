package com.zw.adas.domain.riskManagement;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

/**
 * @author XK
 */
@Data
public class SendAlarmMessage implements T808MsgBody {
    /**
     * 报警类型ID
     */
    private Integer alarmType;

    /**
     * 报警级别
     */
    private Integer level;

    /**
     * 事件开始时间 格式:yyMMddHHmmss
     */
    private String startTime;

    /**
     * 事件结束时间 格式:yyMMddHHmmss
     */
    private String endTime;

}
