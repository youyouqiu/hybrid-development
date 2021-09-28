package com.zw.talkback.domain.basicinfo;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

@Data
public class MileageExport {

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

    private String actualWorkingDurationStr;

    /**
     * 出勤率
     */
    private String attendance;

    /**
     * 实际工作时段
     */
    private String actualWorkingPeriod;

    /**
     * 当日有效里程
     */
    @ExcelField(title = "实际工作有效总里程（km）")
    private Double dayEffectiveMileage;

    /**
     * 平均里程
     */
    @ExcelField(title = "每天平均有效里程（km）")
    private String averageMileage;

    private Double averageMileageDouble;
}
