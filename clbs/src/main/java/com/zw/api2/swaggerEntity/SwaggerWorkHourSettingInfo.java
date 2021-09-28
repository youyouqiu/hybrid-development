package com.zw.api2.swaggerEntity;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SwaggerWorkHourSettingInfo {
    /**
     * 补偿使能（1:使能,2:禁用）',
     */
    @ApiParam(value = "补偿使能（1:使能,2:禁用）", required = true)
    private String compensateStr;

    /**
     * 滤波系数（1:实时,2:平滑,3:平稳）
     */
    @ApiParam(value = "滤波系数（1:实时,2:平滑,3:平稳）", required = true)
    private String filterFactorStr;

    /**
     * 工时与车辆管理表
     */
    @ApiParam(value = "工时与车辆管理表id", required = true)
    private String id;

    /**
     * 下发参数id
     */
    @ApiParam(value = "下发参数id", required = true)
    private String paramId;

    /**
     * 续时长(s)
     */
    @ApiParam(value = "续时长(s)", required = true)
    private Integer lastTime;

    /**
     * 电压阈值（V）
     */
    @ApiParam(value = "电压阈值（V）", required = true)
    private String thresholdVoltage;

    /**
     * 工作流量阈值（L/h）
     */
    @Deprecated
    @ApiParam(value = "工作流量阈值（L/h）", required = true)
    private String thresholdWorkFlow;

    /**
     * 待机报警阈值
     */
    @Deprecated
    @ApiParam(value = "待机报警阈值", required = true)
    private String thresholdStandbyAlarm;

    /**
     * 车辆id
     */
    @ApiParam(value = "车辆id", required = true)
    private String vehicleId;

    /**
     * 平滑系数
     */
    @ApiParam(value = "平滑系数", required = true)
    private Integer smoothingFactor;

    /**
     * 波动计算个数
     */
    @ApiParam(value = "波动计算个数", required = true)
    private Integer baudRateCalculateNumber;

    /**
     * 波动计算时段:
     * 1：10 秒
     * 2：15 秒；
     * 3：20 秒；
     * 4：30 秒(缺省值)；
     * 5：60 秒；
     */
    @ApiParam(value = "波动计算时段: 1：10 秒；2：15 秒；3：20 秒；4：30 秒(缺省值)；5：60 秒；")
    private Integer baudRateCalculateTimeScope;

    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    @ApiParam(value = "传感器序号: 0:发动机1; 1:发动机2")
    private Integer sensorSequence;

    /**
     * 总待机时长基值
     */
    @ApiParam(value = "总待机时长基值")
    private Long totalAwaitBaseValue;

    /**
     * 总停机时长基值
     */
    @ApiParam(value = "总停机时长基值")
    private Long totalHaltBaseValue;

    /**
     * 总工作时长基值
     */
    @ApiParam(value = "总工作时长基值")
    private Long totalWorkBaseValue;
}
