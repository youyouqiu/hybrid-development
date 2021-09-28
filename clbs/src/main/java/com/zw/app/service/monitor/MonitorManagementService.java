package com.zw.app.service.monitor;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.monitor.BasicLocationInfo;
import com.zw.app.domain.monitor.BasicLocationInfoNew;
import com.zw.app.util.common.AppResultBean;
import com.zw.protocol.msg.t808.body.LocationInfo;

/**
 * @author hujun
 * @date 2018/8/20 15:09
 */
public interface MonitorManagementService {

    /**
     * 获取当前用户权限分组数据监控对象统计数据
     * @return
     * @throws Exception
     */
    JSONObject getAssignmentByUser(Integer type) throws Exception;

    /**
     * 获取当前用户监控对象列表
     * @return
     * @throws Exception
     */
    JSONObject getMonitorList(String assignmentId, Integer type, Integer page, Integer pageSize) throws Exception;

    /**
     * 模糊搜索当前用户监控对象列表
     * @param fuzzyParam
     * @return
     * @throws Exception
     */
    JSONObject getFuzzyMonitorList(String fuzzyParam) throws Exception;

    /**
     * 2.1.5优化模糊搜索当前用户监控对象列表
     * @param fuzzyParam
     * @return
     * @throws Exception
     */
    JSONObject fuzzyMonitorList(String fuzzyParam) throws Exception;

    /**
     * 2.1.5优化模糊搜索当前用户监控对象列表详情接口
     * @return
     * @throws Exception
     */
    JSONObject fuzzyMonitorDetailList(String vehicleIds) throws Exception;

    /**
     * 获取当前用户监控对象ids
     * @return
     * @throws Exception
     */
    JSONObject getMonitorIds(String id, String favoritesIds) throws Exception;

    /**
     * 获取监控对象基础位置信息
     * @param id
     * @return
     * @throws Exception
     */
    BasicLocationInfo getBasicLocationInfo(String id) throws Exception;

    /**
     * 取监控对象基础位置信息
     * @param id
     * @return
     * @throws Exception APP2.0.0新增
     */
    BasicLocationInfoNew getBasicLocationInfoByMonitorId(String id) throws Exception;

    /**
     * 获取监控对象详细位置信息
     * @return
     * @throws Exception APP2.2.2新增
     */
    AppResultBean setDetailLocationInfo(String location, Integer version) throws Exception;

    /**
     * 获取监控对象详细位置信息
     * @param id
     * @return
     * @throws Exception
     */
    AppResultBean getDetailLocationInfo(String id, Integer version) throws Exception;

    /**
     * 获取监控对象详细位置信息
     * @param id
     * @return
     * @throws Exception
     */
    AppResultBean getDetailLocationInfoBefore(String id, Integer version) throws Exception;

    /**
     * 获取监控对象信息
     * @param id
     * @return
     * @throws Exception
     */
    JSONObject getMonitorInfo(String id) throws Exception;

    /**
     * 获取监控对象位置历史数据
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JSONObject getHistoryLocation(String id, String startTime, String endTime) throws Exception;

    /**
     * 获取监控对象湿度历史数据
     */
    JSONObject getHumidityInfo(String id, String starTime, String endTime) throws Exception;

    /**
     * 获取监控对象外设列表
     */
    JSONObject getMonitorAttached(String vehicleId) throws Exception;

    /**
     * 获取监控对象正反转历史数据
     */
    JSONObject getMonitorWinchInfo(String id, String starTime, String endTime) throws Exception;

    /**
     * 获取监控对象开关历史数据
     */
    JSONObject getSwitchInfo(String id, String starTime, String endTime) throws Exception;

    /**
     * 获取监控对象里程速度历史数据
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JSONObject getMileageHistoryData(String id, String startTime, String endTime) throws Exception;

    /**
     * 获取停止数据历史数据
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JSONObject getStopHistoryData(String id, String startTime, String endTime) throws Exception;

    /**
     * 获取监控对象停止/行驶数据(2.1.0版本接口)
     * @param monitorId 监控对象id
     * @param startTime 查询开始时间
     * @param endTime   查询结束时间
     * @return 停止和行驶数据
     * @throws Exception 运行时异常
     */
    JSONObject getStopHistoryDataToVersion(String monitorId, String startTime, String endTime) throws Exception;

    /**
     * 获取监控对象油量历史数据
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JSONObject getOilHistoryData(String id, String startTime, String endTime) throws Exception;

    /**
     * 获取监控对象油耗历史数据
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JSONObject getOilConsumeHistoryData(String id, String startTime, String endTime) throws Exception;

    /**
     * 获取监控对象温度历史数据
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JSONObject getTemperatureHistoryData(String id, String startTime, String endTime) throws Exception;

    /**
     * 获取监控对象工时历史数据
     */
    JSONObject getWorkHoursHistoryData(String id, String startTime, String endTime) throws Exception;

    /**
     * 获取监控对象设置通道数据
     * @param id
     * @return
     * @throws Exception
     */
    JSONObject getChannelData(String id) throws Exception;

    /**
     * 获取监控对象指定时间段里程统计数据
     * @param id
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    JSONObject getMileDayStatistics(String id, String startDate, String endDate) throws Exception;

    /**
     * 判断监控对象是否在线及是否为808协议
     * @param id
     * @return
     * @throws Exception
     */
    Integer checkMonitorOnline(String id) throws Exception;

    /**
     * 监控对象权限校验(判断该监控对象是否解绑以及当前用户是否有该监控对象权限)
     * @param id
     * @return 1:校验通过 2：该监控对象已解绑 3：当前用户没有该监控对象权限
     * @throws Exception
     */
    Integer checkMonitorAuth(String id) throws Exception;

    AppResultBean getLoadWeightDate(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception;

    AppResultBean getTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception;

    /**
     * 设置当日里程及当日油耗  --0x0201 单点位置应答 时需要计算里程和油耗，0x0200在flink端已经计算好
     * @param vehicleId 车id
     * @param info      位置数据
     */
    void setDailyMileAndOil(String vehicleId, LocationInfo info);

}
