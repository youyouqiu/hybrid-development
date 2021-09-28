package com.zw.platform.domain.riskManagement.form;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * RiskCampaignForm
 */
@Data
public class RiskCampaignForm implements Serializable {
    private String id;

    private byte[] riskId;

    private byte[] riskEventId;

    /**
     * 车辆id
     */
    private String vehicleId;

    /**
     * 风险编号
     */
    private String riskNumber;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 风险状态
     */
    private String status;

    /**
     * 速度
     */
    private double speed;

    /**
     * 处理人
     */
    private String dealId;

    /**
     * 司机
     */
    private String driverId;

    /**
     * 位置
     */
    private String address;

    /**
     * 岗位
     */
    private String job;

    /**
     * 归档时间
     */
    private Date fileTime;

    private Long fileTimestamp;

    /**
     * 处理时间
     */
    private Date dealTime;

    private Long dealTimestamp;

    /**
     * 风控结果
     */
    private int riskResult;

    /**
     * 事件类型
     */
    private String riskEvent;

    /**
     * 车牌号
     */
    private String brand;

    /**
     * 企业名
     */
    private String groupName;

    /**
     * 车队点话
     */
    private String groupPhone;

    /**
     * 司机名
     */
    private String driverName;

    /**
     * 司机点话
     */
    private String driverPhone;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系人点话
     */
    private String emergencyContactPhone;

    /**
     * 0不显示、1显示
     */
    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    private long startTime;

    private long endTime;

    /**
     * 开始时间
     */
    private Date warningTime;

    private Date endTimeStr;

    private long tempTime;

    private long updateTime;

    private String visit1;

    private String visit2;

    private String visit3;

    private String visit4;

    private Integer visitTimes;

    private long lastVisitTime;

    private String mediaUrl;

    private String mediaName;

    private short supervise;

    private short accurate;
}
