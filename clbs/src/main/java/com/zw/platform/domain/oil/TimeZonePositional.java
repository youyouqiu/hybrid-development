package com.zw.platform.domain.oil;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouzongbo on 2019/5/5 15:59
 */
@Data
public class TimeZonePositional implements Serializable {
    private static final long serialVersionUID = 5406135381479865255L;

    private byte[] vehicleId;

    private String vehicleIdStr;
    /**
     * 监控对象ID，用作调用paas_cloud接口
     */
    private String monitorId;

    /**
     * 经度
     */
    private String longtitude;

    /**
     * 纬度
     */
    private String latitude;

    private String startTimeFormat;

    private String endTimeFormat;

    private String monitorNumber;

    /**
     * 避免区域相同的情况下,出现对象
     */
    private String areaName;

    private Long vtime;

    private String startTime;

    private String endTime;
}
