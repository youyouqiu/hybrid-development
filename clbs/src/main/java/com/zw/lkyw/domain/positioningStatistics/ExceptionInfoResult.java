package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

@Data
public class ExceptionInfoResult {
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
     * 无效数
     */
    private Integer invalidNum;
    /**
     * 定位数
     */
    private Integer locationNum;
    /**
     * 定位时间 时间戳,单位: 秒
     */
    private Long time;

    /**
     * 定位时间 （格式20200106）
     */
    private String timeStr;

    /**
     * 下标
     */
    private int index;
}
