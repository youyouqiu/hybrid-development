package com.zw.platform.domain.alarm;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/12/28 16:12
 */
@Data
public class ForwardAlarmInfo implements Serializable {
    private static final long serialVersionUID = 4361618743924964848L;
    /**
     * 监控对象id
     */
    private String monitorId;
    private String monitorType;

    @ExcelField(title = "车牌号")
    private String monitorName;

    @ExcelField(title = "标识颜色")
    private String plateColor;

    @ExcelField(title = "从业人员")
    private String professionalsName;

    @ExcelField(title = "所属企业")
    private String groupName;

    @ExcelField(title = "分组")
    private String assignmentName;

    @ExcelField(title = "处理状态")
    private String alarmStatus;
    private Integer status;

    @ExcelField(title = "报警类型")
    private String description;

    @ExcelField(title = "严重程度")
    private String severityName;
    private Double severity;

    @ExcelField(title = "报警来源")
    private String alarmSourceStr;
    private Integer alarmSource;

    @ExcelField(title = "报警开始速度")
    private String speed;

    @ExcelField(title = "道路类型")
    private String roadTypeStr;
    private Integer roadType;

    @ExcelField(title = "平台限速")
    private Double speedLimit;

    @ExcelField(title = "路网限速")
    private Double roadNetSpeedLimit;

    @ExcelField(title = "超速时长")
    private Integer overSpeedTime;

    @ExcelField(title = "报警时间")
    private String startTimeStr;
    private Long alarmStartTime;
    private String alarmStartTimeStr;

    /**
     * 持续报警开始的时间 毫秒
     */
    private Long alarmTime;


    @ExcelField(title = "报警经度")
    private String alarmLongitude;

    @ExcelField(title = "报警纬度")
    private String alarmLatitude;

    //@ExcelField(title = "报警位置")
    private String alarmLocation;

    @ExcelField(title = "报警位置")
    private String alarmAddress;

    @ExcelField(title = "围栏类型")
    private String fenceType;

    @ExcelField(title = "围栏名称")
    private String fenceName;

    @ExcelField(title = "漏报类型")
    @Setter(AccessLevel.NONE)
    private String  omissionAlarmType;

    @ExcelField(title = "处理人")
    private String personName;

    @ExcelField(title = "处理时间")
    private String handleTimeStr;
    private Long handleTime;

    /**
     * 809转发主动安全关联的riskEventId
     */
    private String riskEventId;

    private String functionId;

    @ExcelField(title = "处理方式")
    private String handleType;

    @ExcelField(title = "备注")
    private String remark;

    private String protocolType;

    /**
     * 报警类型
     */
    private Integer alarmType;

    private String swiftNumber;

    /**
     * 漏报类型 1：正常，0：漏报
     */
    private Integer failureFlag = 1;

    public String getOmissionAlarmType() {
        if (failureFlag == 0) {
            return "终端漏报";
        }
        return "";
    }
}
