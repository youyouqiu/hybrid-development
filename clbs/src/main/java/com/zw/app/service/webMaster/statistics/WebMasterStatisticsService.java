package com.zw.app.service.webMaster.statistics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author lijie
 * @date 2018/12/07 17:49
 */
public interface WebMasterStatisticsService {

    JSONObject getStatistics(String id);//根据组织id得到综合统计配置

    Boolean updateStatisticsConfig(JSONArray jsonArray, String groupId);//修改综合统计配置

    Boolean resetStatisticsConfig();//恢复综合统计配置为组织默认值

    Boolean defaultStatisticsConfig();//设置综合统计配置为组织默认值

    JSONObject referenceGroup();//获取参考组织信息

}
