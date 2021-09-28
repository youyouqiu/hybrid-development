package com.cb.platform.dto;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/23 16:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MonitorOffRoutePageQuery extends BaseQueryBean {
    private static final long serialVersionUID = -6631442171689875140L;
    /**
     * 监控对象id
     */
    private Set<String> monitorIds;
    /**
     * 月份 yyyy-MM
     */
    private String month;
}
