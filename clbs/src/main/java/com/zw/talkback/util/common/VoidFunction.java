package com.zw.talkback.util.common;

/**
 *  返回值为空的函数通用方法
 */
@FunctionalInterface
public interface VoidFunction {
    void execute() throws Exception;
}
