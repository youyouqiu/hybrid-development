package com.zw.platform.domain.redis;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class RedisImportQueue<E> {
    private static int LIST_SIZE = 250;
    private Deque<Set<E>> queue;

    public RedisImportQueue() {
        queue = new LinkedList<>();
    }

    public boolean add(E e) {
        if (queue.isEmpty()) {
            initNewSet();
            return queue.peekLast().add(e);
        }
        Set<E> set = queue.peekLast();
        if (set.size() == LIST_SIZE) {
            initNewSet();
            return queue.peekLast().add(e);
        }
        return set.add(e);
    }

    private void initNewSet() {
        queue.add(new HashSet<>(LIST_SIZE));
    }

    public Set<E> poll() {
        return queue.poll();
    }

    public int size() {
        return queue.size();
    }
}
