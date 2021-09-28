package com.zw.platform.domain.oil;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by jiangxiaoqiang on 2016/9/28.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FuelConsumptionStatistics implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @ExcelField(title = "监控对象")
    private String plateNumber;// 车辆编号
    
    @ExcelField(title = "起驶时间")
    private String startTime;//开始时间
    
    @ExcelField(title = "停驶时间")
    private String endTime;//结束时间
    
    @ExcelField(title = "行驶时间")
    private String steerTime;//行驶时间
    
    @ExcelField(title = "耗油")
    private double fuelConsumption;//油耗
    
    @ExcelField(title = "行驶里程")
    private double steerMileage;//里程

    @ExcelField(title = "百公里油耗")
    private double perHundredKilimeters;//百公里油耗
    
    @ExcelField(title = "开始位置")
    private String startPositonal;//起始位置
    
    @ExcelField(title = "结束位置")
    private String endPositonal;//结束位置

    private double startMileage;
    private double endMileage;
    private double startOil;
    private double endOil;
    
    private String id;
    
    private String vehicleId;

}
