package com.zw.platform.push.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Component
public class SubcibeTable {
    private static final Logger logger = LogManager.getLogger(SubcibeTable.class);

    /**
     * 缓存最大个数
     */
    private static final Integer CACHE_MAX_NUMBER = 10000000;

    /**
     * 默认过期时间十分钟
     */
    private static final Long TEN_MINUTE = 10 * 60L;

    /**
     * 缓存对象
     */
    private static final Cache<String, CacheObj> cache = Caffeine.newBuilder()
        .maximumSize(CACHE_MAX_NUMBER)
        .expireAfter(new Expiry<String, CacheObj>() {
            @Override
            public long expireAfterCreate(@NonNull String key, @NonNull CacheObj cacheObj, long currentTime) {
                final Long expireTime = cacheObj.getTtlTime();
                if (expireTime == -1) {
                    return Long.MAX_VALUE;
                }
                long delta = Instant.ofEpochMilli(expireTime)
                    .minus(System.currentTimeMillis(), ChronoUnit.MILLIS)
                    .toEpochMilli();
                return TimeUnit.MILLISECONDS.toNanos(delta);
            }

            @Override
            public long expireAfterUpdate(@NonNull String key, @NonNull CacheObj cacheObj, long currentTime,
                @NonNegative long currentDuration) {
                return currentDuration;
            }

            @Override
            public long expireAfterRead(@NonNull String key, @NonNull CacheObj cacheObj, long currentTime,
                @NonNegative long currentDuration) {
                return currentDuration;
            }
        })
        .build();


    /**
     * 设置缓存
     * @param cacheKey 缓存key
     * @param cacheValue 缓存值
     * @param cacheTime 过期时间，单位：秒
     */
    public static void put(String cacheKey, Object cacheValue, long cacheTime) {
        Long ttlTime = null;
        //传入-1时代表没有timeout
        if (cacheTime <= 0L) {
            if (cacheTime == -1L) {
                ttlTime = -1L;
            } else {
                return;
            }
        }
        if (ttlTime == null) {
            ttlTime = Instant.now().plus(cacheTime, ChronoUnit.SECONDS).toEpochMilli();
        }
        CacheObj cacheObj = new CacheObj(cacheValue, ttlTime);
        cache.put(cacheKey, cacheObj);
    }

    /**
     * 设置缓存
     */
    public static void put(String cacheKey, Object cacheValue) {
        put(cacheKey, cacheValue, TEN_MINUTE);
    }

    /**
     * 获取缓存
     */
    public static Object get(String cacheKey) {
        if (containsKey(cacheKey)) {
            CacheObj cacheObj = cache.getIfPresent(cacheKey);
            return cacheObj != null ? cacheObj.getCacheValue() : null;
        }
        return null;
    }

    /**
     * 删除所有缓存
     */
    public static void clear() {
        cache.invalidateAll();
    }

    /**
     * 删除某个缓存
     */
    public static void remove(String cacheKey) {
        cache.invalidate(cacheKey);
    }

    /**
     * 判断缓存在不在,过没过期
     */
    public static boolean containsKey(String cacheKey) {
        final CacheObj value = cache.getIfPresent(cacheKey);
        return value != null;
    }

    public static Integer size() {
        return cache.asMap().size();
    }

}
