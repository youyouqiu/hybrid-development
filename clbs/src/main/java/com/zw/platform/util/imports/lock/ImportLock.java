package com.zw.platform.util.imports.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 导入锁, 主要用于方法上.
 * @author create by zhouzongbo on 2020/8/28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ImportLock {

    /**
     * 导入模块
     * 默认锁CONFIG
     * @return ImportModule
     */
    ImportModule value() default ImportModule.CONFIG;
}
