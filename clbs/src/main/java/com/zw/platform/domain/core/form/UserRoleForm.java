package com.zw.platform.domain.core.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * 用户角色关联Form
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserRoleForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String roleId;
}