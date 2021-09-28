package com.zw.app.domain.monitor;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/23 17:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OilMassSensorData extends SensorData {
    private static final long serialVersionUID = 3141343934593552613L;
    /**
     * 当日油耗
     */
    private String dayOilWear;
}
