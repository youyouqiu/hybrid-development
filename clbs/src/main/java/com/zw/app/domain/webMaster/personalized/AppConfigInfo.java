package com.zw.app.domain.webMaster.personalized;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lijie
 * @date 2018/8/22 15:55
 */
@Data
public class AppConfigInfo implements Serializable{
    private int aggrNum;
    private int queryHistoryPeriod;
    private int queryAlarmPeriod;
    private int maxStatObjNum;
}
