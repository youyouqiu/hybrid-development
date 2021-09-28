package com.zw.platform.domain.riskManagement.form;

import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Data
public class RiskReportForm implements Serializable {
    private String id;

    private byte[] idByte;

    /**
     * 风险编号
     */
    private String riskNumber;

    /**
     * 预警时间
     */
    private Date warningTime;

    /**
     * 监控对象
     */
    private String brand;

    /**
     * 状态
     */
    private String status;

    /**
     * 出理时间
     */
    private String dealTime;

    /**
     * 风险类型
     */
    private String riskType;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 速度
     */
    private double speed;

    private List<Map<String, String>> driverPics = new ArrayList<>();

    /**
     * 预警记录
     */
    private List<RiskEventAlarmReportForm> reafList = new ArrayList<>();

    /**
     * 预警位置
     */
    private String address;

    /**
     * 所属企业
     */
    private String groupName;

    /**
     * 企业id
     */
    private String groupId;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 车队管理电话
     */
    private String groupPhone;

    /**
     * 一大群司机
     */
    private List<ProfessionalsInfo> drivers = new ArrayList<>();

    /**
     * 处理的那个司机
     */
    private ProfessionalsInfo driver;

    /**
     * 处理人
     */
    private String dealId;

    private List<RiskVisitReportForm> riskVisits = new ArrayList<>();

    /**
     * 风控结果
     */
    private String riskResult;

    private RiskVisitReportForm dealVisit;

    /**
     * 车辆类别
     */
    private String vehicleCategory;

    private String vehicleId;

    private String driverIds;

    private String visit1;

    private String visit2;

    private String visit3;

    private String visit4;

    private String driverNames;

    private Long fileTime;

    private String visitTimes;

    private String weather;

}
