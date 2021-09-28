package com.zw.platform.push.config;

import com.zw.platform.push.handler.WebSocketHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@Import(CustomBrokerMessageHandlerConfiguration.class)
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        /*
         * 启用了STOMP代理中继功能：并将其目的地前缀设置为 "/topic"；
         * spring就能知道 所有目的地前缀为"/topic" 的消息都会发送到STOMP代理中；
         */
        config.enableSimpleBroker("/topic", "/queue");
        /*
         * 设置了应用的前缀为"app"：所有目的地以"/app"打头的消息（发送消息url not连接url）
         * 都会路由到带有@MessageMapping注解的方法中，而不会发布到代理队列或主题中；
         */
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
    	registration.setMessageSizeLimit(1310720);//109656
    	registration.setSendBufferSizeLimit(10485760);
    	registration.setSendTimeLimit(25000);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws", "/videoPlaybackForward")
            .setAllowedOrigins("*")
            .addInterceptors(new WebSocketHandshakeInterceptor())
            .withSockJS()
            .setClientLibraryUrl("/clbs/resources/js/sockjs-1.1.1.min.js");
    }
}
