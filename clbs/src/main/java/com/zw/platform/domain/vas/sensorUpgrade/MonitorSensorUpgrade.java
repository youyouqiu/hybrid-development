package com.zw.platform.domain.vas.sensorUpgrade;

import lombok.Data;


@Data
public class MonitorSensorUpgrade {
    private String vehicleId; // 车辆id

    private String brand; // 车牌号

    private String groupName; // 所属企业名称

    private String assagnName; // 分组名称

    private String sensorUpgradeDateStr; // 最近升级日期

    private Integer sensorUpgradeStatus; // 升级状态

    private String sensorId; // 传感器id

    private String deviceId; // 设备id
}
