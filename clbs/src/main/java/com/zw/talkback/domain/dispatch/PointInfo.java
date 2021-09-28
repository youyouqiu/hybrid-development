package com.zw.talkback.domain.dispatch;

import lombok.Data;

@Data
public class PointInfo {
    private String monitorName;

    private String monitorId;

    private Double longitude;

    private Double latitude;

    private String jobId;

    private String jobIcon;

    private String address;

    private String gpsTime;

    private boolean hasTrackPlaybackPermissions;
}