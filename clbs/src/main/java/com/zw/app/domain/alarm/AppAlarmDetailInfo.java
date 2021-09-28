package com.zw.app.domain.alarm;

import lombok.Data;

import java.io.Serializable;


@Data
public class AppAlarmDetailInfo implements Serializable {

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 报警开始时间
     */
    private Long alarmStarTime;

    /**
     * 报警结束时间
     */
    private Long alarmEndTime;

    /**
     * 报警类型
     */
    private Integer type;

    /**
     * 报警类型
     */
    private String name;

    /**
     * 报警地点
     */
    private String address;

    /**
     * 报警对应的值
     */
    private String alarmValue;

    /**
     * 报警处理状态 0：未处理 1：已处理
     */
    private Integer status;
}
