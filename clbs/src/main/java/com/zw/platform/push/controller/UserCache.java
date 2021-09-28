package com.zw.platform.push.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户对应流水号
 *
 * @author  Tdz
 * @create 2017-03-14 15:59
 **/
public class UserCache {

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();

    public static Map<String, String> getInstance() {
        return CACHE;
    }

    public static void put(String key, String userInfo) {
        CACHE.put(key, userInfo);
    }

    public static String getUserInfo(String key) {
        return CACHE.get(key);
    }

    public static void remove(String key) {
        CACHE.remove(key);
    }
}
