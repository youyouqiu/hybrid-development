package com.zw.platform.util.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapUtil {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER = objectMapper;
    }

    /**
     * @param obj
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException   HashMap<String,Object>
     * @Description: javaBean 转 Map
     * @exception:
     * @author: wangying
     * @time:2016年12月15日 上午11:05:34
     */
    public static HashMap<String, Object> objToHash(Object obj) throws IllegalAccessException {
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        Class clazz = obj.getClass();
        List<Class> clazzs = new ArrayList<Class>();

        do {
            clazzs.add(clazz);
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Object.class));

        for (Class subClazz : clazzs) {
            Field[] fields = subClazz.getDeclaredFields();
            for (Field field : fields) {
                Object objVal = null;
                field.setAccessible(true);
                objVal = field.get(obj);
                hashMap.put(field.getName(), objVal);
            }
        }

        return hashMap;
    }

    public static HashMap<String, String> objToMap(Object obj) {
        HashMap<String, String> hashMap = new HashMap<>();
        Class clazz = obj.getClass();
        List<Class> clazzs = new ArrayList<Class>();

        do {
            clazzs.add(clazz);
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Object.class));

        for (Class subClazz : clazzs) {
            Field[] fields = subClazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object objVal = null;
                try {
                    objVal = field.get(obj);
                } catch (IllegalAccessException e) {
                    continue;
                }
                if (Objects.isNull(objVal)) {
                    continue;
                }

                hashMap.put(field.getName(), String.valueOf(objVal));
            }
        }

        return hashMap;
    }

    public static HashMap<String, Object> objNonNullFieldToHash(Object obj) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>(16);
        Class clazz = obj.getClass();
        List<Class> clazzList = new ArrayList<Class>();

        do {
            clazzList.add(clazz);
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Object.class));
        for (Class subClazz : clazzList) {
            Field[] fields = subClazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object objVal;
                try {
                    objVal = field.get(obj);
                } catch (IllegalAccessException e) {
                    continue;
                }
                if (Objects.isNull(objVal)) {
                    continue;
                }

                hashMap.put(field.getName(), objVal);
            }
        }
        return hashMap;
    }

    /**
     * map转换成实体类
     * @param map       map
     * @param beanClass beanClass
     * @param <E>       <E>
     * @return 转换后的实体类
     */
    public static <E> E mapToObj(Map<String, String> map, Class<E> beanClass) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.convertValue(map, beanClass);
        } catch (Exception e) {
            return null;
        }
    }
}
