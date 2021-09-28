package com.cb.platform.service;

import com.cb.platform.util.ChatOpCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.EnumMap;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
@Component
public class ChatActionMap {
    private static final EnumMap<ChatOpCode, ChatAction> chatActionMap = new EnumMap<>(ChatOpCode.class);

    @Autowired
    @Resource(name = "chatActionGetUserGroupImpl")
    private ChatAction chatActionGetUserGroup;

    @Autowired
    @Resource(name = "chatActionGetGroupListImpl")
    private ChatAction chatActionGetGroupList;

    @Autowired
    @Resource(name = "chatActionGetUserListImpl")
    private ChatAction chatActionGetUserList;

    @Autowired
    @Resource(name = "chatActionGetUserImpl")
    private ChatAction chatActionGetUser;

    @PostConstruct
    private void init() {
        chatActionMap.put(ChatOpCode.GET_USER_GROUP, chatActionGetUserGroup);
        chatActionMap.put(ChatOpCode.GET_USER_GROUP_LIST, chatActionGetGroupList);
        chatActionMap.put(ChatOpCode.GET_USER_LIST, chatActionGetUserList);
        chatActionMap.put(ChatOpCode.GET_USER, chatActionGetUser);
    }

    public static ChatAction getChatAction(String opCode) {
        return chatActionMap.get(ChatOpCode.find(opCode));
    }
}
