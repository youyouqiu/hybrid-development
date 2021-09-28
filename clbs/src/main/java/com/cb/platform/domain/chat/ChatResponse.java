package com.cb.platform.domain.chat;

import com.cb.platform.util.ChatOpCode;
import lombok.Data;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
@Data
public class ChatResponse<T> {
    private String hOpCode = ChatOpCode.UC_ERROR.toString();
    private String tokenId;
    private T data;
}
