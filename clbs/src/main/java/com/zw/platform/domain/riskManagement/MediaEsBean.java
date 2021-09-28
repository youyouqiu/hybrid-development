package com.zw.platform.domain.riskManagement;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class MediaEsBean extends RiskEventEsBean implements Serializable {

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

    public MediaEsBean(String id, String vehicleId, Date warningTime, String riskNumber, String riskType,
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

}
