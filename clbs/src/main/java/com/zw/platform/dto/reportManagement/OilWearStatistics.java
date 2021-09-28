package com.zw.platform.dto.reportManagement;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Description: 油耗报表信息(调用paas-cloud api 返回实体)
 * @Author Tianzhangxu
 * @Date 2020/6/19 11:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OilWearStatistics extends SensorBaseDTO implements Serializable {
    private static final long serialVersionUID = -4092986790336885L;

    /**
     * 一号传感器总油耗
     */
    private String totalOilwearOne;

    /**
     * 燃油温度1
     */
    private String oiltankTemperatureOne;

    /**
     *  一号传感器瞬时油耗
     */
    private String transientOilwearOne;

    /**
     * 1号传感器累计行驶时长
     */
    private String totalTimeOne;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 行驶时间
     */
    private String steerTime;
}
