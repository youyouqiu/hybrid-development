package com.cb.platform.dto;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/23 17:46
 */
@Data
public class MonitorOffRouteStatisticsDetailDTO {

    /**
     * 报警开始时间(格式:yyyyMMddHHmmssSSS)
     */
    private String alarmStartTime;

    /**
     * 报警结束时间(格式:yyyyMMddHHmmssSSS)
     */
    private String alarmEndTime;

    /**
     * 线路名称
     */
    private String lineName;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 报警类型描述
     */
    private String description;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 结构化地址
     */
    private String address;
}
