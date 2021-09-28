package com.zw.platform.domain.oil;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 油量，油耗分页查询
 *
 * @author  Tdz
 * @create 2017-02-14 10:13
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class PositionlQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String vehicleId;

    private long vtime;// gps时间

    private String vtimeStr = ""; // gps时间String类型，非数据库字段名，用作碳排放能耗统计用 add by liubq 2017-01-19

    private String alarm;

    private String status;//vcc

    private String longtitude;

    private String latitude;

    private String height;

    private String speed;

    private String angle;

    private String temperture;// 环境温度

    private String reserve;

    private String gpsMile;//里程

    private String deviceIdOne;

    private String deviceIdTwo;

    private String messageLengthOne;

    private String messageLengthTwo;

    /**
     * 流量传感器
     */

    private String totalOilwearOne;//一号传感器总油耗

    private String totalOilwearTwo;

    private String oiltankTemperatureOne;// 燃油温度

    private String oiltankTemperatureTwo;

    private String transientOilwearOne;//一号传感器瞬时油耗

    private String transientOilwearTwo;

    private String totalTimeOne;//累计行驶时长

    private String totalTimeTwo;

    private String plateNumber;// 车辆编号

    private String vehicleType;// 车辆类型

    private String fuelType;// 燃料类型

    private String airConditionStatus;//空调状态

    private String satelliteNumber;//卫星颗数

    private String assignmentName;//分组信息

    // 油杆传感器
    private String oilTankIdOne; // 油箱id1

    private String oilTankIdTwo; // 油箱id2

    private String oilTankMsgLengthOne; // 消息长度1

    private String oilTankMsgLengthTwo; // 消息长度2

    private String fuelTemOne; // 燃油温度1

    private String fuelTemTwo; // 燃油温度2

    private String environmentTemOne; // 环境温度1

    private String environmentTemTwo; // 环境温度2

    private String fuelAmountOne; // 加油量1

    private String fuelAmountTwo; // 加油量2

    private String fuelSpillOne; // 漏油量1

    private String fuelSpillTwo; // 漏油量2

    private String oilTankOne; // 油箱油量1

    private String oilTankTwo; // 油箱油量2

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

    private String deviceNumber;//设备编号

    private String simCard;//SIM卡卡号

    private Double mileageSpeed;//里程传感器车速
    private Double mileageTotal; //里程传感器累积里程
}
