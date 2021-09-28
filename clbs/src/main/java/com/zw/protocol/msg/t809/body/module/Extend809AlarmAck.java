package com.zw.protocol.msg.t809.body.module;

import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;


/**
 * 西藏扩展809报警督办应答
 */
@Data
public class Extend809AlarmAck implements T809MsgBody {
    private Integer infoId; // 信息id
    private Integer result;
    private String dutyman; // 督办响应用户
}
