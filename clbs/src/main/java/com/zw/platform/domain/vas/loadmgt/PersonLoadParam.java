package com.zw.platform.domain.vas.loadmgt;

import lombok.Data;

import java.math.BigDecimal;

/***
 @Author gfw
 @Date 2018/9/11 11:36
 @Description 载重传感器个性化参数
 @version 1.0
 **/
@Data
public class PersonLoadParam {
    /**
     * 载重测量方式 0:单计重 1:双计重 2:四计重
     */
    private String loadMeterWay;
    private String loadMeterWayStr;
    /**
     * 传感器重量单位 0:0.1kg 1:1kg 2:10kg 3:100kg
     */
    private String loadMeterUnit;
    private String loadMeterUnitStr;
    /**
     * 空载阈值
     */
    private BigDecimal noLoadValue;
    /**
     * 轻载阈值
     */
    private BigDecimal lightLoadValue;
    /**
     * 满载阈值
     */
    private BigDecimal fullLoadValue;
    /**
     * 超载阈值
     */
    private BigDecimal overLoadValue;
    /**
     * 空载阈值偏差
     */
    private BigDecimal noLoadThreshold;
    /**
     * 轻载阈值偏差
     */
    private BigDecimal lightLoadThreshold;
    /**
     * 满载阈值偏差
     */
    private BigDecimal fullLoadThreshold;
    /**
     * 超载阈值偏差
     */
    private BigDecimal overLoadThreshold;

}
