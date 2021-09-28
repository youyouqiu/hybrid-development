package com.cb.platform.dto.report.sichuan;

import com.zw.platform.basic.dto.query.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/9 9:11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VehicleAbnormalDrivingReportQuery extends BasePageQuery {
    private static final long serialVersionUID = -4923157261008976512L;

    /**
     * 企业id
     */
    private String orgIds;
    /**
     * 监控对象id
     */
    private String monitorIds;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 报警类型 全部:不传; 异动报警(客运车):7702; 异动报警(山路):7703
     */
    private Integer alarmType;
}
