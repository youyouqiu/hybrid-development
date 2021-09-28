package com.zw.app.entity.appOCR;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;


/**
 * 监控对象-驾驶证信息实体
 */
@Data
public class VehicleDrivingLicenseUploadEntity {
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

    /**
     * 行驶证副本图片(存储路径)
     */
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

    /**
     * 数据修改时间
     */
    private Date updateDataTime = new Date();

    /**
     * 修改者username
     */
    private String updateDataUsername;
}
