package com.zw.adas.repository.mysql.common;

import java.util.List;
import java.util.Map;

/**
 * 功能描述:
 * @author zhengjc
 * @date 2019/6/10
 * @time 14:44
 */
public interface AdasCommonDao {

    /**
     * 获取事件id和名称
     * @return
     */
    List<Map<String, String>> getEventMap();

    /**
     * 获取风险的等级数字和名称
     * @return
     */
    List<Map<String, String>> getRiskLevelMap();

    /**
     * 获取风险事件关联的808id
     * @return
     */
    List<Map<String, String>> getEvent808Map();

    List<Map<String, String>> queryEventCommonFieldAndName();

    List<Map<String, String>> queryCommonFieldEvents();

}
