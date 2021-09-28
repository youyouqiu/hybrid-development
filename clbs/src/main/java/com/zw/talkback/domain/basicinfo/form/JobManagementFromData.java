package com.zw.talkback.domain.basicinfo.form;


import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;


@Data
public class JobManagementFromData extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Pattern(message = "【职位名称】填值错误！可支持汉字、字母、数字或短横杠", regexp = "^[A-Za-z0-9\\u4e00-\\u9fa5\\-]+$", groups = {
        ValidGroupAdd.class,
        ValidGroupUpdate.class})
    @Size(max = 20, message = "【职位名称】不能超过20个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    @NotNull(message = "【职位名称】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String jobName;

    @NotNull(message = "【职位图标】不能为空！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String jobIconName;

    @Size(max = 100, message = "【备注】不能超过100个字符！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String remark;
}
