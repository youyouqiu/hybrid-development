package com.cb.platform.domain;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/6 16:19
 */
@Data
public class VehicleDailyMileageReportDO {
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
    private String plateColor;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 所属企业
     */
    private String orgName;

    /**
     * 合计
     */
    private Double count;

    private Double[] days;
}
