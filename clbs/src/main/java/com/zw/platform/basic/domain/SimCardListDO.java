package com.zw.platform.basic.domain;

import lombok.Data;

import java.util.Date;

/**
 * @author: zjc
 * @Description:sim卡列表信息do,需要和数据库一一对应
 * @Date: create in 2020/11/6 10:40
 */
@Data
public class SimCardListDO {
    /**
     * id
     */
    private String id;

    /**
     * 下发状态
     */
    private Integer sendStatus;

    /**
     * 下发参数id
     */
    private Integer sendParamId;

    /**
     * ICCID
     */
    private String iccid;

    /**
     * IMEI
     */
    private String imei;

    /**
     * IMSI
     */
    private String imsi;
    /**
     * 终端手机号
     */
    private String simcardNumber;

    /**
     * 真实SIM卡号
     */
    private String realId;
    /**
     * 所属企业id
     */
    private String orgId;

    /**
     * 启停状态0:停用,1:启用
     */
    private Integer isStart;

    /**
     * 运营商
     */
    private String operator;

    /**
     * 发放地市（1120改动）
     */
    private String placementCity;

    /**
     * 套餐流量(M)
     */
    private String simFlow;

    /**
     * 当日流量(M)
     */
    private String dayRealValue;

    /**
     * 当月流量(M)
     */
    private String monthRealValue;

    /**
     * 流量最后更新时间
     */
    private String monthTrafficDeadline;

    /**
     * 月预警流量(M)
     */
    private String alertsFlow;
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
     * 激活日期
     */
    private Date openCardTime;

    /**
     * 到期时间yyyy-MM-dd
     */
    private Date endTime;

    /**
     * 终端手机号
     */
    private String deviceNumber;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 数据创建时间
     */

    private Date createDataTime;

    /**
     * 数据修改时间
     */

    private Date updateDataTime;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 信息配置id
     */
    private String configId;

}
