package com.cb.platform.dto;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/23 16:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MonitorOffRouteDetailPageQuery extends BaseQueryBean {
    private static final long serialVersionUID = -6631442171689875140L;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 月份 yyyy-MM
     */
    private String month;
}
