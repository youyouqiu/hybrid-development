package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/7/12 15:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuerySendWorkHourPollMonitorInfoEntity extends BaseEntity {
    @NotNull(message = "页码不能为空")
    private Long page;
    @NotNull(message = "页容量不能为空")
    private Long pageSize;
    private Long defaultSize;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(page);
        args.add(pageSize);
        args.add(defaultSize);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] objects = new Class<?>[3];
        objects[0] = Long.class;
        objects[1] = Long.class;
        objects[2] = Long.class;
        return objects;
    }

    @Override
    public String getExceptionInfo() {
        return "查询用户权限下轮询了工时传感器的监控对象异常";
    }
}
