package com.zw.platform.repository.core;

import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.RoleResource;
import com.zw.platform.domain.core.form.RoleResourceForm;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色Dao
 */
public interface RoleDao {

    /**
     * @param id
     * @return List<Resource>
     * @throws @author wangying
     * @Title: 根据id查询角色的权限
     */
    List<Resource> getPermissionById(@Param("id") String id);

    /**
     * @param roleIds
     * @return List<RoleResource>
     * @throws @author wangying
     * @Title: 根据roleId查询角色和资源的关联
     */
    List<RoleResource> getRoleResourceByRoleId(@Param("roleIds") List<String> roleIds);

    /**
     * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 批量新增角色资源关联
     */
    boolean addRoleResourceByBatch(List<RoleResourceForm> list);

    /**
     * @param roleId
     * @return boolean
     * @throws @author wangying
     * @Title: 根据role删除关联关系
     */
    boolean deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 根据roleId集合，删除对应关联关系
     *
     * @param roleIds roleIds
     * @return boolean
     */
    boolean deleteBatchByRoleId(@Param("roleIds") Collection<String> roleIds);

    /**
     * 初始化admin权限首先删除admin权限
     *
     * @param roleId
     * @return
     */
    boolean deleteByAdmin(@Param("roleId") String roleId);

    /**
     * @param roleId
     * @return boolean
     * @throws @author wangying
     * @Title: 根据role删除关联关系
     */
    boolean deleteByRoleIdResource(@Param("roleId") String roleId, @Param("resuorceId") String resuorceId);

    boolean deleteByResourceId(@Param("resourceId") String resourceId);

    /**
     * @param roleId
     * @return String
     * @throws @author wangying
     * @Title: 根据roleId 查询
     */
    List<RoleResource> findIdByRoleId(@Param("roleId") String roleId);

    /**
     * @param roleList
     * @return int
     * @throws
     * @Title: 根据role查询是否有权限
     * @author wangying
     */
    List<String> getMenuEditableByRoles(@Param("roleList") List<String> roleList);

    /**
     * @param resourceId
     * @param roleList
     * @return int
     * @throws @author wangying
     * @Title: 统计数量
     */
    Integer countMenuEditableByRoles(@Param("roleList") List<String> roleList, @Param("resourceId") String resourceId);

    /**
     * @param roleList
     * @return int
     * @throws
     * @Title: 根据role加载菜单
     * @author fanlu
     */
    List<Resource> getMenuByRoles(@Param("roleList") List<String> roleList);

    /**
     * 查询用户指定报表中的权限报表
     *
     * @param roleList
     * @param reportIds
     * @return
     */
    Set<String> getReportMenuByRolesAndReportIds(@Param("roleList") List<String> roleList,
                                                 @Param("reportIds") Collection<String> reportIds);

    /**
     * @param map
     * @return int
     * @throws
     * @Title: 获取role_id
     * @author fanlu
     */
    List<String> getIdByNameAndType(Map<String, String> map);

    /**
     * 根据角色id查询菜单
     */
    Set<String> listResourceIdByRoleId(Collection<String> roleIds);
}