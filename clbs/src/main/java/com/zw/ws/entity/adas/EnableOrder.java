package com.zw.ws.entity.adas;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: 用来标记中位标准使能顺序
 * @Author zhangqiang
 * @Date 2020/5/8 14:27
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface EnableOrder {

    /**
     * 使能顺序
     * @return
     */
    int enableIndex() default -1;

    /**
     * 辅助多媒体使能顺序
     * @return
     */
    int auxiliaryEnableIndex() default -1;

    /**
     * 事件id
     * @return
     */
    String functionId() default "";

    /**
     * 要设置的参数
     * @return
     */
    String value() default "";
}
