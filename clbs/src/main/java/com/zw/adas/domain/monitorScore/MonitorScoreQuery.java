package com.zw.adas.domain.monitorScore;

import lombok.Data;


@Data
public class MonitorScoreQuery {
    /**
     * es查询下一页标示
     */
    private Object[] searchAfter;

    /**
     * 每次返回条数(默认10)
     */
    private int limit = 10;

    /**
     * 查询时间
     */
    private int time;

    /**
     * 车辆id
     */
    private String vehicleId;

    public MonitorScoreQuery() {

    }

    public MonitorScoreQuery(String vehicleId, int limit, int time) {
        this.vehicleId = vehicleId;
        this.limit = limit;
        this.time = time;
    }
}
