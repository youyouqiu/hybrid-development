package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class QueryEventsEntity extends BaseEntity {

    private String riskId;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(riskId);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[1];
        argClass[0] = (String.class);
        return argClass;
    }

    @Override
    public String getExceptionInfo() {
        return "APP查询报警风险列表异常";
    }

}
