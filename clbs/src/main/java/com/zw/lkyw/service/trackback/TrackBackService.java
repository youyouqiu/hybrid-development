package com.zw.lkyw.service.trackback;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.oil.HistoryMileAndSpeed;
import com.zw.platform.domain.oil.HistoryStopAndTravel;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oil.PositionalQuery;
import com.zw.platform.domain.vas.history.TrackPlayBackChartDataQuery;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface TrackBackService {

    /**
     * 轨迹回放，获取车辆行驶数据，停止数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param reissue
     * @return
     * @throws Exception
     */
    JsonResultBean getHistoryVehicle(String vehicleId, String startTime, String endTime, Integer sensorFlag, String ip,
        Integer reissue)
        throws Exception;

    /**
     * 轨迹回放，获取人行驶数据，停止数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param reissue
     * @return
     * @throws Exception
     */
    JsonResultBean getHistoryPeople(String vehicleId, String startTime, String endTime, Integer sensorFlag, String ip,
        Integer reissue)
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

    List<Positional> getQueryDetails(String vehicleId, String startTime, String endTime, Integer reissue)
        throws IOException;

    /**
     * 时间段多区域查车
     * @param areaListStr  areaListStr
     * @param monitorIds   monitorIds
     * @param startTimeOne startTimeOne
     * @param endTimeOne   endTimeOne
     * @param startTimeTwo startTimeTwo
     * @param endTimeTwo   endTimeTwo
     * @return JsonResultBean
     */
    JsonResultBean findHistoryByTimeAndAddress(String areaListStr, String monitorIds, String startTimeOne,
        String endTimeOne, String startTimeTwo, String endTimeTwo);

    /**
     * 获得油耗图表数据
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param sensorFlag
     * @param sensorNo
     * @param reissue
     * @return
     * @throws Exception
     */
    JsonResultBean getOilConsumptionChartData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo, Integer reissue) throws Exception;

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
    JsonResultBean getMonitorHistoryData(String monitorId, String startTime,
        String endTime, Integer sensorFlag, String ip, Integer reissue) throws Exception;

    JsonResultBean getMonitorObdData(String monitorId, String startTime, String endTime, Integer sensorFlag)
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
     * @param reissue
     * @throws Exception
     */
    void exportTimeZoneTrackPlay(HttpServletResponse response, String areaListStr, String groupName, Integer reissue)
        throws Exception;

    JsonResultBean getLoadWeightDate(String monitorId, String startTime, String endTime, Integer sensorFlag,
        Integer sensorNo) throws Exception;

    JsonResultBean getTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag,
        Integer tireNum) throws Exception;
}
