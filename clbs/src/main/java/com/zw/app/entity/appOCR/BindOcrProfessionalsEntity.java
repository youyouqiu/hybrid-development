package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;

@Data
public class BindOcrProfessionalsEntity extends BaseEntity {

    private String newId;

    private String vehicleId;

    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[2];
        objects[0] = newId;
        objects[1] = vehicleId;
        return objects;
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] classes = new Class<?>[2];
        classes[0] = String.class;
        classes[1] = String.class;
        return classes;
    }

    @Override
    public String getExceptionInfo() {
        return "从业人员绑定异常";
    }
}
