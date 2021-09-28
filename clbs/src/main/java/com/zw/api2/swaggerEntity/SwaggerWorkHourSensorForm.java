package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.UUID;

@Data
public class SwaggerWorkHourSensorForm {


    private String id = UUID.randomUUID().toString();
    /**
     * 传感器型号
     */
    @ApiParam(value = "传感器型号",required = true)
    private String sensorNumber;

    /**
     * 检测方式(1:电压比较式;2:油耗阈值式;3:油耗波动式)
     */
    @ApiParam(value = "检测方式(1:电压比较式;2:油耗阈值式;3:油耗波动式)",required = true)
    private Integer detectionMode;

    /**
     * 滤波系数（1:实时,2:平滑,3:平稳）
     */
    @ApiParam(value = "滤波系数（1:实时,2:平滑,3:平稳）",required = true)
    private Integer filterFactor;

    /**
     * 波特率 (其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200)
     */
    @ApiParam(value = "波特率 (其中1:2400,2:4800,3:9600,4:19200,5:38400,6:57600,7:115200)",required = true)
    private Integer baudRate;

    /**
     * 奇偶校验（1：奇校验；2：偶校验；3：无校验）
     */
    @ApiParam(value = "奇偶校验（1：奇校验；2：偶校验；3：无校验）",required = true)
    private Integer oddEvenCheck;

    /**
     * 补偿使能（1:使能,2:禁用）
     */
    @ApiParam(value = "补偿使能（1:使能,2:禁用）",required = true)
    private Integer compensate;

    /**
     * 备注
     */
    @ApiParam(value = "备注")
    private String remark;
}
