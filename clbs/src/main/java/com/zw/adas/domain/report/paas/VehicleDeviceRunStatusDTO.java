package com.zw.adas.domain.report.paas;

import lombok.Data;

/**
 * 车辆与终端运行状态信息 DTO
 *
 * @author zhangjuan
 */
@Data
public class VehicleDeviceRunStatusDTO {
    /**
     * 企业id
     */
    private String groupId;

    /**
     * 定位时间(格式: yyyyMMddHHmmss)
     */
    private String time;

    /**
     * 数据来源
     * 0:0x0200
     * 1:0x0710
     */
    private Integer dataSource;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 分组名称
     */
    private String assignmentName;

    /**
     * 企业名称
     */
    private String groupName;

    /**
     * 车牌颜色
     */
    private Integer plateColor;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 经度
     */
    private String originalLongitude;

    /**
     * 纬度
     */
    private String originalLatitude;

    /**
     * 位置
     */
    private String address;

    /**
     * 海拔高度(单位:米)
     */
    private Integer altitude;

    /**
     * gps速度(单位:1km/h)
     */
    private Double speed;

    /**
     * 方向(0-359),正北为0,顺时针
     */
    private Integer direction;

    /**
     * 原车速度(单位:1km/h)
     */
    private Double vehicleSpeed;

    /**
     * X轴加速度(单位:m/s2)
     */
    private Double axisAccelerationX;

    /**
     * Y轴加速度(单位:m/s2)
     */
    private Double axisAccelerationY;

    /**
     * Z轴加速度(单位:m/s2)
     */
    private Double axisAccelerationZ;

    /**
     * X轴角速度(单位:rad/s)
     */
    private Double axisAngularX;

    /**
     * Y轴角速度(单位:rad/s)
     */
    private Double axisAngularY;

    /**
     * Z轴角速度(单位:rad/s)
     */
    private Double axisAngularZ;

    /**
     * 制动状态(0:无制动 1:制动)
     */
    private Integer brakingStatus;

    /**
     * 转向灯状态(0:未开方向灯 1:左转方向灯 2:右转方向灯)
     */
    private Integer turnSignalStatus;

    /**
     * 远光状态 0:关 1:开
     */
    private Integer highBeamStatus;

    /**
     * 近光状态 0:关 1:开
     */
    private Integer lowBeamStatus;

    /**
     * OBD速度(单位:1km/h)
     */
    private Double obdSpeed;

    /**
     * 档位状态 0:空挡 1-9:档位 10:倒挡 11:驻车档
     */
    private Integer gearStatus;

    /**
     * 加速踏板行程值
     */
    private Integer acceleratorPedalValue;

    /**
     * 制动踏板行程值
     */
    private Integer brakePedalValue;

    /**
     * 发动机转速(单位:RPM)
     */
    private Integer engineSpeed;

    /**
     * 方向盘角度(顺时针为正，逆时针为负)
     */
    private Integer steeringWheelAngle;

    /**
     * 空调状态 0:关 1:开
     */
    private Integer airStatus;

    /**
     * 加热器状态 0:关 1:开
     */
    private Integer heaterStatus;

    /**
     * 离合器状态 0:关 1:开
     */
    private Integer clutchStatus;

    /**
     * ABS状态 0:关 1:开
     */
    private Integer absStatus;

    /**
     * 示廓灯状态 0:关 1:开
     */
    private Integer clearanceLampStatus;

    /**
     * 主电源状态 0:正常 1:欠压 2:掉电
     */
    private Integer powerStatus;

    /**
     * 备用电源状态 0:正常 1:欠压 2:掉电
     */
    private Integer spareBatteryStatus;

    /**
     * 卫星定位模块状态 0:正常 1:故障
     */
    private Integer gpsStatus;

    /**
     * TTS模块状态 0:正常 1:故障
     */
    private Integer ttsStatus;

    /**
     * 存储器状态 0:正常 1:故障
     */
    private Integer memoryStatus;

    /**
     * 备用存储器状态 0:正常 1:故障
     */
    private Integer spareMemoryStatus;
}
