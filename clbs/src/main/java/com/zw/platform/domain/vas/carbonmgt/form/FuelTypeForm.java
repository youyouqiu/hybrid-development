package com.zw.platform.domain.vas.carbonmgt.form;

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
public class FuelTypeForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;

	@Size(max = 20, message = "【燃料类型】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	@ExcelField(title = "燃料类型")
	private String fuelType; // 燃料类型
	
	//0:柴油 1：汽油 2：天然气
	@Size(max = 20, message = "【燃料类别】长度不超过20！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	@ExcelField(title = "燃料类别")
	private String fuelCategory; // 燃料类别
	
	@Size(max = 50, message = "【燃料类型描述】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	@ExcelField(title = "燃料类型描述")
	private String describes; // 燃料类型描述

}
