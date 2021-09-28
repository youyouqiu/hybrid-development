package com.zw.platform.domain.core.query;

import com.zw.platform.util.common.BaseQueryBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.naming.Name;
import java.io.Serializable;


/**
 * User Query
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserQuery extends BaseQueryBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private Name id;
	private String fullName;
	private String lastName;
	private String mail;
	private String mobile;
	private String groupId;
	private String username;
	private String groupName;
	private String roleName;
	private String password;
	
}