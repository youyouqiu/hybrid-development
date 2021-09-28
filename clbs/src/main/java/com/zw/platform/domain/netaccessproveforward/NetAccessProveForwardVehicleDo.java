package com.zw.platform.domain.netaccessproveforward;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/7/20 11:56
 */
@Data
public class NetAccessProveForwardVehicleDo implements Serializable {
    private static final long serialVersionUID = 4356731061963286000L;
    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 数据创建时间
     */
    private Date createDataTime;

    /**
     * 创建者username
     */
    private String createDataUsername;

    public NetAccessProveForwardVehicleDo() {
    }

    public NetAccessProveForwardVehicleDo(String vehicleId, Date createDataTime, String createDataUsername) {
        this.vehicleId = vehicleId;
        this.createDataTime = createDataTime;
        this.createDataUsername = createDataUsername;
    }
}
