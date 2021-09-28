package com.zw.talkback.domain.basicinfo.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zhouzongbo on 2019/8/16 16:45
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FriendForm extends BaseFormBean {

    private Long userId;

    private Long friendId;

    private Integer type;

    private  String monitorType;
}