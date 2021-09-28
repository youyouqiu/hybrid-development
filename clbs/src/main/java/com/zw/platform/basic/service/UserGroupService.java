package com.zw.platform.basic.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.dto.UserGroupDTO;
import org.apache.ibatis.annotations.Param;

import com.zw.platform.basic.domain.UserGroupDO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.util.common.BusinessException;

/**
 * @author wanxing
 * @Title: 用户分组类
 * @date 2020/9/2510:38
 */
public interface UserGroupService extends CacheService {

    /**
     * 分配分组给用户
     * @param userDn
     * @param groupIds
     * @return
     * @throws Exception
     */
    boolean addGroups2User(String userDn, String groupIds) throws Exception;

    /**
     * 更新用户分组关系
     * @param userId
     * @param userName
     * @param delList
     * @param addList
     * @throws Exception
     */
    void updateUserGroupIds(String userId, String userName, Collection<String> delList, Collection<String> addList)
        throws Exception;

    /**
     * 分配分组给用户
     * @param assignmentId
     * @param userList
     * @return
     * @throws Exception
     */
    boolean addGroup2User(String assignmentId, String userList) throws Exception;

    JSONArray distributeUserGroupTree(String userDn);

    Map<String, String> getGroupMap();

    /**
     * 查询用户权限下的分组
     * @param userId
     * @param orgIds
     * @return
     */
    List<String> findUserGroupIds(String userId, @Param("orgIds") List<String> orgIds);

    /**
     * 批量插入数据库
     * @param userGroupDos  userGroupDos
     */
    boolean batchAddToDb(List<UserGroupDO> userGroupDos);

    /**
     * 批量插入redis
     * @param userGroupList  userGroupDos
     */
    void batchAddToRedis(List<UserGroupDTO> userGroupList);

    /**
     * 通过组织Id获取组织下的分组绑定的用户Id
     * @param orgIds
     * @return
     */
    Set<String> getBingUserIdByOrgId(Collection<String> orgIds);

    /**
     * 获取分组授权给用户的组织树
     * @param groupId
     * @return
     */
    String getUserGroupTree(String groupId) throws BusinessException;

    /**
     * 删除用户-分组关系，通过userId
     * @param userId
     * @return
     */
    boolean deleteByUserId(String userId);

    /**
     * 获取所有的用户分组关系
     * @return
     */
    List<UserGroupDTO> getAll();

    /**
     * 获取用户的uuid
     * @param groupId 分组Id
     * @return
     */
    List<String> getUserIdsByGroupId(String groupId);

    /**
     * 通过分组id进行删除
     * @param id
     */
    void deleteByGroupId(String id);

    /**
     * 批量获取用户uuid
     * @param groupIds
     * @return
     */
    List<UserGroupDTO> getUserIdsByGroupIds(Collection<String> groupIds);

    /**
     * 批量删除分组
     * @param groupIds
     */
    void deleteByGroupIds(Collection<String> groupIds);

    /**
     * 获取当前用户的分组监控对象树
     * @param multiple
     * @param groupId
     * @param queryParam
     * @param queryType
     * @param result
     * @return
     */
    List<String> getCurrentUserGroupTree(String multiple, String groupId,
        String queryParam, String queryType, JSONArray result);

    /**
     * 批量删除通过用户uuid
     * @param userIds
     * @return
     */
    boolean deleteByUserIds(Collection<String> userIds);

    /**
     * 通过用户id和组织id获取分组
     * @param currentUserUuid
     * @param orgIdList
     * @return
     */
    List<GroupDTO> getByGroupIdsAndUserId(String currentUserUuid, List<String> orgIdList);

    /**
     * 根据车id查询分组
     */
    List<GroupDTO> getUserAssignmentByVehicleId(@Param("userUuid") String userUuid,
        @Param("vehicleIds") List<String> vehicleIds);
}
