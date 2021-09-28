package com.zw.adas.push.RedisKeyExpire.listener;


import com.zw.adas.push.RedisKeyExpire.handler.KeyExpireHandlerImpl;
import com.zw.adas.push.RedisKeyExpire.handler.abstracts.KeyExpireHandlerAbstract;
import redis.clients.jedis.JedisPubSub;


public class KeyExpiredListener extends JedisPubSub {

    private final KeyExpireHandlerAbstract keyExpireHandler;

    public KeyExpiredListener(KeyExpireHandlerImpl keyExpireHandlerImpl) {
        this.keyExpireHandler = keyExpireHandlerImpl;
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        //System.err.println("onPSubscribe " + pattern + " " + subscribedChannels);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        // System.err.println(
        //     "pattern = [" + pattern + "], channel = [" + channel + "], message = [" + message + "]");
        keyExpireHandler.execute(message);
    }

}
