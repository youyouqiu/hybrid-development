package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.GroupMonitorDO;
import com.zw.platform.basic.dto.CountDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.GroupMonitorDTO;
import com.zw.platform.basic.repository.GroupMonitorDao;
import com.zw.platform.basic.service.ConfigMessageService;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wanxing
 * @Title: ??????-??????service???
 * @date 2020/10/279:24
 */
@Service
@Slf4j
public class GroupMonitorServiceImpl implements GroupMonitorService, IpAddressService {
    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMonitorDao groupMonitorDao;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private ConfigMessageService configMessageService;

    @Override
    public Set<String> getMonitorIdsByOrgId(Collection<String> orgIds) {

        if (CollectionUtils.isEmpty(orgIds)) {
            return new HashSet<>(1);
        }
        List<RedisKey> redisKeys = Lists.newArrayList();
        for (String orgId : orgIds) {
            redisKeys.add(RedisKeyEnum.ORG_GROUP.of(orgId));
        }
        //??????Id
        Set<String> groupIds = RedisHelper.batchGetSet(redisKeys);
        return getMonitorIdsByGroupId(groupIds);
    }

    @Override
    public Set<String> getMonitorIdsByGroupId(Collection<String> groupIds) {
        List<RedisKey> redisKeys = Lists.newArrayList();
        for (String groupId : groupIds) {
            redisKeys.add(RedisKeyEnum.GROUP_MONITOR.of(groupId));
        }
        return RedisHelper.batchGetSet(redisKeys);
    }

    @Override
    public boolean updateMonitorGroup(List<GroupMonitorDTO> addList, List<GroupMonitorDTO> deleteList, String groupId)
        throws BusinessException {

        GroupDTO groupDTO = groupService.getById(groupId);
        boolean flag = addList == null || deleteList == null || (addList.isEmpty() && deleteList.isEmpty());
        if (flag) {
            return true;
        }
        // ???????????????
        checkRepeat(addList, deleteList);
        if (addList.isEmpty() && deleteList.isEmpty()) {
            return true;
        }
        // ??????????????????
        Set<String> subVehicle = new HashSet<>();
        // ??????
        if (!deleteList.isEmpty()) {
            for (GroupMonitorDTO data : deleteList) {
                groupMonitorDao.deleteByGroupIdAndMonitorId(data.getGroupId(), data.getMonitorId());
                subVehicle.add(data.getMonitorId());
            }
        }
        // ??????
        if (!addList.isEmpty()) {
            Set<String> monitorIdSet = new HashSet<>();
            List<GroupMonitorDO> list = new ArrayList<>(addList.size());
            GroupMonitorDO groupMonitorDO;
            for (GroupMonitorDTO groupMonitorDTO : addList) {
                groupMonitorDO = new GroupMonitorDO();
                groupMonitorDO.setGroupId(groupMonitorDTO.getGroupId());
                groupMonitorDO.setMonitorType(groupMonitorDTO.getMonitorType());
                groupMonitorDO.setVehicleId(groupMonitorDTO.getMonitorId());
                groupMonitorDO.setUpdateDataUsername(SystemHelper.getCurrentUsername());
                groupMonitorDO.setUpdateDataTime(new Date());
                list.add(groupMonitorDO);
                monitorIdSet.add(groupMonitorDTO.getMonitorId());
            }
            Map<String, Set<String>> monitorIdAndGroupIdsMap =
                groupMonitorDao.getListByMonitorIds(monitorIdSet).stream().collect(Collectors
                    .groupingBy(GroupMonitorDTO::getMonitorId,
                        Collectors.mapping(GroupMonitorDTO::getGroupId, Collectors.toSet())));
            list = list.stream().filter(
                groupMonitor -> !monitorIdAndGroupIdsMap.getOrDefault(groupMonitor.getVehicleId(), Sets.newHashSet())
                    .contains(groupMonitor.getGroupId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                groupMonitorDao.batchAdd(list);
                subVehicle.addAll(list.stream().map(GroupMonitorDO::getVehicleId).collect(Collectors.toSet()));
            }
        }
        //??????F3
        configMessageService.sendToF3(subVehicle);
        // ????????????
        operateRedisCache(addList, deleteList);
        String msg = "???????????????" + groupDTO.getName() + " ( @" + groupDTO.getOrgName() + " ) ??????????????????";
        logSearchService.addLog(getIpAddress(), msg, "3", "", "-", "");
        // ??????????????????
        WebSubscribeManager.getInstance().updateSubStatus(subVehicle);
        return true;
    }

    @Override
    public Map<String, String> getGroupMonitorTreeByGroupId(String groupId) {
        GroupDTO groupDTO = groupService.getMonitorCountById(groupId);
        if (groupDTO == null) {
            log.error("????????????????????????Id??????{}", groupId);
            throw new RuntimeException("??????Id????????????");
        }
        //????????????Id??????????????????
        OrganizationLdap organization = organizationService.getOrganizationByUuid(groupDTO.getOrgId());
        if (organization == null) {
            log.error("????????????????????????????????????????????????id???????????????Id??????{}", groupId);
            throw new RuntimeException("?????????????????????");
        }
        groupDTO.setOrgName(organization.getName());
        JSONArray result = new JSONArray();
        JsonUtil.addGroupJsonObj(groupDTO, null, "multiple", result);
        //???????????????????????????
        List<GroupMonitorDTO> groupMonitorList = getMonitorByGroupId(groupId);
        if (!groupMonitorList.isEmpty()) {
            for (GroupMonitorDTO monitorDTO : groupMonitorList) {
                JSONObject obj = JsonUtil.assembleVehicleObject(monitorDTO);
                obj.put("pId", groupDTO.getId());
                result.add(obj);
            }
        }
        Map<String, String> resultMap = new HashMap<>(5);
        resultMap.put("groupName", groupDTO.getName());
        resultMap.put("groupMonitorTree", result.toJSONString());
        return resultMap;
    }

    @Override
    public List<CountDTO> getCountListByGroupId(Collection<String> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        return groupMonitorDao.getCountListByGroupId(groupIds);
    }

    @Override
    public boolean deleteByMonitorIds(Collection<String> monitorIds, boolean isUpdateRedis) {
        List<GroupMonitorDTO> groupMonitors = null;
        if (isUpdateRedis) {
            groupMonitors = groupMonitorDao.getListByMonitorIds(new HashSet<>(monitorIds));
        }
        groupMonitorDao.deleteByMonitorIds(monitorIds);
        if (CollectionUtils.isEmpty(groupMonitors)) {
            return true;
        }
        Map<RedisKey, Collection<String>> groupMonitorSet = new HashMap<>(16);
        for (GroupMonitorDTO groupMonitor : groupMonitors) {
            RedisKey redisKey = RedisKeyEnum.GROUP_MONITOR.of(groupMonitor.getGroupId());
            Collection<String> monitorSet = groupMonitorSet.getOrDefault(redisKey, new HashSet<>());
            monitorSet.add(groupMonitor.getMonitorId());
            groupMonitorSet.put(redisKey, monitorSet);
        }
        RedisHelper.batchDelSet(groupMonitorSet);
        return true;
    }

    @Override
    public boolean add(Collection<GroupMonitorDO> groupMonitorList, boolean isUpdateRedis) {
        groupMonitorDao.batchAdd(groupMonitorList);
        if (!isUpdateRedis) {
            return true;
        }
        Map<RedisKey, Collection<String>> groupMonitorSet = new HashMap<>(16);
        for (GroupMonitorDO groupMonitor : groupMonitorList) {
            RedisKey redisKey = RedisKeyEnum.GROUP_MONITOR.of(groupMonitor.getGroupId());
            Collection<String> monitorSet = groupMonitorSet.getOrDefault(redisKey, new HashSet<>());
            monitorSet.add(groupMonitor.getVehicleId());
            groupMonitorSet.put(redisKey, monitorSet);
        }
        RedisHelper.batchAddToSet(groupMonitorSet);
        return true;
    }

    @Override
    public List<GroupMonitorDTO> getByGroupIds(Collection<String> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return new ArrayList<>();
        }
        return groupMonitorDao.getByGroupIds(groupIds);
    }

    @Override
    public List<GroupMonitorDO> getAll() {
        return groupMonitorDao.getAll();
    }

    @Override
    public List<GroupMonitorDTO> getMonitorByGroupId(String groupId) {
        return groupMonitorDao.getMonitorByGroupId(groupId);
    }

    @Override
    public JSONArray getGroupMonitorTree(String multiple, String groupId, String queryParam, String queryType) {

        // ??????????????????????????????
        JSONArray tree = new JSONArray();
        List<String> groupIds =
            userGroupService.getCurrentUserGroupTree(multiple, groupId, queryParam, queryType, tree);
        // ???????????????????????????????????????????????????????????????????????????
        if (!queryParam.isEmpty() && "name".equals(queryType)) {
            for (Object obj : tree) {
                JSONObject item = (JSONObject) obj;
                item.remove("count");
                item.remove("isParent");
            }
        }
        if (groupIds.isEmpty()) {
            return tree;
        }
        if (!"name".equals(queryType)) {
            queryParam = null;
        }
        queryParam = StringUtil.mysqlLikeWildcardTranslation(queryParam);
        // ?????????????????????????????????????????????????????????(???????????????)
        List<GroupMonitorDTO> monitorList = groupMonitorDao.getMonitorByGroupIds(groupIds, queryParam);
        JSONArray vehicleArray = assembleVehicleData(monitorList);
        tree.addAll(vehicleArray);
        for (int i = 0; i < tree.size(); i++) {
            JSONObject jsonObject = tree.getJSONObject(i);
            jsonObject.put("open", false);
        }
        return tree;
    }

    @Override
    public boolean checkGroupMonitorNum(String groupId) {
        int monitorNum = groupMonitorDao.getCountByGroupId(groupId);
        return monitorNum < configHelper.getMaxNumberAssignmentMonitor();
    }

    @Override
    public List<GroupMonitorDTO> getByMonitorIds(Collection<String> monitorIds) {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new ArrayList<>();
        }
        return groupMonitorDao.getListByMonitorIds(monitorIds);
    }

    @Override
    public List<String> getUserOwnGroupByMonitorId(String monitorId) {
        String userUuid = userService.getCurrentUserUuid();
        return groupMonitorDao.getGroupIdIdByMonitorId(monitorId, userUuid);
    }

    @Override
    public Map<String, Set<String>> getGroupMonitorIdSet(Collection<String> groupIds) {
        List<RedisKey> groupMonitorKeys = RedisKeyEnum.GROUP_MONITOR.ofs(groupIds);
        return RedisHelper.batchGetSetReturnMap(groupMonitorKeys);
    }

    private JSONArray assembleVehicleData(List<GroupMonitorDTO> monitorList) {
        JSONArray result = new JSONArray();
        if (CollectionUtils.isEmpty(monitorList)) {
            return result;
        }
        // ???????????????
        monitorList.sort(Comparator.comparing(GroupMonitorDTO::getBrand));
        for (GroupMonitorDTO monitorDTO : monitorList) {
            JSONObject vehicleObj = JsonUtil.assembleVehicleObject(monitorDTO);
            result.add(vehicleObj);
        }
        return result;
    }

    /**
     * ????????????
     * @param addList    ????????????
     * @param deleteList ????????????
     */
    private void checkRepeat(List<GroupMonitorDTO> addList, List<GroupMonitorDTO> deleteList) {
        if (!addList.isEmpty()) {
            List<GroupMonitorDTO> duplicateList = new ArrayList<>();
            for (GroupMonitorDTO form : addList) {
                if (deleteList.contains(form)) {
                    duplicateList.add(form);
                }
            }
            if (!duplicateList.isEmpty()) {
                addList.removeAll(duplicateList);
                deleteList.removeAll(duplicateList);
            }
        }
    }

    /**
     * ????????????-??????????????????
     * @param deleteList ????????????????????????
     * @param addList    ????????????????????????
     */
    private void operateRedisCache(List<GroupMonitorDTO> addList, List<GroupMonitorDTO> deleteList) {

        List<RedisKey> keys = Lists.newLinkedList();
        //????????????--????????????
        Map<RedisKey, Collection<String>> dataMap = generateDateMap(addList, keys);
        RedisHelper.batchAddToSet(dataMap);
        dataMap.clear();
        keys.clear();

        dataMap = generateDateMap(deleteList, keys);
        RedisHelper.batchDeleteSet(dataMap);
    }

    /**
     * ????????????
     * @param addList
     * @param keys
     * @return
     */
    private Map<RedisKey, Collection<String>> generateDateMap(List<GroupMonitorDTO> addList, List<RedisKey> keys) {
        Map<RedisKey, Collection<String>> dataMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(addList)) {
            for (GroupMonitorDTO groupMonitorDTO : addList) {
                dataMap
                    .computeIfAbsent(RedisKeyEnum.GROUP_MONITOR.of(groupMonitorDTO.getGroupId()), o -> new HashSet<>())
                    .add(groupMonitorDTO.getMonitorId());
                keys.add(RedisKeyEnum.MONITOR_INFO.of(groupMonitorDTO.getMonitorId()));
            }
        }
        return dataMap;
    }

    private Map<String, GroupMonitorDTO> getMonitorMap(List<Map<String, String>> monitorFieldMap) {
        if (CollectionUtils.isEmpty(monitorFieldMap)) {
            return new HashMap<>(1);
        }
        Map<String, GroupMonitorDTO> monitorMap = new HashMap<>(CommonUtil.ofMapCapacity(monitorFieldMap.size() * 3));
        GroupMonitorDTO groupMonitorDTO;
        for (Map<String, String> map : monitorFieldMap) {
            groupMonitorDTO = new GroupMonitorDTO();
            groupMonitorDTO.setGroupName(map.get("groupName"));
            groupMonitorDTO.setMonitorId(map.get("id"));
            groupMonitorDTO.setGroupId(map.get("groupId"));
            monitorMap.put(map.get("id"), groupMonitorDTO);
        }
        return monitorMap;
    }

    /**
     * ????????????????????????
     * @param addList    ????????????
     * @param deleteList ????????????
     */
    private void operateMonitorRedisCache(List<GroupMonitorDTO> addList, List<GroupMonitorDTO> deleteList,
        Map<String, GroupMonitorDTO> monitorAddMap, List<RedisKey> keys) {

        StringBuilder groupNameBuilder = new StringBuilder();
        StringBuilder groupIdBuilder = new StringBuilder();
        Map<RedisKey, Map<String, String>> data = new HashMap<>(200);
        GroupMonitorDTO groupMonitorDTORedis;
        //??????
        for (GroupMonitorDTO groupMonitorDTO : addList) {
            groupMonitorDTORedis = monitorAddMap.get(groupMonitorDTO.getMonitorId());
            if (groupMonitorDTORedis != null) {
                groupIdBuilder.append(groupMonitorDTORedis.getGroupId()).append(",");
                groupNameBuilder.append(groupMonitorDTORedis.getGroupName()).append(",");
            }
            groupNameBuilder.append(groupMonitorDTO.getGroupName());
            groupIdBuilder.append(groupMonitorDTO.getGroupId());
            generateParameter(groupNameBuilder, groupIdBuilder, data, groupMonitorDTO);
        }
        RedisHelper.batchAddToHash(data);

        //??????
        List<Map<String, String>> monitorFieldMap =
            RedisHelper.batchGetHashMap(keys, Lists.newArrayList("groupName", "groupId", "id"));
        Map<String, GroupMonitorDTO> monitorDelMap = getMonitorMap(monitorFieldMap);
        for (GroupMonitorDTO groupMonitorDTO : deleteList) {
            groupMonitorDTORedis = monitorDelMap.get(groupMonitorDTO.getMonitorId());
            if (groupMonitorDTORedis == null) {
                continue;
            }
            compareDel(groupIdBuilder, groupMonitorDTORedis.getGroupId(), groupMonitorDTO.getGroupId());
            groupMonitorDTORedis.setGroupId(groupIdBuilder.toString());
            compareDel(groupNameBuilder, groupMonitorDTORedis.getGroupName(), groupMonitorDTO.getGroupName());
            groupMonitorDTORedis.setGroupName(groupNameBuilder.toString());
            generateParameter(groupNameBuilder, groupIdBuilder, data, groupMonitorDTO);
        }
        RedisHelper.batchAddToHash(data);
    }

    /**
     * ????????????
     * @param groupIdBuilder
     * @param parameter1
     * @param parameter2
     */
    private void compareDel(StringBuilder groupIdBuilder, String parameter1, String parameter2) {
        if (StringUtils.isBlank(parameter1)) {
            return;
        }
        LinkedHashSet<String> list = new LinkedHashSet<>(Arrays.asList(parameter1.split(",")));
        list.remove(parameter2);
        groupIdBuilder.append(StringUtils.join(list, ","));
    }

    /**
     * ????????????
     * @param groupNameBuilder ????????????
     * @param groupIdBuilder   ??????ID
     * @param data             ??????
     * @param groupMonitorDTO  ??????-??????????????????
     */
    private void generateParameter(StringBuilder groupNameBuilder, StringBuilder groupIdBuilder,
        Map<RedisKey, Map<String, String>> data, GroupMonitorDTO groupMonitorDTO) {
        Map<String, String> map = Maps.newHashMap();
        map.put("groupName", groupNameBuilder.toString());
        map.put("groupId", groupIdBuilder.toString());
        data.put(RedisKeyEnum.MONITOR_INFO.of(groupMonitorDTO.getMonitorId()), map);
        groupIdBuilder.delete(0, groupIdBuilder.length());
        groupNameBuilder.delete(0, groupNameBuilder.length());
    }

    /**
     * ???????????????????????????
     */
    @Override
    public void initCache() {
        log.info("????????????????????????????????????redis?????????~");
        List<GroupMonitorDO> allData = getAll();
        RedisHelper.delByPattern(RedisKeyEnum.GROUP_MONITOR_PATTERN.of());
        if (allData.isEmpty()) {
            return;
        }
        //??????????????????????????????????????????-????????????ID?????????HashMap?????????????????????????????????
        int groupNum = allData.size() / 100;
        Map<RedisKey, Collection<String>> map = new HashMap<>(CommonUtil.ofMapCapacity(groupNum));
        for (GroupMonitorDO groupMonitorDO : allData) {
            if (StringUtils.isBlank(groupMonitorDO.getVehicleId())) {
                continue;
            }
            map.computeIfAbsent(RedisKeyEnum.GROUP_MONITOR.of(groupMonitorDO.getGroupId()), o -> new HashSet<>())
                .add(groupMonitorDO.getVehicleId());
        }
        RedisHelper.batchAddToSet(map);
        log.info("??????????????????????????????redis?????????.");
    }
}
