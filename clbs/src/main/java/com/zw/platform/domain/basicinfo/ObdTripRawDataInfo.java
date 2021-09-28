package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/3/8 14:37
 */
@Data
public class ObdTripRawDataInfo implements Serializable {
    private static final long serialVersionUID = -4392349368455663027L;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 外设IdA0
     */
    private Integer id;
    /**
     * 长度
     */
    private Integer len;
    /**
     * 行程阶段 1:开始 2:结束 4:行程中;
     */
    private Integer hodometerphase;
    /**
     * 行程序号
     */
    private Long hodometerNumber;
    /**
     * 开始时间
     */
    private String startTime;
    /**
     * 结束时间
     */
    private String endTime;
    /**
     * 行程时长
     */
    private Integer hodometerDuration = 0;
    /**
     * 行程内行驶时长
     */
    private Integer runDuration;
    /**
     * 行程内行驶里程
     */
    private Double runMileage;
    /**
     * 行程内怠速次数
     */
    private Integer idlingCount;
    /**
     * 行程内怠速时长
     */
    private Integer idlingTime;
    /**
     * 行程总油耗
     */
    private Double allOilMouse;
    /**
     * 行程内行驶油耗
     */
    private Double runOilMouse;
    /**
     * 行程内怠速油耗
     */
    private Double idlingOilMouse;
    /**
     * 行程内急加速次数
     */
    private Integer speedUpCount;
    /**
     * 行程内急减速次数
     */
    private Integer speedCutCount;
    /**
     * 行程内急转弯次数
     */
    private Integer wheelCount;
    /**
     * 行程内刹车次数
     */
    private Integer brakeCount;
    /**
     * 行程内离合次数
     */
    private Integer clutchCount;

    /**
     * 时间
     */
    private Long vtime;
}
