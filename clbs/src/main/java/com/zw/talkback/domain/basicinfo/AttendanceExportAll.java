package com.zw.talkback.domain.basicinfo;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

@Data
public class AttendanceExportAll {

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

    @ExcelField(title = "日期")
    private String dayStr;

    /**
     * 实际工作时段
     */
    @ExcelField(title = "实际工作时段")
    private String actualWorkingPeriod;

    /**
     * 应工作天数
     */
    private Integer workDays;

    /**
     * 应工作总时长
     */
    private Long shouldWorkDuration;

    @ExcelField(title = "实际工作时长")
    private String actualWorkingDurationStr;


    @ExcelField(title = "应工作总时长")
    private String shouldWorkDurationStr;

    /**
     * 实际工作天数
     */
    private Integer actualWorkDays;

    /**
     * 实际工作时长
     */
    private Long actualWorkingDuration;

    /**
     * 出勤率
     */
    @ExcelField(title = "出勤率（%）")
    private String attendance;

    /**
     * 出勤率
     */
    private Double attendanceDouble;

}
