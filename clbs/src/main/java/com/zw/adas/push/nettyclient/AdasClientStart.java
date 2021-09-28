package com.zw.adas.push.nettyclient;


import com.zw.adas.push.common.AdasWebClientHandleCom;
import com.zw.adas.push.nettyclient.common.AdasApplicationEntity;

public abstract class AdasClientStart {
    protected AdasApplicationEntity adasApplicationEntity;

    protected String clientId;

    protected AdasWebClientHandleCom component;

    public void setAdasApplicationEntity(AdasApplicationEntity adasApplicationEntity) {
        this.adasApplicationEntity = adasApplicationEntity;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setController(AdasWebClientHandleCom component) {
        this.component = component;
    }

    public void init() {
        run();
    }

    protected abstract void run();

    public abstract void close();
}
