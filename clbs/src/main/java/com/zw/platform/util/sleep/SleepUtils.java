package com.zw.platform.util.sleep;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/***
 @Author zhengjc
 @Date 2019/7/27 16:04
 @Description 延时等待工具类
 @version 1.0
 **/
public class SleepUtils {
    private static final Logger log = LogManager.getLogger(SleepUtils.class);

    /**
     * @param execution 需要被执行的函数
     * @param time    每次重新执行该函数间隔时间
     * @param times     总共尝试多少次
     * @param <T>       返回的数据类型
     * @return
     */
    public static <T> List<T> waitAndDo(SleepExecution execution, Long time, int times) {
        List<T> result = null;
        for (int i = 0; i < times; i++) {
            try {
                result = execution.execute();
                if (CollectionUtils.isNotEmpty(result)) {
                    return result;
                }
                Thread.sleep(20 * time);
            } catch (InterruptedException e) {
                log.error("休眠异常了", e);
            } catch (Exception e) {
                log.error("执行等待操作程序异常了", e);
            }

        }
        if (CollectionUtils.isEmpty(result)) {
            log.error("执行等待循环操作程序异常了！");
        }
        return result;
    }

    public static <T> List<T> waitAndDo(SleepExecution execution) {
        return waitAndDo(execution, 10L, 10);
    }
}
