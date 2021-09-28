package com.zw.lkyw.domain.positioningStatistics;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class GroupListQueryParam extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * /**
     * 企业ids
     */
    private String groupIds;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 模糊搜索内容
     */
    private String search;

    /**
     * 0代表点击查询按钮，1代表其他的查询
     */
    private int searchType;

}
