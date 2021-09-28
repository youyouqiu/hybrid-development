package com.zw.platform.push.config;

import com.zw.adas.push.common.RiskSessionManager;
import com.zw.platform.push.common.WsSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Created by jiangxiaoqiang on 2016/10/27.
 */
@Component
public class WebSocketDisconnectHandler implements ApplicationListener<SessionDisconnectEvent> {
    private static final Logger logger = LogManager.getLogger(WebSocketDisconnectHandler.class);


    /**
     * 当客户端断开连接时 删除服务端缓存
     */
    @Override
    public void onApplicationEvent(SessionDisconnectEvent sessionDisconnectEvent) {
        try {
            String sessionId = sessionDisconnectEvent.getSessionId();
            WsSessionManager.INSTANCE.removeBySession(sessionId);
            //断开连接时删除
            RiskSessionManager.INSTANCE.removeReminders(sessionId);
            RiskSessionManager.INSTANCE.unsubscribeRisk(sessionId);
        } catch (Exception e) {
            logger.error("websocket链接断开,处理失败", e);
        }
    }

}
