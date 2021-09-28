package com.zw.adas.domain.riskManagement.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.zw.adas.domain.riskManagement.AdasDealInfo;
import com.zw.adas.domain.riskManagement.form.AdasDealRiskForm;
import com.zw.platform.util.StringUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AdasMediaEsBean extends AdasRiskEventEsBean implements Serializable {

    /*用于判断是证据证据  还是风控证据*/
    @JSONField(name = "media_id")
    private Long mediaId;

    /*证据类型*/
    @JSONField(name = "evidence_type")
    private Integer evidenceType;

    @JSONField(name = "risk_event_id")
    private String riskEventId;

    /*回访id*/
    @JSONField(name = "visit_id")
    private String visitId;

    /*媒体类型*/
    @JSONField(name = "media_type")
    private Integer mediaType;

    @JSONField(name = "driver_name")
    private String driverName;

    @JSONField(name = "driver_number")
    private String driverNumber;

    public AdasMediaEsBean(String id, String vehicleId, Date warningTime, String riskNumber, String riskType,
        Integer riskLevel, String brand, String driver, Integer status, String dealer, Integer visitTimes,
        Integer riskResult, String riskId, Integer eventType, Long mediaId, Integer evidenceType, String riskEventId,
        Date eventTime, String visitId, Integer mediaType, String eventNumber, String mediaIdStr) {
        super(id, vehicleId, warningTime, riskNumber, riskType, riskLevel, brand, driver, status, dealer, visitTimes,
            riskResult, riskId, eventType, eventTime, eventNumber, mediaIdStr);
        this.mediaId = mediaId;
        this.evidenceType = evidenceType;
        this.riskEventId = riskEventId;
        this.visitId = visitId;
        this.mediaType = mediaType;
    }

    public AdasMediaEsBean() {

    }

    public static AdasMediaEsBean getInstance(String id, Integer status, String dealer, Integer result,
        String driverName, String driverNumber) {
        AdasMediaEsBean adasMediaEsBean = new AdasMediaEsBean();
        adasMediaEsBean.setId(id);
        adasMediaEsBean.setStatus(status);
        adasMediaEsBean.setDealer(dealer);
        adasMediaEsBean.setRiskResult(result);
        if (!StringUtil.isNullOrBlank(driverName)) {
            adasMediaEsBean.setDriverName(driverName);
            adasMediaEsBean.setDriverNumber(driverNumber);
            adasMediaEsBean.setDriver(driverName);
        }
        return adasMediaEsBean;
    }

    public static AdasMediaEsBean getInstance(String id, AdasDealInfo dealInfo) {
        return getInstance(id, dealInfo.getStatus(), dealInfo.getDealer(), dealInfo.getRiskResult(),
            dealInfo.getDriverName(), dealInfo.getDriverNumber());
    }

    public static AdasMediaEsBean getInstance(AdasDealRiskForm dealRiskForm, String id) {
        AdasMediaEsBean mediaEsBean = new AdasMediaEsBean();
        mediaEsBean.setId(id);
        mediaEsBean.setStatus(dealRiskForm.getStatus());
        mediaEsBean.setDealer(dealRiskForm.getDealer());
        mediaEsBean.setDriver(dealRiskForm.getDriver());
        mediaEsBean.setRiskResult(dealRiskForm.getRiskResult());
        return mediaEsBean;
    }
}
