package com.zw.platform.domain.vas.loadmgt;

import lombok.Data;

import java.io.Serializable;

/***
 @Author gfw
 @Date 2018/9/14 13:20
 @Description 载重设置参数
 @version 1.0
 **/
@Data
public class LoadSettingParam implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 补偿使能
     */
    private Integer compensate;
    /**
     * 自动上传时间
     */
    private Integer uploadTime;
    /**
     * 滤波方式
     */
    private Integer smoothing;
    /**
     * 输出修正系数K
     */
    private Integer outputCorrectionK;
    /**
     * 输出修正系数B
     */
    private Integer outputCorrectionB;
    /**
     * 保留项 长度8
     */
    private byte[] reservedItem1;
    /**
     * 重量单位
     */
    private Integer unit;
    /**
     * 保留项 长度2
     */
    private Integer reservedItem2;
    /**
     * 核定载荷重量
     */
    private Integer approvedLoadWeight;
    /**
     * 超载阈值
     */
    private Integer overLoadThreshold;
    /**
     * 超载阈值偏差
     */
    private Integer overLoadThresholdOffset;
    /**
     * 载重测量方案
     */
    private Integer scheme;
    /**
     * 满载阈值
     */
    private Integer fullLoadThreshold;
    /**
     * 满载阈值偏差
     */
    private Integer fullLoadThresholdOffset;
    /**
     * 空载阈值
     */
    private Integer nullLoadThreshold;
    /**
     * 空载阈值偏差
     */
    private Integer nullLoadThresholdOffset;
    /**
     * 轻载阈值
     */
    private Integer lightLoadThreshold;
    /**
     * 轻载阈值偏差
     */
    private Integer lightLoadThresholdOffset;
    /**
     * 保留项 长度14
     */
    private byte[] reservedItem3;
}
