package com.zw.platform.push.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lijie on 2020/10/28.
 * app点名对应流水号
 */
public class UserCacheA {
    private static Map<String, String> sessionIdManager;

    public static synchronized Map<String, String> getInstance() {
        if (sessionIdManager == null) {
            sessionIdManager = new HashMap<String, String>();
        }
        return sessionIdManager;
    }

    public static synchronized void put(String key, String userInfo) {
        sessionIdManager.put(key, userInfo);
    }

    public static String getSessionId(String key) {
        if (sessionIdManager != null) {
            return sessionIdManager.get(key);
        }
        return null;
    }

    public static synchronized void remove(String key) {
        String statusInfo = sessionIdManager.get(key);
        if (statusInfo != null) {
            sessionIdManager.remove(key);
        }
    }
}
