package com.cb.platform.service.impl;

import com.cb.platform.domain.chat.ChatRequest;
import com.cb.platform.domain.chat.ChatResponse;
import com.cb.platform.domain.chat.UserGroup;
import com.cb.platform.repository.mysqlDao.ChatGroupUserDao;
import com.cb.platform.service.ChatAction;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.service.core.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
@Service("chatActionGetGroupListImpl")
public class ChatActionGetGroupListImpl implements ChatAction {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatGroupUserDao chatGroupUserDao;

    @Override
    public ChatResponse doAction(ChatRequest request, String userName) {
        ChatResponse<List<UserGroup>> userGroupListResp = new ChatResponse<>();
        UserBean userBean = userService.getUserDetails(userName);
        List<UserGroup> userGroups = chatGroupUserDao.getGroupInfoListByUserId(userBean.getUuid());
        userGroupListResp.setData(userGroups);
        userGroupListResp.setHOpCode(request.getHOpCode());
        return userGroupListResp;
    }
}
