package com.sx.platform.domain.sxReport;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author
 * 超速违章明细实体，对应页面列表
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SxSpeedViolationReport {

    private byte [] vehicleId; // 车辆ID

    private String vehicleIdStr; // 车辆ID
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

    @ExcelField(title = "超速开始时间")
    private String alarmStartTime = ""; // 超速开始时间

    @ExcelField(title = "超速开始位置")
    private String startLocation = ""; // 超速开始位置

    @ExcelField(title = " 超速次数")
    private String alarmCount = ""; // 超速次数

    @ExcelField(title = "从业人员")
    private String professionalName = ""; // 从业人员

    @ExcelField(title = " 电话")
    private String phone = ""; // 电话

    @ExcelField(title = "超速时长")
    private String durationTime; // 超速时长
}
