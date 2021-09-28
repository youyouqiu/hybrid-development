package com.zw.lkyw.service.realTimeMonitoring;

import com.zw.lkyw.domain.ReportMenu;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;
import java.util.Map;

/***
 @Author zhengjc
 @Date 2019/12/30 10:13
 @Description 两客一危实时监控service
 @version 1.0
 **/
public interface LkywRealTimeMonitoringService {
    Map<String, List<ReportMenu>> getUerReportMenu();

    /**
     * 查询报警记录
     * @param latestTime         列表中最新的时间
     * @param latestAlarmDataStr 列表中报警时间为最新时间的报警数据
     * @param oldestTime         列表中最老的时间
     * @param oldestAlarmDataStr 列表中报警时间为最老时间的报警数据
     * @param alarmTypeStr       报警类型逗号分隔(不传就是查询所有需要展示的报警类型)
     * @param mark               页签标识
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getTodayAlarmRecord(String latestTime, String latestAlarmDataStr, String oldestTime,
        String oldestAlarmDataStr, String alarmTypeStr, String mark) throws Exception;

    /**
     * 查询当天报警次数
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getTodayAlarmQuantity() throws Exception;

    /**
     * 进入页面初始化需要查询报警的监控对象id
     * @return List<String>
     * @throws Exception Exception
     */
    List<String> initNeedQueryAlarmMonitorIds() throws Exception;
}
