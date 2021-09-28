package com.zw.platform.basic.domain;

import lombok.Data;

import java.util.Date;

/**
 * @Author: zjc
 * @Description:司机评分模块实体
 * @Date: create in 2021/2/1 15:16
 */
@Data
public class ProfessionalShowDTO {

    /**
     * id
     */
    private String id;
    /**
     * 从业人员名称
     */
    private String name;

    /**
     * 岗位类型
     */
    private String type;

    /**
     * 从业资格证号
     */
    private String cardNumber;
    /**
     * 从业资格证类别
     */
    private String qualificationCategory;

    /**
     * 发证机关
     */
    private String icCardAgencies;
    /**
     * 有效期
     */

    private transient Date icCardEndDate;

    /**
     * 企业id
     */
    private transient String orgId;
    /**
     * 企业名称
     */
    private transient String orgName;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 为1时候代表是ic卡绑定的从业人员
     */
    private Integer lockType;

    private String icCardEndDateStr;

    /**
     * 住址
     */
    private String address;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 身份证号
     */
    private String identity;
    /**
     * 驾驶证号
     */
    private String drivingLicenseNo;

}
