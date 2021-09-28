package com.zw.talkback.domain.basicinfo.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

/**
 * 出勤报表Form
 */
@Data
public class AttendanceForm {

    private String monitorId;

    private byte[] monitorIdByte;

    @ExcelField(title = "监控对象")
    private String monitorName;

    private String scheduledInfoId;

    private byte[] scheduledInfoIdByte;

    private String groupId;

    @ExcelField(title = "所属组织")
    private String groupName;

    @ExcelField(title = "所属分组")
    private String assignmentName;

    /**
     * 日期（当天0点时间戳 秒）
     */
    private Long day;

    private String dayStr;

    /**
     * 应工作天数
     */
    @ExcelField(title = "应工作天数（天）")
    private Integer workDays;

    /**
     * 应工作总时长
     */
    private Long shouldWorkDuration;

    @ExcelField(title = "应工作总时长")
    private String shouldWorkDurationStr;

    /**
     * 实际工作天数
     */
    @ExcelField(title = "实际工作天数（天）")
    private Integer actualWorkDays;

    /**
     * 实际工作时长
     */
    private Long actualWorkingDuration;

    @ExcelField(title = "实际工作时长")
    private String actualWorkingDurationStr;

    /**
     * 出勤率
     */
    @ExcelField(title = "出勤率（%）")
    private String attendance;

    /**
     * 出勤率
     */
    private Double attendanceDouble;

    /**
     * 实际工作时段
     */
    private String actualWorkingPeriod;

    /**
     * 当日有效里程
     */
    private Double dayEffectiveMileage;

    /**
     * 平均里程
     */
    private String averageMileage;
}
