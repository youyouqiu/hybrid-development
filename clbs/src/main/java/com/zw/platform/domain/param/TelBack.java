package com.zw.platform.domain.param;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * 电话回拨
 *
 * @author  Tdz
 * @create 2017-04-21 11:45
 **/
@Data
public class TelBack implements T808MsgBody{
    private Integer sign;
    private String mobile;
}
