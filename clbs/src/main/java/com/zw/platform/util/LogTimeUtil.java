package com.zw.platform.util;

import com.zw.platform.commons.SystemHelper;
import com.zw.talkback.util.common.QueryFunction;
import com.zw.talkback.util.common.QueryListNoParamFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @Author: zjc
 * @Description:打印日志工具类
 * @Date: create in 2020/12/8 10:07
 */
public class LogTimeUtil {
    private static Logger log = LogManager.getLogger(LogTimeUtil.class);

    public static <T> T ifSystemUserLogTime(String method, QueryFunction<T> queryFunction) throws Exception {
        long startTime = System.currentTimeMillis();
        T result = queryFunction.execute();
        long endTime = System.currentTimeMillis();
        log(method, startTime, endTime);
        return result;

    }

    public static <T> List<T> ifSystemUserLogTimes(String method, QueryListNoParamFunction<T> queryFunction) {

        long startTime = System.currentTimeMillis();
        List<T> result = queryFunction.execute();
        long endTime = System.currentTimeMillis();
        log(method, startTime, endTime);
        return result;

    }

    private static void log(String method, long startTime, long endTime) {
        String currentUsername = SystemHelper.getCurrentUsername();
        if (currentUsername.equals("system1") || currentUsername.equals("system")) {
            log.info(method + "花费的时间为" + (endTime - startTime));
        }
    }
}
