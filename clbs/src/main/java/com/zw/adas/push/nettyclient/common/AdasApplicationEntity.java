package com.zw.adas.push.nettyclient.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class AdasApplicationEntity implements Serializable {

    private static final long serialVersionUID = -7436090072380226087L;

    @Value("${media.protocol.host}")
    private String host;

    @Value("${media.protocol.port}")
    private int port;
    private long time;

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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}