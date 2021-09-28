package com.zw.platform.dto.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 停车信息实体类（clbs反前端实体类）
 *
 * @author tianzhangxu
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ParkingInfoDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 监控对象ID
     */
    private String monitorId;

    /**
     * 监控对象名称（车牌号）
     */
    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 分组名称
     */
    @ExcelField(title = "分组")
    private String assignmentName;

    /**
     * 从业人员名称
     */
    @ExcelField(title = "从业人员")
    private String employeeName;

    /**
     * 从业人员电话
     */
    @ExcelField(title = "电话")
    private String employeePhone;

    /**
     * 停止次数
     */
    @ExcelField(title = "停止次数")
    private int stopNum = 0;

    /**
     * 停止时长（秒）
     */
    private long duration = 0L;

    /**
     * 停止时长（xx小时xx分xx秒）
     */
    @ExcelField(title = "停止时长")
    private String stopTime;

    /**
     * 怠速里程
     */
    @ExcelField(title = "怠速里程（km）")
    private Double idleSpeedMile = 0.0;

    /**
     * 最后停止位置(经纬度)
     */
    private String stopLocation;

    /**
     * 最后停止位置
     */
    @ExcelField(title = "最后停止位置")
    private String address;

}