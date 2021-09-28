package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/12/18 9:27
 */
@Data
public class TerminalMileageStatistics implements Serializable {
    private static final long serialVersionUID = 7866450680725621451L;


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
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName;

    /**
     * 分组名称
     */
    @ExcelField(title = "分组")
    private String assignmentName;

    /**
     * 监控对象类型
     */
    @ExcelField(title = "监控对象类型")
    private String monitorType;

    /**
     * 总里程
     */
    @ExcelField(title = "总里程")
    private Double totalMile = 0.0;
    private Double deviceMile = 0.0;


    /**
     * 行驶里程
     */
    @ExcelField(title = "行驶里程")
    private Double travelMile = 0.0;
    private Double deviceTravelMile = 0.0;

    /**
     * 怠速里程
     */
    @ExcelField(title = "怠速里程")
    private Double idleSpeedMile = 0.0;
    private Double deviceIdlingMile = 0.0;

    /**
     * 异常里程
     */
    @ExcelField(title = "异常里程")
    private Double abnormalMile = 0.0;
}
