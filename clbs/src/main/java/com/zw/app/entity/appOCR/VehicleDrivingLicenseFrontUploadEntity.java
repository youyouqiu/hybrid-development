package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


/**
 * APP-OCR 行驶证正本实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleDrivingLicenseFrontUploadEntity extends BaseEntity {
    @NotNull(message = "监控对象id不能为空")
    private String monitorId;

    /**
     * 车架号
     */
    private String chassisNumber;

    /**
     * 发动机号
     */
    private String engineNumber;

    /**
     * 使用性质
     */
    private String usingNature;

    /**
     * 品牌型号
     */
    private String brandModel;

    /**
     * 注册日期(登记日期)
     */
    private String registrationDate;

    /**
     * 发证日期
     */
    private String licenseIssuanceDate;

    /**
     * 行驶证正本图片(存储路径)
     */
    @NotNull(message = "行驶证正本照片不能过为空")
    private String drivingLicenseFrontPhoto;

    /**
     * 旧的行驶证正本图片(存储路径)
     */
    private String oldDrivingLicenseFrontPhoto;

    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(this);
        return args.toArray();
    }

    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[1];
        argClass[0] = VehicleDrivingLicenseFrontUploadEntity.class;
        return argClass;
    }

    public String getExceptionInfo() {
        return "APP上传车辆驾驶证正本信息异常";
    }
}
