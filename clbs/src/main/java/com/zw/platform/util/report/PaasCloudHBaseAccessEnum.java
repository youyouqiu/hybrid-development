package com.zw.platform.util.report;

import com.zw.platform.commons.UrlConvert;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * pass cloud hbase 存取专用API
 * <p> 路径尽量保持格式：/hbase/业务/表名/操作，
 * 如：/hbase/adas/risk_media/list
 *
 * @author Zhang Yanhui
 * @apiNote 某些命名可能因本次迁移中未做重构而略显简陋，请见谅
 * @since 2021/7/8 16:49
 */

public enum PaasCloudHBaseAccessEnum implements UrlConvert {
    /**
     * 查询风险驾驶员
     */
    GET_RISK_DRIVER("/hbase/adas/risk/driver", HttpMethod.POST),
    /**
     * 更新风险
     */
    UPDATE_RISK("/hbase/adas/risk/update", HttpMethod.POST),
    /**
     * 查询风险事件信息
     */
    GET_RISK_EVENT_INFO("/hbase/adas/risk_event/info", HttpMethod.POST),
    /**
     * 查询风险事件
     */
    LIST_RISK_EVENT("/hbase/adas/risk_event/list", HttpMethod.POST),
    /**
     * 更新风险事件
     */
    UPDATE_RISK_EVENT("/hbase/adas/risk_event/update", HttpMethod.POST),
    /**
     * 查询风险媒体
     */
    LIST_RISK_MEDIA("/hbase/adas/risk_media/list", HttpMethod.POST),
    /**
     * 更新风险媒体
     */
    UPDATE_RISK_MEDIA("/hbase/adas/risk_media/update", HttpMethod.POST),

    /**
     * 查询主动安全媒体URL和协议类型
     */
    RISK_MEDIA_URL_AND_PROTOCOL("/hbase/adas/risk_media/info", HttpMethod.POST),

    /**
     * 查询风险时间摘要信息
     */
    GET_RISK_EVENT_SHORT_INFO("/hbase/adas/alarm_forward_info_middle/getRiskEventId", HttpMethod.POST),

    /**
     * 更新风险的媒体flag
     */
    UPDATE_RISK_MEDIA_FLAG("/hbase/adas/risk/updateRiskMediaFlag", HttpMethod.POST),

    /**
     * 更新事件的媒体flag
     */
    UPDATE_EVENT_MEDIA_FLAG("/hbase/adas/risk_event/updateEventMediaFlag", HttpMethod.POST),

    /**
     * 更新媒体
     */
    UPDATE_MEDIA("/hbase/adas/risk_media/updateMedia", HttpMethod.POST),

    /**
     * 查询风险列表
     */
    GET_RISK_LIST("/hbase/adas/risk/getRiskList", HttpMethod.POST),

    /**
     * 查询监控对象报警记录
     */
    LIST_MONITOR_ALARM("/hbase/appAlarm/alarm_handle/listMonitorAlarm", HttpMethod.POST),

    /**
     * 统计用户权限下的报警的监控对象
     */
    LIST_USER_VEHICLE_ALARM("/hbase/appAlarm/alarm_handle/listUserVehicleAlarm", HttpMethod.POST),

    /**
     * 统计用户权限下的最新报警的监控对象
     */
    LIST_USER_VEHICLE_LAST_ALARM("/hbase/appAlarm/last_alarm_handle/listUserVehicleLastAlarm", HttpMethod.POST),

    /**
     * 获取监控对象报警概要信息
     */
    LIST_ALARM_DATE("/hbase/appAlarm/alarm_handle/listAlarmDate", HttpMethod.POST),

    /**
     * 获取监控对象报警详细信息
     */
    LIST_ALARM_DETAIL("/hbase/appAlarm/alarm_handle/listAlarmDetail", HttpMethod.POST),

    /**
     * 查询报警大数据月表数据
     */
    GET_BIG_DATA_ALARM_REPORT_INFO("/hbase/alarm/alarm_info_report/alarmReport", HttpMethod.POST),

    /**
     * 查询对讲出勤报表月表数据
     */
    LIST_ATTENDANCE("/hbase/talkback/scheduled_report/list", HttpMethod.POST),

    /**
     * 查询传感器值
     */
    GET_SENSOR_INFO("/hbase/appLocation/positional/app/sensor", HttpMethod.POST),

    /**
     * 获取监控对象位置历史数据
     */
    LIST_HISTORY_LOCATION("/hbase/appLocation/positional/listHistoryLocation", HttpMethod.POST),

    /**
     * 获取监控对象湿度历史数据
     */
    LIST_HUMIDITY_DATA("/hbase/appLocation/positional/listHumidityData", HttpMethod.POST),

    /**
     * 获取监控对象正反转历史数据
     */
    LIST_WINCH_INFO("/hbase/appLocation/positional/listWinchInfo", HttpMethod.POST),

    /**
     * 获取监控对象开关历史数据
     */
    LIST_SWITCH_SIGN("/hbase/appLocation/positional/listSwitchSign", HttpMethod.POST),

    /**
     * 获取监控对象里程速度历史数据
     */
    LIST_MILEAGE_HISTORY_DATA("/hbase/appLocation/positional/listMileageHistoryData", HttpMethod.POST),

    /**
     * 获取监控对象用于停止计算的历史数据
     */
    LIST_STOP_HISTORY_DATA("/hbase/appLocation/positional/listStopHistoryData", HttpMethod.POST),

    /**
     * 获取监控对象油量历史数据
     */
    LIST_OIL_HISTORY_DATA("/hbase/appLocation/positional/listOilHistoryData", HttpMethod.POST),

    /**
     * 获取监控对象油耗历史数据
     */
    LIST_OIL_CONSUME_HISTORY_DATA("/hbase/appLocation/positional/listOilConsumeHistoryData", HttpMethod.POST),

    /**
     * 获取监控对象温度历史数据
     */
    LIST_TEMPERATURE_DATA("/hbase/appLocation/positional/listTemperatureData", HttpMethod.POST),

    /**
     * 获取监控对象工时历史数据
     */
    LIST_WORK_HOUR_DATA("/hbase/appLocation/positional/listWorkHourData", HttpMethod.POST),

    /**
     * 离线报表-获取监控对象油量里程数据
     */
    LIST_OIL_MASS_AND_MILE_DATA("/hbase/appLocation/positional/listOilMassAndMileData", HttpMethod.POST),

    /**
     * 查询报警报表信息
     */
    GET_ALARM_REPORT_INFO("/hbase/alarm/alarm_handle/alarmReport", HttpMethod.POST),

    /**
     * 查询报警基础信息
     */
    LIST_ALARM_SHORT_INFO("/hbase/alarm/alarm_handle/list/short", HttpMethod.POST),

    /**
     * 查询车辆超速报警报表信息(用于统计)
     */
    LIST_SPEED_ALARM("/hbase/alarm/alarm_handle/listSpeedAlarm", HttpMethod.POST),

    /**
     * 查询车辆超速报警报表信息(用于统计)
     */
    GET_SPEED_ALARM("/hbase/alarm/alarm_handle/getSpeedAlarm", HttpMethod.POST),

    /**
     * 查询围栏进出统计
     */
    LIST_FENCE_REPORT("/hbase/alarm/alarm_handle/listFenceReport", HttpMethod.POST),

    /**
     * 查询车辆报警信息
     */
    GET_ALARM_HANDLE("/hbase/alarm/alarm_handle/getAlarmHandle", HttpMethod.POST),

    /**
     * 查询单车辆报警信息
     */
    GET_ALARM_LIST("/hbase/alarm/alarm_handle/getAlarmList", HttpMethod.POST),

    /**
     * 查询当前车辆最新一条报警结束时间
     */
    GET_LATEST_ALARM_HANDLE("/hbase/alarm/alarm_handle/getLatestAlarmHandle", HttpMethod.POST),

    /**
     * 根据监控对象id、报警开始时间、报警类型查询报警记录
     */
    GET_ALARM_INFO_BY_MONITOR_ID("/hbase/alarm/alarm_handle/getAlarmInfoByMonitorId", HttpMethod.POST),

    /**
     * 获得最近的报警
     */
    GET_CURRENT_ALARM_INFO("/hbase/alarm/alarm_handle/getCurrentAlarmInfo", HttpMethod.POST),

    /**
     * 查询信息统计
     */
    GET_ALARM_MESSAGE_LIST("/hbase/alarm/alarm_handle/getAlarmMessageList", HttpMethod.POST),

    /**
     * 获取809报警转发记录(version:4.0.0)
     */
    GET_809_ALARM_FORWARD_MSG("/hbase/alarm/alarm_forward_info/get809AlarmForwardMsg", HttpMethod.POST),

    /**
     * 获取809报警转发记录
     */
    GET_XIZANG_809_ALARM_FORWARD_MSG("/hbase/alarm/alarm_forward_info/getXizang809AlarmForwardMsg", HttpMethod.POST),

    /**
     * 主动上报报警处理结果查询(version:4.0.0)
     */
    GET_ALARM_FORWARD_INFO_MIDDLE("/hbase/alarm/alarm_forward_info_middle/getAlarmForwardInfoMiddle", HttpMethod.POST),

    /**
     * 根据9406指令信息 查询报警信息
     */
    GET_ALARM_FORWARD_INFO_MIDDLE_INFO(
            "/hbase/alarm/alarm_forward_info_middle/getAlarmForwardInfoMiddleInfo", HttpMethod.POST),

    /**
     * 查询车辆指定时段内所有报警类型
     */
    GET_DAY_ALARM("/hbase/alarm/alarm_forward_info_middle/getDayAlarm", HttpMethod.POST),

    /**
     * 批量更新状态
     */
    BATCH_UPDATE_STATUS("/hbase/alarm/alarm_forward_info_middle/batchUpdateStatus", HttpMethod.POST),

    /**
     * 查询里程小于当前车辆一个月里程的车辆数量
     */
    FIND_SMALL_MILE_COUNT("/hbase/report/integrated_statistics/findSmallMileCount", HttpMethod.POST),

    /**
     * 查询上线天数
     */
    FIND_ONLINES("/hbase/report/integrated_statistics/findOnlines", HttpMethod.POST),

    /**
     * 根据id查询单车的每月详细
     */
    GET_MONTH_SUM_BY_VEHICLE("/hbase/report/integrated_statistics/getMonthSumByVehicle", HttpMethod.POST),

    /**
     * 根据id查询单车单日详细
     */
    GET_DAYS_BY_VEHICLE("/hbase/report/integrated_statistics/getDaysByVehicle", HttpMethod.POST),

    /**
     * 从大数据月报表查询统计所有监控对象的数据
     */
    GET_TOTAL_DATA_BY_MONITORS("/hbase/report/integrated_statistics/getTotalDataByMonitors", HttpMethod.POST),

    /**
     * 从大数据月报表获取指定月份每天所有监控对象的里程和
     */
    GET_ALL_MONITOR_DAILY_MILE("/hbase/report/integrated_statistics/getAllMonitorDailyMile", HttpMethod.POST),

    /**
     * 查询监控对象指定月份到达的城市
     */
    GET_MONITOR_ARRIVED_CITY("/hbase/report/position_statistic/getMonitorArrivedCity", HttpMethod.POST),

    /**
     * 查询监控对象到过城市的最大和最小经纬度
     */
    GET_CITY_COORDINATE("/hbase/report/position_statistic/getCityCoordinate", HttpMethod.POST),

    /**
     * 查询活跃天数
     */
    GET_MONITOR_ACTIVE_DATA("/hbase/report/integrated_statistics/getMonitorActiveData", HttpMethod.POST),

    /**
     * 根据id查询单车的每月详细
     */
    GET_MONITOR_MOUTH_SUM("/hbase/report/integrated_statistics/getMonitorMouthSum", HttpMethod.POST),

    /**
     * 根据id查询单车单日详细
     */
    GET_MONTH_DAYS_DATA("/hbase/report/integrated_statistics/getMonthDaysData", HttpMethod.POST),
    
    /**
     * 查询里程小于当前车辆一个月里程的车辆数量
     */
    GET_SMALL_MILE_COUNT("/hbase/report/integrated_statistics/getSmallMileCount", HttpMethod.POST),

    /**
     * 能耗统计查询
     */
    FIND_POSITIONAL_LIST_STATISTICS_TIME(
            "/hbase/location/positional/findPositionalList_statistics_time", HttpMethod.POST),

    /**
     * 查询日期范围的最后一条数据
     */
    FIND_LAST_POSITIONAL("/hbase/location/positional/findLastPositional", HttpMethod.POST),

    /**
     * 获取app油耗里程数据
     */
    GET_APP_OIL_INFO("/hbase/location/positional/getAppOilInfo", HttpMethod.POST),

    /**
     * 历史轨迹查询
     */
    GET_HISTORY_TRACK_INFO("/hbase/location/positional/getHistoryTrackInfo", HttpMethod.POST),

    /**
     * 工时统计
     */
    GET_WORK_INFO("/hbase/location/work_hours/getWorkInfo", HttpMethod.POST),

    /**
     * 历史轨迹查询（WEB端专用）
     */
    GET_HISTORY_TRACK("/hbase/location/positional/getHistoryTrack", HttpMethod.POST),

    /**
     * 查询历史GPS数据
     */
    GET_MONITOR_TRACK("/hbase/location/positional/getMonitorTrack", HttpMethod.POST),

    /**
     * 轨迹回放，获取人行驶数据，停止数据
     */
    GET_HISTORY_TRACK_PEOPLE("/hbase/location/positional/getHistoryTrackPeople", HttpMethod.POST),

    /**
     * 根据车辆id查询指定月份返回每日里程
     */
    GET_DAILY_MILE_BY_DATE("/hbase/location/positional/getDailyMileByDate", HttpMethod.POST),

    /**
     * 获取轨迹回放车辆行驶的日历数据
     */
    GET_DAILY_MILE_BY_DATE_SENSOR_MESSAGE(
            "/hbase/location/positional/getDailyMileByDateSensorMessage", HttpMethod.POST),

    /**
     * 获取轨迹回放车辆行驶的日历数据
     */
    GET_DAILY_MILE_BY_DATE_PEOPLE("/hbase/location/positional/getDailyMileByDatePeople", HttpMethod.POST),

    /**
     * 获取轨迹回放车辆行驶的日历数据
     */
    GET_DAILY_POINT_BY_DATE("/hbase/location/positional/getDailyPointByDate", HttpMethod.POST),

    /**
     * 根据车id和指定日期获取里程和油耗
     */
    GET_MILE_BY_ID("/hbase/location/positional/getMileById", HttpMethod.POST),

    /**
     * 根据条件查询移动源油量信息
     */
    MOBILE_SOURCE_MANAGE("/hbase/location/positional/mobileSourceManage", HttpMethod.POST),

    /**
     * 根据车辆id查询所有轨迹
     */
    GET_POSITIONALS_BY_IDS("/hbase/location/positional/getPositionalsByIds", HttpMethod.POST),

    /**
     * 根据监控对象id查询监控对象终端上传的温度传感器温度信息
     */
    GET_TEMP_DATE_BY_VEHICLE_ID("/hbase/location/positional/getTempDateByVehicleId", HttpMethod.POST),

    /**
     * 根据车辆id、开始时间和结束时间查询温湿度数据
     */
    GET_TEMP_DATA_AND_HUMIDITY_DATA("/hbase/location/positional/getTempDataAndHumidityData", HttpMethod.POST),

    /**
     * 查询一段时间内的总油量和总里程（大数据月表）
     */
    GET_MILEAGE_DATA("/hbase/location/travlled_distance_report/getMileageData", HttpMethod.POST),

    /**
     * 查询一段时间停驶数据（大数据月表）
     */
    GET_STOP_BIG_DATA("/hbase/location/stopped_report/getStopBigData", HttpMethod.POST),

    /**
     * 查询一段时间超速报警信息
     */
    GET_SPEED_REPORT_BIG_DATA("/hbase/location/alarm_info_report/getSpeedReportBigData", HttpMethod.POST),

    /**
     * 查询一段时间超速报警信息分平台与终端报警
     */
    GET_SPEED_REPORT("/hbase/location/alarm_info_report/getSpeedReport", HttpMethod.POST),

    /**
     * 查询一段时间一辆车超速报警信息分平台与终端报警
     */
    GET_SPEED_REPORT_BY_VID("/hbase/location/alarm_info_report/getSpeedReportByVid", HttpMethod.POST),

    /**
     * 获得obd原车数据
     */
    GET_OBD_VEHICLE_DATA("/hbase/location/positional/getOBDVehicleData", HttpMethod.POST),

    /**
     * 查询停止数据
     */
    FIND_MILEAGE_BY_VEHICLE_IDS("/hbase/location/positional/findMileageByVehicleIds", HttpMethod.POST),

    /**
     * 获取里程统计数据
     */
    GET_MILEAGE_STATISTIC_DATA("/hbase/location/mileage_statistic/getMileageStatisticData", HttpMethod.POST),

    /**
     * 获取监控对象每日里程数据
     */
    GET_MILEAGE_DATA_BY_DAY("/hbase/location/mileage_statistic/getMileageDataByDay", HttpMethod.POST),

    /**
     * 时间段多区域查车
     */
    FIND_POSITIONAL_BY_ADDRESS("/hbase/location/positional/findPositionalByAddress", HttpMethod.POST),

    /**
     * 根据监控对象ID和时间获取位置数据
     */
    FIND_POSITIONAL_BY_MONITOR_ID_AND_TIME(
            "/hbase/location/positional/findPositionalByMonitorIdAndTime", HttpMethod.POST),

    /**
     * 轨迹回放查询历史数据(包括传感器数据)
     */
    GET_MONITOR_HISTORY_POSITIONAL_DATA("/hbase/location/positional/getMonitorHistoryPositionalData", HttpMethod.POST),

    /**
     * app获得工时统计信息
     */
    GET_WORK_HOUR_STATISTICS_INFO("/hbase/location/working_report/getWorkHourStatisticsInfo", HttpMethod.POST),

    /**
     * 获得工时数据
     */
    GET_WORK_HOUR_DATA("/hbase/location/positional/getWorkHourData", HttpMethod.POST),

    /**
     * 获取监控对象里程统计(离线月表)数据
     */
    GET_OFF_LINE_MILEAGE_STATISTIC_DATA("/hbase/location/positional/getOffLineMileageStatisticData", HttpMethod.POST),

    /**
     * 查询终端位置数据
     */
    FIND_TERMINAL_POSITIONAL_LIST("/hbase/location/positional/findTerminalPositionalList", HttpMethod.POST),

    /**
     * 获取监控对象里程统计数据
     */
    FIND_TOTAL_MILE_LIST("/hbase/location/mileage_statistic/findTotalMileList", HttpMethod.POST),

    /**
     * 普货抽查表在线情况
     */
    GET_SPOT_CHECK_ONLINE("/hbase/location/positional/getSpotCheckOnline", HttpMethod.POST),

    /**
     * 获取f3高精度报表
     */
    GET_F3_HIGH_PRECISION_REPORTS("/hbase/location/positional/getF3HighPrecisionReports", HttpMethod.POST),

    /**
     * 查询位置数据
     */
    GET_MONITOR_LOCATION("/hbase/location/positional/getMonitorLocation", HttpMethod.POST),

    ;

    /**
     * uri
     */
    private final String path;
    /**
     * 请求方式
     */
    private final HttpMethod httpMethod;

    PaasCloudHBaseAccessEnum(String path, HttpMethod httpMethod) {
        this.path = path;
        this.httpMethod = httpMethod;
    }

    /**
     * path cloud api pair
     */
    private static final Map<String, String> API_URL = new HashMap<>(values().length);

    /**
     * 聚合address + path
     *
     * @param address address
     */
    public static void assembleUrl(String address) {
        for (PaasCloudHBaseAccessEnum value : values()) {
            API_URL.put(value.name(), address + value.getPath());
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getUrl() {
        return API_URL.get(this.name());
    }

    @Override
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
}
