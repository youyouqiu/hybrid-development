package com.zw.lkyw.domain.positioningStatistics;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExceptionInfoQueryParam extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 监控对象ids
     */
    private String monitorId;
    /**
     * 时间
     */
    private String time;

    /**
     * 0代表点击查询按钮，1代表其他的查询
     */
    private int searchType;
}
