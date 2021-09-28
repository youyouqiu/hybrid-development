package com.cb.platform.service;

import com.cb.platform.domain.chat.ChatRequest;
import com.cb.platform.domain.chat.ChatResponse;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
public interface ChatAction {
    ChatResponse doAction(ChatRequest request, String userName);
}
