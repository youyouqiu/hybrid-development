package com.zw.platform.domain.sendTxt;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * 透传
 * @author  Tdz
 * @create 2017-04-26 16:37
 **/
@Data
public class OriginalOrder implements T808MsgBody {
    private Integer type;
    private byte[] data;
}
