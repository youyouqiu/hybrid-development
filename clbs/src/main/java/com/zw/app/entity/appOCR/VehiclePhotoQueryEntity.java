package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = false)
public class VehiclePhotoQueryEntity extends BaseEntity {
    /**
     * 监控对象id
     */
    @NotNull(message = "监控对象id不能为空")
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
        return "查询监控对象-车的照片信息异常";
    }
}
