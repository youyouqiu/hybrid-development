package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

@Data
public class MonitorPositioningInfo {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象name
     */
    private String monitorName;

    /**
     * 车牌颜色
     */
    private String plateColor;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 监控对象类型（0 车 ,1人,2物）
     */
    private String monitorType;
    /**
     * 所属企业id
     */
    private String groupId;

    /**
     * 所属企业name
     */
    private String groupName;

    /**
     * 合计定位总数
     */
    private Integer locationTotal = 0;

    /**
     * 合计无效定位数
     */
    private Integer invalidLocations = 0;

    /**
     * 定位统计有效率
     */
    private Double locationEfficiency = 0.0;

    /**
     * 定位统计有效率(字符串)
     */
    private String locationEfficiencyStr = "--";

    /**
     * 定位时间
     */
    private long locationDate;

    /**
     * 定位时间(str)
     */
    private String locationDateStr;

    /**
     * 最后定位地址
     */
    private String address;
    /**
     * 下标
     */
    private int index;

}
