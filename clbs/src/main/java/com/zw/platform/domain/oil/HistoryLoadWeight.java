package com.zw.platform.domain.oil;

import lombok.Data;

/**
 * 轨迹回放载重信息
 * @author XK
 */
@Data
public class HistoryLoadWeight {
    /**
     * 定位时间
     */
    private Long time;

    /**
     * 载重
     */
    private Float weight;
}
