package com.cb.platform.service.impl;

import com.cb.platform.domain.ChatGroupDo;
import com.cb.platform.domain.ChatGroupUserDo;
import com.cb.platform.domain.query.ChatGroupQuery;
import com.cb.platform.repository.mysqlDao.ChatGroupDao;
import com.cb.platform.service.ChatGroupService;
import com.cb.platform.service.ChatGroupUserService;
import com.github.pagehelper.Page;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.PageHelperUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 分组信息处理
 */
@Service
public class ChatGroupServiceImpl implements ChatGroupService {

    @Autowired
    public ChatGroupDao chatGroupDao;

    @Autowired
    public ChatGroupUserService chatGroupUserService;

    @Autowired
    public LogSearchService logSearchService;

    @Override
    public Page<ChatGroupDo> findAll(ChatGroupQuery query) {
        return PageHelperUtil.doSelect(query, () -> chatGroupDao.findByParam(query));
    }

    @Override
    public ChatGroupDo findByName(ChatGroupQuery query) throws Exception {
        return chatGroupDao.findByName(query);
    }

    @Override
    public ChatGroupDo getChatGroup(String chatGroupId) throws Exception {
        return chatGroupDao.getChatGroupById(chatGroupId);
    }

    @Override
    public Integer updateChatGroup(ChatGroupDo chatGroupDo, String ipAddress) throws Exception {
        //删除绑定的聊天用户
        chatGroupUserService.delBathChatGroup(chatGroupDo.getGroupId());
        //新增聊天用户
        insertChatGroupUser(chatGroupDo.getUserIds(), chatGroupDo.getGroupId());
        //添加日志信息
        logSearchService.addLog(ipAddress, "修改聊天分组信息:" + chatGroupDo.getGroupName(), "3", "", "-", "");
        //修改分组信息
        return chatGroupDao.updateChatGroup(chatGroupDo);
    }

    @Override
    public Integer saveChatGroup(ChatGroupDo chatGroupDo, String ipAddress) throws Exception {
        chatGroupDo.setGroupId(UUID.randomUUID().toString());
        //新增聊天用户
        insertChatGroupUser(chatGroupDo.getUserIds(), chatGroupDo.getGroupId());
        chatGroupDo.setCreateDataTime(new Date());
        chatGroupDo.setCreateDataUsername(SystemHelper.getCurrentUsername());
        //添加日志信息
        logSearchService.addLog(ipAddress, "创建聊天分组:" + chatGroupDo.getGroupName(), "3", "", "-", "");
        return chatGroupDao.insertChatGroup(chatGroupDo);
    }

    /**
     * 批量新增聊天用户
     * @param userStrids
     * @param groupId
     * @throws Exception
     */
    private void insertChatGroupUser(String userStrids, String groupId) throws Exception {
        //新增聊天用户
        if (StringUtils.isEmpty(userStrids)) {
            return;
        }
        String[] userids = userStrids.split(",");
        List<ChatGroupUserDo> userDoList = new ArrayList<>();
        ChatGroupUserDo userDo;
        for (String userid : userids) {
            userDo = new ChatGroupUserDo();
            userDo.setCreateDataTime(new Date());
            userDo.setCreateDataUsername(SystemHelper.getCurrentUsername());
            userDo.setGroupId(groupId);
            userDo.setId(UUID.randomUUID().toString());
            userDo.setUserId(userid);
            userDoList.add(userDo);
        }
        if (userDoList.size() > 0) {
            chatGroupUserService.insertBathChatGroup(userDoList);
        }
    }

    @Override
    public Integer delBathChatGroup(String groupIds, String ipAddress) throws Exception {
        String[] groupStrIds = groupIds.split(",");
        ChatGroupDo groupDo;
        for (String groupId : groupStrIds) {
            groupDo = chatGroupDao.getChatGroupById(groupId);
            //添加日志信息
            logSearchService
                .addLog(ipAddress, "删除聊天分组信息:" + (groupDo == null ? "未知分组" : groupDo.getGroupName()), "3", "", "-", "");
            // 删除绑定的聊天用户
            chatGroupUserService.delBathChatGroup(groupId);
            chatGroupDao.removeChatGroup(groupId);
        }
        return 1;
    }

    @Override
    public Integer delChatGroup(String groupId, String ipAddress) throws Exception {
        ChatGroupDo groupDo = chatGroupDao.getChatGroupById(groupId);
        logSearchService
            .addLog(ipAddress, "删除聊天分组信息:" + (groupDo == null ? "未知分组" : groupDo.getGroupName()), "3", "", "-", "");
        // 删除绑定的聊天用户
        chatGroupUserService.delBathChatGroup(groupId);
        return this.chatGroupDao.removeChatGroup(groupId);
    }
}
