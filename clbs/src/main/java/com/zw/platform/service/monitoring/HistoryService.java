package com.zw.platform.service.monitoring;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.domain.oil.HistoryMileAndSpeed;
import com.zw.platform.domain.oil.HistoryStopAndTravel;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.vas.history.TimeZoneQueryParam;
import com.zw.platform.domain.vas.history.TrackPlayBackChartDataQuery;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by LiaoYuecai on 2016/10/18.
 */
public interface HistoryService {
    /**
     * 轨迹回放，获取车辆轨迹数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    List<Positional> getHistory(String vehicleId, String startTime, String endTime) throws Exception;

    /**
     * 轨迹回放，获取车辆行驶数据，停止数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JsonResultBean getHistoryVehicle(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception;

    /**
     * 查询轨迹回放日志
     * @param vehicleId
     * @param ip
     */
    void addlog(String vehicleId, String ip);

    /**
     * 轨迹回放，获取人行驶数据，停止数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JsonResultBean getHistoryPeople(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception;

    /**
     * 获取轨迹回放车辆行驶的日历数据
     * @param vehicleId
     * @param nowMonth
     * @param nextMonth
     * @param type
     * @param isAppFlag
     * @return
     * @throws Exception
     */
    JsonResultBean changeHistoryActiveDate(String vehicleId, String nowMonth, String nextMonth, String type,
        Integer bigDataFlag, boolean isAppFlag) throws Exception;

    List<Map<String, String>> getDailyMileByDate(String vehicleId, long date1, long date2);

    List<Map<String, String>> getDailyMileByDateSensorMessage(String vehicleId, long date1, long date2);

    List<Map<String, String>> getDailyPointByDate(String vehicleId, long date1, long date2);

    List<Map<String, String>> getDailyMileByDatePeople(String vehicleId, long date1, long date2);

    List<Positional> getQueryDetails(String vehicleId, long startTime, long endTime);

    /**
     * 轨迹回放查询报警数据
     * @param vehicleId           监控对象id
     * @param startTime           开始时间
     * @param endTime             结束时间
     * @param isSaveRedisDataFlag isSaveRedisDataFlag
     * @param isLkywTrackBack     是否是两客一危报警查询
     * @return JsonResultBean JsonResultBean
     * @throws IOException IOException
     */
    JsonResultBean getAlarmData(String vehicleId, Long startTime, Long endTime, Boolean isSaveRedisDataFlag,
        boolean isLkywTrackBack) throws Exception;

    /**
     * 时间段多区域查车
     * @param queryParam queryParam
     * @return JsonResultBean
     */
    JsonResultBean findHistoryByTimeAndAddress(TimeZoneQueryParam queryParam);

    /**
     * 获得油耗图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorFlag
     * @param sensorNo
     * @return
     * @throws Exception
     */
    JsonResultBean getOilConsumptionChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception;

    /**
     * 获得温度图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorFlag
     * @param sensorNo
     * @return
     * @throws Exception
     */
    JsonResultBean getTemperatureChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception;

    /**
     * 获得湿度图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorFlag
     * @param sensorNo
     * @return
     * @throws Exception
     */
    JsonResultBean getHumidityChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception;

    /**
     * 获得湿度图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorFlag
     * @param sensorNo
     * @return
     * @throws Exception
     */
    JsonResultBean getWorkHourChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception;

    /**
     * 查询历史数据(轨迹回放)
     * @param monitorId
     * @param startTime
     * @param endTime
     * @param sensorFlag
     * @param reissue
     * @return
     * @throws Exception
     */
    JsonResultBean getMonitorHistoryData(String monitorId, String startTime, String endTime,
        Integer sensorFlag, Integer reissue)
        throws Exception;

    JsonResultBean getMonitorObdDate(String monitorId, String startTime, String endTime, Integer sensorFlag)
        throws Exception;

    /**
     * 轨迹回放图表-获取里程速度数据
     */
    List<HistoryMileAndSpeed> getMileSpeedData(TrackPlayBackChartDataQuery query) throws Exception;

    /**
     * 轨迹回放图表-获取行驶/停止数据
     */
    List<HistoryStopAndTravel> getTravelAndStopData(TrackPlayBackChartDataQuery query) throws Exception;

    /**
     * 轨迹回放图表-获取油量数据
     */
    JsonResultBean getOilMassData(TrackPlayBackChartDataQuery query) throws Exception;

    /**
     * 轨迹回放图表-获取外设轮询列表
     */
    JSONObject getSensorPollingListByMonitorId(String monitorId);

    JsonResultBean getPositiveInversionDate(String monitorId, String startTime, String endTime, Integer sensorFlag)
        throws Exception;

    /**
     * 轨迹回放图表-获取监控对象I/O数据
     */
    JsonResultBean getSwitchData(TrackPlayBackChartDataQuery query) throws Exception;

    void exportTrackPlay(HttpServletResponse response, PositionalQuery query) throws Exception;

    /**
     * 定时定区域详情导出
     * @param response
     * @param areaListStr
     * @param groupName
     * @throws Exception
     */
    void exportTimeZoneTrackPlay(HttpServletResponse response, String areaListStr, String groupName) throws Exception;

    JsonResultBean getLoadWeightDate(String monitorId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception;

    AppResultBean appLoadWeightDate(String monitorId, String startTime, String endTime, Integer sensorFlag)
        throws Exception;


    JsonResultBean getTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer tireNum) throws Exception;

    /**
     * 获取载重数据
     * @param vehicleId 监控对象ID
     * @param startTime 查询开始时间
     * @param endTime   查询结束时间
     * @param sensorFlag    传感器
     * @return          json
     * @throws Exception e
     */
    AppResultBean appTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception;
}
