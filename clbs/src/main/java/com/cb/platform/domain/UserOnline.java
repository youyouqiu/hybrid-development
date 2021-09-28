package com.cb.platform.domain;

import java.io.Serializable;
import java.util.Date;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;


@Data
public class UserOnline extends BaseFormBean implements Serializable {

    private String userId; // 用户id

    private String groupId; // 组织id

    private Date onlineTime; // 上线时间

    private Date offlineTime; // 下线时间

    private long onlineDuration; //  在线时间
}
