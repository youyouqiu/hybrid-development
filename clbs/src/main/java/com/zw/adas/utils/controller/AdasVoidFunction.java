package com.zw.adas.utils.controller;

/***
 @Author zhengjc
 @Date 2019/1/13 14:48
 @Description 返回值为空的函数通用方法
 @version 1.0
 **/
@FunctionalInterface
public interface AdasVoidFunction {
    void execute() throws Exception;
}
