package com.zw.platform.basic.dto;

import com.zw.platform.util.Translator;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Pattern;

/**
 * 人员信息实体
 * @author zhangjuan 2020-10-20
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PeopleDTO extends BindDTO {

    public static final Translator<String, Integer> IS_INCUMBENCY = Translator.of("离职", 0, "在职", 2, "", 1);

    @Pattern(message = "【性别】输入错误，只能输入1,2,其中1:男,2:女！", regexp = "^[1-2]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ApiModelProperty(value = "性别")
    private String gender;

    private String genderStr;

    @ApiModelProperty(value = "出生年月")
    private String birthday;

    @Pattern(message = "【身份证号】输入错误，请输入正确的身份证号！", regexp = "^\\s*$|[1-9]\\d{13,16}[a-zA-Z0-9]{1}", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @ApiModelProperty(value = "身份证号")
    private String identity;

    @ApiModelProperty(value = "电话")
    private String phone;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "身份证照片(存储路径)")
    private String identityCardPhoto;

    @ApiModelProperty(value = "是否在职 0:离职； 2:在职 1:显示空白")
    private Integer isIncumbency;

    @ApiModelProperty(value = "职位id")
    private String jobId;

    @ApiModelProperty(value = "职位")
    private String jobName;

    @ApiModelProperty(value = "民族ID")
    private String nationId;

    @ApiModelProperty(value = "民族")
    private String nationName;

    @ApiModelProperty(value = "血型id")
    private String bloodTypeId;

    @ApiModelProperty(value = "血型")
    private String bloodTypeName;

    @ApiModelProperty(value = "资格证id")
    private String qualificationId;

    @ApiModelProperty(value = "资格证")
    private String qualificationName;

    @ApiModelProperty(value = "技能ID,多个用逗号隔开")
    private String skillIds;

    @ApiModelProperty(value = "技能名称,多个用逗号隔开")
    private String skillNames;

    @ApiModelProperty(value = "驾照类别Id,多个用逗号隔开")
    private String driverTypeIds;

    @ApiModelProperty(value = "驾照类别名称,多个用逗号隔开")
    private String driverTypeNames;

    @ApiModelProperty(value = "个性化图标ID")
    private String iconId;

    @ApiModelProperty(value = "个性化图标名称")
    private String iconName;

}
