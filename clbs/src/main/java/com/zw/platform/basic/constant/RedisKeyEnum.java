package com.zw.platform.basic.constant;

/**
 * redisKey的枚举类
 * @author zhangjuan
 * @date 2020/09/24
 */
public enum RedisKeyEnum implements RedisKeysConvert {

    /**
     * 组织-分组ID缓存
     */
    ORG_GROUP("ORGANIZATION:GROUP:%s", 10),

    /**
     * 组织-分组ID缓存 正则匹配
     */
    ORG_GROUP_PATTERN("ORGANIZATION:GROUP:*", 10),

    /**
     * 分组-监控对象ID缓存
     */
    GROUP_MONITOR("GROUP:MONITOR:%s", 10),

    /**
     * 分组-监控对象 正则匹配
     */
    GROUP_MONITOR_PATTERN("GROUP:MONITOR:*", 10),

    /**
     * 用户-分组ID缓存
     */
    USER_GROUP("USER:GROUP:%s", 10),

    /**
     * 用户-分组ID缓存 正则匹配
     */
    USER_GROUP_PATTERN("USER:GROUP:*", 10),

    /**
     * 人员顺序缓存
     */
    PEOPLE_SORT_LIST("PEOPLE:SORT_LIST", 10),

    /**
     * 车辆顺序缓存
     */
    VEHICLE_SORT_LIST("VEHICLE:SORT_LIST", 10),

    /**
     * 物品顺序缓存
     */
    THING_SORT_LIST("THING:SORT_LIST", 10),

    /**
     * 企业下未绑定车辆缓存
     */
    ORG_UNBIND_VEHICLE("ORGANIZATION:UNBIND:VEHICLE:%s", 10),

    /**
     * 企业下未绑定车辆缓存pattern
     */
    ORG_UNBIND_VEHICLE_PATTERN("ORGANIZATION:UNBIND:VEHICLE:*", 10),

    /**
     * 企业下未绑定物品缓存
     */
    ORG_UNBIND_THING("ORGANIZATION:UNBIND:THING:%s", 10),

    /**
     * 企业下未绑定物品缓存pattern
     */
    ORG_UNBIND_THING_PATTERN("ORGANIZATION:UNBIND:THING:*", 10),

    /**
     * 企业下未绑定人员信息缓存
     */
    ORG_UNBIND_PEOPLE("ORGANIZATION:UNBIND:PEOPLE:%s", 10),

    /**
     * 企业下未绑定人员信息缓存pattern
     */
    ORG_UNBIND_PEOPLE_PATTERN("ORGANIZATION:UNBIND:PEOPLE:*", 10),

    /**
     * 监控对象信息缓存 -- 包括未绑定的和已绑定的
     */
    MONITOR_INFO("MONITOR:INFO:%s", 10),

    /**
     * 组织信息缓存  供F3使用
     */
    ORGANIZATION_INFO("ORGANIZATION:INFO:%s", 10),

    /**
     * 监控对象图标 --监控对象Id和图标名称缓存
     */
    MONITOR_ICON("MONITOR:ICON", 10),

    /**
     * 终端类型下，监控对象的的id和name的map缓存
     */
    MONITOR_PROTOCOL("MONITOR:PROTOCOL:%s", 10),

    /**
     * 终端类型与监控对象关系缓存的pattern
     */
    MONITOR_PROTOCOL_PATTERN("MONITOR:PROTOCOL:*", 10),

    /**
     * 监控对象信息缓存pattern
     */
    MONITOR_INFO_PATTERN("MONITOR:INFO:*", 10),

    /**
     * 监控对象、终端、SIM卡模糊搜索
     */
    FUZZY_MONITOR_DEVICE_SIMCARD("FUZZY:MONITOR_DEVICE_SIMCARD", 10),

    /**
     * 报警设置-报警联动-缓存信息: ALARM_LINKAGE:监控对象ID
     */
    ALARM_LINKAGE("ALARM_LINKAGE:%s", 12),

    /**
     * 用户车辆导出缓存
     */
    USER_VEHICLE_EXPORT("USER:VEHICLE_EXPORT:%s", 12),

    /**
     * 从业人员企业关系缓存
     */
    ORGANIZATION_PROFESSIONAL_ID("ORGANIZATION:PROFESSIONAL:ID:%s", 10),

    /**
     * 从业人员信息
     */
    PROFESSIONAL_INFO("PROFESSIONAL:INFO:%s", 10),

    /**
     * 从业人员排序
     */
    PROFESSIONAL_SORT_ID("PROFESSIONAL:SORT:ID", 10),

    /**
     * 从业人员模糊搜索
     */
    FUZZY_PROFESSIONAL("FUZZY:PROFESSIONAL", 10),

    /**
     * 企业下的终端缓存
     */
    ORG_DEVICE("ORGANIZATION:DEVICE:%s", 10),

    /**
     * 企业下未绑定终端缓存
     */
    ORG_UNBIND_DEVICE("ORGANIZATION:UNBIND:DEVICE:%s", 10),

    /**
     * 终端顺序缓存
     */
    DEVICE_SORT_LIST("DEVICE:SORT_LIST", 10),

    /**
     * 企业下的sim卡缓存
     */
    ORG_SIM_CARD("ORGANIZATION:SIM_CARD:%s", 10),

    /**
     * 企业下未绑定sim卡
     */
    ORG_UNBIND_SIM_CARD("ORGANIZATION:UNBIND:SIM_CARD:%s", 10),

    /**
     * sim卡顺序缓存
     */
    SIM_CARD_SORT_LIST("SIM_CARD:SORT_LIST", 10),

    /**
     * 信息配置顺序缓存
     */
    CONFIG_SORT_LIST("MONITOR:BIND_SORT_LIST", 10),

    /**
     * 企业下未绑定SIM卡
     */
    ORG_UNBIND_SIM("ORGANIZATION:UNBIND:SIM_CARD:%s", 10),

    /**
     * 对讲信息顺序列表
     */
    INTERCOM_SORT_LIST("MONITOR:INTERCOM_SORT_LIST", 10),

    /**
     * 对讲信息模糊搜索
     */
    FUZZY_INTERCOM("FUZZY:INTERCOM", 10),

    /**
     * 车辆-邮箱传感器
     */
    VEHICLE_OIL_BOX_MONITOR_LIST("vehicle_oil_box_monitor_list", 10),

    /**
     * 车辆-油位传感器
     */
    VEHICLE_OIL_SENSOR_LIST("vehicle_oil_sensor_list", 10),

    /**
     * 车辆-油耗传感器
     */
    VEHICLE_OIL_CONSUME_MONITOR_LIST("vehicle_oil_consume_monitor_list", 10),

    /**
     * 车辆-振动传感器
     */
    VEHICLE_SHOCK_MONITOR_LIST("vehicle_shock_monitor_list", 10),

    /**
     * 车辆-温度传感器
     */
    VEHICLE_TEMPERATURE_MONITOR_LIST("vehicle_temperature_monitor_list", 10),

    /**
     * 车辆-湿度传感器
     */
    VEHICLE_WET_MONITOR_LIST("vehicle_wet_monitor_list", 10),

    /**
     * 车辆- 时传感器
     */
    VEHICLE_ROTATE_MONITOR_LIST("vehicle_rotate_monitor_list", 10),

    /**
     * 车辆-里程传感器
     */
    VEHICLE_MILEAGE_MONITOR_LIST("vehicle_mileage_monitor_list", 10),

    /**
     * 工时传感器
     */
    WORK_HOUR_SETTING_MONITORY_LIST("vehicle_work_hour_monitory_list", 10),

    /**
     * 载重传感器
     */
    LOAD_SETTING_MONITORY_LIST("vehicle_load_monitory_list", 10),

    /**
     * OBD管理设置
     */
    OBD_SETTING_MONITORY_LIST("vehicle_obd_monitory_list", 10),

    /**
     * 胎压监测设置
     */
    TYRE_PRESSURE_MONITORY_LIST("vehicle_tyre_pressure_monitory_list", 10),

    /**
     * obd行程统计数据
     */
    OBD_TRIP_DATA_EXPORT_KEY("obdTripDataExportKey", 3),

    /**
     *表单重复提交标记
     */
    FORM_REPEAT_SUBMIT_HASH_CODE("form_repeat_submit_hash_code:%s", 8),

    /**
     * monitor_id_address
     */
    MONITOR_ID_ADDRESS("%s", 11),

    /**
     * 信息列表查询导出数据
     */
    USER_CONFIG_EXPORT("%s_userConfigExport", 10),

    /**
     * 从业人员列表查询导出数据
     */
    USER_PROFESSIONAL_EXPORT("%s_userProfessionalExport", 10),

    /**
     * 人员信息列表查询导出数据
     */
    USER_PEOPLE_EXPORT("%s_userPeopleExport", 10),

    /**
     * 物品信息列表查询导出数据
     */
    USER_THING_EXPORT("%s_userThingExport", 10),

    /**
     * 终端管理列表查询导出数据
     */
    USER_DEVICE_EXPORT("%s_userDeviceExport", 10),

    /**
     * SIM卡管理列表查询导出数据
     */
    USER_SIM_CARD_EXPORT("%s_userSimCardExport", 10),

    /**
     * 登录失败信息
     */
    FAIL_LOGIN("FAIL_LOGIN:%s", 8),

    /**
     * 主动安全后处理车辆列表
     */
    ADAS_PIC_POSTPROCESS_ID("ADAS:PIC_POSTPROCESS:ID", 12),

    /**
     * 主动安全后处理车辆列表
     */
    ADAS_PIC_POSTPROCESS_LIST("ADAS:PIC_POSTPROCESS:LIST", 12),
    ;

    private final String pattern;
    private final int dbIndex;

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public int getDbIndex() {
        return dbIndex;
    }

    RedisKeyEnum(String pattern, int dbIndex) {
        this.pattern = pattern;
        this.dbIndex = dbIndex;
    }

}
