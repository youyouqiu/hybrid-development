package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.domain.infoconfig.form.ConfigImportForm;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;
import org.apache.commons.lang3.time.DateFormatUtils;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tdz on 2016/7/21.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PersonnelForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 人员编号
     */
    @NotEmpty(message = "【人员编号】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 20, message = "【人员编号】最大20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "编号")
    private String peopleNumber; // 人员编号

    /**
     * 出生年月
     */
    private Date birthday;

    /**
     * 性别
     */
    @Pattern(message = "【性别】输入错误，只能输入1,2,其中1:男,2:女！", regexp = "^[1-2]{1}$", groups = { ValidGroupAdd.class,
        ValidGroupUpdate.class })
    @ExcelField(title = "性别")
    private String gender;

    /**
     * 姓名
     */
    @Size(max = 8, message = "【姓名】最大8个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "姓名")
    private String name;

    /**
     * 身份证号
     */
    @Pattern(message = "【身份证号】输入错误，请输入正确的身份证号！", regexp = "^\\s*$|[1-9]\\d{13,16}[a-zA-Z0-9]{1}", groups = {
        ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "身份证号")
    private String identity;

    /**
     * 电话
     */
    @ExcelField(title = "电话")
    private String phone;

    /**
     * 地址
     */
    private String address;

    /**
     * 邮箱
     */
    @ExcelField(title = "邮箱")
    private String email;
    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String remark;

    @NotEmpty(message = "【分组】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String groupId; // 所属企业

    private String groupName; // 所属企业名称

    private String deviceNumber; // 终端编号

    private String simcardNumber; // sim卡号

    private String createDataTimeStr; // 创建时间 yyyy-MM-dd

    private String assignmentId; // 分组id

    private String assignmentName; // 分组名称

    private String assignmentGroupId; // 分组所属企业id

    private String assignId;

    private String assign;

    /**
     * 身份证照片(存储路径)
     */
    private String identityCardPhoto;

    public static PersonnelForm initConfigImport(ConfigImportForm config, String userName) {
        PersonnelForm personnelForm = new PersonnelForm();
        personnelForm.setPeopleNumber(config.getCarLicense());
        personnelForm.setEmail("");
        personnelForm.setName("");
        personnelForm.setIdentity("");
        personnelForm.setPhone("");
        personnelForm.setGender("1");
        personnelForm.setCreateDataUsername(userName);
        personnelForm.setGroupId(config.getGroupId());
        personnelForm.setDeviceNumber(config.getDeviceNumber());
        personnelForm.setSimcardNumber(config.getSimcardNumber());
        personnelForm.setCreateDataTimeStr(DateFormatUtils.format(personnelForm.getCreateDataTime(), "yyyy-MM-dd"));
        String[] assignmentAndGroup = config.getGroupName().split("@");
        personnelForm.setAssignmentName(assignmentAndGroup[0]);
        personnelForm.setGroupName(config.getGroupName());
        return personnelForm;
    }

    public PeopleDTO getPeopleDTO() {
        PeopleDTO peopleDTO = new PeopleDTO();
        peopleDTO.setId(this.getId());
        peopleDTO.setName(this.peopleNumber);
        peopleDTO.setAlias(this.name);
        peopleDTO.setOrgName(this.groupName);
        peopleDTO.setOrgId(this.groupId);
        peopleDTO.setIdentity(this.identity);
        peopleDTO.setGender(this.gender);
        peopleDTO.setPhone(this.phone);
        peopleDTO.setEmail(this.email);
        peopleDTO.setRemark(this.remark);
        peopleDTO.setBindType(Vehicle.BindType.UNBIND);
        return peopleDTO;
    }

}
