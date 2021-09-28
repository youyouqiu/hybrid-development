package com.zw.platform.service.core.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.RoleResource;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.repository.core.RoleDao;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.service.core.RoleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.MethodLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p> Title: 角色管理ServiceImpl </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年8月12日上午10:19:56
 */
@Service("oldRoleService")
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    // 组织与用户的资源id
    private static final String USER_CODE = "186a9eac-855c-4c52-995c-8af38bd2453b";

    // 角色的资源id
    private static final String ROLE_CODE = "a9f91a9a-f29a-4380-8751-16e1af38c31f";

    /**
     * 根据id查询角色的权限
     */
    @MethodLog(name = "根据id查询角色的权限", description = "根据id查询角色的权限")
    public List<Resource> getPermissionById(String id) throws Exception {
        List<Resource> list = new ArrayList<Resource>();
        if (id != null) {
            list = roleDao.getPermissionById(id);
            return list;
        }
        return null;
    }

    /**
     * 初始化admin权限首先删除admin权限
     */
    public boolean deleteByAdmin(String roleId) {
        return roleDao.deleteByAdmin(roleId);
    }

    /**
     * 新增角色资源关联
     */
    @MethodLog(name = "新增角色资源关联", description = "新增角色资源关联")
    public boolean addRoleResourceByBatch(List<RoleResourceForm> list) throws Exception {
        if (list != null && list.size() > 0) {
            for (RoleResourceForm form : list) {
                form.setCreateDataUsername(SystemHelper.getCurrentUsername());
                // 创建者
                form.setCreateDataTime(new Date()); // 创建时间
            }
            return roleDao.addRoleResourceByBatch(list);
        }
        return false;
    }

    /**
     * 根据roleId删除
     */
    @MethodLog(name = "根据roleId删除", description = "根据roleId删除")
    public boolean deleteByRole(String roleId) throws Exception {
        String[] roleIds = roleId.split(";");
        for (int i = 0; i < roleIds.length; i++) {
            roleDao.deleteByRoleId(roleIds[i]);
        }
        return true;
    }

    /**
     * 根据roleId和resourceId删除
     */
    @MethodLog(name = "根据roleId删除", description = "根据roleId删除")
    public boolean deleteByRoleResource(String roleId, String resourceId) throws Exception {
        if (roleId != null && roleId != "" && resourceId != null && resourceId != "") {
            return roleDao.deleteByRoleIdResource(roleId, resourceId);
        }
        return false;
    }

    /**
     * 根据roleId查询
     */
    @MethodLog(name = "根据roleId查询", description = "根据roleId查询")
    public List<RoleResource> findIdByRoleId(String roleId) throws Exception {
        if (roleId != null && roleId != "") {
            return roleDao.findIdByRoleId(roleId);
        }
        return null;
    }

    /**
     * 根据roleId查询角色和资源的关联
     */
    @MethodLog(name = "根据roleId查询角色和资源的关联", description = "根据roleId查询角色和资源的关联")
    public List<RoleResource> getRoleResourceByRoleId(List<String> roleIds) {
        List<RoleResource> list = new ArrayList<RoleResource>();
        if (roleIds != null && roleIds.size() > 0) {
            list = roleDao.getRoleResourceByRoleId(roleIds);
            if (list != null && list.size() > 0) {
                Map<String, RoleResource> handleMap = new HashMap<>();
                for (RoleResource resource : list) {
                    RoleResource roleResource = handleMap.getOrDefault(resource.getResourceId(), new RoleResource());
                    roleResource.setResourceId(resource.getResourceId());
                    Integer editable = roleResource.getEditable();
                    if (editable == null || editable != 1) {
                        roleResource.setEditable(resource.getEditable());
                    }
                    handleMap.put(resource.getResourceId(), roleResource);
                }
                list.clear();
                list.addAll(handleMap.values());
            }
            return list;
        }
        return null;
    }

    /**
     * 根据role查询是否有权限
     */
    @MethodLog(name = "根据role查询是否有权限", description = "根据role查询是否有权限")
    public List<String> getMenuEditableByRoles(List<String> roleList) throws Exception {
        if (roleList != null && roleList.size() > 0) {
            return roleDao.getMenuEditableByRoles(roleList);
        }
        return null;
    }

    @Override
    public List<Resource> getMenuByRoles(List<String> roleList) throws Exception {
        if (roleList != null && roleList.size() > 0) {
            return roleDao.getMenuByRoles(roleList);
        }
        return null;
    }

    @Override
    public List<String> getIdByNameAndType(Map<String, String> map) {

        return roleDao.getIdByNameAndType(map);
    }

    @Override
    public JSONArray generateTree(String roleId) {
        // 修改角色权限时 权限列表只展示当前用户所具有的权限 gfw 20180905
        // List<Resource> resources = resourceService.findAll(); // 所有权限
        // 当前用户所拥有角色

        List<String> roleIds = getUserRoleIds();

        // 查询当前用户拥有的菜单权限
        List<Resource> resources;
        boolean isAdmin = userService.isADMINRole();
        if (isAdmin) {
            resources = resourceService.findAll(); // 所有权限
        } else {
            resources = resourceService.findResourceListByRoleIds(roleIds);
        }
        // 角色勾选集合
        List<RoleResource> isChecked = new ArrayList<>();
        // 查询角色勾选的角色ID集合
        List<String> checkedRoleIds = new ArrayList<>();
        if (StringUtils.isNotBlank(roleId)) {
            checkedRoleIds.add(roleId);
        }
        // 查询当前角色勾选信息
        if (checkedRoleIds.size() > 0) {
            isChecked = getRoleResourceByRoleId(checkedRoleIds);
        }
        // 查询当前用户下所有角色勾选信息（除当前角色）
        List<RoleResource> allChecked = new ArrayList<>();
        // 查询当前用户下所有角色勾选信息
        if (roleIds.size() > 0) {
            allChecked = getRoleResourceByRoleId(roleIds);
        }

        // 组装用户权限菜单
        JSONArray result = new JSONArray();
        for (Resource resource : resources) {
            // 除去用户管理和角色管理
            JSONObject obj = new JSONObject();
            obj.put("id", resource.getId());
            obj.put("pId", resource.getParentId());
            obj.put("name", resource.getResourceName());
            // if (USER_CODE.equals(resource.getId()) || ROLE_CODE.equals(resource.getId())) {
            //     obj.put("chkDisabled", true);
            // }
            // 组装可写子菜单,type = 0： 叶子菜单
            JSONObject editObj = new JSONObject();
            // 监控对象不允许分配可写权限（车，人，物）默认超级管理员和普通管理员拥有权限
            if (resource.getType() == 0) {
                // 组装可写
                editObj.put("id", resource.getId() + "edit");
                editObj.put("pId", resource.getId());
                editObj.put("type", "premissionEdit");
                editObj.put("name", "可写");
                // if (USER_CODE.equals(resource.getId()) || ROLE_CODE.equals(resource.getId())) {
                //     editObj.put("chkDisabled", true);
                // }
            }
            // 判断当前角色勾选（仅当修改时有效）
            if (isChecked.size() > 0) {
                for (RoleResource reResource : isChecked) {
                    if (resource.getId().equals(reResource.getResourceId())) {
                        obj.put("checked", true);
                        if (resource.getType() == 0) {
                            // 若为最下级子菜单&可写框处于没有勾选状态&不为admin用户则限制该勾选框
                            if (reResource.getEditable() != 0) {
                                editObj.put("checked", true);
                            } else if (!isAdmin) {
                                editObj.put("chkDisabled", true);
                            }
                        }
                    }
                }
            }
            // 判断用户权限下所有角色勾选/
            if (allChecked.size() > 0) {
                for (RoleResource reResource : allChecked) {
                    if (resource.getId().equals(reResource.getResourceId())) {
                        if (resource.getType() == 0) {
                            // 若为最下级子菜单&可写框处于没有勾选状态&不为admin用户则限制该勾选框
                            // 并且当前用户其他角色权限下也没有改菜单的可写权限, 则该菜单的可写权限被禁用
                            Boolean chkDisabled = editObj.getBoolean("chkDisabled");
                            Integer editable = reResource.getEditable();
                            if (editable == 0) {
                                if (!isAdmin) {
                                    editObj.put("chkDisabled", true);
                                }
                            } else if (editable == 1 && Objects.nonNull(chkDisabled) && chkDisabled) {
                                // 如果当前用户权限下的角色拥有某个菜单的可写权限, 但是当前正在被修改的角色没有该菜单,则该菜单的可写不被禁用
                                editObj.put("chkDisabled", false);
                            }
                        }
                    }
                }
            }

            result.add(obj);
            if (editObj.size() > 0) {
                result.add(editObj);
            }

        }
        return result;
    }

    private List<String> getUserRoleIds() {
        LdapName name = LdapUtils
            .newLdapName(SystemHelper.getCurrentUser().getId() + "," + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) userService.findByMember(name);
        List<String> roleIds = new ArrayList<String>();
        if (roles != null && roles.size() > 0) {
            for (Group role : roles) {
                roleIds.add(role.getId().toString());
            }
        }
        return roleIds;
    }

    @Override
    public Integer countMenuEditableByRoles(List<String> roleList, String resourceId) {

        return roleDao.countMenuEditableByRoles(roleList, resourceId);
    }

    private List<OrganizationLdap> getOrganizationLdaps() {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        return userService.getOrgChild(userService.getOrgIdByUserId(userId));
    }

    private LdapName getCurrentLdapName() {
        return LdapUtils
            .newLdapName(SystemHelper.getCurrentUser().getId() + "," + userService.getBaseLdapPath().toString());
    }

    private Map<String, List<UserBean>> getUserBeanByOrg(List<OrganizationLdap> organizations) {
        Map<String, List<UserBean>> map = new HashMap<>();
        for (OrganizationLdap group : organizations) {
            List<UserBean> list = userService.listUserByOrgId(group.getId().toString());
            if (list.size() == 0) {
                continue;
            }
            map.put(group.getCid(), list);
        }
        return map;
    }
}
