package com.zw.platform.util.sleep;

import java.util.List;

/***
 @Author zhengjc
 @Date 2019/7/27 16:06
 @Description 进行睡眠等待所做的操作函数
 @version 1.0
 **/
@FunctionalInterface
public interface SleepExecution<T> {
    List<T> execute();
}
