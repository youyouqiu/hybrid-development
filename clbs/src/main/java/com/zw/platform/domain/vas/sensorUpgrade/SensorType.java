package com.zw.platform.domain.vas.sensorUpgrade;

import lombok.Data;


/**
 * 传感器类型实体
 */
@Data
public class SensorType {
    private String sensorName; // 传感器名称
    private String sensorId; // 传感器id(eg:0x41)
    private String modelName; // 传感器型号
}
