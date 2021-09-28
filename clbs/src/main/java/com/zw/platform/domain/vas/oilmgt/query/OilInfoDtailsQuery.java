package com.zw.platform.domain.vas.oilmgt.query;

import lombok.Data;

/**
 * 油耗行驶记录
 * Created by Tdz on 2016/9/28.
 */
@Data
public class OilInfoDtailsQuery {
    private String plateNumber; //车牌号
    private String startTime;//开始时间
    private String endTime;//结束时间
    private String steerTime;//行驶时间
    private String fuelConsumption;//油耗
    private String steerMileage;//行驶里程
    private String perHundredKilometers;//百公里油耗
    private String startPositional;//开始位置
    private String endPositional;//结束位置
}
