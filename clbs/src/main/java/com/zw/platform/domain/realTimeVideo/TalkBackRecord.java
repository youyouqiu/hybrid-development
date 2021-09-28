package com.zw.platform.domain.realTimeVideo;

import lombok.Data;

@Data
public class TalkBackRecord {
    private String simcardNumber;
    private String riskNumber;
    private int recordNumber = 1;
    private String recordStartTime;
}

