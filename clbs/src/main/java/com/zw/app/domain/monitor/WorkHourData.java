package com.zw.app.domain.monitor;

import lombok.Data;


@Data
public class WorkHourData {
    private String id;

    private long vtime; // gps时间

    private String speed;

    private Integer workInspectionMethodOne; //  工时传感器1工时检查方式

    /**
     * 工作状态   0:停机 1:工作 2:待机(油耗波动式专有)
     */
    private Integer workingPositionOne; // 工时传感器1工作状态

    private Long continueTimeOne; // 工时传感器1持续时间

    private Double checkDataOne; // 工时传感器1检测数据

    private Double fluctuateValueOne; // 工时传感器1波动值

    private Integer workInspectionMethodTwo; //  工时传感器2工时检查方式

    private Integer workingPositionTwo; // 工时传感器2工作状态

    private Long continueTimeTwo; // 工时传感器2持续时间

    private Double checkDataTwo; // 工时传感器2检测数据

    private Double fluctuateValueTwo; // 工时传感器2波动值

    /**
     * 是否是有效数据 0:有效 1:无效 3:空白数据
     */
    private Integer effectiveDataOne = 0; //有效数据标识

    private Integer effectiveDataTwo = 0; //有效数据标识
}
