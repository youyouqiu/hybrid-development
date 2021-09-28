package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lijie
 * @version 1.0
 * @date 2019/10/12 15:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryOilMileageEntity extends BaseEntity {
    @NotNull(message = "车辆id不能为空")
    private String vehicleId;

    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @NotNull(message = "结束时间不能为空")
    private String endTime;

    @Override
    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(vehicleId);
        args.add(startTime);
        args.add(endTime);
        return args.toArray();
    }

    @Override
    public Class<?>[] getArgClasses() {
        Class<?>[] objects = new Class<?>[3];
        objects[0] = String.class;
        objects[1] = String.class;
        objects[2] = String.class;
        return objects;
    }

    @Override
    public String getExceptionInfo() {
        return "查询油耗里程统计异常";
    }
}
