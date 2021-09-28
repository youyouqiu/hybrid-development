package com.zw.platform.basic.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * sim卡实体
 * @author wangying
 */
@Data
public class SimCardInfoDo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * sim卡信息
     */
    private String id;

    /**
     * sim卡号
     */
    private String simCardNumber;

    /**
     * 启停状态
     */
    private Integer isStart;

    /**
     * 运营商
     */
    private String operator;

    /**
     * 开卡时间
     */
    private Date openCardTime;

    /**
     * 容量
     */
    private String capacity;

    /**
     * 网络类型
     */
    private String networkType;

    /**
     * 套餐流量
     */
    private String simFlow;

    /**
     * 已用流量
     */
    private String useFlow;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 月预警流量(M)
     */
    private String alertsFlow;

    private Date endTime;

    /**
     * 所属企业id
     */
    private String orgId;

    /**
     * ICCID
     */
    private String iccid;
    /**
     * IMSI
     */
    private String imsi;

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
     * 流量月结日
     */
    private String monthlyStatement;

    /**
     * 当月流量(M)
     */
    private String monthRealValue;

    /**
     * 当日流量(M)
     */
    private String dayRealValue;

    /**
     * IMEI
     */
    private String imei;

    /**
     * 终端编号
     */
    private String deviceNumber;

    /**
     * 流量最后更新时间
     */
    private String monthTrafficDeadline;
    /**
     * 备注信息
     */
    private String remark;

    private String fakeIp;

    /**
     * 车辆id
     */
    private String vehicleId;
    /**
     * 真实SIM卡号
     */
    private String realId;

    /**
     * 发放城市
     */
    private String placementCity;


    /**
     * 绑定id
     */
    private String bindId;
}
