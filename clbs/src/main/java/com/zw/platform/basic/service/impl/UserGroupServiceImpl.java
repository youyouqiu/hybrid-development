package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.UserGroupDO;
import com.zw.platform.basic.dto.CountDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.UserGroupDTO;
import com.zw.platform.basic.repository.UserGroupDao;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.TreeUtils;
import com.zw.platform.util.common.BusinessException;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.query.SearchScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: 用户分组类
 * @date 2020/9/2510:39
 */
@Service
@Slf4j
public class UserGroupServiceImpl implements UserGroupService, IpAddressService {

    @Autowired
    private UserGroupDao userGroupDao;

    @Autowired
    private UserService userService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private GroupMonitorService groupMonitorService;

    /**
     * 用户页面：用户分配分组
     * @param userDn   用户dn
     * @param groupIds 分组Id
     * @return
     */
    @Override
    public boolean addGroups2User(String userDn, String groupIds) throws Exception {
        boolean empty = StringUtils.isEmpty(groupIds);
        // 用户所选权限车组
        String userName = userDn.split(",")[0].split("=")[1];
        Set<String> beforeGroupIds = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(userName));
        //选定的分组
        HashSet<String> currentGroupIds = Sets.newHashSet();
        if (!empty) {
            currentGroupIds = JSON.parseObject(groupIds, new TypeReference<HashSet<String>>(){});
        }
        // 需要删除的数据
        Set<String> delList = new HashSet<>(beforeGroupIds);
        Set<String> addList = new HashSet<>(currentGroupIds);
        //需要删除的数据
        delList.removeAll(currentGroupIds);
        // 需要新增的数据
        addList.removeAll(beforeGroupIds);
        if (CollectionUtils.isEmpty(addList) && CollectionUtils.isEmpty(delList)) {
            //没有改变分组
            return true;
        }
        UserDTO user = userService.getUserByEntryDn(userDn);
        updateUserGroupIds(user.getUuid(), user.getUsername(), delList, addList);
        log(addList, delList, user);
        return true;
    }

    @Override
    public void updateUserGroupIds(String userId, String userName, Collection<String> delList,
        Collection<String> addList) throws Exception {
        //操作mysql
        if (!delList.isEmpty()) {
            if (StringUtils.isNotEmpty(userId) && CollectionUtils.isNotEmpty(delList)) {
                userGroupDao.deleteUserGroupByUserAndGroupIds(userId, delList);
            }
        }
        List<UserGroupDO> userGroupDOList = new ArrayList<>();
        if (!addList.isEmpty()) {
            for (String groupId : addList) {
                userGroupDOList.add(new UserGroupDO(groupId, userId, SystemHelper.getCurrentUsername()));
            }
            userGroupDao.batchAdd(userGroupDOList);
        }
        //操作redis
        RedisKey redisKey = RedisKeyEnum.USER_GROUP.of(userName);
        RedisHelper.delSetItem(redisKey, delList);
        RedisHelper.addToSet(redisKey, addList);
    }

    /**
     * 记录日志
     * @param addList
     * @param delList
     * @param user
     */
    private void log(Set<String> addList, Set<String> delList, UserDTO user) {

        StringBuilder logBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(addList)) {
            logBuilder.append("授权").append(user.getUsername()).append(" 查看分组(");
            List<String> groupNames = groupService.getNamesByIds(addList);
            if (CollectionUtils.isNotEmpty(groupNames)) {
                logBuilder.append(String.join(",", groupNames)).append(",");
                logBuilder.deleteCharAt(logBuilder.length() - 1).append(")");
            }
        }
        if (CollectionUtils.isNotEmpty(delList)) {
            logBuilder.append("取消").append(user.getUsername()).append(" 查看分组(");
            List<String> groupNames = groupService.getNamesByIds(delList);
            if (CollectionUtils.isNotEmpty(groupNames)) {
                logBuilder.append(String.join(",", groupNames)).append(",");
                logBuilder.deleteCharAt(logBuilder.length() - 1).append(")");
            }
        }
        LogSearchForm form = new LogSearchForm();
        form.setEventDate(new Date());
        // 获取到当前用户的用户名
        form.setUsername(SystemHelper.getCurrentUsername());
        form.setIpAddress(getIpAddress());
        form.setGroupId(user.getOrgId());
        form.setLogSource("3");
        form.setModule("more");
        form.setMonitoringOperation("操作：" + user.getUsername() + " 分组()");
        form.setMessage(logBuilder.toString());
        form.setBrand("-");
        form.setPlateColor(null);
        logSearchService.addLogBean(form);
    }

    /**
     * 查询用户权限下的分组
     * @param userId
     * @param orgIds
     * @return
     */
    @Override
    public List<String> findUserGroupIds(String userId, List<String> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>(0);
        }
        return userGroupDao.findUserGroupIds(userId, orgIds);
    }

    @Override
    public boolean batchAddToDb(List<UserGroupDO> assignmentUserForms) {
        return userGroupDao.batchAdd(assignmentUserForms);
    }

    @Override
    public void batchAddToRedis(List<UserGroupDTO> userGroupList) {
        if (CollectionUtils.isEmpty(userGroupList)) {
            return;
        }
        Map<RedisKey, Collection<String>> setKeyValueMap = new HashMap<>(16);
        for (UserGroupDTO userGroup : userGroupList) {
            RedisKey redisKey = RedisKeyEnum.USER_GROUP.of(userGroup.getUserName());
            Collection<String> groupIds = setKeyValueMap.getOrDefault(redisKey, new HashSet<>());
            groupIds.add(userGroup.getGroupId());
            setKeyValueMap.put(redisKey, groupIds);
        }
        RedisHelper.batchAddToSet(setKeyValueMap);
    }

    @Override
    public Set<String> getBingUserIdByOrgId(Collection<String> orgIds) {

        if (CollectionUtils.isEmpty(orgIds)) {
            return new HashSet<>(1);
        }
        return userGroupDao.getBingUserIdByOrgId(orgIds);
    }

    @Override
    public String getUserGroupTree(String groupId) throws BusinessException {

        GroupDTO groupDTO = groupService.getById(groupId);
        // 获取分组所属企业及直属上级企业
        List<OrganizationLdap> validOrg = new ArrayList<>();
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> allOrg = userService.getCurrentUseOrgList();
        // 获取当前分组所属组织
        OrganizationLdap organization = organizationService.getOrganizationByUuid(groupDTO.getOrgId());
        organizationService.getParentOrgList(allOrg, organization.getId().toString(), validOrg);
        // 可分配监控人员列表
        List<UserDTO> users = new ArrayList<>();
        // 可分配监控人员id集合
        List<String> userIdList = new ArrayList<>();
        if (!validOrg.isEmpty()) {
            for (OrganizationLdap organizationLdap : validOrg) {
                // 根据组织查询一级用户
                List<UserDTO> userList =
                    userService.getUserByOrgDn(organizationLdap.getId().toString(), SearchScope.ONELEVEL);
                if (userList != null && !userList.isEmpty()) {
                    for (UserDTO user : userList) {
                        //设置组织ID
                        user.setOrgDn(organizationLdap.getId().toString());
                        userIdList.add(user.getUuid());
                    }
                    users.addAll(userList);
                }
            }
        }
        users.sort(Comparator.comparing(UserDTO::getCreateTimestamp).reversed());
        // 已分配监控人员列表
        List<String> assignedUserIds = getAssignedUserIdByIdAndUserId(groupId, userIdList);
        // 组装监控人员树
        return generateUserGroupTree(validOrg, users, assignedUserIds);
    }

    @Override
    public boolean deleteByUserId(String userId) {
        return userGroupDao.deleteByUserId(userId);
    }

    @Override
    public boolean deleteByUserIds(Collection<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return false;
        }
        return userGroupDao.deleteByUserIds(userIds);
    }

    @Override
    public List<GroupDTO> getByGroupIdsAndUserId(String currentUserUuid, List<String> orgIdList) {
        return userGroupDao.getByGroupIdsAndUserId(currentUserUuid, orgIdList);
    }

    @Override
    public List<UserGroupDTO> getAll() {
        return userGroupDao.getAll();
    }

    @Override
    public List<String> getUserIdsByGroupId(String groupId) {
        return userGroupDao.getUserIdsByGroupId(groupId);
    }

    @Override
    public void deleteByGroupId(String id) {
        userGroupDao.deleteByGroupId(id);
    }

    @Override
    public List<UserGroupDTO> getUserIdsByGroupIds(Collection<String> groupIds) {
        return userGroupDao.getUserIdsByGroupIds(groupIds);
    }

    @Override
    public void deleteByGroupIds(Collection<String> groupIds) {
        userGroupDao.deleteByGroupIds(groupIds);
    }

    @Override
    public List<String> getCurrentUserGroupTree(String multiple, String groupId, String queryParam, String queryType,
        JSONArray tree) {

        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> currentUseOrgList = userService.getCurrentUseOrgList();
        if (CollectionUtils.isEmpty(currentUseOrgList)) {
            return new ArrayList<>(1);
        }
        //当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<>();
        Set<OrganizationLdap> orgList = new HashSet<>();
        Map<String, OrganizationLdap> map = new HashMap<>(CommonUtil.ofMapCapacity(currentUseOrgList.size()));
        if ("groupName".equals(queryType) && StringUtils.isNotEmpty(queryParam)) {
            for (OrganizationLdap org : currentUseOrgList) {
                map.put(org.getUuid(), org);
                if (!org.getName().contains(queryParam)) {
                    //模糊查询组织，对组织进行过滤
                    continue;
                }
                userOrgListId.add(org.getUuid());
                TreeUtils.getLowerOrg(orgList, currentUseOrgList, org);
            }
        } else {
            // 不用通过组织名称过滤
            orgList.addAll(currentUseOrgList);
            for (OrganizationLdap org : orgList) {
                userOrgListId.add(org.getUuid());
                map.put(org.getUuid(), org);
            }
        }
        if (CollectionUtils.isEmpty(userOrgListId)) {
            return new ArrayList<>();
        }
        //通过用户和企业获取分组信息
        List<GroupDTO> groupDTOList =
            userGroupDao.getByGroupIdsAndUserId(userService.getCurrentUserUuid(), userOrgListId);
        if (CollectionUtils.isEmpty(groupDTOList)) {
            return new ArrayList<>(1);
        }
        Set<String> groupIds = groupDTOList.stream().map(GroupDTO::getId).collect(Collectors.toSet());
        List<CountDTO> list = groupMonitorService.getCountListByGroupId(groupIds);
        Map<String, Integer> groupCountMap =
            list.stream().collect(Collectors.toMap(CountDTO::getId, CountDTO::getCount, (key1, key2) -> key2));
        List<String> filterGroupIds = Lists.newArrayList();
        List<String> filterOrgDnList = new ArrayList<>();
        OrganizationLdap organizationLdap;
        Integer monitorCount;
        for (GroupDTO groupDTO : groupDTOList) {
            // 排除传入的分组
            if (Objects.equals(groupDTO.getId(), groupId)) {
                continue;
            }
            if ("assignName".equals(queryType) && !groupDTO.getName().contains(queryParam)) {
                continue;
            }
            organizationLdap = map.get(groupDTO.getOrgId());
            monitorCount = groupCountMap.get(groupDTO.getId());
            groupDTO.setMonitorCount(monitorCount == null ? 0 : monitorCount);
            if (organizationLdap == null) {
                continue;
            }
            filterGroupIds.add(groupDTO.getId());
            filterOrgDnList.add(organizationLdap.getId().toString());
            // 组装分组树
            JsonUtil.addGroupJsonObj(groupDTO, organizationLdap, multiple, tree);
        }
        if ("assignName".equals(queryType)) {
            Set<OrganizationLdap> filterOrgs = new HashSet<>();
            for (String pid : filterOrgDnList) {
                filterGroup(currentUseOrgList, filterOrgs, pid);
            }
            orgList.clear();
            orgList.addAll(filterOrgs);
        }
        JSONArray jsonArray = JsonUtil.getOrgTree(new ArrayList<>(orgList), multiple);
        // 组装组织树结构
        tree.addAll(jsonArray);
        return filterGroupIds;
    }

    /**
     * 找到企业的上级及其上上级直到顶级
     * @param orgList
     * @param filterOrgs
     * @param parentId
     */
    private void filterGroup(List<OrganizationLdap> orgList, Set<OrganizationLdap> filterOrgs, String parentId) {
        for (OrganizationLdap org : orgList) {
            if (org.getId().toString().equals(parentId)) {
                filterOrgs.add(org);
                filterGroup(orgList, filterOrgs, org.getPid());
            }
        }
    }

    private String generateUserGroupTree(List<OrganizationLdap> validOrg, List<UserDTO> users,
        List<String> assignedUserIds) {
        JSONArray result = new JSONArray();
        if (!users.isEmpty()) {
            for (UserDTO user : users) {
                String urId = user.getId().toString();
                String userUuid = user.getUuid();
                // 获取组织id(根据用户id得到用户所在部门)
                String userPid = userService.getUserOrgDnByDn(urId);

                JSONObject userObj = new JSONObject();
                userObj.put("id", urId);
                userObj.put("pId", userPid);
                userObj.put("name", user.getUsername());
                if (user.getFullName() != null) {
                    userObj.put("count", user.getFullName());
                }
                userObj.put("type", "user");
                userObj.put("uuid", userUuid);
                userObj.put("iconSkin", "userSkin");
                if (!assignedUserIds.isEmpty() && assignedUserIds.contains(userUuid)) {
                    userObj.put("checked", true);
                }
                result.add(userObj);
            }
        }
        // 组装组织树
        result.addAll(JsonUtil.getOrgTree(validOrg, "multiple"));
        return result.toJSONString();
    }

    /**
     * 分组页面：分配分组给用户
     */
    @Override
    public boolean addGroup2User(String groupId, String userIdList) throws BusinessException {

        // 当前用户所属组织
        String orgDn = userService.getCurrentUserOrgDn();
        // 得到当前用户所属组织及下级组织的user
        List<UserDTO> users = userService.getUserByOrgDn(orgDn, SearchScope.SUBTREE);
        List<String> beforeUserIdList = new ArrayList<>();
        for (UserDTO user : users) {
            beforeUserIdList.add(user.getUuid());
        }
        // 数据库已有的数据
        List<String> beforeUser = getAssignedUserIdByIdAndUserId(groupId, beforeUserIdList);
        // 用户所选权限
        Set<String> addList = new HashSet<>();
        if (!StringUtils.isEmpty(userIdList)) {
            String[] userIdArr = userIdList.split(";");
            addList.addAll(Arrays.asList(userIdArr));
        }
        // 需要删除的数据
        Set<String> delList = new HashSet<>(beforeUser);
        delList.removeAll(addList);
        if (delList.size() > 0) {
            userGroupDao.deleteUserGroupByUserIdAndGroupId(groupId, delList);
        }
        addList.removeAll(beforeUser);
        if (addList.isEmpty() && delList.isEmpty()) {
            return true;
        }
        List<UserGroupDO> userGroupDoList = new ArrayList<>();
        String currentUsername = SystemHelper.getCurrentUsername();
        if (addList.size() > 0) {
            for (String userId : addList) {
                UserGroupDO userGroupDo = new UserGroupDO(groupId, userId, currentUsername);
                userGroupDoList.add(userGroupDo);
            }
            userGroupDao.batchAdd(userGroupDoList);
        }
        //要添加的用户下的分组ID
        List<String> cacheAddList = users.stream().filter(p -> addList.contains(p.getUuid())).map(UserDTO::getUsername)
            .collect(Collectors.toList());
        //要删除的用户下的分组ID
        List<String> cacheDelList = users.stream().filter(p -> delList.contains(p.getUuid())).map(UserDTO::getUsername)
            .collect(Collectors.toList());
        /**
         * 操作缓存
         */
        operateGroupRedisCache(groupId, cacheDelList, cacheAddList);
        GroupDTO groupDTO = groupService.getById(groupId);
        String msg =
            "分组管理：" + groupDTO.getName() + " ( @" + organizationService.getOrganizationByUuid(groupDTO.getOrgId())
                + " ) 分配监控人员";
        logSearchService.addLog(getIpAddress(), msg, "3", "", "-", "");
        WebSubscribeManager.getInstance().clearSubUser();
        return true;
    }

    private void operateGroupRedisCache(String groupId, List<String> cacheDelList, List<String> cacheAddList) {
        Map<RedisKey, Collection<String>> addMap = new HashMap<>(200);
        Map<RedisKey, Collection<String>> delMap = new HashMap<>(200);
        for (String userName : cacheDelList) {
            delMap.computeIfAbsent(RedisKeyEnum.USER_GROUP.of(userName), o -> Sets.newHashSet()).add(groupId);
        }
        for (String userName : cacheAddList) {
            addMap.computeIfAbsent(RedisKeyEnum.USER_GROUP.of(userName), o -> Sets.newHashSet()).add(groupId);
        }
        RedisHelper.batchDeleteSet(delMap);
        RedisHelper.batchAddToSet(addMap);

    }

    /**
     * 通过分组Id, 用户Id进行获取已经被的分配用户
     * @param groupId    分组Id
     * @param userIdList 用户Id
     * @return 用户Id集合
     */
    private List<String> getAssignedUserIdByIdAndUserId(String groupId, List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return new ArrayList<>(1);
        }
        return userGroupDao.getAssignedUserIdByIdAndUserId(groupId, userIdList);
    }

    @Override
    public JSONArray distributeUserGroupTree(String userDn) {
        // 获取当前的用户与组的关联关系的缓存数据
        JSONArray array = new JSONArray();
        String username = userService.getUsernameByUserDn(userDn);
        Set<String> userGroupIdSet = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(username));
        List<GroupDTO> userGroups;
        JSONObject jsonObject;
        Set<String> repeatGroupIds = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(userGroupIdSet)) {
            userGroups = groupService.getGroupsById(userGroupIdSet);
            if (CollectionUtils.isEmpty(userGroups)) {
                log.error("分组缓存出错了，redis缓存在分组的垃圾数据");
                return array;
            }
            //通过分组id查询数据库
            for (GroupDTO groupDTO : userGroups) {
                jsonObject = new JSONObject();
                jsonObject.put("id", groupDTO.getId());
                jsonObject.put("name", groupDTO.getName());
                jsonObject.put("type", "assignment");
                jsonObject.put("pId", groupDTO.getOrgDn());
                jsonObject.put("groupName", groupDTO.getOrgName());
                jsonObject.put("checked", "true");
                jsonObject.put("iconSkin", "assignmentSkin");
                repeatGroupIds.add(groupDTO.getId());
                array.add(jsonObject);
            }
        }
        userGroupIdSet = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(SystemHelper.getCurrentUsername()));
        if (CollectionUtils.isEmpty(userGroupIdSet)) {
            return array;
        }
        //通过分组id查询数据库
        userGroups = groupService.getGroupsById(userGroupIdSet);
        if (CollectionUtils.isEmpty(userGroups)) {
            log.error("分组缓存出错了，redis缓存在分组的垃圾数据");
            return array;
        }
        // 查询指定用户的组织及下级组织
        List<String> userOrgIds = userService.getOrgIdsByUserDn(userDn);
        for (GroupDTO groupDTO : userGroups) {
            if (!userOrgIds.contains(groupDTO.getOrgId()) || repeatGroupIds.contains(groupDTO.getId())) {
                continue;
            }
            jsonObject = new JSONObject();
            jsonObject.put("id", groupDTO.getId());
            jsonObject.put("name", groupDTO.getName());
            jsonObject.put("type", "assignment");
            jsonObject.put("groupName", groupDTO.getOrgName());
            jsonObject.put("pId", groupDTO.getOrgDn());
            jsonObject.put("iconSkin", "assignmentSkin");
            array.add(jsonObject);
        }
        List<OrganizationLdap> orgList = userService.getOrgListByUserDn(userDn);
        array.addAll(JsonUtil.getOrgTree(orgList, "multiple"));
        return array;
    }

    @Override
    public void initCache() {
        log.info("开始进行用户-分组的redis初始化.");
        //初始化用户-分组的缓存
        List<UserDTO> allUser = userService.findAllUser();
        if (CollectionUtils.isEmpty(allUser)) {
            return;
        }
        List<UserGroupDTO> userGroupList = getAll();
        RedisHelper.delByPattern(RedisKeyEnum.USER_GROUP_PATTERN.of());
        if (userGroupList.isEmpty()) {
            return;
        }
        Map<String, Collection<String>> map = new HashMap<>(userGroupList.size());
        for (UserGroupDTO userGroupDTO : userGroupList) {
            map.computeIfAbsent(userGroupDTO.getUserId(), o -> new HashSet<>()).add(userGroupDTO.getGroupId());
        }
        Collection<String> data;
        Map<RedisKey, Collection<String>> userGroupKeyMap = new HashMap<>(allUser.size() * 2);
        for (UserDTO userDTO : allUser) {
            data = map.get(userDTO.getUuid());
            if (CollectionUtils.isEmpty(data)) {
                continue;
            }
            userGroupKeyMap.put(RedisKeyEnum.USER_GROUP.of(userDTO.getUsername()), data);
        }
        RedisHelper.batchAddToSet(userGroupKeyMap);
        log.info("结束用户-分组的redis初始化.");
    }

    @Override
    public Map<String, String> getGroupMap() {
        List<GroupDTO> groupList = userService.getCurrentUserGroupList();
        return groupList != null ? groupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName)) :
            new HashMap<>(16);
    }

    /**
     * 根据车id查询分组
     * @param userUuid
     * @param vehicleIds
     */
    @Override
    public List<GroupDTO> getUserAssignmentByVehicleId(String userUuid, List<String> vehicleIds) {
        return userGroupDao.getUserAssignmentByVehicleId(userUuid, vehicleIds);
    }
}
