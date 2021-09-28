package com.zw.platform.push.config;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

/**
 * Created by jiangxiaoqiang on 2016/10/19.
 */
public class SpringBeanUtil {

    public static ApplicationContext applicationContext;

    static {
        applicationContext = ContextLoader.getCurrentWebApplicationContext();
    }

    /**
     * 取得Bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

}
