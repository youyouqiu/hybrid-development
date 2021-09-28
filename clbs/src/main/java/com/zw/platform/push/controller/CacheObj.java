package com.zw.platform.push.controller;

public class CacheObj {
    /**
     * 缓存对象
     */
    private Object cacheValue;
    /**
     * 缓存过期时间
     */
    private Long ttlTime;

    CacheObj(Object cacheValue, Long ttlTime) {
        this.cacheValue = cacheValue;
        this.ttlTime = ttlTime;
    }

    Object getCacheValue() {
        return cacheValue;
    }

    Long getTtlTime() {
        return ttlTime;
    }

    @Override
    public String toString() {
        return "CacheObj {" + "CacheValue = " + cacheValue + ", ttlTime = " + ttlTime + '}';
    }
}
