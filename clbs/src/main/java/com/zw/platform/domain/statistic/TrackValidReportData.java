package com.zw.platform.domain.statistic;

import lombok.Data;

import java.util.List;

/**
 * @author denghuabing on 2019/12/17 14:23
 */
@Data
public class TrackValidReportData {

    private String monitorId;

    private String monitorName;

    private String groupName;

    private String assignmentName;

    private String signColor;

    private String objectType;

    private List<TrackInfo> trackInfo;
}
