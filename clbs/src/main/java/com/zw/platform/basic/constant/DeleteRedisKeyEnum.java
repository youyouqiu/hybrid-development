package com.zw.platform.basic.constant;

/**
 * 存放废弃的redisKey
 * 1、已经废弃不在使用缓存的Key
 * 2、redisKey名称发生改变、已经有新的redis替换
 */
public enum DeleteRedisKeyEnum implements RedisKeysConvert {
    /**
     * 车辆相关缓存
     */
    SORT_VEHICLE_LIST("sort_vehicle_list", 10, false),

    VEHICLE_INFO("*_vehicle_list", 10, true),

    ORG_UNBIND_VEHICLE("*_group_vehicle_list", 10, true),

    USER_VEHICLE_EXPORT("*_vehicle_export", 12, true),

    VEHICLE_COLOR("*_vehicle_color", 9, true),

    /**
     * 人员相关缓存
     */
    SORT_PEOPLE_LIST("sort_people_list", 10, false),

    PEOPLE_INFO("*_people_list", 10, true),

    ORG_UNBIND_PEOPLE("*_group_people_list", 10, true),

    /**
     * 物品相关缓存
     */
    SORT_THING_LIST("sort_thing_list", 10, false),

    THING_INFO("*_thing_list", 10, true),

    ORG_UNBIND_THING("*_group_thing_list", 10, true),

    /**
     * sim卡相关缓存
     */
    SORT_SIM_CARD_LIST("sort_simcard_list", 10, false),

    ORG_SIM_CARD_LIST("*_group_simcard_list", 10, true),

    ORG_UNBIND_SIM_CARD("*_group_unbind_simcard_list", 10, true),

    SIM_CARD_INFO("*_simcard_list", 10, true),

    /**
     * 终端相关缓存
     */
    SORT_DEVICE_LIST("sort_device_list", 10, false),

    ORG_DEVICE_LIST("*_group_device_list", 10, true),

    ORG_UNBIND_DEVICE("*_group_unbind_vdevice_list", 10, true),

    DEVICE_CARD_INFO("*_device_list", 10, true),

    /**
     * 组织、用户和分组相关缓存
     */
    ORG_INFO("*_organization_list", 9, true),

    ORG_NAME("*_group_name", 11, true),

    USER_GROUP("*_zw_list", 10, true),

    ORG_GROUP("*_group_assign_list", 10, true),

    GROUP_MONITOR("*_assignment_monitor_list", 10, true),

    /**
     * 从业人员相关缓存
     */
    SORT_PROFESSIONAL_LIST("sort_professional_list", 10, false),

    PROFESSIONAL_INFO("*_professional_list", 10, true),

    PROFESSIONAL_FUZZY("name_identity_state_fuzzy_search", 10, false),

    ORG_PROFESSIONAL("*_group_professional_list", 10, true),

    /**
     * 信息配置相关缓存
     */
    SORT_CONFIG_LIST("sort_config_list", 10, false),

    PROTOCOL_MONITOR("protocol_list_*", 10, true),

    CONFIG_INFO("*_config_list", 10, true),

    /**
     * 对讲信列表
     */
    SORT_INTERCOM_INFO_LIST("sort_intercom_info_list", 9, false),

    INTERCOM_INFO("*_intercom_list", 9, true),

    INTERCOM_FUZZY("monitor_intercomDeviceId_simcard_fuzzy_search", 9, false),

    /**
     * 监控对象、终端、sim卡模糊搜索
     */
    VEHICLE_DEVICE_SIMCARD_FUZZY_SEARCH("vehicle_device_simcard_fuzzy_search", 10, false),

    /**
     * 监控对象图标
     */
    MONITOR_ICON("*_useIco_list", 4, true),

    /**
     * 自定义列
     */
    USER_AlERT_WINDOW_REALTIME_DATA_LIST("*AlERT_WINDOW_REALTIME_DATA_LIST", 2, true),

    USER_MULTI_WINDOW_REALTIME_DATA_LIST("*MULTI_WINDOW_REALTIME_DATA_LIST", 2, true),

    USER_REALTIME_MONITORING_ACTIVE_SAFETY_LIST("*REALTIME_MONITORING_ACTIVE_SAFETY_LIST", 2, true),

    USER_REALTIME_MONITORING_ALARM_LIST("*REALTIME_MONITORING_ALARM_LIST", 2, true),

    USER_REALTIME_MONITORING_LIST("*REALTIME_MONITORING_LIST", 2, true),

    USER_REALTIME_MONITORING_LOG_LIST("*REALTIME_MONITORING_LOG_LIST", 2, true),

    USER_REALTIME_MONITORING_OBD_LIST("*REALTIME_MONITORING_OBD_LIST", 2, true),

    USER_TGAC_AlERT_WINDOW_REALTIME_DATA_LIST("*TGAC_AlERT_WINDOW_REALTIME_DATA_LIST", 2, true),

    USER_TGAC_RAPID_SCREENING("*TGAC_RAPID_SCREENING", 2, true),

    USER_TGAC_REALTIME_MONITORING_ACTIVE_SAFETY_LIST("*TGAC_REALTIME_MONITORING_ACTIVE_SAFETY_LIST", 2, true),

    USER_TGAC_REALTIME_MONITORING_ALARM_LIST("*TGAC_REALTIME_MONITORING_ALARM_LIST", 2, true),

    USER_TGAC_REALTIME_MONITORING_LIST("*TGAC_REALTIME_MONITORING_LIST", 2, true),

    USER_TRACKPLAY_ALARM("*TRACKPLAY_ALARM", 2, true),

    USER_TRACKPLAY_DATA("*TRACKPLAY_DATA", 2, true),

    USER_TRACKPLAY_OBD_LIST("*TRACKPLAY_OBD_LIST", 2, true),

    USER_TRACKPLAY_RUN("*TRACKPLAY_RUN", 2, true),

    USER_TRACKPLAY_SPEED("*TRACKPLAY_SPEED", 2, true),

    USER_TRACKPLAY_STOP("*TRACKPLAY_STOP", 2, true),

    EXPIRE_INSURANCE_ID("expireInsuranceId_list", 10, false),

    ;

    private final String pattern;
    private final int dbIndex;
    /**
     * 是否采用正则删除 true：采用正则删除 false：单个key删除
     */
    private boolean deleteByPattern;

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public int getDbIndex() {
        return dbIndex;
    }

    public boolean isDeleteByPattern() {
        return deleteByPattern;
    }

    DeleteRedisKeyEnum(String pattern, int dbIndex, boolean deleteByPattern) {
        this.pattern = pattern;
        this.dbIndex = dbIndex;
        this.deleteByPattern = deleteByPattern;
    }
}
