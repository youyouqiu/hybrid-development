package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * 部标监管报表-行驶里程报表
 * @author penghj
 * @version 1.0
 * @date 2019/12/16 15:05
 */
@Data
public class DrivingMileageDetails implements Serializable {
    private static final long serialVersionUID = 465149429330454196L;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 监控对象名称
     */
    @ExcelField(title = "监控对象")
    private String monitorName;
    /**
     * 开始时间
     */
    @ExcelField(title = "开始时间")
    private String startDateTime;
    /**
     * 结束时间
     */
    @ExcelField(title = "结束时间")
    private String endDateTime;
    /**
     * 行驶时长
     */
    @ExcelField(title = "行驶时长")
    private String duration;
    /**
     * 行驶里程 单位:1km/h
     */
    @ExcelField(title = "行驶里程(km)")
    private Double totalMile;
    /**
     * 累计里程 单位：1km/h
     */
    @ExcelField(title = "累计里程(km)")
    private Double accumulatedMile;
    /**
     * 油耗
     */
    @ExcelField(title = "油耗(L)")
    private Double totalOilWear;
    /**
     * 开始位置
     */
    @ExcelField(title = "开始位置")
    private String startAddress;
    private String startLocation;
    /**
     * 结束位置
     */
    @ExcelField(title = "结束位置")
    private String endAddress;
    private String endLocation;
}
