package com.cb.platform.service;

import com.cb.platform.domain.ChatGroupDo;
import com.cb.platform.domain.query.ChatGroupQuery;
import com.github.pagehelper.Page;

/**
 * 聊天记录业务接口
 */
public interface ChatGroupService {

    /**
     * 根据查询条件讨论组记录
     * @param query
     * @return
     * @throws Exception
     */
    Page<ChatGroupDo> findAll(ChatGroupQuery query);

    /**
     * 根据讨论组名称查询
     * @param query
     * @return
     * @throws Exception
     */
    ChatGroupDo findByName(ChatGroupQuery query)throws  Exception;


    /**
     * 根据聊天分组编号获取分组信息
     * @param chatGroupId
     * @return
     * @throws Exception
     */
    ChatGroupDo getChatGroup(String chatGroupId)throws  Exception;

    /**
     * 修改聊天分组
     * @param chatGroupDo
     * @return
     * @throws Exception
     */
    Integer updateChatGroup(ChatGroupDo chatGroupDo,String ipAddress)throws Exception;

    /**
     * 新增聊天分组
     * @param chatGroupDo
     * @return
     * @throws Exception
     */
    Integer saveChatGroup(ChatGroupDo chatGroupDo,String ipAddress)throws  Exception;

    /**
     * 根据分组编号批量删除聊天分组信息
     * @param groupIds
     * @return
     * @throws Exception
     */
    Integer delBathChatGroup(String groupIds,String ipAddress)throws Exception;

    /**
     * 根据分组编号批量删除聊天分组信息
     * @param groupId
     * @return
     * @throws Exception
     */
    Integer delChatGroup(String groupId,String ipAddress)throws Exception;

}
