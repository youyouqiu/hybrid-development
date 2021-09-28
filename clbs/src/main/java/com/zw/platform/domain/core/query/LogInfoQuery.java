package com.zw.platform.domain.core.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


/**
 * 日志信息Query
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LogInfoQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id; // 操作日志
    private String logType; // 类型(操作日志,登录日志)
    private String message; // 消息
    private String exception; // 异常
    private String ipAddress; // IP
    private Date eventDate; // 时间
    private String username; // 用户名
    private Date startTime; // 开始时间
    private Date endTime; // 结束时间
}