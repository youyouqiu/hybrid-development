package com.zw.adas.domain.monitorScore;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;


@Data
public class MonitorAggregateInfo {
    /**
     * 综合得分
     */
    private double score;

    /**
     * 得分分布
     */
    private transient Map<String, Integer> scoreDistribution = new HashMap<>();

    /**
     * 得分分布最多的区间
     */
    private String scoreDistributionStr;

    /**
     * 得分环比
     */
    private String scoreRingRatio;

    /**
     * 监控对象数量
     */
    private int monitorSize;

    /**
     * 行驶时长
     */
    private Double travelTime;

    /**
     * 平均行驶时长
     */
    private String averageTravelTime;

    /**
     * 报警数
     */
    private long alarmTotal;

    /**
     * 报警数环比
     */
    private String alarmRingRatio;

    /**
     * 百公里报警数
     */
    private Double hundredsAlarmTotal;

    /**
     * 百公里报警数环比
     */

    private String hundredsAlarmRingRatio;

    /**
     * 平均行驶速度
     */

    private Double travelSpeed;
}

