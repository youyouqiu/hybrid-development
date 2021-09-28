package com.zw.adas.push.RedisKeyExpire.handler.abstracts;

public abstract class KeyExpireHandlerAbstract {

    public abstract void executePlatformRemind(String expireKey);

    public void execute(String expireKey) {

        executePlatformRemind(expireKey);
    }
}
