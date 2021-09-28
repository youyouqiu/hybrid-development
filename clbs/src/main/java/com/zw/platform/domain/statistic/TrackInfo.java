package com.zw.platform.domain.statistic;

import lombok.Data;

/**
 * @author denghuabing on 2019/12/17 14:26
 */
@Data
public class TrackInfo {

    private String longitude;
    private String latitude;

    /**
     * 轨迹标识(0:异常点 1:正常点)
     */
    private Integer trackValid;

    /**
     * 轨迹段开始速度 单位:1km/h
     */
    private String startSpeed;
    /**
     * 轨迹段开始里程 单位:1km
     */
    private String startMileage;
    /**
     * 轨迹段结束速度 单位:1km/h
     */
    private String endSpeed;
    /**
     * 轨迹段结束里程 单位:1km
     */
    private String endMileage;
}
