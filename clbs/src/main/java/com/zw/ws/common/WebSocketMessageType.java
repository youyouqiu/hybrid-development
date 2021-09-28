package com.zw.ws.common;

/**
 * Created by tdz on 2016/10/24.
 */
public class WebSocketMessageType {

    /**
     * 状态信息
     */
    public static final int VEHICLE_STATUS = 1;

    /**
     * 位置信息
     */
    public static final int VEHICLE_LOCATION = 2;

    /**
     * 报警信息
     */
    public static final int VEHICLE_ALARM = 3;

    /**
     * 缓存状态信息
     */
    public static final int VEHICLE_CACHE_STATUS = 4;

    /**
     * 报警信息
     */
    public static final int RISK_LOCATION = 5;

    /**
     * 全局报警
     */
    public static final int VEHICLE_ALARM_GLOBAL = 6;

    /**
     * 音视频信息
     */
    public static final int VEHICLE_MEDIA = 7;

    /**
     * 特殊报警
     */
    public static final int SPECIAL_REPORT = 8;

    /**
     * OBD信息
     */
    public static final int MONITOR_OBD_INFO = 10;

    /**
     * SOS报警信息
     */
    public static final int VEHICLE_SOSALARM = 12;
}
