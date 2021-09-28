package com.zw.platform.util.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 延时任务触发器
 *
 * @author Zhang Yanhui
 * @since 2020/5/28 13:58
 */

@Slf4j
@Component
public class QueuedDelayedEventTrigger implements DelayedEventTrigger {
    @Autowired
    private ThreadPoolTaskExecutor longTaskExecutor;
    private static final AtomicBoolean RUN = new AtomicBoolean(true);
    private static final Object LOCK = new Object();
    private static final int INITIAL_CAPACITY = 1 << 13;
    private static final PriorityBlockingQueue<Event> QUEUE =
            new PriorityBlockingQueue<>(INITIAL_CAPACITY, Comparator.comparingLong(Event::getFireTime));

    @PostConstruct
    public void startTrigger() {
        longTaskExecutor.execute(() -> {
            Thread.currentThread().setName("delayed-message-trigger");
            final PriorityBlockingQueue<Event> queue = QUEUE;
            Event event;
            Event peek;
            while (RUN.get()) {
                try {
                    event = queue.take();
                } catch (InterruptedException e) {
                    continue;
                }
                final long fireTime = event.getFireTime();
                final long now = System.currentTimeMillis();
                if (fireTime > now) {
                    queue.offer(event);
                    synchronized (LOCK) {
                        // 检查在queue.take()之后加锁之前，是否有人往队列放了元素
                        peek = queue.peek();
                        if (null == peek || peek.getFireTime() >= event.getFireTime()) {
                            try {
                                LOCK.wait(fireTime - now);
                            } catch (InterruptedException ignored) {
                                // 等待下次检查
                            }
                        }
                    }
                } else {
                    try {
                        event.getEvent().run();
                    } catch (Exception e) {
                        log.error(String.valueOf(e));
                    }
                }
            }
        });
    }

    public void wakeup() {
        synchronized (LOCK) {
            LOCK.notify();
        }
    }

    @PreDestroy
    public void clean() {
        RUN.set(false);
        wakeup();
    }

    @Override
    public void addEvent(long delayTime, TimeUnit timeUnit, Runnable event) {
        addEvent(delayTime, timeUnit, event, UUID.randomUUID().toString());
    }

    @Override
    public void addEvent(long delayTime, TimeUnit timeUnit, Runnable event, String key) {
        final PriorityBlockingQueue<Event> queue = QUEUE;
        long fireTime = System.currentTimeMillis() + timeUnit.toMillis(delayTime);
        queue.offer(new Event(fireTime, event, key));
        Optional.ofNullable(queue.peek())
                .filter(earliest -> earliest.getFireTime() == fireTime)
                .ifPresent(o -> {
                    synchronized (LOCK) {
                        LOCK.notify();
                    }
                });
    }

    @Override
    public void cancelEvent(String key) {
        Assert.notNull(key, "事件key不能为空");
        QUEUE.remove(new Event(0, null, key));
    }

    @Getter
    @AllArgsConstructor
    public static class Event {
        private final long fireTime;
        private final Runnable event;
        private final String key;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Event event = (Event) o;
            return key.equals(event.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }
}

