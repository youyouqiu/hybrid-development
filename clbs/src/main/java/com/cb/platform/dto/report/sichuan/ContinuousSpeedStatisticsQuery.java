package com.cb.platform.dto.report.sichuan;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/13 10:28
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ContinuousSpeedStatisticsQuery extends BaseQueryBean {
    private static final long serialVersionUID = -120944331121714553L;
    /**
     * 企业id
     */
    private String orgIds;
    /**
     * 监控对象id
     */
    private String monitorIds;
    /**
     * 开始时间 yyyy-MM-dd HH:mm:ss
     */
    private String startTime;
    /**
     * 结束时间 yyyy-MM-dd HH:mm:ss
     */
    private String endTime;
}
