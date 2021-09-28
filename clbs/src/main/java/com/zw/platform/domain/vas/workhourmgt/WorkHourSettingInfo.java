package com.zw.platform.domain.vas.workhourmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工时设置
 * @author zhouzongbo on 2018/5/28 16:29
 */
@Data
public class WorkHourSettingInfo implements Serializable {

    private static final long serialVersionUID = -2950108339125386076L;

    /**
     * 发动机1
     */
    public static final int SENSOR_SEQUENCE_ONE = 0;
    /**
     * 发动机2
     */
    public static final int SENSOR_SEQUENCE_TWO = 1;
    /**
     * 工时与车辆管理表
     */
    private String id;
    /**
     * 发动机1-----------------------------------------
     * 传感器型号id
     */
    private String sensorId;
    private String sensorNumber;
    /**
     *  1温度传感器  2湿度传感器  3正反转传感器 4工时传感器
     */
    private String sensorType;

    /**
     * 补偿使能（1:使能,2:禁用）',
     */
    private Integer compensate;
    private String compensateStr;
    /**
     * 奇偶校验（1：奇校验；2：偶校验；3：无校验）
     */
    private String oddEvenCheck;
    private String oddEvenCheckStr;
    /**
     * 波特率
     */
    private String baudRate;
    private String baudRateStr;

    /**
     * 滤波系数（1:实时,2:平滑,3:平稳）
     */
    private Integer filterFactor;

    private String filterFactorStr;
    /**
     * 续时长(s)
     */
    private Integer lastTime;

    /**
     * 电压阈值（V）
     */
    private String thresholdVoltage;

    /**
     * 工作流量阈值（L/h）
     */
    @Deprecated
    private String thresholdWorkFlow;

    /**
     * 待机报警阈值
     */
    @Deprecated
    private String thresholdStandbyAlarm;

    /**
     * 传感器序号: 0:发动机1; 1:发动机2
     */
    private Integer sensorSequence;

    /**
     * 检测方式(1:电压比较式;2:油耗阈值式;3.油耗波动式)
     */
    private Integer detectionMode;

    /**
     * 平滑系数
     */
    private Integer smoothingFactor;


    /**
     * 波动计算个数
     */
    private Integer baudRateCalculateNumber;

    /**
     * 波动阈值（L/h）
     */
    private String baudRateThreshold;

    /**
     * 波动计算时段:
     * 1：10 秒
     * 2：15 秒；
     * 3：20 秒；
     * 4：30 秒(缺省值)；
     * 5：60 秒；
     */
    private Integer baudRateCalculateTimeScope;

    /**
     * 速度阈值（km/h）
     */
    private String speedThreshold;

    /**
     * 油耗阈值(L/h)
     */
    private String threshold;
    /**
     * 发动机2-----------------------------------------
     * 工时与车辆管理表
     */
    private String twoId;
    /**
     *
     * 传感器型号id
     */
    private String twoSensorId;
    private String twoSensorNumber;
    /**
     *  传感器型号
     */
    private String twoSensorType;

    /**
     * 补偿使能
     */
    private Integer twoCompensate;
    /**
     * 奇偶校验
     */
    private String twoOddEvenCheck;

    /**
     * 波特率
     */
    private String twoBaudRate;

    /**
     * 滤波系数
     */
    private Integer twoFilterFactor;

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
     * 检测方式(1:电压比较式;2:油耗流量计式)
     */
    private Integer twoDetectionMode;

    /**
     * 发动机二平滑系数
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
     * 车牌号
     */
    private String plateNumber;

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 下发参数id
     */
    private String paramId;

    /**
     * 通讯参数下发id
     */
    private String transmissionParamId;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 组织
     */
    private String groupId;

    private String groupName;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 分组
     */
    private String assignmentId;

    /**
     * 协议类型
     */
    private String protocol;

    /**
     * 下发状态
     */
    private Integer status;

    /**
     * 自动上传时间
     */
    private String  uploadTime;

    /**
     * 输出修正系数K
     */
    private String outputCorrectionK;
    /**
     * 输出修正系数B
     */
    private String outputCorrectionB;

    /**
     * 总工作时长基值
     */
    private Long totalWorkBaseValue;
    /**
     * 总待机时长基值
     */
    private Long totalAwaitBaseValue;
    /**
     * 总停机时长基值
     */
    private Long totalHaltBaseValue;

    /**
     * 常规参数中的传感器外设ID
     */
    private String sensorPeripheralID;


    /**
     * 监控对象类型 {1:车,2:物,3:人}
     */
    private String monitorType;

    /**
     * 0: 发动机1; 2: 发动机2. 用于页面tab 切换
     */
    private String sensorSequenceType;
}
