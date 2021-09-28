package com.zw.platform.domain.oilsubsidy.subsidyManage;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/29 14:36
 */
@Data
public class VehicleLocationSupplementaryInfo {
    /**
     * 加密标识 固定为:1
     * 0:未加密(为终端上报原始经纬度)
     * 1:已加密(为解码纠偏后经纬度)
     */
    private Integer excrypt = 1;
    /**
     * 日
     */
    private Integer day;
    /**
     * 月
     */
    private Integer month;
    /**
     * 年
     */
    private Integer year;
    /**
     * 时
     */
    private Integer hour;
    /**
     * 分
     */
    private Integer minute;
    /**
     * 秒
     */
    private Integer second;
    /**
     * 经度(*10的6次方)
     */
    private Double lon;
    /**
     * 纬度(*10的6次方)
     */
    private Double lat;
    /**
     * GPS速度
     */
    private Integer vec1;
    /**
     * 行驶里程速度
     */
    private Integer vec2;
    /**
     * 车辆总里程数
     */
    private Long vec3;
    /**
     * 方向
     */
    private Integer direction;
    /**
     * 海拔高度，单位为米（ m)
     */
    private Integer altitude;
    /**
     * 车辆状态
     */
    private Integer state;
    /**
     * 上下行状态0:上行，1:下行，2:离线，3:上行场区，4:下行场区
     */
    private Integer upDown;
}
