package com.zw.platform.domain.other.protocol.jl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 14:06
 */
@Data
@NoArgsConstructor
public class AlarmVehicleDO {

    private String id;
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 报警开始时间
     */
    private Date startTime;

    /**
     * 报警解除时间
     */
    private Date endTime;

    /**
     * 报警类型: 0:紧急报警; 10: 疲劳报警; 200:进入报警; 201 进出报警; 210: 偏航报警; 41: 超速报警; 53夜间行驶报警
     */
    private Integer alarmType;

    /**
     * 报警处理状态: 1:处理中; 2:已处理; 3: 不作处理; 4: 将来处理
     */
    private Integer alarmStatus;

    /**
     * 车牌颜色：1蓝，2黄，3黑，4白，9其他，90:农蓝， 91农黄，92农绿，93黄绿色，94渐变绿色
     */
    private Integer plateColor;

    /**
     * 所属企业
     */
    private String groupName;

    /**
     * 上报时间
     */
    private Date uploadTime;

    /**
     * 上上传状态：0: 失败; 1: 成功
     */
    private Integer uploadState;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 错误信息
     */
    private String errorMsg;

    public AlarmVehicleDO(String monitorId, String monitorName, Date startTime, Date endTime, Integer alarmType,
        Integer alarmStatus, Integer plateColor, String groupName, Date uploadTime, Integer uploadState,
        String operator, String errorMsg) {
        this.id = UUID.randomUUID().toString();
        this.monitorId = monitorId;
        this.monitorName = monitorName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.alarmType = alarmType;
        this.alarmStatus = alarmStatus;
        this.plateColor = plateColor;
        this.groupName = groupName;
        this.uploadTime = uploadTime;
        this.uploadState = uploadState;
        this.operator = operator;
        this.errorMsg = errorMsg;
    }
}
