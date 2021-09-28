package com.zw.app.domain.webMaster.personalized;

import lombok.Data;

import java.io.Serializable;
@Data
public class PlatformAppConfig implements Serializable {
    private String aggrNum;
    private String queryHistoryPeriod;
    private String queryAlarmPeriod;
    private String maxStatObjNum;
}
