package com.zw.platform.util;

import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.zw.platform.basic.constant.RedisKeyEnum.FAIL_LOGIN;

@Component
public class AccountLocker {
    @Value("${login.fail.max-retries:5}")
    private int maxRetries;

    @Value("${login.fail.lock-time:15}")
    private int lockTime;

    /**
     * 用户是否被锁定
     * @param username 用户名
     * @param currentTime 当前登录时间
     * @return 如果重试超过最大重试次数，距离最后一次失败登录时间间隔小于等于锁定时长，则账号为锁定状态，返回true，反之则返回false
     */
    public boolean isLocked(String username, long currentTime) {
        if (maxRetries <= 0) {
            return false;
        }
        RedisKey key = FAIL_LOGIN.of(username);
        String count = RedisHelper.hget(key, "count");
        if (count == null || Long.parseLong(count) < maxRetries - 1) {
            return false;
        }
        String time = RedisHelper.hget(key, "time");
        //重试超过最大重试次数，距离最后一次失败登录时间间隔小于等于锁定时长
        return time != null && currentTime - Long.parseLong(time) <= Duration.ofMinutes(lockTime).toMillis();
    }

    /**
     * 获取剩余允许重试登录次数
     * @param username 登录失败的用户名
     * @param currentTime 当前登录失败的时间戳
     */
    public int retryCountLeft(String username, long currentTime) {
        RedisKey key = FAIL_LOGIN.of(username);
        Long count = RedisHelper.hincrBy(key, "count", 1L);
        if (count < maxRetries) {
            RedisHelper.addToHash(key, "time", String.valueOf(currentTime));
            return maxRetries - count.intValue();
        }
        return 0;
    }

    /**
     * 获取用户锁定剩余分钟数
     * @param username 登录失败的用户名
     * @param currentTime 当前登录失败的时间戳
     */
    public long lockTimeLeft(String username, long currentTime) {
        RedisKey key = FAIL_LOGIN.of(username);
        RedisHelper.hincrBy(key, "count", 1L);
        String time = RedisHelper.hget(key, "time");
        if (time == null) {
            RedisHelper.addToHash(key, "time", String.valueOf(currentTime));
            return lockTime;
        }
        long lockTimeMillis = Duration.ofMinutes(lockTime).toMillis();
        long delta = System.currentTimeMillis() - Long.parseLong(time);
        if (delta > lockTimeMillis) {
            RedisHelper.addToHash(key, "time", String.valueOf(currentTime));
            return lockTime;
        }
        return Duration.ofMillis(lockTimeMillis - delta).toMinutes();
    }

    /**
     * 重置用户错误登录信息
     * @param username 用户名
     */
    public void reset(String username) {
        RedisHelper.delete(FAIL_LOGIN.of(username));
    }
}
