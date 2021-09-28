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
public class VehicleTransportInfoUploadEntity extends BaseEntity {
    /**
     * 车辆id
     */
    @NotNull(message = "监控对象id不能为空")
    private String monitorId;

    /**
     * 运输证号
     */
    private String transportNumber;

    /**
     * 运输证图片(存储路径)
     */
    @NotNull(message = "运输证图片不能为空")
    private String transportNumberPhoto;

    /**
     * 旧的运输证图片(存储路径)
     */
    private String oldTransportNumberPhoto;

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
        argClass[0] = VehicleTransportInfoUploadEntity.class;
        return argClass;
    }

    public String getExceptionInfo() {
        return "APP上传车辆运输证信息异常";
    }
}
