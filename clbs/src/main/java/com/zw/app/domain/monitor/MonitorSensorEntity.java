package com.zw.app.domain.monitor;

import com.zw.app.entity.BaseEntity;
import lombok.Data;

@Data
public class MonitorSensorEntity extends BaseEntity {

    private String monitorId;//监控对象ids

    private String startTime;//开始时间

    private String endTime;//结束时间

    private Integer sensorFlag;//传感器标识

    private Integer tireNum;//胎压传感器轮胎编号

    private Integer sensorNo;//传感器序号

}
