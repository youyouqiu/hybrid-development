package com.zw.platform.domain.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 日志信息
 */
@Data
public class LogInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id; // 操作日志
    private String logType; // 类型(操作日志,登录日志)
    private String message; // 消息
    private String exception; // 异常
    private String ipAddress; // IP
    private Date eventDate; // 时间
    private String username; // 用户名

    /**
     * 日志来源：1：终端上报，2：平台下发，3：平台操作，4：APP操作
     */
    public static final String LOG_SOURCE_DEVICE = "1";
    public static final String LOG_SOURCE_PLATFORM_SEND = "2";
    public static final String LOG_SOURCE_PLATFORM_OPERATOR = "3";
    public static final String LOG_SOURCE_APP = "4";
}