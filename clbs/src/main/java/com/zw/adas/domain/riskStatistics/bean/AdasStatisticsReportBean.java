package com.zw.adas.domain.riskStatistics.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.Getter;

/**
 *
 * @Author zhangqiang
 * @Date 2020/6/18 12:00
 */
@Data
public class AdasStatisticsReportBean {

    @Getter
    private String address;

    @Getter
    private String starTime;
    @Getter
    private String endTime;

    @Getter
    private String eventType;

    @Getter
    private String riskEventId;

    private boolean hasVideo;

    private boolean hasPic;

    private String commonName;


    @JSONField(name = "event_time")
    public void setStarTime(String starTime) {
        this.starTime = starTime;
        this.endTime = starTime;
    }

    @JSONField(name = "address")
    public void setAddress(String address) {
        this.address = address;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @JSONField(name = "event_type")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @JSONField(name = "risk_event_id")
    public void setRiskEventId(String riskEventId) {
        this.riskEventId = riskEventId;
    }
}
