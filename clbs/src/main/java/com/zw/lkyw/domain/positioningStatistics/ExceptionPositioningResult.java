package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

@Data
public class ExceptionPositioningResult {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象name
     */
    private String monitorName;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 所属企业id
     */
    private String groupId;

    /**
     * 所属企业name
     */
    private String groupName;
    /**
     * 不定位天数
     */
    private int noLocationDayNum;
    /**
     * 定位率
     */
    private double locationRatio;

    /**
     * 定位率(字符串)
     */
    private String locationRatioStr;
    /**
     * 无效天数
     */
    private int invalidDayNum;
    /**
     * 无效定位数
     */
    private int invalidLocationNum;
    /**
     * 有效率 ((月定位总数-无效定位数)/ 月定位总数)*100%
     */
    private double ration;

    /**
     * 有效率 ((月定位总数-无效定位数)/ 月定位总数)*100%(字符串)
     */
    private String rationStr;
    /**
     * 下标
     */
    private int index;

}
