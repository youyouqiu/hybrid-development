package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class QueryMediaEntity extends BaseEntity {

    private String riskId;

    private int mediaType;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(riskId);
        args.add(mediaType);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[2];
        argClass[0] = (String.class);
        argClass[1] = (int.class);
        return argClass;
    }

    @Override
    public String getExceptionInfo() {
        return "APP查询风险多媒体列表异常";
    }

}
