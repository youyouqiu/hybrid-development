package com.zw.platform.domain.vas.history;

import lombok.Data;


@Data
public class TrackPlayBackChartDataQuery {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 查询开始时间
     */
    private String startTime;

    /**
     * 查询结束时间
     */
    private String endTime;

    /**
     * 监控对象是否绑定里程传感器标识
     */
    private Integer sensorFlag;
}
