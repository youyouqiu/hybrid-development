package com.zw.platform.domain.core.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 角色资源关联Form
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RoleResourceForm extends BaseFormBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String roleId; // 角色id
	private String resourceId; // 资源id
	/**
	 * 0表示可读、1表示可写
	 */
	private Integer editable;
}