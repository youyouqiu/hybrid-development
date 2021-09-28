package com.zw.talkback.domain.intercom.info;

import lombok.Data;

/**
 *   云调度员管理
 */
@Data
public class IntercomIotUserInfo {
    private String password;

    private int onlineStatus;

    private String loginName;

    private int attributes;

    private String userName;

    private int userId;

    private int userNumber;
}
