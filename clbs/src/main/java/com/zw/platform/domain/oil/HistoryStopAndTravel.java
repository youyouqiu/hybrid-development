package com.zw.platform.domain.oil;

import lombok.Data;


@Data
public class HistoryStopAndTravel {
    /**
     * 定位时间
     */
    private long time;

    /**
     * 状态(1:停止,2:行驶)
     */
    private String status;

    /**
     * 里程
     */
    private Double mileage;
}
