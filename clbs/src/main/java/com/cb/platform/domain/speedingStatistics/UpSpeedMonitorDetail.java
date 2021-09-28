package com.cb.platform.domain.speedingStatistics;

import lombok.Data;

/**
 * @Description:
 * @Author zhangqiang
 * @Date 2020/5/27 13:46
 */
@Data
public class UpSpeedMonitorDetail {
    /**
     * 报警结束位置(具体位置)
     */
    private String alarmEndAddress;
    /**
     * 报警结束位置(经纬度)
     */
    private String alarmEndLocation;
    /**
     * 报警结束时间(格式:yyyyMMddHHmmssSSS)
     */
    private String alarmEndTime;
    /**
     * 报警开始位置(具体位置)
     */
    private String alarmStartAddress;
    /**
     * 报警开始位置(经纬度)
     */
    private String alarmStartLocation;
    /**
     * 报警开始时间(格式:yyyyMMddHHmmssSSS)
     */
    private String alarmStartTime;
    /**
     * 最高速度 单位:km/h
     */
    private String maxSpeed;
    /**
     * 严重程度 单位:%
     */
    private String severity;
    /**
     * 限速值 单位:km/h
     */
    private String speedLimit;
    /**
     * 是否查询逆地址(true代表后端已做查询操作，前端直接显示逆地址，false后端没有查询逆地址，需要前端列表显示按钮，让用户手动点击查询)
     */
    private boolean addressSearchFlag;

    /**
     * 持续时长
     */
    private String duration;
    /**
     * 限速类型 0-3日间限速 10-13 夜间限速
     */
    private Integer speedType;

    public void setAddressSearchFlag(boolean addressSearchFlag) {
        this.addressSearchFlag = addressSearchFlag;
    }
}
