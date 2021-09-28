package com.zw.platform.util.common;

import com.zw.CustomRunner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RunWith(CustomRunner.class)
public class QueuedDelayedEventTriggerTest {
    private static final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    @BeforeClass
    public static void beforeClass() {
        executor.initialize();
    }

    @Test
    public void testAddEvent() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
        final QueuedDelayedEventTrigger trigger = new QueuedDelayedEventTrigger();
        final Field field = QueuedDelayedEventTrigger.class.getDeclaredField("longTaskExecutor");
        field.setAccessible(true);
        field.set(trigger, executor);
        final Map<String, String> map = new ConcurrentHashMap<>();
        trigger.addEvent(5, TimeUnit.SECONDS, () -> map.put("1", "add element in delayed event"));
        trigger.startTrigger();
        Thread.sleep(2000);
        Assert.assertTrue(map.isEmpty());
        Thread.sleep(5000);
        Assert.assertFalse(map.isEmpty());
    }

    @Test
    public void testCancelEvent() throws InterruptedException, IllegalAccessException, NoSuchFieldException {
        final QueuedDelayedEventTrigger trigger = new QueuedDelayedEventTrigger();
        final Field field = QueuedDelayedEventTrigger.class.getDeclaredField("longTaskExecutor");
        field.setAccessible(true);
        field.set(trigger, executor);
        final Map<String, String> map = new ConcurrentHashMap<>();
        trigger.addEvent(5, TimeUnit.SECONDS, () -> map.put("1", "add element in delayed event"), "event#1123");
        trigger.startTrigger();
        trigger.cancelEvent("event#1123");
        Thread.sleep(7000);
        Assert.assertTrue(map.isEmpty());
    }
}