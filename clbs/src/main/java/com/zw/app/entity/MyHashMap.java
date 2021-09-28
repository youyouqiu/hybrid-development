package com.zw.app.entity;

import java.util.HashMap;

/***
 @Author gfw
 @Date 2018/12/10 14:57
 @Description 单例hashMap
 @version 1.0
 **/
public class MyHashMap extends HashMap {
    private static volatile MyHashMap instance;

    private MyHashMap() {}

    public static MyHashMap getInstance() {

        if (instance == null) {

            synchronized (MyHashMap.class) {

                if (instance == null) {

                    instance = new MyHashMap();

                }

            }

        }

        return instance;

    }
}
