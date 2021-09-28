package com.zw.api.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AlarmDO {
    private String monitorName;
    private int type;
    private long startTime;
    private long stopTime;
    private int source;
    private String startLocation;
    private String stopLocation;
    private String speed;
    private String fenceType;
    private String fenceName;
}
