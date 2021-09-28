package com.zw.platform.util.common;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 防止表单重复提交的token注解
 *
 * @author wanxin
 */
@Target({java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AvoidRepeatSubmitToken {
    /**
     * 在弹出增加页面的controller方法上,将setToken置为true,removeToken为默认值
     *
     * @return 是否成功
     */
    @Deprecated
    boolean setToken() default false;

    /**
     * 提交到后台的controller方法上,将removeToken置为true,setToken为默认值 改为：前端根据参数生成hash，后端根据该标记进行判断,是否进行表单重复校验
     *
     * @return 是否成功
     */
    boolean removeToken() default false;
}