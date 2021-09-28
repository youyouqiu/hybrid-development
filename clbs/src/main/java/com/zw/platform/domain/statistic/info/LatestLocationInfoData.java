package com.zw.platform.domain.statistic.info;

import lombok.Data;

/**
 * @author denghuabing on 2019/12/18 16:24
 */
@Data
public class LatestLocationInfoData {

    private String monitorId;
    private String monitorName;
    /**
     * acc状态 0:关 1：开
     */
    private Integer accStatus;
    /**
     * 在线状态0:离线 1:在线
     */
    private Integer isOnline;

    private Long time;

    private String speed;

    /**
     * 	监控对象类型 0:车 1:人 2:物
     */
    private Integer monitorType;

    private Integer protocolType;

    private String  address;

    private String longitude;

    private String latitude;
}
