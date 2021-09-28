package com.zw.platform.domain.BigDataReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.util.List;

/**
 * @author denghuabing on 2019/12/16 17:44
 */
@Data
public class OnlineReportData {

    private String monitorId;
    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 标识颜色
     */
    private Integer signColor;

    @ExcelField(title = "车牌颜色")
    private String color = "";

    @ExcelField(title = "上线天数")
    private Integer onlineDayNumber;

    @ExcelField(title = "总天数")
    private Integer countDayNumber;

    @ExcelField(title = "上线时长")
    private Long onlineDuration;

    @ExcelField(title = "上线次数")
    private Integer onlineCount;

    @ExcelField(title = "上线率")
    private String onlineRate;

    /**
     * 每天上线时间（每天第一条数据时间）
     */
    private List<String> firstDataTime;

    @ExcelField(title = "所属分组")
    private String assignmentName;

    @ExcelField(title = "从业人员")
    private String professionalName;
}
