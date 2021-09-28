package com.zw.platform.domain.statistic.info;

import lombok.Data;

/**
 * 载重INFO
 * @author zhouzongbo on 2018/9/10 14:50
 */
@Data
public class LoadInfo {

    private static final long serialVersionUID = 2725940595737007262L;

    /**
     * 消息长度
     */
    private Integer len;

    /**
     * 重要数据标识 0 普通数据；1:重要数据；
     */
    private Integer important;

    /**
     * 重量单位
     * 单位：0: 0.1Kg；1: 1kg；2: 10kg；3: 100kg；4-255 保留
     */
    private Integer unit;

    /**
     * 载重状态
     * 01: 空载； 02: 满载； 03: 超载； 04: 装载； 05: 卸载；06: 轻载；07: 重载
     */
    private Integer status;

    /**
     * 卸载/装载次数
     */
    private Integer countNum;


    /**
     * 载荷重量
     */
    private Double loadWeight;

    /**
     * 装载/卸载重量
     */
    private Double weight;

    /**
     * 载重相对值
     */
    private Double weightAd;

    /**
     * 原始 AD 值
     */
    private Double originalAd;

    /**
     * 浮动零点
     */
    private Double floatAd;


}
