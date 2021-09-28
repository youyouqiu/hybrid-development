package com.zw.platform.util.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 全局方法耗时记录
 * <p>为避免日志太多，本功能只允许生效一段时间（过期时间为 RECORD_UNTIL）
 *
 * @author ZhangYanhui
 */
public final class GlobalRtLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalRtLogger.class);
    /**
     * 线程内traceId
     */
    private static final ThreadLocal<String> TRACE_ID =
            ThreadLocal.withInitial(() -> UUID.randomUUID().toString().replaceAll("-", ""));
    /**
     * 记录方法耗时的截止时间
     */
    private static final AtomicLong RECORD_UNTIL = new AtomicLong(0L);

    /**
     * 上次System.currentTimeMillis()的结果，仅用于判断RECORD_UNTIL是否小于当前时间戳
     * <p>假设时间不会回拨，此值可减少不必要的System.currentTimeMillis()调用
     * <p>不需要线程安全
     */
    private static long latestTimestamp = 1L;
    /**
     * 慢接口门槛，ms
     */
    private static final long SLOW_RT = 500L;

    /**
     * 开始记录方法耗时，直到duration毫秒后结束
     * <p>传0意味着立即结束
     *
     * @param duration 持续时间，单位ms
     */
    public static void setStopwatch(long duration) {
        final long previous = RECORD_UNTIL.get();
        final long recordUntil = System.currentTimeMillis() + duration;
        RECORD_UNTIL.set(recordUntil);
        // 此处旧值仅作参考，恕不保证原子性
        LOGGER.info("方法耗时设为在{}时刻停止记录，原为{}", recordUntil, previous);
    }

    /**
     * 方法耗时记录(清除traceId，适用于controller、consumer等)
     */
    public Object apiRtRecorder(ProceedingJoinPoint pjp) throws Throwable {
        return record(pjp, true);
    }

    /**
     * 方法耗时记录(不清除traceId)
     */
    public Object serviceRtRecorder(ProceedingJoinPoint pjp) throws Throwable {
        return record(pjp, false);
    }


    /**
     * 方法耗时记录
     * <p>因入参/出参可能过大，这里不打印，有需要请用Arthas
     */
    private Object record(ProceedingJoinPoint pjp, boolean clearTraceId) throws Throwable {
        Object[] args = pjp.getArgs();

        if (RECORD_UNTIL.get() < latestTimestamp) {
            // 当前不跟踪时，直接返回
            return pjp.proceed(args);
        }

        final long begin = System.currentTimeMillis();
        latestTimestamp = begin;
        final String traceId = TRACE_ID.get();
        try {
            return pjp.proceed(args);
        } finally {
            if (clearTraceId) {
                TRACE_ID.remove();
            }
            final long end = System.currentTimeMillis();
            final long cost = end - begin;
            final String className = pjp.getTarget().getClass().getSimpleName();
            final String methodName = pjp.getSignature().getName();
            final String suggest = cost > SLOW_RT ? "，有点慢建议配合Arthas优化" : "";
            LOGGER.info("方法耗时：{}ms，traceId：[{}], 方法名：[{}.{}]，参数个数：[{}]{}",
                    cost, traceId, className, methodName, args.length, suggest);
        }
    }
}