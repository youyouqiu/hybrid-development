package com.zw.platform.domain.basicinfo.form;


import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;
import java.io.Serializable;


/**
 * 岗位类型实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProfessionalsTypeForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 20, message = "【岗位类型】长度不超过20！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "岗位类型")
    private String professionalstype; // 车辆类别

    @Size(max = 50, message = "【类型描述】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @ExcelField(title = "类型描述")
    private String description; // 类型描述
}
