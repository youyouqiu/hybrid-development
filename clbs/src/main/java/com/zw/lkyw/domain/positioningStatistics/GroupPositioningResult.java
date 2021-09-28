package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

@Data
public class GroupPositioningResult {
    /**
     * 道路运输企业Id
     */
    private String groupId;
    /**
     * 道路运输企业
     */
    private String groupName;
    /**
     * 车辆总数
     */
    private Integer vehicleNumbers = 0;
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
     * 无定位车辆数
     */
    private Integer vehicleUnLocation = 0;
    /**
     * 无定位率
     */
    private Double unLocationRadio = 0.0;

    /**
     * 无定位率(字符串)
     */
    private String unLocationRadioStr = "--";
    /**
     * 定位中断统计车辆数
     */
    private Integer interruptVehicle = 0;
    /**
     * 定位中断统计次数
     */
    private Integer interruptNumber = 0;
    /**
     * 离线位移统计车辆数
     */
    private Integer offlineVehicle = 0;
    /**
     * 离线位移统计次数
     */
    private Integer offlineNumber = 0;

    /**
     * 定位统计有效率(字符串)
     */
    private String locationEfficiencyStr = "--";

    /**
     * 下标
     */
    private int index;
}
