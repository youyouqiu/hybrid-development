package com.cb.platform.service.impl;

import com.cb.platform.domain.ChatGroupDo;
import com.cb.platform.domain.ChatGroupUserDo;
import com.cb.platform.domain.query.ChatGroupQuery;
import com.cb.platform.repository.mysqlDao.ChatGroupDao;
import com.cb.platform.repository.mysqlDao.ChatGroupUserDao;
import com.cb.platform.service.ChatGroupService;
import com.cb.platform.service.ChatGroupUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分组信息处理
 */
@Service
public class ChatGroupUserServiceImpl implements ChatGroupUserService {

    @Autowired
    public ChatGroupUserDao chatGroupUserDao;


    @Override
    public List<ChatGroupUserDo> findAll(String groupId) throws Exception {
        return chatGroupUserDao.findByGroupId(groupId);
    }

    @Override
    public List<String> findGroupUserByGroupId(String groupId) throws Exception {
        return this.chatGroupUserDao.findGroupUserByGroupId(groupId);
    }

    @Override
    public Integer delBathChatGroup(String groupId) throws Exception {
        return chatGroupUserDao.delBathChatGroup(groupId);
    }

    @Override
    public Integer insertBathChatGroup(List<ChatGroupUserDo> chatGroupUserDoList) throws Exception {
        return chatGroupUserDao.insertBathChatGroup(chatGroupUserDoList);
    }
}
