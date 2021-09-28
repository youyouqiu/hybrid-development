package com.zw.platform.util.common;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Chen Feng
 * @version 1.0 2019/1/22
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E> {
    private final Map<E, Boolean> map = new ConcurrentHashMap<>();
    private transient Set<E> keys;

    public ConcurrentHashSet() {
        this.keys = this.map.keySet();
    }

    public boolean add(E e) {
        return this.map.put(e, Boolean.TRUE) == null;
    }

    public void clear() {
        this.map.clear();
    }

    public boolean contains(Object o) {
        return this.map.containsKey(o);
    }

    public boolean containsAll(Collection<?> c) {
        return this.keys.containsAll(c);
    }

    public boolean equals(Object o) {
        return o == this || this.keys.equals(o);
    }

    public int hashCode() {
        return this.keys.hashCode();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public Iterator<E> iterator() {
        return this.keys.iterator();
    }

    public boolean remove(Object o) {
        return this.map.remove(o) != null;
    }

    public boolean removeAll(Collection<?> c) {
        return this.keys.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return this.keys.retainAll(c);
    }

    public int size() {
        return this.map.size();
    }

    public Object[] toArray() {
        return this.keys.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return this.keys.toArray(a);
    }

    public String toString() {
        return this.keys.toString();
    }
}
