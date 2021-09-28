package com.zw.app.service.monitor;

import com.alibaba.fastjson.JSONObject;


/**
 * @author CJY
 */
public interface MonitorHardwareDataService {
    /**
     * 获取监控对象停止/行驶数据
     * @param monitorId 监控对象id
     * @param startTime 查询开始时间
     * @param endTime 查询结束时间
     * @return 停止和行驶数据
     * @throws Exception 运行时异常
     */
    JSONObject getStopHistoryData(String monitorId, String startTime, String endTime) throws Exception;
}
