package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;

@Data
public class QueryOcrEntity extends BaseEntity {

    private String id;

    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[1];
        objects[0] = id;
        return objects;
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] classes = new Class<?>[1];
        classes[0] = String.class;
        return classes;
    }

    @Override
    public String getExceptionInfo() {
        return "从业人员列表查询异常";
    }
}
