package com.cb.platform.domain;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/6 16:09
 */
@Data
public class VehicleMileageStatisticsDO {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 企业名称
     */
    private String orgName;

    /**
     * 行驶时间段 yyyyMMddHHmmss
     */
    private String driveStartTime;

    /**
     * 行驶结束时间 yyyyMMddHHmmss
     */
    private String driveEndTime;

    /**
     * 行驶里程数(gps里程)
     */
    private Double gpsMile;
}
