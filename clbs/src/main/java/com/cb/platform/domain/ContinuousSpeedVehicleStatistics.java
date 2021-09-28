package com.cb.platform.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 持续超速车辆统计报表实体
 * @author hujun
 * @Date 创建时间：2018年4月27日 上午10:29:22
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ContinuousSpeedVehicleStatistics implements Serializable {
    private static final long serialVersionUID = 7867207533292675598L;

    /**
     * 车牌号
     */
    private String monitorName;
    private String monitorId;
    /**
     * 车牌颜色
     */
    private String plateColor;
    private Integer signColor;
    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 超速一般严重5分钟以下
     */
    private int shortForGeneral = 0;
    /**
     * 超速一般严重5-10分钟
     */
    private int middleForGeneral = 0;
    /**
     * 超速一般严重10分钟以上
     */
    private int longForGeneral = 0;

    /**
     * 超速比较严重5分钟以下
     */
    private int shortForRelatively = 0;
    /**
     * 超速比较严重5-10分钟
     */
    private int middleForRelatively = 0;
    /**
     * 超速比较严重10分钟以上
     */
    private int longForRelatively = 0;

    /**
     * 超速特别严重5分钟以下
     */
    private int shortForEspecially = 0;
    /**
     * 超速特别严重5-10分钟
     */
    private int middleForEspecially = 0;
    /**
     * 超速特别严重10分钟以上
     */
    private int longForEspecially = 0;

    /**
     * 合计
     */
    private int total = 0;

    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 分组名称
     */
    private String assignmentName;
}
