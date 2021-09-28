package com.zw.platform.basic.dto.export;

import com.zw.platform.basic.constant.GenderEnum;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对讲人员导出实体
 *
 * @author zhangjuan
 */
@Data
@NoArgsConstructor
public class IntercomPeopleExportDTO {
    @ExcelField(title = "监控对象")
    private String number;

    @ExcelField(title = "职位类别")
    private String jobName;

    @ExcelField(title = "技能")
    private String skillNames;

    @ExcelField(title = "驾照类别")
    private String driverTypeNames;

    @ExcelField(title = "资格证")
    private String qualification;

    @ExcelField(title = "血型")
    private String bloodType;

    @ExcelField(title = "身份证")
    private String identity;

    @ExcelField(title = "民族")
    private String nation;

    @ExcelField(title = "性别")
    private String gender;

    @ExcelField(title = "联系电话")
    private String phone;

    @ExcelField(title = "工作状态")
    private String isIncumbency;

    @ExcelField(title = "终端号")
    private String deviceNumber;

    @ExcelField(title = "终端手机号")
    private String simCardNumber;

    @ExcelField(title = "所属分组")
    private String groupName;

    @ExcelField(title = "备注")
    private String remark;

    public IntercomPeopleExportDTO(PeopleDTO people) {
        this.number = people.getName();
        this.jobName = people.getJobName();
        this.skillNames = people.getSkillNames();
        this.driverTypeNames = people.getDriverTypeNames();
        this.qualification = people.getQualificationName();
        this.bloodType = people.getBloodTypeName();
        this.identity = people.getIdentity();
        this.nation = people.getNationName();
        this.gender = GenderEnum.getName(people.getGender());
        this.phone = people.getPhone();
        this.isIncumbency = PeopleDTO.IS_INCUMBENCY.p2b(people.getIsIncumbency());
        this.deviceNumber = people.getDeviceNumber();
        this.simCardNumber = people.getSimCardNumber();
        this.groupName = people.getGroupName();
        this.remark = people.getRemark();
    }

}
