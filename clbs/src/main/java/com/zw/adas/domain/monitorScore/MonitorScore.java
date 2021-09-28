package com.zw.adas.domain.monitorScore;

import lombok.Data;


@Data
public class MonitorScore {

    /**
     * 监控对象id
     */
    private String vehicleId;

    /**
     * 监控对象name
     */
    private String vehicleName;

    /**
     * 运营类别
     */
    private String purposeCategoryName;

    /**
     * 所属企业name
     */
    private String groupName;

    /**
     * 所属企业Id
     */
    private String groupId;

    /**
     * 行驶里程
     */
    private Double travelMile;

    /**
     * 行驶次数
     */
    private Integer travelNum;

    /**
     * 行驶时长
     */
    private Double travelTime;

    /**
     * 日均行驶时长
     */
    private String averageTravelTime;

    /**
     * 平均行驶速度
     */

    private Double travelSpeed;

    /**
     * 报警数
     */
    private long alarmTotal;

    /**
     * 报警数环比
     */
    private transient String alarmRingRatio;

    /**
     * 综合得分
     */
    private Double score;

    /**
     * 得分环比
     */
    private transient String scoreRingRatio;

    /**
     * 百公里报警数
     */
    private transient Double hundredsAlarmTotal;

    /**
     * 百公里报警数环比
     */

    private transient String hundredsAlarmRingRatio;

    /**
     * 风险事件json字符串
     */
    private transient String eventInfos;

    /**
     * 时间，格式201910
     */
    private int time;

    /**
     * 序号
     */
    private int index;
}
