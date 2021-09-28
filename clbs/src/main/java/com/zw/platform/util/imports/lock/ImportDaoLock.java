package com.zw.platform.util.imports.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 导入锁, 主要用于Dao层方法上.
 * @author create by zhouzongbo on 2020/8/28.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ImportDaoLock {

    /**
     * 导入模块
     * 默认锁CONFIG
     * @return ImportModule
     */
    ImportTable value() default ImportTable.ZW_M_CONFIG;
}
