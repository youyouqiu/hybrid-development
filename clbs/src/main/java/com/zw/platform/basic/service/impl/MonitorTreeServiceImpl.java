package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasVehicleCardNumDao;
import com.zw.lkyw.service.videoCarousel.impl.VideoCarouselServiceImpl;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.GroupMonitorBindDO;
import com.zw.platform.basic.domain.GroupMonitorCountDo;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.query.MonitorQuery;
import com.zw.platform.basic.dto.query.MonitorTreeHolder;
import com.zw.platform.basic.dto.query.MonitorTreeReq;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.basic.repository.GroupMonitorDao;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.repository.PeopleDao;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.basic.service.GroupService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.MonitorTreeService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.OrganizationUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.MonitorAccStatus;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.TreeUtils;
import com.zw.platform.util.common.MapUtil;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.talkback.domain.basicinfo.LeaveJobPersonnel;
import com.zw.ws.common.PublicVariable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ???????????????????????????
 * @author zhangjuan
 */
@Service
public class MonitorTreeServiceImpl implements MonitorTreeService {
    private static final String GROUP_TYPE = "group";
    private static final String ORG_TYPE = "org";
    /**
     * ?????????????????????????????????
     */
    private static final String[] MONITOR_TREE_NODE_FIELD =
        { "id", "name", "monitorType", "alias", "isVideo", "deviceNumber", "deviceType", "plateColor", "simCardNumber",
            "professionalNames" };

    @Autowired
    private UserService userService;

    @Autowired
    private GroupDao groupDao;

    @Autowired
    private GroupMonitorDao groupMonitorDao;

    @Autowired
    private GroupService groupService;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private VideoCarouselServiceImpl videoCarouselService;

    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    private PeopleDao peopleDao;

    @Autowired
    private NewVehicleDao vehicleDao;

    @Autowired
    private AdasVehicleCardNumDao adasVehicleCardNumDao;

    @Override
    public JSONArray getGroupTree(String type, boolean needMonitorNum, boolean needOnlineNum, boolean needFilterOrg,
        boolean isBigData) {
        MonitorTreeHolder holder =
            getOrgAndGroupNodes(null, null, type, needMonitorNum, needOnlineNum, needFilterOrg, isBigData);
        return Objects.isNull(holder) ? new JSONArray() : holder.getTreeNodes();
    }

    @Override
    public JSONArray getMonitorTree(String type, Integer webType, boolean isCarousel, boolean needAcc) {
        MonitorTreeHolder holder = getOrgAndGroupNodes(null, null, type, true, true, true, false);
        if (Objects.isNull(holder) || CollectionUtils.isEmpty(holder.getGroupList())) {
            return new JSONArray();
        }
        //??????????????????
        Set<String> ownIdSet = holder.getMonitorSet();
        //??????????????????????????????808?????????????????????
        if (Objects.equals(MonitorTreeReq.REAL_TIME_VIDEO, webType)) {
            monitorService.filterByDeviceType(Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE), ownIdSet);
        }

        //????????????????????????
        List<String> fields = Arrays.asList(MONITOR_TREE_NODE_FIELD);
        List<BindDTO> monitorList =
            getMonitorList(ownIdSet, holder.getGroupMonitorSetMap(), holder.getGroupList(), fields, false);

        //?????????????????????????????????????????????????????????
        Map<String, List<VideoChannelSetting>> channelSettingMap = null;
        if (isCarousel) {
            channelSettingMap = getChannelMap(ownIdSet);
        }

        //??????ACC??????
        Map<String, MonitorAccStatus> accStatusMap = null;
        if (needAcc) {
            accStatusMap = monitorService.getAccAndStatus(ownIdSet);
        }

        boolean isParent = Objects.equals(MonitorTreeReq.REAL_TIME_VIDEO, webType);
        JSONArray treeNodes = holder.getTreeNodes();
        for (BindDTO bindDTO : monitorList) {
            JSONObject treeNode = buildMonitorTreeNode(bindDTO, false, isParent, channelSettingMap, accStatusMap);
            treeNodes.add(treeNode);
        }
        return treeNodes;
    }

    @Override
    public JSONArray getMonitorStateTree(Integer webType, String monitorNameKeyword, Integer onlineStatus,
        String deviceType, boolean isCarousel, boolean needAccStatus) {
        //1???????????????????????????????????????
        MonitorTreeReq monitorTreeQuery = new MonitorTreeReq();
        monitorTreeQuery.setType("multiple");
        monitorTreeQuery.setChecked(false);
        monitorTreeQuery.setKeyword(monitorNameKeyword);
        monitorTreeQuery.setQueryType("name");
        monitorTreeQuery.setWebType(webType);
        monitorTreeQuery.setStatus(onlineStatus);
        List<String> deviceTypes = StringUtils.isNotBlank(deviceType) ? Collections.singletonList(deviceType) : null;
        monitorTreeQuery.setDeviceTypes(deviceTypes);
        monitorTreeQuery.setNeedCarousel(isCarousel);
        monitorTreeQuery.setNeedAccStatus(needAccStatus);
        monitorTreeQuery.setNeedOnlineMonitorCount(true);

        //2?????????????????????????????????
        return getMonitorTreeFuzzy(monitorTreeQuery);
    }

    @Override
    public JSONArray getMonitorTreeFuzzy(MonitorTreeReq query) {
        //?????????????????????
        MonitorTreeHolder holder = getOrgAndGroup(query);
        if (Objects.isNull(holder) || CollectionUtils.isEmpty(holder.getGroupList())) {
            return new JSONArray();
        }
        return getTreeNodes(query, holder, false);
    }

    @Override
    public JSONObject getMonitorTree(MonitorTreeReq monitorTreeQuery, boolean isReturnAll) {
        //?????????????????????
        monitorTreeQuery.setType("multiple");
        MonitorTreeHolder holder = getOrgAndGroup(monitorTreeQuery);
        JSONObject jsonObject = new JSONObject();
        if (Objects.isNull(holder) || CollectionUtils.isEmpty(holder.getGroupList())) {
            jsonObject.put("size", 0);
            jsonObject.put("tree", new JSONArray());
            return jsonObject;
        }
        //????????????????????????????????????
        Set<String> monitorIds = getMonitorSet(monitorTreeQuery, holder);
        boolean isFuzzyQuery = StringUtils.isNotBlank(monitorTreeQuery.getKeyword());
        boolean isFilterVhType = StringUtils.isNotBlank(monitorTreeQuery.getVehicleTypeName());
        monitorTreeQuery.setMonitorIds(monitorIds);
        monitorTreeQuery.setLimitMonitorNum(PublicVariable.MONITOR_COUNT);
        int monitorCount = monitorIds.size();

        //????????????????????????????????????????????????
        boolean isNotLimit =
            monitorCount < PublicVariable.MONITOR_COUNT || isFuzzyQuery || isFilterVhType || isReturnAll;
        JSONArray treeNodes;
        if (isNotLimit) {
            //?????????????????????????????????????????????
            treeNodes = getTreeNodes(monitorTreeQuery, holder, false);
        } else {
            //??????????????????????????????????????????????????????????????????????????????
            treeNodes = holder.getTreeNodes();
            for (Object object : holder.getTreeNodes()) {
                JSONObject treeNode = (JSONObject) object;
                if (Objects.equals("assignment", treeNode.getString("type"))) {
                    treeNode.put("isParent", true);
                }
            }
        }
        jsonObject.put("size", monitorCount);
        jsonObject.put("tree", treeNodes);
        return jsonObject;
    }

    @Override
    public JSONObject getMonitorTreeByType(String type, boolean includeQuitPeopleFlag) {
        JSONObject resultJsonObj = new JSONObject();
        JSONArray treeJsonArr = new JSONArray();
        String username = SystemHelper.getCurrentUsername();
        // ???????????????????????????????????????
        Map<String, OrganizationLdap> userOrgMap = userService.getCurrentUseOrgList().stream()
            .collect(Collectors.toMap(OrganizationLdap::getUuid, Function.identity(), (o, p) -> o, LinkedHashMap::new));
        // ???????????????????????????
        Set<String> groupIds = RedisHelper.getSet(RedisKeyEnum.USER_GROUP.of(username));
        Map<String, GroupMonitorCountDo> groupMonitorCountMap = CollectionUtils.isEmpty(groupIds)
                ? new HashMap<>(4)
                : groupMonitorDao.getGroupMonitorCountList(groupIds, null).stream()
                        .collect(Collectors.toMap(GroupMonitorCountDo::getId, Function.identity(),
                            (o, p) -> o, LinkedHashMap::new));
        // ???????????????????????????????????????id
        Set<String> groupMonitorIds = RedisHelper.batchGetSet(RedisKeyEnum.GROUP_MONITOR.ofs(groupIds));
        List<LeaveJobPersonnel> leaveJobPersonnelList = null;
        Set<String> leaveJobPeopleIds = new HashSet<>();
        if (includeQuitPeopleFlag) {
            leaveJobPersonnelList = peopleDao.getLeaveJobPersonnelList(groupIds);
            if (CollectionUtils.isNotEmpty(groupIds)) {
                leaveJobPeopleIds =
                    leaveJobPersonnelList.stream().map(LeaveJobPersonnel::getPeopleId).collect(Collectors.toSet());
            }
        }
        int monitorNumber = Sets.union(groupMonitorIds, leaveJobPeopleIds).size();
        if (monitorNumber > PublicVariable.MONITOR_COUNT) {
            // ???????????????
            treeJsonArr.addAll(JsonUtil.assembleGroupTree(groupMonitorCountMap.values(), userOrgMap, type));
            // ?????????????????????
            treeJsonArr.addAll(JsonUtil.getOrgTree(userOrgMap.values(), type));
            resultJsonObj.put("size", monitorNumber);
            resultJsonObj.put("tree", treeJsonArr);
            return resultJsonObj;
        }
        if (includeQuitPeopleFlag && CollectionUtils.isNotEmpty(leaveJobPersonnelList)) {
            //??????????????????
            for (LeaveJobPersonnel leaveJobPersonnel : leaveJobPersonnelList) {
                JSONObject monitorObjTreeNode = new JSONObject();
                String groupId = leaveJobPersonnel.getAssignmentId();
                monitorObjTreeNode.put("id", leaveJobPersonnel.getPeopleId());
                monitorObjTreeNode.put("type", "people");
                monitorObjTreeNode.put("iconSkin", "peopleSkin");
                monitorObjTreeNode.put("pId", groupId);
                monitorObjTreeNode.put("name", leaveJobPersonnel.getPeopleNumber());
                monitorObjTreeNode.put("assignName", leaveJobPersonnel.getAssignmentName());
                GroupMonitorCountDo groupMonitorCountDo = groupMonitorCountMap.get(groupId);
                if (groupMonitorCountDo != null) {
                    groupMonitorCountDo.setMonitorCount(groupMonitorCountDo.getMonitorCount() + 1);
                }
                treeJsonArr.add(monitorObjTreeNode);
            }
        }
        // ???????????????
        treeJsonArr.addAll(JsonUtil.assembleGroupTree(groupMonitorCountMap.values(), userOrgMap, type));
        // ?????????????????????
        treeJsonArr.addAll(JsonUtil.getOrgTree(userOrgMap.values(), type));
        List<GroupMonitorBindDO> groupMonitorBindInfoLis = CollectionUtils.isEmpty(groupIds)
                ? Collections.emptyList()
                : groupMonitorDao.getGroupMonitorBindInfoListByIds(groupIds, null);
        // ???????????????????????????
        List<JSONObject> monitorObjTreeNodeList =
            groupMonitorBindInfoLis.stream().sorted(Comparator.comparing(GroupMonitorBindDO::getMoName))
                .map(obj -> JsonUtil.assembleMonitorObjectTreeNode(obj, false)).collect(Collectors.toList());
        treeJsonArr.addAll(monitorObjTreeNodeList);
        resultJsonObj.put("size", monitorNumber);
        resultJsonObj.put("tree", treeJsonArr);
        return resultJsonObj;
    }

    @Override
    public int getMonitorTreeFuzzyCount(MonitorTreeReq query) {
        //???????????????????????????
        String queryType = query.getQueryType();
        String keyword = query.getKeyword();
        Map<String, Object> orgAndGroup = getOrgAndGroupList(keyword, queryType, false);
        if (orgAndGroup == null) {
            return 0;
        }
        List<GroupDTO> groupList = (List<GroupDTO>) orgAndGroup.get(GROUP_TYPE);
        if (CollectionUtils.isEmpty(groupList)) {
            return 0;
        }
        List<String> groupIds = groupList.stream().map(GroupDTO::getId).collect(Collectors.toList());
        if (Objects.equals(ORG_TYPE, queryType) || Objects.equals(GROUP_TYPE, queryType)) {
            keyword = null;
        }

        //???????????????????????????????????????
        MonitorQuery monitorQuery =
            new MonitorQuery(groupIds, queryType, keyword, query.getDeviceTypes(), query.getStatus(),
                query.getMonitorType(), query.getVehicleTypeName());
        MonitorTreeHolder holder = new MonitorTreeHolder();
        holder.setNeedAccStatus(query.isNeedAccStatus());
        Set<String> monitorIds = getMonitorIds(monitorQuery, holder);
        if (monitorIds.isEmpty()) {
            return 0;
        }

        //??????????????????
        int count = 0;
        for (Map.Entry<String, Set<String>> entry : holder.getGroupMonitorSetMap().entrySet()) {
            Set<String> groupMonitorIds = entry.getValue();
            for (String monitorId : groupMonitorIds) {
                if (monitorIds.contains(monitorId)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public JSONArray getByGroupId(Collection<String> groupIds, MonitorTreeReq monitorTreeQuery) {
        //?????????????????????????????????????????????ID??????
        MonitorTreeHolder holder = new MonitorTreeHolder();
        holder.setGroupList(groupService.getGroupsById(groupIds));
        holder.setGroupMonitorSetMap(groupMonitorService.getGroupMonitorIdSet(groupIds));
        return getTreeNodes(monitorTreeQuery, holder, true);
    }

    @Override
    public JSONArray getByOrgDn(String orgDn, MonitorTreeReq monitorTreeQuery) {
        //??????????????????????????????????????????
        List<GroupDTO> groupList = groupService.getUserGroupByOrgDn(orgDn);
        if (CollectionUtils.isEmpty(groupList)) {
            return new JSONArray();
        }
        MonitorTreeHolder holder = new MonitorTreeHolder();
        List<String> groupIds = groupList.stream().map(GroupDTO::getId).collect(Collectors.toList());
        holder.setGroupList(groupList);
        holder.setGroupMonitorSetMap(groupMonitorService.getGroupMonitorIdSet(groupIds));

        //???????????????????????????
        return getTreeNodes(monitorTreeQuery, holder, true);
    }

    @Override
    public JSONArray reportFuzzySearch(String type, String queryParam, String queryType, String treeType) {
        JSONArray resultTree = new JSONArray();
        List<OrganizationLdap> useOrgList = userService.getCurrentUseOrgList();
        if (CollectionUtils.isEmpty(useOrgList)) {
            return new JSONArray();
        }
        Map<String, OrganizationLdap> orgMap =
            useOrgList.stream().collect(Collectors.toMap(OrganizationLdap::getUuid, Function.identity()));
        Set<String> queryGroupOrgIds = new HashSet<>(orgMap.keySet());
        boolean queryParamNotBlank = StringUtils.isNotBlank(queryParam);
        Set<OrganizationLdap> orgResult = new HashSet<>(useOrgList);
        // ??????????????????
        if (Objects.equals(type, "group") && queryParamNotBlank) {
            orgResult.clear();
            List<OrganizationLdap> filterOrgList =
                useOrgList.stream().filter(org -> org.getName().contains(queryParam)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filterOrgList)) {
                return new JSONArray();
            }
            queryGroupOrgIds = new HashSet<>();
            for (OrganizationLdap org : filterOrgList) {
                queryGroupOrgIds.add(org.getUuid());
                TreeUtils.getLowerOrg(orgResult, useOrgList, org);
            }
        }

        List<GroupDTO> resultGroupList;
        // ??????????????????
        if (Objects.equals(type, "assignment") && queryParamNotBlank) {
            resultGroupList = groupDao.getUserGroupList(queryGroupOrgIds, userService.getCurrentUserUuid(),
                StringUtil.mysqlLikeWildcardTranslation(queryParam));
            if (CollectionUtils.isEmpty(resultGroupList)) {
                return new JSONArray();
            }
            Set<String> groupOrgIdSet = resultGroupList.stream().map(GroupDTO::getOrgId).collect(Collectors.toSet());
            orgResult.clear();
            for (String groupOrgId : groupOrgIdSet) {
                TreeUtils.filterOrgByGroupOrgId(orgResult, useOrgList, orgMap.get(groupOrgId).getId().toString());
            }
        } else {
            resultGroupList = groupDao.getUserGroupList(queryGroupOrgIds, userService.getCurrentUserUuid(), null);
        }
        if (resultGroupList.isEmpty()) {
            return new JSONArray();
        }
        Set<String> filterGroupIds = resultGroupList.stream().map(GroupDTO::getId).collect(Collectors.toSet());
        List<GroupMonitorBindDO> groupMonitorBindInfoList;
        // ???????????????
        if (Objects.equals(queryType, "multiple")) {
            groupMonitorBindInfoList = groupMonitorDao.getGroupMonitorBindInfoListByIds(filterGroupIds, null);
            // ?????????
        } else {
            groupMonitorBindInfoList = groupMonitorDao.getGroupMonitorBindInfoListByIds(filterGroupIds, "0");
        }
        if (Objects.equals(type, "vehicle") && queryParamNotBlank) {
            groupMonitorBindInfoList =
                groupMonitorBindInfoList.stream().filter(obj -> obj.getMoName().contains(queryParam))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(groupMonitorBindInfoList)) {
                return new JSONArray();
            }
            // ????????????
            Set<String> monitorGroupIds =
                groupMonitorBindInfoList.stream().map(GroupMonitorBindDO::getGroupId).collect(Collectors.toSet());
            resultGroupList = resultGroupList.stream().filter(obj -> monitorGroupIds.contains(obj.getId()))
                .collect(Collectors.toList());
            // ????????????
            Set<String> groupOrgIdSet = resultGroupList.stream().map(GroupDTO::getOrgId).collect(Collectors.toSet());
            orgResult.clear();
            for (String groupOrgId : groupOrgIdSet) {
                TreeUtils.filterOrgByGroupOrgId(orgResult, useOrgList, orgMap.get(groupOrgId).getId().toString());
            }
        }
        List<OrganizationLdap> sortOrgList =
            orgResult.stream().sorted(Comparator.comparing(OrganizationLdap::getCreateTimestamp))
                .collect(Collectors.toList());
        // ?????????????????????
        resultTree.addAll(JsonUtil.getOrgTree(sortOrgList, treeType));
        // ?????????????????????
        Set<String> resultMoIds =
            groupMonitorBindInfoList.stream().map(GroupMonitorBindDO::getMoId).collect(Collectors.toSet());
        assembleGroupTree(resultGroupList, treeType, orgMap, resultMoIds, resultTree);
        // ???????????????????????????
        List<JSONObject> monitorObjTreeNodeList =
            groupMonitorBindInfoList.stream().sorted(Comparator.comparing(GroupMonitorBindDO::getMoName))
                .map(obj -> JsonUtil.assembleMonitorObjectTreeNode(obj, false)).collect(Collectors.toList());
        resultTree.addAll(monitorObjTreeNodeList);

        return resultTree;
    }

    @Override
    public Set<String> getMonitorIdSet(String pid, String type, Integer webType) {
        if (!Objects.equals(webType, 3) && !Objects.equals(webType, 4)) {
            return monitorService.getMonitorByGroupOrOrgDn(pid, type, null);
        }
        List<String> groupIds;
        if (Objects.equals(type, "org")) {
            groupIds = groupService.getUserGroupByOrgDn(pid).stream().map(GroupDTO::getId).collect(Collectors.toList());
        } else {
            groupIds = Collections.singletonList(pid);
        }
        if (CollectionUtils.isEmpty(groupIds)) {
            return new HashSet<>();
        }
        Set<String> monitorIds = new HashSet<>();
        if (Objects.equals(webType, 3)) {
            monitorIds = vehicleDao.get809ForwardIds(groupIds);
        }

        if (Objects.equals(webType, 4)) {
            Set<String> bindIcCardIds = adasVehicleCardNumDao.findAllBindIcCardVehicleId();
            List<RedisKey> redisKeys = RedisKeyEnum.GROUP_MONITOR.ofs(groupIds);
            monitorIds = RedisHelper.batchGetSet(redisKeys);
            monitorIds.retainAll(bindIcCardIds);
        }

        return monitorIds;
    }

    /**
     * ???????????????
     * @param assignmentList ????????????
     * @param type           type
     * @param orgMap         ????????????
     * @param resultMoIds    ?????????????????????????????????id
     * @param resultTree     tree
     */
    public void assembleGroupTree(List<GroupDTO> assignmentList, String type, Map<String, OrganizationLdap> orgMap,
        Set<String> resultMoIds, JSONArray resultTree) {
        //???????????????????????????id
        Set<String> onLineMoIdSet = monitorService.getAllOnLineMonitor();
        Set<String> groupIds = assignmentList.stream().map(GroupDTO::getId).collect(Collectors.toSet());
        Map<String, Set<String>> groupMonitorIdSetMap = groupMonitorService.getGroupMonitorIdSet(groupIds);
        for (GroupDTO groupDTO : assignmentList) {
            String groupId = groupDTO.getId();
            OrganizationLdap orgInfo = orgMap.get(groupDTO.getOrgId());
            // ???????????????
            JSONObject assignmentObj = new JSONObject();
            Set<String> groupMonitorIdSet = groupMonitorIdSetMap.get(groupId);
            int num = 0;
            int onLine = 0;
            if (CollectionUtils.isNotEmpty(groupMonitorIdSet)) {
                Set<String> filterMoIds = Sets.intersection(resultMoIds, groupMonitorIdSet);
                num = filterMoIds.size();
                onLine = Sets.intersection(onLineMoIdSet, filterMoIds).size();
            }
            // ?????????????????????????????????
            assignmentObj.put("canCheck", num);
            assignmentObj.put("onLine", onLine);
            assignmentObj.put("offLine", num - onLine);
            assignmentObj.put("id", groupId);
            assignmentObj.put("pId", orgInfo.getId().toString());
            assignmentObj.put("name", groupDTO.getName());
            assignmentObj.put("type", "assignment");
            assignmentObj.put("iconSkin", "assignmentSkin");
            assignmentObj.put("pName", orgInfo.getName());
            // ?????????????????????
            if ("single".equals(type)) {
                assignmentObj.put("nocheck", true);
            }
            resultTree.add(assignmentObj);
        }
    }

    /**
     * ??????????????????
     * @param query           ??????????????????
     * @param holder          ?????????????????????????????? ?????????????????????????????????????????????
     * @param onlyMonitorNode ?????????????????????????????????
     * @return ???????????????
     */
    private JSONArray getTreeNodes(MonitorTreeReq query, MonitorTreeHolder holder, boolean onlyMonitorNode) {
        //1???????????????????????????????????????
        Set<String> monitorIds = query.getMonitorIds() == null ? getMonitorSet(query, holder) : query.getMonitorIds();
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new JSONArray();
        }

        //2???????????????????????????????????????
        List<BindDTO> bindList = getBindList(monitorIds, holder, query.getWebType(), query.isNeedQuitPeople());

        //3??????????????????????????????????????????ACC??????
        Map<String, MonitorAccStatus> accAndStatusMap = holder.getAccAndStatusMap();
        boolean needAccStatus = query.isNeedAccStatus();
        if (needAccStatus || Objects.nonNull(query.getStatus()) || query.isNeedOnlineStatus()) {
            accAndStatusMap =
                accAndStatusMap == null ? monitorService.getAccAndStatus(monitorIds, needAccStatus) : accAndStatusMap;
            holder.setAccAndStatusMap(accAndStatusMap);
        }

        //4????????????????????????????????????????????????????????????
        Map<String, List<VideoChannelSetting>> channelSettingMap = null;
        if (query.isNeedCarousel()) {
            channelSettingMap = getChannelMap(monitorIds);
        }

        //5??????????????????????????????????????????
        JSONArray treeNodes;
        if (onlyMonitorNode) {
            treeNodes = new JSONArray();
        } else {
            Integer limitNum = query.getLimitMonitorNum();
            boolean isBigData = Objects.nonNull(limitNum) && monitorIds.size() > limitNum;
            treeNodes = filterOrgAndGroup(bindList, holder, isBigData);
        }

        //6??????????????????
        boolean isParent = Objects.equals(MonitorTreeReq.REAL_TIME_VIDEO, query.getWebType());
        for (BindDTO bindDTO : bindList) {
            treeNodes
                .add(buildMonitorTreeNode(bindDTO, query.isChecked(), isParent, channelSettingMap, accAndStatusMap));
        }
        return treeNodes;
    }

    private Set<String> getMonitorSet(MonitorTreeReq query, MonitorTreeHolder holder) {
        MonitorQuery monitorQuery =
            new MonitorQuery(query.getDeviceTypes(), query.getQueryType(), query.getKeyword(), query.getStatus());
        monitorQuery.setVehicleTypeName(query.getVehicleTypeName());
        monitorQuery.setMonitorType(query.getMonitorType());
        if (Objects.isNull(holder)) {
            holder = new MonitorTreeHolder();
        }
        holder.setNeedAccStatus(query.isNeedAccStatus());
        return getMonitorIds(monitorQuery, holder);
    }

    private List<BindDTO> getBindList(Set<String> monitorIds, MonitorTreeHolder holder, Integer webType,
        boolean needQuitPeople) {
        List<String> fields = Arrays.asList(MONITOR_TREE_NODE_FIELD);
        List<BindDTO> bindList =
            getMonitorList(monitorIds, holder.getGroupMonitorSetMap(), holder.getGroupList(), fields, needQuitPeople);
        //????????????????????????????????????808???????????????
        if (Objects.equals(MonitorTreeReq.REAL_TIME_VIDEO, webType)) {
            bindList = bindList.stream().filter(o -> ProtocolTypeUtil.checkDeviceType2013And2019(o.getDeviceType()))
                .collect(Collectors.toList());
        }
        return bindList;
    }

    private MonitorTreeHolder getOrgAndGroup(MonitorTreeReq query) {
        String queryType = query.getQueryType();
        String keyword = null;
        if (Objects.equals(queryType, ORG_TYPE) || Objects.equals(queryType, GROUP_TYPE)) {
            keyword = query.getKeyword();
            query.setKeyword(null);
        }
        return getOrgAndGroupNodes(queryType, keyword, query.getType(), query.isNeedMonitorCount(),
            query.isNeedOnlineMonitorCount(), true, false);
    }

    /**
     * ?????????????????????????????????ID
     * @param monitorQuery ???????????????????????????
     * @param holder       ???????????????????????? ????????????
     * @return ?????????Id??????
     */
    private Set<String> getMonitorIds(MonitorQuery monitorQuery, MonitorTreeHolder holder) {
        //1??????????????????????????????????????????????????????????????????????????????
        Map<String, Set<String>> groupMonitorSetMap = holder.getGroupMonitorSetMap();
        if (groupMonitorSetMap == null && CollectionUtils.isNotEmpty(monitorQuery.getGroupIds())) {
            groupMonitorSetMap = groupMonitorService.getGroupMonitorIdSet(monitorQuery.getGroupIds());
            holder.setGroupMonitorSetMap(groupMonitorSetMap);
            holder.setMonitorSet(null);
        }
        Set<String> monitorIds = holder.getMonitorSet();

        //2???????????????????????????????????????
        if (StringUtils.isNotBlank(monitorQuery.getMonitorType())) {
            monitorService.filterByMonitorType(monitorQuery.getMonitorType(), monitorIds);
        }
        //3??????????????????????????????????????????
        if (StringUtils.isNotBlank(monitorQuery.getKeyword())) {
            monitorService.filterByKeyword(monitorQuery.getKeyword(), monitorQuery.getQueryType(), monitorIds);
        }

        //4???????????????????????????????????????
        if (CollectionUtils.isNotEmpty(monitorQuery.getDeviceTypes())) {
            monitorService.filterByDeviceType(monitorQuery.getDeviceTypes(), monitorIds);
        }
        //5?????????????????????????????????
        if (StringUtils.isNotBlank(monitorQuery.getVehicleTypeName())) {
            monitorService.filterByVehicleTypeName(monitorQuery.getVehicleTypeName(), monitorIds);
        }
        //6???????????????????????????
        if (Objects.nonNull(monitorQuery.getStatus())) {
            Map<String, MonitorAccStatus> accAndStatusMap = holder.getAccAndStatusMap();
            if (accAndStatusMap == null) {
                accAndStatusMap = monitorService.getAccAndStatus(monitorIds, holder.isNeedAccStatus());
                holder.setAccAndStatusMap(accAndStatusMap);
            }
            monitorService.filterByOnlineStatus(accAndStatusMap, monitorIds, monitorQuery.getStatus());
        }
        holder.setMonitorSet(monitorIds);
        return monitorIds;
    }

    private JSONArray filterOrgAndGroup(List<BindDTO> bindList, MonitorTreeHolder holder, boolean bigData) {
        //????????????????????????????????????
        Set<String> monitorGroupIds = new HashSet<>();

        //???????????????????????????????????????
        Map<String, Integer> onLineCountMap = new HashMap<>(CommonUtil.ofMapCapacity(holder.getGroupList().size()));
        Map<String, Integer> monitorCountMap = new HashMap<>(CommonUtil.ofMapCapacity(holder.getGroupList().size()));
        Set<String> onlineIds = holder.getOnlineIds();
        boolean isCountOnLine = onlineIds != null;
        for (BindDTO bindDTO : bindList) {
            String groupId = bindDTO.getGroupId();
            monitorGroupIds.add(groupId);
            Integer monitorCount = monitorCountMap.getOrDefault(groupId, 0);
            monitorCountMap.put(groupId, monitorCount + 1);
            if (isCountOnLine && onlineIds.contains(bindDTO.getId())) {
                Integer onlineNum = onLineCountMap.getOrDefault(groupId, 0);
                onLineCountMap.put(groupId, onlineNum + 1);
            }
        }

        //?????????????????????????????????
        List<GroupDTO> groupList = holder.getGroupList().stream().filter(o -> monitorGroupIds.contains(o.getId()))
            .collect(Collectors.toList());

        //??????????????????????????????
        Set<String> groupOrgIds = groupList.stream().map(GroupDTO::getOrgId).collect(Collectors.toSet());
        List<OrganizationLdap> orgList = OrganizationUtil.filterOrgListByUuid(holder.getOrgList(), groupOrgIds);
        Set<String> orgDnList = orgList.stream().map(OrganizationLdap::getCid).collect(Collectors.toSet());

        //??????????????????????????????????????????????????????
        JSONArray treeNodes = new JSONArray();
        for (Object object : holder.getTreeNodes()) {
            JSONObject treeNode = (JSONObject) object;
            if (Objects.equals("assignment", treeNode.getString("type"))) {
                String groupId = treeNode.getString("id");
                if (monitorGroupIds.contains(groupId)) {
                    if (bigData) {
                        treeNode.put("isParent", true);
                    }
                    Integer monitorCount = monitorCountMap.getOrDefault(groupId, 0);
                    if (treeNode.getInteger("count") != null) {
                        treeNode.put("count", monitorCount);
                    }
                    treeNode.put("canCheck", monitorCount);
                    if (isCountOnLine) {
                        Integer onlineNum = onLineCountMap.getOrDefault(groupId, 0);
                        treeNode.put("onLine", onlineNum);
                        treeNode.put("offLine", monitorCount - onlineNum);
                    }
                    treeNodes.add(treeNode);
                }
            }
            if (Objects.equals("group", treeNode.getString("type"))) {
                if (orgDnList.contains(treeNode.getString("id"))) {
                    treeNodes.add(treeNode);
                }
            }
        }
        holder.setGroupList(groupList);
        holder.setOrgList(orgList);
        holder.setTreeNodes(treeNodes);
        return treeNodes;
    }

    private Map<String, List<VideoChannelSetting>> getChannelMap(Collection<String> monitorIds) {
        // ???????????????????????????????????????????????????
        Map<String, List<VideoChannelSetting>> channelSettingMap = null;
        List<VideoChannelSetting> videoChannelList = videoChannelSettingDao.getVideoChannelByVehicleIds(monitorIds);
        if (CollectionUtils.isNotEmpty(videoChannelList)) {
            channelSettingMap =
                videoChannelList.stream().collect(Collectors.groupingBy(VideoChannelSetting::getVehicleId));
        }
        return channelSettingMap;
    }

    /**
     * ???????????????????????????
     * @param monitorIds         ??????????????????
     * @param groupMonitorSetMap ??????-??????????????????Map
     * @param groupList          ????????????
     * @param fields             ??????????????????????????????
     * @param needQuitPeople     true ????????????????????????
     * @return ????????????????????????
     */
    private List<BindDTO> getMonitorList(Set<String> monitorIds, Map<String, Set<String>> groupMonitorSetMap,
        List<GroupDTO> groupList, List<String> fields, boolean needQuitPeople) {
        //????????????????????????????????????
        List<RedisKey> redisKeys = RedisKeyEnum.MONITOR_INFO.ofs(monitorIds);
        List<Map<String, String>> monitorList = RedisHelper.batchGetHashMap(redisKeys, fields);
        Map<String, Map<String, String>> monitorMap = new HashMap<>(CommonUtil.ofMapCapacity(monitorList.size()));
        monitorList.forEach(monitor -> monitorMap.put(monitor.get("id"), monitor));

        //????????????id-???????????????Map
        Map<String, String> groupMap = AssembleUtil.collectionToMap(groupList, GroupDTO::getId, GroupDTO::getName);

        //???????????????????????????????????????????????????????????????
        List<BindDTO> bindList = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : groupMonitorSetMap.entrySet()) {
            String groupId = entry.getKey();
            for (String monitorId : entry.getValue()) {
                if (monitorMap.containsKey(monitorId)) {
                    BindDTO bindDTO = MapUtil.mapToObj(monitorMap.get(monitorId), BindDTO.class);
                    bindDTO.setGroupId(groupId);
                    bindDTO.setGroupName(groupMap.get(groupId));
                    bindList.add(bindDTO);
                }
            }
        }

        if (needQuitPeople) {
            List<LeaveJobPersonnel> leaveJobPersonnelList = peopleDao.getLeaveJobPersonnelList(groupMap.keySet());
            for (LeaveJobPersonnel personnel : leaveJobPersonnelList) {
                BindDTO bindDTO = new BindDTO();
                bindDTO.setId(personnel.getPeopleId());
                bindDTO.setName(personnel.getPeopleNumber());
                bindDTO.setGroupId(personnel.getAssignmentId());
                bindDTO.setGroupName(personnel.getAssignmentName());
                bindDTO.setMonitorType(MonitorTypeEnum.PEOPLE.getType());
                bindList.add(bindDTO);
            }
        }
        // ???????????????????????????????????????
        bindList.sort(new Comparator<BindDTO>() {
            Collator collator = Collator.getInstance(Locale.CHINA);

            @Override
            public int compare(BindDTO o1, BindDTO o2) {
                CollationKey key1 = collator.getCollationKey(o1.getName());
                CollationKey key2 = collator.getCollationKey(o2.getName());
                return key1.compareTo(key2);
            }
        });
        return bindList;
    }

    /**
     * ?????????????????????????????????
     * @param monitor           ????????????ID
     * @param isChecked         ?????????????????????
     * @param isParent          ????????????????????????
     * @param channelSettingMap ????????????ID-???????????????????????????????????????
     * @param accStatusMap      ????????????ID- ????????????ACC????????????
     * @return ?????????
     */
    private JSONObject buildMonitorTreeNode(BindDTO monitor, boolean isChecked, boolean isParent,
        Map<String, List<VideoChannelSetting>> channelSettingMap, Map<String, MonitorAccStatus> accStatusMap) {
        JSONObject treeNode = new JSONObject();
        treeNode.put("id", monitor.getId());
        MonitorTypeEnum monitorTypeEnum = MonitorTypeEnum.getByType(monitor.getMonitorType());
        if (Objects.nonNull(monitorTypeEnum)) {
            treeNode.put("type", monitorTypeEnum.getEnName());
            treeNode.put("iconSkin", monitorTypeEnum.getIconSkin());
        }
        treeNode.put("pId", monitor.getGroupId());
        treeNode.put("name", monitor.getName());
        setTreeNode(treeNode, "deviceNumber", monitor.getDeviceNumber());
        setTreeNode(treeNode, "deviceType", monitor.getDeviceType());
        setTreeNode(treeNode, "isVideo", monitor.getIsVideo());
        setTreeNode(treeNode, "plateColor", monitor.getPlateColor());
        setTreeNode(treeNode, "simcardNumber", monitor.getSimCardNumber());
        setTreeNode(treeNode, "professional", monitor.getProfessionalNames());
        setTreeNode(treeNode, "assignName", monitor.getGroupName());
        setTreeNode(treeNode, "aliases", monitor.getAlias());
        if (isChecked) {
            treeNode.put("checked", true);
        }
        if (isParent) {
            treeNode.put("isParent", true);
        }
        if (MapUtils.isNotEmpty(accStatusMap) && accStatusMap.containsKey(monitor.getId())) {
            MonitorAccStatus monitorAccStatus = accStatusMap.get(monitor.getId());
            treeNode.put("status", monitorAccStatus.getStatus());
            treeNode.put("acc", monitorAccStatus.getAcc());
        }
        if (channelSettingMap != null) {
            // ?????????????????????
            videoCarouselService.putChannelProperties(monitor.getId(), treeNode, channelSettingMap);
        }
        return treeNode;
    }

    private void setTreeNode(JSONObject treeNode, String filedName, Object value) {
        if (Objects.nonNull(value)) {
            treeNode.put(filedName, value);
        }
    }

    /**
     * ????????????+???????????????
     * @param keyword        ???????????????
     * @param queryType      group:????????????????????? org????????????????????????
     * @param type           ????????????????????? single???nocheck=true multiple ????????? nocheck
     * @param needMonitorNum ????????????????????????????????????????????????
     * @param needOnlineNum  ?????????????????????????????????????????????????????????
     * @param needFilterOrg  ?????????????????????????????????????????? true ????????????  false ????????????
     * @param isBigData      ???????????? ?????????????????? true???isParent = true
     * @return MonitorTreeHolder ????????????+??????????????????????????????????????????
     */
    private MonitorTreeHolder getOrgAndGroupNodes(String queryType, String keyword, String type, boolean needMonitorNum,
        boolean needOnlineNum, boolean needFilterOrg, boolean isBigData) {
        //???????????????????????????????????????
        Map<String, Object> orgAndGroup = getOrgAndGroupList(keyword, queryType, needFilterOrg);
        if (orgAndGroup == null) {
            return null;
        }
        List<OrganizationLdap> orgList = (List<OrganizationLdap>) orgAndGroup.get(ORG_TYPE);
        List<GroupDTO> groupList = (List<GroupDTO>) orgAndGroup.get(GROUP_TYPE);

        //???????????????????????????????????????
        if (CollectionUtils.isEmpty(orgList)) {
            return null;
        }
        //???????????????,????????????????????????????????????
        MonitorTreeHolder holder = new MonitorTreeHolder();
        if (CollectionUtils.isEmpty(groupList)) {
            holder.setTreeNodes(JsonUtil.getOrgTree(orgList, type));
            return holder;
        }

        //????????????-??????????????????????????????Map
        List<String> groupIds = groupList.stream().map(GroupDTO::getId).collect(Collectors.toList());
        Map<String, Set<String>> groupMonitorMap = groupMonitorService.getGroupMonitorIdSet(groupIds);

        //???????????????????????????????????????????????????????????????????????????
        Set<String> onLineMonitorIds = null;
        if (needOnlineNum) {
            onLineMonitorIds = monitorService.getAllOnLineMonitor();
        }


        //???????????????????????????????????????????????????????????????
        groupService.getGroupMonitorCount(groupList, groupMonitorMap, onLineMonitorIds);

        //??????????????????
        JSONArray treeNodes = new JSONArray();
        treeNodes.addAll(groupService.buildTreeNodes(groupList, orgList, type, isBigData, needMonitorNum));
        treeNodes.addAll(JsonUtil.getOrgTree(orgList, type));

        //??????????????????
        holder.setGroupList(groupList);
        holder.setTreeNodes(treeNodes);
        holder.setGroupMonitorSetMap(groupMonitorMap);
        holder.setOnlineIds(onLineMonitorIds);
        holder.setOrgList(orgList);
        return holder;
    }

    /**
     * ?????????????????????--????????????????????????
     * @param keyword   ???????????????
     * @param filterOrg ???????????????????????????????????? true ?????? false ??????????????????
     * @param queryType group:????????????????????? org????????????????????????
     * @return ??????????????? key=group ???????????????org????????????
     */
    private Map<String, Object> getOrgAndGroupList(String keyword, String queryType, boolean filterOrg) {
        //??????????????????????????????
        List<OrganizationLdap> orgList = userService.getCurrentUseOrgList();

        //???????????????????????????
        String orgKeyword = Objects.equals(ORG_TYPE, queryType) ? keyword : null;
        List<String> orgIds = getFuzzyOrgIds(orgList, orgKeyword);
        if (CollectionUtils.isEmpty(orgIds)) {
            return null;
        }

        //??????????????????????????????????????????
        String groupKeyword = Objects.equals(GROUP_TYPE, queryType) ? keyword : null;
        List<GroupDTO> groupList = groupDao.getUserGroupList(orgIds, userService.getCurrentUserUuid(), groupKeyword);

        //????????????????????????????????????????????????????????????????????????
        if (filterOrg || StringUtils.isNotBlank(groupKeyword)) {
            Set<String> filterOrgIds = groupList.stream().map(GroupDTO::getOrgId).collect(Collectors.toSet());
            orgList = OrganizationUtil.filterOrgListByUuid(orgList, filterOrgIds);
        }
        //?????????????????????
        return ImmutableMap.of(ORG_TYPE, orgList, GROUP_TYPE, groupList);
    }

    /**
     * ???????????????????????????
     * @param orgList ??????ID
     * @param keyword ???????????????
     * @return ?????????????????????ID
     */
    private List<String> getFuzzyOrgIds(List<OrganizationLdap> orgList, String keyword) {
        List<String> orgIds = new ArrayList<>();
        boolean isFuzzyOrg = StringUtils.isNotBlank(keyword);
        for (OrganizationLdap organizationLdap : orgList) {
            if (!isFuzzyOrg) {
                orgIds.add(organizationLdap.getUuid());
                continue;
            }
            if (organizationLdap.getName().contains(keyword)) {
                orgIds.add(organizationLdap.getUuid());
            }
        }
        //???????????????????????????????????????????????????
        if (isFuzzyOrg) {
            List<OrganizationLdap> filterList = OrganizationUtil.filterOrgListByUuid(orgList, orgIds);
            orgList.clear();
            orgList.addAll(filterList);
        }
        return orgIds;
    }

}
