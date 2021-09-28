package com.zw.platform.domain.vas.monitoring;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by Administrator on 2017/4/26.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmData extends BaseFormBean {
    private static final long serialVersionUID = 1L;
    @ExcelField(title = "监控对象")
    private String monitorName;

    @ExcelField(title = "所属分组")
    private String assignmentName;

    private Integer alarmType;
    @ExcelField(title = "报警类型")
    private String description;

    @ExcelField(title = "报警开始速度")
    private String speed;

    @ExcelField(title = "行车记录仪速度")
    private String recorderSpeed;

    @ExcelField(title = "处理状态")
    private String alarmStatus;
    private Integer status;

    @ExcelField(title = "处理人")
    private String personName;

    private long alarmStartTime;
    @ExcelField(title = "报警开始时间")
    private String startTime;

    private String alarmStartLocation;
    @ExcelField(title = "报警开始位置")
    private String startLocation;

    private long alarmEndTime;
    @ExcelField(title = "报警结束时间")
    private String endTime;

    private String alarmEndLocation;
    @ExcelField(title = "报警结束位置")
    private String endLocation;
    @ExcelField(title = "围栏类型")
    private String fenceType;

    @ExcelField(title = "围栏名称")
    private String fenceName;
    private String height;
    private byte[] vehicleIdByte;
    private String vehicleId;
}
