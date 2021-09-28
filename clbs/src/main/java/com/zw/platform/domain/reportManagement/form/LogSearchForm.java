package com.zw.platform.domain.reportManagement.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Title: 日志Form
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
 *
 * @author wangying
 * @version 1.0
 * @date 2017年4月7日下午6:11:38
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LogSearchForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
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

    /**
     * 所属企业id
     */
    private String groupId;

    private String module; // 日志模块

    /**
     * 日志来源：1：终端上报，2：平台下发，3：平台操作 4: 单车登录小程序操作
     */
    private String logSource = "3";

    /**
     * 监控对象操作
     */
    private String monitoringOperation;

    private String brand = "-";

    /**
     * 车辆颜色
     */
    private Integer plateColor;
}
