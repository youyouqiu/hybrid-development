package com.zw.platform.domain.alarm;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/12/26 15:51
 */
@Data
public class AlarmInfo implements Serializable {
    private static final long serialVersionUID = -3271184890609806576L;

    /**
     * 报警id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 监控对象id
     */
    private String monitorId;

    @ExcelField(title = "监控对象")
    private String monitorName;

    @ExcelField(title = "组织名称")
    private String assignmentName;

    @ExcelField(title = "所属企业")
    private String name;

    private String plateColor;
    @ExcelField(title = "车牌颜色")
    private String plateColorString;

    /**
     * 从业人员id
     */
    private String employeeId;

    @ExcelField(title = "从业人员名称")
    private String employeeName;

    private Integer alarmType;

    @ExcelField(title = "报警类型")
    private String description;

    @ExcelField(title = "严重程度")
    private String severityName;
    private Double severity;

    @ExcelField(title = "报警来源")
    private String alarmSourceString;
    private Integer alarmSource;

    @ExcelField(title = "报警开始速度（km/h）")
    private String speed;

    /**
     * 行车记录仪速度
     */
    @ExcelField(title = "行车记录仪速度")
    private String recorderSpeed;

    /**
     * 道路类型
     * 1：高速路 2：都市高速路 3：国道 4：省道 5：县道 6：乡村道路 7：其他道路
     */
    private Integer roadType;
    @ExcelField(title = "道路类型")
    private String roadTypeStr;

    @ExcelField(title = "平台限速")
    private Double speedLimit;

    @ExcelField(title = "路网限速")
    private Double roadNetSpeedLimit;

    @ExcelField(title = "超速时长(S)")
    private Integer speedTime;

    @ExcelField(title = "报警持续时间(S)")
    private Integer continuousTime;

    /**
     * 报警开始时间
     */
    @ExcelField(title = "报警开始时间")
    private String startTime;
    private Long alarmStartTime;

    /**
     * 报警结束时间 毫秒
     */
    @ExcelField(title = "报警结束时间")
    private String endTime;
    private Long alarmEndTime;

    /**
     * 报警开始位置(经度,纬度)
     */
    private String alarmStartLocation;
    @ExcelField(title = "报警开始经度")
    private String alarmStartLongitude;
    @ExcelField(title = "报警开始纬度")
    private String alarmStartLatitude;
    @ExcelField(title = "报警开始位置")
    private String alarmStartSpecificLocation;

    /**
     * 报警结束位置(经度,纬度)
     */
    private String alarmEndLocation;
    @ExcelField(title = "报警结束经度")
    private String alarmEndLongitude;
    @ExcelField(title = "报警结束纬度")
    private String alarmEndLatitude;
    @ExcelField(title = "报警结束位置")
    private String alarmEndSpecificLocation;

    @ExcelField(title = "围栏类型")
    private String fenceType;

    @ExcelField(title = "围栏名称")
    private String fenceName;

    @ExcelField(title = "处理状态")
    private String alarmStatus;
    private Integer status;

    @ExcelField(title = "处理人")
    private String personName;

    /**
     * 处理人ID
     */
    private String personId;

    @ExcelField(title = "处理时间")
    private String handleTimeStr;
    private Long handleTime;

    @ExcelField(title = "处理方式")
    private String handleType;

    /**
     * 监控对象类型
     */
    private Integer monitorType;

    private String swiftNumber;

    /**
     * 报警计算标准：0普通标准 1山西标准 2四川标准
     */
    private Integer calStandard;

    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String remark;

    private String positionalId;

    /**
     * 驾驶证号
     */
    private String drivingLicenseNo;

    /**
     * 高度
     */
    private String height;

    /**
     * sim卡编号
     */
    private String simCardNumber;

    /**
     * 终端编号
     */
    private String deviceNumber;

    /**
     * 协议类型
     */
    private Integer deviceType;
}
