package com.zw.platform.domain.oil;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;


@Data
public class HistoryOilMass {
    /**
     * 定位时间
     */
    private Long time;

    /**
     * 油箱油量
     */
    private JSONArray oilTank;

    /**
     * 加油量
     */
    private JSONArray fuelAmount;

    /**
     * 漏油量
     */
    private JSONArray fuelSpill;
}
