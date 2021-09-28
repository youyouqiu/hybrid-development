package com.cb.platform.domain.sichuan.vehicleonlinerate;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/7 9:50
 */
@Data
public class VehicleOnlineRateOrgMonthReportDO {
    /**
     * 企业id
     */
    private String orgId;

    /**
     * 企业名称
     */
    private String orgName;

    /**
     * 月份  格式:yyyyMM
     */
    private String month;

    /**
     * 合计 单位: %
     */
    private Double total;

    /**
     * 明细(四舍五入后保留两位小数) 单位:%
     */
    private Double[] days;
}
