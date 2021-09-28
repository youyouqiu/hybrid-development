package com.zw.platform.commons;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public final class HashSetValueMap<K, V> {
    private final Map<K, Set<V>> map = new ConcurrentHashMap<>();
    private final Map<V, Set<K>> reverseMap = new ConcurrentHashMap<>();

    /**
     * 添加映射关系
     * @return true - 添加之前map中不存在该value; false - 添加之前map中已经存在该value
     */
    public synchronized boolean put(K key, V value) {
        this.map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(value);
        //lambda表达式中必须使用final变量，所以在这里用数组对boolean进行包装
        boolean[] valueNotExists = new boolean[] { false };
        this.reverseMap.computeIfAbsent(value, k -> {
            valueNotExists[0] = true;
            return ConcurrentHashMap.newKeySet();
        }).add(key);
        return valueNotExists[0];
    }

    public synchronized boolean putAll(K key, Collection<V> values) {
        final boolean added = this.map.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).addAll(values);
        if (!added) {
            return false;
        }
        for (V value : values) {
            this.reverseMap.computeIfAbsent(value, k -> ConcurrentHashMap.newKeySet()).add(key);
        }
        return true;
    }

    public Set<V> getValues(K key) {
        return this.map.getOrDefault(key, Collections.emptySet());
    }

    public Set<K> getKeys(V value) {
        return this.reverseMap.getOrDefault(value, Collections.emptySet());
    }

    private BiFunction<V, Set<K>, Set<K>> removeKeyFunction(K key) {
        return (k, v) -> v.remove(key) && v.isEmpty() ? null : v;
    }

    private BiFunction<K, Set<V>, Set<V>> removeValueFunction(V value) {
        return (k, v) -> v.remove(value) && v.isEmpty() ? null : v;
    }

    /**
     * 删除指定的映射关系
     * @return true - 删除映射关系后，map中不存在value; false - 删除映射关系后，map存在value
     */
    public synchronized boolean remove(K key, V value) {
        this.map.computeIfPresent(key, removeValueFunction(value));
        return null == this.reverseMap.computeIfPresent(value, removeKeyFunction(key));
    }

    public synchronized void removeAll(K key, Collection<V> values) {
        this.map.computeIfPresent(key, (k, v) -> v.removeAll(values) && v.isEmpty() ? null : v);
        for (V value : values) {
            this.reverseMap.computeIfPresent(value, removeKeyFunction(key));
        }
    }

    /**
     * 删除指定key对应的映射关系
     * @return 删除映射关系后，map中不再存在的value值集合
     */
    public synchronized Set<V> removeKey(K key) {
        final Set<V> values = this.map.remove(key);
        if (values == null) {
            return Collections.emptySet();
        }
        final Set<V> valueSet = new HashSet<>(values.size());
        for (V value : values) {
            if (this.reverseMap.computeIfPresent(value, removeKeyFunction(key)) == null) {
                valueSet.add(value);
            }
        }
        return valueSet;
    }

    public synchronized Set<K> removeValue(V value) {
        final Set<K> keys = this.reverseMap.remove(value);
        if (keys == null) {
            return Collections.emptySet();
        }
        for (K key : keys) {
            this.map.computeIfPresent(key, removeValueFunction(value));
        }
        return keys;
    }

    public boolean containsKey(K key) {
        return this.map.containsKey(key);
    }

    public boolean containsValue(V value) {
        return this.reverseMap.containsKey(value);
    }

    public int size() {
        return this.reverseMap.size();
    }

    public Set<K> keySet() {
        return this.map.keySet();
    }

    public Set<V> values() {
        return this.reverseMap.keySet();
    }
}
