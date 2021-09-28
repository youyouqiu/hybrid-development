package com.cb.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/24 10:58
 */
@Data
public class OrgOffRouteMonitorStatisticsDetailDTO implements Serializable {
    private static final long serialVersionUID = 322238983531008415L;
    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 监控对象所属企业名称
     */
    private String orgName;
    /**
     * 车牌颜色
     */
    private Integer plateColor;
    private String plateColorStr;
    /**
     * 车辆类型
     */
    private String vehicleType;
    /**
     * 路线偏离报警数
     */
    private Integer courseDeviation;
    /**
     * 不按规定线路行驶报警数
     */
    private Integer notFollowLine;
    /**
     * 合计
     */
    private Integer total;
}
