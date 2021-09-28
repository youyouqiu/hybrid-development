package com.zw.adas.utils.controller;

import java.util.List;

@FunctionalInterface
public interface AdasQueryListNoParamFunction<T> {
    List<T> execute();
}
