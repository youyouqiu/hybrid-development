package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DealRiskEntity extends BaseEntity {

    private String riskId;
    private Integer riskResult;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(riskId);
        args.add(riskResult);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[2];
        argClass[0] = (String.class);
        argClass[1] = (Integer.class);
        return argClass;
    }

    @Override
    public String getExceptionInfo() {
        return "APP处理风险异常";
    }

}
