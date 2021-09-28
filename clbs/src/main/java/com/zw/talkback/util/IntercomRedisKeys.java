package com.zw.talkback.util;

public final class IntercomRedisKeys {

    private IntercomRedisKeys() {
        // 限制初始化
    }

    /**
     * 对讲对象模糊查询（Redis 9 分区）
     */
    public static final String INTERCOM_INFO_FUZZY_SEARCH = "monitor_intercomDeviceId_simcard_fuzzy_search";

    public static final String VEHICLE_SEPARATOR = "vehicle&";
    public static final String INTERCOM_DEVICE_SEPARATOR = "intercomDevice&";
    public static final String SIM_SEPARATOR = "simcard&";
    public static final String FUZZY_SEPARATOR = "&";

    /**
     * 对讲对象查询（Redis 9 分区）
     */
    public static final String INTERCOM_INFO_SORT_LIST = "sort_intercom_info_list";

    /**
     * 模糊查询值
     * @param monitorId  monitorId
     * @param intercomId 对讲终端ID
     * @param simCardId  SIM卡ID
     * @return value
     */
    public static String fuzzyValue(String monitorId, String intercomId, String simCardId) {
        return VEHICLE_SEPARATOR + monitorId + FUZZY_SEPARATOR + INTERCOM_DEVICE_SEPARATOR + intercomId
            + FUZZY_SEPARATOR + SIM_SEPARATOR + simCardId;
    }

    /**
     * 模糊查询字段
     * @param monitorName      监控对象名称
     * @param intercomDeviceId 对讲终端号
     * @param simCardNumber    SIM卡号
     * @return field
     */
    public static String fuzzyField(String monitorName, String intercomDeviceId, String simCardNumber) {
        return monitorName + FUZZY_SEPARATOR + intercomDeviceId + FUZZY_SEPARATOR + simCardNumber;
    }
}
