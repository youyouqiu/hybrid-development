package com.zw.platform.domain.riskManagement;

import com.alibaba.fastjson.annotation.JSONField;
import com.zw.app.domain.alarm.RiskRankResult;
import com.zw.platform.domain.leaderboard.RISKRESULT;
import com.zw.platform.util.common.UuidUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class RiskEventEsBean extends RiskEsBean implements Serializable {

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

    /**
     * media字符串 多个逗号隔开
     */
    private String mediaStr;

    public RiskEventEsBean(String id, String vehicleId, Date warningTime, String riskNumber, String riskType,
                           Integer riskLevel, String brand, String driver, Integer status, String dealer,
                           Integer visitTimes, Integer riskResult, String riskId, Integer eventType,
                           Date eventTime, String eventNumber, String mediaStr) {
        super(id, vehicleId, warningTime, riskNumber, riskType, riskLevel, brand, driver, status, dealer, visitTimes,
            riskResult);
        this.riskId = riskId;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.eventNumber = eventNumber;
        this.mediaStr = mediaStr;
    }

    public RiskEventEsBean() {

    }

    public static RiskEventEsBean getInstance(String id, Integer status, String dealer, Date dealTime) {
        RiskEventEsBean riskEventEsBean = new RiskEventEsBean();
        riskEventEsBean.setId(id);
        riskEventEsBean.setStatus(status);
        riskEventEsBean.setDealer(dealer);
        riskEventEsBean.setDealTime(dealTime);
        riskEventEsBean.setRiskResult(RISKRESULT.SUCCESS_FILE.getCode());
        return riskEventEsBean;
    }
}
