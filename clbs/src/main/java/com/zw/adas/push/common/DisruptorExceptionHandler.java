package com.zw.adas.push.common;

import com.lmax.disruptor.ExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * disruptor的异常处理类
 * 2020/8/21 14:49
 *
 * @author lijie
 * @version 1.0
 **/
public class DisruptorExceptionHandler implements ExceptionHandler<Object> {

    private static final Logger log = LogManager.getLogger(DisruptorExceptionHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        log.error("disruptor 消费数据时发生异常， 数据为：{}", event, ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("disruptor 启动消费者时发生异常----错误信息为：" + ex.getMessage());
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("disruptor 关闭消费者时发生异常----错误信息为：" + ex.getMessage());
    }
}
