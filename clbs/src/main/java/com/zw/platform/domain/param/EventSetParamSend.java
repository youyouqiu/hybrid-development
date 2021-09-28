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
public class EventSetParamSend extends BaseFormBean implements T808MsgBody {
	
    private Integer type;//0：删除终端现有所有事件，该命令后不带后继字节；1：更新事件；2：追加事件；3：修改事件；4：删除特定几项事件，之后事件项中无需带事件内容
    private Integer eventSum;//设置总数
    private Integer packageSum;
    private JSONArray eventList;//事件项列表
}
