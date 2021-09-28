package com.zw.platform.util;

import com.google.common.collect.Maps;

import java.util.Map;


public class ConstantMap {

    public static Map<Integer, String> alarmTypeMap = Maps.newHashMap();

    static {
        alarmTypeMap.put(7702, "异动报警(客运)");
        alarmTypeMap.put(7703, "异动报警(山路)");
    }

}
