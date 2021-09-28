package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/10 16:40
 */
@Data
public class PassCloudMileageDailyDetail implements Serializable {
    private static final long serialVersionUID = -7117758716570927522L;
    /**
     * 日期 时间戳:秒
     */
    private Long day;
    /**
     * 终端总里程
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
     * 终端怠速里程
     */
    private Double deviceDownMile;
    /**
     * 终端怠速时长
     */
    private Long deviceDownTime;
    /**
     * 里程传感器总里程
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
     * 传感器标识 0：没有 1：有
     */
    private Integer sensorFlag;
}
