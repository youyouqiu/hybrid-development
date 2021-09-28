package com.zw.platform.domain.param;

import com.alibaba.fastjson.JSONArray;
import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;

@Data
public class PhoneBookParamSend implements T808MsgBody {
    private Integer type;//控制标志
    private Integer linkmanSum;//联系人总数
    private Integer packageSum;
    private JSONArray linkmanList;//联系人项

}

