package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryOcrProfessionalsEntity extends BaseEntity {

    private String info;
    private String vehicleId;
    private String oldPhoto;
    private Integer type;

    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[4];
        objects[0] = info;
        objects[1] = vehicleId;
        objects[2] = oldPhoto;
        objects[3] = type;
        return objects;
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] classes = new Class<?>[4];
        classes[0] = String.class;
        classes[1] = String.class;
        classes[2] = String.class;
        classes[3] = Integer.class;
        return classes;
    }

    @Override
    public String getExceptionInfo() {
        return "从业人员OCR存储信息异常";
    }
}
