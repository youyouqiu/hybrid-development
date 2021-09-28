package com.zw.adas.push.RedisKeyExpire.subscribe;

import com.zw.adas.push.RedisKeyExpire.listener.KeyExpiredListener;
import com.zw.platform.basic.core.RedisHelper;


/**
 * 订阅redis key失效
 * @author Administrator
 */
public class KeyExpiredSubscriber {


    private final KeyExpiredListener keyExpiredListener;

    public KeyExpiredSubscriber(KeyExpiredListener keyExpiredListener) {
        this.keyExpiredListener = keyExpiredListener;
    }

    public void sendInfo(int dbIndex) {
        RedisHelper.publish(dbIndex, keyExpiredListener);
    }

}
