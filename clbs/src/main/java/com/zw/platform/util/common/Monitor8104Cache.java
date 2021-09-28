package com.zw.platform.util.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class Monitor8104Cache {
    /**
     * 下发8104查询终端参数流水号+deviceId缓存
     */
    private Cache<String, String> monitor8104Cache;

    @Value("${send.cache.time.out.minutes:5}")
    private Integer timeOutMinutes;

    @PostConstruct
    private void init() {
        monitor8104Cache = Caffeine.newBuilder().expireAfterWrite(timeOutMinutes.longValue(), TimeUnit.MINUTES).build();
    }

    public void put(String key, String deviceId) {
        monitor8104Cache.put(key, deviceId);
    }

    public void remove(String key) {
        monitor8104Cache.invalidate(key);
    }

    public String get(String key) {
        return monitor8104Cache.getIfPresent(key);
    }

}
