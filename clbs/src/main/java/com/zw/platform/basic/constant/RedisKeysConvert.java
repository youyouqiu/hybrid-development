package com.zw.platform.basic.constant;

import com.zw.platform.basic.core.RedisKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Administrator
 */
public interface RedisKeysConvert {
    /**
     * 获取规则
     *
     * @return redis key 生成规则
     */
    String getPattern();

    /**
     * 获取分区
     *
     * @return redis key 所在分区
     */
    int getDbIndex();

    /**
     * RedisKey
     *
     * @param args args
     * @return RedisKey
     */
    default RedisKey of(Object... args) {
        return RedisKey.of(this, args);
    }

    /**
     * RedisKey
     *
     * @param keys
     * @return RedisKey
     */
    default List<RedisKey> ofs(Collection<String> keys) {
        List<RedisKey> redisKeys = new ArrayList<>();
        if (keys == null || keys.size() == 0) {
            return new ArrayList<>();
        }

        for (String key : keys) {
            redisKeys.add(RedisKey.of(this, key));
        }
        return redisKeys;
    }

    /**
     * 默认的build方法
     *
     * @param args 参数
     * @return RedisKey
     */
    default String build(Object... args) {
        return String.format(getPattern(), args);
    }
}
