package com.zw.app.domain.monitor;

import lombok.Data;

import java.io.Serializable;


/**
 * APP-停止/行驶 数据实体
 */
@Data
public class AppTravelAndStopData implements Serializable {
    /**
     * 定位时间
     */
    private Long time;

    /**
     * 速度
     */
    private Double speed;

    /**
     * 里程
     */
    private Double mileage;

    /**
     * 状态
     */
    private Integer status;
}
