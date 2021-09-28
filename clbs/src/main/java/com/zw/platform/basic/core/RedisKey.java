package com.zw.platform.basic.core;

import com.zw.platform.basic.constant.RedisKeysConvert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 张娟
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisKey {
    private final String key;
    private final int database;

    public String get() {
        return key;
    }

    public int database() {
        return database;
    }


    /**
     * 静态方法
     *
     * @param convert RedisKeysConvert
     * @param args    参数值
     * @return RedisKey
     */
    public static RedisKey of(@NonNull RedisKeysConvert convert, Object... args) {
        return new RedisKey(convert.build(args), convert.getDbIndex());
    }

    public static String get(RedisKey key) {
        return key.get();
    }

    public static String[] get(RedisKey[] keys) {
        String[] stringKeys = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            stringKeys[i] = keys[i].get();
        }
        return stringKeys;
    }

    public static Set<String> get(Collection<RedisKey> keys) {
        return keys.stream().map(key -> key.get()).collect(Collectors.toSet());
    }

    /*
     override equals and hashCode
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedisKey redisKey = (RedisKey) o;
        return Objects.equals(key, redisKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
