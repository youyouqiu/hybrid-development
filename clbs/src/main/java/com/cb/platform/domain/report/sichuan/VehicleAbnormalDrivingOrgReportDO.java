package com.cb.platform.domain.report.sichuan;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/9 9:23
 */
@Data
public class VehicleAbnormalDrivingOrgReportDO {
    /**
     * 企业id
     */
    private String orgId;

    /**
     * 企业名称
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
