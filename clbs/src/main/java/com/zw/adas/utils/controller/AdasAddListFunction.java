package com.zw.adas.utils.controller;

import org.elasticsearch.search.SearchHit;

import java.util.List;

@FunctionalInterface
public interface AdasAddListFunction<T> {
    List<T> execute(SearchHit[] hits, String id);
}
