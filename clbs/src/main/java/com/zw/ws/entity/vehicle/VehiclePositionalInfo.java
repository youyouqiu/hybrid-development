package com.zw.ws.entity.vehicle;

import com.zw.protocol.msg.t808.body.LocationInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;


@Data
public class VehiclePositionalInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String vehicleId;

    private String brand;

    private Integer status;

    private String vehicleIcon;

    // 纬度
    private double latitude;

    // 经度
    private double longitude;

    private double direction;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VehiclePositionalInfo that = (VehiclePositionalInfo) o;
        return Objects.equals(vehicleId, that.vehicleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId);
    }


    public VehiclePositionalInfo() {
    }

    public VehiclePositionalInfo(LocationInfo info, String brand, String id) {
        this.vehicleId = id;
        this.brand = brand;
        this.status = info.getStateInfo();
        this.vehicleIcon = info.getMonitorInfo().getMonitorIcon();
        this.latitude = info.getLatitude();
        this.longitude = info.getLongitude();
        this.direction = info.getDirection();
    }
}
