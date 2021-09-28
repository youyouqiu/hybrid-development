package com.zw.app.domain.monitor;

import lombok.Data;

import java.util.List;


/**
 * 位置信息温湿度数据
 */
@Data
public class HumidityTemperatureData {
    private long time; //时间

    private List<Double> sensors; // 传感器数据
}
