package com.zw.platform.commons;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HashSetValueMapTest {

    @Test
    public void put() throws InterruptedException {
        HashSetValueMap<String, Integer> map = new HashSetValueMap<>();
        List<Integer> list1 = IntStream.range(0, 1000).boxed().collect(Collectors.toList());
        List<Integer> list2 = IntStream.range(1000, 2000).boxed().collect(Collectors.toList());
        final ExecutorService executorService = Executors.newFixedThreadPool(8);
        AtomicInteger count = new AtomicInteger(0);
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            executorService.submit(() -> {
                map.put("1", finalI);
                count(count);
            });
        }
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                map.putAll("1", list1);
                count(count);
            });
        }
        for (int i = 0; i < 1000; i++) {
            int finalI = 1000 + i;
            executorService.submit(() -> {
                map.put("2", finalI);
                count(count);
            });
        }
        for (int i = 0; i < 100; i++) {
            executorService.submit(() -> {
                map.putAll("2", list2);
                count(count);
            });
        }
        for (int i = 0; i < 500; i++) {
            int finalI = i;
            executorService.submit(() -> {
                map.remove("1", finalI);
                count(count);
            });
        }
        for (int i = 0; i < 500; i++) {
            int finalI = 1000 + i;
            executorService.submit(() -> {
                map.remove("2", finalI);
                count(count);
            });
        }
        for (int i = 0; i < 500; i++) {
            executorService.submit(() -> {
                map.removeKey("1");
                count(count);
            });
        }
        for (int i = 0; i < 500; i++) {
            executorService.submit(() -> {
                map.removeKey("2");
                count(count);
            });
        }
        synchronized (this) {
            wait();
        }
        final Set<Integer> valuesFor1 = map.getValues("1");
        final Set<Integer> valuesFor2 = map.getValues("2");
        final Set<Integer> values = map.values();
        assertEquals(valuesFor1.size() + valuesFor2.size(), values.size());
        for (Integer value : values) {
            if (value < 1000) {
                assertTrue("value: " + value, valuesFor1.contains(value));
                continue;
            }
            assertTrue("value: " + value, valuesFor2.contains(value));
        }

        for (Integer value : valuesFor1) {
            assertTrue("valueFor1: " + value, values.contains(value));
        }

        for (Integer value : valuesFor2) {
            assertTrue("valueFor2: " + value, values.contains(value));
        }

        map.removeKey("1");
        assertFalse(map.containsKey("1"));

        map.removeKey("2");
        assertFalse(map.containsKey("2"));
    }

    private void count(AtomicInteger count) {
        final int value = count.incrementAndGet();
        if (value == 4200) {
            synchronized (this) {
                notify();
            }
        }
    }
}
