package com.cb.platform.domain.sichuan.vehicleonlinerate;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/7 10:38
 */
@Data
public class VehicleOnlineRateVehicleMonthReportDO {
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
     * 所属企业
     */
    private String orgName;

    /**
     * 格式:yyyyMM
     */
    private String month;

    /**
     * 合计
     */
    private Double total;

    /**
     * 在线率明细 (四舍五入后保留两位小数)
     */
    private Double[] days;
}
