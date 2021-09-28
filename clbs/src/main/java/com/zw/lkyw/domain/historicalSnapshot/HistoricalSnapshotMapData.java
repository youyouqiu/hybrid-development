package com.zw.lkyw.domain.historicalSnapshot;

import lombok.Data;

import java.util.Objects;

/***
 @Author lijie
 @Date 2020/1/8 14:34
 @Description 历史抓拍地图打点实体
 @version 1.0
 **/
@Data
public class HistoricalSnapshotMapData {

    private String latitude;
    private String longitude;
    private String vehicleId;
    private Integer type;
    private Integer count;

    // public HistoricalSnapshotMapData(String latitude, String longitude, String vehicleId,
    // Integer type, Integer count) {
    //     this.latitude = latitude;
    //     this.longitude = longitude;
    //     this.vehicleId = vehicleId;
    //     this.type = type;
    //     this.count = count;
    // }
    //
    // public HistoricalSnapshotMapData(String latitude, String longitude, String vehicleId, Integer type) {
    //     this.latitude = latitude == null ? "0.0" : latitude;
    //     this.longitude = longitude == null ? "0.0" : longitude;
    //     this.vehicleId = vehicleId;
    //     this.type = type;
    // }

    public void setLatitude(String latitude) {
        this.latitude = latitude == null ? "0.0" : latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude == null ? "0.0" : longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HistoricalSnapshotMapData that = (HistoricalSnapshotMapData) o;
        return Objects.equals(latitude, that.latitude) && Objects.equals(longitude, that.longitude) && Objects
            .equals(vehicleId, that.vehicleId) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, vehicleId, type);
    }
}
