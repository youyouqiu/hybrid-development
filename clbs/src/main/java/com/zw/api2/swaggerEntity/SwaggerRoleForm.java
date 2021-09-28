package com.zw.api2.swaggerEntity;

import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import org.apache.bval.constraints.NotEmpty;

import javax.validation.constraints.Size;

@Data
public class SwaggerRoleForm {

    private String id;

    private String name;

    @Size(max = 140, message = "【描述】最大长度为140个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String description; // 描述

    @NotEmpty(message = "【角色名称】不能为空！", groups = { ValidGroupAdd.class })
    @Size(max = 20, message = "【角色名称】最大20个字符！", groups = { ValidGroupAdd.class,
            ValidGroupUpdate.class })
    private String roleName;
}
