package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/10 14:47
 */
@Data
public class PassCloudAlarmInfo implements Serializable {
    private static final long serialVersionUID = -1894399715775017515L;

    /**
     * 报警次数
     */
    private Integer alarmNum;
    /**
     * 报警来源
     */
    private String alarmSource;
    /**
     * 报警类型
     */
    private Integer alarmType;
    /**
     * 日期
     */
    private Long day;
    /**
     * 报警持续时间
     */
    private Long duration;
    /**
     * 报警结束位置
     */
    private String endAddress;
    /**
     * 报警结束经纬度
     */
    private String endLocation;
    /**
     * 报警结束速度
     */
    private Double endSpeed;
    /**
     * 报警结束时间
     */
    private Long endTime;
    /**
     * 最大速度
     */
    private Double maxSpeed;
    /**
     * 最小速度
     */
    private Double minSpeed;
    /**
     * 报警开始位置
     */
    private String startAddress;
    /**
     * 报警开始经纬度
     */
    private String startLocation;
    /**
     * 报警开始速度
     */
    private Double startSpeed;
    /**
     * 报警开始时间
     */
    private Long startTime;
    /**
     * 报警总次数
     */
    private Integer totalNum;
    /**
     * 报警总速度
     */
    private Double totalSpeed;
}
