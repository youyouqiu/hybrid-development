package com.zw.platform.domain.basicinfo;

import com.zw.platform.basic.dto.SimCardDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * sim卡实体
 * @author wangying
 */
@Data
@NoArgsConstructor
public class SimcardInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * sim卡信息
     */
    private String id;

    /**
     * sim卡号
     */
    private String simcardNumber;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd")
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

    private String alertsFlow;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    // add by liubq 2016/10/20
    private String groupId = ""; // 所属企业id

    private String groupName = ""; // 所属企业名称

    private String ICCID;

    private String IMSI;

    private String correctionCoefficient;

    private String forewarningCoefficient;

    private String hourThresholdValue;

    private String dayThresholdValue;

    private String monthThresholdValue;

    private String monthlyStatement;

    private String monthRealValue;

    private String dayRealValue;

    private String imei;

    private String deviceNumber;

    private String monthTrafficDeadline;

    private String remark;

    private String fakeIp;

    private String vehicleId;

    private String realId;

    public SimcardInfo(SimCardDTO simCard) {
        this.id = simCard.getId();
        this.simcardNumber = simCard.getSimcardNumber();
        this.isStart = simCard.getIsStart();
        this.operator = simCard.getOperator();
        this.openCardTime = simCard.getOpenCardTime();
        this.capacity = simCard.getCapacity();
        this.networkType = simCard.getNetworkType();
        this.simFlow = simCard.getSimFlow();
        this.useFlow = simCard.getUseFlow();
        this.alertsFlow = simCard.getAlertsFlow();
        this.endTime = simCard.getEndTime();
        this.groupId = simCard.getOrgId();
        this.groupName = simCard.getOrgName();
        this.ICCID = simCard.getIccid();
        this.IMSI = simCard.getImsi();
        this.correctionCoefficient = simCard.getCorrectionCoefficient();
        this.forewarningCoefficient = simCard.getForewarningCoefficient();
        this.hourThresholdValue = simCard.getHourThresholdValue();
        this.dayThresholdValue = simCard.getDayThresholdValue();
        this.monthThresholdValue = simCard.getMonthThresholdValue();
        this.monthlyStatement = simCard.getMonthlyStatement();
        this.monthRealValue = simCard.getMonthRealValue();
        this.dayRealValue = simCard.getDayRealValue();
        this.imei = simCard.getImei();
        this.deviceNumber = simCard.getDeviceNumber();
        this.monthTrafficDeadline = simCard.getMonthTrafficDeadline();
        this.remark = simCard.getRemark();
        this.fakeIp = simCard.getFakeIP();
        this.vehicleId = simCard.getVehicleId();
        this.realId = simCard.getRealId();
    }
}
