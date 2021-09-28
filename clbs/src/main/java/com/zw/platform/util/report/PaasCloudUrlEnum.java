package com.zw.platform.util.report;

import com.zw.platform.commons.UrlConvert;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * pass cloud url 枚举类(现在的url太多了，以后会更多)
 * @author create by zhouzongbo on 2020/6/18.
 */
public enum PaasCloudUrlEnum implements UrlConvert {
    /**
     * ++++++++++++++++++++部标监管报表++++++++++++++++++++
     * 行驶里程报表-行驶统计
     */
    DRIVING_MILEAGE_STATISTICS_URL("/positional/travel/report", HttpMethod.POST),
    /**
     * 行驶里程报表-行驶明细
     */
    DRIVING_MILEAGE_DETAILS_URL("/positional/travel/list", HttpMethod.GET),
    /**
     * 停止报表
     */
    STOP_MILEAGE_STATISTICS_URL("/positional/stop/report", HttpMethod.POST),
    /**
     * 上线率报表
     */
    ON_LINE_REPORT_URL("/positional/jtt/online_rate/report", HttpMethod.POST),
    /**
     * 轨迹有效性报表
     */
    TRACK_VALID_REPORT_URL("/positional/jtt/track_validity/report", HttpMethod.GET),
    /**
     * 超速报警报表
     */
    SPEED_ALARM_REPORT_URL("/alarm/jtt/report", HttpMethod.POST),
    /**
     * 超速报表-不知道为撒两个一毛一样的url, 但是不想测，所以就保留了.
     */
    SPEEDING_REPORT_LIST_URL("/alarm/jtt/report", HttpMethod.POST),
    /**
     * 报警信息统计-查询统计条数
     */
    ALARM_MESSAGE_STATISTICS_COUNT_URL("/alarm/count/type", HttpMethod.POST),

    /**
     * 809转发报警查询
     */
    ALARM_809_FORWARD("/alarm/v1.1/809/page", HttpMethod.POST),

    /**
     * ++++++++++++++++++++位置报表++++++++++++++++++++
     * 停止报表
     */
    TERMINAL_STOP_MILEAGE_STATISTICS_URL("/positional/stop/report", HttpMethod.POST),
    /**
     * 终端里程报表-里程统计
     */
    TERMINAL_MILEAGE_STATISTICS_URL("/positional/mileage/report", HttpMethod.POST),
    /**
     * 终端里程报表-每日明细
     */
    TERMINAL_MILEAGE_DAILY_DETAILS_URL("/positional/mileage/daily/report", HttpMethod.POST),
    /**
     * 最新位置报表
     */
    LATEST_LOCATION_INFO_REPORT_URL("/positional/last/list", HttpMethod.POST),
    /**
     * 连续性分析报表
     */
    CONTINUITY_ANALYSIS_LIST_URL("/positional/continuity/report/v1.1", HttpMethod.POST),
    /**
     * 出区划累计时长统计
     */
    OUT_AREA_DURATION_STATISTICS("/positional/out_province/total_time/report", HttpMethod.POST),

    /**
     * ++++++++++++++++++++四川监控报表++++++++++++++++++++
     * 视频巡检存储地址
     */
    SAVE_VIDEO_INSPECTION_URL("/video/inspection", HttpMethod.POST),
    /**
     * 视频巡检统计查询地址
     */
    VIDEO_INSPECTION_STATISTICS_URL("/video/inspection/report", HttpMethod.POST),
    /**
     * 视频巡检详情查询地址
     */
    SAVE_VIDEO_INSPECTION_DETAILS_URL("/video/inspection/list", HttpMethod.POST),
    /**
     * 下发消息统计
     */
    SEND_MESSAGE_REPORT_LIST_URL("/instruct/report", HttpMethod.POST),
    /**
     * 下发消息统计详情
     */
    SEND_MESSAGE_REPORT_DETAILS_URL("/instruct/list", HttpMethod.POST),
    /**
     * 批量下发短信接口内容
     */
    BATCH_SAVE_SEND_MSG_URL("/instruct/batch", HttpMethod.POST),
    /**
     * 获取位置历史数据
     */
    POSITIONAL_HISTORY_URL("/positional/v1.1/history/list", HttpMethod.POST),
    /**
     * 获取单车历史数据（基础位置 + 传感器数据(不包含0x50和0x4F))
     */
    SENSOR_BASIC_POSITIONAL_HISTORY_URL("/positional/v1.1/history/list/sensor/basic", HttpMethod.POST),
    /**
     * 监控对象按天里程统计: TODO 暂时没有用
     */
    DAILY_MILEAGE_URL("/positional/mileage/daily/report", HttpMethod.GET),
    /**
     * 监控对象按天里程统计
     */
    LKYW_DAILY_MILEAGE_URL("/positional/mileage_stats/list", HttpMethod.GET),
    /**
     * 定时定区域
     */
    TIME_AREA_URL("/positional/time_area/list", HttpMethod.POST),
    /**
     * 超速企业统计报表图形统计
     */
    UP_SPEED_GRAPHICAL_STATISTICS_URL("/alarm/sichuan/over_speed/org/statistic/detail", HttpMethod.GET),
    /**
     * 超速企业统计报表列表统计
     */
    UP_SPEED_LIST_STATISTICS_URL("/alarm/sichuan/over_speed/org/child/list", HttpMethod.GET),
    /**
     * 超速企业排名
     */
    UP_SPEED_GRAPHICAL_RANK_INFO_URL("/alarm/sichuan/over_speed/org/list", HttpMethod.POST),
    /**
     * 超速统计报表明细列表统计
     */
    UP_SPEED_INFO_LIST_URL("/alarm/sichuan/over_speed/org/monitor/list", HttpMethod.GET),
    /**
     * 超速车辆统计报表列表数据
     */
    UP_SPEED_MONITOR_STATISTICS_LIST_URL("/alarm/sichuan/over_speed/monitor/list", HttpMethod.POST),
    /**
     * 超速车辆统计报表详情列表数据
     */
    UP_SPEED_MONITOR_STATISTICS_INFO_LIST_URL("/alarm/sichuan/over_speed/monitor/detail/list", HttpMethod.POST),
    /**
     * 超速车辆统计报表详情图形统计数据
     */
    UP_SPEED_MONITOR_STATISTICS_GRAPHICAL_URL("/alarm/sichuan/over_speed/monitor/statistic/detail", HttpMethod.GET),
    /**
     * 超速车辆统计报表详情排名
     */
    UP_SPEED_MONITOR_GRAPHICAL_RANK_INFO_URL("/alarm/sichuan/over_speed/monitor/rank/list", HttpMethod.POST),

    /**
     * 路线偏离企业统计 -> 数据列表
     * 企业及其下级企业月路线偏离报警统计(路线偏离企业统计列表)
     */
    ORG_OFF_ROUTE_LIST("/alarm/sichuan/org/child/month/course_deviation/statistic/page", HttpMethod.POST),
    /**
     * 路线偏离企业统计 -> 路线偏离趋势、报警占比
     * 企业月路线偏离报警统计明细(企业路线偏离趋势)
     */
    ORG_OFF_ROUTE_TREND("/alarm/sichuan/org/month/course_deviation/detail/list", HttpMethod.POST),
    /**
     * 路线偏离企业统计 -> 企业路线偏离报警明细 -> 单个企业路线偏离报警基本信息
     * 企业月路线偏离统计
     */
    ORG_OFF_ROUTE_STATISTICS("/alarm/sichuan/org/month/course_deviation/statistic", HttpMethod.POST),
    /**
     * 路线偏离企业统计 -> 企业路线偏离报警明细 -> 数据列表
     * 企业下监控对象月路线偏离统计
     */
    ORG_MONITOR_OFF_ROUTE_LIST("/alarm/sichuan/org/monitor/month/course_deviation/page", HttpMethod.POST),

    /**
     * 路线偏离车辆统计 -> 数据列表
     */
    MONITOR_OFF_ROUTE_LIST("/alarm/sichuan/monitor/month/course_deviation/statistic/page", HttpMethod.POST),
    /**
     * 路线偏离车辆统计 -> 报警明细 -> 图表
     */
    MONITOR_OFF_ROUTE_STATISTICS("/alarm/sichuan/monitor/month/course_deviation/detail/list", HttpMethod.POST),
    /**
     * 路线偏离车辆统计 -> 报警明细 -> 数据列表
     */
    MONITOR_OFF_ROUTE_DETAIL_LIST("/alarm/sichuan/monitor/time/alarm/course_deviation/page", HttpMethod.POST),

    /**
     * ++++++++++++++++++++四川报表++++++++++++++++++++
     * 企业疲劳驾驶列表数据
     */
    FATIGUE_DRIVING_ORG_LIST_URL("/alarm/sichuan/fatigued/org/child/list", HttpMethod.GET),
    /**
     * 企业疲劳驾驶图像
     */
    FATIGUE_DRIVING_GRAPHICS_ORG_URL("/alarm/sichuan/fatigued/org/statistic/detail", HttpMethod.GET),
    /**
     * 企业排行接口
     */
    FATIGUE_DRIVING_RANK_ORG_URL("/alarm/sichuan/fatigued/org/list", HttpMethod.POST),
    /**
     * 企业每个监控对象详情接口
     */
    FATIGUE_DRIVING_DETAIL_ORG_URL("/alarm/sichuan/fatigued/org/monitor/list", HttpMethod.GET),
    /**
     * 车辆疲劳驾驶列表数据结构
     */
    FATIGUE_DRIVING_VEH_LIST_URL("/alarm/sichuan/fatigued/monitor/list", HttpMethod.POST),
    /**
     * 车辆疲劳驾驶图像接口
     */
    FATIGUE_DRIVING_GRAPHICS_VEH_URL("/alarm/sichuan/fatigued/monitor/statistic/detail", HttpMethod.GET),
    /**
     * 每个监控对象详情接口
     */
    FATIGUE_DRIVING_DETAIL_VEH_URL("/alarm/sichuan/fatigued/monitor/detail/list", HttpMethod.POST),
    /**
     * 停运车辆列表
     */
    STOPPED_VEHICLE_URL("/positional/status/monitor/list/v1.1", HttpMethod.GET),
    /**
     * 车辆里程统计 - 车辆里程统计表
     */
    VEHICLE_MILEAGE_STATISTICS("/positional/sichuan/monitor/mileage/statistic/list", HttpMethod.POST),
    /**
     * 车辆里程统计 - 车辆日里程报表
     */
    VEHICLE_DAILY_MILEAGE_REPORT("/positional/sichuan/monitor/month/mileage/detail/list", HttpMethod.POST),

    /**
     * 车辆在线率统计 - 车辆在线率道路运输企业统计月报表
     */
    VEHICLE_ONLINE_RATE_ORG_MONTH_REPORT("/positional/sichuan/org/month/monitor/online_rate/page", HttpMethod.POST),
    /**
     * 车辆在线率统计 - 车辆在线率统计月报表
     */
    VEHICLE_ONLINE_RATE_VEHICLE_MONTH_REPORT("/positional/sichuan/monitor/month/online_rate/page", HttpMethod.POST),
    /**
     * 车辆在线率统计 - 车辆在线明细
     */
    VEHICLE_ONLINE_DETAILS("/positional/sichuan/monitor/online/detail/list", HttpMethod.POST),

    /**
     * 车辆异动统计 - 车辆异常行驶道路运输企业统计报表
     */
    VEHICLE_ABNORMAL_DRIVING_ORG_REPORT("/alarm/sichuan/unusual/move/org/statistic/list", HttpMethod.POST),
    /**
     * 车辆异动统计 - 车辆异常行驶统计报表
     */
    VEHICLE_ABNORMAL_DRIVING_VEHICLE_REPORT("/alarm/sichuan/unusual/move/monitor/statistic/list", HttpMethod.POST),

    /**
     * 持续超速统计-持续超速道路运输企业统计表
     */
    CONTINUOUS_SPEED_ORG_REPORT("/alarm/sichuan/continuous/speed/org/statistic/list", HttpMethod.POST),
    /**
     * 持续超速统计-持续夜超速车辆统计表
     */
    CONTINUOUS_SPEED_VEHICLE_REPORT("/alarm/sichuan/continuous/speed/monitor/statistic/list", HttpMethod.POST),


    /**
     * ++++++++++++++++++++普货监管报表++++++++++++++++++++
     * 违章记录表
     */
    VIOLATION_RECORD_REPORT("/alarm/shandong/violation_record/report", HttpMethod.POST),

    /**
     * 值班交接班记录表
     */
    SHIFT_HANDOVER_RECORD_REPORT("/alarm/shandong/work_handover/report/v1.1", HttpMethod.POST),

    /**
     * ++++++++++++++++++++传感器报表++++++++++++++++++++
     * 载重报表
     */
    LOAD_INFO_URL("/sensor/load/list", HttpMethod.POST),

    /**
     * 胎压报表
     */
    TYRE_PRESSURE_URL("/sensor/tyre/list", HttpMethod.POST),

    /**
     * OBD报表
     */
    OBD_REPORT_URL("/sensor/obd/list", HttpMethod.POST),

    /**
     * 油量里程报表
     */
    OIL_MILE_REPORT_URL("/sensor/oil_mass_mile/list", HttpMethod.POST),

    /**
     * 油量里程报表加漏油详情数据（分页）
     */
    OIL_MILE_REPORT_DETAIL_URL("/sensor/oil/page", HttpMethod.POST),

    /**
     * 传感器-油量传感器详情接口
     */
    SENSOR_OIL_QUANTITY_REPORT_URL("/sensor/oil/list", HttpMethod.POST),
    /**
     * 传感器-油耗传感器详情接口
     */
    SENSOR_OIL_WEAR_REPORT_URL("/sensor/oil_wear/list", HttpMethod.POST),
    /**
     * 传感器-里程传感器详情接口
     */
    SENSOR_MILEAGE_REPORT_URL("/sensor/mileage/list", HttpMethod.POST),
    /**
     * 湿度报表
     * 湿度传感器详细数据
     */
    HUMIDITY_SENSOR("/sensor/humidity/list", HttpMethod.POST),
    /**
     * 正反转报表
     * 正反转传感器数据
     */
    FORWARD_AND_REVERSE_REPORT_URL("/sensor/veer/list", HttpMethod.POST),

    /**
     * I/O传感器详细数据
     */
    IO_EPORT_URL("/sensor/io/list", HttpMethod.POST),

    /**
     * 工时传感器详细数据
     */
    WORK_HOUR_URL("/sensor/work_hour/list", HttpMethod.POST),

    /**
     * ++++++++++++++++++++山西监管报表++++++++++++++++++++
     * 疲劳驾驶报警明细
     */
    SX_FATIGUE_DRIVING_DETAIL_URL("/alarm/sx/tired_drive_alarm/report", HttpMethod.POST),

    /**
     * 疲劳驾驶违章统计
     */
    SX_FATIGUE_DRIVING_STATISTICS_URL("/alarm/sx/tired_drive_violation/report", HttpMethod.POST),

    /**
     * 疲劳驾驶违章统计
     */
    SX_SPEED_ALARM_DETAIL_URL("/alarm/sx/over_speed_alarm/report", HttpMethod.POST),

    /**
     * 疲劳驾驶违章统计
     */
    SX_SPEED_ALARM_STATISTICS_URL("/alarm/sx/over_speed_violation/report", HttpMethod.POST),

    /**
     * 定位数据合格率
     */
    SX_LOCATION_QUALIFIED_URL("/alarm/sx/location_qualified/report", HttpMethod.POST),

    /**
     * 漂移数据报表
     */
    SX_SHIFT_DATA_URL("/alarm/sx/shift_data/report", HttpMethod.POST),

    /**
     * 漂移数据报表
     */
    SX_BEFORE_DAWN_URL("/alarm/sx/before_dawn/report", HttpMethod.POST),

    /**
     * 异常轨迹报表
     */
    SX_ABNORMAL_TRACK_URL("/alarm/sx/track_abnormal/report", HttpMethod.POST),

    /**
     * 企业车辆定位统计
     */
    GROUP_STATISTICS_URL("/positional/sichuan/report", HttpMethod.POST),
    /**
     * 监控对象最后一次有效定位的定位信息
     */
    LAST_POSITIONING_URL("/positional/sichuan/last/list", HttpMethod.POST),
    /**
     * 定位中断统计
     */
    BREAK_POSITIONING_URL("/positional/sichuan/break/list", HttpMethod.POST),
    /**
     * 离线位移统计
     */
    OFF_POSITIONING_URL("/positional/sichuan/offline_move/list", HttpMethod.POST),
    /**
     * 月度定位统计
     */
    MONTH_POSITIONING_URL("/positional/sichuan/daily/report", HttpMethod.POST),
    /**
     * 异常定位统计
     */
    EXCEPTION_REPORT_POSITIONING_URL("/positional/sichuan/exception/report", HttpMethod.POST),
    /**
     * 异常定位统计详情
     */
    EXCEPTION_INFO_POSITIONING_URL("/positional/sichuan/daily/report", HttpMethod.POST),
    /**
     * 逆地址解析
     */
    ADDRESS_URL("/tool/analysis/address", HttpMethod.POST),
    /**
     * 逆地址批量解析
     */
    ADDRESS_BATCH_URL("/tool/analysis/address/batch", HttpMethod.POST),
    /**
     *
     */
    OIL_SUBSIDY_VEHICLE_MILEAGE_REPORT("/positional/month/mile/list", HttpMethod.POST),
    /**
     * 离线位移日list报表
     */
    OFFLINE_DISPLACEMENT_LIST_URL("/positional/date/offline_move/list", HttpMethod.POST),

    /**
     * 报警漏报list报表
     */
    UNDER_REPORT_LIST_URL("/alarm/org/month/omission_report/statistic", HttpMethod.POST),
    /**
     * 报警漏报企业详情
     */
    UNDER_REPORT_DETAIL_URL("/alarm/monitor/month/day/omission_report/detail", HttpMethod.POST),
    /**
     * 报警漏报企业每天数据
     */
    UNDER_REPORT_COUNT_URL("/alarm/org/month/day/omission_report/count", HttpMethod.POST),
    /**
     * 单天里程统计明细接口
     */
    DAILY_MILEAGE_DETAIL_URL("/positional/date/mile/list", HttpMethod.POST),

    /**
     * 企业月定位信息统计图形数据
     */
    VEHICLE_INFORMATION_STATISTICS_ORG_GRAPH_URL("/positional/org/month/day/composite/statistic/detail/avg",
        HttpMethod.POST),
    /**
     * 企业月定位信息统计列表
     */
    VEHICLE_INFORMATION_STATISTICS_ORG_LIST_URL("/positional/org/month/composite/statistic/page", HttpMethod.POST),

    /**
     * 企业月定位信息统计图形数据
     */
    VEHICLE_INFORMATION_STATISTICS_ORG_DETAIL_GRAPH_URL("/positional/org/month/day/composite/statistic/detail",
        HttpMethod.POST),

    /**
     * 企业月定位信息统计列表
     */
    VEHICLE_INFORMATION_STATISTICS_ORG_DETAIL_LIST_URL("/positional/org/monitor/composite/statistic/detail/page",
        HttpMethod.POST),

    /**
     * 查询与政府监管平台连接情况列表
     */
    CONNECTION_STATISTICS_PLATFORM_LIST_URL("/positional/t809/month/connection/statistic/page", HttpMethod.POST),

    /**
     * 查询与政府监管平台连接情况详情列表
     */
    CONNECTION_STATISTICS_PLATFORM_DETAIL_LIST_URL("/positional/t809/month/date/connection/detail/page",
        HttpMethod.POST),

    /**
     * 查询与车载终端连接情况列表
     */
    CONNECTION_STATISTICS_MONITOR_LIST_URL("/positional/monitor/online/statistic", HttpMethod.POST),

    /**
     * 查询与车载终端连接情况详情列表
     */
    CONNECTION_STATISTICS_MONITOR_DETAIL_LIST_URL("/positional/monitor/date/online/detail", HttpMethod.POST),

    /**
     * 企业-月-在线时长统计
     */
    VEHICLE_ONLINE_TIME_ORG_LIST_URL("/positional/org/month/online/duration/page", HttpMethod.POST),
    /**
     * 监控对象-月-在线时长统计
     */
    VEHICLE_ONLINE_TIME_MONITOR_LIST_URL("/positional/monitor/month/online/duration/page", HttpMethod.POST),
    /**
     * 行政区划-月-在线时长统计
     */
    VEHICLE_ONLINE_TIME_DIVISION_LIST_URL("/positional/division/month/online/duration", HttpMethod.POST),
    /**
     * 企业-途经点统计
     */
    POINT_ORG_LIST_URL("/positional/org/pass/point/statistic/page", HttpMethod.POST),
    /**
     * 监控对象-途经点统计
     */
    POINT_MONITOR_LIST_URL("/positional/monitor/pass/point/statistic/page", HttpMethod.POST),
    /**
     * 监控对象-途经点明细
     */
    POINT_MONITOR_DETAIL_LIST_URL("/positional/monitor/pass/point/detail/page", HttpMethod.POST),

    /**
     * ACC统计报表列表
     */
    ACC_STATISTIC_LIST_URL("/accStatistic/report/list", HttpMethod.POST),

    /**
     * ACC统计报表详情列表
     */
    ACC_STATISTIC_DETAIL_LIST_URL("/accStatistic/report/detail", HttpMethod.POST)
    ;
    /**
     * uri
     * 如: /positional/travel/report
     */
    private final String path;
    /**
     * 请求方法
     */
    private final HttpMethod httpMethod;

    PaasCloudUrlEnum(String path, HttpMethod httpMethod) {
        this.path = path;
        this.httpMethod = httpMethod;
    }

    /**
     * path cloud api pair
     */
    private static final Map<String, String> API_URL = new HashMap<>(values().length);

    /**
     * 聚合address + path
     * @param address address
     */
    public static void assembleUrl(String address) {
        for (PaasCloudUrlEnum value : values()) {
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
