package com.zw.platform.domain.BigDataReport;

import lombok.Data;


/**
 * 大数据报表某个月全部监控对象的数据和实体
 */
@Data
public class BigDataMonitorDataSum {

    private Double totalGpsMile; // 总里程

    private long totalTravelTime; // 总的行驶时长

    private long totalDownTime; // 总的停驶时长

    private long totalOverSpeedTimes; // 总的超速次数
}
