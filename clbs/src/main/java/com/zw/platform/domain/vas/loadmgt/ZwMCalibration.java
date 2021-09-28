package com.zw.platform.domain.vas.loadmgt;

import lombok.Data;

@Data
public class ZwMCalibration {
  private String id;
  private String vehicleId;
  private String sensorId;
  private String sensorVehicleId;
  private String flag;
  private String createDataUsername;
  private String updateDataUsername;
  private String calibration;
}
