package com.sx.platform.domain.sxReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class TiredViolationReport {

    private byte [] vehicleId; // 车辆ID

    @ExcelField(title = "车牌号")
    private String brnad = ""; // 车牌号

    @ExcelField(title = "所属企业")
    private String groupName = ""; // 所属企业

    @ExcelField(title = "分组")
    private String assignmentName = ""; // 分组名称

    @ExcelField(title = "车牌颜色")
    private String color = ""; // 车辆颜色

    @ExcelField(title = "车辆类型")
    private String vehicleType = ""; // 车辆类型

    @ExcelField(title = "疲劳驾驶开始时间")
    private String alarmStartTime = ""; // 疲劳驾驶开始时间

    @ExcelField(title = "疲劳驾驶开始位置")
    private String startLocation = ""; // 疲劳驾驶开始位置

    @ExcelField(title = " 疲劳驾驶次数")
    private String alarmCount = ""; // 疲劳驾驶次数

    @ExcelField(title = "从业人员")
    private String professionalName = ""; // 从业人员

    @ExcelField(title = " 电话")
    private String phone = ""; // 电话

}
