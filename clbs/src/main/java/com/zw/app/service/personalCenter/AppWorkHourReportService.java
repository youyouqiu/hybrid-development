package com.zw.app.service.personalCenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/7/12 9:58
 */
public interface AppWorkHourReportService {

    /**
     * 判断用户是否拥有下发了工时传感器轮询的监控对象
     * @return
     * @throws Exception
     */
    boolean judgeUserIfOwnSendWorkHourPollsMonitor() throws Exception;

    boolean judgeUserIfOwnSendPollsMonitor(List<String> sensorIdList) throws Exception;


    /**
     * 获得下发了工时传感器轮询的监控对象信息
     * @param page
     * @param pageSize
     * @param defaultSize
     * @return
     * @throws Exception
     */
    JSONObject getSendWorkHourPollsMonitorInfoSeven(Long page, Long pageSize, Long defaultSize)
        throws Exception;

    /**
     * 根据传感器id获取轮询了该类型传感器的监控对象
     */
    JSONObject getSendSensorPollingMonitorInfoSeven(Long page, Long pageSize, Long defaultSize,
        List<String> sensorId) throws Exception;

    /**
     * 查询App工时统计信息
     * @param monitorIds
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JSONArray getWorkHourStatisticsInfo(String monitorIds, String startTime, String endTime, Integer sensorNo)
        throws Exception;
}
