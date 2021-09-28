package com.zw.lkyw.domain.positioningStatistics;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExceptionListQueryParam extends BaseQueryBean implements Serializable {
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
     * 定位数阈值。定位数小于阈值,则当天算作不定位天数
     */
    private Integer locationNumThreshold;
    /**
     * 无效定位数阈值。无效定位数的数量如果大于阈值,则当天算作无效定位天数
     */
    private Integer invalidNumThreshold;

    /**
     * 0代表点击查询按钮，1代表其他的查询
     */
    private int searchType;
}
