package com.zw.platform.basic.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

/**
 * 从业人员缓存实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalDO {
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
     * 身份证号
     */
    private String identity;

    /**
     * 入职时间
     */
    private Date hiredate;

    /**
     * 状态
     */
    private String state;

    /**
     * 工号
     */
    private String jobNumber;

    /**
     * 卡号
     */
    private String cardNumber;

    /**
     * 从业资格类别
     */
    private String qualificationCategory;

    /**
     * 从业资格证发证机关
     */
    private String icCardAgencies;

    /**
     * 发证日期
     */
    private Date issueCertificateDate;

    /**
     * 从业资格证证有效期
     */
    private Date icCardEndDate;

    /**
     * 性别
     */
    private String gender = "1";

    /**
     * 生日
     */
    private Date birthday;

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
    private String photograph;

    /**
     * 手机1
     */
    private String phone;

    /**
     * 录入信息类别
     */
    private Integer lockType = 0;

    /**
     * 手机2
     */
    private String phoneTwo;

    /**
     * 手机3
     */
    private String phoneThree;

    /**
     * 座机
     */
    private String landline;

    /**
     * 紧急联系人
     */
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    private String emergencyContactPhone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 地址
     */
    private String address;

    /**
     * 操作证号
     */
    private String operationNumber;

    /**
     * 操作证发证机关
     */
    private String operationAgencies;

    /**
     * 驾驶证号
     */
    private String drivingLicenseNo;

    /**
     * 驾驶证发证机关
     */
    private String drivingAgencies;

    /**
     * 准驾车型
     * // * (0:A1(大型客车);1:A2(牵引车);2:A3(城市公交车);
     * // * 3:B1(中型客车);4:B2(大型货车);5:C1(小型汽车);
     * // * 6:C2(小型自动挡汽车);7:C3(低速载货汽车);8:C4(三轮汽车);
     * // * 9:D(普通三轮摩托车);10:E(普通二轮摩托车);11:F(轻便摩托车);
     * // * 12:M(轮式自行机械车);13:N(无轨电车);14:P(有轨电车))
     */
    private String drivingType;

    /**
     * 准驾有效期起
     */
    private Date drivingStartDate;

    /**
     * 准驾有效期至
     */
    private Date drivingEndDate;

    /**
     * 提前提醒天数
     */
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

    private String photoAddress;

    /**
     * 企业id
     */
    private String orgId;

    /**
     * 民族
     */
    private String nationId;

    /**
     * 文化程度
     */
    private String educationId;

    /**
     * create_data_time
     */
    private Date createDataTime;
    /**
     * create_data_username
     */
    private String createDataUsername;
    /**
     * update_data_time
     */
    private Date updateDataTime;
    /**
     * update_data_username
     */
    private String updateDataUsername;

    private Integer flag = 1;



}
