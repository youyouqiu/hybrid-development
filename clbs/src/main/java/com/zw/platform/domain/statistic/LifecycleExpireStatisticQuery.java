package com.zw.platform.domain.statistic;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author zhouzongbo on 2018/9/10 16:05
 */
@Data
public class LifecycleExpireStatisticQuery extends BaseQueryBean implements Serializable{
    private static final long serialVersionUID = -6975902634405843380L;

    /**
     * 查询时间 yyyy-MM-dd
     */
    private String queryDateStr;

    /**
     * 组织ID
     */
    private String groupId;

    private List<String> groupIdList;

    /**
     * 服务到期状态:全部: 0;未到期: 1;即将到期: 2; 已到期: 3
     */
    private Integer lifecycleStatus;

    /**
     * 监控对象ID
     */
    private Set<String> monitoryIds;

    /**
     * 服务到期提前提醒天数
     */
    private Integer expireRemindDays;

    /**
     * 查询时间+30
     */
    private Date expireRemindDate;

    /**
     * 过滤条件: 0: 服务到期报表查询; 1: 即将到期全局跳转服务到期报表查询;
     */
    private Integer filterType = 0;
}
