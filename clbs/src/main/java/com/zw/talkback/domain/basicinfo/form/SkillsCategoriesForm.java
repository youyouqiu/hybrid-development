package com.zw.talkback.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

/**
 * 技能类别表单对象
 */
@Data
public class SkillsCategoriesForm extends BaseFormBean {

    /**
     * 技能类别名称
     */
    private String name;

    /**
     * 技能类别备注
     */
    private String remark;
}
