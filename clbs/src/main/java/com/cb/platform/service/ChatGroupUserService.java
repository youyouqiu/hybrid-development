package com.cb.platform.service;

import com.cb.platform.domain.ChatGroupUserDo;

import java.util.List;

/**
 * 聊天记录业务接口
 */
public interface ChatGroupUserService {

    /**
     * 根据查询条件讨论组-用户记录
     * @param groupId
     * @return
     * @throws Exception
     */
     List<ChatGroupUserDo> findAll(String groupId)throws  Exception;

    /**
     * 根据查询条件讨论组-用户编号
     * @param groupId
     * @return
     * @throws Exception
     */
    List<String> findGroupUserByGroupId(String groupId)throws  Exception;

    /**
     * 批量删除聊天分组-用户
     * @param groupId
     * @return
     * @throws Exception
     */
    Integer delBathChatGroup(String groupId)throws Exception;

    /**
     * 批量新增聊天分组-用户
     * @param chatGroupUserDoList
     * @return
     * @throws Exception
     */
    Integer insertBathChatGroup(List<ChatGroupUserDo> chatGroupUserDoList)throws  Exception;

}
