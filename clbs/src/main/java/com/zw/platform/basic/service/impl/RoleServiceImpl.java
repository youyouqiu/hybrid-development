package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.zw.lkyw.domain.ReportConstant;
import com.zw.lkyw.domain.ReportMenu;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.ldap.mapper.GroupContextMapper;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.GroupRepo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.Resource;
import com.zw.platform.domain.core.RoleResource;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.core.form.RoleResourceForm;
import com.zw.platform.repository.core.RoleDao;
import com.zw.platform.service.core.ResourceService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MethodLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.BaseLdapNameAware;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: 角色类
 * @date 2020/9/2915:51
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService, BaseLdapNameAware {

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private LogSearchService logSearchService;

    private LdapName baseLdapPath;

    @Value("${experience.role.entryDN}")
    private String roleEntryDn;

    @Override
    public void setBaseLdapPath(LdapName baseLdapPath) {
        this.baseLdapPath = baseLdapPath;
    }

    public LdapName getBaseLdapPath() {
        return baseLdapPath;
    }

    @Override
    public Collection<Group> getByMemberName(Name memberName) {
        //用户角色
        return groupRepo.findByMember(memberName);
    }

    @Override
    public Collection<Group> getByMemberNameStr(String memberName) {
        //用户角色
        Name name = LdapUtils.newLdapName(memberName + "," + getBaseLdapPath().toString());
        return groupRepo.findByMember(name);
    }

    /**
     * 根据role查询是否有权限
     */
    @MethodLog(name = "根据role查询是否有权限", description = "根据role查询是否有权限")
    @Override
    public List<String> getMenuEditableByRoles(List<String> roleList) throws Exception {
        if (CollectionUtils.isEmpty(roleList)) {
            return new ArrayList<>(1);
        }
        return roleDao.getMenuEditableByRoles(roleList);
    }

    @Override
    public List<Resource> getMenuByRoles(List<String> roleList) throws Exception {
        if (CollectionUtils.isEmpty(roleList)) {
            return new ArrayList<>(1);
        }
        return roleDao.getMenuByRoles(roleList);
    }

    @Override
    public Set<String> listMenuIdByRoleId(Collection<String> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return new HashSet<>();
        }
        return roleDao.listResourceIdByRoleId(roleIds);
    }

    @Override
    public void ldapGroupReferences(Collection<Group> groups, Name originalId, Name newId) {
        for (Group group : groups) {
            group.removeMember(originalId);
            group.addMember(newId);
            // 懒更新机制 必须要显示调用 next()方法, 可以看该方法'groupRepo.save(Iterable<S> entities)'逻辑
            groupRepo.save(group);
        }
    }

    @Override
    public void deleteUser(String roleDn, Name user) {
        Name groupDn = buildGroupDn(roleDn);
        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.removeAttributeValue("member", user);
        ldapTemplate.modifyAttributes(ctx);
    }

    private Name buildGroupDn(String groupName) {
        return LdapNameBuilder.newInstance("ou=Groups").add("cn", groupName).build();
    }

    /**
     * 根据关键字模糊查询角色
     * @param searchParam searchParam
     * @return result
     * @author xiaoyun
     */
    @Override
    public List<Group> getListByKeyword(String searchParam, boolean checkAdminRole) {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=Groups";
        String[] returnedAtts = { "o", "createTimestamp", "cn", "description" };
        searchCtls.setReturningAttributes(returnedAtts);
        AndFilter andfilter = new AndFilter();
        andfilter.and(new EqualsFilter("objectclass", "groupOfNames"));
        // 如果用户是admin用户 admin 可以查询所有
        if (checkAdminRole && !judgeAdminRole()) {
            LdapName name =
                LdapUtils.newLdapName(SystemHelper.getCurrentUser().getId() + "," + baseLdapPath.toString());
            andfilter.and(new EqualsFilter("member", name.toString()));
        }
        if (StringUtils.isNotBlank(searchParam)) {
            OrFilter orfilter = new OrFilter();
            orfilter.or(new LikeFilter("o", "*" + searchParam + "*"));
            andfilter.append(orfilter);
        }
        List<Group> groups = ldapTemplate.search(searchBase, andfilter.encode(), searchCtls, new GroupContextMapper());
        if (groups != null && !groups.isEmpty()) {
            // 角色列表排序
            groups.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return groups;
        }
        return null;
    }

    /**
     * 校验用户是否是ADMIN权限
     * @return boolean
     * @author wangying
     */
    public boolean judgeAdminRole() {
        boolean adminFlag = false;
        //获取用户的Dn
        String userDn = SystemHelper.getCurrentUserDn();
        // 根据用户id获取角色 ROLE_ADMIN
        Name name = LdapUtils.newLdapName(userDn + "," + baseLdapPath.toString());
        Collection<Group> userGroup = getByMemberName(name);
        // 判断用户是否有admin权限
        if (userGroup != null && userGroup.size() > 0) {
            for (Group group : userGroup) {
                if ("ROLE_ADMIN".equals(group.getName())) {
                    adminFlag = true;
                    break;
                }
            }
        }
        return adminFlag;
    }

    /**
     * 获取所有的角色
     * @return
     */
    @Override
    public List<Group> getAllGroup() {
        return (List<Group>) groupRepo.findAll();
    }

    /**
     * 删除禁言角色
     * @param name
     */
    @Override
    public void deleteSilentRole(LdapName name) {
        Group groupRole = getListByKeyword("禁言角色", false).get(0);
        groupRepo.removeMemberFromGroup(groupRole.getName() + "", name);
    }

    /**
     * 从用户组移除用户
     * @param groupName
     * @param user
     * @author FanLu
     */
    @Override
    public void removeMemberFromGroup(String groupName, Name user) {
        Name groupDn = buildGroupDn(groupName);
        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.removeAttributeValue("member", user);

        ldapTemplate.modifyAttributes(ctx);
    }

    /**
     * 添加用户到用户组
     * @param groupName
     * @param user
     * @author FanLu
     */
    @Override
    public void addMemberToGroup(String groupName, Name user) {
        Name groupDn = buildGroupDn(groupName);
        DirContextOperations ctx = ldapTemplate.lookupContext(groupDn);
        ctx.addAttributeValue("member", user);

        ldapTemplate.modifyAttributes(ctx);
    }

    /**
     * 通过角色名称查询 拥有该角色的成员列表;
     * @param roleCn 角色 cn
     */
    @Override
    public List<LdapName> getMemberNameListByRoleCn(String roleCn) {
        List<LdapName> result = new ArrayList<>();
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String searchBase = "ou=Groups";
        String[] returningAttributes = { "member" };
        searchControls.setReturningAttributes(returningAttributes);
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectclass", "groupOfNames"));
        filter.and(new EqualsFilter("cn", roleCn));
        ldapTemplate.search(searchBase, filter.encode(), searchControls, (ContextMapper<Group>) ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            SortedSet<String> memberList = context.getAttributeSortedStringSet("member");
            for (String member : memberList) {
                result.add(LdapUtils.newLdapName(member));
            }
            return null;
        });
        return result;
    }

    @Override
    public Group getGroupById(String roleDn) {
        return groupRepo.findOne(LdapUtils.newLdapName(roleDn));
    }

    @Override
    public JSONArray getRoleUserTree(Set<Name> roleMembers) {
        JSONArray jsonArray = new JSONArray();
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> organizations = userService.getCurrentUseOrgList();
        //获取组织下的所有用户
        Map<String, List<UserDTO>> userList = getUserBeanByOrg(organizations);
        //获取当前用户信息
        UserDTO currentUserInfo = userService.getCurrentUserInfo();
        LdapName currentUserName = LdapUtils.newLdapName(currentUserInfo.getId() + "," + getBaseLdapPath().toString());
        jsonArray.addAll(JsonUtil.getUserTree(userList, roleMembers, currentUserName));
        jsonArray.addAll(JsonUtil.getOrgTree(organizations, null));
        return jsonArray;
    }

    private Map<String, List<UserDTO>> getUserBeanByOrg(List<OrganizationLdap> organizations) {
        Map<String, List<UserDTO>> map = new HashMap<>();
        for (OrganizationLdap group : organizations) {
            List<UserDTO> list = userService.getUserByOrgDn(group.getId().toString(), SearchScope.ONELEVEL);
            if (list.size() == 0) {
                continue;
            }
            map.put(group.getCid(), list);
        }
        return map;
    }

    @Override
    public JSONArray generateTree(String roleDn) {
        //获取当前用户所拥有角色
        List<Group> roles = (List<Group>) getByMemberNameStr(userService.getCurrentUserInfo().getId().toString());
        List<String> roleDns = roles.stream().map(o -> o.getId().toString()).collect(Collectors.toList());

        //查询当前用户拥有的菜单权限
        List<Resource> resources;
        boolean isAdmin = userService.isAdminRole();
        if (isAdmin) {
            // 所有权限
            resources = resourceService.findAll();
        } else {
            resources = resourceService.findResourceListByRoleIds(roleDns);
        }
        // 角色勾选集合
        List<RoleResource> isChecked = new ArrayList<>();
        // 查询角色勾选的角色ID集合
        List<String> checkedRoleIds = new ArrayList<>();
        if (StringUtils.isNotBlank(roleDn)) {
            checkedRoleIds.add(roleDn);
        }
        // 查询当前角色勾选信息
        if (checkedRoleIds.size() > 0) {
            isChecked = getRoleResourceByRoleId(checkedRoleIds);
        }
        // 查询当前用户下所有角色勾选信息（除当前角色）
        List<RoleResource> allChecked = new ArrayList<>();
        // 查询当前用户下所有角色勾选信息
        if (roleDns.size() > 0) {
            allChecked = getRoleResourceByRoleId(roleDns);
        }

        // 组装用户权限菜单
        JSONArray result = new JSONArray();
        for (Resource resource : resources) {
            // 除去用户管理和角色管理
            JSONObject obj = new JSONObject();
            obj.put("id", resource.getId());
            obj.put("pId", resource.getParentId());
            obj.put("name", resource.getResourceName());

            // 组装可写子菜单,type = 0： 叶子菜单
            JSONObject editObj = new JSONObject();
            // 监控对象不允许分配可写权限（车，人，物）默认超级管理员和普通管理员拥有权限
            if (resource.getType() == 0) {
                // 组装可写
                editObj.put("id", resource.getId() + "edit");
                editObj.put("pId", resource.getId());
                editObj.put("type", "premissionEdit");
                editObj.put("name", "可写");
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

    /**
     * 根据roleDn获取角色和资源的关联关系
     * @param roleDns roleDns
     * @return List<RoleResource>
     */
    private List<RoleResource> getRoleResourceByRoleId(List<String> roleDns) {
        List<RoleResource> list;
        if (roleDns != null && roleDns.size() > 0) {
            list = roleDao.getRoleResourceByRoleId(roleDns);
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

    @Override
    public boolean compareUserForm(List<RoleResourceForm> formList) {
        boolean flag = true;
        String userDn = userService.getCurrentUserInfo().getId().toString();
        Collection<Group> groups = getByMemberNameStr(userDn);
        List<String> roleIds = groups.stream().map(o -> o.getId().toString()).collect(Collectors.toList());
        List<String> addList = formList.stream().map(RoleResourceForm::getResourceId).collect(Collectors.toList());
        List<String> curResources = resourceService.findResourceByRoleIds(roleIds);
        int i = 0;
        int size = addList.size();
        for (String addValue : addList) {
            for (String curResource : curResources) {
                if (addValue.equals(curResource)) {
                    i++;
                    break;
                }
            }
        }
        if (size == i) {
            flag = false;
        }
        return flag;
    }

    @Override
    public void addRole(Group group) {
        Name dn = buildGroupDn(group.getName());
        DirContextAdapter context = new DirContextAdapter(dn);
        LdapName ldapName = LdapUtils.newLdapName("uid=admin,ou=organization");
        LdapName userDn = LdapNameBuilder.newInstance(baseLdapPath).add(ldapName).build();
        context.setAttributeValues("objectclass", new String[] { "groupOfNames", "top" });
        context.setAttributeValue("cn", group.getName());
        if (StringUtils.isNotBlank(group.getDescription())) {
            context.setAttributeValue("description", group.getDescription());
        }
        if (StringUtils.isNotBlank(group.getRoleName())) {
            context.setAttributeValue("o", group.getRoleName());
        }
        context.setAttributeValue("member", userDn);

        ldapTemplate.bind(context);
        // 获取到新增角色的名称
        String message = "新增角色 : " + group.getRoleName();
        String ipAddress = getIpAddress();
        // 把用户的IP和message作为参数传入
        logSearchService.addLog(ipAddress, message, "3", "", "-", "");
    }

    @Override
    public boolean addRoleResourceByBatch(List<RoleResourceForm> list) {
        if (list != null && list.size() > 0) {
            String userName = userService.getCurrentUserInfo().getUsername();
            for (RoleResourceForm form : list) {
                // 创建者
                form.setCreateDataUsername(userName);
                // 创建时间
                form.setCreateDataTime(new Date());
            }
            return roleDao.addRoleResourceByBatch(list);
        }
        return false;
    }

    @Override
    public void addAllotRole(String userId, String roleId) {
        try {
            List<Group> groupList = (List<Group>) getByMemberNameStr(userId);
            StringBuilder builder = new StringBuilder();
            for (Group group : groupList) {
                builder.append(group.getName()).append(",");
            }
            builder.append(roleId);
            userService.updateUserRole(userId, builder.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JsonResultBean updateRole(String groupId, Group group, String permissionEditTree) {
        JSONObject msg = new JSONObject();
        LdapName originalId = LdapUtils.newLdapName(groupId);
        // 根据LdapName查询角色信息实体
        Group existingGroup = groupRepo.findOne(originalId);
        if (existingGroup != null) {
            // 设置角色名
            if (StringUtils.isNotBlank(group.getRoleName())) {
                existingGroup.setRoleName(group.getRoleName());
            }
            // 设置描述
            existingGroup.setDescription(group.getDescription());
            // 修改Ldap中的角色
            Group saveGroup = groupRepo.save(existingGroup);
            // 修改角色名和描述成功后,修改角色的分组操作权限
            if (saveGroup != null) {
                // 修改角色与Resource关联
                String newRoleId = saveGroup.getId().toString();
                // 先删除
                boolean delFlag = true;
                // 根据id查询角色的所有分组操作权限
                List<RoleResource> ids = roleDao.findIdByRoleId(groupId);
                if (ids != null && ids.size() > 0) {
                    // 删除角色的分组操作权限
                    delFlag = roleDao.deleteByRoleId(groupId);
                }
                // 再添加
                List<RoleResourceForm> formList = new ArrayList<RoleResourceForm>();
                if (delFlag) {
                    // 用户所选可写权限
                    if (permissionEditTree != null && !permissionEditTree.isEmpty()) {
                        JSONArray resourceArray = JSON.parseArray(permissionEditTree);
                        for (Object obj : resourceArray) {
                            String id = (String) ((JSONObject) obj).get("id");
                            // 是否可写
                            boolean edit = (boolean) ((JSONObject) obj).get("edit");
                            RoleResourceForm roleResource = new RoleResourceForm();
                            roleResource.setRoleId(newRoleId);
                            roleResource.setResourceId(id);
                            roleResource.setEditable(0);
                            if (edit) {
                                roleResource.setEditable(1);
                            }
                            formList.add(roleResource);
                        }

                    }
                }
                if (CollectionUtils.isNotEmpty(formList)) {
                    roleDao.addRoleResourceByBatch(formList);
                }
                // 获取被修改角色的ID
                String message = "修改角色 : " + group.getRoleName();
                // 修改角色时记录日志
                logSearchService.addLog(getIpAddress(), message, "3", "", "-", "");
                msg.put("flag", 1);
                msg.put("errMsg", "保存成功！");
                return new JsonResultBean(msg);
            }
        }
        msg.put("flag", 2);
        return new JsonResultBean(msg);
    }

    @Override
    public String deleteGroup(String groupId) {
        String[] roleIds = groupId.split(";");
        // 用户操作
        StringBuilder message = new StringBuilder();
        StringBuilder deleteMessage = new StringBuilder();
        StringBuilder notDeleteMessage = new StringBuilder();
        // 待删除roleId
        List<String> deleteRoleIds = new ArrayList<>();
        for (String roleId : roleIds) {
            if (StringUtils.isNotBlank(roleId) && (!"cn=ROLE_ADMIN,ou=Groups".equals(roleId))) {
                if (roleEntryDn.equals(roleId)) {
                    // 即刻体验: 角色不能被删除
                    message.append("即刻体验: 角色不能被删除").append(" <br/>");
                    continue;
                }
                String roleName;
                // 根据id查询角色
                Group g = getGroupById(roleId);
                if (g == null) {
                    continue;
                }
                roleName = g.getRoleName();
                if (("调度员角色").equals(roleName) || "禁言角色".equals(roleName) || "监听角色".equals(roleName)) {
                    notDeleteMessage.append("角色 :【").append(g.getRoleName()).append("】").append("不能被删除 <br/>");
                    continue;

                }

                Set<Name> members = g.getMembers();
                if (members.size() > 2) {
                    deleteMessage.append("角色 :【").append(g.getRoleName()).append("】").append(" <br/>");
                    continue;
                }

                message.append("删除角色 : ").append(g.getRoleName()).append(" <br/>");
                ldapTemplate.unbind(roleId);
                deleteRoleIds.add(roleId);
            }
        }
        // 数据库删除角色与资源关联关系
        if (CollectionUtils.isNotEmpty(deleteRoleIds)) {
            roleDao.deleteBatchByRoleId(deleteRoleIds);
        }
        String ipAddress = getIpAddress();
        if (!message.toString().isEmpty()) {
            if (roleIds.length > 1) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量删除角色");
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
            }
        }
        if (deleteMessage.length() > 0) {
            deleteMessage.append("删除失败，存在其他用户拥有该角色");
        }
        return deleteMessage.toString();
    }

    @Override
    public Map<String, List<ReportMenu>> getUserIntersectionMenu() {
        UserLdap user = SystemHelper.getCurrentUser();
        LdapName name = LdapUtils.newLdapName(user.getId() + "," + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) groupRepo.findByMember(name);
        List<String> roleNames = new ArrayList<>();
        for (Group role : roles) {
            roleNames.add(role.getId().toString());
        }
        Set<String> reportMenu = roleDao.getReportMenuByRolesAndReportIds(roleNames, ReportConstant.getReportIdSet());
        return ReportConstant.getUserReportMenu(reportMenu);
    }

    @Override
    public boolean isGroupWritePower() {
        //获取当前用户所属角色列表
        UserLdap user = SystemHelper.getCurrentUser();
        LdapName name = LdapUtils.newLdapName(user.getId() + "," + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) groupRepo.findByMember(name);

        //获取分组管理的资源Id
        Map<String, String> groupMap = ImmutableMap.of("resourceName", "分组管理", "type", "0");
        List<String> resourceIds = roleDao.getIdByNameAndType(groupMap);
        if (resourceIds.isEmpty()) {
            log.error("未在资源表中找到分组管理的资源");
            return false;
        }

        //判断用户所拥有的角色是否有分组管理的写权限
        String resourceId = resourceIds.get(0);
        List<String> roleIds = roles.stream().map(role -> role.getId().toString()).collect(Collectors.toList());
        Integer isWrite = roleDao.countMenuEditableByRoles(roleIds, resourceId);
        return isWrite > 0;
    }
}
