package com.zw.platform.basic.dto;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:实时监控需要的simcard信息
 * @Date: create in 2021/2/25 15:38
 */
@Data
public class F3SimCardDTO {
    /**
     * sim卡号
     */
    private String simcardNumber;
    /**
     * iccid
     */
    private String iccid;
    /**
     * 终端手机号
     */
    private String deviceNumber;
    /**
     * imei
     */
    private String imei;

    /**
     * 当日流量(M)
     */
    private String dayRealValue;
    /**
     * 当月流量(M)
     */
    private String monthRealValue;
    /**
     * 流量月结日
     */
    private String monthlyStatement;
    /**
     * 修正系数
     */
    private String correctionCoefficient;
    /**
     * 预警系数
     */
    private String forewarningCoefficient;
    /**
     * 小时流量阈值(M)
     */
    private String hourThresholdValue;
    /**
     * 日流量阈值(M)
     */
    private String dayThresholdValue;
    /**
     * 月流量阈值(M)
     */
    private String monthThresholdValue;
    /**
     * 监控对象
     */
    private String brand;
    /**
     * sim卡手机号
     */
    private String sid;
    /**
     * 绑定id(信息配置id)
     */
    private String bindId;

    /**
     * 车辆id
     */
    private String vid;

    /**
     * 监控对象类型
     */
    private String monitorType;

    /**
     * 流量最后更新时间
     */
    private String monthTrafficDeadline;
}
