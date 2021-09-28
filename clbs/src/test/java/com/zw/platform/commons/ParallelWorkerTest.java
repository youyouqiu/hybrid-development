package com.zw.platform.commons;

import com.google.common.collect.Lists;
import com.zw.CustomRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@RunWith(CustomRunner.class)
public class ParallelWorkerTest {
    private static List<Integer> nums;

    @BeforeClass
    public static void setUp() {
        nums = IntStream.range(0, 100)
            .boxed()
            .collect(Collectors.toList());
    }

    @Test
    public void invoke() {
        final AtomicInteger total = new AtomicInteger(0);
        ParallelWorker.invoke(nums, 10,
            list -> total.addAndGet(list.stream().reduce(0, Integer::sum)));
        assertEquals(4950, total.get());
    }

    @Test
    public void invokeAsync() {
        final AtomicInteger total = new AtomicInteger(0);
        final ForkJoinTask<?> task = ParallelWorker.invokeAsync(nums, 10,
            list -> total.addAndGet(list.stream().reduce(0, Integer::sum)));
        try {
            task.get();
            assertEquals(4950, total.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void invokeTask() {
        int total = ParallelWorker.invokeTask(nums, 10, new BatchTask(nums, 10));
        assertEquals(4950, total);
    }

    private static class BatchTask extends RecursiveTask<Integer> {
        private final List<Integer> list;
        private final int batchSize;

        BatchTask(List<Integer> list, int batchSize) {
            this.list = list;
            this.batchSize = batchSize;
        }

        @Override
        protected Integer compute() {
            if (list.size() <= batchSize) {
                return list.stream().reduce(0, Integer::sum);
            }
            final List<BatchTask> tasks = Lists.partition(list, batchSize).parallelStream()
                .map(subList -> new BatchTask(subList, batchSize))
                .collect(Collectors.toList());
            return invokeAll(tasks).stream()
                .map(ForkJoinTask::join)
                .reduce(0, Integer::sum);
        }
    }
}
