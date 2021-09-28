package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import java.io.Serializable;

/**
 * @author zhouzongbo on 2018/5/28 16:47
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SwaggerWorkHourSettingForm implements Serializable {
    private static final long serialVersionUID = -6160724159175678422L;

    /**
     * 发动机1-----------------------------------------
     * 传感器型号id
     */
    @NotEmpty(message = "【传感器型号】不能为空", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "发动机1传感器型号id", required = true)
    private String sensorId;
    @ApiParam(value = "发动机1传感器型号名", required = true)
    private String sensorNumber;
    /**
     * 续时长(s)
     */
    @ApiParam(value = "发动机1续时长(s)", required = true)
    private Integer lastTime;

    /**
     * 电压阈值（V）
     */
    @ApiParam(value = "发动机1电压阈值（V）", required = true)
    private String thresholdVoltage;

    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    @ApiParam(value = "传感器序号： 0:发动机1; 1:发动机2", required = true)
    private Integer sensorSequence;

    /**
     * 传感器型号id
     */
    @ApiParam(value = "发动机2传感器型号id")
    private String twoSensorId;
    @ApiParam(value = "发动机2传感器型号名")
    private String twoSensorNumber;
    /**
     * 续时长(s)
     */
    @ApiParam(value = "发动机2续时长(s)")
    private Integer twoLastTime;

    /**
     * 电压阈值（V）
     */
    @ApiParam(value = "发动机2电压阈值（V）")
    private String twoThresholdVoltage;

    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    @ApiParam(value = "传感器序号: 0:发动机1; 1:发动机2")
    private Integer twoSensorSequence;

    /**
     * 车辆id
     */
    @ApiParam(value = "车辆id", required = true)
    private String vehicleId;
    @ApiParam(value = "车辆号", required = true)
    private String plateNumber;

    /**
     * 监控对象类型 {1:车,2:物,3:人}
     */
    @ApiParam(value = "监控对象类型 {1:车,2:物,3:人}", required = true)
    private Integer monitorType;

}
