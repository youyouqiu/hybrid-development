package com.zw.lkyw.domain.trackback;

import lombok.Data;

import java.io.Serializable;

@Data
public class TimeZonePositionalDTO  implements Serializable {

    private String startTime;
    private String endTime;
    private String monitorId;
    private String monitorName;
}
