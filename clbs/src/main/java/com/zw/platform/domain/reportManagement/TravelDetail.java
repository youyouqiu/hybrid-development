package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * 行驶明细
 * @author zhouzongbo on 2018/12/12 16:56
 */
@Data
public class TravelDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;//车牌号

    @ExcelField(title = "开始时间")
    private String startDateTime;

    @ExcelField(title = "结束时间")
    private String endDateTime;

    @ExcelField(title = "行驶时长")
    private String travelTimeStr;

    @ExcelField(title = "行驶里程(km)")
    private double totalGpsMile = 0.0;//行驶里程

    @ExcelField(title = "累计里程(km)")
    private Double accumulatedGpsMile;

    @ExcelField(title = "油耗(L)")
    private double totalOilWearOne = 0.0;//油耗

    private Integer travelNum = 0;

    @ExcelField(title = "开始位置")
    private String startAddress;

    @ExcelField(title = "结束位置")
    private String endAddress;

    private String vehicleId;//车id

    private byte[] vehicleIdHbase;//hbase中车辆的vehicleId

    private double totalMileage = 0.0; // 里程传感器总行驶里程

    private double mileage = 0.0; //显示到页面的

    /**
     * 总里程
     */
    private Double totalMile;

    /**
     * 开始位置经纬度
     */
    private String startLocation;

    /**
     * 结束位置经纬度
     */
    private String endLocation;

    /**
     * 行驶时长
     */
    private Long travelTime = 0L;
}
