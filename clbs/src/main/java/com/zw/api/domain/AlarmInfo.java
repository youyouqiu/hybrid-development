package com.zw.api.domain;

import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.Converter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("报警信息")
public class AlarmInfo {
    @ApiModelProperty(value = "监控对象名称", dataType = "java.lang.String")
    private String monitorName;

    @ApiModelProperty(value = "报警类型", dataType = "java.lang.String")
    private String type;

    @ApiModelProperty(value = "报警开始时间,格式为：yyyy-MM-dd HH:mm:ss", dataType = "java.lang.String")
    private String startTime;

    @ApiModelProperty(value = "报警结束时间,格式为：yyyy-MM-dd HH:mm:ss", dataType = "java.lang.String")
    private String stopTime;

    @ApiModelProperty(value = "报警来源", dataType = "java.lang.String")
    private String source;

    @ApiModelProperty(value = "报警开始位置,格式为：经度,纬度", dataType = "java.lang.String")
    private String startLocation;

    @ApiModelProperty(value = "报警结束位置,格式为：经度,纬度", dataType = "java.lang.String")
    private String stopLocation;

    @ApiModelProperty(value = "报警开始速度(km/h)", dataType = "double")
    private double speed;

    @ApiModelProperty(value = "围栏类型", dataType = "java.lang.String")
    private String fenceType;

    @ApiModelProperty(value = "围栏名称", dataType = "java.lang.String")
    private String fenceName;

    //region Getters & Setters
    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getStopLocation() {
        return stopLocation;
    }

    public void setStopLocation(String stopLocation) {
        this.stopLocation = stopLocation;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getFenceType() {
        return fenceType;
    }

    public void setFenceType(String fenceType) {
        this.fenceType = fenceType;
    }

    public String getFenceName() {
        return fenceName;
    }

    public void setFenceName(String fenceName) {
        this.fenceName = fenceName;
    }
    //endregion

    public static AlarmInfo fromAlarmDO(AlarmDO alarmDO) {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setMonitorName(alarmDO.getMonitorName());
        alarmInfo.setType(AlarmTypeUtil.getAlarmType(alarmDO.getType()));
        alarmInfo.setStartTime(Converter.convertUnixToDatetime(alarmDO.getStartTime() / 1000));
        alarmInfo.setStopTime(Converter.convertUnixToDatetime(alarmDO.getStopTime() / 1000));
        alarmInfo.setSource(alarmDO.getSource() == 0 ? "终端" : "平台");
        alarmInfo.setStartLocation(alarmDO.getStartLocation());
        alarmInfo.setStopLocation(alarmDO.getStopLocation());
        alarmInfo.setSpeed(Double.parseDouble(alarmDO.getSpeed()));
        alarmInfo.setFenceType(alarmDO.getFenceType());
        alarmInfo.setFenceName(alarmDO.getFenceName());
        return alarmInfo;
    }
}
