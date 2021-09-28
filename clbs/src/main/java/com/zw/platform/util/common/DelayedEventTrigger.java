package com.zw.platform.util.common;

import java.util.concurrent.TimeUnit;

/**
 * 延时事件触发器
 *
 * @author Zhang Yanhui
 * @since 2020/6/12 17:06
 */

public interface DelayedEventTrigger {

    /**
     * 增加延时事件
     *
     * @param delayTime 延时时间
     * @param timeUnit  时间单位
     * @param event    事件内容
     */
    void addEvent(long delayTime, TimeUnit timeUnit, Runnable event);

    /**
     * 增加延时事件
     *
     * @param delayTime 延时时间
     * @param timeUnit  时间单位
     * @param event    事件内容
     * @param key      事件标记，可用于取消
     */
    void addEvent(long delayTime, TimeUnit timeUnit, Runnable event, String key);

    /**
     * 取消延时事件
     * <p> 仅可取消还未到期的事件
     *
     * @param key 事件标记
     */
    void cancelEvent(String key);
}
