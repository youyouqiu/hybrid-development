package com.zw.platform.domain.vas.f3;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @version 1.0
 * @author: nixiangqian
 * @date 2017年05月09日 14:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SensorPolling extends BaseFormBean {
    private static final long serialVersionUID = 6488523225058678317L;
    /**
     * 传感器类型
     */
    private String sensorType;
    /**
     * 传感器轮询名称
     */
    private String pollingName;
    /**
     * 轮询时间
     */
    private Integer pollingTime;
    /**
     * 轮询配置ID
     */
    private String configId;
    /**
     * 外设ID
     */
    private String identId;
    /**
     * 监控对象id
     */
    private String vehicleId;
}
