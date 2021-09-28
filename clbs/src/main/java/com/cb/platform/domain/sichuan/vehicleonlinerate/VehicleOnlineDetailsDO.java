package com.cb.platform.domain.sichuan.vehicleonlinerate;

import lombok.Data;

import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/7 10:55
 */
@Data
public class VehicleOnlineDetailsDO {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 所属企业
     */
    private String orgName;

    /**
     * 在线时间段
     */
    private List<VehicleOnlineTimeSectionDO> detailList;
}
