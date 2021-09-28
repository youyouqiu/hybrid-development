package com.cb.platform.domain.chat;

import lombok.Data;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/8
 */
@Data
public class UserGroup {
    /**
     * 组的id，唯一
     */
    private String userGroupId;
    /**
     * 组的名字
     */
    private String userGroupName;
}
