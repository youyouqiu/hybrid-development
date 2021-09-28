package com.zw.platform.domain.core.form;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 角色Form
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RoleForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name; // 角色名称
	@Size(max = 140, message = "【描述】最大长度为140个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
	private String description; // 描述
	
	@NotEmpty(message = "【角色名称】不能为空！", groups = { ValidGroupAdd.class })
	@Size(max = 20, message = "【角色名称】最大20个字符！", groups = { ValidGroupAdd.class,
			ValidGroupUpdate.class })
	private String roleName;
}