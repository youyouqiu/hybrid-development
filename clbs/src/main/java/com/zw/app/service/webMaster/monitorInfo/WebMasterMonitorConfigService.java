package com.zw.app.service.webMaster.monitorInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author lijie
 * @date 2019/9/27 15:19
 */
public interface WebMasterMonitorConfigService {

    JSONObject getMonitorConfig(String groupId);//根据组织id得到监控对象显示配置

    Boolean updateMonitorConfig(JSONArray jsonArray, String groupId);//修改监控对象显示配置

    Boolean resetMonitorConfig();//恢复监控对象显示配置为组织默认值

    Boolean defaultMonitorConfig();//设置监控对象显示配置为组织默认值

    JSONObject referenceGroup(String type);//获取参考组织信息
}
