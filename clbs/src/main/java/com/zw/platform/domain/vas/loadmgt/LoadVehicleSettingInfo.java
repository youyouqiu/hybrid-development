package com.zw.platform.domain.vas.loadmgt;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/***
 @Author gfw
 @Date 2018/9/10 9:38
 @Description 载重返回实体
 @version 1.0
 **/
@Data
public class LoadVehicleSettingInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 载重传感器1
     */
    public static final int SENSOR_LOAD_ONE = 0;
    /**
     * 载重传感器2
     */
    public static final int SENSOR_LOAD_TWO = 1;
    /**----------              载重1开始              ----------- **/
    /**
     * 载重传感器与车辆关联
     */
    private String id;
    /**
     * 载重传感器1 --------------------
     * 传感器型号id
     */
    private String sensorId;

    /**
     * 传感器型号
     */
    private String sensorNumber;

    /**
     *  传感器类型
     *  1.温度传感器  2.湿度传感器  3.正反转传感器 4.工时传感器 6.载重传感器
     */
    private String sensorType;

    /**
     * 补偿使能（1:使能,2:禁用）
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
     * 载重传感器序号: 0:载重1; 1:载重2
     */
    private Integer sensorSequence;
    /**
     * 个性化参数
     */
    private PersonLoadParam personLoadParam;
    /**
     * 个性化JSON AD标定
     */
    private String adParamJson;
    /**----------              载重1结束              ----------- **/

    /**----------              载重2开始              ----------- **/
    /**
     * 载重传感器与车辆关联
     */
    private String  twoId;
    /**
     * 载重传感器1 --------------------
     * 传感器型号id
     */
    private String  twoSensorId;

    /**
     * 传感器型号
     */
    private String  twoSensorNumber;

    /**
     *  传感器类型
     *  1.温度传感器  2.湿度传感器  3.正反转传感器 4.工时传感器 6.载重传感器
     */
    private String  twoSensorType;

    /**
     * 补偿使能（1:使能,2:禁用）',
     */
    private Integer twoCompensate;
    private String  twoCompensateStr;
    /**
     * 奇偶校验（1：奇校验；2：偶校验；3：无校验）
     */
    private String  twoOddEvenCheck;
    private String  twoOddEvenCheckStr;
    /**
     * 波特率
     */
    private String  twoBaudRate;
    private String  twoBaudRateStr;

    /**
     * 滤波系数（1:实时,2:平滑,3:平稳）
     */
    private Integer twoFilterFactor;
    private String  twoFilterFactorStr;

    /**
     * 载重传感器序号: 0:载重1; 1:载重2
     */
    private Integer twoSensorSequence;
    /**
     * 个性化参数
     */
    private PersonLoadParam twoPersonLoadParam;
    /**
     * 个性化JSON AD标定
     */
    private String twoAdParamJson;
    /**----------              载重2结束              ----------- **/
    /**
     * 备注
     */
    private String remark;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 下发参数id
     */
    private String paramId;

    /**
     * 通讯参数下发id
     */
    private String calibrationParamId;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 组织
     */
    private String groupId;
    /**
     * 组织名称
     */
    private String groupName;

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
     * 常规参数中的传感器外设ID
     */
    private String sensorPeripheralID;


    /**
     * 监控对象类型 {1:车,2:物,3:人}
     */
    private String monitorType;

    /**
     * 0:载重传感器1 1:载重传感器2
     */
    private String sensorSequenceType;

    /**
     * 个性化JSON
     */
    private String personLoadParamJSON;
}
