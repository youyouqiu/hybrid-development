package com.zw.platform.commons;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ParallelWorker {
    public static <T> void invoke(List<T> data, int batchSize, Consumer<List<T>> consumer) {
        int parallelism = computeParallelism(batchSize, data.size());
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            pool.invoke(new BatchAction<>(data, batchSize, consumer));
        } finally {
            pool.shutdown();
        }
    }

    public static <T> ForkJoinTask<?> invokeAsync(List<T> data, int batchSize, Consumer<List<T>> consumer) {
        int parallelism = computeParallelism(batchSize, data.size());
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            return pool.submit(() -> Lists.partition(data, batchSize).parallelStream().forEach(consumer));
        } finally {
            pool.shutdown();
        }
    }

    public static <T, R> R invokeTask(List<T> data, int batchSize, RecursiveTask<R> task) {
        int parallelism = computeParallelism(batchSize, data.size());
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            return pool.invoke(task);
        } finally {
            pool.shutdown();
        }
    }

    /**
     * 计算并行度，最大值为100，限制最多100个线程
     */
    private static int computeParallelism(int batchSize, int totalSize) {
        return Math.min(totalSize % batchSize > 0 ? totalSize / batchSize + 1 : totalSize / batchSize, 100);
    }

    private static class BatchAction<T> extends RecursiveAction {
        private final List<T> list;
        private final int batchSize;
        private final Consumer<List<T>> consumer;

        BatchAction(List<T> list, int batchSize, Consumer<List<T>> consumer) {
            this.list = list;
            this.batchSize = batchSize;
            this.consumer = consumer;
        }

        @Override
        protected void compute() {
            if (list.size() <= batchSize) {
                consumer.accept(list);
                return;
            }
            final List<BatchAction<T>> tasks = Lists.partition(list, batchSize).stream()
                .map(data -> new BatchAction<>(data, batchSize, consumer))
                .collect(Collectors.toList());

            invokeAll(tasks);
        }
    }
}
