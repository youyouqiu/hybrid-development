package com.zw.platform.domain.vas.sensorUpgrade;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Administrator
 */
@Data
@NoArgsConstructor
public class SensorBind {

    /**
     * 监控对象id
     */
    private List<String> vehicleIds;

    /**
     * 传感器id
     */
    private String identId;

    public SensorBind(List<String> vehicleIds, String identId) {
        this.vehicleIds = vehicleIds;
        this.identId = identId;
    }
}
