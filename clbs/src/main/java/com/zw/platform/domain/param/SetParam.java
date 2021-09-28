package com.zw.platform.domain.param;

import com.alibaba.fastjson.JSONArray;
import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;
/**
 * Created by FanLu on 2017/4/19.
 */
@Data
public class SetParam  implements T808MsgBody {
    private Integer parametersCount;
    private Integer packageSum;
    private JSONArray paramItems;

}
