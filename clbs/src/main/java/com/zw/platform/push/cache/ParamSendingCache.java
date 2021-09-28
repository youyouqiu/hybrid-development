package com.zw.platform.push.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/***
 @Author zhengjc
 @Date 2019/12/3 9:39
 @Description 参数下发缓存
 @version 1.0
 **/
@Component
public class ParamSendingCache {

    /**
     * 下发流水号用户sim卡号的缓存
     */
    private Cache<String, SendTarget> cache;

    @Value("${send.cache.time.out.minutes:5}")
    private Integer timeOutMinutes;

    @PostConstruct
    private void init() {
        cache = Caffeine.newBuilder().expireAfterWrite(timeOutMinutes.longValue(), TimeUnit.MINUTES).build();
    }

    /**
     * 在下发的时候存放一个模块和用户以及下发流水号的缓存，用于通用应答进行转发
     * @param key
     * @param sendTarget
     */
    public void put(String key, SendTarget sendTarget) {
        cache.put(key, sendTarget);
    }

    public void put(String userName, Integer serialNumber, String simCardNumber, SendTarget sendTarget) {
        put(getKey(userName, serialNumber, simCardNumber), sendTarget);
    }

    public void remove(String key) {
        cache.invalidate(key);
    }

    public SendTarget get(String key) {
        return cache.getIfPresent(key);
    }

    public String getKey(String userName, Integer serialNumber, String simCardNumber) {
        StringBuilder sb = new StringBuilder();
        sb.append(userName).append("_").append(serialNumber).append("_").append(simCardNumber);
        return sb.toString();
    }
}
