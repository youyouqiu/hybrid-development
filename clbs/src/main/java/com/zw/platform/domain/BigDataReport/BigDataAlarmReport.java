package com.zw.platform.domain.BigDataReport;

import lombok.Data;


@Data
public class BigDataAlarmReport {
    //监控对象名称
    private String monitorName;

    // 分组名称
    private String assignmentName;

    // 监控对象车牌颜色
    private String plateColor;

    // 监控对象类型
    private String monitorType;

    // 报警类型
    private Integer alarmType;

    // 报警次数
    private Integer alarmNum;

    private byte[] vehicleIdByte;
}
