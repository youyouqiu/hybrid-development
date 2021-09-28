package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class DayRiskEntity extends BaseEntity {

    private int pageNum;

    private int pageSize;

    private String vehicleId;

    private int maxQueryDay = 1;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(this);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[1];
        argClass[0] = DayRiskEntity.class;
        return argClass;
    }

    @Override
    public String getExceptionInfo() {
        return "APP查询风险事件异常";
    }
}
