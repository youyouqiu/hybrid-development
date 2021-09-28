package com.zw.platform.basic.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.zw.platform.basic.dto.GroupDTO;
import org.apache.ibatis.annotations.Param;

import com.zw.platform.basic.domain.UserGroupDO;
import com.zw.platform.basic.dto.UserGroupDTO;
import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportTable;

/**
 * @author wanxing
 * @Title: 用户分组类
 * @date 2020/9/2510:40
 */
public interface UserGroupDao {

    /**
     *分配分组给用户
     * @param groups
     * @return
     */
    boolean addGroups2User(List<String> groups);


    /**
     *分配分组给用户
     * @param groups
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_USER)
    boolean batchAdd(@Param("list") List<UserGroupDO> groups);


    /**
     * 查询用户权限下的分组
     * @param userId
     * @param orgIds
     * @return
     */
    List<String> findUserGroupIds(@Param("userId") String userId, @Param("orgIds") List<String> orgIds);

    /**
     * 删除用户和分组的数据库关系
     * @param groupId
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_USER)
    boolean deleteByGroupId(@Param("groupId") String groupId);

    /**
     * 删除用户和分组的数据库关系
     * @param groupIds
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_ASSIGNMENT_USER)
    boolean deleteByGroupIds(@Param("groupIds") Collection<String> groupIds);


    /**
     * 通过分组Id查询用户Id
     * @param groupId
     * @return
     */
    List<String> getUserIdsByGroupId(@Param("groupId") String groupId);

    /**
     * 通过分组Id查询用户Id
     * @param groupIds
     * @return 集合
     */
    List<UserGroupDTO> getUserIdsByGroupIds(@Param("groupIds") Collection<String> groupIds);

    /**
     * 获取已经分配分组的用户
     * @param groupId
     * @param userIdList
     * @return
     */
    List<String> getAssignedUserIdByIdAndUserId(@Param("groupId") String groupId,
        @Param("userIdList") List<String> userIdList);

    /**
     * 删除用户-分组关系，通过分组Id和用户Id
     * @param groupId
     * @param userIds
     * @return
     */
    @ImportDaoLock(ImportTable.ZW_M_VEHICLE_INFO)
    boolean deleteUserGroupByUserIdAndGroupId(@Param("groupId") String groupId,
        @Param("userIds") Collection<String> userIds);

    /**
     * 通过组织Id获取组织下的分组绑定的用户Id
     * @param orgIds
     * @return
     */
    Set<String> getBingUserIdByOrgId(@Param("list") Collection<String> orgIds);

    /**
     * 删除用户-分组关系，通过userId
     * @param userId
     * @return
     */
    boolean deleteByUserId(@Param("userId") String userId);

    /**
     * 删除用户-分组关系 通过userId和分组Id
     * @param userId
     * @param ids
     * @return
     */
    boolean deleteUserGroupByUserAndGroupIds(@Param("userId") String userId, @Param("ids") Collection<String> ids);

    /**
     * 获取所有用户分组
     * @return
     */
    List<UserGroupDTO> getAll();

    /**
     * 通过用户UUID和企业Id
     * @param userId
     * @param userOrgListId
     * @return
     */
    List<GroupDTO> getByGroupIdsAndUserId(@Param("userId") String userId, @Param("orgIds") List<String> userOrgListId);

    /**
     * 批量删除，通过用户uuid
     * @param userIds
     * @return
     */
    boolean deleteByUserIds(@Param("userIds") Collection<String> userIds);

    /**
     * 根据车id查询分组
     */
    List<GroupDTO> getUserAssignmentByVehicleId(@Param("userId") String userId,
        @Param("vehicleIds") List<String> vehicleIds);
}
