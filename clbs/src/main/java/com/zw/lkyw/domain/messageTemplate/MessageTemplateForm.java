package com.zw.lkyw.domain.messageTemplate;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

/**
 * @author XK on 2019/12/26
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MessageTemplateForm extends BaseFormBean {

    @Size(max = 79, message = "【消息内容】长度不超过79！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private String content;

    private Integer status;

    @Size(max = 50, message = "【备注】长度不超过50！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private String remark;

}
