package com.zw.platform.event;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class AssignmentDeleteEvent extends ApplicationEvent {

    private String id;

    private List<String> ids;

    private String ipAddress;

    public AssignmentDeleteEvent(Object source, String id, List<String> ids, String ipAddress) {
        super(source);
        this.id = id;
        this.ids = ids;
        this.ipAddress = ipAddress;
    }

    public String getId() {
        return id;
    }

    public List<String> getIds() {
        return ids;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
