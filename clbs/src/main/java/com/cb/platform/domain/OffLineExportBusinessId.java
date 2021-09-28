package com.cb.platform.domain;

/**
 * 离线导出的业务id
 * @author Administrator
 */
public enum OffLineExportBusinessId {
    /**
     * 企业(编号:01)
     */
    SpeedOrgList("W010101"),
    /**
     * 企业超速明细报表 (编号:02)
     */
    SpeedOrgDetail("W010102"),
    /**
     * 监控对象超速统计报表(编号:01)
     */
    SpeedVehList("W010201"),
    /**
     * 监控对象超速统计明细报表(编号:02)
     */
    SpeedVehDetail("W010203"),

    /**
     * 809转发报警查询
     */
    ALARM_809_FORWARD("W020102"),

    /**
     * 企业疲劳驾驶统计报表(编号:01)
     */
    FatOrgList("W010303"),

    /**
     * 企业疲劳驾驶明细报表(编号:02)
     */
    FatOrgDetail("W010302"),

    /**
     * 监控对象疲劳驾驶统计报表(编号:01)
     */
    FatVehList("W010403"),

    /**
     * 监控对象疲劳驾驶明细报表(编号:02)
     */
    FatVehDetail("W010402"),

    /**
     * 离线位移日报表(编号:02)
     */
    OFFLINE_DISPLACEMENT("W030101"),

    /**
     * ACC报表统计报表及明细
     */
    ACC_STATISTICS_LIST("W030301"),

    /**
     * 报警漏报报表(编号:02)
     */
    OMISSION_ALARM("W040601"),

    /**
     * 报警查询离线导出(编号:01)
     */
    ALARM_RECORD_LIST("W020101"),

    /**
     * 车辆与终端运行状态离线导出（编号 01）
     */
    VEHICLE_DEVICE_STATE("W040101"),

    /**
     * 设备报修记录离线导出 （编号 01）
     */
    EQUIPMENT_REPAIR("W040201"),
    /**
     * 车辆信息统计汇总离线导出 （编号 01）
     */
    VEH_INFORMATION_STATICS_ORG("W040301"),
    /**
     * 车辆信息统计明细离线导出 （编号 02）
     */
    VEH_INFORMATION_STATICS_ORG_DETAIL("W040302"),
    /**
     * 连接情况统计(与政府平台连接情况) (编号:01)
     */
    CONNECTION_STATICS_PLATFORM("W040501"),
    /**
     * 政府监管平台连接情况每日明细 (编号:02)
     */
    CONNECTION_STATICS_PLATFORM_DETAIL("W040502"),
    /**
     * 连接情况统计(与车载终端连接情况) (编号:03)
     */
    CONNECTION_STATICS_MONITOR("W040503"),
    /**
     * 车载终端连接情况每日明细 (编号:04)
     */
    CONNECTION_STATICS_MONITOR_DETAIL("W040504"),
    /**
     * 车辆在线时长统计(按道路运行企业统计)(编号:01)
     */
    VEHICLE_ONLINE_TIME_ORG("W040401"),
    /**
     * 车辆在线时长统计(按车辆统计)(编号:02)
     */
    VEHICLE_ONLINE_TIME_MONITOR("W040402"),
    /**
     * 车辆在线时长统计(按行政区域统计)(编号:03)
     */
    VEHICLE_ONLINE_TIME_DIVISION("W040403"),
    /**
     * 途经点统计(按道路运输企业统计) (编号:01)
     */
    POINT_ORG("W040701"),
    /**
     * 途经点统计(按车辆统计) (编号:02)
     */
    POINT_MONITOR("W040702"),
    /**
     * 途经点统计(按车辆途径顺序统计) (编号:03)
     */
    POINT_MONITOR_DETAIL("W040703"),
    /**
     * 路线偏离统计 -> 路线偏离企业统计数据列表
     */
    ORG_OFF_ROUTE_STATISTICS_LIST("W010901"),
    /**
     * 路线偏离统计 -> 企业下监控对象路线偏离统计列表
     */
    ORG_MONITOR_OFF_ROUTE_STATISTICS_LIST("W010902"),
    /**
     * 路线偏离统计 -> 路线偏离车辆统计数据列表
     */
    MONITOR_OFF_ROUTE_STATISTICS_LIST("W010903"),
    /**
     * 路线偏离统计 -> 路线偏离车辆明细列表
     */
    MONITOR_OFF_ROUTE_DETAIL_LIST("W010904"),

    /* 四川监管报表 */

    /**
     * 车辆里程统计 - 车辆里程统计表
     */
    VEHICLE_MILEAGE_STATISTICS("W010701"),
    /**
     * 车辆里程统计 - 车辆日里程报表
     */
    VEHICLE_DAILY_MILEAGE_REPORT("W010702"),

    /**
     * 车辆在线率统计 - 车辆在线率道路运输企业统计月报表
     */
    VEHICLE_ONLINE_RATE_ORG_MONTH_REPORT("W010801"),
    /**
     * 车辆在线率统计 - 车辆在线率统计月报表
     */
    VEHICLE_ONLINE_RATE_VEHICLE_MONTH_REPORT("W010802"),
    /**
     * 车辆在线率统计 - 车辆在线明细
     */
    VEHICLE_ONLINE_DETAILS("W010803"),

    /**
     * 车辆异动统计 - 车辆异常行驶道路运输企业统计报表
     */
    VEHICLE_ABNORMAL_DRIVING_ORG_REPORT("W010601"),
    /**
     * 车辆异动统计 - 车辆异常行驶统计报表
     */
    VEHICLE_ABNORMAL_DRIVING_VEHICLE_REPORT("W010602"),
    /**
     * 车辆异动统计 - 车辆异常行驶明细报表
     */
    VEHICLE_ABNORMAL_DRIVING_VEHICLE_DETAIL_REPORT("W010603"),
    /**
     * 持续超速统计-持续超速道路运输企业统计表
     */
    CONTINUOUS_SPEED_ORG_REPORT("W010501"),
    /**
     * 持续超速统计-持续超速车辆统计表
     */
    CONTINUOUS_SPEED_VEHICLE_REPORT("W010502"),
    /**
     * 持续超速统计-持续超速车辆明细表
     */
    CONTINUOUS_SPEED_VEHICLE_DETAIL_REPORT("W010503"),
    /**
     * 违章记录表
     */
    VIOLATION_RECORD_REPORT("W050202"),
    /**
     * 值班交接班记录表
     */
    SHIFT_HANDOVER_RECORD_REPORT("W050102"),
    ;

    public String getBusinessId() {
        return businessId;
    }

    private final String businessId;

    OffLineExportBusinessId(String businessId) {
        this.businessId = businessId;
    }
}
