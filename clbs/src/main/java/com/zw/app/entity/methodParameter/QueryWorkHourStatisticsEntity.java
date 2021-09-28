package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/7/12 15:17
 */
@Data
public class QueryWorkHourStatisticsEntity extends BaseEntity {
    @NotNull(message = "监控对象不能为空")
    private String monitorIds;
    @NotNull(message = "开始时间不能为空")
    private String startTime;
    @NotNull(message = "结束时间为空")
    private String endTime;
    private Integer sensorNo;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(monitorIds);
        args.add(startTime);
        args.add(endTime);
        args.add(sensorNo);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] objects = new Class<?>[4];
        objects[3] = Integer.class;
        objects[0] = String.class;
        objects[1] = String.class;
        objects[2] = String.class;
        return objects;
    }

    @Override
    public String getExceptionInfo() {
        return "查询工时统计异常异常";
    }
}
