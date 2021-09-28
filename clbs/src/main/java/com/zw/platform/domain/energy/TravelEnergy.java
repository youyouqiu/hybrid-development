package com.zw.platform.domain.energy;


import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = true)
public class TravelEnergy extends BaseFormBean implements Serializable {
    private String vehicleId; // 车ID

    private Date startTime; // 基准开始时间

    private Date endTime; // 基准结束时间

    private Integer travelTime; // 行驶时长

    private Double tavelMile; // 行驶里程

    private Double tavelFuel; // 行驶油耗

    private String airOpenTime; // 空调开启时长

    private Double travelBaseFuel; // 行驶油耗基准

    private Double travelBaseCapacity; // 行驶CO2基准

    private String brand; // 车牌号

    private String groupName; // 组织名

    private String vehicleType; // 车辆类型

    private String fuelType; // 燃油类型

    private Double avgSpeed; // 平均速度
}
