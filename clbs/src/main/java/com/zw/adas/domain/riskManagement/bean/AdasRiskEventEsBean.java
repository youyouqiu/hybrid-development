package com.zw.adas.domain.riskManagement.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.zw.adas.domain.riskManagement.AdasDealInfo;
import com.zw.adas.domain.riskManagement.form.AdasDealRiskForm;
import com.zw.platform.domain.leaderboard.RiskResultEnum;
import com.zw.platform.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdasRiskEventEsBean extends AdasRiskEsBean implements Serializable {

    /*风险id*/
    @JSONField(name = "risk_id")
    private String riskId;

    /*事件类型*/
    @JSONField(name = "event_type")
    private Integer eventType;

    /*事件类型*/
    @JSONField(name = "event_time", format = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    /*事件编号*/
    @JSONField(name = "event_number")
    private String eventNumber;

    /*企业id*/
    @JSONField(name = "group_id")
    private String groupId;

    @JSONField(name = "alarm_id")
    private String alarmId;

    @JSONField(name = "driver_name")
    private String driverName;

    @JSONField(name = "driver_number")
    private String driverNumber;

    /**
     * media字符串 多个逗号隔开
     */
    private String mediaStr;

    public AdasRiskEventEsBean(String id, String vehicleId, Date warningTime, String riskNumber, String riskType,
        Integer riskLevel, String brand, String driver, Integer status, String dealer, Integer visitTimes,
        Integer riskResult, String riskId, Integer eventType, Date eventTime, String eventNumber, String mediaStr) {
        super(id, vehicleId, warningTime, riskNumber, riskType, riskLevel, brand, driver, status, dealer, visitTimes,
            riskResult);
        this.riskId = riskId;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.eventNumber = eventNumber;
        this.mediaStr = mediaStr;
    }

    public AdasRiskEventEsBean() {

    }

    public static AdasRiskEventEsBean getInstance(String id, Integer status, String dealer, Date dealTime,
        String driverName, String driverNumber) {
        return getInstance(id, status, dealer, dealTime, RiskResultEnum.SUCCESS_FILE.getCode(), driverName,
            driverNumber);
    }

    public static AdasRiskEventEsBean getInstance(String id, Integer status, String dealer, Date dealTime,
        Integer result, String driverName, String driverNumber) {
        AdasRiskEventEsBean riskEventEsBean = new AdasRiskEventEsBean();
        riskEventEsBean.setId(id);
        riskEventEsBean.setStatus(status);
        riskEventEsBean.setDealer(dealer);
        riskEventEsBean.setDealTime(dealTime);
        riskEventEsBean.setRiskResult(result);
        if (!StringUtil.isNullOrBlank(driverName)) {
            riskEventEsBean.setDriverName(driverName);
            riskEventEsBean.setDriver(driverName);
            riskEventEsBean.setDriverNumber(driverNumber);
        }
        return riskEventEsBean;
    }

    public static AdasRiskEventEsBean getInstance(String id, AdasDealInfo dealInfo) {
        return getInstance(id, dealInfo.getStatus(), dealInfo.getDealer(), dealInfo.getDealTime(),
            dealInfo.getRiskResult(), dealInfo.getDriverName(), dealInfo.getDriverNumber());
    }

    public static AdasRiskEventEsBean getInstance(AdasDealRiskForm dealRiskForm, String id) {
        AdasRiskEventEsBean riskEventEsBean = new AdasRiskEventEsBean();
        riskEventEsBean.setId(id);
        riskEventEsBean.setStatus(dealRiskForm.getStatus());
        riskEventEsBean.setDealer(dealRiskForm.getDealer());
        riskEventEsBean.setDriver(dealRiskForm.getDriver());
        riskEventEsBean.setDealTime(dealRiskForm.getDealTime());
        riskEventEsBean.setRiskResult(dealRiskForm.getRiskResult());
        return riskEventEsBean;
    }
}
