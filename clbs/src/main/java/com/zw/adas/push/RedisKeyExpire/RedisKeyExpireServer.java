package com.zw.adas.push.RedisKeyExpire;

import com.zw.adas.push.RedisKeyExpire.handler.KeyExpireHandlerImpl;
import com.zw.adas.push.RedisKeyExpire.listener.KeyExpiredListener;
import com.zw.adas.push.RedisKeyExpire.subscribe.KeyExpiredSubscriber;
import com.zw.ws.common.PublicVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class RedisKeyExpireServer {
    @Autowired
    private KeyExpireHandlerImpl keyExpireHandlerImpl;

    private KeyExpiredListener keyExpiredListener;

    private Thread thread;

    @PostConstruct
    public void start() {
        keyExpiredListener = new KeyExpiredListener(keyExpireHandlerImpl);
        KeyExpiredSubscriber keyExpiredSubscriber = new KeyExpiredSubscriber(keyExpiredListener);
        thread = new Thread(() -> keyExpiredSubscriber.sendInfo(PublicVariable.REDIS_ELEVEN_DATABASE));
        thread.start();
    }

    @PreDestroy
    public void close() {
        System.out.println("RedisKeyExpireServer close().");
        keyExpiredListener.punsubscribe();
        thread.interrupt();
    }
}
