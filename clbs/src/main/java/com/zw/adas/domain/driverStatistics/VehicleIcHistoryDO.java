package com.zw.adas.domain.driverStatistics;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 车辆插卡历史表
 *
 * @author Zhang Yanhui
 * @since 2021/7/26 17:50
 */

@Data
public class VehicleIcHistoryDO implements Serializable {
    private static final long serialVersionUID = -2683389876572579605L;
    private Long id;
    private String vehicleId;
    private String driverName;
    private String identificationNumber;
    private LocalDateTime createDataTime;

    public VehicleIcHistoryDO() {
    }

    public VehicleIcHistoryDO(String vehicleId, String driverName, String identificationNumber) {
        this.vehicleId = vehicleId;
        this.driverName = driverName;
        this.identificationNumber = identificationNumber;
    }
}
