package com.zw.protocol.msg.t809.body;

import com.alibaba.fastjson.JSONObject;
import com.zw.protocol.msg.t809.T809MsgBody;
import lombok.Data;

/**
 * Created by LiaoYuecai on 2017/2/14.
 */
@Data
public class ExchangeInfo implements T809MsgBody {
    protected Integer dataType;
    protected Integer dataLength;
    private JSONObject data;
}
