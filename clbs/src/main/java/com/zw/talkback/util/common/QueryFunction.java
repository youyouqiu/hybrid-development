package com.zw.talkback.util.common;

/*
 * 功能描述:查询接口定义
 */
@FunctionalInterface
public interface QueryFunction<T> {
    T execute() throws Exception;
}
