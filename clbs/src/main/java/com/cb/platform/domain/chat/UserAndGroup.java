package com.cb.platform.domain.chat;

import lombok.Data;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
@Data
public class UserAndGroup {
    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户所属组织id
     */
    private String groupId;
}
