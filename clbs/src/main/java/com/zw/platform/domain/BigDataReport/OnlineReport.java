package com.zw.platform.domain.BigDataReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class OnlineReport {
    private String vehicleId = ""; //车辆ID

    @ExcelField(title = "监控对象")
    private String carLicense = ""; //车牌号
    @ExcelField(title = "车牌颜色")
    private String color = ""; //车辆颜色
    @ExcelField(title = "上线天数")
    private Integer activeDays; //上线天数
    @ExcelField(title = "总天数")
    private Integer allDays; //总天数
    @ExcelField(title = "上线时长")
    private String onlineDurationStr = "0秒"; //上线时长(HH:mm:ss)
    @ExcelField(title = "上线次数")
    private Integer onlineCount = 0; //上线次数
    @ExcelField(title = "上线率")
    private String ratio = "0%"; //上线率
    @ExcelField(title = "所属分组")
    private String assignmentName = ""; //分组名称
    @ExcelField(title = "从业人员")
    private String professionalNames = ""; //从业人员姓名

    private List<String> firstDataTimes; //每天上线时间（每天第一条数据时间）
}