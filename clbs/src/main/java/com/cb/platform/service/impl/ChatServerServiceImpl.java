package com.cb.platform.service.impl;

import com.alibaba.fastjson.JSON;
import com.cb.platform.domain.chat.ChatRequest;
import com.cb.platform.domain.chat.ChatResponse;
import com.cb.platform.service.ChatServerService;
import com.cb.platform.service.ChatActionMap;
import com.zw.platform.commons.SystemHelper;
import org.springframework.stereotype.Service;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
@Service
public class ChatServerServiceImpl implements ChatServerService {

    @Override
    public ChatResponse handle(String opCode, String body) {
        String userName = SystemHelper.getCurrentUsername();
        return doActions(opCode, body, userName);
    }

    private ChatResponse doActions(String opCode, String body, String userName) {
        ChatRequest request = JSON.parseObject(body, ChatRequest.class);
        return ChatActionMap.getChatAction(opCode).doAction(request, userName);
    }
}
