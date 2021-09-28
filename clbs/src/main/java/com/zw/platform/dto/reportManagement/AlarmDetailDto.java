package com.zw.platform.dto.reportManagement;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 部标报表监管-报警信息统计-报警信息详情DTO
 * @Author: Tianzhangxu
 * @Date: 2019/12/18 9:39
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmDetailDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 速度 单位:1km/h
     */
    private String speed;

    /**
     * 状态(0:未处理 1:已处理)
     */
    private String status;

    /**
     * 报警开始位置(经纬度)
     */
    private String alarmStartLocation;

    /**
     * 报警开始位置-具体地址
     */
    private String alarmStartAddress;

    /**
     * 报警开始时间
     */
    private Long alarmStartTime;

    /**
     * 报警开始时间str
     */
    private String alarmStartTimeStr;
}
