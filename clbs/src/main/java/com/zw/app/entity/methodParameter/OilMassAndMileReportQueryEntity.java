package com.zw.app.entity.methodParameter;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;


/**
 * @author zhouzongbo on 2019/1/7 18:32
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OilMassAndMileReportQueryEntity extends BaseEntity {

    @NotNull(message = "监控对象ID不能为空")
    private String monitorIds;

    @NotNull(message = "开始时间不能为空")
    private String startTime;

    @NotNull(message = "结束时间不能为空")
    private String endTime;


    @Override
    public Object[] getArgs() {
        Object[] objects = new Object[3];
        objects[0] = this.monitorIds;
        objects[1] = this.startTime;
        objects[2] = this.endTime;
        return objects;
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
        return "查询油量里程报表异常";
    }
}
