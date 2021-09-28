package com.zw.protocol.netty;

import com.zw.platform.push.common.MessageHandler;
import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.protocol.netty.common.ApplicationEntity;

public abstract class ServerStart {
    protected ApplicationEntity applicationEntity;

    protected String clientId;

    protected WebClientHandleCom component;

    protected MessageHandler messageHandler;

    public void setApplicationEntity(ApplicationEntity applicationEntity) {
        this.applicationEntity = applicationEntity;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setController(WebClientHandleCom component) {
        this.component = component;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void init() {
        run();
    }

    protected abstract void run();

    public abstract void close();
}
