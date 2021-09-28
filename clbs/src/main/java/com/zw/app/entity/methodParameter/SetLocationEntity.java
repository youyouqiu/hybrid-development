package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author lijie
 * @version 1.0
 * @date 2020/11/9 14:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SetLocationEntity extends BaseEntity {
    @NotNull(message = "位置信息不能为空")
    private String locationInfo;

    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[1];
        objects[0] = this.locationInfo;
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
        return "组装监控对象基础位置信息异常";
    }
}
