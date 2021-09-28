package com.zw.platform.domain.forwardplatform;

public class ForwardMapping {
    private String monitorId;
    private int platformId;

    public String getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    public int getPlatformId() {
        return platformId;
    }

    public void setPlatformId(int platformId) {
        this.platformId = platformId;
    }

    public ForwardMapping(String monitorId, int platformId) {
        this.monitorId = monitorId;
        this.platformId = platformId;
    }
}
