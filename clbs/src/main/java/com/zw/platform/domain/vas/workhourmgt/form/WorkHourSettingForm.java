package com.zw.platform.domain.vas.workhourmgt.form;

import com.zw.platform.util.common.BaseFormBean;
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
@EqualsAndHashCode(callSuper = true)
public class WorkHourSettingForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = -6160724159175678422L;

    /**
     * 发动机1-----------------------------------------
     * 传感器型号id
     */
    @NotEmpty(message = "【传感器型号】不能为空", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "发动机1传感器型号id")
    private String sensorId;
    @ApiParam(value = "发动机1传感器型号名")
    private String sensorNumber;
    /**
     * 续时长(s)
     */
    @ApiParam(value = "发动机1续时长(s)")
    private Integer lastTime;

    /**
     * 电压阈值（V）
     */
    @ApiParam(value = "发动机1电压阈值（V）")
    private String thresholdVoltage;

    /**
     * 工作流量阈值（L/h）
     */
    @Deprecated
    @ApiParam(value = "发动机1工作流量阈值（L/h）")
    private String thresholdWorkFlow;

    /**
     * 待机报警阈值
     */
    @Deprecated
    @ApiParam(value = "发动机1待机报警阈值")
    private String thresholdStandbyAlarm;

    /**
     * 平滑系数
     */
    @ApiParam(value = "发动机1平滑系数")
    private Integer smoothingFactor;

    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    @ApiParam(value = "传感器序号： 0:发动机1; 1:发动机2")
    private Integer sensorSequence;

    /**
     * 波动计算个数
     */
    @ApiParam(value = "波动计算个数")
    private Integer baudRateCalculateNumber;

    /**
     * 波动阈值（L/h）
     */
    @ApiParam(value = "波动阈值（L/h）")
    private String baudRateThreshold;

    /**
     * 波动计算时段:
     * 1：10 秒
     * 2：15 秒；
     * 3：20 秒；
     * 4：30 秒(缺省值)；
     * 5：60 秒；
     */
    @ApiParam(value = "波动计算时段1：10 秒；2：15 秒；3：20 秒； 4：30 秒(缺省值)；5：60 秒")
    private Integer baudRateCalculateTimeScope;

    /**
     * 速度阈值（km/h）
     */
    @ApiParam(value = "速度阈值（km/h）")
    private String speedThreshold;

    /**
     * 油耗阈值(L/h)
     */
    private String threshold;

    /**
     * 发动机2-----------------------------------------
     * 传感器车辆关系表id
     */
    @ApiParam(value = "传感器车辆关系表id")
    private String twoId;
    /**
     * 传感器型号id
     */
    @ApiParam(value = "传感器型号id")
    private String twoSensorId;
    @ApiParam(value = "传感器型号名")
    private String twoSensorNumber;
    /**
     * 续时长(s)
     */
    private Integer twoLastTime;

    /**
     * 电压阈值（V）
     */
    private String twoThresholdVoltage;

    /**
     * 工作流量阈值（L/h）
     */
    @Deprecated
    private String twoThresholdWorkFlow;

    /**
     * 待机报警阈值
     */
    @Deprecated
    private String twoThresholdStandbyAlarm;

    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    private Integer twoSensorSequence;

    /**
     * 平滑系数
     */
    private Integer twoSmoothingFactor;

    /**
     * 波动计算个数
     */
    private Integer twoBaudRateCalculateNumber;

    /**
     * 波动阈值（L/h）
     */
    private String twoBaudRateThreshold;
    private Integer twoBaudRateCalculateTimeScope;
    /**
     * 速度阈值（km/h）
     */
    private String twoSpeedThreshold;

    /**
     * 发动机2油耗阈值(L/h)
     */
    private String twoThreshold;

    /**
     * 车辆id
     */
    private String vehicleId;
    private String plateNumber;

    /**
     * 参数id
     */
    private String paramId;

    /**
     * 监控对象类型 {1:车,2:物,3:人}
     */
    private Integer monitorType;

    /**
     * 外设ID
     */
    private String twoSensorOutId;
    private String sensorOutId;

}
