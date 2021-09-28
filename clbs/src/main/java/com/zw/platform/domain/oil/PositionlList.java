package com.zw.platform.domain.oil;

import com.zw.platform.util.common.BaseQueryBean;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

/**
 * 油耗列表
 *
 * @author  Tdz
 * @create 2017-02-14 10:23
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class PositionlList extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    private String vehicleId;

    /**
     * 车辆编号
     */
    @ExcelField(title = "监控对象")
    private String plateNumber;

    /**
     * gps时间
     */
    private long vtime;

    /**
     * 日期格式时间，用于导出报表
     */
    @ExcelField(title = "时间")
    private String stime;

    /**
     * gps时间String类型，非数据库字段名，用作碳排放能耗统计用 add by liubq 2017-01-19
     */
    private String vtimeStr = "";

    private String alarm;

    /**
     * acc
     */
    @ExcelField(title = "ACC")
    private String status;

    /**
     * 总油量
     */
    @ExcelField(title = "总油量")
    private Double oilTankTotal;

    /**
     * 油箱油量1
     */
    @ExcelField(title = "主油箱油量")
    private String oilTankOne;

    /**
     * 油箱油量2
     */
    @ExcelField(title = "副油箱油量")
    private String oilTankTwo;

    /**
     * 传感器1液位高度AD值
     */
    private String adHeightOne;

    /**
     * 传感器1液位高度
     */
    @ExcelField(title = "主油箱液位高度")
    private String oilHeightOne;

    /**
     * 传感器2液位高度AD值
     */
    private String adHeightTwo;

    /**
     * 传感器2液位高度
     */
    @ExcelField(title = "副油箱液位高度")
    private String oilHeightTwo;

    /**
     * 加油量1
     */
    @ExcelField(title = "主油箱加油量")
    private String fuelAmountOne;

    /**
     * 加油量2
     */
    @ExcelField(title = "副油箱加油量")
    private String fuelAmountTwo;

    /**
     * 漏油量1
     */
    @ExcelField(title = "主油箱漏油量")
    private String fuelSpillOne;

    /**
     * 漏油量2
     */
    @ExcelField(title = "副油箱漏油量")
    private String fuelSpillTwo;

    /**
     * 油箱燃油温度1
     */
    @ExcelField(title = "主油箱燃油温度")
    private String fuelTemOne;

    /**
     * 油箱环境温度1
     */
    @ExcelField(title = "主油箱环境温度")
    private String environmentTemOne;

    /**
     * 油箱燃油温度2
     */
    @ExcelField(title = "副油箱燃油温度")
    private String fuelTemTwo;

    /**
     * 油箱环境温度2
     */
    @ExcelField(title = "副油箱环境温度")
    private String environmentTemTwo;

    private String gpsMile;

    private String longtitude;

    private String latitude;

    private String height;

    private String speed;

    private String angle;

    /**
     * 环境温度
     */
    private String temperture;

    private String reserve;

    private String deviceIdOne;

    private String deviceIdTwo;

    private String messageLengthOne;

    private String messageLengthTwo;

    /**
     * 一号传感器总油耗
     */
    private String totalOilwearOne;

    private String totalOilwearTwo;

    /**
     * 燃油温度
     */
    private String oiltankTemperatureOne;

    private String oiltankTemperatureTwo;

    /**
     * 一号传感器瞬时油耗
     */
    private String transientOilwearOne;

    private String transientOilwearTwo;

    /**
     * 累计行驶时长
     */
    private String totalTimeOne;

    private String totalTimeTwo;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 燃料类型
     */
    private String fuelType;

    /**
     * 空调状态
     */
    private String airConditionStatus;

    /**
     * 卫星颗数
     */
    private String satelliteNumber;

    /**
     * 分组信息
     */
    private String assignmentName;

    /**
     *  油箱id1
     */
    private String oilTankIdOne;

    /**
     *  油箱id2
     */
    private String oilTankIdTwo;

    /**
     * 消息长度1
     */
    private String oilTankMsgLengthOne;

    /**
     * 消息长度2
     */
    private String oilTankMsgLengthTwo;

    /**
     * 设备编号
     */
    private String deviceNumber;

    /**
     * SIM卡卡号
     */
    private String simCard;

    /**
     * 里程传感器累积里程
     */
    private Double mileageTotal;

    /**
     * 里程传感器车速
     */
    private Double mileageSpeed;

    /**
     * 总里程，速度，位置字段，用于导出报表时使用。
     */
    @ExcelField(title = "总里程")
    private Double mileForExport;

    @ExcelField(title = "速度")
    private Double speedForExport;

    @ExcelField(title = "位置")
    private String formattedAddress;
}
