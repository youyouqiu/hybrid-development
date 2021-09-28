package com.zw.talkback.domain.intercom.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

/***
 @Author zhengjc
 @Date 2019/8/20 15:18
 @Description 调度员相关信息
 @version 1.0
 **/
@Data
public class IntercomIotUserForm extends BaseFormBean {
    public IntercomIotUserForm(String userName, String userId, String callNumber) {
        this.userName = userName;
        this.userId = Long.parseLong(userId);
        this.callNumber = Long.parseLong(callNumber);
    }

    public IntercomIotUserForm() {

    }

    private String userName;
    private Long userId;
    private Long callNumber;

}
