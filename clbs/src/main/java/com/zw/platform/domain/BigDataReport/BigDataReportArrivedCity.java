package com.zw.platform.domain.BigDataReport;

import lombok.Data;


/**
 * 监控对象到达城市的实体
 */
@Data
public class BigDataReportArrivedCity {
    private String name; //城市名称

    private Integer count; // 达到次数

    private String longtitude; // 经度

    private String latitude; // 纬度
}
