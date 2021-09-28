package com.zw.adas.domain.driverStatistics.bean;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/11 15:53
 @Description 司机统计模块评分时间段详情
 @version 1.0
 **/
@Data
public class AdasDriverStatisticsBean {

    private Long startTime;
    private Long endTime;
    private String time;
    private String mile;

}
