package com.zw.app.domain.alarm;

import lombok.Data;

import java.io.Serializable;


/**
 * 监控对象最新的一条报警信息实体
 */
@Data
public class MonitorNewAlarm implements Serializable {
    private String monitorId;

    private Long alarmTime;

    private Integer alarmType;

    private byte[] hbaseMonitorId;
}
