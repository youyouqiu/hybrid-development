package com.zw.platform.domain.basicinfo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Title: 从业人员管理实体
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月26日下午4:08:45
 */
@Data
@NoArgsConstructor
public class ProfessionalsInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 从业人员信息
     */
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 岗位类型
     */
    private String positionType;

    /**
     * 身份证号
     */
    private String identity;

    /**
     * 工号
     */
    private String jobNumber;

    /**
     * 从业人员资格证号
     */
    private String cardNumber;

    /**
     * 性别
     */
    private String gender;

    /**
     * 出生年月
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 照片
     */
    private String photograph;

    /**
     * 电话
     */
    private String phone;
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

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;
    private String updateDataUsername;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date hiredate;

    private String state;
    /**
     * 岗位类型
     */
    private String type;

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
     * 驾驶证号
     */
    private String drivingLicenseNo;
    /**
     * 驾驶证发证机关
     */
    private String drivingAgencies;
    /**
     * 操作证号
     */
    private String operationNumber;
    /**
     * 操作证发证机关
     */
    private String operationAgencies;

    // * (0:A1(大型客车);1:A2(牵引车);2:A3(城市公交车);
    // * 3:B1(中型客车);4:B2(大型货车);5:C1(小型汽车);
    // * 6:C2(小型自动挡汽车);7:C3(低速载货汽车);8:C4(三轮汽车);
    // * 9:D(普通三轮摩托车);10:E(普通二轮摩托车);11:F(轻便摩托车);
    // * 12:M(轮式自行机械车);13:N(无轨电车);14:P(有轨电车))
    /**
     * 准驾车型
     * @author tianzhangxu
     */
    private String drivingType;
    // /**
    //  * 准驾车型(用于导入导出)
    //  */
    // private String drivingTypeForExport;
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
     * 提前提醒天数
     */
    @DateTimeFormat(pattern = "yyyyMMdd")
    private Date expiryDate;

    /**
     * 从业资格证证有效期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date icCardEndDate;

    /**
     * 从业资格证发证机关
     */
    private String icCardAgencies;

    /**
     * 身份证照片
     * 存储路径
     */
    private String identityCardPhoto;

    /**
     * 从业资格证照片
     * 存储路径
     */
    private String qualificationCertificatePhoto;
    /**
     * 驾驶证证照片
     * 存储路径
     */
    private String driverLicensePhoto;

    /**
     * 标记 0：平台录入  1：插卡录入
     */
    private Integer lockType;

    /**
     * 住址
     */
    private String address;

    /**
     * 从业资格证类别
     */
    private String qualificationCategory;
}
