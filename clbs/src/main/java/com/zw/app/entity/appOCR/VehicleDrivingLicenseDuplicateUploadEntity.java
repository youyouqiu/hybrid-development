package com.zw.app.entity.appOCR;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


/**
 * APP-OCR 行驶证副本实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleDrivingLicenseDuplicateUploadEntity extends BaseEntity {
    @NotNull(message = "监控对象id不能为空")
    private String monitorId;

    /**
     * 行驶证副本图片(存储路径)
     */
    @NotNull(message = "行驶证副本照片不能为空")
    private String drivingLicenseDuplicatePhoto;

    /**
     * 旧的行驶证副本图片(存储路径)
     */
    private String oldDrivingLicenseDuplicatePhoto;

    /**
     * 页面标识(行驶证正本: 1; 行驶证副本: 2)
     */
    private String pageSign;

    /**
     * 校验有效期至
     */
    private String validEndDate;

    /**
     * 总质量（kg）
     */
    private String totalQuality;

    /**
     * 外廓尺寸-长（mm）
     */
    private Integer profileSizeLong;

    /**
     * 外廓尺寸-宽（mm）
     */
    private Integer profileSizeWide;

    /**
     * 外廓尺寸-高（mm）
     */
    private Integer profileSizeHigh;

    public Object[] getArgs() {
        List<Object> args = new ArrayList<>();
        args.add(this);
        return args.toArray();
    }

    public Class<?>[] getArgClasses() {
        Class<?>[] argClass = new Class<?>[1];
        argClass[0] = VehicleDrivingLicenseDuplicateUploadEntity.class;
        return argClass;
    }

    public String getExceptionInfo() {
        return "APP上传车辆驾驶证副本信息异常";
    }
}
