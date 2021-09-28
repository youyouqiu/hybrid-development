package com.zw.talkback.util;

import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;

/***
 @Author zhengjc
 @Date 2019/8/1 17:30
 @Description 个呼和组呼号码Redis工具类
 @version 1.0
 **/
public class CallNumberUtil {
    private static final Logger logger = LoggerFactory.getLogger(CallNumberUtil.class);
    private static final RedisKey PERSON_CALL_NUMBER = HistoryRedisKeyEnum.PERSON_CALL_NUMBER.of();
    private static final RedisKey GROUP_CALL_NUMBER = HistoryRedisKeyEnum.GROUP_CALL_NUMBER.of();

    /**
     * 释放个呼号码
     * @return
     */
    public static String popPersonCallNumber() throws BusinessException {
        return popCallNumber(PERSON_CALL_NUMBER);
    }

    /**
     * 批量释放个呼号码
     * @return
     */
    public static Set<String> popLengthPersonCallNumber(int len) throws BusinessException {
        return getLengthCallNumber(PERSON_CALL_NUMBER, len);
    }

    /**
     * 释放组呼号码
     * @return
     */
    public static String popGroupCallNumber() throws BusinessException {
        return popCallNumber(GROUP_CALL_NUMBER);
    }

    /**
     * 回收个呼号码
     * @param cardNumber
     * @return
     */
    public static void recyclePersonCallNumber(String... cardNumber) {
        addCallNumber(PERSON_CALL_NUMBER, cardNumber);
    }

    /**
     * 回收组呼号码
     * @param cardNumber
     * @return
     */
    public static void recycleGroupCallNumber(String... cardNumber) {
        addCallNumber(GROUP_CALL_NUMBER, cardNumber);
    }

    private static Set<String> getLengthCallNumber(RedisKey callKey, int len) throws BusinessException {
        Set<String> callNumbers;
        long remainNumber = RedisHelper.getSetLen(callKey);
        if (remainNumber < len) {
            throw new BusinessException(callKey + "号码不够用了");
        }
        // 这里为什么随机删除并返回len个元素呢？
        callNumbers = RedisHelper.setPop(callKey, len);
        return callNumbers;
    }

    private static String popCallNumber(RedisKey callKey) throws BusinessException {
        String callNumber = RedisHelper.setPop(callKey);
        if (StrUtil.isBlank(callNumber)) {
            throw new BusinessException(callKey + "号码用尽了");
        }
        return callNumber;
    }

    private static void addCallNumber(RedisKey callKey, String... cardNumber) {
        RedisHelper.addToSet(callKey, Arrays.asList(cardNumber));
    }
}
