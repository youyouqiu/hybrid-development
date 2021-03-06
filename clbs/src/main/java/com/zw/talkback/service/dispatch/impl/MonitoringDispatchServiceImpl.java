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
 * ???????????????????????????
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
     * ??????????????????????????????
     */
    private static final Integer ASSIGNMENT_MXA_MONITOR_NUM = 400;

    /**
     * ??????????????????
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
            // ??????????????????????????????id??????
            installUserOwnTempAssignmentIntercomGroupIdList(userName, data, userId);
            dispatchUserLoginInfo.put("data", data);
            return new JsonResultBean(dispatchUserLoginInfo);
        }
        JSONObject dispatchLoginInResultJsonObj = talkCallUtil.dispatchLoginIn(userName, currentUser.getPassword());
        if (dispatchLoginInResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????");
        }
        Integer dispatchLoginInStatusCode = dispatchLoginInResultJsonObj.getInteger("result");
        if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), dispatchLoginInStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT,
                DispatchErrorMessageEnum.getMessage(dispatchLoginInStatusCode));
        }
        // ??????????????????????????????????????????????????????????????????
        JSONObject data = dispatchLoginInResultJsonObj.getJSONObject("data");
        data.put("audioServerIP", audioServerIP);
        data.put("dispatchServicePort", dispatchServicePort);
        data.put("eventServicePort", eventServicePort);
        // ????????????????????????
        boolean isOwnPreventSpeechRole = false;
        if (StringUtils.isNotBlank(userId)) {
            Collection<Group> userRoleList = userService.findByMember(userId);
            if (CollectionUtils.isNotEmpty(userRoleList)) {
                isOwnPreventSpeechRole =
                    userRoleList.stream().anyMatch(roleInfo -> Objects.equals("????????????", roleInfo.getRoleName()));
            }
        }
        data.put("isOwnPreventSpeechRole", isOwnPreventSpeechRole);
        // ??????????????????????????????id??????
        installUserOwnTempAssignmentIntercomGroupIdList(userName, data, userId);
        dispatchLoginInResultJsonObj.put("data", data);
        // ??????????????????????????????
        WebSubscribeManager.getInstance().saveDispatchUserLoginInfo(userName, dispatchLoginInResultJsonObj);
        return new JsonResultBean(dispatchLoginInResultJsonObj);
    }

    /**
     * ??????????????????????????????id??????
     * @param userName ????????????
     * @param data     ??????
     * @param userId   ??????id
     */
    private void installUserOwnTempAssignmentIntercomGroupIdList(String userName, JSONObject data, String userId) {
        // ??????????????????????????????id
        String uuid = userService.getUserUuidById(userId);
        String orgId = userId.substring(userId.indexOf(",") + 1);
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        if (Objects.equals(userName, "admin")) {
            // ?????????????????????????????????????????????
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
        // ???????????????????????????
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        // ?????????????????????????????????????????????
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo = userService.getUserOwnAuthorityOrganizeInfo(orgId);
        // ???????????????????????????????????????id
        List<String> userOwnAuthorityOrganizeId =
            userOwnAuthorityOrganizeInfo.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        // ????????????????????????????????????
        List<Cluster> assignmentList = clusterService.findUserAssignment(uuid, userOwnAuthorityOrganizeId);
        if (Objects.equals(currentUser.getUsername(), "admin")) {
            currentOrganization = updateOrganizationStructure(currentOrganization, userOwnAuthorityOrganizeInfo);
        }
        Cluster firstTaskAssignmentInfo = null;
        if (currentOrganization != null) {
            String currentGroupId = currentOrganization.getUuid();
            // ????????????????????????
            List<Cluster> userOwnTaskAssignment = clusterDao.findUserOwnTaskAssignment(uuid, currentGroupId);
            for (Cluster assignment : userOwnTaskAssignment) {
                assignment.setGroupId("taskOrganization");
            }
            assignmentList.addAll(userOwnTaskAssignment);
            firstTaskAssignmentInfo =
                CollectionUtils.isNotEmpty(userOwnTaskAssignment) ? userOwnTaskAssignment.get(0) : null;
            if (firstTaskAssignmentInfo != null) {
                // ??????????????????????????????????????????
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
            // ?????????????????????
            userOwnAuthorityOrganizeInfo.add(1, installTaskOrganization(currentOrganization.getId().toString()));
            // ?????????????????????
            userOwnAuthorityOrganizeInfo.add(1, installTemporaryOrganization(currentOrganization.getId().toString()));
        }
        installAssignmentInfoToTree(assignmentList, userOwnAuthorityOrganizeInfo, treeInfoJsonArr,
            firstTaskAssignmentInfo, false);
        treeInfoJsonArr.addAll(JsonUtil.getGroupTree(userOwnAuthorityOrganizeInfo, null, true));
        // ????????????????????????????????????????????????????????????????????????????????????
        setUpTaskAndTemporaryLocation(treeInfoJsonArr);
        Object treeInfo = ZipUtil.compress(treeInfoJsonArr.toJSONString());
        return new JsonResultBean(treeInfo);
    }

    /**
     * ?????????????????????????????????
     * @param firstAssignmentInfo          ????????????????????????
     * @param treeInfoJsonArr              ???
     * @param interlocutorStatus           0:??????; 1:??????; 2:??????;
     * @param needFilterInterlocutorIdList ???????????????????????????id??????
     * @return JsonResultBean
     */
    private JsonResultBean installAssignmentMonitorInfoToTree(Cluster firstAssignmentInfo, JSONArray treeInfoJsonArr,
        Integer interlocutorStatus, List<Long> needFilterInterlocutorIdList) {
        String assignmentId = firstAssignmentInfo.getId();
        String assignmentName = firstAssignmentInfo.getName();
        Long intercomGroupId = firstAssignmentInfo.getIntercomGroupId();
        // ??????????????????
        JSONArray interlocutorAssignmentMemberArr = new JSONArray();
        // ????????????????????????
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
            // ???????????????????????????id
            List<TempAssignmentInterlocutor> inTemporaryAssignmentInterlocutorInfoList =
                clusterDao.findInTemporaryAssignmentInterlocutorInfo(interlocutorIdList);
            List<InterlocutorInfo> interlocutorInfoList =
                intercomPersonnelDao.getInterlocutorInfoByInterlocutorIdList(interlocutorIdList);
            // ??????????????????
            boolean isTempAssignment = Objects.equals(Integer.valueOf(firstAssignmentInfo.getFlag()), 3);
            for (int i = 0, len = interlocutorAssignmentMemberArr.size(); i < len; i++) {
                JSONObject interlocutorInfoJsonObj = interlocutorAssignmentMemberArr.getJSONObject(i);
                Long interlocutorId = interlocutorInfoJsonObj.getLong("userId");
                // ???????????????????????????????????? ???????????????????????????????????????
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
                // ???????????????????????????
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
     * ????????????????????????
     * @param intercomGroupId                 ?????????id
     * @param interlocutorAssignmentMemberArr ???????????????(??????????????????)
     * @param interlocutorStatus              0:??????; 1:??????; 2:??????;
     * @return JsonResultBean
     */
    private JsonResultBean getGroupMemberList(Long intercomGroupId, JSONArray interlocutorAssignmentMemberArr,
        Integer interlocutorStatus) {
        JSONObject dispatchUserLoginInfo =
            WebSubscribeManager.getInstance().getDispatchUserLoginInfo(SystemHelper.getCurrentUsername());
        if (dispatchUserLoginInfo == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
        }
        JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
        JSONObject queryInGroupMemberListResultJsonObj =
            talkCallUtil.queryInGroupMemberList(data.getLong("custId"), intercomGroupId, null, null, 1, 500);
        if (queryInGroupMemberListResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????????????????");
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
            //????????????????????????????????????????????????????????????
            records.removeIf(info -> (isNeedFilterStatus && !Objects
                .equals(((JSONObject) info).getInteger("audioOnlineStatus"), finalInterlocutorStatus)) || !Objects
                .equals(((JSONObject) info).getInteger("type"), 0));
            interlocutorAssignmentMemberArr.addAll(records);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ???????????????????????????
     * @param interlocutorInfoJsonObj ??????????????????
     * @param interlocutorId          ????????????id
     * @param assignmentId            ??????id
     * @param assignmentName          ????????????
     * @param monitorId               ????????????id
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
     * ???????????????????????????
     * @param pid ??????????????????????????????pid
     * @return ???????????????
     */
    private OrganizationLdap installTaskOrganization(String pid) {
        OrganizationLdap taskOrganization = new OrganizationLdap();
        taskOrganization.setId(LdapUtils.newLdapName("ou=taskOrganization,ou=Enterprise_top,ou=organization"));
        taskOrganization.setCid("ou=taskOrganization,ou=Enterprise_top,ou=organization");
        taskOrganization.setUuid("taskOrganization");
        taskOrganization.setName("?????????");
        taskOrganization.setPid(pid);
        return taskOrganization;
    }

    /**
     * ???????????????????????????
     * @param pid ??????????????????????????????id
     * @return ???????????????
     */
    private OrganizationLdap installTemporaryOrganization(String pid) {
        OrganizationLdap temporaryOrganization = new OrganizationLdap();
        temporaryOrganization
            .setId(LdapUtils.newLdapName("ou=temporaryOrganization,ou=Enterprise_top,ou=organization"));
        temporaryOrganization.setCid("ou=temporaryOrganization,ou=Enterprise_top,ou=organization");
        temporaryOrganization.setUuid("temporaryOrganization");
        temporaryOrganization.setName("?????????");
        temporaryOrganization.setPid(pid);
        return temporaryOrganization;
    }

    /**
     * ????????????????????????
     * @param assignmentList          ????????????
     * @param organizeInfoList        ????????????
     * @param treeInfoJsonArr         ???
     * @param needShowAssignmentInfo  ???????????????????????????
     * @param isNeedShowAllAssignment ?????????????????????????????????
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
     * ????????????????????????????????????????????????????????????????????????????????????
     * @param treeInfoJsonArr ???????????????
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
        // ?????????????????????????????????????????????
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo = userService.getUserOwnAuthorityOrganizeInfo(orgId);
        // ???????????????????????????????????????id
        List<String> userOwnAuthorityOrganizeId =
            userOwnAuthorityOrganizeInfo.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        if (Objects.equals(currentUser.getUsername(), "admin")) {
            currentOrganization = updateOrganizationStructure(currentOrganization, userOwnAuthorityOrganizeInfo);
        }
        // ????????????????????????????????????
        List<Cluster> assignmentList = clusterService.findUserAssignment(uuid, userOwnAuthorityOrganizeId);
        if (currentOrganization != null) {
            String currentGroupId = currentOrganization.getUuid();
            // ?????????????????????
            List<Cluster> userOwnTaskAssignment = clusterDao.findUserOwnTaskAssignment(uuid, currentGroupId);
            for (Cluster assignment : userOwnTaskAssignment) {
                assignment.setGroupId("taskOrganization");
                assignment.setFlag((short) 2);
            }
            assignmentList.addAll(userOwnTaskAssignment);
            // ?????????????????????
            userOwnAuthorityOrganizeInfo.add(1, installTaskOrganization(currentOrganization.getId().toString()));
            // ?????????????????????
            List<Cluster> userOwnTemporaryAssignment = clusterDao.findUserOwnTemporaryAssignment(uuid, currentGroupId);
            for (Cluster assignment : userOwnTemporaryAssignment) {
                assignment.setGroupId("temporaryOrganization");
                assignment.setFlag((short) 3);
            }
            assignmentList.addAll(userOwnTemporaryAssignment);
            // ???????????????
            userOwnAuthorityOrganizeInfo.add(1, installTemporaryOrganization(currentOrganization.getId().toString()));
            if (Objects.equals(queryType, "name")) {
                isNeedShowAllAssignment = true;
                if (CollectionUtils.isNotEmpty(assignmentList)) {
                    List<String> assignmentIdList =
                        assignmentList.stream().map(Cluster::getId).collect(Collectors.toList());
                    // ??????????????????id?????????????????????????????????,??????????????????????????????,?????????????????????
                    List<InterlocutorInfo> interlocutorInfoList = StringUtils.isNotBlank(queryParam)
                        ? clusterDao.findInterlocutorByAssignmentIdsAndNameFuzzy(assignmentIdList, queryParam) :
                        new ArrayList<>();
                    // ??????????????????????????????
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
                        // ????????????id
                        Long interlocutorId = interlocutorInfoJsonObj.getLong("userId");
                        //??????????????????????????????id
                        Long defaultGroupId = interlocutorInfoJsonObj.getLong("defaultGroupId");
                        // ??????????????????????????? ???????????????????????????id????????????id
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
                        // ???????????????????????????
                        JSONObject interlocutorInfo =
                            installInterlocutorTreeInfo(interlocutorInfoJsonObj, interlocutorId, assignmentId,
                                assignment.getName(), info.getMonitorId());
                        treeInfoJsonArr.add(interlocutorInfo);
                        filterAssignmentIdList.add(assignmentId);
                    }
                    // ?????????????????????
                    assignmentList = assignmentList.stream()
                        .filter(assignment -> filterAssignmentIdList.contains(assignment.getId()))
                        .collect(Collectors.toList());
                }
            } else if (Objects.equals(queryType, "assignment")) {
                assignmentList = StringUtils.isNotBlank(queryParam)
                    ? assignmentList.stream().filter(assignment -> assignment.getName().contains(queryParam))
                        .collect(Collectors.toList()) : new ArrayList<>();
                if (CollectionUtils.isNotEmpty(assignmentList)) {
                    // ?????????????????????????????????
                    needShowAssignment = getFirstNeedShowAssignment(userOwnAuthorityOrganizeInfo, assignmentList);
                    if (needShowAssignment != null) {
                        // ??????????????????????????????????????????
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
        // ????????????????????????????????????????????????????????????????????????????????????
        setUpTaskAndTemporaryLocation(treeInfoJsonArr);
        Object treeInfo = ZipUtil.compress(treeInfoJsonArr.toJSONString());
        return new JsonResultBean(treeInfo);
    }

    /**
     * ??????????????????
     * @param interlocutorInfoList   ????????????
     * @param queryUserResultJsonArr ?????????????????????
     * @param status                 0 ??? null:??????; 1:??????; 2:??????;
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
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
        }
        JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
        if (userList.size() > 0) {
            // ????????????
            JsonResultBean queryUserListResult =
                queryUserList(data.getLong("custId"), userList, 1, 1000, queryUserResultJsonArr, status, null);
            if (!queryUserListResult.isSuccess()) {
                return queryUserListResult;
            }
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ????????????????????????????????????
     * @param userOwnAuthorityOrganizeInfo ?????????????????????????????????
     * @param assignmentList               ????????????????????????
     * @return Assignment
     */
    private Cluster getFirstNeedShowAssignment(List<OrganizationLdap> userOwnAuthorityOrganizeInfo,
        List<Cluster> assignmentList) {
        // ??????????????????????????? ???????????????????????????
        List<Cluster> temporaryAssignmentList =
            assignmentList.stream().filter(assignment -> Objects.equals(Integer.valueOf(assignment.getFlag()), 3))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(temporaryAssignmentList)) {
            return temporaryAssignmentList.get(0);
        }
        // ????????????????????? ???????????????????????? ??????????????????
        List<Cluster> taskAssignmentList =
            assignmentList.stream().filter(assignment -> Objects.equals(Integer.valueOf(assignment.getFlag()), 2))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(taskAssignmentList)) {
            return taskAssignmentList.get(0);
        }
        // ????????????????????? ????????????????????????
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
     * ????????????
     * @param custId                 ????????????ID ??????????????????-1??????????????????????????????????????????????????????
     * @param userList               ??????????????????
     * @param pageIndex              ????????????
     * @param pageSize               ??????????????????
     * @param queryUserResultJsonArr ?????????????????????
     * @param status                 0 ??? null:??????; 1:??????; 2:??????;
     * @param fixedQuantity          ???????????? ???null ????????????
     * @return JsonResultBean
     */
    private JsonResultBean queryUserList(Long custId, JSONArray userList, Integer pageIndex, Integer pageSize,
        JSONArray queryUserResultJsonArr, Integer status, Integer fixedQuantity) {
        JSONObject queryUserListResultJsonObj = talkCallUtil.queryUserList(custId, userList, pageIndex, pageSize);
        if (queryUserListResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
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
                    // ????????????????????????????????????
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
     * ????????????????????????????????????
     * @param interlocutorStatus      0:??????; 1:??????; 2:??????;
     * @param interlocutorInfoJsonObj ??????????????????
     * @return boolean
     */
    private boolean judgeInterlocutorStatusIsEqual(Integer interlocutorStatus, JSONObject interlocutorInfoJsonObj) {
        // ?????????????????? 1:?????? 0:?????????
        Integer audioOnlineStatus = interlocutorInfoJsonObj.getInteger("audioOnlineStatus");
        audioOnlineStatus = !Objects.equals(audioOnlineStatus, 0) ? audioOnlineStatus : 2;
        return Objects.equals(interlocutorStatus, audioOnlineStatus);
    }

    @Override
    public JsonResultBean getInterlocutorAssignmentMember(Long intercomGroupId, Integer interlocutorStatus)
        throws Exception {
        // ??????????????????
        JSONArray interlocutorAssignmentMemberArr = new JSONArray();
        JSONArray interlocutors = new JSONArray();
        // ????????????
        Cluster assignmentInfo = clusterDao.findAssignmentByIntercomGroupId(intercomGroupId);
        if (assignmentInfo != null) {
            // ????????????????????????
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

            // ?????????????????????????????? ????????????????????????????????????
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
            return new JsonResultBean(JsonResultBean.SUCCESS, "??????????????????");
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
            return new JsonResultBean(JsonResultBean.SUCCESS, "??????????????????");
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
        // ???????????????????????????????????????????????????
        List<Cluster> assignmentList = getUserOwnFixedAssignmentInfoList();
        List<InterlocutorInfo> interlocutorInfoList = getInterlocutorInfoByFixedAssignment(assignmentList);
        // ?????????????????????????????????
        filterInTempAssignmentInterlocutor(interlocutorInfoList);
        List<Long> intercomGroupIdIdList =
            assignmentList.stream().map(Cluster::getIntercomGroupId).collect(Collectors.toList());
        return getInAreaAndOnlineInterlocutorInfoList(intercomGroupIdIdList, interlocutorInfoList, "circle",
            assignmentId, assignmentType, longitude, latitude, radius, null, null, null, null);
    }

    /**
     * ??????????????????????????????????????????
     * @return List<Assignment>
     * @throws Exception Exception
     */
    private List<Cluster> getUserOwnFixedAssignmentInfoList() throws Exception {
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // ?????????????????????????????????????????????
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo =
            userService.getUserOwnAuthorityOrganizeInfo(userId.substring(userId.indexOf(",") + 1));
        // ???????????????????????????????????????id
        List<String> userOwnAuthorityOrganizeId =
            userOwnAuthorityOrganizeInfo.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        // ????????????????????????????????????
        return clusterService.findUserAssignment(uuid, userOwnAuthorityOrganizeId);
    }

    /**
     * ???????????????????????????????????????????????????
     * @param assignmentList ???????????????
     * @return List<InterlocutorInfo>
     * @throws Exception Exception
     */
    private List<InterlocutorInfo> getInterlocutorInfoByFixedAssignment(List<Cluster> assignmentList) throws Exception {
        List<String> assignmentIdList = assignmentList.stream().map(Cluster::getId).collect(Collectors.toList());
        // ??????????????????????????????
        return CollectionUtils.isNotEmpty(assignmentIdList)
            ? clusterDao.findInterlocutorByAssignmentIdsAndNameFuzzy(assignmentIdList, null) : new ArrayList<>();
    }

    /**
     * ?????????????????????????????????
     * @param interlocutorInfoList ?????????????????????????????????
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
     * ??????????????????????????????????????????
     * @param intercomGroupIdIdList ??????????????????
     * @param interlocutorInfoList  ???????????????????????????
     * @param areaType              ????????????  circle:???; rectangle:??????;
     * @param assignmentId          ?????????????????????, ??????????????????????????????id, ???????????????????????????null
     * @param assignmentType        ???????????? 2????????????; 3:?????????, ???????????????????????????null
     * @param longitude             ?????????
     * @param latitude              ?????????
     * @param radius                ?????????
     * @param leftLongitude         ??????????????????????????????
     * @param leftLatitude          ??????????????????????????????
     * @param rightLongitude        ??????????????????????????????
     * @param rightLatitude         ??????????????????????????????
     * @return JsonResultBean
     * @throws Exception Exception
     */
    private JsonResultBean getInAreaAndOnlineInterlocutorInfoList(List<Long> intercomGroupIdIdList,
        List<InterlocutorInfo> interlocutorInfoList, String areaType, String assignmentId, String assignmentType,
        Double longitude, Double latitude, Double radius, Double leftLongitude, Double leftLatitude,
        Double rightLongitude, Double rightLatitude) throws Exception {
        JSONArray resultJsonArr = new JSONArray();
        // ?????????????????????????????????
        Integer needReturnInterlocutorNum = ASSIGNMENT_MXA_MONITOR_NUM;
        // ????????????????????? ?????????????????????????????????
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
            // ??????????????????
            JSONArray queryUserResultJsonArr = new JSONArray();
            JsonResultBean getUserInfoResult = getUserInfo(inAreaInterlocutorInfoList, queryUserResultJsonArr, 1);
            if (!getUserInfoResult.isSuccess()) {
                return getUserInfoResult;
            }
            // ?????????????????????????????????????????????
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
     * ?????????????????????????????????
     * @param longitude             ??????????????????
     * @param latitude              ??????????????????
     * @param circleCenterLongitude ????????????
     * @param circleCenterLatitude  ????????????
     * @param radius                ??????
     * @return boolean
     */
    private boolean judgeIsInCircle(Double longitude, Double latitude, Double circleCenterLongitude,
        Double circleCenterLatitude, Double radius) {
        //??????????????????????????????
        return AddressUtil.getDistance(circleCenterLongitude, circleCenterLatitude, longitude, latitude) <= radius;
    }

    /**
     * ????????????????????????????????????
     * @param longitude      ??????????????????
     * @param latitude       ??????????????????
     * @param leftLongitude  ??????????????????????????????
     * @param leftLatitude   ??????????????????????????????
     * @param rightLongitude ??????????????????????????????
     * @param rightLatitude  ??????????????????????????????
     * @return boolean
     */
    private boolean judgeIsInRectangle(Double longitude, Double latitude, Double leftLongitude, Double leftLatitude,
        Double rightLongitude, Double rightLatitude) {
        return leftLongitude <= longitude && rightLongitude >= longitude && leftLatitude >= latitude
            && rightLatitude <= latitude;
    }

    /**
     * ????????????????????????????????????
     * @param assignmentId               ??????id
     * @param temporaryAssignmentJsonArr ??????????????????????????????
     * @return JsonResultBean
     */
    private JsonResultBean getTemporaryAssignmentInterlocutorInfo(String assignmentId,
        JSONArray temporaryAssignmentJsonArr) {
        // ????????????????????????
        JsonResultBean getGroupMemberListResult =
            getGroupMemberList(Long.valueOf(assignmentId), temporaryAssignmentJsonArr, 0);
        if (!getGroupMemberListResult.isSuccess()) {
            return getGroupMemberListResult;
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????????,????????????????????????
     * @param assignmentId   ?????????????????????, ??????????????????????????????id, ???????????????????????????null
     * @param assignmentType ???????????? 2????????????; 3:?????????, ???????????????????????????null
     * @param leftLongitude  ??????????????????????????????
     * @param leftLatitude   ??????????????????????????????
     * @param rightLongitude ??????????????????????????????
     * @param rightLatitude  ??????????????????????????????
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean findInterlocutorByRectangleArea(String assignmentId, String assignmentType,
        Double leftLongitude, Double leftLatitude, Double rightLongitude, Double rightLatitude) throws Exception {
        // ???????????????????????????????????????????????????
        List<Cluster> assignmentList = getUserOwnFixedAssignmentInfoList();
        List<InterlocutorInfo> interlocutorInfoList = getInterlocutorInfoByFixedAssignment(assignmentList);
        // ?????????????????????????????????
        filterInTempAssignmentInterlocutor(interlocutorInfoList);
        List<Long> intercomGroupIdIdList =
            assignmentList.stream().map(Cluster::getIntercomGroupId).collect(Collectors.toList());
        return getInAreaAndOnlineInterlocutorInfoList(intercomGroupIdIdList, interlocutorInfoList, "rectangle",
            assignmentId, assignmentType, null, null, null, leftLongitude, leftLatitude, rightLongitude, rightLatitude);
    }

    /**
     * ??????????????????,??????????????????
     * @param assignmentId   ?????????????????????, ??????????????????????????????id, ???????????????????????????null
     * @param assignmentType ???????????? 2????????????; 3:?????????, ???????????????????????????null
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
        // ?????????????????????????????????????????????
        List<OrganizationLdap> userOwnAuthorityOrganizeInfo =
            userService.getUserOwnAuthorityOrganizeInfo(userId.substring(userId.indexOf(",") + 1));
        // ???????????????????????????????????????id
        List<String> userOwnAuthorityOrganizeId =
            userOwnAuthorityOrganizeInfo.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        List<Cluster> assignmentList = clusterService.findUserAssignment(uuid, userOwnAuthorityOrganizeId);

        // ????????????????????? ?????????????????????????????????
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
            // ??????????????????????????????????????????
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
     * ??????????????????
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllSkillList() throws Exception {
        return new JsonResultBean(skillDao.getAllSkillList());
    }

    /**
     * ????????????????????????
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllIntercomModeList() throws Exception {
        return new JsonResultBean(intercomModelDao.getAllIntercomModeList());
    }

    /**
     * ????????????????????????
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllDriverLicenseCategoryList() throws Exception {
        return new JsonResultBean(peopleBasicInfoDao.getAllDriverType());
    }

    /**
     * ?????????????????????
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean getAllQualificationList() throws Exception {
        return new JsonResultBean(peopleBasicInfoDao.getAllQualification());
    }

    /**
     * ??????????????????
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
        // ??????id??????
        List<String> assignmentIdList = assignmentList.stream().map(Cluster::getId).collect(Collectors.toList());
        // ??????id??????
        List<String> skillIdList = StringUtils.isNotBlank(skillIds) ? Arrays.asList(skillIds.split(",")) : null;
        // ????????????id??????
        List<String> intercomModelIdList =
            StringUtils.isNotBlank(intercomModelIds) ? Arrays.asList(intercomModelIds.split(",")) : null;
        // ????????????id??????
        List<String> driverLicenseCategoryIdList =
            StringUtils.isNotBlank(driverLicenseCategoryIds) ? Arrays.asList(driverLicenseCategoryIds.split(",")) :
                null;
        // ?????????id??????
        List<String> qualificationIdList =
            StringUtils.isNotBlank(qualificationIds) ? Arrays.asList(qualificationIds.split(",")) : null;
        // ??????id??????
        List<String> bloodTypeIdList =
            StringUtils.isNotBlank(bloodTypeIds) ? Arrays.asList(bloodTypeIds.split(",")) : null;
        // ??????
        List<String> ageRangeList =
            StringUtils.isNotBlank(ageRange) ? Arrays.asList(ageRange.split(",")) : new ArrayList<>();
        List<String> sortAgeRangeList =
            ageRangeList.stream().sorted(Comparator.comparingInt(Integer::valueOf)).collect(Collectors.toList());
        // ???????????????????????????????????????
        List<InterlocutorInfo> interlocutorInfoList = intercomPersonnelDao
            .findInterlocutorByCondition(null, assignmentIdList, skillIdList, intercomModelIdList,
                driverLicenseCategoryIdList, qualificationIdList, bloodTypeIdList, sortAgeRangeList, gender);
        // ?????????????????????????????????
        filterInTempAssignmentInterlocutor(interlocutorInfoList);
        return getInAreaAndOnlineInterlocutorInfoList(intercomGroupIdIdList, interlocutorInfoList, "circle",
            assignmentId, assignmentType, longitude, latitude, radius, null, null, null, null);
    }

    /**
     * ?????????????????????????????????????????????
     * @param interlocutorIds ????????????id
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public JsonResultBean judgeInterlocutorTaskAssignmentNumIsOverLimit(String interlocutorIds) throws Exception {
        JSONObject result = new JSONObject();
        // ?????????????????????????????????
        List<String> overLimitMonitorNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(interlocutorIds)) {
            List<String> interlocutorIdList = Arrays.asList(interlocutorIds.split(","));
            // ?????????????????????????????????
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
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????????????????????????????");
        }
        // ???????????????id
        Long intercomGroupId = null;
        String assignmentId;
        JSONObject assignmentInfoJsonObj = new JSONObject();
        try {
            // ???????????? ????????????
            JSONObject addGroupResultJsonObj = talkCallUtil.addGroup("0", assignmentName, groupCallNumber, null);
            if (addGroupResultJsonObj == null) {
                // ??????????????????
                intercomCallNumberService.updateAndRecycleGroupCallNumber(groupCallNumber);
                return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
            }
            Integer addGroupStatusCode = addGroupResultJsonObj.getInteger("result");
            JSONObject addGroupData = addGroupResultJsonObj.getJSONObject("data");
            if (!Objects.equals(ErrorMessageEnum.CODE_0.getCode(), addGroupStatusCode)) {
                // ??????????????????
                intercomCallNumberService.updateAndRecycleGroupCallNumber(groupCallNumber);
                return new JsonResultBean(JsonResultBean.FAULT, ErrorMessageEnum.getMessage(addGroupStatusCode));
            }
            // ???????????????id
            intercomGroupId = addGroupData != null ? addGroupData.getLong("groupId") : null;
            // ?????????????????????
            assignmentId = addAssignmentInfoAndPermission(assignmentName, groupCallNumber, intercomGroupId, 2);
            // ????????????????????????
            JsonResultBean assignmentInsertMonitorResult =
                assignmentInsertMonitor(interlocutorIds, intercomGroupId, assignmentId);
            if (assignmentInsertMonitorResult.isSuccess()) {
                logSearchService.addLog(ipAddress, "?????????" + assignmentName + "????????????", "3", "", "-", "");
            }
            assignmentInfoJsonObj.put("assignmentId", assignmentId);
            assignmentInfoJsonObj.put("intercomGroupId", intercomGroupId);
            assignmentInfoJsonObj.put("assignmentName", assignmentName);
            return new JsonResultBean(assignmentInfoJsonObj);
        } catch (Exception e) {
            if (intercomGroupId != null) {
                // ????????????
                JsonResultBean deleteGroupResultJsonObj = deleteGroup(intercomGroupId);
                if (!deleteGroupResultJsonObj.isSuccess()) {
                    return deleteGroupResultJsonObj;
                }
            }
            // ??????????????????
            intercomCallNumberService.updateAndRecycleGroupCallNumber(groupCallNumber);
            throw e;
        }
    }

    /**
     * ????????????????????????
     * @param interlocutorIds ??????????????????id
     * @param intercomGroupId ?????????????????????id
     * @param assignmentId    ??????id
     * @return JsonResultBean
     */
    private JsonResultBean assignmentInsertMonitor(String interlocutorIds, Long intercomGroupId, String assignmentId) {
        if (StringUtils.isBlank(interlocutorIds)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        List<String> interlocutorIdStrList = Arrays.asList(interlocutorIds.split(","));
        List<Long> interlocutorIdList = interlocutorIdStrList.stream().map(Long::valueOf).collect(Collectors.toList());
        // ??????????????????
        List<InterlocutorInfo> interlocutorInfoList =
            intercomPersonnelDao.getInterlocutorInfoByInterlocutorIdList(interlocutorIdList);
        //??????????????????????????????
        JSONObject addGroupMemberListParam =
            installAddGroupMemberListParam(intercomGroupId, interlocutorInfoList, assignmentId);
        // ???????????? ??????????????????
        JSONObject addGroupMemberListResultJsonObj = talkCallUtil.addGroupMemberList(addGroupMemberListParam);
        if (addGroupMemberListResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????");
        }
        Integer addGroupMemberListStatusCode = addGroupMemberListResultJsonObj.getInteger("result");
        JSONObject data = addGroupMemberListResultJsonObj.getJSONObject("data");
        // ????????????pid
        if (!Objects.equals(ErrorMessageEnum.CODE_0.getCode(), addGroupMemberListStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT, ErrorMessageEnum.getMessage(addGroupMemberListStatusCode));
        }
        // ???????????????????????????????????????
        List<InterlocutorInfo> filterList = interlocutorInfoList.stream().filter(
            info -> info.getKnobNum() == null || info.getKnobNum() <= 0 || (info.getKnobNum() != null
                && info.getKnobNo() != null)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(filterList)) {
            // ???????????????????????????
            clusterDao.addInterlocutorAssignmentMember(filterList);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ??????????????????????????????
     * @param intercomGroupId      ??????????????????ID
     * @param interlocutorInfoList ??????????????????
     * @param assignmentId         ??????id
     * @return JSONObject
     */
    private JSONObject installAddGroupMemberListParam(Long intercomGroupId, List<InterlocutorInfo> interlocutorInfoList,
        String assignmentId) {
        JSONObject addGroupMemberParam = new JSONObject();
        addGroupMemberParam.put("custId", talkCallUtil.getCustId());
        addGroupMemberParam.put("groupId", intercomGroupId);
        // ????????????????????????
        JSONArray userList = new JSONArray();
        addGroupMemberParam.put("userList", userList);
        // ????????????????????????
        JSONArray knobUserList = new JSONArray();
        addGroupMemberParam.put("knobUserList", knobUserList);
        // ??????????????????
        List<InterlocutorInfo> noGroupKnobInterlocutorList = interlocutorInfoList.stream()
            .filter(interlocutorInfo -> interlocutorInfo.getKnobNum() == null || interlocutorInfo.getKnobNum() <= 0)
            .collect(Collectors.toList());
        for (InterlocutorInfo interlocutorInfo : noGroupKnobInterlocutorList) {
            //????????????????????????
            JSONObject userInfo = new JSONObject();
            userInfo.put("userId", interlocutorInfo.getInterlocutorId());
            userList.add(userInfo);
            interlocutorInfo.setAssignmentId(assignmentId);
            interlocutorInfo.setCreateDataTime(new Date());
            interlocutorInfo.setCreateDataUsername(SystemHelper.getCurrentUsername());
        }
        // ???????????????????????? ???????????????????????????
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
            //????????????????????????
            JSONObject userInfo = new JSONObject();
            userInfo.put("userId", interlocutorInfo.getInterlocutorId());
            userInfo.put("knobNo", knobNo);
            knobUserList.add(userInfo);
        }
        interlocutorInfoList.addAll(noGroupKnobInterlocutorList);
        return addGroupMemberParam;
    }

    /**
     * ?????????????????????????????????
     * @param interlocutorInfo ??????????????????
     * @return ????????????
     */
    private Integer getNoUseKnobNo(InterlocutorInfo interlocutorInfo) {
        Integer knobNo = 1;
        Integer knobNum = interlocutorInfo.getKnobNum();
        String knobNos = interlocutorInfo.getKnobNos();
        if (StringUtils.isNotBlank(knobNos)) {
            // ?????????????????????????????????
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
     * ?????????????????????
     * @param assignmentName  ????????????
     * @param groupCallNumber ????????????
     * @param intercomGroupId ????????????id
     * @param type            1:????????? 2:????????? 3:?????????
     * @return ??????id
     * @throws Exception Exception
     */
    private String addAssignmentInfoAndPermission(String assignmentName, String groupCallNumber, Long intercomGroupId,
        Integer type) throws Exception {
        UserLdap currentUser = SystemHelper.getCurrentUser();
        String username = currentUser.getUsername();
        String userId = currentUser.getId().toString();
        String orgId = userId.substring(userId.indexOf(",") + 1);
        OrganizationLdap currentOrganization = userService.getOrgByEntryDN(orgId);
        // admin??????????????????admin???????????????,???????????????????????????admin?????????????????????
        if (Objects.equals(username, "admin")) {
            // ?????????????????????????????????????????????
            List<OrganizationLdap> userOwnAuthorityOrganizeInfo = userService.getUserOwnAuthorityOrganizeInfo(orgId);
            currentOrganization = updateOrganizationStructure(currentOrganization, userOwnAuthorityOrganizeInfo);
        }
        // ????????????????????????id
        String currentGroupId = currentOrganization.getUuid();
        // ????????????
        ClusterForm assignmentInfo = new ClusterForm();
        String assignmentId = assignmentInfo.getId();
        assignmentInfo.setName(assignmentName);
        assignmentInfo.setCreateDataUsername(username);
        assignmentInfo.setFlag(type);
        assignmentInfo.setGroupCallNumber(groupCallNumber);
        assignmentInfo.setIntercomGroupId(intercomGroupId);
        assignmentInfo.setTypes((short) 1);
        // ????????????
        clusterDao.addInterlocutorAssignment(assignmentInfo);
        // ????????????????????????
        AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
        assignmentGroupForm.setAssignmentId(assignmentId);
        assignmentGroupForm.setGroupId(currentGroupId);
        assignmentGroupForm.setCreateDataUsername(username);
        // ????????????????????????
        clusterDao.addGroupAssignment(assignmentGroupForm);
        // ??????????????????uuid
        String currentUserId = userService.getUserUuidById(currentUser.getId().toString());
        AssignmentUserForm assignmentUserForm = new AssignmentUserForm(assignmentId, currentUserId);
        clusterDao.addAssignmentUserByBatch(Collections.singletonList(assignmentUserForm));
        return assignmentId;
    }

    /**
     * ????????????
     * @param intercomGroupId ????????????id
     * @return JsonResultBean
     */
    private JsonResultBean deleteGroup(Long intercomGroupId) {
        JSONObject deleteGroupResultJsonObj = talkCallUtil.deleteGroup(intercomGroupId);
        if (deleteGroupResultJsonObj == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "???????????????????????????????????????");
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
        // ?????????????????????
        String assignmentId = addAssignmentInfoAndPermission(assignmentName, null, intercomGroupId, 3);
        if (StringUtils.isNotBlank(interlocutorIds)) {
            // ????????????????????????
            tempAssignmentInsertMonitor(intercomGroupId, interlocutorIds, assignmentId);
        }
        logSearchService.addLog(ipAddress, "?????????" + assignmentName + "????????????", "3", "", "-", "");
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
            log.info("????????????????????????????????????--???????????????????????????id:{},???????????????{}", assignmentId,
                SystemHelper.getCurrentUsername());
            return unbindTaskAssignmentAndMonitor(assignmentId);
        }
        if (Objects.equals(assignmentType, "3")) {
            Long intercomGroupId = Long.valueOf(assignmentId);
            // ????????????????????????
            clusterDao.delTemporaryAssignmentInterlocutorByIntercomGroupId(intercomGroupId, null);
            Cluster assignmentInfo = clusterDao.findAssignmentByIntercomGroupId(intercomGroupId);
            assignmentId = assignmentInfo != null ? assignmentInfo.getId() : null;
            return unbindTemporaryAssignmentAndMonitor(assignmentId);
        }
        return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
    }

    /**
     * ???????????????
     * @param assignmentId ??????id
     * @return JsonResultBean
     * @throws Exception Exception
     */
    private JsonResultBean unbindTaskAssignmentAndMonitor(String assignmentId) throws Exception {
        Cluster assignmentInfo = clusterDao.findAssignmentById(assignmentId);
        // ????????????
        JsonResultBean deleteGroupResultJsonObj = deleteGroup(assignmentInfo.getIntercomGroupId());
        if (!deleteGroupResultJsonObj.isSuccess()) {
            return deleteGroupResultJsonObj;
        }
        // ????????????????????????????????????
        clusterDao.unbindAssignmentAndMonitor(assignmentId);
        // ????????????
        clusterDao.deleteAssignment(assignmentId);
        // ??????????????????????????????
        clusterDao.deleteAssignmentGroupByAssId(assignmentId);
        // ???????????????????????????
        clusterDao.deleteAssignmentUserByAssId(assignmentId);
        // ??????????????????
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
        return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
    }

    /**
     * ??????????????????????????????????????????
     * @param assignmentId ??????id
     * @return JsonResultBean
     */
    private JsonResultBean judgeTaskAssignmentIfJoinMonitor(String assignmentId) {
        // ???????????????????????????????????????????????????
        Cluster assignmentInfo = clusterDao.findAssignmentAndMonitorNumById(assignmentId);
        Integer assignmentMonitorNum = assignmentInfo.getMNum();
        Boolean ifCanAdd = assignmentMonitorNum == null || assignmentMonitorNum < ASSIGNMENT_MXA_MONITOR_NUM;
        return new JsonResultBean(ifCanAdd);
    }

    /**
     * ??????????????????????????????????????????
     * @param assignmentId ??????id
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
        // ????????????????????????
        JsonResultBean assignmentInsertMonitorResult =
            assignmentInsertMonitor(interlocutorIds, assignmentInfo.getIntercomGroupId(), assignmentId);
        if (assignmentInsertMonitorResult.isSuccess()) {
            logSearchService.addLog(ipAddress, "???" + assignmentInfo.getName() + "????????????????????????", "3", "", "-", "");
        }
        return assignmentInsertMonitorResult;
    }

    @Override
    public JsonResultBean insertTemporaryAssignmentRecordLog(Long intercomGroupId, String interlocutorIds,
        String ipAddress) throws Exception {
        Cluster assignmentInfo = clusterDao.findAssignmentByIntercomGroupId(intercomGroupId);
        if (StringUtils.isNotBlank(interlocutorIds)) {
            // ????????????????????????
            tempAssignmentInsertMonitor(intercomGroupId, interlocutorIds, assignmentInfo.getId());
        }
        logSearchService.addLog(ipAddress, "???" + assignmentInfo.getName() + "????????????????????????", "3", "", "-", "");
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
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????");
        }
        Integer deleteGroupMemberListStatusCode = deleteGroupMemberListResultJsonObj.getInteger("result");
        if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), deleteGroupMemberListStatusCode)) {
            return new JsonResultBean(JsonResultBean.FAULT,
                DispatchErrorMessageEnum.getMessage(deleteGroupMemberListStatusCode));
        }
        log.info("?????????????????????????????????????????????--?????????????????????id:{}, ??????id:{},???????????????{}",
            interlocutorInfo.getMonitorId(), assignmentId, SystemHelper.getCurrentUsername());
        clusterDao.deleteVehiclePerBatch(assignmentId, Collections.singletonList(interlocutorInfo.getMonitorId()));
        logSearchService
            .addLog(ipAddress, "???" + interlocutorInfo.getMonitorName() + "???????????????" + assignmentInfo.getName() + "?????????",
                "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public JsonResultBean removeTemporaryAssignmentInterlocutorRecordLog(Long intercomGroupId, Long interlocutorId,
        String ipAddress) throws Exception {
        Cluster assignmentInfo = clusterDao.findAssignmentByIntercomGroupId(intercomGroupId);
        InterlocutorInfo interlocutorInfo = intercomPersonnelDao.getInterlocutorInfoByInterlocutorId(interlocutorId);
        // ????????????????????????
        clusterDao.delTemporaryAssignmentInterlocutorByIntercomGroupId(intercomGroupId, interlocutorId);
        logSearchService
            .addLog(ipAddress, "???" + interlocutorInfo.getMonitorName() + "???????????????" + assignmentInfo.getName() + "?????????",
                "3", "", "-", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private JsonResultBean unbindTemporaryAssignmentAndMonitor(String assignmentId) throws Exception {
        if (StringUtils.isNotBlank(assignmentId)) {
            // ????????????
            clusterDao.deleteAssignment(assignmentId);
            // ??????????????????????????????
            clusterDao.deleteAssignmentGroupByAssId(assignmentId);
            // ???????????????????????????
            clusterDao.deleteAssignmentUserByAssId(assignmentId);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ????????????????????????????????????
     * @param userName ????????????
     * @return JsonResultBean
     * @throws Exception Exception
     */
    @Override
    public void dispatchLoginOut(String userName) throws Exception {
        JSONObject dispatchUserLoginInfo = WebSubscribeManager.getInstance().getDispatchUserLoginInfo(userName);
        // ??????????????????????????????
        WebSubscribeManager.getInstance().removeDispatchUserLoginInfo(userName);
        // ???????????????????????????
        clusterService.delUserOwnTemporaryAssignment(userName);
        if (dispatchUserLoginInfo == null) {
            log.info("????????????????????????????????????????????????");
            return;
        }
        JSONObject data = dispatchUserLoginInfo.getJSONObject("data");
        // ???????????????
        JSONObject clearTempGroupResultJsonObj =
            talkCallUtil.clearTempGroupList(data.getLong("custId"), data.getLong("id"), userName);
        if (clearTempGroupResultJsonObj == null) {
            log.error("??????????????????????????????????????????");
        } else {
            Integer clearTempGroupStatusCode = clearTempGroupResultJsonObj.getInteger("result");
            if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), clearTempGroupStatusCode)) {
                log.error(DispatchErrorMessageEnum.getMessage(clearTempGroupStatusCode));
            }
        }
        //??????????????????
        JSONObject dispatchLoginOutResultJsonObj = talkCallUtil.dispatchLoginOut(userName);
        if (dispatchLoginOutResultJsonObj == null) {
            log.error("?????????????????????????????????????????????");
        } else {
            Integer dispatchLoginOutStatusCode = dispatchLoginOutResultJsonObj.getInteger("result");
            if (!Objects.equals(DispatchErrorMessageEnum.CODE_0.getCode(), dispatchLoginOutStatusCode)) {
                log.error(DispatchErrorMessageEnum.getMessage(dispatchLoginOutStatusCode));
            }
        }
        // ???????????????cookie
        HttpClientUtil.removeSavedCookie(userName);
    }

}
