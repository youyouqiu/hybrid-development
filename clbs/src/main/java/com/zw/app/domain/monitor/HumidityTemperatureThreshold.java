package com.zw.app.domain.monitor;

import lombok.Data;


/**
 * 温湿度监测设置阈值
 */
@Data
public class HumidityTemperatureThreshold {
    /**
     * 上阈值
     */
    private String high;

    /**
     * 下阈值
     */
    private String low;
}
