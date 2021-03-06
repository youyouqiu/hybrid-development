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
 * @Title: ???????????????
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
     * ?????????????????????????????????
     * @param userDn   ??????dn
     * @param groupIds ??????Id
     * @return
     */
    @Override
    public boolean addGroups2User(String userDn, String groupIds) throws Exception {
        boolean empty = StringUtils.isEmpty(groupIds);
        // ????????????????????????
        String userName = userDn.split(",")[0].split("=")[1];
        Set<String> beforeGroupIds = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(userName));
        //???????????????
        HashSet<String> currentGroupIds = Sets.newHashSet();
        if (!empty) {
            currentGroupIds = JSON.parseObject(groupIds, new TypeReference<HashSet<String>>(){});
        }
        // ?????????????????????
        Set<String> delList = new HashSet<>(beforeGroupIds);
        Set<String> addList = new HashSet<>(currentGroupIds);
        //?????????????????????
        delList.removeAll(currentGroupIds);
        // ?????????????????????
        addList.removeAll(beforeGroupIds);
        if (CollectionUtils.isEmpty(addList) && CollectionUtils.isEmpty(delList)) {
            //??????????????????
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
        //??????mysql
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
        //??????redis
        RedisKey redisKey = RedisKeyEnum.USER_GROUP.of(userName);
        RedisHelper.delSetItem(redisKey, delList);
        RedisHelper.addToSet(redisKey, addList);
    }

    /**
     * ????????????
     * @param addList
     * @param delList
     * @param user
     */
    private void log(Set<String> addList, Set<String> delList, UserDTO user) {

        StringBuilder logBuilder = new StringBuilder();
        if (CollectionUtils.isNotEmpty(addList)) {
            logBuilder.append("??????").append(user.getUsername()).append(" ????????????(");
            List<String> groupNames = groupService.getNamesByIds(addList);
            if (CollectionUtils.isNotEmpty(groupNames)) {
                logBuilder.append(String.join(",", groupNames)).append(",");
                logBuilder.deleteCharAt(logBuilder.length() - 1).append(")");
            }
        }
        if (CollectionUtils.isNotEmpty(delList)) {
            logBuilder.append("??????").append(user.getUsername()).append(" ????????????(");
            List<String> groupNames = groupService.getNamesByIds(delList);
            if (CollectionUtils.isNotEmpty(groupNames)) {
                logBuilder.append(String.join(",", groupNames)).append(",");
                logBuilder.deleteCharAt(logBuilder.length() - 1).append(")");
            }
        }
        LogSearchForm form = new LogSearchForm();
        form.setEventDate(new Date());
        // ?????????????????????????????????
        form.setUsername(SystemHelper.getCurrentUsername());
        form.setIpAddress(getIpAddress());
        form.setGroupId(user.getOrgId());
        form.setLogSource("3");
        form.setModule("more");
        form.setMonitoringOperation("?????????" + user.getUsername() + " ??????()");
        form.setMessage(logBuilder.toString());
        form.setBrand("-");
        form.setPlateColor(null);
        logSearchService.addLogBean(form);
    }

    /**
     * ??????????????????????????????
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
        // ?????????????????????????????????????????????
        List<OrganizationLdap> validOrg = new ArrayList<>();
        // ?????????????????????????????????????????????
        List<OrganizationLdap> allOrg = userService.getCurrentUseOrgList();
        // ??????????????????????????????
        OrganizationLdap organization = organizationService.getOrganizationByUuid(groupDTO.getOrgId());
        organizationService.getParentOrgList(allOrg, organization.getId().toString(), validOrg);
        // ???????????????????????????
        List<UserDTO> users = new ArrayList<>();
        // ?????????????????????id??????
        List<String> userIdList = new ArrayList<>();
        if (!validOrg.isEmpty()) {
            for (OrganizationLdap organizationLdap : validOrg) {
                // ??????????????????????????????
                List<UserDTO> userList =
                    userService.getUserByOrgDn(organizationLdap.getId().toString(), SearchScope.ONELEVEL);
                if (userList != null && !userList.isEmpty()) {
                    for (UserDTO user : userList) {
                        //????????????ID
                        user.setOrgDn(organizationLdap.getId().toString());
                        userIdList.add(user.getUuid());
                    }
                    users.addAll(userList);
                }
            }
        }
        users.sort(Comparator.comparing(UserDTO::getCreateTimestamp).reversed());
        // ???????????????????????????
        List<String> assignedUserIds = getAssignedUserIdByIdAndUserId(groupId, userIdList);
        // ?????????????????????
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

        // ?????????????????????????????????????????????
        List<OrganizationLdap> currentUseOrgList = userService.getCurrentUseOrgList();
        if (CollectionUtils.isEmpty(currentUseOrgList)) {
            return new ArrayList<>(1);
        }
        //?????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<>();
        Set<OrganizationLdap> orgList = new HashSet<>();
        Map<String, OrganizationLdap> map = new HashMap<>(CommonUtil.ofMapCapacity(currentUseOrgList.size()));
        if ("groupName".equals(queryType) && StringUtils.isNotEmpty(queryParam)) {
            for (OrganizationLdap org : currentUseOrgList) {
                map.put(org.getUuid(), org);
                if (!org.getName().contains(queryParam)) {
                    //??????????????????????????????????????????
                    continue;
                }
                userOrgListId.add(org.getUuid());
                TreeUtils.getLowerOrg(orgList, currentUseOrgList, org);
            }
        } else {
            // ??????????????????????????????
            orgList.addAll(currentUseOrgList);
            for (OrganizationLdap org : orgList) {
                userOrgListId.add(org.getUuid());
                map.put(org.getUuid(), org);
            }
        }
        if (CollectionUtils.isEmpty(userOrgListId)) {
            return new ArrayList<>();
        }
        //???????????????????????????????????????
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
            // ?????????????????????
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
            // ???????????????
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
        // ?????????????????????
        tree.addAll(jsonArray);
        return filterGroupIds;
    }

    /**
     * ????????????????????????????????????????????????
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
                // ????????????id(????????????id????????????????????????)
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
        // ???????????????
        result.addAll(JsonUtil.getOrgTree(validOrg, "multiple"));
        return result.toJSONString();
    }

    /**
     * ????????????????????????????????????
     */
    @Override
    public boolean addGroup2User(String groupId, String userIdList) throws BusinessException {

        // ????????????????????????
        String orgDn = userService.getCurrentUserOrgDn();
        // ????????????????????????????????????????????????user
        List<UserDTO> users = userService.getUserByOrgDn(orgDn, SearchScope.SUBTREE);
        List<String> beforeUserIdList = new ArrayList<>();
        for (UserDTO user : users) {
            beforeUserIdList.add(user.getUuid());
        }
        // ????????????????????????
        List<String> beforeUser = getAssignedUserIdByIdAndUserId(groupId, beforeUserIdList);
        // ??????????????????
        Set<String> addList = new HashSet<>();
        if (!StringUtils.isEmpty(userIdList)) {
            String[] userIdArr = userIdList.split(";");
            addList.addAll(Arrays.asList(userIdArr));
        }
        // ?????????????????????
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
        //??????????????????????????????ID
        List<String> cacheAddList = users.stream().filter(p -> addList.contains(p.getUuid())).map(UserDTO::getUsername)
            .collect(Collectors.toList());
        //??????????????????????????????ID
        List<String> cacheDelList = users.stream().filter(p -> delList.contains(p.getUuid())).map(UserDTO::getUsername)
            .collect(Collectors.toList());
        /**
         * ????????????
         */
        operateGroupRedisCache(groupId, cacheDelList, cacheAddList);
        GroupDTO groupDTO = groupService.getById(groupId);
        String msg =
            "???????????????" + groupDTO.getName() + " ( @" + organizationService.getOrganizationByUuid(groupDTO.getOrgId())
                + " ) ??????????????????";
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
     * ????????????Id, ??????Id????????????????????????????????????
     * @param groupId    ??????Id
     * @param userIdList ??????Id
     * @return ??????Id??????
     */
    private List<String> getAssignedUserIdByIdAndUserId(String groupId, List<String> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            return new ArrayList<>(1);
        }
        return userGroupDao.getAssignedUserIdByIdAndUserId(groupId, userIdList);
    }

    @Override
    public JSONArray distributeUserGroupTree(String userDn) {
        // ?????????????????????????????????????????????????????????
        JSONArray array = new JSONArray();
        String username = userService.getUsernameByUserDn(userDn);
        Set<String> userGroupIdSet = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(username));
        List<GroupDTO> userGroups;
        JSONObject jsonObject;
        Set<String> repeatGroupIds = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(userGroupIdSet)) {
            userGroups = groupService.getGroupsById(userGroupIdSet);
            if (CollectionUtils.isEmpty(userGroups)) {
                log.error("????????????????????????redis??????????????????????????????");
                return array;
            }
            //????????????id???????????????
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
        //????????????id???????????????
        userGroups = groupService.getGroupsById(userGroupIdSet);
        if (CollectionUtils.isEmpty(userGroups)) {
            log.error("????????????????????????redis??????????????????????????????");
            return array;
        }
        // ??????????????????????????????????????????
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
        log.info("??????????????????-?????????redis?????????.");
        //???????????????-???????????????
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
        log.info("????????????-?????????redis?????????.");
    }

    @Override
    public Map<String, String> getGroupMap() {
        List<GroupDTO> groupList = userService.getCurrentUserGroupList();
        return groupList != null ? groupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName)) :
            new HashMap<>(16);
    }

    /**
     * ?????????id????????????
     * @param userUuid
     * @param vehicleIds
     */
    @Override
    public List<GroupDTO> getUserAssignmentByVehicleId(String userUuid, List<String> vehicleIds) {
        return userGroupDao.getUserAssignmentByVehicleId(userUuid, vehicleIds);
    }
}
