package com.zw.platform.dto.reportManagement;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Description: 加漏油信息（调用paas-cloud api 返回实体）
 * @Author Tianzhangxu
 * @Date 2021/5/18 17:21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OilAmountAndSpillDTO implements Serializable {
    private static final long serialVersionUID = 6829590336777167355L;

    private String id;

    /**
     * 车牌号
     */
    private String monitorName;

    /**
     * 位置数据更新时间
     */
    private String vTimeStr;

    /**
     * 加油量1
     */
    private String fuelAmountOne;

    /**
     * 加油量2
     */
    private String fuelAmountTwo;

    /**
     * 漏油量1
     */
    private String fuelSpillOne;

    /**
     * 漏油量2
     */
    private String fuelSpillTwo;

    /**
     * 油箱油量1
     */
    private String oilTankOne;

    /**
     *  油箱油量2
     */
    private String oilTankTwo;
}
