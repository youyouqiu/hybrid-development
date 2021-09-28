package com.zw.platform.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThrottlingQueue<K, V> {

    /** The queued item keys */
    private final Object[] keys;

    /** The queued item values */
    private final Map<K, V> values;

    /** items index for next take, poll, peek or remove */
    private int takeIndex;

    /** items index for next put, offer, or add */
    private int putIndex;

    /** Number of elements in the queue */
    private int count;

    /*
     * Concurrency control uses the classic two-condition algorithm
     * found in any textbook.
     */

    /** Main lock guarding all access */
    private final ReentrantLock lock;

    /** Condition for waiting takes */
    private final Condition notEmpty;

    /** Condition for waiting puts */
    private final Condition notFull;

    public ThrottlingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.keys = new Object[capacity];
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.notFull =  lock.newCondition();
        this.values = new HashMap<>(capacity);
    }

    /**
     * Throws NullPointerException if argument is null.
     *
     * @param v the element
     */
    private static void checkNotNull(Object v) {
        if (v == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Inserts element at current put position, advances, and signals.
     * Call only when holding lock.
     */
    private void enqueue(K key, V value) {
        // assert lock.getHoldCount() == 1;
        // assert items[putIndex] == null;
        final Object[] items = this.keys;
        items[putIndex] = key;
        if (++putIndex == items.length) {
            putIndex = 0;
        }
        values.put(key, value);
        count++;
        notEmpty.signal();
    }

    /**
     * Extracts element at current take position, advances, and signals.
     * Call only when holding lock.
     */
    private V dequeue() {
        // assert lock.getHoldCount() == 1;
        // assert items[takeIndex] != null;
        final Object[] items = this.keys;
        @SuppressWarnings("unchecked")
        K key = (K) items[takeIndex];
        items[takeIndex] = null;
        if (++takeIndex == items.length) {
            takeIndex = 0;
        }
        count--;
        notFull.signal();
        return values.remove(key);
    }

    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     */
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return this.count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting
     * up to the specified wait time for space to become available if
     * the queue is full.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(K key, V value, long timeout, TimeUnit unit) throws InterruptedException {
        checkNotNull(key);
        checkNotNull(value);
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            if (values.containsKey(key)) {
                values.put(key, value);
                return true;
            }
            while (count == keys.length) {
                if (nanos <= 0) {
                    // 如果队列已满，则移除头元素，更新尾元素
                    dequeue();
                    enqueue(key, value);
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(key, value);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public V take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0) {
                notEmpty.await();
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }
}
