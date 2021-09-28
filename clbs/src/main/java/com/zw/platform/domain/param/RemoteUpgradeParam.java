package com.zw.platform.domain.param;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouzongbo on 2019/1/17 14:35
 */
@Data
public class RemoteUpgradeParam implements Serializable {

    private static final long serialVersionUID = -5819199539695216135L;
    /**
     * 监控对象ID
     */
    private String monitorId;
    /**
     * 车辆绑定传感器ID
     */
    private String sensorVehicleId;

    /**
     * 下发参数ID,用于下发
     */
    private String paramId;
}
