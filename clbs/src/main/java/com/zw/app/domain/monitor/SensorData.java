package com.zw.app.domain.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 传感器数据
 * @author hujun
 * @date 2018/8/22 14:21
 */
@Data
public class SensorData implements Serializable{
    private static final long serialVersionUID = 1L;

    private String type;//传感器类型
    private String value;//传感器取值
    private String name;//传感器名称
    private String status;//传感器状态
}
