package com.zw.platform.domain.vas.oilmgt;

import lombok.Data;

/**
 * 油耗统计
 * Created by Tdz on 2016/9/18.
 */
@Data
public class OilStatistical {
    /**
     * 油量
     */
    private Integer oilMass;
    /**
     *里程
     */
    private Integer mileage;
    /**
     * 速度
     */
    private Integer speed;
    /**
     * 油量1
     */
    private String oilMassOne;
    /**
     * 油量2
     */
    private String oilMassTwo;
    /**
     * 燃油温度
     */
    private String oilTem;
    /**
     * 环境温度
     */
    private String envTem;
    /**
     * 累计油耗
     */
    private String allExpend;

    /**
     * 空调开关
     * 1：开启
     * null：关闭
     */
    private Integer isOpen;

}
