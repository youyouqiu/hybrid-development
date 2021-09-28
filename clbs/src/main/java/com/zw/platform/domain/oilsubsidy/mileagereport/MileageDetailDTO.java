package com.zw.platform.domain.oilsubsidy.mileagereport;

import lombok.Data;

/**
 * 里程详情对象
 *
 * @author zhangjuan
 */
@Data
public class MileageDetailDTO {

    private Long day;
    private Double gpsMile;
    private Double mileage;
    private Integer sensorFlag;

    /**
     * 里程结束纬度
     */
    private Double endLat;
    /**
     * 里程结束经度
     */
    private Double endLon;

    /**
     * 里程开始纬度
     */
    private Double startLat;

    /**
     * 里程开始经度
     */
    private Double startLon;

    /**
     * 里程开始时间
     */
    private String startTime;

    /**
     * 里程结束时间
     */
    private String endTime;

    /**
     * 监控对象ID
     */
    private String monitorId;
}
