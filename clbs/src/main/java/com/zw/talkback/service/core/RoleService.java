package com.zw.talkback.service.core;

import com.alibaba.fastjson.JSONArray;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.RoleResource;
import com.zw.platform.domain.core.form.RoleForm;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.ibatis.annotations.Param;

import javax.naming.Name;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Title: 角色Service
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年8月12日下午2:17:02
 */
public interface RoleService {

    /**
     * @param id
     * @return List<Resource>
     * @throws @author wangying
     * @Title: 根据id查询角色的权限
     */
    List<Resource> getPermissionById(String id) throws Exception;

    /**
     * @param roleIds
     * @return List<RoleResource>
     * @throws @author wangying
     * @Title: 根据roleId查询角色和资源的关联
     */
    List<RoleResource> getRoleResourceByRoleId(List<String> roleIds);

    /**
     * 初始化admin权限首先删除admin权限
     * @param roleId
     * @return
     */
    boolean deleteByAdmin(@Param("roleId") String roleId);

    /**
     * @param list
     * @return boolean
     * @throws @author wangying
     * @Title: 批量新增
     */
    boolean addRoleResourceByBatch(List<RoleResourceForm> list) throws Exception;

    /**
     * @param roleId
     * @return boolean
     * @throws @author wangying
     * @Title: 根据id删除
     */
    boolean deleteByRole(String roleId) throws Exception;

    /**
     * @param roleId
     * @return boolean
     * @throws @author wangying
     * @Title: 根据id删除
     */
    boolean deleteByRoleResource(String roleId, String resuorceId) throws Exception;

    /**
     * @return List<String>
     * @throws @author wangying
     * @Title: 根据roleId 查询
     */
    List<RoleResource> findIdByRoleId(String roleId) throws Exception;

    /**
     * @param roleList
     * @return int
     * @throws
     * @Title: 根据role查询是否有权限
     * @author wangying
     */
    List<String> getMenuEditableByRoles(List<String> roleList) throws Exception;

    /**
     * @param roleList
     * @return int
     * @throws
     * @Title: 根据role加载菜单
     * @author Fan Lu
     */
    List<Resource> getMenuByRoles(List<String> roleList) throws Exception;

    /**
     * @param roleList
     * @return int
     * @throws @author Fan Lu
     * @Title:
     */
    Integer countMenuEditableByRoles(List<String> roleList, String resourceId);

    /**
     * @param map
     * @return int
     * @throws
     * @Title: 获取role_id
     * @author Fan Lu
     */
    List<String> getIdByNameAndType(Map<String, String> map);

    /**
     * 获取角色权限菜单树
     * @param roleId
     * @return
     */
    JSONArray generateTree(String roleId);

    JSONArray getRoleUserTree(Set<Name> roleMembers);

    /**
     * 新增角色
     * @param form
     * @param permissionTree
     * @return
     */
    JsonResultBean addRole(RoleForm form, String permissionTree, String ipAddress) throws Exception;
}