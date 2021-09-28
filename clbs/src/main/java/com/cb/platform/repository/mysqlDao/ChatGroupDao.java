package com.cb.platform.repository.mysqlDao;

import com.cb.platform.domain.ChatGroupDo;
import com.cb.platform.domain.query.ChatGroupQuery;

import java.util.List;

/**
 *
 */
public interface ChatGroupDao {

    /**
     * @param query 分组名称
     * @return
     * @throws Exception
     */
    List<ChatGroupDo> findByParam(final ChatGroupQuery query);
    /**
     * @param query 分组名称
     * @return
     * @throws Exception
     */
    ChatGroupDo findByName(final ChatGroupQuery query)throws  Exception;

    /**
     * 根据分组编号获取分组信息
     * @param groupId
     * @return
     * @throws Exception
     */
    ChatGroupDo getChatGroupById(String groupId);

    /**
     * 新增聊天分组信息
     * @param chatGroupDo
     * @return
     * @throws Exception
     */
    Integer insertChatGroup(final ChatGroupDo chatGroupDo)throws  Exception;

    /**
     * 修改聊天分组信息
     * @param chatGroupDo
     * @return
     * @throws Exception
     */
    Integer updateChatGroup(final ChatGroupDo chatGroupDo)throws  Exception;

    /**
     * 删除聊天分组
     * @param groupId
     * @return
     * @throws Exception
     */
    Integer removeChatGroup(String groupId)throws Exception;

}
