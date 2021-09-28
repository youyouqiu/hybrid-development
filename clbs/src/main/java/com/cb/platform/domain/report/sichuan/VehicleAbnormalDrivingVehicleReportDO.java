package com.cb.platform.domain.report.sichuan;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/9 9:45
 */
@Data
public class VehicleAbnormalDrivingVehicleReportDO {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 标识颜色
     */
    private Integer plateColor;
    private String plateColorStr;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 所属企业
     */
    private String orgName;

    /**
     * 客运车禁行次数
     */
    private Integer passengerVehicleForbid;

    /**
     * 山区公路禁行
     */
    private Integer mountainRoadForbid;

    /**
     * 合计
     */
    private Integer total;
}
