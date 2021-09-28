package com.zw.ws.entity.t808.location;

import lombok.Data;

import java.io.Serializable;

/**
 * 位置信息附加sim卡信息表
 *
 * @author  Tdz
 * @create 2017-03-04 15:21
 **/
@Data
public class LocationAttachSimCrad implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer len;

    /**
     * 当日流量
     */
    private String dayRealValue;

    /**
     * 当月流量
     */
    private String monthRealValue;

    /**
     * 流量状态
     */
    private String trafficState;

    /**
     *  iccid
     */
    private String iccid;

    /**
     *  imsi
     */
    private String imsi;

    /**
     *  imei
     */
    private String imei;

    /**
     *  修正系数
     */
    private String correctionCoefficient;

    /**
     * 预警系数
     */
    private String forewarningCoefficient;

    /**
     * 月流量阈值
     */
    private String monthThresholdValue;

    /**
     * 日流量阈值
     */
    private String dayThresholdValue;

    /**
     * 小时流量阈值
     */
    private String hourThresholdValue;

}
