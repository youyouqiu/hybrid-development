package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

@Data
public class OfflineDetailInfo {
    /**
     * 离线位移公里数
     */
    private Double displaceMile;
    /**
     * 离线结束时间 时间戳,单位:秒
     */
    private Long endTime;
    /**
     * 离线开始时间 时间戳,单位:秒
     */
    private Long startTime;

    /**
     * 离线结束时间 格式 2020-01-01 12:00:00
     */
    private String offLineEndTimeStr;
    /**
     * 离线开始时间 格式 2020-01-01 12:00:00
     */
    private String offLineStartTimeStr;
}
