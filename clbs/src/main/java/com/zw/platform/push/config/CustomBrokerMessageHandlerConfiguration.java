package com.zw.platform.push.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.web.socket.config.annotation.DelegatingWebSocketMessageBrokerConfiguration;

@Configuration
public class CustomBrokerMessageHandlerConfiguration extends DelegatingWebSocketMessageBrokerConfiguration {
    @Bean
    @Override
    public AbstractBrokerMessageHandler simpleBrokerMessageHandler() {
        SimpleBrokerMessageHandler handler = (SimpleBrokerMessageHandler) super.simpleBrokerMessageHandler();

        if (handler != null) {
            final CustomSubscriptionRegistry subscriptionRegistry = new CustomSubscriptionRegistry();
            subscriptionRegistry.setCacheLimit(8192);
            handler.setSubscriptionRegistry(subscriptionRegistry);
        }

        return handler;
    }
}
