package com.zw.platform.basic.imports.handler;

import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.domain.UserGroupDO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.imports.GroupImportDTO;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.repository.UserGroupDao;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.RoleService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import com.zw.platform.util.imports.lock.ImportTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: 分组导入处理类
 * @date 2020/10/2815:53
 */
public class GroupImportHandler extends BaseImportHandler {

    private List<GroupImportDTO> groupImportDTOList;
    private Map<String, String> groupNameIdMap;
    private GroupDao groupDao;
    private List<GroupDO> importList;
    private UserService userService;
    private UserGroupDao userGroupDao;
    private OrganizationService organizationService;
    private RoleService roleService;

    private Map<String, Set<String>> userNameAndGroupIdListMap;
    private List<UserGroupDO> userGroupList;
    private Map<String, Set<String>> orgIdAndGroupIdListMap;

    @Override
    public ImportModule module() {
        return ImportModule.ASSIGNMENT;
    }

    public GroupImportHandler(List<GroupImportDTO> groupImportDTOList, Map<String, String> groupNameIdMap,
        GroupDao groupDao, UserService userService, UserGroupDao userGroupDao,
        RoleService roleService, OrganizationService organizationService) {
        this.groupImportDTOList = groupImportDTOList;
        this.groupNameIdMap = groupNameIdMap;
        this.groupDao = groupDao;
        this.userService = userService;
        this.userGroupDao = userGroupDao;
        this.roleService = roleService;
        this.organizationService = organizationService;
    }

    @Override
    public ImportTable[] tables() {
        return new ImportTable[] { ImportTable.ZW_M_ASSIGNMENT };
    }

    @Override
    public boolean addMysql() {
        partition(importList, groupDao::batchAdd);
        partition(userGroupList, userGroupDao::batchAdd);
        return true;
    }

    @Override
    public boolean uniqueValid() {
        List<GroupDO> groupDOList = groupDao.getGroupListByGroupIds(groupNameIdMap.values());
        Map<String, Set<String>> groupAssignmentNamesMap = groupDOList.stream().collect(
            Collectors.groupingBy(GroupDO::getOrgId, Collectors.mapping(GroupDO::getName, Collectors.toSet())));
        // 企业下的分组数量
        Map<String, Integer>  orgIdNumberMap = groupDOList.stream()
            .collect(Collectors.groupingBy(GroupDO::getOrgId, Collectors.summingInt(o -> 1)));
        for (GroupImportDTO groupImportDTO : groupImportDTOList) {
            if (StringUtils.isNotBlank(groupImportDTO.getErrorMsg())) {
                continue;
            }
            String orgName = groupImportDTO.getOrgName();
            String orgId = groupNameIdMap.get(orgName);
            groupImportDTO.setOrgId(orgId);
            // 同一组织下分组不能同名
            if (groupAssignmentNamesMap.containsKey(orgId)) {
                if (groupAssignmentNamesMap.get(orgId).contains(groupImportDTO.getName())) {
                    groupImportDTO.setErrorMsg("分组已在当前企业存在");
                    continue;
                }
            }
            // 验证是否超过了一百条
            Integer number = orgIdNumberMap.computeIfAbsent(orgId, key -> 0);
            if (number >= 100) {
                groupImportDTO.setErrorMsg("企业下的分组上限已达到100个！");
                continue;
            }
            ++number;
            orgIdNumberMap.put(orgId, number);
        }
        Set<String> errors = groupImportDTOList.stream().map(GroupImportDTO::getErrorMsg)
            .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        boolean empty = CollectionUtils.isEmpty(errors);
        if (empty) {
            assemblyData();
            progressBar.setTotalProgress(importList.size() + userGroupList.size());
        }

        return empty;
    }

    private void assemblyData() {
        importList = new ArrayList<>();
        Map<String, String> groupIdAndOrgIMap = new HashMap<>(16);
        String id;
        for (GroupImportDTO groupImportDTO : groupImportDTOList) {
            GroupDO groupDO = new GroupDO();
            id = UUID.randomUUID().toString();
            groupDO.setId(id);
            BeanUtils.copyProperties(groupImportDTO, groupDO);
            groupDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
            groupDO.setCreateDataTime(new Date());
            groupDO.setOrgId(groupImportDTO.getOrgId());
            groupDO.setFlag(1);
            groupIdAndOrgIMap.put(groupDO.getId(), groupDO.getOrgId());
            importList.add(groupDO);
        }
        userGroupList = assembleGroupUserList(groupIdAndOrgIMap);
    }


    private List<UserGroupDO> assembleGroupUserList(Map<String, String> groupIdAndOrgUuidMap) {
        // 用户下需要新增的分组id
        userNameAndGroupIdListMap = new HashMap<>(16);
        // 企业下需要新增的分组id
        orgIdAndGroupIdListMap = new HashMap<>(16);
        List<UserGroupDO> list = new ArrayList<>();
        // 查询超级管理员和普通管理员下的成员
        List<LdapName> memberList = roleService.getMemberNameListByRoleCn("POWER_USER");
        Set<LdapName> memberNameList = new HashSet<>(memberList);
        memberNameList.addAll(roleService.getMemberNameListByRoleCn("ROLE_ADMIN"));
        // 所有组织 当前组织
        List<OrganizationLdap> allOrg = organizationService.getOrgChildList("ou=organization");
        // 企业uuid 对应的 企业
        Map<String, OrganizationLdap> orgUuidMap =
            allOrg.stream().collect(Collectors.toMap(OrganizationLdap::getUuid, Function.identity()));
        // 当前用户
        UserLdap currentUser = SystemHelper.getCurrentUser();
        String currentUsername = currentUser.getUsername();
        String currentUserUuid = userService.getCurrentUserUuid();
        List<UserDTO> allUser = userService.findAllUser();
        Map<String, List<UserDTO>> orgIdAndUserListMap = allUser
            .stream()
            .collect(Collectors.groupingBy(user -> {
                String userId = user.getId().toString();
                return userId.substring(userId.indexOf(",") + 1);
            }));
        String baseLdapPath = userService.getBaseLdapPath().toString();
        for (Map.Entry<String, String> entry : groupIdAndOrgUuidMap.entrySet()) {
            String groupId = entry.getKey();
            String orgUuid = entry.getValue();
            OrganizationLdap organization = orgUuidMap.get(orgUuid);
            if (organization == null || organization.getId() == null) {
                continue;
            }
            orgIdAndGroupIdListMap.computeIfAbsent(orgUuid, x -> Sets.newHashSet()).add(groupId);
            String orgDn = organization.getId().toString();
            // 当前组织的上级组织list
            List<OrganizationLdap> currentAndSuperiorOrgList = new ArrayList<>();
            // 递归获取当前组织和上级组织list
            getParentOrg(allOrg, orgDn, currentAndSuperiorOrgList);
            // 当前组织及上级组织里的管理员UUID和名称
            Map<String, String> currentAndSuperiorUserUuidAndNameMap = new HashMap<>(16);
            currentAndSuperiorUserUuidAndNameMap.put(currentUserUuid, currentUsername);
            for (OrganizationLdap org : currentAndSuperiorOrgList) {
                orgDn = org.getId().toString();
                // 查询组织下的用户
                List<UserDTO> orgUserList = orgIdAndUserListMap.get(orgDn);
                if (CollectionUtils.isEmpty(orgUserList)) {
                    continue;
                }
                for (UserDTO user : orgUserList) {
                    String userUuid = user.getUuid();
                    LdapName roleName = LdapUtils.newLdapName(user.getId().toString() + "," + baseLdapPath);
                    if (!memberNameList.contains(roleName)) {
                        continue;
                    }
                    currentAndSuperiorUserUuidAndNameMap.put(userUuid, user.getUsername());
                }
            }
            UserGroupDO userGroupDo;
            for (Map.Entry<String, String> uuidAndNameEntry : currentAndSuperiorUserUuidAndNameMap.entrySet()) {
                String userUuid = uuidAndNameEntry.getKey();
                String userName = uuidAndNameEntry.getValue();
                userGroupDo = new UserGroupDO(groupId, userUuid, currentUsername);
                list.add(userGroupDo);
                userNameAndGroupIdListMap.computeIfAbsent(userName, x -> Sets.newHashSet()).add(groupId);
            }
        }
        return list;
    }

    private void getParentOrg(List<OrganizationLdap> allList, String id, List<OrganizationLdap> returnList) {
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(allList)) {
            for (OrganizationLdap org : allList) {
                if (org.getId().toString().equals(id)) {
                    returnList.add(org);
                    String parentId = org.getPid();
                    if (parentId != null && !"".equals(parentId)) {
                        getParentOrg(allList, org.getPid(), returnList);
                    }
                }
            }
        }
    }

    @Override
    public void addOrUpdateRedis() {
        // 维护企业分组缓存
        addOrgGroups(orgIdAndGroupIdListMap);
        // 维护用户分组缓存
        addUserGroups(userNameAndGroupIdListMap);
    }

    /**
     * 分组和企业之间的Redis缓存关系
     * @param orgIdAndGroupIdListMap
     */
    private void addOrgGroups(Map<String, Set<String>> orgIdAndGroupIdListMap) {
        if (orgIdAndGroupIdListMap == null || orgIdAndGroupIdListMap.isEmpty()) {
            return;
        }
        Map<RedisKey, Collection<String>> keyValuesMap =
            new HashMap<>(CommonUtil.ofMapCapacity(orgIdAndGroupIdListMap.size()));
        Set<String> value;
        for (Map.Entry<String, Set<String>> entry : orgIdAndGroupIdListMap.entrySet()) {
            value = entry.getValue();
            if (CollectionUtils.isEmpty(value)) {
                continue;
            }
            keyValuesMap.put(RedisKeyEnum.ORG_GROUP.of(entry.getKey()), value);
        }
        RedisHelper.batchAddToSet(keyValuesMap);
    }

    /**
     * 用户分组之间的关系
     * @param userNameGroupIdListMap
     */
    private void addUserGroups(Map<String, Set<String>> userNameGroupIdListMap) {
        if (userNameGroupIdListMap == null || userNameGroupIdListMap.isEmpty()) {
            return;
        }
        Set<Map.Entry<String, Set<String>>> entries = userNameAndGroupIdListMap.entrySet();
        Map<RedisKey, Collection<String>> keyValuesMap =
            new HashMap<>(CommonUtil.ofMapCapacity(orgIdAndGroupIdListMap.size()));
        Set<String> value;
        for (Map.Entry<String, Set<String>> entry : entries) {
            value = entry.getValue();
            if (CollectionUtils.isEmpty(value)) {
                continue;
            }
            keyValuesMap.put(RedisKeyEnum.USER_GROUP.of(entry.getKey()), value);
        }
        RedisHelper.batchAddToSet(keyValuesMap);
    }

}
