package com.zw.lkyw.domain.positioningStatistics;

import lombok.Data;

@Data
public class MonitorInterruptDetailInfo {
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
     * 定位中断明细
     */
    private InterruptDetailInfo detailInfo;
    /**
     * 下标
     */
    private int index;

    /**
     * 获取定位中断开始时间
     * @return
     */
    public long getDetailInfoStarTime() {
        return detailInfo.getStartTime();
    }
}
