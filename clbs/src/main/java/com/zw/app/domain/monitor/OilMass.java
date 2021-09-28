package com.zw.app.domain.monitor;

import lombok.Data;

import java.io.Serializable;

/**
 * 监控对象油量信息
 * @author hujun
 * @date 2018/8/27 17:46
 */
@Data
public class OilMass implements Serializable{
    private static final long serialVersionUID = 1L;

    private Long time;//时间
    private String oilTankOne;//主油箱油量
    private String oilTankTwo;//副油箱油量
    private String fuelAmountOne;//主油箱加油量
    private String fuelAmountTwo;//副油箱加油量
    private String fuelSpillOne;//主油箱漏油量
    private String fuelSpillTwo;//副油箱漏油量
}
