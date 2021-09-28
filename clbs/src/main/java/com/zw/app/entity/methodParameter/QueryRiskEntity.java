package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryRiskEntity extends BaseEntity {
    private long pageNum;

    private long pageSize;

    private String riskIds;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(pageNum);
        args.add(pageSize);
        args.add(riskIds);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[3];
        argClass[0] = long.class;
        argClass[1] = long.class;
        argClass[2] = String.class;
        return argClass;
    }

    @Override
    public String getExceptionInfo() {
        return "APP查询风险事件异常";
    }

}
