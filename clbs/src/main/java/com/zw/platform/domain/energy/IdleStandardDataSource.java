package com.zw.platform.domain.energy;


import lombok.Data;

import java.io.Serializable;


/**
 * 怠速数据
 */
@Data
public class IdleStandardDataSource implements Serializable {

    private String vehicleId;// 车辆id

    private String plateNumber;// 车牌号

    private long vsTime;// 时间

    private Double totalDistance;// 总里程

    private Double speed;// 速度

    private Double totalFuel;// 总油耗

    private int accStatus;// acc状态

    private int airConditionStatus;// 空调状态
}
