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
 * 资源Form
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ResourceForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotEmpty(message = "【资源名称】不能为空！", groups = { ValidGroupAdd.class })
    @Size(min = 1, max = 20, message = "【资源名称】最少1个字符，最大20个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String resourceName; // 名称
    private String iconCls;
    private Integer type = 1; // 资源类型 1菜单 2按钮 3方法
    private String code; // 资源代码
    @NotEmpty(message = "【资源标志】不能为空！", groups = { ValidGroupAdd.class })
    private String permission; // 授权
    private String permValue;
    @Size(max = 100, message = "【描述】最大长度为100个字符！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class })
    private String description; // 描述
    private String parentId; //
    private String codeNum;
}