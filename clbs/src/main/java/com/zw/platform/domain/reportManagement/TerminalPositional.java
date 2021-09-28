package com.zw.platform.domain.reportManagement;

import lombok.Data;


@Data
public class TerminalPositional {
    /**
     * 车辆id  byte
     */
    private byte[] vehicleId;

    /**
     * 车辆id str
     */
    private String monitorId;

    /**
     * 行驶停止状态（2停止，1行驶）
     */
    private String status;

    /**
     * 车辆速度
     */
    private String speed;

    /**
     * gps里程
     */
    private String gpsMile;

    /**
     * gps时间
     */
    private Long vtime;

    /**
     * 经度
     */
    private String longtitude;

    /**
     * 维度
     */
    private String latitude;

}
