package com.zw.platform.push.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wjy on 2017/4/20.
 * 实时监控点名用户对应流水号
 */
public class UserCacheS {
    private static Map<String, String> userInfoManager;

    public static synchronized Map<String, String> getInstance() {
        if (userInfoManager == null) {
            userInfoManager = new HashMap<String, String>();
        }
        return userInfoManager;
    }

    public synchronized static void put(String key, String userInfo) {
        userInfoManager.put(key, userInfo);
    }

    public static String getUserInfo(String key) {
        if (userInfoManager != null) {
            return userInfoManager.get(key);
        }
        return null;
    }

    public synchronized static void remove(String key) {
        String statusInfo = userInfoManager.get(key);
        if (statusInfo != null) {
            userInfoManager.remove(key);
        }
    }
}
