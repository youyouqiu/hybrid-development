package com.zw.protocol.netty;

import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.push.common.MessageHandler;
import com.zw.platform.push.common.WebClientHandleCom;
import com.zw.platform.push.config.Nullable;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.netty.client.server.VideoClientStart;
import com.zw.protocol.netty.client.server.WebClientStart;
import com.zw.protocol.netty.common.ApplicationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by LiaoYuecai on 2017/7/5.
 */
@Component
public class ServerComponent {

    @Autowired
    private ServerParamList serverParamList;

    @Autowired
    private WebClientHandleCom component;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    private List<ServerStart> serverStarts;

    @Value("${netty.queue.consumer.count:1}")
    private int consumerCount;

    @PostConstruct
    public void run() {
        List<ApplicationEntity> list = serverParamList.getList();
        ServerStart start;
        String clientId = serverParamList.getClientId();
        serverStarts = new ArrayList<>(list.size());
        for (ApplicationEntity entity : list) {
            start = createServerStart(entity);
            if (start == null) {
                continue;
            }
            start.setApplicationEntity(entity);
            start.setController(component);
            start.setMessageHandler(messageHandler);
            start.setClientId(clientId);
            start.init();
            serverStarts.add(start);
        }
        WebSubscribeManager.getInstance().startHandleQueue(taskExecutor, consumerCount);
    }

    @Nullable
    private ServerStart createServerStart(ApplicationEntity entity) {
        switch (entity.getType()) {
            case "protocol":
                return new WebClientStart();
            case "video":
                return new VideoClientStart();
            default:
                return null;
        }
    }

    @PreDestroy
    public void close() {
        if (serverStarts == null) {
            return;
        }
        for (ServerStart serverStart : serverStarts) {
            serverStart.close();
        }
    }
}
