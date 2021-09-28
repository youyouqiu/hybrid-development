package com.zw.app.service.webMaster.alarmType;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


/**
 * @author lijie
 * @date 2018/8/28 09:19
 */
public interface WebMasterAlarmTypeService {

    JSONObject getAlarmType(String groupId);//根据组织id得到配置的报警类型

    Boolean updateAlarmType(JSONArray jsonArray, String groupId);//修改报警配置

    Boolean resetAlarmType();//恢复报警配置为组织默认值

    Boolean defaultAlarmType();//设置报警配置为组织默认值

    JSONObject referenceGroup();//获取参考组织信息
}
