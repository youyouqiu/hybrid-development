package com.cb.platform.domain.chat;

import lombok.Data;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/2
 */
@Data
public class ChatRequest {
    private String hOpCode;
    private String userGroupTopId;
    private String userGroupId;
    private String userName;
    private String userPassword;
    private String message;
}
