package com.zw.platform.util.imports.lock.dto;

import lombok.Data;

import java.util.Date;

/**
 * 信息配置实体
 * @author create by zhouzongbo on 2020/9/11.
 */
@Data
public class ConfigDo {

    /**
     * 信息配置
     */
    private String id;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 终端id
     */
    private String deviceId;
    /**
     * sim卡id
     */
    private String simCardId;
    /**
     * 服务周期id
     */
    private String serviceLifecycleId;
    /**
     * 报警状态
     */
    private Integer alarmStatus;
    /**
     * 报警时间
     */
    private Date alarmTime;
    /**
     * 在线状态
     */
    private Integer onlineStatus;
    /**
     * 离线时间
     */
    private Date offlineTime;
    /**
     * 在线时间
     */
    private Date onlineTime;
    /**
     * 最后车的经度
     */
    private Double longitude;
    /**
     * 最后车的纬度
     */
    private Double latitude;
    /**
     * 速度
     */
    private Integer speed;
    /**
     * 方向
     */
    private String orientation;
    /**
     * 位置
     */
    private String location;
    /**
     * 海拔高度
     */
    private Integer altitude;
    /**
     * 是否定位（0是未定位、1是定位）
     */
    private Integer isLocation;
    /**
     * gps时间
     */
    private Date gpsTime;
    /**
     * 最后返回时间
     */
    private Date returnTime;
    /**
     * 0点火、1是熄火
     */
    private Integer accStatus;
    /**
     * flag
     */
    private Integer flag;
    /**
     * create_data_time
     */
    private Date createDataTime;
    /**
     * create_data_username
     */
    private String createDataUsername;
    /**
     * update_data_time
     */
    private Date updateDataTime;
    /**
     * update_data_username
     */
    private Date updateDataUsername;
    /**
     * 人id
     */
    private String peopleId;
    /**
     * 物id
     */
    private String thingId;
    /**
     * 外设id
     */
    private String peripheralsId;
    /**
     * 监控对象类型（0：车，1：人）
     */
    private String monitorType;
    /**
     * 对讲对象id
     */
    private String intercomInfoId;
    /**
     * 车辆密码
     */
    private String vehiclePassword;
}
