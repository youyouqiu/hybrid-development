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
 * @Title: ?????????
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
        //????????????
        return groupRepo.findByMember(memberName);
    }

    @Override
    public Collection<Group> getByMemberNameStr(String memberName) {
        //????????????
        Name name = LdapUtils.newLdapName(memberName + "," + getBaseLdapPath().toString());
        return groupRepo.findByMember(name);
    }

    /**
     * ??????role?????????????????????
     */
    @MethodLog(name = "??????role?????????????????????", description = "??????role?????????????????????")
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
            // ??????????????? ????????????????????? next()??????, ??????????????????'groupRepo.save(Iterable<S> entities)'??????
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
     * ?????????????????????????????????
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
        // ???????????????admin?????? admin ??????????????????
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
            // ??????????????????
            groups.sort((o1, o2) -> o2.getCreateTimestamp().compareTo(o1.getCreateTimestamp()));
            return groups;
        }
        return null;
    }

    /**
     * ?????????????????????ADMIN??????
     * @return boolean
     * @author wangying
     */
    public boolean judgeAdminRole() {
        boolean adminFlag = false;
        //???????????????Dn
        String userDn = SystemHelper.getCurrentUserDn();
        // ????????????id???????????? ROLE_ADMIN
        Name name = LdapUtils.newLdapName(userDn + "," + baseLdapPath.toString());
        Collection<Group> userGroup = getByMemberName(name);
        // ?????????????????????admin??????
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
     * ?????????????????????
     * @return
     */
    @Override
    public List<Group> getAllGroup() {
        return (List<Group>) groupRepo.findAll();
    }

    /**
     * ??????????????????
     * @param name
     */
    @Override
    public void deleteSilentRole(LdapName name) {
        Group groupRole = getListByKeyword("????????????", false).get(0);
        groupRepo.removeMemberFromGroup(groupRole.getName() + "", name);
    }

    /**
     * ????????????????????????
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
     * ????????????????????????
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
     * ???????????????????????? ??????????????????????????????;
     * @param roleCn ?????? cn
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
        // ?????????????????????????????????????????????
        List<OrganizationLdap> organizations = userService.getCurrentUseOrgList();
        //??????????????????????????????
        Map<String, List<UserDTO>> userList = getUserBeanByOrg(organizations);
        //????????????????????????
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
        //?????????????????????????????????
        List<Group> roles = (List<Group>) getByMemberNameStr(userService.getCurrentUserInfo().getId().toString());
        List<String> roleDns = roles.stream().map(o -> o.getId().toString()).collect(Collectors.toList());

        //???????????????????????????????????????
        List<Resource> resources;
        boolean isAdmin = userService.isAdminRole();
        if (isAdmin) {
            // ????????????
            resources = resourceService.findAll();
        } else {
            resources = resourceService.findResourceListByRoleIds(roleDns);
        }
        // ??????????????????
        List<RoleResource> isChecked = new ArrayList<>();
        // ???????????????????????????ID??????
        List<String> checkedRoleIds = new ArrayList<>();
        if (StringUtils.isNotBlank(roleDn)) {
            checkedRoleIds.add(roleDn);
        }
        // ??????????????????????????????
        if (checkedRoleIds.size() > 0) {
            isChecked = getRoleResourceByRoleId(checkedRoleIds);
        }
        // ??????????????????????????????????????????????????????????????????
        List<RoleResource> allChecked = new ArrayList<>();
        // ?????????????????????????????????????????????
        if (roleDns.size() > 0) {
            allChecked = getRoleResourceByRoleId(roleDns);
        }

        // ????????????????????????
        JSONArray result = new JSONArray();
        for (Resource resource : resources) {
            // ?????????????????????????????????
            JSONObject obj = new JSONObject();
            obj.put("id", resource.getId());
            obj.put("pId", resource.getParentId());
            obj.put("name", resource.getResourceName());

            // ?????????????????????,type = 0??? ????????????
            JSONObject editObj = new JSONObject();
            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (resource.getType() == 0) {
                // ????????????
                editObj.put("id", resource.getId() + "edit");
                editObj.put("pId", resource.getId());
                editObj.put("type", "premissionEdit");
                editObj.put("name", "??????");
            }
            // ???????????????????????????????????????????????????
            if (isChecked.size() > 0) {
                for (RoleResource reResource : isChecked) {
                    if (resource.getId().equals(reResource.getResourceId())) {
                        obj.put("checked", true);
                        if (resource.getType() == 0) {
                            // ????????????????????????&?????????????????????????????????&??????admin???????????????????????????
                            if (reResource.getEditable() != 0) {
                                editObj.put("checked", true);
                            } else if (!isAdmin) {
                                editObj.put("chkDisabled", true);
                            }
                        }
                    }
                }
            }
            // ???????????????????????????????????????/
            if (allChecked.size() > 0) {
                for (RoleResource reResource : allChecked) {
                    if (resource.getId().equals(reResource.getResourceId())) {
                        if (resource.getType() == 0) {
                            // ????????????????????????&?????????????????????????????????&??????admin???????????????????????????
                            // ????????????????????????????????????????????????????????????????????????, ????????????????????????????????????
                            Boolean chkDisabled = editObj.getBoolean("chkDisabled");
                            Integer editable = reResource.getEditable();
                            if (editable == 0) {
                                if (!isAdmin) {
                                    editObj.put("chkDisabled", true);
                                }
                            } else if (editable == 1 && Objects.nonNull(chkDisabled) && chkDisabled) {
                                // ?????????????????????????????????????????????????????????????????????, ???????????????????????????????????????????????????,?????????????????????????????????
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
     * ??????roleDn????????????????????????????????????
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
        // ??????????????????????????????
        String message = "???????????? : " + group.getRoleName();
        String ipAddress = getIpAddress();
        // ????????????IP???message??????????????????
        logSearchService.addLog(ipAddress, message, "3", "", "-", "");
    }

    @Override
    public boolean addRoleResourceByBatch(List<RoleResourceForm> list) {
        if (list != null && list.size() > 0) {
            String userName = userService.getCurrentUserInfo().getUsername();
            for (RoleResourceForm form : list) {
                // ?????????
                form.setCreateDataUsername(userName);
                // ????????????
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
        // ??????LdapName????????????????????????
        Group existingGroup = groupRepo.findOne(originalId);
        if (existingGroup != null) {
            // ???????????????
            if (StringUtils.isNotBlank(group.getRoleName())) {
                existingGroup.setRoleName(group.getRoleName());
            }
            // ????????????
            existingGroup.setDescription(group.getDescription());
            // ??????Ldap????????????
            Group saveGroup = groupRepo.save(existingGroup);
            // ?????????????????????????????????,?????????????????????????????????
            if (saveGroup != null) {
                // ???????????????Resource??????
                String newRoleId = saveGroup.getId().toString();
                // ?????????
                boolean delFlag = true;
                // ??????id???????????????????????????????????????
                List<RoleResource> ids = roleDao.findIdByRoleId(groupId);
                if (ids != null && ids.size() > 0) {
                    // ?????????????????????????????????
                    delFlag = roleDao.deleteByRoleId(groupId);
                }
                // ?????????
                List<RoleResourceForm> formList = new ArrayList<RoleResourceForm>();
                if (delFlag) {
                    // ????????????????????????
                    if (permissionEditTree != null && !permissionEditTree.isEmpty()) {
                        JSONArray resourceArray = JSON.parseArray(permissionEditTree);
                        for (Object obj : resourceArray) {
                            String id = (String) ((JSONObject) obj).get("id");
                            // ????????????
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
                // ????????????????????????ID
                String message = "???????????? : " + group.getRoleName();
                // ???????????????????????????
                logSearchService.addLog(getIpAddress(), message, "3", "", "-", "");
                msg.put("flag", 1);
                msg.put("errMsg", "???????????????");
                return new JsonResultBean(msg);
            }
        }
        msg.put("flag", 2);
        return new JsonResultBean(msg);
    }

    @Override
    public String deleteGroup(String groupId) {
        String[] roleIds = groupId.split(";");
        // ????????????
        StringBuilder message = new StringBuilder();
        StringBuilder deleteMessage = new StringBuilder();
        StringBuilder notDeleteMessage = new StringBuilder();
        // ?????????roleId
        List<String> deleteRoleIds = new ArrayList<>();
        for (String roleId : roleIds) {
            if (StringUtils.isNotBlank(roleId) && (!"cn=ROLE_ADMIN,ou=Groups".equals(roleId))) {
                if (roleEntryDn.equals(roleId)) {
                    // ????????????: ?????????????????????
                    message.append("????????????: ?????????????????????").append(" <br/>");
                    continue;
                }
                String roleName;
                // ??????id????????????
                Group g = getGroupById(roleId);
                if (g == null) {
                    continue;
                }
                roleName = g.getRoleName();
                if (("???????????????").equals(roleName) || "????????????".equals(roleName) || "????????????".equals(roleName)) {
                    notDeleteMessage.append("?????? :???").append(g.getRoleName()).append("???").append("??????????????? <br/>");
                    continue;

                }

                Set<Name> members = g.getMembers();
                if (members.size() > 2) {
                    deleteMessage.append("?????? :???").append(g.getRoleName()).append("???").append(" <br/>");
                    continue;
                }

                message.append("???????????? : ").append(g.getRoleName()).append(" <br/>");
                ldapTemplate.unbind(roleId);
                deleteRoleIds.add(roleId);
            }
        }
        // ??????????????????????????????????????????
        if (CollectionUtils.isNotEmpty(deleteRoleIds)) {
            roleDao.deleteBatchByRoleId(deleteRoleIds);
        }
        String ipAddress = getIpAddress();
        if (!message.toString().isEmpty()) {
            if (roleIds.length > 1) {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "??????????????????");
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "", "-", "");
            }
        }
        if (deleteMessage.length() > 0) {
            deleteMessage.append("????????????????????????????????????????????????");
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
        //????????????????????????????????????
        UserLdap user = SystemHelper.getCurrentUser();
        LdapName name = LdapUtils.newLdapName(user.getId() + "," + userService.getBaseLdapPath().toString());
        List<Group> roles = (List<Group>) groupRepo.findByMember(name);

        //???????????????????????????Id
        Map<String, String> groupMap = ImmutableMap.of("resourceName", "????????????", "type", "0");
        List<String> resourceIds = roleDao.getIdByNameAndType(groupMap);
        if (resourceIds.isEmpty()) {
            log.error("?????????????????????????????????????????????");
            return false;
        }

        //???????????????????????????????????????????????????????????????
        String resourceId = resourceIds.get(0);
        List<String> roleIds = roles.stream().map(role -> role.getId().toString()).collect(Collectors.toList());
        Integer isWrite = roleDao.countMenuEditableByRoles(roleIds, resourceId);
        return isWrite > 0;
    }
}
