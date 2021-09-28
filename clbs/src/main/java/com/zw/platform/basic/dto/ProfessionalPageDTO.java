package com.zw.platform.basic.dto;

import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 从业人员缓存实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalPageDTO {
    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();

    /**
     * 姓名
     */
    private String name;

    /**
     * 服务企业
     */
    private String serviceCompany;

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
     * 入职时间 yyyy-MM-dd
     */
    @ApiParam(value = "入职时间")
    private String hiredate;

    /**
     * 状态
     */
    private String state;

    /**
     * 工号
     */
    @ApiParam(value = "工号")
    private String jobNumber;

    /**
     * 卡号
     */
    private String cardNumber;

    /**
     * 从业资格类别
     */
    @ApiParam(value = "从业资格类别")
    private String qualificationCategory;

    /**
     * 从业资格证发证机关
     */
    private String icCardAgencies;

    /**
     * 发证日期 yyyy-MM-dd
     */
    @ApiParam(value = "发证日期")
    private String issueCertificateDate;

    /**
     * 从业资格证证有效期
     */
    private String icCardEndDate;

    /**
     * 性别
     */
    @ApiParam(value = "姓别")
    private String gender;

    /**
     * 生日 yyyy-MM-dd
     */
    @ApiParam(value = "生日")
    private String birthday;

    /**
     * 所属地域
     */
    private String regional;

    /**
     * 籍贯
     */
    private String nativePlace;

    /**
     * 照片
     */
    @ApiParam(value = "照片")
    private String photograph;

    /**
     * 手机1
     */
    @ApiParam(value = "手机号1")
    private String phone;

    /**
     * 手机2
     */
    @ApiParam(value = "手机号2")
    private String phoneTwo;

    /**
     * 手机3
     */
    @ApiParam(value = "手机号3")
    private String phoneThree;

    /**
     * 座机
     */
    @ApiParam(value = "座机")
    private String landline;

    /**
     * 紧急联系人
     */
    @ApiParam(value = "紧急联系人")
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    @ApiParam(value = "紧急联系人电话")
    private String emergencyContactPhone;

    /**
     * 邮箱
     */
    @ApiParam(value = "邮箱")
    private String email;

    /**
     * 地址
     */
    @ApiParam(value = "地址")
    private String address;

    /**
     * 操作证号
     */
    @ApiParam(value = "操作证号")
    private String operationNumber;

    /**
     * 操作证发证机关
     */
    @ApiParam(value = "操作证发证机关")
    private String operationAgencies;

    /**
     * 驾驶证号
     */
    private String drivingLicenseNo;

    /**
     * 驾驶证发证机关
     */
    @ApiParam(value = "驾驶证发证机关")
    private String drivingAgencies;

    /**
     * 准驾车型
     * // * (0:A1(大型客车);1:A2(牵引车);2:A3(城市公交车);
     * // * 3:B1(中型客车);4:B2(大型货车);5:C1(小型汽车);
     * // * 6:C2(小型自动挡汽车);7:C3(低速载货汽车);8:C4(三轮汽车);
     * // * 9:D(普通三轮摩托车);10:E(普通二轮摩托车);11:F(轻便摩托车);
     * // * 12:M(轮式自行机械车);13:N(无轨电车);14:P(有轨电车))
     */
    @ApiParam(value = "准驾车型")
    private String drivingType;

    /**
     * 准驾有效期起 yyyy-MM-dd
     */
    @ApiParam(value = "准驾有效期起")
    private String drivingStartDate;

    /**
     * 准驾有效期至 yyyy-MM-dd
     */
    @ApiParam(value = "准驾有效期止")
    private String drivingEndDate;

    /**
     * 提前提醒天数
     */
    @ApiParam(value = "提前提醒天数")
    private Integer remindDays;

    /**
     * 身份证地址
     */
    private String identityCardPhoto;

    /**
     * 驾驶证地址
     */
    private String driverLicensePhoto;

    /**
     * 从业资格证地址
     */
    private String qualificationCertificatePhoto;

    /**
     * 组织
     */
    @ApiParam(value = "所属企业名字")
    private String orgName;

    /**
     * 人脸id
     */
    private String faceId;

    /**
     * 民族
     */
    private String nationId;

    private String nation;

    /**
     * 文化程度
     */
    private String educationId;

    private String education;

}
