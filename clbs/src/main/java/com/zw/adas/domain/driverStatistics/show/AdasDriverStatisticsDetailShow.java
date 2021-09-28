package com.zw.adas.domain.driverStatistics.show;

import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/10/11 14:59
 @Description 司机里程详情信息
 @version 1.0
 **/
@Data
public class AdasDriverStatisticsDetailShow {

    private int number;
    /**
     * 行驶开始时间
     */
    private String travelStartTime;

    /**
     * 行驶结束时间
     */
    private String travelEndTime;

    /**
     * 休息开始时间
     */
    private String restStartTime;
    /**
     * 休息结束时间
     */
    private String restEndTime;

    /**
     * 行驶时长
     */
    private String travelTime;

    /**
     * 休息时长
     */
    private String restTime;

    /**
     * 行驶里程
     */
    private String travelMile;
}
