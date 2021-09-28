package com.zw.ws.entity.vehicle;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Copyright: Copyright (c) 2016
 * </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年9月13日上午11:48:54
 */
@Data
@NoArgsConstructor
public class VehicleStatus implements Serializable {
    /**
     * 未定位
     */
    public static final int NOT_LOCATE = 2;
    /**
     * 离线
     */
    public static final int OFFLINE = 3;
    /**
     * 上线
     */
    public static final int ONLINE = 4;
    /**
     * 报警
     */
    public static final int ALARM = 5;
    /**
     * 休眠
     */
    public static final int SLEEP = 7;
    /**
     * 深度休眠
     */
    public static final int DEEP_SLEEP = 8;
    /**
     * 超速报警
     */
    public static final int OVER_SPEED = 9;

    /**
     * 在线行驶
     */
    public static final int ONLINE_RUN = 10;
    /**
     * 心跳
     */
    public static final int HEART_BEAT = 11;
    private static final long serialVersionUID = 4590616343410140792L;

    /**
     * 监控对象名称
     */
    private String brand;
    /**
     * 终端类型
     */
    private String deviceType;
    /**
     * 分组id
     */
    private List<String> groupIds;
    private Long lastMonitorTime;
    private Long latestGpsDate;
    /**
     * 监控对象类型
     */
    private Integer monitorType;
    /**
     * 速度
     */
    private String speed;
    /**
     * 监控对象id
     */
    private String vehicleId;
    /**
     * 监控对象状态
     */
    private Integer vehicleStatus;

    /**
     * 一小时
     */
    public static final int ONE_HOUR = 60 * 60;

    public static final int TEN_M = 600;

    public VehicleStatus(String vehicleId, Integer vehicleStatus) {
        this.vehicleId = vehicleId;
        this.vehicleStatus = vehicleStatus;
    }
}
