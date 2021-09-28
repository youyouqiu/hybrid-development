package com.zw.adas.utils.controller;

@FunctionalInterface
public interface AdasQueryFunction<T> {
    T execute();
}
