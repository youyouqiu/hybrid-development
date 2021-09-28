package com.zw.platform.domain.reportManagement.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Title: 日志查询Query
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2017年4月7日下午14:00:00
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LogSearchQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 日志表
     */
    private String id;
    /**
     * 日志类型
     */
    private String logType;

    /**
     * 日志内容
     */
    private String message;

    /**
     * 异常
     */
    private String exception;

    /**
     * 操作时间
     */
    private Date eventDate;

    /**
     * ip地址
     */
    private String ipAddress;

    /**
     * 操作用户名
     */
    private String username;

    private String startTime;

    private String endTime;

    private Integer flag;

    /**
     * 日志模块
     */
    private String module;

    /**
     * '日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作'
     */
    private String logSource;

    private List<String> groupIds;

    private Set<String> usernames;
}
