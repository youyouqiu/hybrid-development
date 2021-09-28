package com.cb.platform.dto;

import com.zw.platform.basic.dto.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/6 18:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VehicleOnlineRateQuery extends BasePageQuery {
    private static final long serialVersionUID = 9041872604042052225L;
    /**
     * 企业id
     */
    private String enterpriseIds;

    /**
     * 监控对象id
     */
    private String vehicleIds;

    /**
     * 查询月份 yyyy-MM
     */
    private String month;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
