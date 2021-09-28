package com.zw.lkyw.domain.trackback;

import lombok.Data;

@Data
public class MileageDetail {
    /**
     * 监控对象id
     */
    private byte[] monitorIdByte;

    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 日期
     */
    private Long day;

    /**
     * gps 终端里程
     */
    private Double deviceMile;

    /**
     * 终端行驶里程
     */
    private Double deviceTravelMile;

    /**
     * 终端行驶时长
     */
    private Long deviceTravelTime;

    /**
     * 终端怠速时长
     */
    private Long deviceDownTime;

    /**
     * 终端怠速里程
     */
    private Double deviceDownMile;

    /**
     * 里程传感器里程
     */
    private Double sensorMile;

    /**
     * 里程传感器行驶里程
     */
    private Double sensorTravelMile;

    /**
     * 里程传感器行驶时长
     */
    private Long sensorTravelTime;

    /**
     * 里程传感器怠速里程
     */
    private Double sensorDownMile;

    /**
     * 里程传感器怠速时长
     */
    private Long sensorDownTime;
    /**
     * 是否绑定了里程传感器，0:代表没有绑定,1:代表绑定了里程传感器
     */
    private Integer sensorFlag;
}
