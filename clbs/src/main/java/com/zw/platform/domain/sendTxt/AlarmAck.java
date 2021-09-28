package com.zw.platform.domain.sendTxt;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/4/6.
 */
@Data
public class AlarmAck  implements T808MsgBody{
    private Integer msgSNACK;//报警消息流水号
    private Integer type;//人工确认报警类型
}
