package com.cb.platform.domain.speedingStatistics;

import lombok.Data;

/**
 * @Description: 超速统计每日数据
 * @Author zhangqiang
 * @Date 2020/5/18 10:17
 */
@Data
public class UpSpeedDayInfo {
    /**
     * 时间（天纬度）
     */
    private int day;
    /**
     * 超速车辆数
     */
    private int monitorNum;
    /**
     * 超速次数
     */
    private int upSpeedNum;
    /**
     * 超速百分之20-百分之50
     */
    private int upSpeed20;
    /**
     * 超速百分之50以上；
     */
    private int upSpeed50;
    /**
     * 超速百分之20以下
     */
    private int upSpeed;
    /**
     * 环比
     */
    private Double sequential;
    /**
     *
     */
    private Double yearOnYear;
}
