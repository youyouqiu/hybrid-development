package com.cb.platform.service.impl;

import com.cb.platform.domain.chat.ChatRequest;
import com.cb.platform.domain.chat.ChatResponse;
import com.cb.platform.domain.chat.User;
import com.cb.platform.domain.chat.UserAndGroup;
import com.cb.platform.repository.mysqlDao.ChatGroupUserDao;
import com.cb.platform.service.ChatAction;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.service.core.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chen Feng
 * @version 1.0 2018/5/17
 */
@Service("chatActionGetUserListImpl")
public class ChatActionGetUserListImpl implements ChatAction {

    @Autowired
    private ChatGroupUserDao chatGroupUserDao;

    @Autowired
    private UserService userService;

    @Override
    public ChatResponse doAction(ChatRequest request, String userName) {
        ChatResponse<List<User>> userListResp = new ChatResponse<>();
        List<User> users = getAllUserList(userName);
        if (users.isEmpty()) {
            return userListResp;
        }
        userListResp.setData(users);
        userListResp.setHOpCode(request.getHOpCode());
        return userListResp;
    }

    private List<User> getAllUserList(String userName) {
        UserBean userBean = userService.getUserDetails(userName);
        if (userBean == null) {
            return new ArrayList<>();
        }
        List<UserAndGroup> userAndGroups = chatGroupUserDao.getAllUserGroups();
        // 去重的用户uuid列表
        Set<String> userIdsSet = getUserIdsSet(userAndGroups);
        // 用户uuid与该用户所属讨论组的映射关系
        Map<String, List<String>> userGroupMap = getUserGroupMap(userAndGroups);
        return getAllChatUsers(userIdsSet, userGroupMap);
    }

    private Set<String> getUserIdsSet(List<UserAndGroup> userAndGroups) {
        Set<String> userIdsSet = new HashSet<>();
        for (UserAndGroup userAndGroup : userAndGroups) {
            userIdsSet.add(userAndGroup.getUserId());
        }
        return userIdsSet;
    }

    private Map<String, List<String>> getUserGroupMap(List<UserAndGroup> userAndGroups) {
        Map<String, List<String>> map = new HashMap<>();
        for (UserAndGroup userAndGroup : userAndGroups) {
            map.computeIfAbsent(userAndGroup.getUserId(), k -> new ArrayList<>()).add(userAndGroup.getGroupId());
        }
        return map;
    }

    private List<User> getAllChatUsers(Set<String> userIdsSet, Map<String, List<String>> userGroupMap) {
        List<User> users = new ArrayList<>();
        List<UserBean> allUsers = userService.findAllUserUUID();
        for (UserBean userBean : allUsers) {
            if (!userIdsSet.contains(userBean.getUuid())) {
                continue;
            }
            User user = new User();
            user.setUserId(userBean.getUuid());
            user.setUserRealName(userBean.getUsername());
            user.setUserGroupList(userGroupMap.get(userBean.getUuid()));
            users.add(user);
        }
        return users;
    }
}
