package com.zw.app.entity.monitor;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;


/**
 * @author GJY
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MonitorRunAndStopDataQueryEntity extends BaseEntity {

    private String monitorId;

    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @NotNull(message = "开始时间不能为空")
    private String endTime;

    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[3];
        objects[0] = monitorId;
        objects[1] = startTime;
        objects[2] = endTime;
        return objects;
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] classes = new Class<?>[3];
        classes[0] = String.class;
        classes[1] = String.class;
        classes[2] = String.class;
        return classes;
    }

    @Override
    public String getExceptionInfo() {
        return "获取停止数据历史数据异常";
    }
}
