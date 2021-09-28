package com.zw.platform.domain.vas.switching;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yangyi
 * @date 2019/11/22
 */
@Data
public class IoStatistics  implements Serializable {

    /**
     * IO口 1、2、3、4
     */
    private Integer ioOne;

    private Integer ioTwo;

    private Integer ioThree;

    private Integer ioFour;

    /**
     * 采集板1
     */
    private String ioObjOne;
    /**
     * 采集板2
     */
    private String ioObjTwo;

    /**
     * 车辆id
     */
    private String monitorId;

    /**
     * 车辆id
     */
    private byte [] vehicleId;

    /**
     * 状态
     */
    private String status;
    /**
     * Acc 状态
     */
    private Integer acc;
    /**
     * 定位状态
     */
    private String locationStatus;
    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 车牌号
     */
    private String monitorName;
    /**
     * 行驶里程
     */
    private String gpsMile;
    /**
     * 速度
     */
    private String speed;
    /**
     * 里程传感器车速
     */
    private Double mileageSpeed;

    /**
     * 里程传感器累积里程
     */
    private Double mileageTotal;

    /**
     * 位置数据更新时间
     */
    private long vTime;

    /**
     * 位置数据更新时间
     */
    private String vTimeStr;


    /**
     * 经度
     */
    private String longtitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 位置
     */
    private String address;
    /**
     * 位置坐标(纬度,经度)
     */
    private String positionCoordinates;



}
