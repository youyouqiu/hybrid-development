package com.sx.platform.domain.sxReport;


import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author
 * 超速报警明细实体，对应页面列表
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SxSpeedAlarmReport {
    private byte[] vehicleId; // 车辆ID

    private String vehicleIdStr; // 车辆ID

    private Integer alarmType; // 报警类型

    private String alarmTypeStr; // 报警类型

    @ExcelField(title = "车牌号")
    private String brnad = ""; // 车牌号

    @ExcelField(title = "车牌颜色")
    private String color = ""; // 车牌颜色

    @ExcelField(title = "分组")
    private String assignmentName = ""; // 分组名称

    @ExcelField(title = "所属企业")
    private String groupName = ""; // 所属企业

    private Integer speedType; // 限速类型

    @ExcelField(title = "限速类型")
    private String speedTypeStr; // 限速类型

    @ExcelField(title = "最高速度")
    private String speed; // 最高速度

    @ExcelField(title = "车辆类型")
    private String vehicleType = ""; // 车辆类型

    private Long alarmStartTime; // 报警开始时间

    private Long alarmEndTime; // 报警结束时间

    @ExcelField(title = "报警开始时间")
    private String alarmStartTimeStr; // 报警开始时间

    @ExcelField(title = "报警结束时间")
    private String alarmEndTimeStr; // 报警结束时间

    @ExcelField(title = "持续时长")
    private String durationTime; // 持续时长

    @ExcelField(title = "报警开始位置")
    private String alarmStartLocation = " "; // 报警开始位置

    @ExcelField(title = "报警结束位置")
    private String alarmEndLocation = ""; // 报警结束位置

    @ExcelField(title = "从业人员")
    private String professionalName = ""; // 从业人员

    @ExcelField(title = "电话")
    private String phone = ""; // 电话
}
