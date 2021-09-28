package com.zw.talkback.service.core.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.RoleResource;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.form.RoleForm;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.repository.core.RoleDao;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import com.zw.talkback.service.core.RoleService;
import com.zw.talkback.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * <p> Title: 角色管理ServiceImpl </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年8月12日上午10:19:56
 */
@Service
public class RoleServiceImpl2 implements RoleService {
    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    @Value("${requisite.null}")
    private String requisiteNull;

    @Value("${role.exist}")
    private String roleExist;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

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

        LdapName name = LdapUtils
            .newLdapName(SystemHelper.getCurrentUser().getId() + "," + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) userService.findByMember(name);
        List<String> roleIds = new ArrayList<String>();
        if (roles != null && roles.size() > 0) {
            for (Group role : roles) {
                roleIds.add(role.getId().toString());
            }
        }

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
                            if (reResource.getEditable() == 0 && !isAdmin) {
                                editObj.put("chkDisabled", true);
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

    @Override
    public Integer countMenuEditableByRoles(List<String> roleList, String resourceId) {

        return roleDao.countMenuEditableByRoles(roleList, resourceId);
    }

    @Override
    public JSONArray getRoleUserTree(Set<Name> roleMembers) {
        JSONArray jsonArray = new JSONArray();
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> organizations = getOrganizationLdaps();
        //获取组织下的所有用户
        Map<String, List<UserBean>> userList = getUserBeanByOrg(organizations);
        LdapName currentUserName = getCurrentLdapName();
        jsonArray.addAll(JsonUtil.getUserTree(userList, roleMembers, currentUserName));
        jsonArray.addAll(JsonUtil.getGroupTree(organizations, null, false));
        return jsonArray;
    }

    @Override
    public JsonResultBean addRole(RoleForm form, String permissionTree, String ipAddress) throws Exception {

        JSONObject msg = new JSONObject();
        // 0：失败 1： 通过 2：校验失败
        msg.put("flag", 2);
        // 校验数据
        if (StrUtil.isBlank(form.getRoleName())) {
            msg.put("errMsg", requisiteNull);
            return new JsonResultBean(msg);
        }
        // 校验角色名是否重复
        List<Group> groupByName = userService.getGroupByName(form.getRoleName());
        if (groupByName != null) {
            msg.put("errMsg", roleExist);
            return new JsonResultBean(msg);
        }
        // 生成cn
        String cn = "ROLE_" + UUID.randomUUID();
        // 生成id
        String roleId = "cn=" + cn + ",ou=Groups";
        List<RoleResourceForm> formList = new ArrayList<RoleResourceForm>();
        // 检测用户新增角色所选权限是否在用户角色范围内 gfw 20180904
        // 用户所选可写权限
        initRoleResources(permissionTree, roleId, formList);
        if (formList.size() > 0 && userService
            .compareUserForm(SystemHelper.getCurrentUser().getId().toString(), formList)) {
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

        // ldap 新增角色
        Group group = new Group();
        LdapName ldapName = LdapUtils.newLdapName(roleId);
        group.setId(ldapName);
        group.setName(cn);
        group.setDescription(form.getDescription());
        group.setRoleName(form.getRoleName());
        userService.addRole(group, ipAddress);
        if (formList.size() > 0) {
            addRoleResourceByBatch(formList);
        }

        msg.put("flag", 1);
        msg.put("errMsg", "保存成功！");

        // 新增角色自动分配到创建用户和admin用户  gfw 20180904
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 用户 添加角色 gfw 20180904
        userService.addAllotRole(userId, cn, ipAddress);
        // admin 添加角色 gfw 20180904
        String admin = userService.getUserDetails("admin").getId().toString();
        userService.addAllotRole(admin, cn, ipAddress);
        return new JsonResultBean(msg);
    }

    /**
     * 初始化角色的关联的菜单信息
     * @param permissionTree
     * @param roleId
     * @param formList
     */
    private void initRoleResources(String permissionTree, String roleId, List<RoleResourceForm> formList) {
        if (StrUtil.isNotBlank(permissionTree)) {
            JSONArray resourceArray = JSON.parseArray(permissionTree);
            for (Object obj : resourceArray) {
                RoleResourceForm roleResource = new RoleResourceForm();
                String id = (String) ((JSONObject) obj).get("id");
                // 是否可写
                boolean edit = (boolean) ((JSONObject) obj).get("edit");
                roleResource.setRoleId(roleId);
                roleResource.setResourceId(id);
                if (edit) {
                    roleResource.setEditable(1);
                } else {
                    roleResource.setEditable(0);
                }
                formList.add(roleResource);
            }
        }
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
