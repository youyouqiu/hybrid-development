package com.zw.platform.basic.dto.export;

import com.zw.platform.basic.constant.GenderEnum;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 人员导入实体
 *
 * @author zhangjuan
 */
@Data
@NoArgsConstructor
public class PeopleExportDTO {
    @ExcelField(title = "编号")
    private String number;

    @ExcelField(title = "性别")
    private String gender;

    @ExcelField(title = "姓名")
    private String name;

    @ExcelField(title = "身份证号")
    private String identity;

    @ExcelField(title = "电话")
    private String phone;

    @ExcelField(title = "邮箱")
    private String email;

    @ExcelField(title = "备注")
    private String remark;

    public PeopleExportDTO(PeopleDTO peopleDTO) {
        this.number = peopleDTO.getName();
        this.name = peopleDTO.getAlias();
        this.gender = GenderEnum.getName(peopleDTO.getGender());
        this.identity = peopleDTO.getIdentity();
        this.phone = peopleDTO.getPhone();
        this.email = peopleDTO.getEmail();
        this.remark = peopleDTO.getRemark();
    }
}
