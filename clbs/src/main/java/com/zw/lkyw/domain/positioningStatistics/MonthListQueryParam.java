package com.zw.lkyw.domain.positioningStatistics;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.io.Serializable;

@Data
public class MonthListQueryParam extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 监控对象ids
     */
    private String monitorIds;
    /**
     * 时间
     */
    private String time;
    /**
     * 模糊搜索内容
     */
    private String search;

    /**
     * 0代表点击查询按钮，1代表其他的查询
     */
    private int searchType;
}
