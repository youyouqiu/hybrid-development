package com.zw.talkback.util.common;

import java.util.List;

/**
 * 功能描述:查询列表的函数定义
 */
@FunctionalInterface
public interface QueryListNoParamFunction<T> {
    List<T> execute();
}
