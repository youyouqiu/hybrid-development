package com.zw.talkback.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

@Data
public class SkillForm  extends BaseFormBean {

    private String name;

    private String categoriesId;

    private String remark;

    private String categoriesName;
}
