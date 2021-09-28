package com.cb.platform.dto;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/23 17:46
 */
@Data
public class OrgOffRouteStatisticsDetailDTO {
    /**
     * 企业id
     */
    private String orgId;
    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 路线偏离车辆数
     */
    private Integer alarmMonitorNum;
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
    /**
     * 月排名
     */
    private Integer rank;
}
