package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;


@Data
public class MileageReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;//车牌号

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "分组")
    private String assignmentName;

    @ExcelField(title = "标识颜色")
    private String vehicleColor;//车牌颜色

    /**
     * 监控对象类型: 0：车，1：人, 2 :物
     */
    @ExcelField(title = "监控对象类型")
    private String monitorType;

    @ExcelField(title = "行驶时长")
    private String travelTimeStr;

    @ExcelField(title = "行驶里程(KM)")
    private Double totalGpsMile;//行驶里程

    @ExcelField(title = "油耗(L)")
    private Double totalOilWearOne;//油耗

    @ExcelField(title = "行驶次数")
    private Integer travelNum;

    private String vehicleId;//车id

    private byte[] vehicleIdHbase;//hbase中车辆的vehicleId

    private double totalMileage = 0.0; // 里程传感器总行驶里程

    private Double mileage = 0.0; //显示到页面的

    /**
     * 总里程
     */
    private Double totalMile;

    /**
     *
     */
    private Double accumulatedGpsMile;

    /**
     * 开始位置经纬度
     */
    private String startLocation;

    /**
     * 结束位置经纬度
     */
    private String endLocation;

    private String startDateTime;

    private String endDateTime;

    /**
     * 行驶时长
     */
    private Long travelTime;

    private Long day;

    private String dayFormat;

    /**
     * 日期
     */
    private String dayDate;
}
