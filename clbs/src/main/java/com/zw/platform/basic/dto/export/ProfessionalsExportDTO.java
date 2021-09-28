package com.zw.platform.basic.dto.export;

import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p> Title: 从业人员管理Form </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年7月26日下午4:13:57
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalsExportDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 姓名
     */
    @ExcelField(title = "姓名")
    private String name;



    /**
     * 服务企业
     */
    @ExcelField(title = "服务企业")
    private String serviceCompany;


    @ExcelField(title = "岗位类型")
    @ApiParam(hidden = true)
    private String type;

    /**
     * 身份证号
     */
    @ExcelField(title = "身份证号")
    @ApiParam(value = "身份证号")
    private String identity;

    /**
     * 入职时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date hiredate;



    /**
     * 入职时间
     */
    @ExcelField(title = "入职时间")
    private String hiredateStr;

    /**
     * 状态
     */
    @ExcelField(title = "状态")
    private String state;

    /**
     * 工号
     */
    @ExcelField(title = "工号")
    private String jobNumber;

    /**
     * 卡号
     */
    @ExcelField(title = "从业资格证号")
    private String cardNumber;

    /**
     * 从业资格类别
     */
    @ExcelField(title = "从业资格类别")
    private String qualificationCategory;

    /**
     * 从业资格证发证机关
     */
    @ExcelField(title = "发证机关")
    private String icCardAgencies;

    /**
     * 发证日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date issueCertificateDate;

    /**
     * 发证日期
     */
    @ExcelField(title = "发证日期")
    private String issueCertificateDateStr;

    /**
     * 从业资格证证有效期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date icCardEndDate;

    /**
     * 证件有效期
     */
    @ExcelField(title = "证件有效期")
    private String icCardEndDateStr;

    /**
     * 性别
     */
    @ExcelField(title = "性别")
    private String gender;

    /**
     * 生日
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 生日
     */
    @ExcelField(title = "生日")
    private String birthdayStr;

    /**
     * 所属地域
     */
    @ExcelField(title = "所属地域")
    private String regional;

    /**
     * 籍贯
     */
    @ExcelField(title = "籍贯")
    private String nativePlace;


    /**
     * 手机1
     */
    @ExcelField(title = "手机1")
    private String phone;

    /**
     * 手机2
     */
    @ExcelField(title = "手机2")
    private String phoneTwo;

    /**
     * 手机3
     */
    @ExcelField(title = "手机3")
    private String phoneThree;

    /**
     * 座机
     */
    @ExcelField(title = "座机")
    private String landline;

    /**
     * 紧急联系人
     */
    @ExcelField(title = "紧急联系人")
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    @ExcelField(title = "紧急联系人电话")
    private String emergencyContactPhone;

    /**
     * 邮箱
     */
    @ExcelField(title = "邮箱")
    private String email;

    /**
     * 地址
     */
    @ExcelField(title = "地址")
    private String address;

    /**
     * 操作证号
     */
    @ExcelField(title = "操作证号")
    private String operationNumber;

    /**
     * 操作证发证机关
     */
    @ExcelField(title = "操作证发证机关")
    private String operationAgencies;

    /**
     * 驾驶证号
     */
    @ExcelField(title = "驾驶证号")
    private String drivingLicenseNo;

    /**
     * 驾驶证发证机关
     */
    @ExcelField(title = "驾驶证发证机关")
    private String drivingAgencies;

    /**
     * 准驾车型
     * // * (0:A1(大型客车);1:A2(牵引车);2:A3(城市公交车);
     * // * 3:B1(中型客车);4:B2(大型货车);5:C1(小型汽车);
     * // * 6:C2(小型自动挡汽车);7:C3(低速载货汽车);8:C4(三轮汽车);
     * // * 9:D(普通三轮摩托车);10:E(普通二轮摩托车);11:F(轻便摩托车);
     * // * 12:M(轮式自行机械车);13:N(无轨电车);14:P(有轨电车))
     */
    @ExcelField(title = "准驾车型")
    private String drivingType;


    /**
     * 准驾有效期起
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date drivingStartDate;

    /**
     * 准驾有效期起
     */
    @ExcelField(title = "准驾有效期起")
    private String drivingStartDateStr;

    /**
     * 准驾有效期至
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date drivingEndDate;

    /**
     * 准驾有效期至
     */
    @ExcelField(title = "准驾有效期至")
    private String drivingEndDateStr;

    /**
     * 提前提醒天数
     */
    @ExcelField(title = "提前提醒天数")
    private Integer remindDays;

    /**
     * 民族
     */
    private String nationId;

    @ExcelField(title = "民族")
    private String nation;

    /**
     * 文化程度
     */
    private String educationId;

    @ExcelField(title = "文化程度")
    private String education;

}
