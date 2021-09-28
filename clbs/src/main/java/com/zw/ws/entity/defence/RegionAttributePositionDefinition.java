package com.zw.ws.entity.defence;

/**
 * Created by jiangxiaoqiang on 2016/10/21.
 * 区域属性定义，T808-2013版本表58
 */
public class RegionAttributePositionDefinition {
    public static final int ACCORDING_TIME = 0;

    public static final int LIMIT_SPEED = 1;

    /**
     * T808-2013版本表47,改动
     */
    public static final int MAX_AND_NIGNT_SPEED_AND_OVER_SPEED_TIME = 1;
    public static final int INTO_REGION_ALRAM_TO_PLATFORM = 3;

    public static final int OUT_REGION_ALARM_TO_PLATFORM = 5;

    public static final int INTO_REGION_ALRAM_TO_DRIVER = 2;

    public static final int OUT_REGION_ALARM_TO_DRIVER = 4;

    public static final int OPEN_DOOR = 8;

    public static final int IN_REGION_COMMUNICATION = 14;

    public static final int IN_REGION_NOT_COLLECT_GNSS = 15;
}
