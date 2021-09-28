package com.zw.platform.basic.util;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * redis业务相关的工具类（替换的时候将自己的方法写到这里面，避免有交叉（等待后续全部替换完成，可以移动出来））
 * @author zjc
 */
public class RedisServiceUtils {
    private static final Logger log = LoggerFactory.getLogger(RedisServiceUtils.class);

    /**
     * 存储风控客服登录信息
     */
    public static void storeCustomerLoginState(String userName) {
        RedisHelper.setString(HistoryRedisKeyEnum.ADAS_REPORT_CUSTOMER_SERVICE.of(userName), userName);
    }

    /**
     * 删除风控客服登录信息
     */
    public static void delCustomerLoginState(String userName) {
        RedisHelper.delete(HistoryRedisKeyEnum.ADAS_REPORT_CUSTOMER_SERVICE.of(userName));
    }

    /**
     * 主动安全处理风险锁
     */
    public static boolean lockRisk(String riskId) {
        int timeOut = 2 * 60;
        RedisKey lockKey = HistoryRedisKeyEnum.RISK_LOCK.of(riskId);
        return RedisHelper.setStringNxAndExpire(lockKey, riskId, timeOut);
    }

    /**
     * 释放主动安全风险处理锁
     */
    public static synchronized void releaseRiskLock(String riskId) {
        RedisKey lockKey = HistoryRedisKeyEnum.RISK_LOCK.of(riskId);
        String value = RedisHelper.getString(lockKey);
        if (StringUtils.isNotBlank(value)) {
            RedisHelper.delete(lockKey);
        }
    }

    /**
     * 处理（批量）锁
     */
    public static synchronized String[] lockDealRisks(String[] riskIdArray) {
        List<String> riskIds = new ArrayList<>(Arrays.asList(riskIdArray));
        List<RedisKey> redisKeys = riskIds.stream()
                .map(HistoryRedisKeyEnum.RISK_LOCK::of)
                .collect(Collectors.toList());
        //默认设置两分钟失效
        int timeOut = 2 * 60;
        try {
            List<String> lockRiskIds = RedisHelper.batchGetString(redisKeys);
            if (CollectionUtils.isNotEmpty(lockRiskIds)) {
                riskIds.removeAll(lockRiskIds);
            }
            redisKeys = riskIds.stream()
                    .map(HistoryRedisKeyEnum.RISK_LOCK::of)
                    .collect(Collectors.toList());
            RedisHelper.expireKeys(redisKeys, timeOut);

        } catch (Exception e) {
            log.error("风险锁定失败", e);
        }
        return riskIds.toArray(new String[] {});

    }

    /**
     * 释放处理（批量）锁
     */
    public static synchronized void releaseDealRisksLock(String[] lockKeys) {
        List<RedisKey> redisKeys = Arrays.stream(lockKeys)
                .map(HistoryRedisKeyEnum.RISK_LOCK::of)
                .collect(Collectors.toList());
        try {
            RedisHelper.delete(redisKeys);
        } catch (Exception e) {
            log.error("风险解锁失败", e);
        }
    }
}
