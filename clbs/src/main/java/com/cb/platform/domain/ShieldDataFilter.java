package com.cb.platform.domain;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * @author zhangsq
 * @date 2018/5/15 15:27
 */
@Data
public class ShieldDataFilter {

    // 车牌号
    @ExcelField(title = "监控对象")
    private String brand;

    // 企业名称
    @ExcelField(title = "所属企业")
    private String groupName;

    //所属分组
    @ExcelField(title = "分组")
    private String assignmentName;

    // 车牌颜色
    @ExcelField(title = "标识颜色")
    private String color;

    // 车牌颜色
    @ExcelField(title = "对象类型")
    private String vehicleType;

    @ExcelField(title = "中断开始时间")
    private String breakStartTime;

    @ExcelField(title = "开始时速度")
    private String breakStartSpeed;

    @ExcelField(title = "开始时里程")
    private String breakStartTotalMileage;

    @ExcelField(title = "中断结束时间")
    private String breakEndTime;

    @ExcelField(title = "结束时速度")
    private String breakEndSpeed;

    @ExcelField(title = "结束时里程")
    private String breakEndTotalMileage;

    @ExcelField(title = "中断时长")
    private String longTime;

    @ExcelField(title = "中断纬度")
    private double breakLongitude;

    @ExcelField(title = "中断纬度")
    private double breakLatitude;

    @ExcelField(title = "中断位置")
    private String breakAddress;

    private double nextBreakLongitude;

    private double nextBreakLatitude;

    public ShieldDataFilter(String brand, String groupName, String assignmentName, String color, String vehicleType,
        String breakStartTime, String breakStartSpeed, String breakStartTotalMileage, double breakLongitude,
        double breakLatitude, double nextBreakLongitude, double nextBreakLatitude) {
        this.brand = brand;
        this.groupName = groupName;
        this.assignmentName = assignmentName;
        this.color = color;
        this.vehicleType = vehicleType;
        this.breakStartTime = breakStartTime;
        this.breakLongitude = breakLongitude;
        this.breakLatitude = breakLatitude;
        this.nextBreakLongitude = nextBreakLongitude;
        this.nextBreakLatitude = nextBreakLatitude;
        this.breakStartSpeed = breakStartSpeed;
        this.breakStartTotalMileage = breakStartTotalMileage;
    }

    public ShieldDataFilter() {
    }
}
