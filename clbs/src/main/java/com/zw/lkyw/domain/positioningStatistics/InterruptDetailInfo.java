package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

@Data
public class InterruptDetailInfo {
    /**
     * 中断时长 单位:秒
     */
    private long duration;
    /**
     * 中断时长 HH:mm:ss格式
     */
    private String durationStr;
    /**
     * 结束位置(具体地址)
     */
    private String endAddress;
    /**
     * 结束位置(经纬度)
     */
    private String endLocation;
    /**
     * 开始位置(具体地址)
     */
    private String startAddress;
    /**
     * 开始位置(经纬度)
     */
    private String startLocation;
    /**
     * 开始时间 时间戳, 单位:秒
     */
    private Long startTime;
    /**
     * 结束时间 时间戳, 单位:秒
     */
    private Long endTime;

    /**
     * 开始时间  格式（2020-01-01 12:00:00）
     */
    private String startTimeStr;
    /**
     * 结束时间 格式（2020-01-01 12:00:00）
     */
    private String endTimeStr;
}
