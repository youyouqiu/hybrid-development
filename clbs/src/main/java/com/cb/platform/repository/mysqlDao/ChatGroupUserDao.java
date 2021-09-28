package com.cb.platform.repository.mysqlDao;

import com.cb.platform.domain.ChatGroupUserDo;
import com.cb.platform.domain.chat.UserAndGroup;
import com.cb.platform.domain.chat.UserGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 */
public interface ChatGroupUserDao {

    /**
     * @param groupId 分组编号
     * @return
     * @throws Exception
     */
    List<ChatGroupUserDo> findByGroupId(final String groupId)throws  Exception;

    /**
     * 根据查询条件讨论组-用户编号
     * @param groupId
     * @return
     * @throws Exception
     */
    List<String> findGroupUserByGroupId(String groupId)throws  Exception;

    /**
     * 批量新增聊天分组信息
     * @param list
     * @return
     * @throws Exception
     */
    Integer insertBathChatGroup(@Param("list") final List<ChatGroupUserDo> list)throws  Exception;

    /**
     * 批量删除聊天分组信息
     * @param groupId
     * @return
     * @throws Exception
     */
    Integer delBathChatGroup(final String groupId)throws  Exception;

    /**
     * 获取包含指定用户的讨论组id列表
     * @param userId 指定的用户的id
     * @return 讨论组id列表
     */
    List<String> getGroupListByUserId(String userId);

    /**
     * 获取包含指定用户的讨论组信息
     * @param userId 指定的用户的id
     * @return 讨论组信息
     */
    List<UserGroup> getGroupInfoListByUserId(String userId);

    /**
     * 获取与指定用户在相同的讨论组下的所有用户
     * @param userId 指定的用户的id
     * @return 用户id列表
     */
    List<UserAndGroup> getRelatedUserIds(String userId);

    List<UserAndGroup> getAllUserGroups();

}
