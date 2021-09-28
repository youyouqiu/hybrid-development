package com.zw.platform.domain.statistic;

import lombok.Data;

import java.io.Serializable;

@Data
public class TyrePressureReportInfo implements Serializable {

    private static final long serialVersionUID = 490067412273767632L;

    private String vehicleId;

    /**
     * gps时间
     */
    private long vtime = 0L;

    private String vtimeStr = "";

    /**
     * 经度
     */
    private String longtitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 速度
     */
    private String speed;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 总里程
     */
    private String totalMileage;

    /**
     * 轮胎号
     */
    private Integer tyreNumber;

    /**
     * 轮胎压力
     */
    private String pressure;

    /**
     * 轮胎温度
     */
    private String temperature;

    /**
     * 电池电量
     */
    private String electric;

    /**
     * 胎压数据
     */
    private String tirePressureParameter;
}
