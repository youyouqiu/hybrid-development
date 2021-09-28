package com.zw.platform.event;

import org.springframework.context.ApplicationEvent;

public class VideoPlayEvent extends ApplicationEvent {
    private final String monitorId;

    private final Integer channel;

    public VideoPlayEvent(Object source, String monitorId, Integer channel) {
        super(source);
        this.monitorId = monitorId;
        this.channel = channel;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public Integer getChannel() {
        return channel;
    }

}
