package com.zw.platform.dto.reportManagement;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 停车信息实体类（调用paas-cloud接口获取的数据对应实体类）
 *
 * @author tianzhangxu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ParkingInfoPaasDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 监控对象ID
     */
    private String monitorId;

    /**
     * 监控对象名称（车牌号）
     */
    private String monitorName;

    /**
     * 分组名称
     */
    private String assignmentName;

    /**
     * 终端停止次数
     */
    private int deviceStopNum = 0;

    /**
     * 终端停止时长 单位:秒
     */
    private long deviceDuration = 0L;

    /**
     * 终端怠速里程 单位：1km/h
     */
    private Double deviceIdleSpeedMile = 0.0;

    /**
     * 终端最后停止位置(经纬度)
     */
    private String deviceStopLocation;

    /**
     * 终端最后停止位置(具体地址)
     */
    private String deviceStopAddress;

    /**
     * 里程传感器停止次数
     */
    private int sensorStopNum = 0;

    /**
     * 里程传感器停止时长（秒）
     */
    private long sensorDuration = 0L;

    /**
     * 里程传感器怠速里程 单位:1km/h
     */
    private Double sensorIdleSpeedMile = 0.0;

    /**
     * 里程传感器最后停止位置（经纬度）
     */
    private String sensorStopLocation;

    /**
     * 里程传感器最后停止位置（具体地址）
     */
    private String sensorStopAddress;

}