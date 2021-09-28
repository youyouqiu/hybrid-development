package com.zw.app.domain.alarm;

import lombok.Data;

import java.io.Serializable;


@Data
public class AppAlarmAction implements Serializable {
    /**
     *  查询时间段内的第几天
     */
    private Integer actionDay;

    /**
     * 具体的报警时间
     */
    private long  date;

    /**
     * 报警数量
     */
    private Integer alarmCount;
}
