package com.cb.platform.service.impl;

import com.cb.platform.domain.ChatGroupDo;
import com.cb.platform.domain.chat.ChatRequest;
import com.cb.platform.domain.chat.ChatResponse;
import com.cb.platform.domain.chat.UserGroup;
import com.cb.platform.repository.mysqlDao.ChatGroupDao;
import com.cb.platform.service.ChatAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/23
 */
@Service("chatActionGetUserGroupImpl")
public class ChatActionGetUserGroupImpl implements ChatAction {

    @Autowired
    public ChatGroupDao chatGroupDao;

    @Override
    public ChatResponse doAction(ChatRequest request, String userName) {
        String groupId = request.getUserGroupId();
        ChatResponse<UserGroup> response = new ChatResponse<>();
        if (groupId == null) {
            return response;
        }
        ChatGroupDo group = chatGroupDao.getChatGroupById(groupId);
        UserGroup userGroup = new UserGroup();
        userGroup.setUserGroupId(group.getGroupId());
        userGroup.setUserGroupName(group.getGroupName());
        response.setData(userGroup);
        response.setHOpCode(request.getHOpCode());
        return response;
    }
}
