package com.zw.protocol.netty.common;

import java.io.Serializable;


public class ApplicationEntity implements Serializable {
    private static final long serialVersionUID = -7436090072380226087L;

    private String id;
    private String host;
    private int port;
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    public String toUrl() {
        return host + ":" + port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}