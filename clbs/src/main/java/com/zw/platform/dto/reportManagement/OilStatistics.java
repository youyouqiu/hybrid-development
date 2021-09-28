package com.zw.platform.dto.reportManagement;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Description: 油量报表信息（调用paas-cloud api 返回实体）
 * @Author Tianzhangxu
 * @Date 2020/6/19 9:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OilStatistics extends SensorBaseDTO implements Serializable {

    private static final long serialVersionUID = 5703857317290344682L;

    /**
     * 油箱id1
     */
    private String oilTankIdOne;
    /**
     * 油箱id2
     */
    private String oilTankIdTwo;
    /**
     * 燃油温度1
     */
    private String fuelTemOne;
    /**
     * 燃油温度2
     */
    private String fuelTemTwo;
    /**
     * 环境温度1
     */
    private String environmentTemOne;
    /**
     * 环境温度2
     */
    private String environmentTemTwo;
    /**
     * 加油量1
     */
    private String fuelAmountOne = "0";
    /**
     * 加油量2
     */
    private String fuelAmountTwo = "0";
    /**
     * 漏油量1
     */
    private String fuelSpillOne = "0";
    /**
     * 漏油量2
     */
    private String fuelSpillTwo = "0";
    /**
     * 油箱油量1
     */
    private String oilTankOne = "0";
    /**
     *  油箱油量2
     */
    private String oilTankTwo = "0";
    /**
     * IO口 1、2、3、4
     */
    private Integer ioOne;

    private Integer ioTwo;

    private Integer ioThree;

    private Integer ioFour;
    /**
     *  空调状态
     */
    private String airConditionStatus;

    /**
     * 传感器1液位高度
     */
    private String oilHeightOne;

    /**
     * 传感器2液位高度
     */
    private String oilHeightTwo;
}
