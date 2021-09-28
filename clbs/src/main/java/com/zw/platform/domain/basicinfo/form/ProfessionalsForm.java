package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.util.ConverterDateUtil;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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
public class ProfessionalsForm extends BaseFormBean implements Serializable, ConverterDateUtil {
    private static final long serialVersionUID = 1L;

    /**
     * 姓名
     */
    @NotEmpty(message = "【姓名】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(min = 1, max = 20, message = "【姓名】长度为2——20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "姓名")
    @ApiParam(value = "姓名")
    private String name;

    /**
     * 组织
     */
    // @ExcelField(title = "所属企业")
    @ApiParam(value = "所属企业名字")
    private String groupName;

    /**
     * 组织id
     */
    @ApiParam(value = "所属企业id")
    private String groupId;

    /**
     * 服务企业
     */
    @ExcelField(title = "服务企业")
    @Size(max = 20, message = "【从业资格证发证机关】长度不能超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "服务企业")
    private String serviceCompany;

    /**
     * 岗位类型
     */
    @Size(max = 64, message = "【岗位类型】长度不能超过64！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "岗位类型")
    private String positionType;

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
    @ApiParam(value = "入职时间")
    private Date hiredate;

    /**
     * 入职时间
     */
    @ExcelField(title = "入职时间")
    @ApiParam(hidden = true)
    private String hiredateStr;

    /**
     * 状态
     */
    @ExcelField(title = "状态")
    @ApiParam(value = "状态")
    private String state;

    /**
     * 工号
     */
    @Size(max = 30, message = "【工号】长度不能超过30！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "工号")
    @ApiParam(value = "工号")
    private String jobNumber;

    /**
     * 卡号
     */
    @Size(max = 30, message = "【从业资格证号】长度不能超过30！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "从业资格证号")
    @ApiParam(value = "从业资格证号")
    private String cardNumber;

    /**
     * 从业资格类别
     */
    @ExcelField(title = "从业资格类别")
    @Size(max = 50, message = "【从业资格类别】长度不能超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "从业资格类别")
    private String qualificationCategory;

    /**
     * 从业资格证发证机关
     */
    @ExcelField(title = "发证机关")
    @Size(max = 128, message = "【发证机关】长度不能超过128！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "发证机关")
    private String icCardAgencies;

    /**
     * 发证日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiParam(value = "发证日期")
    private Date issueCertificateDate;

    /**
     * 发证日期
     */
    @ExcelField(title = "发证日期")
    @ApiParam(hidden = true)
    private String issueCertificateDateStr;

    /**
     * 从业资格证证有效期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiParam(value = "证件有效期")
    private Date icCardEndDate;

    /**
     * 证件有效期
     */
    @ExcelField(title = "证件有效期")
    @ApiParam(hidden = true)
    private String icCardEndDateStr;

    /**
     * 性别
     */
    @Size(max = 4, message = "【性别】长度不能超过4！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Pattern(message = "【性别】填值错误！", regexp = "^[1-2]$", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "性别")
    @ApiParam(value = "姓别")
    private String gender;

    /**
     * 生日
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiParam(value = "生日")
    private Date birthday;

    /**
     * 生日
     */
    @ExcelField(title = "生日")
    @ApiParam(hidden = true)
    private String birthdayStr;

    /**
     * 所属地域
     */
    @ExcelField(title = "所属地域")
    @Size(max = 20, message = "【所属地域】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String regional;

    /**
     * 籍贯
     */
    @ExcelField(title = "籍贯")
    @Size(max = 20, message = "【籍贯】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String nativePlace;

    /**
     * 照片
     */
    @ApiParam(value = "照片")
    private String photograph;

    /**
     * 手机1
     */
    // @NotEmpty(message = "【手机1】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    //@Pattern(message = "【手机1】格式错误！", regexp = "^((13[0-9]|14[579]|15[0-3,5-9]|16[6]|
    // 17[0135678]|18[0-9]|19[89])\\d{8})?$", groups = {ValidGroupAdd.class,ValidGroupUpdate.class})
    @Pattern(regexp = "^(\\d{7,13})?$", message = "【手机1】整数长度7-13！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "手机1")
    @ApiParam(value = "手机号1")
    private String phone;

    /**
     * 录入信息类别
     */
    private Integer lockType = 0;

    /**
     * 手机2
     */
    //@Pattern(message = "【手机2】格式错误！", regexp = "^((13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]
    //|18[0-9]|19[89])\\d{8})?$", groups = {ValidGroupAdd.class,ValidGroupUpdate.class})
    @Pattern(regexp = "^(\\d{7,13})?$", message = "【手机2】整数长度7-13！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "手机2")
    @ApiParam(value = "手机号2")
    private String phoneTwo;

    /**
     * 手机3
     */
    //@Pattern(message = "【手机3】格式错误！", regexp = "^((13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]
    // |18[0-9]|19[89])\\d{8})?$", groups = {ValidGroupAdd.class,ValidGroupUpdate.class})
    @Pattern(regexp = "^(\\d{7,13})?$", message = "【手机3】整数长度7-13！", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "手机3")
    @ApiParam(value = "手机号3")
    private String phoneThree;

    /**
     * 座机
     */
    @Pattern(message = "【座机】格式错误！", regexp = "^(\\d{3}-\\d{8}|\\d{4}-\\d{7,8}|\\d{7,13})?$", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "座机")
    @ApiParam(value = "座机")
    private String landline;

    /**
     * 紧急联系人
     */
    @ExcelField(title = "紧急联系人")
    @Size(max = 20, message = "【紧急联系人】长度不能超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "紧急联系人")
    private String emergencyContact;

    /**
     * 紧急联系人电话
     */
    @ExcelField(title = "紧急联系人电话")
    //@Pattern(message = "【紧急联系人电话】格式错误！", regexp = "^((13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]
    //|18[0-9]|19[89])\\d{8})?$", groups = {ValidGroupAdd.class,ValidGroupUpdate.class})
    @Pattern(regexp = "^(\\d{3}-\\d{8}|\\d{4}-\\d{7,8}|\\d{7,13})?$", message = "【紧急联系人电话】长度7-13！", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "紧急联系人电话")
    private String emergencyContactPhone;

    /**
     * 邮箱
     */
    @Pattern(message = "【邮箱】格式错误！", regexp = "^\\s*$|^[\\w.]+@\\w+\\.[a-z]+(\\.[a-z]+)?", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 50, message = "【邮箱】长度不能超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "邮箱")
    @ApiParam(value = "邮箱")
    private String email;

    /**
     * 地址
     */
    @ExcelField(title = "地址")
    @Size(max = 50, message = "【地址】长度不能超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "地址")
    private String address;

    /**
     * 操作证号
     */
    @ExcelField(title = "操作证号")
    @Size(max = 64, message = "【操作证号】长度不能超过64！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "操作证号")
    private String operationNumber;

    /**
     * 操作证发证机关
     */
    @ExcelField(title = "操作证发证机关")
    @Size(max = 128, message = "【操作证发证机关】长度不能超过128！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "操作证发证机关")
    private String operationAgencies;

    /**
     * 驾驶证号
     */
    @ExcelField(title = "驾驶证号")
    @Size(max = 64, message = "【驾驶证号】长度不能超过64！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiParam(value = "驾驶证号")
    private String drivingLicenseNo;

    /**
     * 驾驶证发证机关
     */
    @ExcelField(title = "驾驶证发证机关")
    @Size(max = 128, message = "【驾驶证发证机关】长度不能超过128！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
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
    @Size(max = 10, message = "【准驾车型】长度不能超过10！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "准驾车型")
    @ApiParam(value = "准驾车型")
    private String drivingType;

    // /**
    //  * 准驾车型(用于导入导出)
    //  */
    // @ExcelField(title = "准驾车型")
    // @ApiParam(hidden = true)
    // private String drivingTypeForExport;

    /**
     * 准驾有效期起
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiParam(value = "准驾有效期起")
    private Date drivingStartDate;

    /**
     * 准驾有效期起
     */
    @ExcelField(title = "准驾有效期起")
    @ApiParam(hidden = true)
    private String drivingStartDateStr;

    /**
     * 准驾有效期至
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiParam(value = "准驾有效期起")
    private Date drivingEndDate;

    /**
     * 准驾有效期至
     */
    @ExcelField(title = "准驾有效期至")
    @ApiParam(hidden = true)
    private String drivingEndDateStr;

    /**
     * 提前提醒天数
     */
    @ExcelField(title = "提前提醒天数")
    @Max(value = 9999, message = "【提前提醒天数】范围0-9999！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Min(value = 0, message = "【提前提醒天数】范围0-9999！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
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

    private String photoAddress;

    private String faceId;

    /**
     * 民族
     */
    @ExcelField(title = "民族")
    private String nation;

    private String nationId;

    /**
     * 文化程度
     */
    @ExcelField(title = "文化程度")
    private String education;

    private String educationId;


    @ExcelField(title = "错误信息", type = 1)
    private String errorMsg;
}
