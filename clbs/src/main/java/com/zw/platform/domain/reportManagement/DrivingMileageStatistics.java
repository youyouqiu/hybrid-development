package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * 部标监管报表-行驶里程报表
 * @author penghj
 * @version 1.0
 * @date 2019/12/13 11:52
 */
@Data
public class DrivingMileageStatistics implements Serializable {
    private static final long serialVersionUID = 1016472050396985742L;

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
     * 企业名称
     */
    @ExcelField(title = "所属企业")
    private String groupName;
    /**
     * 分组名称
     */
    @ExcelField(title = "分组")
    private String assignmentName;
    /**
     * 标识颜色 车牌颜色
     */
    @ExcelField(title = "标识颜色")
    private String signColor;
    /**
     * 监控对象类型
     */
    @ExcelField(title = "监控对象类型")
    private String monitorType;
    /**
     * 行驶时长 秒
     */
    private Long deviceDuration;
    private Long sensorDuration;
    private Long duration;
    @ExcelField(title = "行驶时长")
    private String durationStr;
    /**
     * 行驶里程 km
     */
    private Double deviceMile;
    private Double sensorMile;
    @ExcelField(title = "行驶里程(KM)")
    private Double totalMile;
    /**
     * 油耗 L
     */
    @ExcelField(title = "油耗(L)")
    private Double oilWear;
    /**
     * 行驶次数
     */
    private Integer deviceTravelNum;
    private Integer sensorTravelNum;
    @ExcelField(title = "行驶次数")
    private Integer travelNum;
}
