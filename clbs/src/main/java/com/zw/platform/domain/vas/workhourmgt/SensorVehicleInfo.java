package com.zw.platform.domain.vas.workhourmgt;

import lombok.Data;

import java.io.Serializable;

/**
 * 车辆绑定传感器公共DTO
 * create by phj on 2018-9-10
 */
@Data
public class SensorVehicleInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工时
     */
    public static final int SENSOR_TYPE_WORK_HOUR = 4;
    /**
     * 液位
     */
    public static final int SENSOR_TYPE_LIQUID_LEVEL = 5;
    /**
     * 载重
     */
    public static final int SENSOR_TYPE_LOAD = 6;
    /**
     * 胎压
     */
    public static final int SENSOR_TYPE_TYRE_PRESSURE = 7;
    /**
     * 工时传感器与车辆关联
     */
    private String id;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 传感器ID
     */
    private String sensorId;

}