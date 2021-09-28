package com.zw.talkback.service.dispatch.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.form.AssignmentGroupForm;
import com.zw.platform.domain.basicinfo.form.AssignmentUserForm;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.ClusterInfo;
import com.zw.talkback.domain.basicinfo.InterlocutorInfo;
import com.zw.talkback.domain.basicinfo.TempAssignmentInterlocutor;
import com.zw.talkback.domain.basicinfo.form.ClusterForm;
import com.zw.talkback.domain.dispatch.DispatchErrorMessageEnum;
import com.zw.talkback.domain.intercom.ErrorMessageEnum;
import com.zw.talkback.repository.mysql.ClusterDao;
import com.zw.talkback.repository.mysql.IntercomModelDao;
import com.zw.talkback.repository.mysql.IntercomPersonnelDao;
import com.zw.talkback.repository.mysql.PeopleBasicInfoDao;
import com.zw.talkback.repository.mysql.SkillDao;
import com.zw.talkback.service.baseinfo.ClusterService;
import com.zw.talkback.service.baseinfo.IntercomCallNumberService;
import com.zw.talkback.service.dispatch.MonitoringDispatchService;
import com.zw.talkback.util.CallNumberExhaustException;
import com.zw.talkback.util.JsonUtil;
import com.zw.talkback.util.OrganizationUtil;
import com.zw.talkback.util.TalkCallUtil;
import com.zw.talkback.util.common.HttpClientUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 监控调度逻辑实现层
 */
@Service
public class MonitoringDispatchServiceImpl implements MonitoringDispatchService {

    private static Logger log = LogManager.getLogger(MonitoringDispatchServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TalkCallUtil talkCallUtil;

    @Autowired
    private ClusterDao clusterDao;

    @Autowired
    private SkillDao skillDao;

    @Autowired
    private IntercomModelDao intercomModelDao;

    @Autowired
    private IntercomPersonnelDao intercomPersonnelDao;

    @Autowired
    private PeopleBasicInfoDao peopleBasicInfoDao;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private IntercomCallNumberService intercomCallNumberService;

    @Autowired
    private LogSearchService logSearchService;

    @Value("${task.serverIP}")
    private String audioServerIP;

    @Value("${task.dispatchService.serverPort}")
    private String dispatchServicePort;

    @Value("${task.eventService.serverPort}")
    private String eventServicePort;

    /**
     * 分组最大监控对象数量
     */
    private static final Integer ASSIGNMENT_MXA_MONITOR_NUM = 400;

    /**
     * 调度服务登录
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean dispatchLoginIn() throws Exception {
        UserLdap currentUser = SystemHelper.getCurrentUser();
        String userName = currentUser.getUsername();
        String userId = currentUser.getId().toString();
        JSONObject dispatchUserLoginInfo = WebSubscribeManager.getInstance().getDispatchUserLoginInfo(userName);
        if (dispatchUserLoginInfo != null) {
            JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
            // 组装用户拥有的临时组id集合
            installUserOwnTempAssignmentIntercomGroupIdList(userName, data, userId);
            dispatchUserLoginInfo.put("data", data);
            return new JsonResultBean(dispatchUserLoginInfo);
        }
        JSONObject dispatchLoginInResultJsonObj = talkCallUtil.dispatchLoginIn(userName, currentUser.getPassword());
        if (dispatchLoginInResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "登录调度服务异常，请联系管理员");
        }
        Integer dispatchLoginInStatusCode = dispatchLoginInResultJsonObj.getInteger("result");
        if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), dispatchLoginInStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT,
                DispatchErrorMessageEnum.getMessage(dispatchLoginInStatusCode));
        }
        // 设置配置的调度服务和通知服务登录的域名和端口
        JSONObject data = dispatchLoginInResultJsonObj.getJSONObject("data");
        data.put("audioServerIP", audioServerIP);
        data.put("dispatchServicePort", dispatchServicePort);
        data.put("eventServicePort", eventServicePort);
        // 是否用于禁言角色
        boolean isOwnPreventSpeechRole = false;
        if (StringUtils.isNotBlank(userId)) {
            Collection<Group> userRoleList = userService.findByMember(userId);
            if (CollectionUtils.isNotEmpty(userRoleList)) {
                isOwnPreventSpeechRole =
                    userRoleList.stream().anyMatch(roleInfo -> Objects.equals("禁言角色", roleInfo.getRoleName()));
            }
        }
        data.put("isOwnPreventSpeechRole", isOwnPreventSpeechRole);
        // 组装用户拥有的临时组id集合
        installUserOwnTempAssignmentIntercomGroupIdList(userName, data, userId);
        dispatchLoginInResultJsonObj.put("data", data);
        // 保存调度用户登录信息
        WebSubscribeManager.getInstance().saveDispatchUserLoginInfo(userName, dispatchLoginInResultJsonObj);
        return new JsonResultBean(dispatchLoginInResultJsonObj);
    }

    /**
     * 组装用户拥有的临时组id集合
     * @param userName 用户名称
     * @param data     数据
     * @param userId   用户id
     */
    private void installUserOwnTempAssignmentIntercomGroupIdList(String userName, JSONObject data, String userId) {
        // 组装用户拥有的临时组id
        String uuid = userService.getUserUuidById(userId);
        String orgId = userId.substring(userId.indexOf(",") + 1);
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        if (Objects.equals(userName, "admin")) {
            // 用户当前组织和所属下级组织信息
            List<OrganizationLdap> userOwnAuthorityOrganizeInfo = userService.getUserOwnAuthorityOrganizeInfo(orgId);
            currentOrganization = updateOrganizationStructure(currentOrganization, userOwnAuthorityOrganizeInfo);
        }
        List<Cluster> userOwnTemporaryAssignment = currentOrganization != null
            ? clusterDao.findUserOwnTemporaryAssignment(uuid, currentOrganization.getUuid()) : new ArrayList<>();
        List<Long> userOwnTempAssignmentIntercomGroupIdList =
            userOwnTemporaryAssignment.stream().map(Cluster::getIntercomGroupId).distinct()
                .collect(Collectors.toList());
        data.put("userOwnTempAssignmentIntercomGroupIdList", userOwnTempAssignmentIntercomGroupIdList);
    }

    @Override
    public OrganizationLdap updateOrganizationStructure(OrganizationLdap currentOrganization,
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo) {
        String currentId = currentOrganization.getId().toString();
        userOwnAuthorityOrganizeInfo
            .removeIf(organization -> Objects.equals(organization.getId().toString(), currentId));
        Optional<OrganizationLdap> optional = userOwnAuthorityOrganizeInfo.stream()
            .filter(organization -> Objects.equals(organization.getPid(), currentId)).findFirst();
        return optional.orElse(null);
    }

    @Override
    public JsonResultBean getInterlocutorTree(Integer interlocutorStatus) throws Exception {
        JSONArray treeInfoJsonArr = new JSONArray();
        UserLdap currentUser = SystemHelper.getCurrentUser();
        String userId = currentUser.getId().toString();
        String uuid = userService.getUserUuidById(userId);
        String orgId = userId.substring(userId.indexOf(",") + 1);
        // 用户当前的所属组织
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        // 用户当前组织和所属下级组织信息
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo = userService.getUserOwnAuthorityOrganizeInfo(orgId);
        // 用户当前组织和所属下级组织id
        List<String> userOwnAuthorityOrganizeId =
            userOwnAuthorityOrganizeInfo.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        // 查询当前用户权限固定分组
        List<Cluster> assignmentList = clusterService.findUserAssignment(uuid, userOwnAuthorityOrganizeId);
        if (Objects.equals(currentUser.getUsername(), "admin")) {
            currentOrganization = updateOrganizationStructure(currentOrganization, userOwnAuthorityOrganizeInfo);
        }
        Cluster firstTaskAssignmentInfo = null;
        if (currentOrganization != null) {
            String currentGroupId = currentOrganization.getUuid();
            // 用户拥有的任务组
            List<Cluster> userOwnTaskAssignment = clusterDao.findUserOwnTaskAssignment(uuid, currentGroupId);
            for (Cluster assignment : userOwnTaskAssignment) {
                assignment.setGroupId("taskOrganization");
            }
            assignmentList.addAll(userOwnTaskAssignment);
            firstTaskAssignmentInfo =
                CollectionUtils.isNotEmpty(userOwnTaskAssignment) ? userOwnTaskAssignment.get(0) : null;
            if (firstTaskAssignmentInfo != null) {
                // 组装第一个任务组内的对讲对象
                JsonResultBean installTaskAssignmentMonitorInfoToTreeResult =
                    installAssignmentMonitorInfoToTree(firstTaskAssignmentInfo, treeInfoJsonArr, interlocutorStatus,
                        null);
                if (!installTaskAssignmentMonitorInfoToTreeResult.isSuccess()) {
                    return installTaskAssignmentMonitorInfoToTreeResult;
                }
            }
        }
        userOwnAuthorityOrganizeInfo =
            new ArrayList<>(OrganizationUtil.filterOrgListNew(userOwnAuthorityOrganizeInfo, assignmentList));
        if (currentOrganization != null && CollectionUtils.isNotEmpty(userOwnAuthorityOrganizeInfo)) {
            // 添加任务组组织
            userOwnAuthorityOrganizeInfo.add(1, installTaskOrganization(currentOrganization.getId().toString()));
            // 添加临时组组织
            userOwnAuthorityOrganizeInfo.add(1, installTemporaryOrganization(currentOrganization.getId().toString()));
        }
        installAssignmentInfoToTree(assignmentList, userOwnAuthorityOrganizeInfo, treeInfoJsonArr,
            firstTaskAssignmentInfo, false);
        treeInfoJsonArr.addAll(JsonUtil.getGroupTree(userOwnAuthorityOrganizeInfo, null, true));
        // 设置树结构中临时组和任务组的位置在当前组织下的第一和第二
        setUpTaskAndTemporaryLocation(treeInfoJsonArr);
        Object treeInfo = ZipUtil.compress(treeInfoJsonArr.toJSONString());
        return new JsonResultBean(treeInfo);
    }

    /**
     * 组装对讲组内的对讲对象
     * @param firstAssignmentInfo          第一个对讲组信息
     * @param treeInfoJsonArr              树
     * @param interlocutorStatus           0:全部; 1:在线; 2:离线;
     * @param needFilterInterlocutorIdList 需要过滤的对讲对象id集合
     * @return JsonResultBean
     */
    private JsonResultBean installAssignmentMonitorInfoToTree(Cluster firstAssignmentInfo, JSONArray treeInfoJsonArr,
        Integer interlocutorStatus, List<Long> needFilterInterlocutorIdList) {
        String assignmentId = firstAssignmentInfo.getId();
        String assignmentName = firstAssignmentInfo.getName();
        Long intercomGroupId = firstAssignmentInfo.getIntercomGroupId();
        // 对讲组内用户
        JSONArray interlocutorAssignmentMemberArr = new JSONArray();
        // 获取对讲组内用户
        JsonResultBean getGroupMemberListResult =
            getGroupMemberList(intercomGroupId, interlocutorAssignmentMemberArr, interlocutorStatus);
        if (!getGroupMemberListResult.isSuccess()) {
            return getGroupMemberListResult;
        }
        Integer monitorNum = 0;
        if (interlocutorAssignmentMemberArr.size() > 0) {
            List<Long> interlocutorIdList =
                interlocutorAssignmentMemberArr.stream().map(info -> ((JSONObject) info).getLong("userId"))
                    .collect(Collectors.toList());
            // 在临时组内对讲对象id
            List<TempAssignmentInterlocutor> inTemporaryAssignmentInterlocutorInfoList =
                clusterDao.findInTemporaryAssignmentInterlocutorInfo(interlocutorIdList);
            List<InterlocutorInfo> interlocutorInfoList =
                intercomPersonnelDao.getInterlocutorInfoByInterlocutorIdList(interlocutorIdList);
            // 是否是临时组
            boolean isTempAssignment = Objects.equals(Integer.valueOf(firstAssignmentInfo.getFlag()), 3);
            for (int i = 0, len = interlocutorAssignmentMemberArr.size(); i < len; i++) {
                JSONObject interlocutorInfoJsonObj = interlocutorAssignmentMemberArr.getJSONObject(i);
                Long interlocutorId = interlocutorInfoJsonObj.getLong("userId");
                // 如果需要展开的不是临时组 过滤掉在临时组内的对讲对象
                if (!isTempAssignment) {
                    if (inTemporaryAssignmentInterlocutorInfoList.stream()
                        .anyMatch(info -> Objects.equals(info.getInterlocutorId(), interlocutorId))) {
                        continue;
                    }
                }
                if (CollectionUtils.isNotEmpty(needFilterInterlocutorIdList) && needFilterInterlocutorIdList
                    .contains(interlocutorId)) {
                    continue;
                }
                Optional<InterlocutorInfo> optional = interlocutorInfoList.stream()
                    .filter(info -> Objects.equals(info.getInterlocutorId(), interlocutorId)).findFirst();
                if (!optional.isPresent()) {
                    continue;
                }
                InterlocutorInfo info = optional.get();
                interlocutorInfoList.remove(info);
                interlocutorInfoJsonObj.put("monitorType", info.getMonitorType());
                // 组装对讲对象树信息
                JSONObject interlocutorInfo =
                    installInterlocutorTreeInfo(interlocutorInfoJsonObj, interlocutorId, assignmentId, assignmentName,
                        info.getMonitorId());
                treeInfoJsonArr.add(interlocutorInfo);
                monitorNum++;
            }
        }
        firstAssignmentInfo.setMNum(monitorNum);
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 获取对讲组内用户
     * @param intercomGroupId                 对讲组id
     * @param interlocutorAssignmentMemberArr 返回的结果(对讲组内用户)
     * @param interlocutorStatus              0:全部; 1:在线; 2:离线;
     * @return JsonResultBean
     */
    private JsonResultBean getGroupMemberList(Long intercomGroupId, JSONArray interlocutorAssignmentMemberArr,
        Integer interlocutorStatus) {
        JSONObject dispatchUserLoginInfo =
            WebSubscribeManager.getInstance().getDispatchUserLoginInfo(SystemHelper.getCurrentUsername());
        if (dispatchUserLoginInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "调度服务未登录");
        }
        JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
        JSONObject queryInGroupMemberListResultJsonObj =
            talkCallUtil.queryInGroupMemberList(data.getLong("custId"), intercomGroupId, null, null, 1, 500);
        if (queryInGroupMemberListResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询对讲组内用户异常，请联系管理员");
        }
        Integer queryInGroupMemberListStatusCode = queryInGroupMemberListResultJsonObj.getInteger("result");
        if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), queryInGroupMemberListStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT,
                DispatchErrorMessageEnum.getMessage(queryInGroupMemberListStatusCode));
        }
        JSONArray records = queryInGroupMemberListResultJsonObj.getJSONObject("data").getJSONArray("records");
        if (records != null && records.size() > 0) {
            boolean isNeedFilterStatus = interlocutorStatus != null && !Objects.equals(interlocutorStatus, 0);
            Integer finalInterlocutorStatus =
                interlocutorStatus != null ? Objects.equals(interlocutorStatus, 2) ? 0 : interlocutorStatus : null;
            //过滤掉除对讲对象外的用户和在线状态不同的
            records.removeIf(info -> (isNeedFilterStatus && !Objects
                .equals(((JSONObject) info).getInteger("audioOnlineStatus"), finalInterlocutorStatus)) || !Objects
                .equals(((JSONObject) info).getInteger("type"), 0));
            interlocutorAssignmentMemberArr.addAll(records);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 组装对讲对象树信息
     * @param interlocutorInfoJsonObj 查询出的结果
     * @param interlocutorId          对讲对象id
     * @param assignmentId            分组id
     * @param assignmentName          分组名称
     * @param monitorId               监控对象id
     * @return JSONObject
     */
    private JSONObject installInterlocutorTreeInfo(JSONObject interlocutorInfoJsonObj, Long interlocutorId,
        String assignmentId, String assignmentName, String monitorId) {
        JSONObject interlocutorInfo = new JSONObject();
        if ("0".equals(interlocutorInfoJsonObj.get("monitorType"))) {
            interlocutorInfo.put("iconSkin", "vehicleSkin");
            interlocutorInfo.put("type", "vehicle");
        } else if ("1".equals(interlocutorInfoJsonObj.get("monitorType"))) {
            interlocutorInfo.put("iconSkin", "peopleSkin");
            interlocutorInfo.put("type", "people");
        } else if ("2".equals(interlocutorInfoJsonObj.get("monitorType"))) {
            interlocutorInfo.put("iconSkin", "thingSkin");
            interlocutorInfo.put("type", "thing");
        }
        interlocutorInfo.put("id", monitorId);
        interlocutorInfo.put("pId", assignmentId);
        interlocutorInfo.put("name", interlocutorInfoJsonObj.getString("userName"));
        interlocutorInfo.put("assignName", assignmentName);
        interlocutorInfo.put("interlocutorId", interlocutorId);
        interlocutorInfo.put("userNumber", interlocutorInfoJsonObj.getLong("userNumber"));
        interlocutorInfo.put("audioOnlineStatus", interlocutorInfoJsonObj.getInteger("audioOnlineStatus"));
        return interlocutorInfo;
    }

    /**
     * 组装任务组组织信息
     * @param pid 任务组组织所属组织的pid
     * @return 任务组组织
     */
    private OrganizationLdap installTaskOrganization(String pid) {
        OrganizationLdap taskOrganization = new OrganizationLdap();
        taskOrganization.setId(LdapUtils.newLdapName("ou=taskOrganization,ou=Enterprise_top,ou=organization"));
        taskOrganization.setCid("ou=taskOrganization,ou=Enterprise_top,ou=organization");
        taskOrganization.setUuid("taskOrganization");
        taskOrganization.setName("任务组");
        taskOrganization.setPid(pid);
        return taskOrganization;
    }

    /**
     * 组装临时组组织信息
     * @param pid 临时组组织所属组织的id
     * @return 临时组组织
     */
    private OrganizationLdap installTemporaryOrganization(String pid) {
        OrganizationLdap temporaryOrganization = new OrganizationLdap();
        temporaryOrganization
            .setId(LdapUtils.newLdapName("ou=temporaryOrganization,ou=Enterprise_top,ou=organization"));
        temporaryOrganization.setCid("ou=temporaryOrganization,ou=Enterprise_top,ou=organization");
        temporaryOrganization.setUuid("temporaryOrganization");
        temporaryOrganization.setName("临时组");
        temporaryOrganization.setPid(pid);
        return temporaryOrganization;
    }

    /**
     * 组装分组信息到树
     * @param assignmentList          分组信息
     * @param organizeInfoList        组织信息
     * @param treeInfoJsonArr         树
     * @param needShowAssignmentInfo  需要展开的分组信息
     * @param isNeedShowAllAssignment 是否需要展开所有的分组
     */
    private void installAssignmentInfoToTree(List<Cluster> assignmentList, List<OrganizationLdap> organizeInfoList,
        JSONArray treeInfoJsonArr, Cluster needShowAssignmentInfo, boolean isNeedShowAllAssignment) {
        if (CollectionUtils.isEmpty(assignmentList) || CollectionUtils.isEmpty(organizeInfoList)) {
            return;
        }
        String firstAssignmentId = needShowAssignmentInfo == null ? null : needShowAssignmentInfo.getId();
        for (OrganizationLdap organize : organizeInfoList) {
            List<Cluster> filterAssignmentInfoList = assignmentList.stream()
                .filter(assignmentInfo -> Objects.equals(assignmentInfo.getGroupId(), organize.getUuid()))
                .collect(Collectors.toList());
            for (Cluster assignment : filterAssignmentInfoList) {
                JSONObject assignmentJsonObj = new JSONObject();
                String assignmentId = assignment.getId();
                if (isNeedShowAllAssignment) {
                    assignmentJsonObj.put("open", true);
                }
                assignmentJsonObj.put("isParent", true);
                if (firstAssignmentId != null && Objects.equals(firstAssignmentId, assignmentId)) {
                    Integer interlocutorNum = needShowAssignmentInfo.getMNum();
                    assignmentJsonObj.put("mNum", interlocutorNum);
                    assignmentJsonObj.put("open", true);
                    if (interlocutorNum == null || interlocutorNum == 0) {
                        assignmentJsonObj.put("isParent", false);
                    }
                }
                assignmentJsonObj.put("pId", organize.getId().toString());
                assignmentJsonObj.put("pName", organize.getName());
                assignmentJsonObj.put("id", assignmentId);
                assignmentJsonObj.put("name", assignment.getName());
                assignmentJsonObj.put("intercomGroupId", assignment.getIntercomGroupId());
                assignmentJsonObj.put("groupCallNumber", assignment.getGroupCallNumber());
                assignmentJsonObj.put("type", "assignment");
                assignmentJsonObj.put("iconSkin", "assignmentSkin");
                treeInfoJsonArr.add(assignmentJsonObj);
            }
            assignmentList.removeAll(filterAssignmentInfoList);
        }
    }

    /**
     * 设置树结构中临时组和任务组的位置在当前组织下的第一和第二
     * @param treeInfoJsonArr 树结构数据
     */
    private void setUpTaskAndTemporaryLocation(JSONArray treeInfoJsonArr) {
        Optional<Object> taskOptional = treeInfoJsonArr.stream()
            .filter(jsonObj -> Objects.equals(((JSONObject) jsonObj).getString("uuid"), "taskOrganization"))
            .findFirst();
        if (taskOptional.isPresent()) {
            Object taskOrganization = taskOptional.get();
            treeInfoJsonArr.remove(taskOrganization);
            treeInfoJsonArr.add(0, taskOrganization);
        }
        Optional<Object> temporaryOptional = treeInfoJsonArr.stream()
            .filter(jsonObj -> Objects.equals(((JSONObject) jsonObj).getString("uuid"), "temporaryOrganization"))
            .findFirst();
        if (temporaryOptional.isPresent()) {
            Object temporaryOrganization = temporaryOptional.get();
            treeInfoJsonArr.remove(temporaryOrganization);
            treeInfoJsonArr.add(0, temporaryOrganization);
        }
    }

    @Override
    public JsonResultBean fuzzySearchInterlocutor(String queryParam, String queryType) throws Exception {
        JSONArray treeInfoJsonArr = new JSONArray();
        boolean isNeedShowAllAssignment = false;
        UserLdap currentUser = SystemHelper.getCurrentUser();
        String userId = currentUser.getId().toString();
        String orgId = userId.substring(userId.indexOf(",") + 1);
        String uuid = userService.getUserUuidById(userId);
        Cluster needShowAssignment = null;
        // 用户当前组织和所属下级组织信息
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo = userService.getUserOwnAuthorityOrganizeInfo(orgId);
        // 用户当前组织和所属下级组织id
        List<String> userOwnAuthorityOrganizeId =
            userOwnAuthorityOrganizeInfo.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        if (Objects.equals(currentUser.getUsername(), "admin")) {
            currentOrganization = updateOrganizationStructure(currentOrganization, userOwnAuthorityOrganizeInfo);
        }
        // 查询当前用户权限固定分组
        List<Cluster> assignmentList = clusterService.findUserAssignment(uuid, userOwnAuthorityOrganizeId);
        if (currentOrganization != null) {
            String currentGroupId = currentOrganization.getUuid();
            // 查询任务组信息
            List<Cluster> userOwnTaskAssignment = clusterDao.findUserOwnTaskAssignment(uuid, currentGroupId);
            for (Cluster assignment : userOwnTaskAssignment) {
                assignment.setGroupId("taskOrganization");
                assignment.setFlag((short) 2);
            }
            assignmentList.addAll(userOwnTaskAssignment);
            // 添加任务组组织
            userOwnAuthorityOrganizeInfo.add(1, installTaskOrganization(currentOrganization.getId().toString()));
            // 查询临时组信息
            List<Cluster> userOwnTemporaryAssignment = clusterDao.findUserOwnTemporaryAssignment(uuid, currentGroupId);
            for (Cluster assignment : userOwnTemporaryAssignment) {
                assignment.setGroupId("temporaryOrganization");
                assignment.setFlag((short) 3);
            }
            assignmentList.addAll(userOwnTemporaryAssignment);
            // 临时组组织
            userOwnAuthorityOrganizeInfo.add(1, installTemporaryOrganization(currentOrganization.getId().toString()));
            if (Objects.equals(queryType, "name")) {
                isNeedShowAllAssignment = true;
                if (CollectionUtils.isNotEmpty(assignmentList)) {
                    List<String> assignmentIdList =
                        assignmentList.stream().map(Cluster::getId).collect(Collectors.toList());
                    // 通过对讲对象id查询监控对象所在对讲组,组装对讲对象分组信息,过滤多余的分组
                    List<InterlocutorInfo> interlocutorInfoList = StringUtils.isNotBlank(queryParam)
                        ? clusterDao.findInterlocutorByAssignmentIdsAndNameFuzzy(assignmentIdList, queryParam) :
                        new ArrayList<>();
                    // 在临时组内的对讲对象
                    List<TempAssignmentInterlocutor> inTemporaryAssignmentInterlocutorInfoList = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(interlocutorInfoList)) {
                        inTemporaryAssignmentInterlocutorInfoList = clusterDao
                            .findInTemporaryAssignmentInterlocutorInfo(
                                interlocutorInfoList.stream().map(InterlocutorInfo::getInterlocutorId)
                                    .collect(Collectors.toList()));
                    }
                    JSONArray queryUserResultJsonArr = new JSONArray();
                    JsonResultBean getUserInfoResult = getUserInfo(interlocutorInfoList, queryUserResultJsonArr, 0);
                    if (!getUserInfoResult.isSuccess()) {
                        return getUserInfoResult;
                    }
                    List<String> filterAssignmentIdList = new ArrayList<>();
                    for (int i = 0, len = queryUserResultJsonArr.size(); i < len; i++) {
                        JSONObject interlocutorInfoJsonObj = queryUserResultJsonArr.getJSONObject(i);
                        // 对讲对象id
                        Long interlocutorId = interlocutorInfoJsonObj.getLong("userId");
                        //对讲对象当前所在群组id
                        Long defaultGroupId = interlocutorInfoJsonObj.getLong("defaultGroupId");
                        // 判断是否在临时组内 如果在当前所在群组id为临时组id
                        Optional<TempAssignmentInterlocutor> interlocutorOptional =
                            inTemporaryAssignmentInterlocutorInfoList.stream()
                                .filter(info -> Objects.equals(info.getInterlocutorId(), interlocutorId)).findFirst();
                        if (interlocutorOptional.isPresent()) {
                            defaultGroupId = interlocutorOptional.get().getIntercomGroupId();
                        }
                        Long finalDefaultGroupId = defaultGroupId;
                        Optional<Cluster> assignmentOptional = assignmentList.stream()
                            .filter(assignment -> Objects.equals(assignment.getIntercomGroupId(), finalDefaultGroupId))
                            .findFirst();
                        if (!assignmentOptional.isPresent()) {
                            continue;
                        }
                        Optional<InterlocutorInfo> optional = interlocutorInfoList.stream()
                            .filter(info -> Objects.equals(info.getInterlocutorId(), interlocutorId)).findFirst();
                        if (!optional.isPresent()) {
                            continue;
                        }
                        Cluster assignment = assignmentOptional.get();
                        String assignmentId = assignment.getId();
                        InterlocutorInfo info = optional.get();
                        interlocutorInfoList.remove(info);
                        interlocutorInfoJsonObj.put("monitorType", info.getMonitorType());
                        // 组装对讲对象树信息
                        JSONObject interlocutorInfo =
                            installInterlocutorTreeInfo(interlocutorInfoJsonObj, interlocutorId, assignmentId,
                                assignment.getName(), info.getMonitorId());
                        treeInfoJsonArr.add(interlocutorInfo);
                        filterAssignmentIdList.add(assignmentId);
                    }
                    // 过滤多余的分组
                    assignmentList = assignmentList.stream()
                        .filter(assignment -> filterAssignmentIdList.contains(assignment.getId()))
                        .collect(Collectors.toList());
                }
            } else if (Objects.equals(queryType, "assignment")) {
                assignmentList = StringUtils.isNotBlank(queryParam)
                    ? assignmentList.stream().filter(assignment -> assignment.getName().contains(queryParam))
                        .collect(Collectors.toList()) : new ArrayList<>();
                if (CollectionUtils.isNotEmpty(assignmentList)) {
                    // 获得第一个需要展开分组
                    needShowAssignment = getFirstNeedShowAssignment(userOwnAuthorityOrganizeInfo, assignmentList);
                    if (needShowAssignment != null) {
                        // 组装第一个对讲组内的对讲对象
                        JsonResultBean installAssignmentMonitorInfoToTreeResult =
                            installAssignmentMonitorInfoToTree(needShowAssignment, treeInfoJsonArr, 0, null);
                        if (!installAssignmentMonitorInfoToTreeResult.isSuccess()) {
                            return installAssignmentMonitorInfoToTreeResult;
                        }
                    }
                }
            }
        }
        userOwnAuthorityOrganizeInfo =
            new ArrayList<>(OrganizationUtil.filterOrgListNew(userOwnAuthorityOrganizeInfo, assignmentList));
        installAssignmentInfoToTree(assignmentList, userOwnAuthorityOrganizeInfo, treeInfoJsonArr, needShowAssignment,
            isNeedShowAllAssignment);
        treeInfoJsonArr.addAll(JsonUtil.getGroupTree(userOwnAuthorityOrganizeInfo, null, true));
        // 设置树结构中临时组和任务组的位置在当前组织下的第一和第二
        setUpTaskAndTemporaryLocation(treeInfoJsonArr);
        Object treeInfo = ZipUtil.compress(treeInfoJsonArr.toJSONString());
        return new JsonResultBean(treeInfo);
    }

    /**
     * 获得用户信息
     * @param interlocutorInfoList   对讲对象
     * @param queryUserResultJsonArr 返回的用户信息
     * @param status                 0 和 null:全部; 1:在线; 2:离线;
     * @return JsonResultBean
     */
    private JsonResultBean getUserInfo(List<InterlocutorInfo> interlocutorInfoList, JSONArray queryUserResultJsonArr,
        Integer status) {
        JSONArray userList = new JSONArray();
        for (InterlocutorInfo info : interlocutorInfoList) {
            JSONObject userJsonObj = new JSONObject();
            userJsonObj.put("userId", info.getInterlocutorId());
            userList.add(userJsonObj);
        }
        JSONObject dispatchUserLoginInfo =
            WebSubscribeManager.getInstance().getDispatchUserLoginInfo(SystemHelper.getCurrentUsername());
        if (dispatchUserLoginInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "调度服务未登录");
        }
        JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
        if (userList.size() > 0) {
            // 查询用户
            JsonResultBean queryUserListResult =
                queryUserList(data.getLong("custId"), userList, 1, 1000, queryUserResultJsonArr, status, null);
            if (!queryUserListResult.isSuccess()) {
                return queryUserListResult;
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 获得第一个需要展开的分组
     * @param userOwnAuthorityOrganizeInfo 用户当前组织和下级组织
     * @param assignmentList               用户权限内的分组
     * @return Assignment
     */
    private Cluster getFirstNeedShowAssignment(List<OrganizationLdap> userOwnAuthorityOrganizeInfo,
        List<Cluster> assignmentList) {
        // 先判断是否有临时组 有临时组展开第一个
        List<Cluster> temporaryAssignmentList =
            assignmentList.stream().filter(assignment -> Objects.equals(Integer.valueOf(assignment.getFlag()), 3))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(temporaryAssignmentList)) {
            return temporaryAssignmentList.get(0);
        }
        // 如果没有临时组 判断是否有任务组 有展开第一个
        List<Cluster> taskAssignmentList =
            assignmentList.stream().filter(assignment -> Objects.equals(Integer.valueOf(assignment.getFlag()), 2))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(taskAssignmentList)) {
            return taskAssignmentList.get(0);
        }
        // 如果没有任务组 展开第一个固定组
        for (OrganizationLdap organization : userOwnAuthorityOrganizeInfo) {
            List<Cluster> filterAssignmentInfoList = assignmentList.stream()
                .filter(assignmentInfo -> Objects.equals(assignmentInfo.getGroupId(), organization.getUuid()))
                .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(filterAssignmentInfoList)) {
                return filterAssignmentInfoList.get(0);
            }
        }
        return null;
    }

    /**
     * 查询用户
     * @param custId                 指定客户ID 若不指定填写-1，查询结果为本次登陆的客户账号下用户
     * @param userList               用户信息列表
     * @param pageIndex              当前页码
     * @param pageSize               每页记录条数
     * @param queryUserResultJsonArr 查询用户的结果
     * @param status                 0 和 null:全部; 1:在线; 2:离线;
     * @param fixedQuantity          固定数量 为null 查询全部
     * @return JsonResultBean
     */
    private JsonResultBean queryUserList(Long custId, JSONArray userList, Integer pageIndex, Integer pageSize,
        JSONArray queryUserResultJsonArr, Integer status, Integer fixedQuantity) {
        JSONObject queryUserListResultJsonObj = talkCallUtil.queryUserList(custId, userList, pageIndex, pageSize);
        if (queryUserListResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "查询用户异常，请联系管理员");
        }
        Integer queryUserListListStatusCode = queryUserListResultJsonObj.getInteger("result");
        if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), queryUserListListStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT,
                DispatchErrorMessageEnum.getMessage(queryUserListListStatusCode));
        }
        JSONArray records = queryUserListResultJsonObj.getJSONObject("data").getJSONArray("records");
        if (records != null && records.size() > 0) {
            if (status == null || Objects.equals(status, 0)) {
                queryUserResultJsonArr.addAll(records);
            } else {
                for (int i = 0, len = records.size(); i < len; i++) {
                    JSONObject interlocutorInfoJsonObj = records.getJSONObject(i);
                    // 判断对讲在线状态是否相同
                    boolean statusIsEqual = judgeInterlocutorStatusIsEqual(status, interlocutorInfoJsonObj);
                    if (!statusIsEqual) {
                        continue;
                    }
                    queryUserResultJsonArr.add(interlocutorInfoJsonObj);
                }
            }
        }
        Integer totalPages = queryUserListResultJsonObj.getJSONObject("pageInfo").getInteger("totalPages");
        if (pageIndex < totalPages && (fixedQuantity == null || queryUserResultJsonArr.size() < fixedQuantity)) {
            pageIndex++;
            return queryUserList(custId, userList, pageIndex, pageSize, queryUserResultJsonArr, status, fixedQuantity);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 判断对讲在线状态是否相同
     * @param interlocutorStatus      0:全部; 1:在线; 2:离线;
     * @param interlocutorInfoJsonObj 查询出的结果
     * @return boolean
     */
    private boolean judgeInterlocutorStatusIsEqual(Integer interlocutorStatus, JSONObject interlocutorInfoJsonObj) {
        // 对讲在线状态 1:在线 0:不在线
        Integer audioOnlineStatus = interlocutorInfoJsonObj.getInteger("audioOnlineStatus");
        audioOnlineStatus = !Objects.equals(audioOnlineStatus, 0) ? audioOnlineStatus : 2;
        return Objects.equals(interlocutorStatus, audioOnlineStatus);
    }

    @Override
    public JsonResultBean getInterlocutorAssignmentMember(Long intercomGroupId, Integer interlocutorStatus)
        throws Exception {
        // 对讲组内用户
        JSONArray interlocutorAssignmentMemberArr = new JSONArray();
        JSONArray interlocutors = new JSONArray();
        // 分组信息
        Cluster assignmentInfo = clusterDao.findAssignmentByIntercomGroupId(intercomGroupId);
        if (assignmentInfo != null) {
            // 获取对讲组内用户
            JsonResultBean getGroupMemberListResult =
                getGroupMemberList(intercomGroupId, interlocutorAssignmentMemberArr, interlocutorStatus);
            if (!getGroupMemberListResult.isSuccess()) {
                return getGroupMemberListResult;
            }
            List<Long> interlocutorIdList =
                interlocutorAssignmentMemberArr.stream().map(info -> ((JSONObject) info).getLong("userId"))
                    .collect(Collectors.toList());
            if (interlocutorIdList.isEmpty()) {
                return new JsonResultBean(interlocutors);
            }
            List<InterlocutorInfo> interlocutorInfoList =
                intercomPersonnelDao.getInterlocutorInfoByInterlocutorIdList(interlocutorIdList);

            // 如果查询的不是临时组 过滤掉临时组内的对讲对象
            if (!Objects.equals(Integer.valueOf(assignmentInfo.getFlag()), 3)
                && interlocutorAssignmentMemberArr.size() > 0) {
                List<Long> inTemporaryAssignmentInterlocutorIdList =
                    clusterDao.findInTemporaryAssignmentInterlocutorId(interlocutorIdList);
                if (CollectionUtils.isNotEmpty(inTemporaryAssignmentInterlocutorIdList)) {
                    interlocutorAssignmentMemberArr.removeIf(info -> inTemporaryAssignmentInterlocutorIdList
                        .contains(((JSONObject) info).getLong("userId")));
                }
            }
            for (int i = 0, len = interlocutorAssignmentMemberArr.size(); i < len; i++) {
                JSONObject interlocutorInfoJsonObj = interlocutorAssignmentMemberArr.getJSONObject(i);
                Long interlocutorId = interlocutorInfoJsonObj.getLong("userId");
                Optional<InterlocutorInfo> optional = interlocutorInfoList.stream()
                    .filter(info -> Objects.equals(info.getInterlocutorId(), interlocutorId)).findFirst();
                InterlocutorInfo info = optional.get();
                if (info != null) {
                    interlocutorInfoList.remove(info);
                    interlocutorInfoJsonObj.put("iconSkin", geticonSkin(info.getMonitorType()));
                    interlocutorInfoJsonObj.put("type", getType(info.getMonitorType()));
                }
                interlocutors.add(interlocutorInfoJsonObj);
            }
        }
        return new JsonResultBean(interlocutors);
    }

    private String getType(String moitorType) {
        if ("0".equals(moitorType)) {
            return "vehicle";
        } else if ("1".equals(moitorType)) {
            return "people";
        } else if ("2".equals(moitorType)) {
            return "thing";
        }
        return "";
    }

    private String geticonSkin(String moitorType) {
        if ("0".equals(moitorType)) {
            return "vehicleSkin";
        } else if ("1".equals(moitorType)) {
            return "peopleSkin";
        } else if ("2".equals(moitorType)) {
            return "thingSkin";
        }
        return "";
    }

    @Override
    public JsonResultBean getInterlocutorInfoById(Long interlocutorId) throws Exception {
        Long intercomGroupId = null;
        List<TempAssignmentInterlocutor> inTemporaryAssignmentInterlocutorInfo =
            clusterDao.findInTemporaryAssignmentInterlocutorInfo(Collections.singletonList(interlocutorId));
        if (CollectionUtils.isNotEmpty(inTemporaryAssignmentInterlocutorInfo)) {
            intercomGroupId = inTemporaryAssignmentInterlocutorInfo.get(0).getIntercomGroupId();
        }
        JSONArray queryUserResultJsonArr = new JSONArray();
        InterlocutorInfo interlocutorInfo = intercomPersonnelDao.getInterlocutorInfoByInterlocutorId(interlocutorId);
        if (interlocutorInfo == null) {
            return new JsonResultBean(JsonResultBean.SUCCESS, "不是对讲对象");
        }
        interlocutorInfo.setInterlocutorId(interlocutorId);
        JsonResultBean getUserInfoResult =
            getUserInfo(Collections.singletonList(interlocutorInfo), queryUserResultJsonArr, 0);
        if (!getUserInfoResult.isSuccess()) {
            return getUserInfoResult;
        }
        Optional<Object> optional = queryUserResultJsonArr.stream().filter(
            info -> Objects.equals(((JSONObject) info).getInteger("type"), 0) && Objects
                .equals(((JSONObject) info).getLong("userId"), interlocutorId)).findFirst();
        if (!optional.isPresent()) {
            return new JsonResultBean(JsonResultBean.SUCCESS, "不是对讲对象");
        }
        JSONObject jsonObj = JSON.parseObject(JSONObject.toJSONString(optional.get()));
        jsonObj.put("type", getType(interlocutorInfo.getMonitorType()));
        jsonObj.put("iconSkin", geticonSkin(interlocutorInfo.getMonitorType()));
        if (intercomGroupId == null) {
            return new JsonResultBean(jsonObj);
        }

        jsonObj.put("defaultGroupId", intercomGroupId);
        return new JsonResultBean(jsonObj);
    }

    @Override
    public JsonResultBean findInterlocutorByCircleArea(String assignmentId, String assignmentType, Double longitude,
        Double latitude, Double radius) throws Exception {
        // 获得权限下固定分组内的对讲对象信息
        List<Cluster> assignmentList = getUserOwnFixedAssignmentInfoList();
        List<InterlocutorInfo> interlocutorInfoList = getInterlocutorInfoByFixedAssignment(assignmentList);
        // 过滤在临时组内对讲对象
        filterInTempAssignmentInterlocutor(interlocutorInfoList);
        List<Long> intercomGroupIdIdList =
            assignmentList.stream().map(Cluster::getIntercomGroupId).collect(Collectors.toList());
        return getInAreaAndOnlineInterlocutorInfoList(intercomGroupIdIdList, interlocutorInfoList, "circle",
            assignmentId, assignmentType, longitude, latitude, radius, null, null, null, null);
    }

    /**
     * 获得用户权限下的固定分组信息
     * @return List<Assignment>
     * @throws Exception Exception
     */
    private List<Cluster> getUserOwnFixedAssignmentInfoList() throws Exception {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 用户当前组织和所属下级组织信息
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo =
            userService.getUserOwnAuthorityOrganizeInfo(userId.substring(userId.indexOf(",") + 1));
        // 用户当前组织和所属下级组织id
        List<String> userOwnAuthorityOrganizeId =
            userOwnAuthorityOrganizeInfo.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        // 查询当前用户权限固定分组
        return clusterService.findUserAssignment(uuid, userOwnAuthorityOrganizeId);
    }

    /**
     * 获得权限下固定分组内的对讲对象信息
     * @param assignmentList 权限下分组
     * @return List<InterlocutorInfo>
     * @throws Exception Exception
     */
    private List<InterlocutorInfo> getInterlocutorInfoByFixedAssignment(List<Cluster> assignmentList) throws Exception {
        List<String> assignmentIdList = assignmentList.stream().map(Cluster::getId).collect(Collectors.toList());
        // 查询分组下的对讲对象
        return CollectionUtils.isNotEmpty(assignmentIdList)
            ? clusterDao.findInterlocutorByAssignmentIdsAndNameFuzzy(assignmentIdList, null) : new ArrayList<>();
    }

    /**
     * 过滤在临时组内对讲对象
     * @param interlocutorInfoList 需要过滤的对讲对象信息
     */
    private void filterInTempAssignmentInterlocutor(List<InterlocutorInfo> interlocutorInfoList) {
        if (CollectionUtils.isNotEmpty(interlocutorInfoList)) {
            List<Long> inTemporaryAssignmentInterlocutorIdList = clusterDao.findInTemporaryAssignmentInterlocutorId(
                interlocutorInfoList.stream().map(InterlocutorInfo::getInterlocutorId).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(inTemporaryAssignmentInterlocutorIdList)) {
                interlocutorInfoList
                    .removeIf(info -> inTemporaryAssignmentInterlocutorIdList.contains(info.getInterlocutorId()));
            }
        }
    }

    /**
     * 获得在区域内的在线的对讲对象
     * @param intercomGroupIdIdList 权限下固定组
     * @param interlocutorInfoList  需要筛选的对讲对象
     * @param areaType              区域类型  circle:圆; rectangle:矩形;
     * @param assignmentId          如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType        分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @param longitude             圆经度
     * @param latitude              圆纬度
     * @param radius                圆半径
     * @param leftLongitude         矩形区域左上角的经度
     * @param leftLatitude          矩形区域左上角的纬度
     * @param rightLongitude        矩形区域右下角的经度
     * @param rightLatitude         矩形区域右下角的纬度
     * @return JsonResultBean
     * @throws Exception Exception
     */
    private JsonResultBean getInAreaAndOnlineInterlocutorInfoList(List<Long> intercomGroupIdIdList,
        List<InterlocutorInfo> interlocutorInfoList, String areaType, String assignmentId, String assignmentType,
        Double longitude, Double latitude, Double radius, Double leftLongitude, Double leftLatitude,
        Double rightLongitude, Double rightLatitude) throws Exception {
        JSONArray resultJsonArr = new JSONArray();
        // 需要返回的对讲对象数量
        Integer needReturnInterlocutorNum = ASSIGNMENT_MXA_MONITOR_NUM;
        // 如果是加入群组 要排除已在群组中的对象
        if (StringUtils.isNotBlank(assignmentId)) {
            List<InterlocutorInfo> needRemoveInterlocutorInfoList = new ArrayList<>();
            if (Objects.equals(assignmentType, "2")) {
                List<String> needRemoveMonitorIdList = clusterDao.findMonitorIdsByAssignmentId(assignmentId);
                needRemoveInterlocutorInfoList =
                    interlocutorInfoList.stream().filter(info -> needRemoveMonitorIdList.contains(info.getMonitorId()))
                        .collect(Collectors.toList());
                needReturnInterlocutorNum = needReturnInterlocutorNum - needRemoveMonitorIdList.size();
            }
            if (Objects.equals(assignmentType, "3")) {
                JSONArray temporaryAssignmentInterlocutorJsonArr = new JSONArray();
                JsonResultBean getTemporaryAssignmentInterlocutorInfoResult =
                    getTemporaryAssignmentInterlocutorInfo(assignmentId, temporaryAssignmentInterlocutorJsonArr);
                if (!getTemporaryAssignmentInterlocutorInfoResult.isSuccess()) {
                    return getTemporaryAssignmentInterlocutorInfoResult;
                }
                if (temporaryAssignmentInterlocutorJsonArr.size() > 0) {
                    List<Long> needRemoveInterlocutorIdList = temporaryAssignmentInterlocutorJsonArr.stream()
                        .map(info -> ((JSONObject) info).getLong("userId")).collect(Collectors.toList());
                    needRemoveInterlocutorInfoList = interlocutorInfoList.stream()
                        .filter(info -> needRemoveInterlocutorIdList.contains(info.getInterlocutorId()))
                        .collect(Collectors.toList());
                    needReturnInterlocutorNum = needReturnInterlocutorNum - needRemoveInterlocutorIdList.size();
                }
            }
            interlocutorInfoList.removeAll(needRemoveInterlocutorInfoList);
        }
        List<String> monitorIdList =
            interlocutorInfoList.stream().map(InterlocutorInfo::getMonitorId).collect(Collectors.toList());
        final RedisKey key = HistoryRedisKeyEnum.MASSIVE_LOCATION.of();
        List<String> interlocutorLocationJsonStrList = RedisHelper.hmget(key, monitorIdList);
        List<String> inAreaMonitorIdList = null;
        if (Objects.equals(areaType, "circle")) {
            inAreaMonitorIdList = interlocutorLocationJsonStrList.stream().map(JSON::parseObject).filter(
                jsonObj -> judgeIsInCircle(jsonObj.getDouble("longitude"), jsonObj.getDouble("latitude"), longitude,
                    latitude, radius)).map(jsonObj -> jsonObj.getString("monitorId")).collect(Collectors.toList());
        } else if (Objects.equals(areaType, "rectangle")) {
            inAreaMonitorIdList = interlocutorLocationJsonStrList.stream().map(JSON::parseObject).filter(
                jsonObj -> judgeIsInRectangle(jsonObj.getDouble("longitude"), jsonObj.getDouble("latitude"),
                    leftLongitude, leftLatitude, rightLongitude, rightLatitude))
                .map(jsonObj -> jsonObj.getString("monitorId")).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(inAreaMonitorIdList)) {
            List<String> finalInAreaMonitorIdList = inAreaMonitorIdList;
            List<InterlocutorInfo> inAreaInterlocutorInfoList =
                interlocutorInfoList.stream().filter(info -> finalInAreaMonitorIdList.contains(info.getMonitorId()))
                    .collect(Collectors.toList());
            // 筛选出在线的
            JSONArray queryUserResultJsonArr = new JSONArray();
            JsonResultBean getUserInfoResult = getUserInfo(inAreaInterlocutorInfoList, queryUserResultJsonArr, 1);
            if (!getUserInfoResult.isSuccess()) {
                return getUserInfoResult;
            }
            // 移除不在权限下固定组中对讲对象
            queryUserResultJsonArr
                .removeIf(info -> !intercomGroupIdIdList.contains(((JSONObject) info).getLong("defaultGroupId")));
            int len = queryUserResultJsonArr.size();
            len = len < needReturnInterlocutorNum ? len : needReturnInterlocutorNum;
            for (int i = 0; i < len; i++) {
                JSONObject userInfoJsonObj = queryUserResultJsonArr.getJSONObject(i);
                Long interlocutorId = userInfoJsonObj.getLong("userId");
                inAreaInterlocutorInfoList.stream()
                    .filter(info -> Objects.equals(info.getInterlocutorId(), interlocutorId)).findFirst()
                    .ifPresent(interlocutorInfo -> {
                        JSONObject interlocutorInfoJsonObj = new JSONObject();
                        interlocutorInfoJsonObj.put("interlocutorId", interlocutorInfo.getInterlocutorId());
                        interlocutorInfoJsonObj.put("monitorId", interlocutorInfo.getMonitorId());
                        interlocutorInfoJsonObj.put("monitorName", interlocutorInfo.getMonitorName());
                        resultJsonArr.add(interlocutorInfoJsonObj);
                    });
            }
        }
        return new JsonResultBean(resultJsonArr);
    }

    /**
     * 判断对讲对象是否在圆内
     * @param longitude             对讲对象经度
     * @param latitude              对讲对象纬度
     * @param circleCenterLongitude 圆心经度
     * @param circleCenterLatitude  圆心纬度
     * @param radius                半径
     * @return boolean
     */
    private boolean judgeIsInCircle(Double longitude, Double latitude, Double circleCenterLongitude,
        Double circleCenterLatitude, Double radius) {
        //两个经纬度之间的距离
        return AddressUtil.getDistance(circleCenterLongitude, circleCenterLatitude, longitude, latitude) <= radius;
    }

    /**
     * 判断对讲对象是否在矩形内
     * @param longitude      对讲对象经度
     * @param latitude       对讲对象纬度
     * @param leftLongitude  矩形区域左上角的经度
     * @param leftLatitude   矩形区域左上角的纬度
     * @param rightLongitude 矩形区域右下角的经度
     * @param rightLatitude  矩形区域右下角的纬度
     * @return boolean
     */
    private boolean judgeIsInRectangle(Double longitude, Double latitude, Double leftLongitude, Double leftLatitude,
        Double rightLongitude, Double rightLatitude) {
        return leftLongitude <= longitude && rightLongitude >= longitude && leftLatitude >= latitude
            && rightLatitude <= latitude;
    }

    /**
     * 获得临时组内对讲对象数量
     * @param assignmentId               分组id
     * @param temporaryAssignmentJsonArr 临时组内对讲对象信息
     * @return JsonResultBean
     */
    private JsonResultBean getTemporaryAssignmentInterlocutorInfo(String assignmentId,
        JSONArray temporaryAssignmentJsonArr) {
        // 获取对讲组内用户
        JsonResultBean getGroupMemberListResult =
            getGroupMemberList(Long.valueOf(assignmentId), temporaryAssignmentJsonArr, 0);
        if (!getGroupMemberListResult.isSuccess()) {
            return getGroupMemberListResult;
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 查找对讲对象,通过画的矩形区域
     * @param assignmentId   如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @param leftLongitude  矩形区域左上角的经度
     * @param leftLatitude   矩形区域左上角的纬度
     * @param rightLongitude 矩形区域右下角的经度
     * @param rightLatitude  矩形区域右下角的纬度
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean findInterlocutorByRectangleArea(String assignmentId, String assignmentType,
        Double leftLongitude, Double leftLatitude, Double rightLongitude, Double rightLatitude) throws Exception {
        // 获得权限下固定分组内的对讲对象信息
        List<Cluster> assignmentList = getUserOwnFixedAssignmentInfoList();
        List<InterlocutorInfo> interlocutorInfoList = getInterlocutorInfoByFixedAssignment(assignmentList);
        // 过滤在临时组内对讲对象
        filterInTempAssignmentInterlocutor(interlocutorInfoList);
        List<Long> intercomGroupIdIdList =
            assignmentList.stream().map(Cluster::getIntercomGroupId).collect(Collectors.toList());
        return getInAreaAndOnlineInterlocutorInfoList(intercomGroupIdIdList, interlocutorInfoList, "rectangle",
            assignmentId, assignmentType, null, null, null, leftLongitude, leftLatitude, rightLongitude, rightLatitude);
    }

    /**
     * 查找对讲对象,通过固定对象
     * @param assignmentId   如果是加入分组, 该字段就是加入的分组id, 如果是创建该字段为null
     * @param assignmentType 分组类型 2：任务组; 3:临时组, 如果是创建该字段为null
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean findInterlocutorByFixedInterlocutor(String assignmentId, String assignmentType)
        throws Exception {
        JSONObject result = new JSONObject();
        JSONArray treeInfoJsonArr = new JSONArray();
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 用户当前组织和所属下级组织信息
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo =
            userService.getUserOwnAuthorityOrganizeInfo(userId.substring(userId.indexOf(",") + 1));
        // 用户当前组织和所属下级组织id
        List<String> userOwnAuthorityOrganizeId =
            userOwnAuthorityOrganizeInfo.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        List<Cluster> assignmentList = clusterService.findUserAssignment(uuid, userOwnAuthorityOrganizeId);

        // 如果是加入群组 要过滤已在群组中的对象
        List<Long> needFilterInterlocutorIdList = new ArrayList<>();
        int assignmentAlreadyMonitorNum = 0;
        if (StringUtils.isNotBlank(assignmentId)) {
            if (Objects.equals(assignmentType, "2")) {
                List<String> assignmentMonitorIdList = clusterDao.findMonitorIdsByAssignmentId(assignmentId);
                assignmentAlreadyMonitorNum = assignmentMonitorIdList.size();
                List<InterlocutorInfo> needFilterInterlocutorInfoList = clusterDao
                    .findInterlocutorByAssignmentIdsAndNameFuzzy(Collections.singletonList(assignmentId), null);
                needFilterInterlocutorIdList =
                    needFilterInterlocutorInfoList.stream().map(InterlocutorInfo::getInterlocutorId)
                        .collect(Collectors.toList());
            }
            if (Objects.equals(assignmentType, "3")) {
                JSONArray temporaryAssignmentInterlocutorJsonArr = new JSONArray();
                JsonResultBean getTemporaryAssignmentInterlocutorInfoResult =
                    getTemporaryAssignmentInterlocutorInfo(assignmentId, temporaryAssignmentInterlocutorJsonArr);
                if (!getTemporaryAssignmentInterlocutorInfoResult.isSuccess()) {
                    return getTemporaryAssignmentInterlocutorInfoResult;
                }
                assignmentAlreadyMonitorNum = temporaryAssignmentInterlocutorJsonArr.size();
                if (assignmentAlreadyMonitorNum > 0) {
                    needFilterInterlocutorIdList = temporaryAssignmentInterlocutorJsonArr.stream()
                        .map(info -> ((JSONObject) info).getLong("userId")).collect(Collectors.toList());
                }
            }
        }
        Cluster needShowAssignment = getFirstNeedShowAssignment(userOwnAuthorityOrganizeInfo, assignmentList);
        if (needShowAssignment != null) {
            // 组装第一个对讲组内的对讲对象
            JsonResultBean installAssignmentMonitorInfoToTreeResult =
                installAssignmentMonitorInfoToTree(needShowAssignment, treeInfoJsonArr, 1,
                    needFilterInterlocutorIdList);
            if (!installAssignmentMonitorInfoToTreeResult.isSuccess()) {
                return installAssignmentMonitorInfoToTreeResult;
            }
        }
        userOwnAuthorityOrganizeInfo =
            new ArrayList<>(OrganizationUtil.filterOrgListNew(userOwnAuthorityOrganizeInfo, assignmentList));
        installAssignmentInfoToTree(assignmentList, userOwnAuthorityOrganizeInfo, treeInfoJsonArr, needShowAssignment,
            false);
        treeInfoJsonArr.addAll(JsonUtil.getGroupTree(userOwnAuthorityOrganizeInfo, null, true));
        Object treeInfo = ZipUtil.compress(treeInfoJsonArr.toJSONString());
        result.put("treeInfo", treeInfo);
        result.put("needFilterInterlocutorId", needFilterInterlocutorIdList);
        result.put("assignmentAlreadyMonitorNum", assignmentAlreadyMonitorNum);
        return new JsonResultBean(result);
    }

    /**
     * 获得技能列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllSkillList() throws Exception {
        return new JsonResultBean(skillDao.getAllSkillList());
    }

    /**
     * 获得对讲机型列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllIntercomModeList() throws Exception {
        return new JsonResultBean(intercomModelDao.getAllIntercomModeList());
    }

    /**
     * 获得驾照类别列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllDriverLicenseCategoryList() throws Exception {
        return new JsonResultBean(peopleBasicInfoDao.getAllDriverType());
    }

    /**
     * 获得资格证列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllQualificationList() throws Exception {
        return new JsonResultBean(peopleBasicInfoDao.getAllQualification());
    }

    /**
     * 获得血型列表
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllBloodTypeList() throws Exception {
        return new JsonResultBean(peopleBasicInfoDao.getAllBloodType());
    }

    @Override
    public JsonResultBean findInterlocutorByFixedCondition(String assignmentId, String assignmentType, String skillIds,
        String intercomModelIds, String driverLicenseCategoryIds, String qualificationIds, String gender,
        String bloodTypeIds, String ageRange, Double longitude, Double latitude, Double radius) throws Exception {
        List<Cluster> assignmentList = getUserOwnFixedAssignmentInfoList();
        List<Long> intercomGroupIdIdList =
            assignmentList.stream().map(Cluster::getIntercomGroupId).collect(Collectors.toList());
        // 分组id集合
        List<String> assignmentIdList = assignmentList.stream().map(Cluster::getId).collect(Collectors.toList());
        // 技能id集合
        List<String> skillIdList = StringUtils.isNotBlank(skillIds) ? Arrays.asList(skillIds.split(",")) : null;
        // 对讲机型id集合
        List<String> intercomModelIdList =
            StringUtils.isNotBlank(intercomModelIds) ? Arrays.asList(intercomModelIds.split(",")) : null;
        // 驾照类别id集合
        List<String> driverLicenseCategoryIdList =
            StringUtils.isNotBlank(driverLicenseCategoryIds) ? Arrays.asList(driverLicenseCategoryIds.split(",")) :
                null;
        // 资格证id集合
        List<String> qualificationIdList =
            StringUtils.isNotBlank(qualificationIds) ? Arrays.asList(qualificationIds.split(",")) : null;
        // 血型id集合
        List<String> bloodTypeIdList =
            StringUtils.isNotBlank(bloodTypeIds) ? Arrays.asList(bloodTypeIds.split(",")) : null;
        // 年龄
        List<String> ageRangeList =
            StringUtils.isNotBlank(ageRange) ? Arrays.asList(ageRange.split(",")) : new ArrayList<>();
        List<String> sortAgeRangeList =
            ageRangeList.stream().sorted(Comparator.comparingInt(Integer::valueOf)).collect(Collectors.toList());
        // 固定组中满足条件的对讲对象
        List<InterlocutorInfo> interlocutorInfoList = intercomPersonnelDao
            .findInterlocutorByCondition(null, assignmentIdList, skillIdList, intercomModelIdList,
                driverLicenseCategoryIdList, qualificationIdList, bloodTypeIdList, sortAgeRangeList, gender);
        // 过滤在临时组内对讲对象
        filterInTempAssignmentInterlocutor(interlocutorInfoList);
        return getInAreaAndOnlineInterlocutorInfoList(intercomGroupIdIdList, interlocutorInfoList, "circle",
            assignmentId, assignmentType, longitude, latitude, radius, null, null, null, null);
    }

    /**
     * 判断对讲对象任务组是否超出限制
     * @param interlocutorIds 监控对象id
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean judgeInterlocutorTaskAssignmentNumIsOverLimit(String interlocutorIds) throws Exception {
        JSONObject result = new JSONObject();
        // 超出限制的监控对象名称
        List<String> overLimitMonitorNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(interlocutorIds)) {
            List<String> interlocutorIdList = Arrays.asList(interlocutorIds.split(","));
            // 查询对讲对象任务组信息
            List<ClusterInfo> interlocutorTaskAssignmentInfoList =
                clusterDao.findInterlocutorTaskAssignmentInfo(interlocutorIdList);
            for (String interlocutorId : interlocutorIdList) {
                List<ClusterInfo> filterAssignmentList = interlocutorTaskAssignmentInfoList.stream()
                    .filter(assignment -> Objects.equals(assignment.getInterlocutorId(), Long.valueOf(interlocutorId)))
                    .collect(Collectors.toList());
                if (filterAssignmentList.size() >= 8) {
                    overLimitMonitorNameList.add(filterAssignmentList.get(0).getMonitorName());
                }
                interlocutorTaskAssignmentInfoList.removeAll(filterAssignmentList);
            }
        }
        result.put("isOverLimit", CollectionUtils.isNotEmpty(overLimitMonitorNameList));
        result.put("overLimitMonitorName", StringUtils.join(overLimitMonitorNameList, ","));
        return new JsonResultBean(result);
    }

    @Override
    public JsonResultBean addTaskAssignmentAndMember(String assignmentName, String interlocutorIds, String ipAddress)
        throws Exception {
        String groupCallNumber;
        try {
            groupCallNumber = intercomCallNumberService.updateAndReturnGroupCallNumber();
        } catch (CallNumberExhaustException e) {
            return new JsonResultBean(JsonResultBean.FAULT, "组呼号码已用完，请联系管理员");
        }
        // 创建的群组id
        Long intercomGroupId = null;
        String assignmentId;
        JSONObject assignmentInfoJsonObj = new JSONObject();
        try {
            // 调用接口 添加群组
            JSONObject addGroupResultJsonObj = talkCallUtil.addGroup("0", assignmentName, groupCallNumber, null);
            if (addGroupResultJsonObj == null) {
                // 释放组呼号码
                intercomCallNumberService.updateAndRecycleGroupCallNumber(groupCallNumber);
                return new JsonResultBean(JsonResultBean.FAULT, "添加群组异常，请联系管理员");
            }
            Integer addGroupStatusCode = addGroupResultJsonObj.getInteger("result");
            JSONObject addGroupData = addGroupResultJsonObj.getJSONObject("data");
            if (!Objects.equals(ErrorMessageEnum.CODE_0.getCode(), addGroupStatusCode)) {
                // 释放组呼号码
                intercomCallNumberService.updateAndRecycleGroupCallNumber(groupCallNumber);
                return new JsonResultBean(JsonResultBean.FAULT, ErrorMessageEnum.getMessage(addGroupStatusCode));
            }
            // 创建的群组id
            intercomGroupId = addGroupData != null ? addGroupData.getLong("groupId") : null;
            // 添加分组和权限
            assignmentId = addAssignmentInfoAndPermission(assignmentName, groupCallNumber, intercomGroupId, 2);
            // 分组添加监控对象
            JsonResultBean assignmentInsertMonitorResult =
                assignmentInsertMonitor(interlocutorIds, intercomGroupId, assignmentId);
            if (assignmentInsertMonitorResult.isSuccess()) {
                logSearchService.addLog(ipAddress, "创建“" + assignmentName + "”任务组", "3", "", "-", "");
            }
            assignmentInfoJsonObj.put("assignmentId", assignmentId);
            assignmentInfoJsonObj.put("intercomGroupId", intercomGroupId);
            assignmentInfoJsonObj.put("assignmentName", assignmentName);
            return new JsonResultBean(assignmentInfoJsonObj);
        } catch (Exception e) {
            if (intercomGroupId != null) {
                // 删除群组
                JsonResultBean deleteGroupResultJsonObj = deleteGroup(intercomGroupId);
                if (!deleteGroupResultJsonObj.isSuccess()) {
                    return deleteGroupResultJsonObj;
                }
            }
            // 释放组呼号码
            intercomCallNumberService.updateAndRecycleGroupCallNumber(groupCallNumber);
            throw e;
        }
    }

    /**
     * 分组添加监控对象
     * @param interlocutorIds 组内对讲对象id
     * @param intercomGroupId 分组对应对讲组id
     * @param assignmentId    分组id
     * @return JsonResultBean
     */
    private JsonResultBean assignmentInsertMonitor(String interlocutorIds, Long intercomGroupId, String assignmentId) {
        if (StringUtils.isBlank(interlocutorIds)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        List<String> interlocutorIdStrList = Arrays.asList(interlocutorIds.split(","));
        List<Long> interlocutorIdList = interlocutorIdStrList.stream().map(Long::valueOf).collect(Collectors.toList());
        // 对讲对象信息
        List<InterlocutorInfo> interlocutorInfoList =
            intercomPersonnelDao.getInterlocutorInfoByInterlocutorIdList(interlocutorIdList);
        //组装添加群组成员参数
        JSONObject addGroupMemberListParam =
            installAddGroupMemberListParam(intercomGroupId, interlocutorInfoList, assignmentId);
        // 调用接口 添加群组成员
        JSONObject addGroupMemberListResultJsonObj = talkCallUtil.addGroupMemberList(addGroupMemberListParam);
        if (addGroupMemberListResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "添加群组成员异常，请联系管理员");
        }
        Integer addGroupMemberListStatusCode = addGroupMemberListResultJsonObj.getInteger("result");
        JSONObject data = addGroupMemberListResultJsonObj.getJSONObject("data");
        // 需要更新pid
        if (!Objects.equals(ErrorMessageEnum.CODE_0.getCode(), addGroupMemberListStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT, ErrorMessageEnum.getMessage(addGroupMemberListStatusCode));
        }
        // 过滤掉旋钮编号已经使用完的
        List<InterlocutorInfo> filterList = interlocutorInfoList.stream().filter(
            info -> info.getKnobNum() == null || info.getKnobNum() <= 0 || (info.getKnobNum() != null
                && info.getKnobNo() != null)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(filterList)) {
            // 添加分组内监控对象
            clusterDao.addInterlocutorAssignmentMember(filterList);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 组装添加群组成员参数
     * @param intercomGroupId      当前选择群组ID
     * @param interlocutorInfoList 对讲对象信息
     * @param assignmentId         分组id
     * @return JSONObject
     */
    private JSONObject installAddGroupMemberListParam(Long intercomGroupId, List<InterlocutorInfo> interlocutorInfoList,
        String assignmentId) {
        JSONObject addGroupMemberParam = new JSONObject();
        addGroupMemberParam.put("custId", talkCallUtil.getCustId());
        addGroupMemberParam.put("groupId", intercomGroupId);
        // 无组旋钮用户列表
        JSONArray userList = new JSONArray();
        addGroupMemberParam.put("userList", userList);
        // 无组旋钮用户列表
        JSONArray knobUserList = new JSONArray();
        addGroupMemberParam.put("knobUserList", knobUserList);
        // 无组旋钮用户
        List<InterlocutorInfo> noGroupKnobInterlocutorList = interlocutorInfoList.stream()
            .filter(interlocutorInfo -> interlocutorInfo.getKnobNum() == null || interlocutorInfo.getKnobNum() <= 0)
            .collect(Collectors.toList());
        for (InterlocutorInfo interlocutorInfo : noGroupKnobInterlocutorList) {
            //无组旋钮用户信息
            JSONObject userInfo = new JSONObject();
            userInfo.put("userId", interlocutorInfo.getInterlocutorId());
            userList.add(userInfo);
            interlocutorInfo.setAssignmentId(assignmentId);
            interlocutorInfo.setCreateDataTime(new Date());
            interlocutorInfo.setCreateDataUsername(SystemHelper.getCurrentUsername());
        }
        // 移除无组旋钮用户 剩下的是组旋钮用户
        interlocutorInfoList.removeAll(noGroupKnobInterlocutorList);
        for (InterlocutorInfo interlocutorInfo : interlocutorInfoList) {
            Integer knobNo = getNoUseKnobNo(interlocutorInfo);
            if (knobNo == null) {
                continue;
            }
            interlocutorInfo.setKnobNo(knobNo);
            interlocutorInfo.setAssignmentId(assignmentId);
            interlocutorInfo.setCreateDataTime(new Date());
            interlocutorInfo.setCreateDataUsername(SystemHelper.getCurrentUsername());
            //无组旋钮用户信息
            JSONObject userInfo = new JSONObject();
            userInfo.put("userId", interlocutorInfo.getInterlocutorId());
            userInfo.put("knobNo", knobNo);
            knobUserList.add(userInfo);
        }
        interlocutorInfoList.addAll(noGroupKnobInterlocutorList);
        return addGroupMemberParam;
    }

    /**
     * 获得没有使用的旋钮编号
     * @param interlocutorInfo 对讲对象信息
     * @return 旋钮编号
     */
    private Integer getNoUseKnobNo(InterlocutorInfo interlocutorInfo) {
        Integer knobNo = 1;
        Integer knobNum = interlocutorInfo.getKnobNum();
        String knobNos = interlocutorInfo.getKnobNos();
        if (StringUtils.isNotBlank(knobNos)) {
            // 已经使用的旋钮编号集合
            List<String> alreadyUseKnobNoList = Arrays.asList(knobNos.split(","));
            List<String> allKnobNoList = new ArrayList<>();
            for (int i = 1; i <= knobNum; i++) {
                allKnobNoList.add(String.valueOf(i));
            }
            allKnobNoList.removeAll(alreadyUseKnobNoList);
            if (CollectionUtils.isEmpty(allKnobNoList)) {
                return null;
            }
            knobNo = Integer.valueOf(allKnobNoList.get(0));
        }
        return knobNo;
    }

    /**
     * 添加分组和权限
     * @param assignmentName  分组名称
     * @param groupCallNumber 组呼号码
     * @param intercomGroupId 对讲分组id
     * @param type            1:固定组 2:任务组 3:临时组
     * @return 分组id
     * @throws Exception Exception
     */
    private String addAssignmentInfoAndPermission(String assignmentName, String groupCallNumber, Long intercomGroupId,
        Integer type) throws Exception {
        UserLdap currentUser = SystemHelper.getCurrentUser();
        String username = currentUser.getUsername();
        String userId = currentUser.getId().toString();
        String orgId = userId.substring(userId.indexOf(",") + 1);
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        // admin用户需要移除admin的所属企业,修改树的顶级节点为admin下的第一个组织
        if (Objects.equals(username, "admin")) {
            // 用户当前组织和所属下级组织信息
            List<OrganizationLdap> userOwnAuthorityOrganizeInfo = userService.getUserOwnAuthorityOrganizeInfo(orgId);
            currentOrganization = updateOrganizationStructure(currentOrganization, userOwnAuthorityOrganizeInfo);
        }
        // 当前用户所属组织id
        String currentGroupId = currentOrganization.getUuid();
        // 分组信息
        ClusterForm assignmentInfo = new ClusterForm();
        String assignmentId = assignmentInfo.getId();
        assignmentInfo.setName(assignmentName);
        assignmentInfo.setCreateDataUsername(username);
        assignmentInfo.setFlag(type);
        assignmentInfo.setGroupCallNumber(groupCallNumber);
        assignmentInfo.setIntercomGroupId(intercomGroupId);
        assignmentInfo.setTypes((short) 1);
        // 添加分组
        clusterDao.addInterlocutorAssignment(assignmentInfo);
        // 分组关联企业信息
        AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
        assignmentGroupForm.setAssignmentId(assignmentId);
        assignmentGroupForm.setGroupId(currentGroupId);
        assignmentGroupForm.setCreateDataUsername(username);
        // 添加分组所属企业
        clusterDao.addGroupAssignment(assignmentGroupForm);
        // 当前登录用户uuid
        String currentUserId = userService.getUserUuidById(currentUser.getId().toString());
        AssignmentUserForm assignmentUserForm = new AssignmentUserForm(assignmentId, currentUserId);
        clusterDao.addAssignmentUserByBatch(Collections.singletonList(assignmentUserForm));
        return assignmentId;
    }

    /**
     * 删除群组
     * @param intercomGroupId 对讲群组id
     * @return JsonResultBean
     */
    private JsonResultBean deleteGroup(Long intercomGroupId) {
        JSONObject deleteGroupResultJsonObj = talkCallUtil.deleteGroup(intercomGroupId);
        if (deleteGroupResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "删除群组异常，请联系管理员");
        }
        Integer deleteGroupStatusCode = deleteGroupResultJsonObj.getInteger("result");

        if (!Objects.equals(ErrorMessageEnum.CODE_0.getCode(), deleteGroupStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT, ErrorMessageEnum.getMessage(deleteGroupStatusCode));
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean addTemporaryAssignment(String assignmentName, String ipAddress, Long intercomGroupId,
        String interlocutorIds) throws Exception {
        // 添加分组和权限
        String assignmentId = addAssignmentInfoAndPermission(assignmentName, null, intercomGroupId, 3);
        if (StringUtils.isNotBlank(interlocutorIds)) {
            // 添加临时组内成员
            tempAssignmentInsertMonitor(intercomGroupId, interlocutorIds, assignmentId);
        }
        logSearchService.addLog(ipAddress, "创建“" + assignmentName + "”临时组", "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private void tempAssignmentInsertMonitor(Long intercomGroupId, String interlocutorIds, String assignmentId) {
        List<TempAssignmentInterlocutor> tempAssignmentInterlocutorList = new ArrayList<>();
        List<String> interlocutorIdStrList = Arrays.asList(interlocutorIds.split(","));
        List<Long> interlocutorIdList = interlocutorIdStrList.stream().map(Long::valueOf).collect(Collectors.toList());
        for (Long interlocutorId : interlocutorIdList) {
            TempAssignmentInterlocutor tempAssignmentInterlocutor = new TempAssignmentInterlocutor();
            tempAssignmentInterlocutor.setAssignmentId(assignmentId);
            tempAssignmentInterlocutor.setIntercomGroupId(intercomGroupId);
            tempAssignmentInterlocutor.setInterlocutorId(interlocutorId);
            tempAssignmentInterlocutorList.add(tempAssignmentInterlocutor);
        }
        clusterDao.addTemporaryAssignmentInterlocutorId(assignmentId, intercomGroupId, tempAssignmentInterlocutorList);
    }

    @Override
    public JsonResultBean unbindAssignmentAndMonitor(String assignmentId, String assignmentType) throws Exception {
        if (Objects.equals(assignmentType, "2")) {
            log.info("解散对讲组模块：删除车辆--分组关系：删除分组id:{},操作用户：{}", assignmentId,
                SystemHelper.getCurrentUsername());
            return unbindTaskAssignmentAndMonitor(assignmentId);
        }
        if (Objects.equals(assignmentType, "3")) {
            Long intercomGroupId = Long.valueOf(assignmentId);
            // 删除临时组内成员
            clusterDao.delTemporaryAssignmentInterlocutorByIntercomGroupId(intercomGroupId, null);
            Cluster assignmentInfo = clusterDao.findAssignmentByIntercomGroupId(intercomGroupId);
            assignmentId = assignmentInfo != null ? assignmentInfo.getId() : null;
            return unbindTemporaryAssignmentAndMonitor(assignmentId);
        }
        return new JsonResultBean(JsonResultBean.FAULT, "分组类型错误");
    }

    /**
     * 解散任务组
     * @param assignmentId 分组id
     * @return JsonResultBean
     * @throws Exception Exception
     */
    private JsonResultBean unbindTaskAssignmentAndMonitor(String assignmentId) throws Exception {
        Cluster assignmentInfo = clusterDao.findAssignmentById(assignmentId);
        // 删除群组
        JsonResultBean deleteGroupResultJsonObj = deleteGroup(assignmentInfo.getIntercomGroupId());
        if (!deleteGroupResultJsonObj.isSuccess()) {
            return deleteGroupResultJsonObj;
        }
        // 解除分组下的监控对象绑定
        clusterDao.unbindAssignmentAndMonitor(assignmentId);
        // 删除分组
        clusterDao.deleteAssignment(assignmentId);
        // 删除分组与企业的关联
        clusterDao.deleteAssignmentGroupByAssId(assignmentId);
        // 删除分组与人的关联
        clusterDao.deleteAssignmentUserByAssId(assignmentId);
        // 释放组呼号码
        intercomCallNumberService.updateAndRecycleGroupCallNumber(assignmentInfo.getGroupCallNumber());
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean judgeAssignmentIfJoinMonitor(String assignmentId, String assignmentType) throws Exception {
        if (Objects.equals(assignmentType, "2")) {
            return judgeTaskAssignmentIfJoinMonitor(assignmentId);
        }
        if (Objects.equals(assignmentType, "3")) {
            return judgeTemporaryAssignmentIfJoinMonitor(assignmentId);
        }
        return new JsonResultBean(JsonResultBean.FAULT, "分组类型错误");
    }

    /**
     * 判断任务组是否能加入对讲对象
     * @param assignmentId 分组id
     * @return JsonResultBean
     */
    private JsonResultBean judgeTaskAssignmentIfJoinMonitor(String assignmentId) {
        // 查找分组和分组下的监控对象数量信息
        Cluster assignmentInfo = clusterDao.findAssignmentAndMonitorNumById(assignmentId);
        Integer assignmentMonitorNum = assignmentInfo.getMNum();
        Boolean ifCanAdd = assignmentMonitorNum == null || assignmentMonitorNum < ASSIGNMENT_MXA_MONITOR_NUM;
        return new JsonResultBean(ifCanAdd);
    }

    /**
     * 判断临时组是否能加入对讲对象
     * @param assignmentId 分组id
     * @return JsonResultBean
     */
    private JsonResultBean judgeTemporaryAssignmentIfJoinMonitor(String assignmentId) {
        JSONArray temporaryAssignmentInterlocutorJsonArr = new JSONArray();
        JsonResultBean getTemporaryAssignmentInterlocutorInfoResult =
            getTemporaryAssignmentInterlocutorInfo(assignmentId, temporaryAssignmentInterlocutorJsonArr);
        if (!getTemporaryAssignmentInterlocutorInfoResult.isSuccess()) {
            return getTemporaryAssignmentInterlocutorInfoResult;
        }
        Boolean ifCanAdd = temporaryAssignmentInterlocutorJsonArr.size() < ASSIGNMENT_MXA_MONITOR_NUM;
        return new JsonResultBean(ifCanAdd);
    }

    @Override
    public JsonResultBean insertTaskAssignmentAndMember(String assignmentId, String interlocutorIds, String ipAddress)
        throws Exception {
        Cluster assignmentInfo = clusterDao.findAssignmentById(assignmentId);
        // 分组添加监控对象
        JsonResultBean assignmentInsertMonitorResult =
            assignmentInsertMonitor(interlocutorIds, assignmentInfo.getIntercomGroupId(), assignmentId);
        if (assignmentInsertMonitorResult.isSuccess()) {
            logSearchService.addLog(ipAddress, "“" + assignmentInfo.getName() + "”任务组加入成员", "3", "", "-", "");
        }
        return assignmentInsertMonitorResult;
    }

    @Override
    public JsonResultBean insertTemporaryAssignmentRecordLog(Long intercomGroupId, String interlocutorIds,
        String ipAddress) throws Exception {
        Cluster assignmentInfo = clusterDao.findAssignmentByIntercomGroupId(intercomGroupId);
        if (StringUtils.isNotBlank(interlocutorIds)) {
            // 添加临时组内成员
            tempAssignmentInsertMonitor(intercomGroupId, interlocutorIds, assignmentInfo.getId());
        }
        logSearchService.addLog(ipAddress, "“" + assignmentInfo.getName() + "”临时组加入成员", "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean removeTaskAssignmentInterlocutor(String assignmentId, Long interlocutorId, String ipAddress)
        throws Exception {
        Cluster assignmentInfo = clusterDao.findAssignmentById(assignmentId);
        InterlocutorInfo interlocutorInfo = intercomPersonnelDao.getInterlocutorInfoByInterlocutorId(interlocutorId);
        JSONObject deleteGroupMemberListResultJsonObj = talkCallUtil
            .deleteGroupMemberList(assignmentInfo.getIntercomGroupId(), Collections.singletonList(interlocutorId));
        if (deleteGroupMemberListResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "删除群组成员异常，请联系管理员");
        }
        Integer deleteGroupMemberListStatusCode = deleteGroupMemberListResultJsonObj.getInteger("result");
        if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), deleteGroupMemberListStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT,
                DispatchErrorMessageEnum.getMessage(deleteGroupMemberListStatusCode));
        }
        log.info("踢出任务组内对讲对象：删除车辆--分组关系：车辆id:{}, 分组id:{},操作用户：{}",
            interlocutorInfo.getMonitorId(), assignmentId, SystemHelper.getCurrentUsername());
        clusterDao.deleteVehiclePerBatch(assignmentId, Collections.singletonList(interlocutorInfo.getMonitorId()));
        logSearchService
            .addLog(ipAddress, "“" + interlocutorInfo.getMonitorName() + "”被踢出“" + assignmentInfo.getName() + "”群组",
                "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean removeTemporaryAssignmentInterlocutorRecordLog(Long intercomGroupId, Long interlocutorId,
        String ipAddress) throws Exception {
        Cluster assignmentInfo = clusterDao.findAssignmentByIntercomGroupId(intercomGroupId);
        InterlocutorInfo interlocutorInfo = intercomPersonnelDao.getInterlocutorInfoByInterlocutorId(interlocutorId);
        // 删除临时组内成员
        clusterDao.delTemporaryAssignmentInterlocutorByIntercomGroupId(intercomGroupId, interlocutorId);
        logSearchService
            .addLog(ipAddress, "“" + interlocutorInfo.getMonitorName() + "”被踢出“" + assignmentInfo.getName() + "”群组",
                "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private JsonResultBean unbindTemporaryAssignmentAndMonitor(String assignmentId) throws Exception {
        if (StringUtils.isNotBlank(assignmentId)) {
            // 删除分组
            clusterDao.deleteAssignment(assignmentId);
            // 删除分组与企业的关联
            clusterDao.deleteAssignmentGroupByAssId(assignmentId);
            // 删除分组与人的关联
            clusterDao.deleteAssignmentUserByAssId(assignmentId);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 调度服务登出并清除临时组
     * @param userName 用户名称
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public void dispatchLoginOut(String userName) throws Exception {
        JSONObject dispatchUserLoginInfo = WebSubscribeManager.getInstance().getDispatchUserLoginInfo(userName);
        // 移除调度用户登录信息
        WebSubscribeManager.getInstance().removeDispatchUserLoginInfo(userName);
        // 删除用户拥有临时组
        clusterService.delUserOwnTemporaryAssignment(userName);
        if (dispatchUserLoginInfo == null) {
            log.info("登出调度服务异常，调度服务未登录");
            return;
        }
        JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
        // 清除临时组
        JSONObject clearTempGroupResultJsonObj =
            talkCallUtil.clearTempGroupList(data.getLong("custId"), data.getLong("id"), userName);
        if (clearTempGroupResultJsonObj == null) {
            log.error("清除临时组异常，请联系管理员");
        } else {
            Integer clearTempGroupStatusCode = clearTempGroupResultJsonObj.getInteger("result");
            if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), clearTempGroupStatusCode)) {
                log.error(DispatchErrorMessageEnum.getMessage(clearTempGroupStatusCode));
            }
        }
        //调度服务登出
        JSONObject dispatchLoginOutResultJsonObj = talkCallUtil.dispatchLoginOut(userName);
        if (dispatchLoginOutResultJsonObj == null) {
            log.error("登出调度服务异常，请联系管理员");
        } else {
            Integer dispatchLoginOutStatusCode = dispatchLoginOutResultJsonObj.getInteger("result");
            if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), dispatchLoginOutStatusCode)) {
                log.error(DispatchErrorMessageEnum.getMessage(dispatchLoginOutStatusCode));
            }
        }
        // 移除保存的cookie
        HttpClientUtil.removeSavedCookie(userName);
    }

}
