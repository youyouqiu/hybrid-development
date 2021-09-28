package com.zw.platform.domain.vas.loadmgt;

import lombok.Data;

import java.io.Serializable;

/***
 @Author gfw
 @Date 2018/9/14 11:31
 @Description 载重接收参数
 @version 1.0
 **/
@Data
public class LoadReceiveParam implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 外设ID 0x70~0x71
     */
    private Integer id;
    /**
     * 消息长度
     */
    private Integer len;
    /**
     * 重要数据标识
     * 0 普通数据；1:重要数据；
     */
    private Integer important;
    /**
     * 是否传感器异常
     *  1 异常 0正常
     */
    private Integer unusual;
    /**
     * 重量单位
     * 单位：0-0.1Kg；1-1kg；2-10kg；3-100kg；4-255 保留
     */
    private Integer unit;
    /**
     * 载重状态
     * 01-空载； 02-满载； 03-超载； 04-装载； 05-卸载；06- 轻载；07-重载
     */
    private Integer status;
    /**
     * 保留
     */
    private Integer reservedItem1;
    /**
     * 卸载/装载次数
     */
    private Integer countNum;
    /**
     * 保留
     */
    private Integer reservedItem2;
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
     * 保留
     */
    private Integer reservedItem3;
    /**
     * 原始 AD 值
     */
    private Double originalAd;
    /**
     * 保留
     */
    private Integer reservedItem4;
    /**
     * 浮动零点
     */
    private Double floatAd;
}
