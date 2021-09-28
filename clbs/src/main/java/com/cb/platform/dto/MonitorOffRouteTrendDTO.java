package com.cb.platform.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/24 10:21
 */
@Data
public class MonitorOffRouteTrendDTO implements Serializable {
    private static final long serialVersionUID = 5624904248151608778L;

    /**
     * 监控对象id
     */
    private String monitorId;
    /**
     * 监控对象名称
     */
    private String monitorName;
    /**
     * 所属企业
     */
    private String orgName;
    /**
     * 车牌颜色类型
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
     * 排名
     */
    private Integer rank;
    /**
     * 明细
     */
    private List<OffRouteDayDetailDTO> detailList;
}
