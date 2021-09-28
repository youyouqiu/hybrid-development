package com.zw.platform.push.common;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class WsSessionManagerTest {

    @Test
    public void addAndRemove() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.addPositions("s1", Sets.newHashSet("d1"));
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.addPositions("s2", Sets.newHashSet("d1"));
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.removeBySession("s1");
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.removeBySession("s2");
            count(count);
        });

        executorService.submit(() -> {
            WsSessionManager.INSTANCE.addPositions("s3", Sets.newHashSet("d1"));
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.addPositions("s4", Sets.newHashSet("d1"));
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.removeBySession("s3");
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.removeBySession("s4");
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.addPositions("s5", Sets.newHashSet("d1"));
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.removeBySession("s5");
            count(count);
        });

        executorService.submit(() -> {
            WsSessionManager.INSTANCE.addPositions("s6", Sets.newHashSet("d1"));
            count(count);
        });
        executorService.submit(() -> {
            WsSessionManager.INSTANCE.removeBySession("s6");
            count(count);
        });

        synchronized (this) {
            wait();
        }
        executorService.shutdown();
        assertTrue(WsSessionManager.INSTANCE.getAllPositions().isEmpty());

        WsSessionManager.INSTANCE.addPositions("s1", Sets.newHashSet("d1"));
        WsSessionManager.INSTANCE.addPositions("s2", Sets.newHashSet("d1"));
        assertTrue(WsSessionManager.INSTANCE.getAllPositions().contains("d1"));

        WsSessionManager.INSTANCE.removeBySession("s1");
        assertTrue(WsSessionManager.INSTANCE.getAllPositions().contains("d1"));

        WsSessionManager.INSTANCE.removeBySession("s2");
        assertTrue(WsSessionManager.INSTANCE.getAllPositions().isEmpty());
    }

    private void count(AtomicInteger count) {
        final int value = count.incrementAndGet();
        if (value == 12) {
            synchronized (this) {
                notify();
            }
        }
    }
}
