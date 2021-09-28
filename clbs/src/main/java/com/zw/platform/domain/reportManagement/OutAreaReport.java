package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

/**
 * 出区划累计时长报表实体
 */
@Data
public class OutAreaReport implements Serializable {
    private static final long serialVersionUID = 4355732395400076739L;

    /**
     * 监控对象id
     */
    private String monitorId;
    private byte[] monitorIdByte;

    /**
     * 日期 时间戳,单位:秒
     */
    private Long day;

    /**
     * 0:表示该日期出区域 1:表示该日期未出区域
     */
    private Integer isOut;

    /**
     * 出区域时间 时间戳 单位:秒
     */
    private Long outTime;

    /**
     * 出区划累计时长 单位:天
     */
    private Integer outDuration;
}
