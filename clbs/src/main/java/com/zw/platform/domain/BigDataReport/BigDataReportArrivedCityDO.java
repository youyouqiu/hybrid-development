package com.zw.platform.domain.BigDataReport;

import lombok.Data;


/**
 * 监控对象到达城市的实体
 */
@Data
public class BigDataReportArrivedCityDO {

    /**
     * 经度-东
     */
    private String longitudeEast;

    /**
     * 纬度-东
     */
    private String latitudeEast;

    /**
     * 经度-西
     */
    private String longitudeWest;

    /**
     * 纬度-西
     */
    private String latitudeWest;

    /**
     * 经度-南
     */
    private String longitudeSouth;

    /**
     * 纬度-南
     */
    private String latitudeSouth;

    /**
     * 经度-北
     */
    private String longitudeNorth;

    /**
     * 纬度-北
     */
    private String latitudeNorth;
}
