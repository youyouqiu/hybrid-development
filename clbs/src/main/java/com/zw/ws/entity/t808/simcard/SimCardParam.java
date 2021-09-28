package com.zw.ws.entity.t808.simcard;

import lombok.Data;

import java.io.Serializable;

/**
 * sim卡参数下发
 *
 * @author  Tdz
 * @create 2017-02-20 9:53
 **/
@Data
public class SimCardParam implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 外设ID
     */
    private Integer paramItemId;

    /**
     * 参数长度
     */
    private Integer paramItemLength;
    /**
     * 修正系数
     */
    private Integer correctionCoefficient;
    /**
     * 预警系数
     */
    private Integer forewarningCoefficient;
    /**
     * 月流量阈值
     */
    private Integer monthThresholdValue;
    /**
     * 日流量阈值
     */
    private Integer dayThresholdValue;
    /**
     * 小时流量阈值
     */
    private Integer hourThresholdValue;
    /**
     * 月结日
     */
    private Integer monthlyStatement;
    /**
     * 手机号
     */
    private Long simcardNumber;
    /**
     * 当月流量真实值
     */
    private Integer monthRealValue;
    /**
     * 当日流量真实值
     */
    private Integer dayRealValue;

    private Integer monthTrafficDeadline;
    /**
     * 上报策略
     */
    private Integer type=0;
}
