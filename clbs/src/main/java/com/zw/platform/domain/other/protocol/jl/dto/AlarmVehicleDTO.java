package com.zw.platform.domain.other.protocol.jl.dto;

import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 15:30
 */
@Data
@NoArgsConstructor
public class AlarmVehicleDTO {
    private String id;
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    @ExcelField(title = "监控对象")
    private String monitorName;

    /**
     * 报警开始时间
     */
    @ExcelField(title = "报警时间")
    private String alarmStartTime;

    /**
     * 报警解除时间
     */
    @ExcelField(title = "报警解除时间")
    private String alarmEndTime;

    /**
     * 报警类型;
     */
    @ExcelField(title = "报警类型")
    private String alarmTypeStr;

    /**
     * 报警处理状态
     */
    @ExcelField(title = "报警处理状态")
    private String alarmStatusStr;

    /**
     * 车牌颜色
     */
    @ExcelField(title = "车牌颜色")
    private String plateColorStr;

    /**
     * 所属企业
     */
    @ExcelField(title = "所属企业")
    private String groupName;

    /**
     * 上报时间
     */
    @ExcelField(title = "上报时间")
    private String uploadTimeStr;

    /**
     * 上上传状态：0: 失败; 1: 成功
     */
    @ExcelField(title = "上传状态")
    private String uploadStateStr;

    /**
     * 操作人
     */
    @ExcelField(title = "操作人")
    private String operator;

    /**
     * 错误信息
     */
    private String errorMsg;

    public AlarmVehicleDTO(String id, String monitorId, String monitorName, String alarmStartTime, String alarmEndTime,
        String alarmTypeStr, String alarmStatusStr, String plateColorStr, String groupName, String uploadTimeStr,
        String uploadStateStr, String operator, String errorMsg) {
        this.id = id;
        this.monitorId = monitorId;
        this.monitorName = monitorName;
        this.alarmStartTime = alarmStartTime;
        this.alarmEndTime = alarmEndTime;
        this.alarmTypeStr = alarmTypeStr;
        this.alarmStatusStr = alarmStatusStr;
        this.plateColorStr = plateColorStr;
        this.groupName = groupName;
        this.uploadTimeStr = uploadTimeStr;
        this.uploadStateStr = uploadStateStr;
        this.operator = operator;
        this.errorMsg = errorMsg;
    }
}
