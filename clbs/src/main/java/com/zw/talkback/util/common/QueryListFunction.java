package com.zw.talkback.util.common;

import java.util.List;

/**
 * 功能描述:查询列表的函数定义
 */
@FunctionalInterface
public interface QueryListFunction<T> {
    List<T> execute(List<T> datas, String simpleQueryParam);
}
