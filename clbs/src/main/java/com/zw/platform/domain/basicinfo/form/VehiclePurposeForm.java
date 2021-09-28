package com.zw.platform.domain.basicinfo.form;

import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import com.zw.platform.util.excel.annotation.ExcelField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class VehiclePurposeForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(max = 20, message = "【运营类别】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "运营类别")
    private String purposeCategory;// 用途类别

    @Size(max = 50, message = "【说明】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    @ExcelField(title = "备注")
    private String description; // 车辆用途说明

    private String codeNum;

    private boolean duplicated = false;

    public VehiclePurposeDTO convert() {
        VehiclePurposeDTO purposeDTO = new VehiclePurposeDTO();
        purposeDTO.setId(this.getId());
        purposeDTO.setCodeNum(this.codeNum);
        purposeDTO.setDescription(this.description);
        purposeDTO.setPurposeCategory(this.purposeCategory);
        return purposeDTO;
    }
}
