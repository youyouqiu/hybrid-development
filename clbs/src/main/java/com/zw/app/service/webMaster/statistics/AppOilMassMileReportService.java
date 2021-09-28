package com.zw.app.service.webMaster.statistics;

import com.alibaba.fastjson.JSONObject;


public interface AppOilMassMileReportService {
    /**
     * 获取里程油量数据
     */
    JSONObject getOilMassMileData(String monitorIds, String startTime, String endTime) throws Exception;

    /**
     * 判断用户权限内是否有轮询了油量传感器的监控对象
     */
    boolean judgeUserPollingOilMassMonitor() throws Exception;


    /**
     * 获取用户权限内轮询了油量传感器的监控对象
     */
    JSONObject getPollingOilMassMonitorSeven(Long page, Long pageSize, Long defaultSize) throws Exception;
}
