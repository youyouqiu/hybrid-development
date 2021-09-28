package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/12/17 16:28
 */
@Data
public class SpeedingInfo implements Serializable {
    private static final long serialVersionUID = -82243497186548347L;
    /**
     * 超速报表列表数据key
     */
    public static final String SPEEDING_REPORT_LIST_KEY = "speeding_report_list_";
    /**
     * 监控对象id
     */
    private String  monitorId;

    @ExcelField(title = "监控对象")
    private String monitorName;

    @ExcelField(title = "分组")
    private String assignmentName;

    @ExcelField(title = "从业人员")
    private String employeeName;

    @ExcelField(title = "超速次数")
    private Integer overSpeedNum;

    @ExcelField(title = "最大速度")
    private Double maxSpeed;

    @ExcelField(title = "最小速度")
    private Double minSpeed;

    @ExcelField(title = "平均速度")
    private Double averageSpeed;
}
