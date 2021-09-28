package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/19 15:49
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JudgeIsBingObdSensorEntity extends BaseEntity {
    @NotNull(message = "监控对象ID不能为空")
    private String monitorId;

    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[1];
        objects[0] = this.monitorId;
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
        return "判断监控对象是否绑定obd传感器错误";
    }
}
