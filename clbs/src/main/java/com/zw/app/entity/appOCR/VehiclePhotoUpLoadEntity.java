package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
public class VehiclePhotoUpLoadEntity extends BaseEntity {
    /**
     * 监控对象id
     */
    @NotNull(message = "监控对象id不能为空")
    private String monitorId;

    /**
     * 车辆照片(存储路径)
     */
    @NotNull(message = "车辆照片不能为空")
    private String vehiclePhoto;

    /**
     * 旧的车辆照片存储路径
     */
    private String oldVehiclePhoto;

    /**
     * 数据修改时间
     */
    private Date updateDataTime = new Date();

    /**
     * 修改者username
     */
    private String updateDataUsername;

    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(this);
        return args.toArray();
    }

    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[1];
        argClass[0] = VehiclePhotoUpLoadEntity.class;
        return argClass;
    }

    @Override
    public String getExceptionInfo() {
        return "APP上传车辆照片异常";
    }
}
