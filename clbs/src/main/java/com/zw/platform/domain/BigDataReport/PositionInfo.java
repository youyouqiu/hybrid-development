/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of
 * ZhongWei, Inc. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with ZhongWei.
 */

package com.zw.platform.domain.BigDataReport;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PositionInfo {

    // 主键id
    private String id = "";
    // 车辆id
    private String vehicleId = "";
    // hbase中的车辆id
    private byte[] vehicleIdHbase;
    // 车牌号
    private String plateNumber = "";
    // 当日里程
    private Double gpsMile = (double) 0;
    // 行驶时间
    private Integer travelTime = 0;
    // 停驶时间
    private Integer downTime = 0;
    // 超速次数
    private Integer overSpeedTimes = 0;
    // 行驶次数
    private Integer travelTimes = 0;
    // 报警次数
    private Integer alarmTimes = 0;
    // 夜间 行驶
    private Double nightTravelMile = (double) 0;
    // 日期
    private Long dayTime;
    // 上线天数
    private Integer activeDays = 0;
    // 每天上线时间
    private String firstDataTime;
    // 每天上线时间集合（每天第一条数据时间）
    private List<String> firstDataTimes = new ArrayList<>();
    // 天数集合（用于兼容以前没有firstDataTimes的数据，以这个集合长度作为activeDays）
    private List<Long> dayList;
    // 在线时长
    private Long onlineDuration = 0L;
    // 上线时长(HH:mm:ss)
    private String onlineDurationStr;
    // 上线次数
    private Integer onlineCount = 0;

    // 本月第几天
    private Integer day;

    /**
     * 停驶次数
     */
    private Integer downTimes = 0;
    /**
     * 行驶里程
     */
    private Double travelMile = 0.0;

    /**
     * 停止里程
     */
    private Double downMile = 0.0;
}