package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.io.Serializable;


@Data
public class SwaggerDemOilVehicleSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 车辆id
     */
    @ApiParam(value = "车辆id", required = true)
    private String vehicleId;

    /**
     * 传感器型号
     */
    @ApiParam(value = "传感器型号", required = true)
    private String sensorType;

    /**
     * 油箱类型  油箱1   油箱2
     */
    @ApiParam(value = "油箱类型  油箱1   油箱2", required = true)
    private String oilBoxType;

    /**
     * 油箱id
     */
    @ApiParam(value = "油箱id", required = true)
    private String oilBoxId;

    /**
     * 车牌号
     */
    @ApiParam(value = "车牌号", required = true)
    private String brand;

    @ApiParam(value = "传感器编号", required = true)
    private String sensorNumber;

}
