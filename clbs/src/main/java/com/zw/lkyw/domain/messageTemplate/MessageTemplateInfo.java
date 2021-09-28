package com.zw.lkyw.domain.messageTemplate;

import com.zw.platform.util.common.BaseFormBean;
import com.zw.platform.util.common.ValidGroupAdd;
import com.zw.platform.util.common.ValidGroupUpdate;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author XK on 2019/12/26
 */

@Data
public class MessageTemplateInfo extends BaseFormBean {
    /**
     * 消息内容
     */
    @Size(max = 79, message = "【消息内容】长度不超过79！", groups = { ValidGroupAdd.class, ValidGroupUpdate.class})
    private String content;

    /**
     * 停启状态
     */
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 50, message = "【备注】长度不超过50！", groups = {ValidGroupAdd.class, ValidGroupUpdate.class})
    private String remark;

}
