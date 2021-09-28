package com.zw.adas.utils.controller;

import java.util.List;

@FunctionalInterface
public interface AdasQueryListFunction<T> {
    List<T> execute(List<T> datas, String simpleQueryParam);
}
