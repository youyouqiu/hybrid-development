package com.zw.platform.domain.basicinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/1/8 16:35
 */
@Data
public class ProfessionalInfo implements Serializable {
    private static final long serialVersionUID = -2029296008502011660L;
    private String id;
    /**
     * 从业资格证发证机关
     */
    private String icCardAgencies;
    /**
     * 企业id
     */
    private String groupName;
    /**
     * 岗位类型id
     */
    private String positionType;
    /**
     * 岗位类型
     */
    private String type;
    /**
     * 身份证号
     */
    private String identity;
    /**
     * 驾驶证号
     */
    private String drivingLicenseNo;
    /**
     * 姓名
     */
    private String name;
    private String state;
    /**
     * 标记 0：平台录入  1：插卡录入
     */
    private Integer lockType;

    /**
     * 从业资格证证有效期 毫秒
     */
    private Long icCardEndDate;
    /**
     * 从业人员资格证号
     */
    private String cardNumber;
}
