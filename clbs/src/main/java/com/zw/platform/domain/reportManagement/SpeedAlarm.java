package com.zw.platform.domain.reportManagement;


import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * 车辆超速报警实体类
 * @author hujun
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SpeedAlarm implements Serializable {
    private static final long serialVersionUID = 1L;

    @ExcelField(title = "监控对象")
    private String plateNumber;//车牌号

    @ExcelField(title = "分组")
    private String assignmentName;//分组

    @ExcelField(title = "报警来源")
    private String alarmSource;//报警来源

    @ExcelField(title = "报警开始时间")
    private String alarmStartTime;//报警开始时间(时分秒)

    @ExcelField(title = "报警开始位置")
    private String alarmStartLocation;//报警开始位置

    @ExcelField(title = "报警开始车速")
    private String startSpeed;//报警开始车速

    @ExcelField(title = "报警结束时间")
    private String alarmEndTime;//报警结束时间（时分秒）

    @ExcelField(title = "报警结束位置")
    private String alarmEndLocation;//报警结束位置

    @ExcelField(title = "报警结束车速")
    private String endSpeed;//报警结束车速

    @ExcelField(title = "时长")
    private String duration;//报警时长

    private Long durationTime;//报警时长（秒数）

    private Long startTime;//报警开始时间

    private Long endTime;//报警结束时间

    private byte[] vehicleIdByte;
}
