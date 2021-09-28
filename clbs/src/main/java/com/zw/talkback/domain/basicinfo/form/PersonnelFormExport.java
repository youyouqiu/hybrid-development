package com.zw.talkback.domain.basicinfo.form;

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

@Data
@EqualsAndHashCode(callSuper = false)
public class PersonnelFormExport extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_JOB_ID = "default"; // 默认的职位类别id
    /**
     * 人员编号
     */
    @NotEmpty(message = "【监控对象】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @Size(max = 20, message = "【监控对象】最大20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "监控对象")
    private String peopleNumber; // 人员编号

    private String groupName;

    /**
     * 职位类别id
     */
    @NotEmpty(message = "【职业类别】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String jobId;

    @ExcelField(title = "职位类别")
    private String jobName;

    /**
     * 技能
     */
    private String skillIds;

    @ExcelField(title = "技能")
    private String skillNames;

    /**
     * 驾照类别
     */
    private String driverTypeIds;

    @ExcelField(title = "驾照类别")
    private String driverTypeNames;

    /**
     * 资格证
     */
    private String qualificationId;

    @ExcelField(title = "资格证")
    private String qualificationName;

    /**
     * 血型
     */
    private String bloodTypeId;

    @ExcelField(title = "血型")
    private String bloodTypeName;

    @ExcelField(title = "身份证")
    private String identity;

    /**
     * 民族
     */
    private String nationId;

    @ExcelField(title = "民族")
    private String nationName;

    /**
     * 出生年月
     */
    private Date birthday;

    /**
     * 性别
     */
    @Pattern(message = "【性别】输入错误，只能输入1,2,其中1:男,2:女！", regexp = "^[1-2]{1}$",
        groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "性别")
    private String gender;

    /**
     * 姓名
     */
    @Size(max = 15, message = "【姓名】最大15个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String name;

    /**
     * 电话
     */
    @ExcelField(title = "联系电话")
    private String phone;

    /**
     * 工作状态（是否在职 在职2 离职0   新增默认1 空白）
     */
    private Integer isIncumbency = 1;

    private String isIncumbencyStr;

    /**
     * 地址
     */
    private String address;
    
   
    /**
     * 邮箱
     */
    private String email;
    /**
     * 备注
     */
    @ExcelField(title = "备注")
    private String remark;
    
    @NotEmpty(message = "【群组】不能为空！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String groupId; // 所属企业

    private String deviceNumber; // 终端编号

    private String simcardNumber; // sim卡号

    private String createDataTimeStr; // 创建时间 yyyy-MM-dd

    private String assignmentId; // 分组id

    private String assignmentName; // 分组名称

    private String assignmentGroupId; // 分组所属企业id

    public static PersonnelFormExport initConfigImport(ConfigImportForm config, String userName) {
        PersonnelFormExport personnelForm = new PersonnelFormExport();
        personnelForm.setPeopleNumber(config.getCarLicense());
        personnelForm.setEmail("");
        personnelForm.setName("");
        personnelForm.setIdentity("");
        personnelForm.setPhone("");
        personnelForm.setGender("1");
        personnelForm.setJobId("default");
        personnelForm.setIsIncumbency(2);  // 导入后变在职
        personnelForm.setCreateDataUsername(userName);
        personnelForm.setGroupId(config.getGroupId());
        personnelForm.setDeviceNumber(config.getDeviceNumber());
        personnelForm.setSimcardNumber(config.getSimcardNumber());
        personnelForm.setCreateDataTimeStr(DateFormatUtils.format(personnelForm.getCreateDataTime(), "yyyy-MM-dd"));
        String[] assignmentAndGroup = config.getGroupName().split("@");
        personnelForm.setAssignmentName(assignmentAndGroup[0]);
        personnelForm.setGroupName(config.getGroupName());
        personnelForm.setJobId(DEFAULT_JOB_ID);
        return personnelForm;
    }
}
