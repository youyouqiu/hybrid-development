package com.zw.platform.domain.param;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.protocol.msg.t808.T808MsgBody;

import lombok.Data;
import lombok.EqualsAndHashCode;
/**
 * Created by FanLu on 2017/4/19.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class InformationParamSend extends BaseFormBean implements T808MsgBody {
    private Integer type;//设置类型
    private Integer infoSum;//信息项总数
    private Integer packageSum;
    private JSONArray infoList;//信息项列表
}
