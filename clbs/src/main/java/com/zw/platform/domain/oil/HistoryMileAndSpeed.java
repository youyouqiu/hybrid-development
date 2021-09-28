package com.zw.platform.domain.oil;

import lombok.Data;


@Data
public class HistoryMileAndSpeed {
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
}
