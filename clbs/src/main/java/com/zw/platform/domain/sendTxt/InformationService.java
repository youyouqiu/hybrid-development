package com.zw.platform.domain.sendTxt;

import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

/**
 * 信息服务
 *
 * @author  Tdz
 * @create 2017-05-11 11:12
 **/
@Data
public class InformationService implements T808MsgBody{
    private Integer type;
    private Integer len;
    private Integer packageSum;
    private String value;
}
