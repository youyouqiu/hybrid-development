package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Title: 日志查询
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
 * @version 1.0
 * @author: wangying
 * @date 2017年4月7日下午14:00:00
 */
@Data
public class LogSearch implements Serializable {
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
     * 操作时间
     */
    @ExcelField(title = "操作时间")
    private Date eventDate;


    /**
     * ip地址
     */
    @ExcelField(title = "IP地址")
    private String ipAddress;


    /**
     * 操作人
     */
    @ExcelField(title = "操作人")
    private String username;

    /**
     * 企业id
     */
    private String orgId;

    /**
     * 监控对象
     */
    @ExcelField(title = "监控对象")
    private String brand;

    /**
     * 车牌颜色
     */
    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    /**
     * 日志内容
     */
    @ExcelField(title = "操作内容")
    private String message;

    /**
     * 异常
     */
    private String exception;

    @ExcelField(title = "日志来源")
    private String logSource; // 日志来源

    private Integer flag;

    private String module; // 日志模块


    /**
     * 监控对象操作
     */
    private String monitoringOperation;


    /**
     * 车牌颜色
     */
    private Integer plateColor;


}
