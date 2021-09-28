package com.zw.platform.domain.sendTxt;

import com.alibaba.fastjson.JSONObject;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;


/**
 * 行驶记录参数下发
 */
@Data
public class RecordSend implements T808MsgBody {

    private JSONObject data = new JSONObject();
    private Integer cw;

}
