package com.zw.platform.util.imports.lock;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * bean工具类，用于静态获取bean
 *
 * @author Zhang Yanhui
 * @since 2020/5/29 15:47
 */

@Component
public class BeanUtil implements BeanFactoryAware {
    private static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory factory) {
        beanFactory = factory;
    }

    /**
     * 根据类型获取bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return beanFactory.getBean(clazz);
    }

}