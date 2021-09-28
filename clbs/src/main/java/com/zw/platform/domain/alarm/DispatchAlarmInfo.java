package com.zw.platform.domain.alarm;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/11 13:01
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DispatchAlarmInfo extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1209417534947247920L;
    /**
     * 位置信息表ID
     */
    private String positionalId;

    /**
     * 处理人ID
     */
    private String personId;

    @ExcelField(title = "监控对象")
    private String monitorName;

    @ExcelField(title = "所属组织")
    private String name;

    @ExcelField(title = "所属分组")
    private String assignmentName;

    @ExcelField(title = "处理状态")
    private String alarmStatus;
    private Integer status;

    @ExcelField(title = "报警类型")
    private String description;

    @ExcelField(title = "报警开始时间")
    private String startTime;
    private Long alarmStartTime;

    @ExcelField(title = "报警结束时间")
    private String endTime;
    private Long alarmEndTime;

    @ExcelField(title = "报警持续时长")
    private String alarmDuration;

    @ExcelField(title = "报警开始位置")
    private String alarmStartSpecificLocation;

    @ExcelField(title = "报警结束位置")
    private String alarmEndSpecificLocation;

    @ExcelField(title = "围栏类型")
    private String fenceType;

    @ExcelField(title = "围栏名称")
    private String fenceName;

    @ExcelField(title = "处理人")
    private String personName;

    @ExcelField(title = "处理时间")
    private String handleTimeStr;
    private Long handleTime;

    @ExcelField(title = "处理描述")
    private String remark;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 报警来源
     */
    private Integer alarmSource;

    /**
     * 报警开始位置
     */
    private String alarmStartLocation;

    /**
     * 报警结束位置
     */
    private String alarmEndLocation;

    /**
     * 报警时长
     */
    private Long duration;

    private String monitorId;
    private String monitorType;
}
