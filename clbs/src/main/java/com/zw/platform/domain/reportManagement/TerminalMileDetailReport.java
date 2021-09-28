package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;


@Data
public class TerminalMileDetailReport  {

    /**
     * 监控对象id
     */
    private String vehicleId;

    /**
     * 监控对象名称
     */
    @ExcelField(title = "监控对象")
    private String carLicense;

    /**
     * 每日明细 用到的时间
     */
    @ExcelField(title = "日期")
    private String dayDate;

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

    public String getMonitorType() {
        return monitorType;
    }

    public void setMonitorType(String monitorType) {
        if ("0".equals(monitorType)) {
            this.monitorType = "车";
            return;
        }
        if ("1".equals(monitorType)) {
            this.monitorType = "人";
            return;
        }
        if ("2".equals(monitorType)) {
            this.monitorType = "物";
            return;
        }
        this.monitorType = monitorType;
    }

    /**
     * 行驶里程
     */
    @ExcelField(title = "行驶里程")
    private Double runMile = 0.0;

    /**
     * 停止里程
     */
    @ExcelField(title = "停止里程")
    private Double stopMile = 0.0;

    /**
     * 异常里程
     */
    @ExcelField(title = "异常里程")
    private Double abnormalMile = 0.0;
}
