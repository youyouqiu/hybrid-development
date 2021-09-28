package com.cb.platform.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/24 10:49
 */
@Data
@NoArgsConstructor
public class OrgOffRouteBasicInfoAndTrendDTO implements Serializable {
    private static final long serialVersionUID = -59563489564925974L;

    /**
     * 基本信息
     */
    private OrgOffRouteDetailDTO basicInfo;

    /**
     * 报警趋势
     */
    private OrgOffRouteTrendDTO alarmTrend;

    public OrgOffRouteBasicInfoAndTrendDTO(OrgOffRouteDetailDTO basicInfo, OrgOffRouteTrendDTO alarmTrend) {
        this.basicInfo = basicInfo;
        this.alarmTrend = alarmTrend;
    }
}
