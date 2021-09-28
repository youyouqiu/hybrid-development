package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.excel.annotation.ExcelField;
import com.zw.platform.util.imports.ImportErrorData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/9/14 10:36
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalsImportForm extends ImportErrorData implements Serializable {
    private static final long serialVersionUID = 243227947422804406L;

    private String id = UUID.randomUUID().toString();

    @ExcelField(title = "姓名")
    private String name;

    /**
     * 组织
     */
    private String groupName;

    /**
     * 组织id
     */
    private String groupId;

    @ExcelField(title = "服务企业")
    private String serviceCompany;

    /**
     * 岗位类型
     */
    private String positionType;

    @ExcelField(title = "岗位类型")
    private String type;

    @ExcelField(title = "身份证号")
    private String identity;

    /**
     * 入职时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date hiredate;

    @ExcelField(title = "入职时间")
    private String hiredateStr;

    @ExcelField(title = "状态")
    private String state;

    @ExcelField(title = "工号")
    private String jobNumber;

    @ExcelField(title = "从业资格证号")
    private String cardNumber;

    @ExcelField(title = "从业资格类别")
    private String qualificationCategory;

    @ExcelField(title = "发证机关")
    private String icCardAgencies;

    /**
     * 发证日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date issueCertificateDate;

    @ExcelField(title = "发证日期")
    private String issueCertificateDateStr;

    /**
     * 从业资格证证有效期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date icCardEndDate;

    @ExcelField(title = "证件有效期")
    private String icCardEndDateStr;

    @ExcelField(title = "性别")
    private String gender;

    /**
     * 生日
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    @ExcelField(title = "生日")
    private String birthdayStr;

    @ExcelField(title = "所属地域")
    private String regional;

    @ExcelField(title = "籍贯")
    private String nativePlace;

    /**
     * 照片
     */
    private String photograph;

    @ExcelField(title = "手机1")
    private String phone;

    /**
     * 录入信息类别
     */
    private Integer lockType = 0;

    @ExcelField(title = "手机2")
    private String phoneTwo;

    @ExcelField(title = "手机3")
    private String phoneThree;

    @ExcelField(title = "座机")
    private String landline;

    @ExcelField(title = "紧急联系人")
    private String emergencyContact;

    @ExcelField(title = "紧急联系人电话")
    private String emergencyContactPhone;

    @ExcelField(title = "邮箱")
    private String email;

    @ExcelField(title = "地址")
    private String address;

    @ExcelField(title = "操作证号")
    private String operationNumber;

    @ExcelField(title = "操作证发证机关")
    private String operationAgencies;

    @ExcelField(title = "驾驶证号")
    private String drivingLicenseNo;

    @ExcelField(title = "驾驶证发证机关")
    private String drivingAgencies;

    @ExcelField(title = "准驾车型")
    private String drivingType;

    /**
     * 准驾有效期起
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date drivingStartDate;

    @ExcelField(title = "准驾有效期起")
    private String drivingStartDateStr;

    /**
     * 准驾有效期至
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date drivingEndDate;

    @ExcelField(title = "准驾有效期至")
    private String drivingEndDateStr;

    @ExcelField(title = "提前提醒天数")
    private Integer remindDays;

    /**
     * 民族
     */
    @ExcelField(title = "民族")
    private String nation;

    private  String nationId;

    /**
     * 文化程度
     */
    @ExcelField(title = "文化程度")
    private String education;

    private String educationId;


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

    private Integer flag = 1;

    private Date createDataTime = new Date();

    private String createDataUsername;

    private Date updateDataTime = new Date();

    private String updateDataUsername;

    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}
