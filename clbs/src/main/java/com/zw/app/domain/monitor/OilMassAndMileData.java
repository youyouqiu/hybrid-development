package com.zw.app.domain.monitor;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;

@Data
public class OilMassAndMileData implements Serializable {
    // 监控对象id
    private byte[] monitorId;

    private String monitorStrId;

    private Long day;

    private String monitorName; // 监控对象名称

    private Double oilTank; // 用油量

    private JSONObject dailyOilTank;

    private Double fuelAmount; // 加油量

    private JSONObject dailyFuelAmount;

    private Double fuelSpill; // 漏油量

    private JSONObject dailyFuelSpill;

    private Double gpsMile; // gps里程

    private JSONObject dailyGpsMile;
}
