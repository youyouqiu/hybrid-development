package com.zw.platform.domain.reportManagement;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;

/**
 * 报警信息转发实体
 * @author Administrator
 */
@Data
public class AlarmForwardInfo implements Serializable {
    private static final long serialVersionUID = 4100198166320555863L;

    /**
     * 报警编号
     */
    private Integer id;

    /**
     * 监控对象id
     */
    private byte[] monitorId;

    private String vehicleId;

    @ExcelField(title = "车牌号")
    private String plateNumber;

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

    @ExcelField(title = "报警类型")
    private String description;

    @ExcelField(title = "严重程度")
    private String severityName;

    private Integer alarmSource;

    @ExcelField(title = "报警来源")
    private String alarmSourceStr;

    @ExcelField(title = "报警开始速度")
    private String speed;

    private Integer roadType;

    @ExcelField(title = "道路类型")
    private String roadTypeStr;

    @ExcelField(title = "平台限速")
    private Double speedLimit;

    @ExcelField(title = "路网限速")
    private Double roadNetSpeedLimit;

    @ExcelField(title = "超速时长")
    private Integer speedTime;

    /**
     * 持续报警的开始时间
     */
    private Long startTime;

    @ExcelField(title = "报警时间")
    private String startTimeStr;

    @ExcelField(title = "报警经度")
    private String alarmLongitude;

    @ExcelField(title = "报警纬度")
    private String alarmLatitude;

    @ExcelField(title = "报警位置")
    private String alarmLocation;

    @ExcelField(title = "围栏类型")
    private String fenceType;

    @ExcelField(title = "围栏名称")
    private String fenceName;

    @ExcelField(title = "处理人")
    private String personName;

    @ExcelField(title = "处理时间")
    private String handleTime;

    /**
     * 809转发主动安全关联的riskEventId
     */
    private String riskEventId;

    private byte[] riskEventByteId;

    private String functionId;

    /**
     * 状态
     */
    private int status;

    @ExcelField(title = "处理方式")
    private String handleType;

    @ExcelField(title = "备注")
    private String remark;

    private String protocolType;

    /**
     * 报警时间(gpsTime)
     */
    private Long time;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 严重程度
     */
    private Double severity;

    /**
     * 报警计算标准：0普通标准 1山西标准 2四川标准
     */
    private Integer calStandard;

    private String swiftNumber;
}
