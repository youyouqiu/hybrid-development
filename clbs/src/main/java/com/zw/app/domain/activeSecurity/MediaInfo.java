package com.zw.app.domain.activeSecurity;

import lombok.Data;

import java.util.Date;

@Data
public class MediaInfo {

    private Date eventTime;

    private String eventName;

    private String mediaUrl;
    /**
     * 协议类型（1.黑标，12.川标，13.冀标）
     */
    private int protocolType;

    private String eventId;

    private String riskEventId;
}
