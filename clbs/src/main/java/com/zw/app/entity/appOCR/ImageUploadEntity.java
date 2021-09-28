package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * @author zhouzongbo on 2019/7/8 16:24
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ImageUploadEntity extends BaseEntity {

    @NotNull(message = "图片不能为空")
    private String decodeImage;

    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[1];
        objects[0] = this.decodeImage;
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
        return "图片上传异常";
    }
}
