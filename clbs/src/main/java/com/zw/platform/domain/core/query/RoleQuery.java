package com.zw.platform.domain.core.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.naming.Name;
import java.io.Serializable;

/**
 * 角色Query
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RoleQuery extends BaseQueryBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private Name id;
	private String description;
	private String roleName;

}