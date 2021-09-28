package com.zw.platform.domain.reportManagement.query;

import lombok.Data;

import java.util.Date;

@Data
public class DrivingRecordInfoQuery {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 流水号
     */
    private Integer msgSNAck;

    /**
     * 采集指令
     */
    private String collectionCommand;

    /**
     * 最大时间
     */
    private Date maxDate;

    /**
     * 最小时间
     */
    private Date minDate;

    /**
     * 消息
     */
    private String message;
}
