package com.zw.adas.push.nettyclient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import com.zw.adas.push.common.AdasWebClientHandleCom;
import com.zw.adas.push.nettyclient.client.AdasWebClientStart;
import com.zw.adas.push.nettyclient.common.AdasApplicationEntity;
import org.springframework.stereotype.Component;

/**
 * lijie
 * 主动安全附件上传
 */
@Component
public class AdasClientComponent {

    @Autowired
    AdasApplicationEntity adasApplicationEntity;

    @Autowired
    private AdasWebClientHandleCom component;

    List<AdasWebClientStart> adasWebServerStarts = new ArrayList<>();

    @PostConstruct
    public void run() {
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            AdasWebClientStart start = new AdasWebClientStart();
            start.setAdasApplicationEntity(adasApplicationEntity);
            start.setController(component);
            start.setClientId(UUID.randomUUID().toString());
            start.init();
            adasWebServerStarts.add(start);
            //  WebSubscribeManager.getInstance().setClientId(clientId);
        }
    }

    @PreDestroy
    public void close() {
        if (adasWebServerStarts == null) {
            return;
        }
        for (AdasClientStart adasServerStart : adasWebServerStarts) {
            adasServerStart.close();
        }
    }
}
