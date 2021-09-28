package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/5/20 14:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryLocationEntity extends BaseEntity {
    @NotNull(message = "监控对象ID不能为空")
    private String id;

    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[1];
        objects[0] = this.id;
        return objects;
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] objects = new Class<?>[1];
        objects[0] = String.class;
        return objects;
    }

    @Override
    public String getExceptionInfo() {
        return "获取监控对象基础位置信息异常";
    }
}
