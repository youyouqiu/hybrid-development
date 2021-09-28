package com.zw.platform.domain.oil;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class Positional implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private byte[] vehicleId;

    private long vtime = 0L;// gps时间

    private long time = 0L; //gps时间(paas_cloud)

    private String uploadTime; //接收定位服务器时间

    private String vtimeStr = ""; // gps时间String类型，非数据库字段名，用作碳排放能耗统计用 add by liubq 2017-01-19

    private String alarm = "0";// 警报

    private String status = "0";// acc

    private String acc = ""; // acc

    private String locationStatus;

    private String longtitude;// 经度（老接口使用）

    private String longitude;// 经度(paas_cloud接口使用)

    private String latitude;// 纬度

    private String height = "0";// 高度

    private String speed = "0";// 速度

    /**
     * 行车记录仪速度
     */
    private String recorderSpeed;

    private String angle = "0";

    private String temperture;// 环境温度

    private String reserve;

    private String gpsMile = "0";// 里程

    private String deviceIdOne;

    private String deviceIdTwo;

    private String messageLengthOne;

    private String messageLengthTwo;

    private String formattedAddress;

    private String locationType; //定位方式

    /**
     * 流量传感器
     */

    private String totalOilwearOne;// 一号传感器总油耗

    private String totalOilwearTwo;// 二号传感器总油耗

    private String oiltankTemperatureOne;// 燃油温度

    private String oiltankTemperatureTwo;

    private String transientOilwearOne;// 一号传感器瞬时油耗

    private String transientOilwearTwo;// 二号传感器瞬时油耗

    private String totalTimeOne;// 累计行驶时长

    private String totalTimeTwo;

    private String plateNumber;// 车辆编号(车牌号)

    private String monitorName;

    private String vehicleType;// 车辆类型

    private String fuelType;// 燃料类型

    private String airConditionStatus;// 空调状态

    private String satelliteNumber;// 卫星颗数

    private String assignmentName;// 分组信息

    // 油杆传感器
    private String oilTankIdOne; // 油箱id1

    private String oilTankIdTwo; // 油箱id2

    private String oilTankMsgLengthOne; // 消息长度1

    private String oilTankMsgLengthTwo; // 消息长度2

    private String fuelTemOne; // 燃油温度1

    private String fuelTemTwo; // 燃油温度2

    private String environmentTemOne; // 环境温度1

    private String environmentTemTwo; // 环境温度2

    private String fuelAmountOne = "0"; // 加油量1

    private String fuelAmountTwo = "0"; // 加油量2

    private String fuelSpillOne = "0"; // 漏油量1

    private String fuelSpillTwo = "0"; // 漏油量2

    private String oilTankOne = "0"; // 油箱油量1

    private String oilTankTwo = "0"; // 油箱油量2

    /**
     * 传感器1液位高度AD值
     */
    private String adHeightOne;

    /**
     * 传感器1液位高度
     */
    private String oilHeightOne;

    /**
     * 传感器2液位高度AD值
     */
    private String adHeightTwo;

    /**
     * 传感器2液位高度
     */
    private String oilHeightTwo;

    private String deviceNumber;// 设备编号

    private String simCard;// SIM卡卡号

    private String plateColor;// 车牌颜色

    private String ico;// 自定义图片

    private Double mileageSpeed;// 里程传感器车速

    private Double mileageTotal; // 里程传感器累积里程

    private Integer tempValueOne;// 温度传感器温度值

    private Integer tempTimeOne; // 温度传感器超出阈值持续时间

    private Integer tempHighLowOne; // 温度传感器高低温报警

    private Integer tempValueTwo;// 温度传感器温度值

    private Integer tempTimeTwo; // 温度传感器超出阈值持续时间

    private Integer tempHighLowTwo; // 温度传感器高低温报警

    private Integer tempValueThree;// 温度传感器温度值

    private Integer tempTimeThree; // 温度传感器超出阈值持续时间

    private Integer tempHighLowThree; // 温度传感器高低温报警

    private Integer tempValueFour;// 温度传感器温度值

    private Integer tempTimeFour; // 温度传感器超出阈值持续时间

    private Integer tempHighLowFour; // 温度传感器高低温报警

    private Integer tempValueFive;// 温度传感器温度值

    private Integer tempTimeFive; // 温度传感器超出阈值持续时间

    private Integer tempHighLowFive; // 温度传感器高低温报警

    private Integer wetnessValueOne; // 湿度传感器湿度值

    private Integer wetnessTimeOne; // 湿度传感器超出阈值持续时间

    private Integer wetnessHighLowOne; // 湿度传感器高低湿度报警

    private Integer wetnessValueTwo; // 湿度传感器湿度值

    private Integer wetnessTimeTwo; // 湿度传感器超出阈值持续时间

    private Integer wetnessHighLowTwo; // 湿度传感器高低湿度报警

    private Integer wetnessValueThree; // 湿度传感器湿度值

    private Integer wetnessTimeThree; // 湿度传感器超出阈值持续时间

    private Integer wetnessHighLowThree; // 湿度传感器高低湿度报警

    private Integer wetnessValueFour; // 湿度传感器湿度值

    private Integer wetnessTimeFour; // 湿度传感器超出阈值持续时间

    private Integer wetnessHighLowFour; // 湿度传感器高低湿度报警

    private Integer winchStatus;// 正反转传感器旋转状态

    private Integer winchOrientation;// 正反转传感器旋转方向

    private Integer winchSpeed; // 正反转传感器旋转速度

    private Integer winchTime; // 正反转传感器累计运行时间

    private Integer winchCounter; // 正反转传感器累计脉冲数量

    private Integer winchRotateTime;// 正反转传感器旋转方向持续时间

    private String originalLongtitude = "0.0"; // 原始经度

    private String originalLatitude = "0.0";  //原始维度

    /**
     * IO口 1、2、3、4
     */
    private Integer ioOne;

    private Integer ioTwo;

    private Integer ioThree;

    private Integer ioFour;

    /**
     * 采集板1
     */
    private String ioObjOne;
    /**
     * 采集板2
     */
    private String ioObjTwo;

    /**
     * 载重1
     */
    private String loadObjOne;

    /**
     * 载重2
     */
    private String loadObjTwo;

    /**
     * 胎压
     */
    private String tirePressureParameter;

    /**
     * 轮胎总数
     */
    private Integer totalTireNum;

    /**
     * obd行程统计信息
     */
    private String obdTripData;

    /**
     * 行驶状态 1:行驶; 2:停止;
     */
    private String drivingState;

    /**
     * obd
     */
    private String obdOriginalVehicleData;

    /**
     * 工时检查方式   0:电压比较式  1:油耗阈值式  2:油耗波动式
     */
    private Integer workInspectionMethod;
    private Integer workInspectionMethodOne;
    private Integer workInspectionMethodTwo;

    /**
     * 工作状态   0:停机 1:工作 2:待机(油耗波动式专有)
     */
    private Integer workingPosition;
    private Integer workingPositionOne;
    private Integer workingPositionTwo;

    /**
     * 波动值
     */
    private Double fluctuateValue;
    private Double fluctuateValueOne;
    private Double fluctuateValueTwo;

    /**
     * 检测数据
     */
    private Double checkData;
    private Double checkDataOne;
    private Double checkDataTwo;

    /**
     * rfid 1D 数据
     */
    private String rfid1D;

    /**
     * rfid 1E 数据
     */
    private String rfid1E;

    /**
     * rfid 1F 数据
     */
    private String rfid1F;

    /**
     * 补传状态
     */
    private Integer reissue;

    /**
     * 基站信息
     */
    private String stationInfo;

    /**
     * 是否有基站信息标识 true：有； false：没有(此字段用于4.4.2轨迹回放列表过滤基站定位信息)
     */
    private Boolean stationEnabled = false;

    /**
     * obd数据流
     */
    private String obdObj;
}
