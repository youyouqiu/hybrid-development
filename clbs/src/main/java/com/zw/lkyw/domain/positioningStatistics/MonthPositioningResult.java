package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

import java.util.List;

@Data
public class MonthPositioningResult {
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
     * 所属企业id
     */
    private String groupId;

    /**
     * 所属企业name
     */
    private String groupName;
    /**
     * 累计无效数
     */
    private int totalInvalidNum;
    /**
     * 累计定位总数
     */
    private int totalLocationNum;
    /**
     * 有效率
     */
    private Double totalRatio;

    /**
     * 有效率(字符串)
     */
    private String totalRatioStr = "--";
    /**
     * 下标
     */
    private int index;

    /**
     * 每天详情数据
     */
    List<MonthDetailInfo> monthDetailInfoList;
}
