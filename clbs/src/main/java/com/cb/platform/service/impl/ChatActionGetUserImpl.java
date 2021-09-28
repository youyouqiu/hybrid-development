package com.cb.platform.service.impl;

import com.cb.platform.domain.chat.ChatRequest;
import com.cb.platform.domain.chat.ChatResponse;
import com.cb.platform.domain.chat.User;
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
@Service("chatActionGetUserImpl")
public class ChatActionGetUserImpl implements ChatAction {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatGroupUserDao chatGroupUserDao;

    @Override
    public ChatResponse doAction(ChatRequest request, String userName) {
        ChatResponse<User> userResp = new ChatResponse<>();
        User user = getUser(userName);
        userResp.setData(user);
        userResp.setHOpCode(request.getHOpCode());
        return userResp;
    }

    private User getUser(String userName) {
        UserBean userBean = userService.getUserDetails(userName);
        List<String> groupList = chatGroupUserDao.getGroupListByUserId(userBean.getUuid());
        User user = new User();
        user.setUserId(userBean.getUuid());
        user.setUserRealName(userName);
        user.setUserGroupList(groupList);
        if (groupList.size() > 0) {
            user.setUserGroupTopId(groupList.get(0));
        }
        return user;
    }
}
