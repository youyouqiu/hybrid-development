package com.zw.platform.push.config;

import com.zw.platform.push.common.WsSessionManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;


/**
 * Created by jiangxiaoqiang on 2016/10/27.
 */
@Component
public class WebSocketConnectHandler implements ApplicationListener<SessionConnectEvent> {

    /**
     * 当客户端与服务端连接时
     */
    @Override
    public void onApplicationEvent(SessionConnectEvent sessionConnectEvent) {
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(sessionConnectEvent.getMessage());
        if (stompHeaderAccessor.getUser() == null) {
            return;
        }
        String userName = stompHeaderAccessor.getUser().getName();
        String sessionId = stompHeaderAccessor.getSessionId();
        if (StringUtils.isNotBlank(userName)) {
            // WebSocketMonitor.getInstance().userFlag.put(userName, true);
            /*
             * 缓存Session
             */
            // RedisHelper.setString(sessionId + "websocketsession", userName, PublicVariable.REDIS_SIX_DATABASE);
            WsSessionManager.INSTANCE.addSessionUser(sessionId, userName);
        }
    }
}
