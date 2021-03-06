package com.zw.talkback.service.baseinfo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import com.zw.platform.domain.basicinfo.form.AssignmentGroupForm;
import com.zw.platform.domain.basicinfo.form.AssignmentUserForm;
import com.zw.platform.domain.core.Group;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserBean;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.event.AssignmentDeleteEvent;
import com.zw.platform.event.AssignmentUpdateEvent;
import com.zw.platform.event.UpdateAssignmentEvent;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.basicinfo.VehicleService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.TreeUtils;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.platform.util.excel.ImportExcel;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.form.AssignmentVehicleForm;
import com.zw.talkback.domain.basicinfo.form.ClusterForm;
import com.zw.talkback.domain.basicinfo.form.ClusterFormExport;
import com.zw.talkback.domain.basicinfo.form.JobInfoData;
import com.zw.talkback.domain.basicinfo.form.Personnel;
import com.zw.talkback.domain.basicinfo.query.AssignmentQuery;
import com.zw.talkback.domain.intercom.ErrorMessageEnum;
import com.zw.talkback.domain.intercom.form.IntercomObjectForm;
import com.zw.talkback.repository.mysql.ClusterDao;
import com.zw.talkback.repository.mysql.IntercomPersonnelDao;
import com.zw.talkback.repository.mysql.JobManagementDao;
import com.zw.talkback.service.baseinfo.ClusterService;
import com.zw.talkback.service.baseinfo.IntercomCallNumberService;
import com.zw.talkback.service.baseinfo.IntercomObjectService;
import com.zw.talkback.service.redis.RedisClusterService;
import com.zw.talkback.util.CallNumberExhaustException;
import com.zw.talkback.util.JsonUtil;
import com.zw.talkback.util.OrganizationUtil;
import com.zw.talkback.util.TalkCallUtil;
import com.zw.talkback.util.common.JsonResultBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.ldap.LdapName;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.zw.talkback.util.JsonUtil.getGroupTree;

/**
 * @author wangying
 */
@Service
@Slf4j
public class ClusterServiceImpl implements ClusterService {

    private ClusterDao clusterDao;

    private UserService userService;

    private VehicleService vehicleService;

    @Autowired
    private RedisClusterService redisClusterService;

    @Autowired
    private JobManagementDao jobManagementDao;

    @Autowired
    private LogSearchServiceImpl logSearchServiceImpl;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private IntercomObjectService intercomObjectService;

    @Autowired
    private IntercomCallNumberService intercomCallNumberService;

    @Autowired
    public void setClusterDao(ClusterDao clusterDao) {
        this.clusterDao = clusterDao;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setVehicleService(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    private static final int ASSIGNMENT_MAX_NUMBER = 400;

    @Autowired
    private IntercomPersonnelDao intercomPersonnelDao;

    @Autowired
    private TalkCallUtil talkCallUtils;

    /**
     * ????????????
     */
    @MethodLog(name = "????????????", description = "????????????")
    @Override
    public List<Cluster> findAssignment(AssignmentQuery query) throws Exception {
        // ?????????????????????????????????????????????
        UserLdap user = SystemHelper.getCurrentUser();
        List<String> groupList = userService.getOrgUuidsByUser(user.getId().toString());
        List<Cluster> list;
        query.setGroupList(groupList);

        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            //??????????????????
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue());
        if ("admin".equals(user.getUsername())) {
            list = clusterDao.findAssignment(query);
        } else {
            String userId = userService.getUserUuidById(user.getId().toString());
            list = clusterDao.listAssignment(userId, query);
        }
        // ??????result??????groupId?????????groupName???list??????????????????
        userService.setGroupNameByGroupId(list);
        return list;
    }

    public Map<String, String> findGroupName(List<String> brandList) {
        Map<String, String> map = new HashMap<>(10);
        List<Cluster> list = clusterDao.findGroupNameByBrand(brandList);
        // ??????result??????groupId?????????groupName???list??????????????????
        userService.setGroupNameByGroupId(list);
        for (Cluster cluster : list) {
            //VehicleId ??????????????????????????????ID
            map.put(cluster.getVehicleId(), cluster.getGroupName());
        }
        return map;
    }

    /**
     * ??????user???????????????
     */
    @Override
    public List<Cluster> findUserAssignment(String userId, List<String> groupList) throws Exception {
        List<Cluster> list = new ArrayList<>();
        if (StringUtils.isNotBlank(userId) && CollectionUtils.isNotEmpty(groupList)) {
            
            list = clusterDao.findUserAssignment(userId, groupList);
        }
        return list;
    }

    /**
     * ??????user????????????????????????
     */
    @Override
    public List<Cluster> findUserAssignmentNum(String userId, List<String> groupList, String monitorType,
        String deviceType) throws Exception {
        List<Cluster> list = new ArrayList<>();
        if (userId != null && !"".equals(userId) && groupList != null && groupList.size() > 0) {
            
            list = clusterDao.findUserAssignment(userId, groupList);
            List<Cluster> countList = clusterDao.findAssignmentNum(list, monitorType, deviceType);
            Map<String, Integer> map = new HashMap<>();
            countList.forEach(c -> map.put(c.getId(), c.getMNum()));
            // list.forEach(c -> c.setMNum(map.get(c.getId())));
            for (Cluster assign : list) {
                Integer count = map.get(assign.getId());
                assign.setMNum(count == null ? Integer.valueOf(0) : count);
            }
        }
        return list;
    }

    /**
     * ????????????
     */
    @Override
    public boolean addAssignment(ClusterForm form) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
        form.setCreateDataTime(new Date()); // ????????????
        return clusterDao.addAssignment(form);
    }

    /**
     * ??????????????????????????????
     */
    @Override
    public boolean addAssignmentGroup(AssignmentGroupForm form) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername()); // ?????????
        form.setCreateDataTime(new Date()); // ????????????
        return clusterDao.addGroupAssignment(form);
    }

    /**
     * ?????????????????????????????????
     */
    @Override
    public boolean assignmentToSuperior(List<String> assignmentIdList, String uuid, HttpSession session)
        throws Exception {
        if (assignmentIdList != null && !assignmentIdList.isEmpty() && uuid != null && !"".equals(uuid)) {
            OrganizationLdap organization = userService.getOrgByUuid(uuid);
            String groupId = "";
            if (organization != null && organization.getId() != null) {
                groupId = organization.getId().toString();
            }
            if (StringUtils.isNotBlank(groupId)) {
                List<AssignmentUserForm> assUserList = new ArrayList<>();
                List<String> parentUserAdmin = new ArrayList<>(); // ??????????????????????????????????????????UUID
                List<String> adminUserNames = new ArrayList<>(); // ???????????????????????????????????????????????????
                // ??????????????????uuid
                String currUserUuid = userService.getUserUuidById(SystemHelper.getCurrentUser().getId().toString());
                parentUserAdmin.add(currUserUuid);
                adminUserNames.add(SystemHelper.getCurrentUsername());
                List<OrganizationLdap> validOrg = new ArrayList<>(); // ???????????????????????????list
                // ?????????????????????????????????
                List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization"); // ???????????? ????????????
                // ???????????????????????????????????????list
                getParentOrg(allOrg, groupId, validOrg);
                // ???????????????????????????????????????????????????
                List<LdapName> memberNameList = userService.getMemberNameListByRoleCn("POWER_USER");
                memberNameList.addAll(userService.getMemberNameListByRoleCn("ROLE_ADMIN"));
                // ???????????????????????????
                for (OrganizationLdap org : validOrg) {
                    // ????????????????????????
                    List<UserBean> orgUserList = userService.getUserList(null, org.getId().toString(), false);
                    if (orgUserList == null || orgUserList.isEmpty()) {
                        continue;
                    }
                    for (UserBean user : orgUserList) {
                        String userUuid = user.getUuid();
                        // ??????????????????????????????
                        List<Group> roles;
                        LdapName roleName = LdapUtils
                            .newLdapName(user.getId().toString() + "," + userService.getBaseLdapPath().toString());
                        if (memberNameList.contains(roleName)) {
                            parentUserAdmin.add(userUuid);
                            adminUserNames.add(user.getUsername());
                            continue;
                        }
                        // roles = (List<Group>) userService.findByMember(roleName);
                        // if (roles == null || roles.isEmpty()) {
                        //     continue;
                        // }
                        // for (Group role : roles) {
                        //     // ??????????????????????????????????????????????????????????????????????????????
                        //     boolean isAdmin =
                        // "ROLE_ADMIN".equals(role.getName()) || "POWER_USER".equals(role.getName()) && !userUuid
                        //             .equals(currUserUuid);
                        //     if (isAdmin) {
                        //         parentUserAdmin.add(userUuid);
                        //         adminUserNames.add(user.getUsername());
                        //         break;
                        //     }
                        // }
                    }
                }
                if (session != null) {
                    session.setAttribute("CONFIG_IMPORT_PROGRESS", 40);
                }
                for (String assignmentId : assignmentIdList) {
                    // ?????????????????????????????????????????????
                    for (String userId : parentUserAdmin) {
                        assUserList.add(new AssignmentUserForm(assignmentId, userId));
                    }
                }
                // ????????????
                boolean result = clusterDao.addAssignmentUserByBatch(assUserList);
                if (session != null) {
                    session.setAttribute("CONFIG_IMPORT_PROGRESS", 60);
                }
                redisClusterService.addUserAssignments(adminUserNames, assignmentIdList, session);
                if (session != null) {
                    session.setAttribute("CONFIG_IMPORT_PROGRESS", 90);
                }
                return result;
            }
        }
        return false;
    }

    /**
     * ???????????????????????????????????????????????????
     * @param userId     ??????id
     * @param oldGroupDn ???????????????Dn
     * @param newGroupDn ???????????????Dn
     */
    @Override
    public void updateUserGroup(String userId, String oldGroupDn, String newGroupDn) throws Exception {
        String userUuid = userService.getUserByEntryDn(userId).getUuid();
        List<OrganizationLdap> oldOrgList = userService.getOrgChild(oldGroupDn);
        List<OrganizationLdap> newOrgList = userService.getOrgChild(newGroupDn);
        List<Cluster> oldClusters = listAssignmentsByGroupList(userUuid, oldOrgList);
        List<Cluster> newClusters = listAssignmentsByGroupList(userUuid, newOrgList);

        List<String> delList =
            oldClusters.stream().filter(p -> !newClusters.contains(p)).map(Cluster::getId).collect(Collectors.toList());
        List<String> addList = new ArrayList<>();

        updateUserAssignments(userId, delList, addList);
        redisClusterService.updateAssignmentsByUserID(userId, delList, addList);
    }

    private List<Cluster> listAssignmentsByGroupList(String userId, List<OrganizationLdap> groupList) {
        List<String> orgIdList = groupList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        return clusterDao.findUserAssignment(userId, orgIdList);
    }

    @Override
    public void updateUserAssignments(String userId, List<String> delList, List<String> addList) throws Exception {
        userId = userService.getUserByEntryDn(userId).getUuid();
        if (delList.size() > 0) {
            vehicleService.deleteUserAssByUserAndAssign(userId, delList);
        }
        List<AssignmentUserForm> userAssignmentForm = new ArrayList<>();
        if (addList.size() > 0) {
            for (String assignmentId : addList) {
                userAssignmentForm.add(new AssignmentUserForm(assignmentId, userId));
            }
            clusterDao.addAssignmentUserByBatch(userAssignmentForm);
        }
    }

    /**
     * ???????????????????????????????????????
     * @param allList    ????????????
     * @param id         ????????????id
     * @param returnList ??????list
     * @author wangying
     */
    @Override
    public void getParentOrg(List<OrganizationLdap> allList, String id, List<OrganizationLdap> returnList) {
        if (allList != null && allList.size() > 0) {
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

    /**
     * ?????????????????????
     */
    @Override
    public JsonResultBean addAssignmentAndPermission(ClusterForm form, AssignmentGroupForm assGroupform,
        String ipAddress, boolean flag) throws Exception {
        List<String> assignmentIdList = new ArrayList<>();
        String assignmentId = form.getId(); // ??????id
        String groupId = assGroupform.getGroupId(); // ????????????id
        String groupCallNumber = "";
        try {
            groupCallNumber = intercomCallNumberService.updateAndReturnGroupCallNumber();
        } catch (CallNumberExhaustException c) {
            // ??????????????????
            return new JsonResultBean(JsonResultBean.FAULT, c.getMessage());
        }
        form.setGroupCallNumber(groupCallNumber);
        // ????????????????????????
        JSONObject callBack = talkCallUtils.addAssignment(form);
        if (callBack == null) {
            intercomCallNumberService.updateAndRecycleGroupCallNumber(groupCallNumber);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (callBack.getInteger("result") != 0) {
            String errorMessage = callBack.getString("message");
            intercomCallNumberService.updateAndRecycleGroupCallNumber(groupCallNumber);
            return new JsonResultBean(JsonResultBean.FAULT, errorMessage);
        }
        Long intercomGroupId = callBack.getJSONObject("data").getLong("groupId");

        form.setIntercomGroupId(intercomGroupId);

        Integer soundRecording = form.getSoundRecording();
        String currentUsername = SystemHelper.getCurrentUsername();
        // ????????????
        if ("admin".equals(currentUsername) && soundRecording != null && soundRecording == 1) {
            Cluster cluster = new Cluster();
            cluster.setIntercomGroupId(form.getIntercomGroupId());
            cluster.setSoundRecording(soundRecording);
            JSONObject object = talkCallUtils.setRecordStatus(cluster);
            if (object == null || object.getInteger("result") != 0) {
                // ??????????????????  ?????????????????????
                form.setSoundRecording(0);
            }
        }

        if (flag == true) {
            // ????????????
            addAssignment(form);
            // ??????????????????????????????
            addAssignmentGroup(assGroupform);
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            assignmentIdList.add(assignmentId);
            assignmentToSuperior(assignmentIdList, groupId, null);
            String msg = "???????????????" + form.getName() + "( @" + userService.getOrgByUuid(groupId).getName() + " )";
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        } else {
            clusterDao.updateCluster(form);

            assignmentIdList.add(assignmentId);
            assignmentToSuperior(assignmentIdList, groupId, null);
            String msg = "???????????????" + form.getName() + "( @" + userService.getOrgByUuid(groupId).getName() + " )";
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");

        }

        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * ?????????????????????(??????)
     */
    @Override
    public JSONArray getAssignmentTree() throws Exception {
        // ?????????????????????????????????????????????
        JSONArray result = new JSONArray();
        // ???????????????????????????id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ?????????????????????????????????????????????
        String orgId = userService.getOrgIdByUser();
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // ?????????????????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<String>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        // ??????
        List<Cluster> clusterList = findUserAssignment(userService.getUserUuidById(userId), userOrgListId);
        putAssignmentTree(clusterList, result, "multiple", false);
        result.addAll(getGroupTree(orgs, "multiple", false));
        return result;
    }

    /**
     * ?????????????????????(??????)
     */
    @Override
    public JSONArray getEditAssignmentTree(String assignUserId) throws Exception {
        JSONArray result = redisClusterService.getAssignmentByUserID(assignUserId);

        // ?????????????????????????????????????????????
        List<OrganizationLdap> organizations = userService.getOrgChild(userService.getOrgIdByUserId(assignUserId));
        result.addAll(getGroupTree(organizations, "multiple", false));
        if (!result.isEmpty()) {
            return result;
        }
        // ????????????id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ????????????
        List<String> groupList = new ArrayList<>();
        if (organizations != null && organizations.size() > 0) {
            for (OrganizationLdap o : organizations) {
                groupList.add(o.getUuid());
            }
        }
        // ????????????(?????????????????????????????????????????????????????????????????????????????????)
        List<Cluster> clusterList = findUserAssignment(userService.getUserUuidById(userId), groupList);
        // ????????????????????????
        List<String> assignGroupList = userService.getOrgUuidsByUser(assignUserId); // ??????????????????user????????????????????????
        List<Cluster> checkClusterList = findUserAssignment(userService.getUserUuidById(assignUserId), assignGroupList);
        // ???????????????
        if (clusterList == null || clusterList.isEmpty()) {
            return result;
        }
        List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization"); // ???????????? ????????????
        for (OrganizationLdap organization : allOrg) {
            for (Cluster ass : clusterList) {
                if (organization.getUuid().equals(ass.getGroupId())) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", ass.getId());
                    obj.put("pId", organization.getId().toString());
                    obj.put("name", ass.getName());
                    obj.put("type", "assignment");
                    obj.put("iconSkin", "assignmentSkin");
                    if (checkClusterList.contains(ass)) {
                        obj.put("checked", true);
                    }
                    result.add(obj);
                }
            }
        }
        return result;
    }

    /**
     * ???????????????????????????
     * @param assignmentID ??????ID
     * @return ?????????????????????JSON?????????
     */
    @Override
    public String getAssignMonitorUserTree(String assignmentID) throws Exception {
        Cluster assign = findAssignmentById(assignmentID);
        // ?????????????????????????????????????????????
        List<OrganizationLdap> validOrg = new ArrayList<>(); // ???????????????????????????list
        // ?????????????????????????????????????????????
        String orgId = userService.getOrgIdByUser();
        List<OrganizationLdap> allOrg = userService.getOrgChild(orgId); // ???????????? ????????????
        // ??????????????????????????????
        String uuid = assign.getGroupId();
        OrganizationLdap organization = userService.getOrgByUuid(uuid);
        if (organization != null && organization.getId() != null) {
            String assignGroupId = organization.getId().toString();
            getParentOrg(allOrg, assignGroupId, validOrg);
        }

        List<UserBean> users = new ArrayList<>(); // ???????????????????????????
        List<String> ulist = new ArrayList<>(); // ?????????????????????id??????
        if (users.isEmpty() && !validOrg.isEmpty()) {
            for (OrganizationLdap group : validOrg) {
                // ????????????????????????
                // List<UserBean> userList = userService.getUserList(null, group.getId().toString(), false);
                List<UserBean> userList = userService.listUserByOrgId(group.getId().toString());
                if (userList != null && !userList.isEmpty()) {
                    for (UserBean user : userList) {
                        user.setGroupId(group.getId().toString());
                        ulist.add(user.getUuid());
                    }
                    users.addAll(userList);
                }
            }
        }
        users.sort(Comparator.comparing(UserBean::getCreateTimestamp).reversed());

        // ???????????????????????????
        List<String> assignedUsers = new ArrayList<>();
        if (assignmentID != null && !"".equals(assignmentID)) {
            assignedUsers = vehicleService.findUserAssignByAid(assignmentID, ulist);
        }

        // ?????????????????????
        JSONArray result = new JSONArray();
        if (!users.isEmpty()) {
            for (UserBean user : users) {
                String urId = user.getId().toString();
                String userUuid = user.getUuid();
                // ????????????id(????????????id????????????????????????)
                String userPid = userService.getOrgIdByUserId(urId);

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
                if (!assignedUsers.isEmpty() && assignedUsers.contains(userUuid)) {
                    userObj.put("checked", true);
                }
                result.add(userObj);
            }
        }
        // ???????????????
        result.addAll(getGroupTree(validOrg, "multiple", false));
        return result.toJSONString();
    }

    @Override
    public Cluster findAssignmentById(String id) throws Exception {
        if (id != null && !"".equals(id)) {
            List<Cluster> list = new ArrayList<>();
            Cluster ass = clusterDao.findAssignmentById(id);
            if (ass != null) {
                list.add(ass);
                // ???groupId?????????groupName???list??????????????????
                userService.setGroupNameByGroupId(list);
                return list.get(0);
            }
        }
        return null;
    }

    @Override
    public Cluster findAssignmentByIdNum(String id) throws Exception {
        if (id != null && !"".equals(id)) {
            List<Cluster> list = new ArrayList<>();
            Cluster ass = clusterDao.findAssignmentByIdNum(id);
            if (ass != null) {
                list.add(ass);
                // ???groupId?????????groupName???list??????????????????
                userService.setGroupNameByGroupId(list);
                return list.get(0);
            }
        }
        return null;
    }

    @Override
    public JsonResultBean updateAssignment(ClusterForm form, String ipAddress) throws Exception {
        String id = form.getId();
        String name = form.getName();
        Cluster assign = clusterDao.findAssignmentById(id);
        JSONObject callBack = talkCallUtils.updateAssignment(assign, form);
        if (callBack == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        if (callBack.getInteger("result") != 0) {
            String errorMessage = callBack.getString("message");
            return new JsonResultBean(JsonResultBean.FAULT, errorMessage);
        }
        Integer soundRecording = form.getSoundRecording();
        String currentUsername = SystemHelper.getCurrentUsername();
        // ????????????
        if ("admin".equals(currentUsername) && soundRecording != null && !soundRecording
            .equals(assign.getSoundRecording())) {
            Cluster cluster = new Cluster();
            cluster.setIntercomGroupId(assign.getIntercomGroupId());
            cluster.setSoundRecording(soundRecording);
            JSONObject object = talkCallUtils.setRecordStatus(cluster);
            if (object == null || object.getInteger("result") != 0) {
                // ??????????????????  ???????????????????????????
                form.setSoundRecording(assign.getSoundRecording());
            }
        }
        String groupId = findAssignmentGroupId(id);
        List<Assignment> assignments = assignmentService.findByNameForOneOrg(name, form.getGroupId());
        if (CollectionUtils.isNotEmpty(assignments)) {
            for (Assignment assignment : assignments) {
                if ((!Objects.equals(id, assignment.getId())) && Objects.equals(assignment.getName(), name)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
                }
            }
        }

        boolean result = clusterDao.updateAssignment(form);
        // ?????????????????????,????????????
        if (result) {
            // ?????????????????????????????????????????????????????????????????????
            redisClusterService.updateAssignment(id, assign.getName(), name);
            StringBuilder message = new StringBuilder();
            if (!name.equals(assign.getName())) {
                message.append("???????????? : ").append(assign.getName()).append(" ( @")
                    .append(userService.getOrgByUuid(groupId).getName()).append(" ????????? : ").append(name).append(" ( @")
                    .append(userService.getOrgByUuid(groupId).getName()).append(" )").append(")");
            } else {
                message.append("???????????? : ").append(assign.getName()).append(" ( @")
                    .append(userService.getOrgByUuid(groupId).getName()).append(")");
            }
            logSearchServiceImpl.addLog(ipAddress, message.toString(), "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @EventListener
    public void listenerUpdateAssignmentEvent(UpdateAssignmentEvent event) {
        AssignmentForm form = event.getForm();
        ClusterForm clusterForm = JSON.parseObject(JSON.toJSONString(form), new TypeReference<ClusterForm>() {
        });
        Cluster assign = clusterDao.findAssignmentById(form.getId());
        talkCallUtils.updateAssignment(assign, clusterForm);

    }

    @Override
    public List<VehicleInfo> findVehicleByAssignmentId(String assignmentId) throws Exception {
        if (assignmentId != null && !"".equals(assignmentId)) {
            return clusterDao.findVehicleByAssignmentId(assignmentId);
        }
        return null;
    }

    @Override
    public List<VehicleInfo> findMonitorByAssignmentId(String assignmentId) throws Exception {
        if (assignmentId != null && !"".equals(assignmentId)) {
            return clusterDao.findMonitorByAssignmentId(assignmentId);
        }
        return null;
    }

    @Override
    public JsonResultBean deleteAssignment(String id, String ipAddress) throws Exception {
        if (id != null && !"".equals(id)) {
            List<Cluster> clusters = clusterDao.findAssignmentByBatch(Collections.singletonList(id));
            if (CollectionUtils.isEmpty(clusters)) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            Cluster cluster = clusters.get(0);
            JSONObject callBack = talkCallUtils.delAssignment(cluster);
            if (callBack == null) {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            if (callBack.getInteger("result") != 0) {
                String errorMessage = callBack.getString("message");
                return new JsonResultBean(JsonResultBean.FAULT, errorMessage);
            }
            // ????????????
            clusterDao.deleteAssignment(id);
            // ????????????id
            intercomCallNumberService.updateAndRecycleGroupCallNumber(cluster.getGroupCallNumber());
            // ??????????????????????????????
            clusterDao.deleteAssignmentGroupByAssId(id);
            // ???????????????????????????
            clusterDao.deleteAssignmentUserByAssId(id);
            // ????????????
            redisClusterService.deleteAssignmentsCache(clusters);
            String groupId = findAssignmentGroupId(id);
            String msg = "???????????? ???" + cluster.getName() + "( @" + userService.getOrgByUuid(groupId).getName() + " )";
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ??????????????????????????????????????????
     * @param event
     * @throws Exception
     */
    @EventListener
    public void listenerAssignmentDelete(AssignmentDeleteEvent event) throws Exception {
        String id = event.getId();
        if (id != null && !"".equals(id)) {
            List<Cluster> clusters = clusterDao.findAssignmentByBatchListener(Collections.singletonList(id));
            if (clusters.size() > 0) {
                Cluster cluster = clusters.get(0);
                talkCallUtils.delAssignment(cluster);
                intercomCallNumberService.updateAndRecycleGroupCallNumber(cluster.getGroupCallNumber());
            }
        }
    }

    /**
     * ????????????????????????????????????????????????
     * @param event
     * @throws Exception
     */
    @EventListener
    public void listenerAssignmentDeletes(AssignmentDeleteEvent event) throws Exception {
        List<String> ids = event.getIds();
        if (ids == null || ids.size() == 0) {
            return;
        }
        for (String id : ids) {
            List<Cluster> clusters = clusterDao.findAssignmentByBatchListener(Collections.singletonList(id));
            if (CollectionUtils.isEmpty(clusters)) {
                continue;
            }
            Cluster cluster = clusters.get(0);
            talkCallUtils.delAssignment(cluster);
            // ????????????id
            intercomCallNumberService.updateAndRecycleGroupCallNumber(cluster.getGroupCallNumber());

        }
    }

    @Override
    public JsonResultBean deleteAssignmentByBatch(List<String> ids, String ipAddress) throws Exception {
        boolean flagAssign = false; // ??????????????????????????????
        StringBuilder errorMessage = new StringBuilder();
        StringBuilder msg = new StringBuilder(); // ??????????????????
        for (String id : ids) {
            List<Cluster> clusters = clusterDao.findAssignmentByBatch(Collections.singletonList(id));
            if (CollectionUtils.isEmpty(clusters)) {
                continue;
            }
            Cluster cluster = clusters.get(0);
            JSONObject callBack = talkCallUtils.delAssignment(cluster);
            if (callBack == null) {
                errorMessage.append("???????????????" + cluster.getName() + "??????,????????????????????????" + "</br>");
                continue;
            }
            if (callBack.getInteger("result") != 0) {
                String message = callBack.getString("message");
                errorMessage.append("???????????????" + cluster.getName() + "??????," + message + "</br>");
                continue;
            }
            // ????????????
            clusterDao.deleteAssignment(id);
            // ????????????id
            intercomCallNumberService.updateAndRecycleGroupCallNumber(cluster.getGroupCallNumber());
            // ??????????????????????????????
            clusterDao.deleteAssignmentGroupByAssId(id);
            // ???????????????????????????
            clusterDao.deleteAssignmentUserByAssId(id);
            // ????????????
            redisClusterService.deleteAssignmentsCache(clusters);
            String groupId = findAssignmentGroupId(id);
            msg.append("???????????? ???" + cluster.getName() + "( @" + userService.getOrgByUuid(groupId).getName() + " )");
        }
        if (msg.length() > 0) {
            logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "??????????????????");
        }
        if (errorMessage.length() > 0) {
            return new JsonResultBean(errorMessage.toString());
        } else {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }

        /*List<Assignment> assignments = assignmentDao.findAssignmentByBatch(ids); // ????????????????????????
        StringBuilder msg = new StringBuilder(); // ??????????????????
        if (assignments != null && assignments.size() > 0) {
            for (Assignment assignment : assignments) {
                if (assignment.getGroupId() != null) {
                    msg.append("???????????? : ").append(assignment.getName()).append(" ( @")
                        .append(userService.getOrgByUuid(assignment.getGroupId()).getName()).append(" ) <br/>");
                }
            }
            // ??????????????????????????????(??????)
            boolean flagGroup = assignmentDao.deleteAssignmentGroupByAssIdByBatch(ids);// ????????????????????????????????????????????????
            // ???????????????????????????(??????)
            boolean flagPeople = assignmentDao.deleteAssignmentUserByAssIdByBatch(ids); // ?????????????????????????????????????????????
            if (flagGroup && flagPeople) { // ????????????????????????????????????
                // ????????????(??????)
                flagAssign = assignmentDao.deleteAssignmentByBatch(ids);
            }
        }
        if (flagAssign) { // ??????????????????
            // ????????????
            redisAssignService.deleteAssignmentsCache(assignments);
            // ????????????
            String monitoringOperation = "??????????????????";
            logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", monitoringOperation);
            return true;
        } else {
            return false;
        }*/

    }

    @Override
    public boolean addAssignmentVehicle(AssignmentVehicleForm assignmentVehicleForm) throws Exception {
        return clusterDao.addAssignmentVehicle(assignmentVehicleForm);
    }

    @Override
    public boolean addAssignVehicleList(Collection<AssignmentVehicleForm> formList) {
        return formList.isEmpty() || clusterDao.addAssignVehicleList(formList);
    }

    @Override
    public List<Cluster> findAssignmentByVehicleId(String vehicleId) throws Exception {
        // ??????id
        String userId = "uid=admin,ou=organization"; // SystemHelper.getCurrentUser().getId().toString();
        // ???????????????????????????
        List<String> groupList = userService.getOrgIdsByUser(userId);
        if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(userId) && groupList != null
            && groupList.size() > 0) {
            return clusterDao.findAssignmentByVehicleId(vehicleId, userId, groupList);
        }
        return null;
    }


    @Override
    public List<Cluster> findByNameForOneOrg(String name, String groupId) throws Exception {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(groupId)) {
            return clusterDao.findByNameForOneOrg(name, groupId);
        }
        return null;
    }

    @Override
    public List<Cluster> findByNameForOne(String name) throws Exception {
        if (StringUtils.isNotBlank(name)) {
            return clusterDao.findByNameForOne(name);
        }
        return null;
    }

    @Override
    public List<Cluster> findOneOrgAssiForNameRep(String id, String name, String groupId) throws Exception {
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(groupId)) {
            return clusterDao.findOneOrgAssiForNameRep(id, name, groupId);
        }
        return null;
    }

    @Override
    public Map importAssignment(MultipartFile multipartFile, String ipAddress, HttpSession session) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("flag", 0);
        StringBuilder errorMsg = new StringBuilder();
        String resultInfo = "";
        // ???????????????
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel ????????? list
        List<ClusterForm> list = importExcel.getDataList(ClusterForm.class);
        String temp;
        List<ClusterForm> importList = new ArrayList<>();
        List<AssignmentGroupForm> assignmentGroupList = new ArrayList<>();
        List<String> assignmentIdList = new ArrayList<>();

        // ??????????????????????????????
        Set<String> groupIdList = new HashSet<>();
        Map<String, ClusterForm> groupAssignments = new HashMap<>(16);
        StringBuilder msg = new StringBuilder(); // ?????????????????????
        // ?????????????????????
        if (list != null && list.size() > 0) {
            getGroupAssignment(list, groupAssignments);

            // ????????????id???????????????
            for (int i = 0; i < list.size(); i++) {
                ClusterForm assign = list.get(i);
                //???????????????????????????
                if (assign.getName() != null) {
                    assign.setName(assign.getName().trim());
                }
                // ??????????????????
                if (StringUtils.isBlank(assign.getName())) {
                    //resultMap.put("flag", 0);
                    //errorMsg.append("???").append(i + 1).append("???????????????????????????<br/>");
                    continue;
                }

                // ?????????????????????
                if ("REPEAT".equals(assign.getName())) {
                    continue;
                }
                for (int j = list.size() - 1; j > i; j--) {
                    ClusterForm clusterForm = list.get(j);
                    if (StringUtils.isBlank(clusterForm.getName()) || StringUtils.isBlank(assign.getName())) {
                        continue;
                    }
                    // ???????????????, ??????????????????, ???????????????
                    String inGroupName = clusterForm.getGroupName();
                    String outGroupName = assign.getGroupName();
                    if (inGroupName.equals(outGroupName) && clusterForm.getName().equals(assign.getName())) {
                        temp = assign.getName();
                        errorMsg.append("???").append(i + 1).append("?????????????????????").append(j + 1).append("?????????????????????")
                            .append(temp).append("<br/>");
                        clusterForm.setName("REPEAT");
                    }
                }
                session.setAttribute("CONFIG_IMPORT_PROGRESS", i * 10 / list.size());
            }
            for (int i = 0; i < list.size(); i++) {
                ClusterForm assign = list.get(i);
                if ("REPEAT".equals(assign.getName())) {
                    continue;
                }
                // ??????????????????
                if (StringUtils.isBlank(assign.getName())) {
                    resultMap.put("flag", 0);
                    errorMsg.append("???").append(i + 1).append("???????????????????????????<br/>");
                    continue;
                }
                String groupName = assign.getGroupName();

                // ????????????
                if (StringUtils.isEmpty(groupName)) {
                    resultMap.put("flag", 0);
                    errorMsg.append("???").append(i + 1).append("???????????????????????????<br/>");
                    continue;
                }

                ClusterForm clusterForm = groupAssignments.get(groupName);
                // ??????????????????????????????
                if (clusterForm == null) {
                    resultMap.put("flag", 0);
                    errorMsg.append("???").append(i + 1).append("?????????????????????????????????/?????????????????????<br/>");
                    continue;
                }
                String groupId = clusterForm.getGroupId();
                // ?????????????????????????????????
                List<Cluster> assignByGroupId = findByNameForOneOrg(assign.getName(), groupId);
                if (assignByGroupId != null && !assignByGroupId.isEmpty()) {
                    resultMap.put("flag", 0);
                    errorMsg.append("???").append(i + 1).append("??????????????????????????????").append(assign.getName())
                        .append("??????????????????????????????<br/>");
                    continue;
                }

                // ????????????????????????
                if (Converter.toBlank(assign.getName()).length() > 30) {
                    resultMap.put("flag", 0);
                    errorMsg.append("???").append(i + 1).append("????????????????????????????????????30???<br/>");
                    continue;
                }
                // ???????????????????????????????????????????????????
                if (Converter.toBlank(assign.getContacts()).length() > 20) {
                    assign.setContacts("");
                }
                if (Converter.toBlank(assign.getDescription()).length() > 50) {
                    assign.setDescription("");
                }
                // ??????????????????
                if (StringUtils.isNotBlank(assign.getTelephone())) {
                    // ?????? Pattern ??????
                    Pattern patternMoble = Pattern.compile("^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\\d{8})$");
                    Pattern patternTel = Pattern.compile("^(\\d{3,4}-?)?\\d{7,9}$");
                    // ???????????? matcher ??????
                    Matcher matcherMoble = patternMoble.matcher(assign.getTelephone());
                    Matcher matcherTel = patternTel.matcher(assign.getTelephone());
                    if (!(matcherTel.matches() || (assign.getTelephone().length() == 11 && matcherMoble.matches()))) {
                        assign.setTelephone("");
                    }
                }

                // ??????????????????????????????
                Integer assignmentNumber = clusterForm.getAssignmentNumber();
                if (Objects.nonNull(assignmentNumber)) {
                    if (assignmentNumber >= ASSIGNMENT_MAX_NUMBER) {
                        resultMap.put("flag", 0);
                        errorMsg.append("???").append(i + 1).append("???????????????").append(groupName).append("??????????????????????????????500??????");
                        continue;
                    }
                    clusterForm.setAssignmentNumber(++assignmentNumber);
                    groupAssignments.put(groupName, clusterForm);
                } else {
                    clusterForm.setAssignmentNumber(1);
                    groupAssignments.put(groupName, clusterForm);
                }

                groupIdList.add(groupId);
                assign.setCreateDataUsername(SystemHelper.getCurrentUsername());
                // ?????????
                assign.setCreateDataTime(new Date()); // ????????????
                AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
                // ???????????????
                assignmentGroupForm.setAssignmentId(assign.getId());
                assignmentGroupForm.setGroupId(groupId);
                assignmentGroupForm.setCreateDataTime(new Date());
                assignmentGroupForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
                importList.add(assign);
                assignmentGroupList.add(assignmentGroupForm);
                assignmentIdList.add(assign.getId());
                session.setAttribute("CONFIG_IMPORT_PROGRESS", i * 10 / list.size() + 10);
                msg.append("?????? : ").append(assign.getName()).append(" ( @")
                    .append(userService.getOrgByUuid(groupId).getName()).append(" ) <br/>");
            }
        }
        // ??????????????????
        if (!importList.isEmpty() && !assignmentGroupList.isEmpty()) {
            // ???????????????????????????????????????????????????????????????????????????
            boolean flag = clusterDao.addAssignmentByBatch(importList);
            if (flag) {
                flag = clusterDao.addGroupAssignmentByBatch(assignmentGroupList);
            }
            session.setAttribute("CONFIG_IMPORT_PROGRESS", 30);
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                for (String groupId : groupIdList) {
                    assignmentToSuperior(assignmentIdList, groupId, session);
                }
            }

            if (flag && list != null) {
                resultInfo +=
                    "????????????" + importList.size() + "?????????, <br/> ????????????" + (list.size() - importList.size()) + "????????????";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg.toString());
                resultMap.put("resultInfo", resultInfo);
                if (!"".equals(msg.toString())) {
                    String monitoringOperation = "????????????";
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", monitoringOperation);
                }
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "???????????????");
                return resultMap;
            }

        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg.toString());
            resultMap.put("resultInfo", "????????????0????????????");
            return resultMap;
        }
        return resultMap;
    }

    /**
     * ??????????????????
     * @param list             list
     * @param groupAssignments groupAssignments
     */
    public void getGroupAssignment(List<ClusterForm> list, Map<String, ClusterForm> groupAssignments) {
        // ?????????????????????????????????????????????, ?????????????????????????????????????????????, ????????????
        List<OrganizationLdap> organizationLdapList = userService.getOrgChild(userService.getOrgIdByUser());
        List<String> groupIdList =
            organizationLdapList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        // ????????????Excel ????????????????????????, ??????????????????????????????????????????????????????
        Set<String> groupNames = list.stream().map(ClusterForm::getGroupName).collect(Collectors.toSet());
        groupNames.forEach(groupName -> {
            OrganizationLdap organizationLdap = userService.getOrgInfoByName(groupName);
            if (Objects.nonNull(organizationLdap)) {
                String groupId = organizationLdap.getUuid();
                if (groupIdList.contains(groupId)) {
                    ClusterForm clusterForm = clusterDao.getGroupHasAssignmentNumber(groupId);
                    clusterForm.setGroupId(groupId);
                    groupAssignments.put(organizationLdap.getName(), clusterForm);
                }
            }
        });
    }

    @Override
    public List<Cluster> findAssignmentByGroupId(String groupId) {
        if (StringUtils.isNotBlank(groupId)) {
            return clusterDao.findAssignmentByGroupId(groupId);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // ??????
        headList.add("????????????");
        headList.add("????????????");
        headList.add("????????????");
        headList.add("?????????");
        headList.add("????????????");
        headList.add("??????");
        // ????????????
        requiredList.add("????????????");
        requiredList.add("????????????");
        // ????????????????????????
        // ?????????????????????????????????????????????
        List<String> groupNames = userService.getOrgNamesByUser();
        exportList.add("??????1???");
        if (CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        exportList.add("00000");
        exportList.add("??????");
        exportList.add("13658965874");
        exportList.add("??????");

        // ?????????????????????
        Map<String, String[]> selectMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("????????????", groupNameArr);
        }
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// ????????????????????????????????????
        out.close();
        return true;
    }

    @Override
    public List<Cluster> findAssignByGroupIdExpectVehicle(String groupId, String assignmentId) {
        if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(assignmentId)) {
            return clusterDao.findAssignByGroupIdExpectVehicle(groupId, assignmentId);
        }
        return null;
    }

    @Override
    public boolean exportAssignment(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, ClusterForm.class, 1);
        ExportExcel export1 = new ExportExcel(title, ClusterFormExport.class, 1);
        List<ClusterForm> exportList = new ArrayList<>();
        List<ClusterFormExport> exportList1 = new ArrayList<>();

        String currentUsername = SystemHelper.getCurrentUsername();
        // ?????????????????????
        AssignmentQuery query = new AssignmentQuery();
        query.setPage(0L);
        query.setLimit(0L);
        List<Cluster> clusterList = findAssignment(query);
        for (Cluster info : clusterList) {
            ClusterForm form = new ClusterForm();
            Integer soundRecording = info.getSoundRecording();
            if (soundRecording == 0) {
                form.setState("?????????");
            } else {
                form.setState("?????????");
            }
            BeanUtils.copyProperties(info, form);
            exportList.add(form);
        }
        if (currentUsername.equals("admin")) {
            export.setDataList(exportList);
        } else {
            List<Cluster> clusterList1 = findAssignment(query);
            for (Cluster info : clusterList1) {
                ClusterFormExport form1 = new ClusterFormExport();
                BeanUtils.copyProperties(info, form1);
                exportList1.add(form1);
            }
            export1.setDataList(exportList1);
            // ???????????????
            OutputStream out;
            out = response.getOutputStream();
            export1.write(out);
            out.close();
            return true;
        }
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// ????????????????????????????????????
        export1.write(out);
        out.close();
        return true;
    }

    @Override
    public JsonResultBean saveVehiclePer(List<AssignmentVehicleForm> vehiclePerAddList,
        List<AssignmentVehicleForm> vehiclePerDeleteList, String assignmentId, String ipAddress) throws Exception {
        Cluster cluster = findAssignmentById(assignmentId); // ????????????????????????
        if (CollectionUtils.isEmpty(vehiclePerAddList) && CollectionUtils.isEmpty(vehiclePerDeleteList)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        JsonResultBean computeNum = computeNum(vehiclePerAddList, vehiclePerDeleteList);
        if (computeNum != null) {
            return computeNum;
        }
        // ??????????????????
        Set<String> subVehicle = new HashSet<>();
        // ??????
        if (!vehiclePerDeleteList.isEmpty()) {
            Map<String, List<AssignmentVehicleForm>> assignmentDelForms =
                vehiclePerDeleteList.stream().collect(Collectors.groupingBy(AssignmentVehicleForm::getAssignmentId));
            JsonResultBean message = deleteUser(assignmentDelForms, subVehicle);
            if (message != null) {
                return message;
            }
        }
        // ??????
        if (!vehiclePerAddList.isEmpty()) {
            Map<String, List<AssignmentVehicleForm>> assignmentAddForms =
                vehiclePerAddList.stream().collect(Collectors.groupingBy(AssignmentVehicleForm::getAssignmentId));
            JsonResultBean message = addUser(assignmentAddForms, subVehicle);
            if (message != null) {
                return message;
            }
        }

        // ????????????
        redisClusterService.updateVehiclesCache(vehiclePerDeleteList, vehiclePerAddList);
        String msg = "???????????????" + cluster.getName() + " ( @" + userService.getOrgByUuid(cluster.getGroupId()).getName()
            + " ) ??????????????????";
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        // ??????????????????
        WebSubscribeManager.getInstance().updateSubStatus(subVehicle);

        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @EventListener
    public void listenerAssignUpdataEvent(AssignmentUpdateEvent event) throws Exception {
        List<com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm> vehiclePerDeleteForms =
            event.getVehiclePerDeleteList();
        List<com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm> vehiclePerAddList =
            event.getVehiclePerAddList();

        String assignmentId = event.getAssignmentId();
        //????????????id????????????
        Cluster assignmentById = clusterDao.findAssignmentById(assignmentId);

        if (CollectionUtils.isNotEmpty(vehiclePerDeleteForms)) {
            for (com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm
                    vehiclePerDeleteForm : vehiclePerDeleteForms) {
                final RedisKey key = HistoryRedisKeyEnum.INTERCOM_LIST.of(vehiclePerDeleteForm.getVehicleId());
                String s = RedisHelper.getString(key);
                IntercomObjectInfo interObject = JSONObject.parseObject(s, IntercomObjectInfo.class);
                if (StringUtils.isNotBlank(s)) {

                    String[] split = interObject.getAssignmentId().split(",");
                    int length = split.length;
                    if (length == 1 && assignmentById == null) {
                        updatePeopleRedis(assignmentId, interObject, vehiclePerDeleteForm.getVehicleId(), false);
                        String configId = newConfigDao.getConfigIdByVehicleId(vehiclePerDeleteForm.getVehicleId());
                        intercomObjectService
                            .deleteIntercomObject(configId, "", Arrays.asList(vehiclePerDeleteForm.getVehicleId()));
                    } else {
                        //???????????????id???????????????????????????id
                        // if (interObject.getAssignmentId().contains(vehiclePerDeleteForm.getAssignmentId())) {
                        //????????????id???????????????
                        String replace =
                            interObject.getAssignmentId().replace(vehiclePerDeleteForm.getAssignmentId() + ",", "");
                        interObject.setAssignmentId(replace);
                        //}
                        String assignName = clusterDao.findAssignName(vehiclePerDeleteForm.getAssignmentId());

                        String replaceName = interObject.getAssignmentName().replace(assignName + ",", "");
                        interObject.setAssignmentName(replaceName);

                        //??????????????????
                        updatePeopleRedis(vehiclePerDeleteForm.getAssignmentId(), interObject,
                            vehiclePerDeleteForm.getVehicleId(), true);
                        Cluster cluster = clusterDao.findAssignmentById(vehiclePerDeleteForm.getAssignmentId());
                        interObject.setCurrentGroupNum(interObject.getCurrentGroupNum() - 1);
                        final RedisKey redisKey =
                                HistoryRedisKeyEnum.INTERCOM_LIST.of(vehiclePerDeleteForm.getVehicleId());
                        RedisHelper.setString(redisKey, JSON.toJSONString(interObject));
                        List<JSONObject> deleteIntercomId = new ArrayList<>();
                        JSONObject object = new JSONObject();
                        object.put("userId", interObject.getUserId());
                        deleteIntercomId.add(object);
                        talkCallUtils.delAssignmentUser(cluster, deleteIntercomId);

                    }
                }

            }

        }

        if (CollectionUtils.isNotEmpty(vehiclePerAddList)) {
            for (com.zw.platform.domain.infoconfig.form.AssignmentVehicleForm vehiclePerAddForm : vehiclePerAddList) {
                //????????????????????????????????????
                if (assignmentById != null && !"".equals(assignmentById.getId())) {
                    final RedisKey key = HistoryRedisKeyEnum.INTERCOM_LIST.of(vehiclePerAddForm.getVehicleId());
                    String s = RedisHelper.getString(key);
                    IntercomObjectInfo interObject = JSONObject.parseObject(s, IntercomObjectInfo.class);
                    //?????????????????????????????????
                    if (interObject != null) {

                        String assignmentStr = new StringBuffer().append(interObject.getAssignmentId()).append(",")
                            .append(vehiclePerAddForm.getAssignmentId()).toString();

                        interObject.setAssignmentId(assignmentStr);

                        String assignmentNameStr =
                            new StringBuffer().append(interObject.getAssignmentName()).append(",")
                                .append(vehiclePerAddForm.getAssignmentName()).toString();
                        interObject.setAssignmentName(assignmentNameStr);
                        //??????????????????
                        updatePeopleRedis(vehiclePerAddForm.getAssignmentId(), interObject,
                            vehiclePerAddForm.getVehicleId(), true);
                        updateOrAddAssignsKnobs(interObject);

                        boolean userIdIsNull = Objects.isNull(interObject.getUserId());
                        if (!userIdIsNull) {
                            Cluster cluster = clusterDao.findAssignmentById(assignmentId);
                            List<JSONObject> addIntercomId = new ArrayList<>();
                            List<JSONObject> knobUserList = new ArrayList<>();
                            Integer knobNum = intercomPersonnelDao.findKnobNum(vehiclePerAddForm.getVehicleId());
                            Integer knobNo = 1;
                            if (knobNum != null && knobNum > 0) {
                                List<Integer> vehicleKnobNumbers =
                                    intercomPersonnelDao.getVehicleKnobNumbers(vehiclePerAddForm.getVehicleId());
                                if (CollectionUtils.isNotEmpty(vehicleKnobNumbers)) {
                                    for (int i = 1; i <= knobNum; i++) {
                                        if (!vehicleKnobNumbers.contains(i)) {
                                            knobNo = i;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (knobNum != null && knobNum > 0) {
                                JSONObject object = new JSONObject();
                                object.put("userId", interObject.getUserId());
                                object.put("knobNo", knobNo);
                                knobUserList.add(object);
                            } else {
                                JSONObject object = new JSONObject();
                                object.put("userId", interObject.getUserId());
                                addIntercomId.add(object);
                            }
                            talkCallUtils.addAssignmentUser(cluster, addIntercomId, knobUserList);
                        }
                        Integer groupNum = interObject.getCurrentGroupNum();
                        interObject.setCurrentGroupNum(groupNum + 1);

                        final RedisKey redisKey =
                                HistoryRedisKeyEnum.INTERCOM_LIST.of(vehiclePerAddForm.getVehicleId());
                        RedisHelper.setString(redisKey, JSON.toJSONString(interObject));
                    }
                }
            }
        }

    }

    private void updatePeopleRedis(String assignmentId, IntercomObjectInfo interObject, String peopleId,
                                   // flag????????????
                                   boolean flag) {
        Set<String> assignmentIds = new HashSet<>();
        Set<String> assignNames = new HashSet<>();
        assignmentIds.add(assignmentId);
        assignmentIds.add(interObject.getAssignmentId());
        String assignName = clusterDao.findAssignName(assignmentId);
        assignNames.add(assignName);
        assignNames.add(interObject.getAssignmentName());
        if (interObject.getMonitorType().equals("1")) {
            final RedisKey redisKey = RedisKeyEnum.MONITOR_INFO.of(peopleId);
            Map<String, String> peoples = RedisHelper.hgetAll(redisKey);
            JSONObject jsonPeople = JSONObject.parseObject(JSON.toJSONString(peoples), JSONObject.class);
            if (flag) {
                jsonPeople.put("assignId", assignmentIds);
                jsonPeople.put("assign", assignNames);
            } else {
                String replace = interObject.getAssignmentId().replace(interObject.getAssignmentId(), assignmentId);
                jsonPeople.put("assignId", replace);
                String assignName1 = clusterDao.findAssignName(assignmentId);
                String replaceName =
                    interObject.getAssignmentName().replace(interObject.getAssignmentName(), assignName1);
                jsonPeople.put("assign", replaceName);

            }

            JobInfoData jobByMonitorId = jobManagementDao.findJobByMonitorId(peopleId);
            Personnel personnel = intercomPersonnelDao.get(peopleId);
            //????????????
            if (personnel.getDriverTypeIds() != null) {
                jsonPeople.put("driverTypeIds", personnel.getDriverTypeIds());
                jsonPeople.put("driverTypeNames", personnel.getDriverTypeNames());
            }
            //??????
            if (personnel.getBloodTypeId() != null) {
                jsonPeople.put("bloodTypeId", personnel.getBloodTypeId());
                jsonPeople.put("bloodTypeName", personnel.getBloodTypeName());
            }
            //??????
            if (personnel.getNationId() != null) {

                jsonPeople.put("nationId", personnel.getNationId());
                jsonPeople.put("nationName", personnel.getNationName());
            }
            //??????
            if (personnel.getSkillIds() != null) {
                jsonPeople.put("skillIds", personnel.getSkillIds());
                jsonPeople.put("skillNames", personnel.getSkillNames());
            }
            // ?????????
            if (personnel.getQualificationId() != null) {
                jsonPeople.put("qualificationId", personnel.getQualificationId());
                jsonPeople.put("qualificationName", personnel.getQualificationName());
            }

            //????????????
            if (jobByMonitorId == null) {
                jsonPeople.put("jobId", "default");
                // ????????????????????????????????????
                jobManagementDao.updateMonitorJobId(peopleId, "default", 2);
            } else {
                jsonPeople.put("jobId", jobByMonitorId.getId());
                jsonPeople.put("jobName", jobByMonitorId.getJobName());
                if (flag) {
                    jobManagementDao.updateMonitorJobId(peopleId, null, 2);
                }

            }
            if (flag) {
                jsonPeople.put("isIncumbency", 2);
            }
            RedisHelper.setString(redisKey, JSON.toJSONString(jsonPeople));
        }

    }

    private void updateOrAddAssignsKnobs(IntercomObjectInfo interObject) {
        String[] assignIds = interObject.getAssignmentId().split(",");

        List<String> existAssignIds = clusterDao.getAssignIdsForMonitor(interObject.getMonitorId());
        List<AssignmentVehicleForm> addAssignmentVehicleList = new ArrayList<>();
        Integer knobNum = 0;
        for (String assignId : assignIds) {
            AssignmentVehicleForm cluster = new AssignmentVehicleForm();
            knobNum++;
            cluster.setAssignmentId(Converter.toBlank(assignId));
            cluster.setVehicleId(interObject.getMonitorId());
            cluster.setCreateDataUsername(SystemHelper.getCurrentUsername());
            cluster.setMonitorType(interObject.getMonitorType());
            cluster.setKnobNo(knobNum);
            cluster.setCreateDataTime(new Date());
            if (existAssignIds.contains(assignId)) {
                clusterDao.updateAssignKnob(cluster);
            } else {
                addAssignmentVehicleList.add(cluster);
            }
        }
        if (!addAssignmentVehicleList.isEmpty()) {
            clusterDao.addAssignVehicleList(addAssignmentVehicleList);
        }
    }

    /**
     * ???????????????????????????????????????8???
     * @param vehiclePerAddList
     * @param vehiclePerDeleteList
     * @return
     */
    private JsonResultBean computeNum(List<AssignmentVehicleForm> vehiclePerAddList,
        List<AssignmentVehicleForm> vehiclePerDeleteList) {
        Map<String, Long> vehcileAddNum = new HashMap<>();
        Map<String, Long> vehcileDelNum = new HashMap<>();
        StringBuilder errorMsg = new StringBuilder("");
        if (CollectionUtils.isNotEmpty(vehiclePerAddList)) {
            vehcileAddNum = vehiclePerAddList.stream()
                .collect(Collectors.groupingBy(AssignmentVehicleForm::getVehicleId, Collectors.counting()));
        }
        if (CollectionUtils.isNotEmpty(vehiclePerDeleteList)) {
            vehcileDelNum = vehiclePerDeleteList.stream()
                .collect(Collectors.groupingBy(AssignmentVehicleForm::getVehicleId, Collectors.counting()));
        }
        for (Map.Entry<String, Long> entry : vehcileAddNum.entrySet()) {
            String vid = entry.getKey();
            Long addNum = entry.getValue();
            if (vehcileDelNum.containsKey(vid)) {
                Long delNum = vehcileDelNum.get(vid);
                addNum = addNum - delNum;
            }
            // ??????????????????????????????
            Integer assignmentNumberOfMonitor = clusterDao.getAssignmentNumberOfMonitor(vid);
            if ((assignmentNumberOfMonitor + addNum) > 8) {
                // ???????????????????????????????????? + ??????????????? ???????????? 8
                final String name = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(vid), "name");
                errorMsg.append(name).append("???");
            }
        }
        if (errorMsg.length() > 0) {
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg.toString() + "????????????????????????8???");
        }
        return null;
    }

    private JsonResultBean addUser(Map<String, List<AssignmentVehicleForm>> assignmentAddForms,
        Set<String> subVehicle) {
        for (Map.Entry<String, List<AssignmentVehicleForm>> entry : assignmentAddForms.entrySet()) {
            String assignmentId = entry.getKey();
            Cluster cluster = clusterDao.findAssignmentById(assignmentId);
            List<AssignmentVehicleForm> forms = entry.getValue();
            List<JSONObject> addIntercomId = new ArrayList<>();
            List<JSONObject> knobUserList = new ArrayList<>();
            for (AssignmentVehicleForm form : forms) {
                String vid = form.getVehicleId();
                form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
                form.setUpdateDataTime(new Date());
                subVehicle.add(vid);
                Integer knobNum = intercomPersonnelDao.findKnobNum(vid);
                Integer knob = 1;
                form.setKnobNo(knob++);
                clusterDao.updateAssignKnob(form);
                Integer knobNo = 1;
                if (knobNum != null && knobNum > 0) {
                    List<Integer> vehicleKnobNumbers = intercomPersonnelDao.getVehicleKnobNumbers(vid);
                    if (CollectionUtils.isNotEmpty(vehicleKnobNumbers)) {
                        for (int i = 1; i <= knobNum; i++) {
                            if (!vehicleKnobNumbers.contains(i)) {
                                knobNo = i;
                                form.setKnobNo(knobNo);
                                break;
                            }
                        }
                    }
                }
                IntercomObjectForm intercomInfoByPeopleId = intercomPersonnelDao.findIntercomInfoByPeopleId(vid);
                if (intercomInfoByPeopleId == null) {
                    // ??????????????????
                    continue;
                }
                if (knobNum != null && knobNum > 0) {
                    JSONObject object = new JSONObject();
                    object.put("userId", intercomInfoByPeopleId.getUserId());
                    object.put("knobNo", knobNo);
                    knobUserList.add(object);
                } else {
                    JSONObject object = new JSONObject();
                    object.put("userId", intercomInfoByPeopleId.getUserId());
                    addIntercomId.add(object);
                }
                // ???20???1??????????????????
                if (knobUserList.size() == 20 || addIntercomId.size() == 20) {
                    JSONObject callBack = talkCallUtils.addAssignmentUser(cluster, addIntercomId, knobUserList);
                    if (callBack != null) {
                        if (callBack.getInteger("result") != 0) {
                            String message = callBack.getString("message");
                            return new JsonResultBean(JsonResultBean.FAULT, message);
                        }
                    } else {
                        return new JsonResultBean(JsonResultBean.FAULT);
                    }
                    knobUserList.clear();
                    addIntercomId.clear();
                }
            }
            if (addIntercomId.size() > 0 || knobUserList.size() > 0) {
                JSONObject callBack = talkCallUtils.addAssignmentUser(cluster, addIntercomId, knobUserList);
                if (callBack != null) {
                    if (callBack.getInteger("result") != 0) {
                        String message = callBack.getString("message");
                        return new JsonResultBean(JsonResultBean.FAULT, message);
                    }
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
            clusterDao.addVehiclePer(forms);
        }
        return null;
    }

    private JsonResultBean deleteUser(Map<String, List<AssignmentVehicleForm>> assignmentDelForms,
        Set<String> subVehicle) {
        for (Map.Entry<String, List<AssignmentVehicleForm>> entry : assignmentDelForms.entrySet()) {
            String assignmentId = entry.getKey();
            Cluster cluster = clusterDao.findAssignmentById(assignmentId);
            List<AssignmentVehicleForm> forms = entry.getValue();
            List<String> deleteIds = new ArrayList<>();
            List<JSONObject> deleteIntercomId = new ArrayList<>();
            // ???20???1??????????????????
            for (AssignmentVehicleForm form : forms) {
                String vid = form.getVehicleId();
                subVehicle.add(vid);
                deleteIds.add(vid);
                IntercomObjectForm intercomInfoByPeopleId =
                    intercomPersonnelDao.findIntercomInfoByPeopleId(form.getVehicleId());
                if (intercomInfoByPeopleId != null) {
                    JSONObject object = new JSONObject();
                    object.put("userId", intercomInfoByPeopleId.getUserId());
                    deleteIntercomId.add(object);
                    if (deleteIntercomId.size() == 20) {
                        JSONObject callBack = talkCallUtils.delAssignmentUser(cluster, deleteIntercomId);
                        if (callBack != null) {
                            if (callBack.getInteger("result") != 0) {
                                String message = callBack.getString("message");
                                return new JsonResultBean(JsonResultBean.FAULT, message);
                            }
                        } else {
                            return new JsonResultBean(JsonResultBean.FAULT);
                        }
                        deleteIntercomId.clear();
                    }
                }
            }
            if (deleteIntercomId.size() > 0) {
                JSONObject callBack = talkCallUtils.delAssignmentUser(cluster, deleteIntercomId);
                if (callBack != null) {
                    if (callBack.getInteger("result") != 0) {
                        String message = callBack.getString("message");
                        return new JsonResultBean(JsonResultBean.FAULT, message);
                    }
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
            }
            log.info("?????????????????????????????????????????????--?????????????????????id:{}, ??????id:{},???????????????{}???",
                deleteIds, assignmentId, SystemHelper.getCurrentUsername());
            clusterDao.deleteVehiclePerBatch(assignmentId, deleteIds);
        }

        return null;
    }

    @Override
    public String findAssignmentGroupId(String id) {
        // TODO Auto-generated method stub
        return clusterDao.findAssignsGroupId(id);
    }

    @Override
    public List<String> findAssignsGroupIds(List<String> assignmentIds) {
        return clusterDao.findAssignsGroupIds(assignmentIds);
    }

    /**
     * ??????????????????????????????
     * @param assignmentId ??????ID
     * @return ?????????????????????
     * @throws Exception ??????
     */
    @Override
    public JSONArray getMonitorByAssignmentID(String assignmentId) throws Exception {
        Cluster cluster = findAssignmentByIdNum(assignmentId);
        List<VehicleInfo> vehicleList = findMonitorByAssignmentId(assignmentId);

        JSONArray result = new JSONArray();
        if (cluster != null) {
            JsonUtil.addAssignmentObjNum(cluster, null, "multiple", result);
            if (vehicleList != null && vehicleList.size() > 0) {
                for (VehicleInfo info : vehicleList) {
                    JSONObject obj = JsonUtil.assembleVehicleObject(info);
                    obj.put("pId", cluster.getId());
                    //obj.put("checked", true);
                    result.add(obj);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> findAssignNames(List<String> assignmentIds) {
        return clusterDao.findAssignNames(assignmentIds);
    }

    @Override
    public List<String> putAssignmentTree(List<Cluster> clusterList, JSONArray result, String type, boolean isBigData) {
        List<String> assignIdList = new ArrayList<>();
        if (clusterList != null && clusterList.size() > 0) {
            List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization"); // ????????????
            // ????????????
            if (allOrg == null || allOrg.isEmpty()) {
                return assignIdList;
            }
            for (OrganizationLdap organization : allOrg) {
                for (Cluster assign : clusterList) {
                    if (organization.getUuid().equals(assign.getGroupId())) {
                        // ??????id list
                        assignIdList.add(assign.getId());
                        // ???????????????
                        JSONObject assignmentObj = new JSONObject();
                        // ??????
                        if (assign.getMNum() != null) {
                            assignmentObj.put("count", assign.getMNum());
                        }
                        assignmentObj.put("id", assign.getId());
                        assignmentObj.put("pId", organization.getId().toString());
                        assignmentObj.put("name", assign.getName());
                        assignmentObj.put("type", "assignment");
                        assignmentObj.put("iconSkin", "assignmentSkin");
                        assignmentObj.put("pName", organization.getName());
                        if ("single".equals(type)) { // ?????????????????????
                            assignmentObj.put("nocheck", true);
                        }
                        if (isBigData) { // ????????????????????????5000
                            assignmentObj.put("isParent", true); // ????????????
                        }
                        result.add(assignmentObj);
                    }
                }
            }
        }
        return assignIdList;
    }

    @Override
    public JSONArray getAssignmentTreeForBigData(String type, boolean isAssignNum) throws Exception {
        JSONArray result = new JSONArray();
        // ???????????????????????????id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // ?????????????????????????????????????????????id???list
        List<String> userOrgListId = userService.getOrgUuidsByUser(userId);
        // ??????????????????????????????
        List<Cluster> clusterList;
        if (isAssignNum) {
            clusterList = findUserAssignmentNum(uuid, userOrgListId, "", "");
        } else {
            clusterList = findUserAssignment(uuid, userOrgListId);
        }
        // ?????????????????????
        putAssignmentTree(clusterList, result, type, true);
        // ???????????????
        // ?????????????????????????????????????????????
        int beginIndex = userId.indexOf(","); // ????????????id(????????????id????????????????????????)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        orgs = new ArrayList<>(OrganizationUtil.filterOrgList(orgs, clusterList));
        result.addAll(getGroupTree(orgs, type, false));
        return result;
    }

    /**
     * ????????????id?????????????????????????????????
     * @param groupId    ??????id
     * @param type       vehicle:?????? monitor:???????????????
     * @param statusType
     * @return ????????????
     * @throws Exception ??????
     */
    @Override
    public Set<String> getMonitorCountByGroup(String groupId, String type, String deviceType, String statusType)
        throws Exception {
        Set<String> result = new HashSet<>();
        // List<String> assignList = new ArrayList<>();
        // List<String> vehicleList = new ArrayList<>();
        // // ???????????????????????????id
        // String userId = SystemHelper.getCurrentUser().getId().toString();
        // String uuid = userService.getUserUuidById(userId);
        // List<OrganizationLdap> childGroup = userService.getOrgChild(groupId);
        // List<String> groupList = new ArrayList<>();
        // if (childGroup != null && !childGroup.isEmpty()) {
        //     for (OrganizationLdap group : childGroup) {
        //         groupList.add(group.getUuid());
        //     }
        // }
        // findAssignmentList(assignList, uuid, groupList);
        // findMonitorList(type, deviceType, result, assignList, vehicleList);
        // if (CollectionUtils.isNotEmpty(result) && StringUtils.isNotBlank(statusType)) {
        //     //??????
        //     if (Objects.equals(statusType, "10")) {
        //         result = Sets.intersection(InitializationData.onlineIds, result);
        //         //??????
        //     } else if (Objects.equals(statusType, "11")) {
        //         Map<String, Response<String>> vehicleStatusMap = RedisHelper.getVehicleStatusMapFromRedis(result);
        //         for (Map.Entry<String, Response<String>> entry : vehicleStatusMap.entrySet()) {
        //             Response<String> response = entry.getValue();
        //             if (response != null) {
        //                 JSONObject vehicleStatusJsonObj = JSON.parseObject(response.get());
        //                 if (vehicleStatusJsonObj != null) {
        //                     Integer vehicleStatus = vehicleStatusJsonObj.getInteger("vehicleStatus");
        //                     if (vehicleStatus != null && vehicleStatus != 3 && vehicleStatus != 11) {
        //                         result.remove(entry.getKey());
        //                     }
        //                 }
        //             }
        //         }
        //     } else if (Objects.equals(statusType, "12")) {
        //         Set<String> restIdSet = new HashSet<>();
        //         String nowTimeStr = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
        //         String restIdStr =
        //             RedisHelper.get(nowTimeStr + RedisKeys.REST_PEOPLE_IDS, PublicVariable.REDIS_TEN_DATABASE);
        //         if (StringUtils.isNotBlank(restIdStr)) {
        //             restIdSet = JSON.parseObject(restIdStr, Set.class);
        //         }
        //         result = Sets.intersection(restIdSet, result);
        //     } else if (Objects.equals(statusType, "13")) {
        //         Set<String> scheduledIdSet = new HashSet<>();
        //         String nowTimeStr = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
        //         String scheduledIdStr =
        //             RedisHelper.get(nowTimeStr + RedisKeys.SCHEDULED_PEOPLE_IDS, PublicVariable.REDIS_TEN_DATABASE);
        //         if (StringUtils.isNotBlank(scheduledIdStr)) {
        //             scheduledIdSet = JSON.parseObject(scheduledIdStr, Set.class);
        //         }
        //         result = Sets.intersection(scheduledIdSet, result);
        //     } else if (Objects.equals(statusType, "14")) {
        //         result = Sets.intersection(getShouldWorkIdSet(), result);
        //     } else if (Objects.equals(statusType, "15")) {
        //         result = getMonitorIdsByWorkStatus(result, statusType);
        //     } else if (Objects.equals(statusType, "16")) {
        //         result = getMonitorIdsByWorkStatus(result, statusType);
        //     } else if (Objects.equals(statusType, "17")) {
        //         result = getMonitorIdsByWorkStatus(result, statusType);
        //     }
        //
        // }
        return result;
    }

    /**
     * ???????????????
     * @return
     */
    private Set<String> getShouldWorkIdSet() {
        Set<String> nowShouldWorkIdSet = new HashSet<>();
        // String nowShouldWorkIdStr =
        //     RedisHelper.get(RedisKeys.SCHEDULED_PEOPLE_SHOULD, PublicVariable.REDIS_FIFTEEN_DATABASE);
        // if (StringUtils.isNotBlank(nowShouldWorkIdStr)) {
        //     nowShouldWorkIdSet = JSON.parseObject(nowShouldWorkIdStr, Set.class);
        // }
        return nowShouldWorkIdSet;
    }

    /**
     * @param result
     * @param workStatus 15:????????????; 16:????????????; 17:??????(??????????????????)
     * @return
     */
    private Set<String> getMonitorIdsByWorkStatus(Set<String> result, String workStatus) {
        // ???????????????????????????????????????
        Set<String> shouldWorkIdSet = Sets.intersection(getShouldWorkIdSet(), result);
        Set<String> monitorIdSet = new HashSet<>();
        // Map<String, Response<String>> vehicleStatusMap = RedisHelper.getVehicleStatusMapFromRedis(shouldWorkIdSet);
        // Map<String, Response<String>> workStatusMap = RedisHelper.getWorkStatusMapFromRedis(shouldWorkIdSet);
        // for (Map.Entry<String, Response<String>> entry : vehicleStatusMap.entrySet()) {
        //     String monitorId = entry.getKey();
        //     Response<String> response = entry.getValue();
        //     if (response == null) {
        //         if (Objects.equals(workStatus, "17")) {
        //             monitorIdSet.add(monitorId);
        //         }
        //         continue;
        //     }
        //     JSONObject vehicleStatusJsonObj = JSON.parseObject(response.get());
        //     if (vehicleStatusJsonObj == null) {
        //         if (Objects.equals(workStatus, "17")) {
        //             monitorIdSet.add(monitorId);
        //         }
        //         continue;
        //     }
        //     Integer vehicleStatus = vehicleStatusJsonObj.getInteger("vehicleStatus");
        //     if (vehicleStatus == null || vehicleStatus == 3 || vehicleStatus == 11) {
        //         if (Objects.equals(workStatus, "17")) {
        //             monitorIdSet.add(monitorId);
        //         }
        //         continue;
        //     }
        //     Response<String> workStatusResponse = workStatusMap.get(monitorId);
        //     if (workStatusResponse == null) {
        //         continue;
        //     }
        //     JSONObject workStatusJsonObj = JSON.parseObject(workStatusResponse.get());
        //     if (workStatusJsonObj == null) {
        //         continue;
        //     }
        //     Integer monitorStatus = workStatusJsonObj.getInteger("monitorStatus");
        //     if (Objects.equals(workStatus, "15")) {
        //         if (Objects.equals(monitorStatus, 2)) {
        //             monitorIdSet.add(monitorId);
        //         }
        //         continue;
        //     }
        //     if (Objects.equals(workStatus, "16")) {
        //         if (Objects.equals(monitorStatus, 3) || Objects.equals(monitorStatus, 4)) {
        //             monitorIdSet.add(monitorId);
        //         }
        //     }
        //
        // }
        return monitorIdSet;
    }

    private void findMonitorList(String type, String deviceType, Set<String> result, List<String> assignList,
        List<String> vehicleList) {
        // ?????????????????????
        if ("monitor".equals(type) && !assignList.isEmpty()) {
            vehicleList = clusterDao.findMonitorIdsByAssignmentIds(assignList);
        } else if ("vehicle".equals(type) && !assignList.isEmpty()) {
            vehicleList = clusterDao.findVehicleIdsByAssignmentIds(assignList, deviceType);
        }
        if (vehicleList != null && !vehicleList.isEmpty()) {
            result.addAll(vehicleList);
        }
    }

    private void findAssignmentList(List<String> assignList, String uuid, List<String> groupList) throws Exception {
        // ????????????????????????
        List<Cluster> clusterList = findUserAssignment(uuid, groupList);
        if (clusterList != null && !clusterList.isEmpty()) {
            for (Cluster anClusterList : clusterList) {
                assignList.add(anClusterList.getId());
            }
        }
    }

    @Override
    public String getAssignsByMonitorId(String vehicleIds) throws Exception {
        List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
        List<Cluster> list = clusterDao.getAssignsByMonitorId(vehicleIdList);
        StringBuilder assignIds = new StringBuilder();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                assignIds.append(list.get(i).getAssignmentId()).append(",");
            }
        }
        return assignIds.toString();
    }

    @Override
    public String checkAssignment(String groupName) {
        // ?????????????????????
        String[] groupNameArr = groupName.split(",");
        List<String> groupNames = new ArrayList<>();
        // ??????????????????(????????????????????????????????????)
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        List<Cluster> userClusters = clusterDao.findUserAssignment(uuid, null);
        if (CollectionUtils.isNotEmpty(userClusters)) {
            for (String name : groupNameArr) {
                if (!"".equals(name)) {
                    for (Cluster userCluster : userClusters) {
                        if (name.equals(userCluster.getName())) {
                            groupNames.add(userCluster.getName());
                            break;
                        }
                    }
                }
            }
        }
        return Joiner.on(",").join(groupNames);
    }

    @Override
    public Set<String> findMonitorByGroupId(List<String> groupIdList, String type, String deviceType) throws Exception {
        Set<String> result = new HashSet<>();
        List<String> assignList = new ArrayList<>();
        List<String> vehicleList = new ArrayList<>();
        // ???????????????????????????id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);

        findAssignmentList(assignList, uuid, groupIdList);
        findMonitorList(type, deviceType, result, assignList, vehicleList);
        return result;
    }

    @Override
    public List<Cluster> findUserAssignmentFuzzy(String userId, List<String> groupList, String query) throws Exception {
        List<Cluster> list = new ArrayList<>();
        if (userId != null && !"".equals(userId) && groupList != null && groupList.size() > 0) {
            
            list = clusterDao.findUserAssignmentFuzzy(userId, groupList, query);
        }
        return list;
    }

    @Override
    public int countLeaveJobPeopleNum(String assignmentId) throws Exception {
        return intercomPersonnelDao.countLeaveJobPeopleNum(assignmentId);
    }

    @Override
    public JsonResultBean getAssignmentNumberOfMonitor(String id) {
        return new JsonResultBean(clusterDao.getAssignmentNumberOfMonitor(id));

    }

    @Override
    public JsonResultBean changeRecordingSwitch(String ipAddress, String assignmentId, Integer flag) {
        Cluster cluster = clusterDao.findAssignmentById(assignmentId);
        cluster.setSoundRecording(flag);
        JSONObject object = talkCallUtils.setRecordStatus(cluster);
        if (object != null) {
            if (object.getInteger("result") != 0) {
                String errorMessage = ErrorMessageEnum.getMessage(object.getInteger("result"));
                return new JsonResultBean(JsonResultBean.FAULT, errorMessage);
            }
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        clusterDao.changeRecordingSwitch(cluster);
        String msg = "";
        if (flag == 1) {
            msg = "???????????????" + cluster.getName() + "???????????????";
        } else {
            msg = "???????????????" + cluster.getName() + "???????????????";
        }
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "");
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public void delUserOwnTemporaryAssignment(String userName) throws Exception {
        List<Cluster> temporaryClusterInfoList = clusterDao.findTemporaryAssignmentByUserName(userName);
        if (CollectionUtils.isNotEmpty(temporaryClusterInfoList)) {
            List<String> temporaryAssignmentIdList =
                temporaryClusterInfoList.stream().map(Cluster::getId).collect(Collectors.toList());
            // ???????????????
            clusterDao.deleteAssignmentByBatch(temporaryAssignmentIdList);
            // ????????????????????????
            clusterDao.delTemporaryAssignmentInterlocutorByAssignmentIdList(temporaryAssignmentIdList);
            // ??????????????????????????????
            clusterDao.deleteAssignmentGroupByAssIdByBatch(temporaryAssignmentIdList);
        }
    }

    @Override
    public List<ClusterForm> findAll() {
        return clusterDao.findAll();
    }

    @Override
    public AssignmentGroupForm getGroupForm(String id) {
        return clusterDao.getGroupForm(id);
    }

    @Override
    public JSONArray vehicleTreeForAssign(String type, String assignmentId, String queryParam, String queryType)
        throws Exception {
        JSONArray result = new JSONArray();
        // ??????????????????????????????
        List<String> assignIdList = listAssignmentsOfCurrentUser(type, assignmentId, result, queryParam, queryType);
        // ???????????????????????????????????????????????????????????????????????????
        if (!queryParam.isEmpty() && "name".equals(queryType)) {
            for (Object obj : result) {
                JSONObject item = (JSONObject) obj;
                item.remove("count");
                item.remove("isParent");
            }
        }
        if (assignIdList.isEmpty()) {
            return result;
        }
        if (!"name".equals(queryType)) {
            queryParam = "";
        }
        queryParam = StringUtil.mysqlLikeWildcardTranslation(queryParam);
        // ?????????????????????????????????????????????????????????(???????????????)
        List<VehicleInfo> vehicleList = clusterDao.findMonitorByAssignmentIdList(assignIdList, queryParam, null, null);
        JSONArray vehicleArray = assembleVehicleData(vehicleList);
        result.addAll(vehicleArray);
        for (int i = 0; i < result.size(); i++) {
            JSONObject jsonObject = result.getJSONObject(i);
            jsonObject.put("open", false);
        }
        return result;
    }

    private List<String> listAssignmentsOfCurrentUser(String type, String assignmentId, JSONArray result,
        String queryParam, String queryType) throws Exception {
        Set<OrganizationLdap> orgList = new HashSet<>();
        // ?????????????????????????????????id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // ?????????????????????????????????????????????
        List<OrganizationLdap> organizations = userService.getOrgChild(userService.getOrgIdByUserId(userId));
        //?????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<>();
        orgList.addAll(organizations);
        boolean notEmpty = CollectionUtils.isNotEmpty(organizations);
        if (queryType != null && "groupName".equals(queryType)) {
            orgList.clear();
            List<OrganizationLdap> filterList =
                organizations.stream().filter(org -> org.getName().contains(queryParam)).collect(Collectors.toList());
            for (OrganizationLdap org : filterList) {
                userOrgListId.add(org.getUuid());
            }
            if (notEmpty) {
                if (CollectionUtils.isNotEmpty(filterList)) {
                    for (OrganizationLdap filterOrg : filterList) {
                        TreeUtils.getLowerOrg(orgList, organizations, filterOrg);
                    }
                }
            }
        }
        // ?????????????????????????????????????????????id???list
        if (notEmpty) {
            if (!"groupName".equals(queryType)) {
                for (OrganizationLdap org : orgList) {
                    userOrgListId.add(org.getUuid());
                }
            }

        }
        // ??????????????????????????????
        List<String> assignIdList = new ArrayList<>();
        List<Cluster> assignmentList = findUserAssignmentNum(uuid, userOrgListId, null, null);
        if (assignmentList != null && !assignmentList.isEmpty()) {
            // ????????????
            List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization");
            for (OrganizationLdap organization : allOrg) {
                for (Cluster assign : assignmentList) {
                    // ?????????????????????
                    if (assignmentId.equals(assign.getId())) {
                        continue;
                    }
                    if (queryType != null && "assignName".equals(queryType)) {
                        if (!assign.getName().contains(queryParam)) {
                            continue;
                        }
                    }
                    if (organization.getUuid().equals(assign.getGroupId())) {
                        // ??????id list
                        assignIdList.add(assign.getId());
                        // ???????????????
                        JsonUtil.addAssignmentObjNum(assign, organization, type, result);
                    }
                }
            }
        }
        if (queryType != null && "assignName".equals(queryType)) {
            Set<OrganizationLdap> filterOrgs = new HashSet<>();
            List<String> pids = new ArrayList<>();
            for (int i = 0; i < result.size(); i++) {
                pids.add(result.getJSONObject(i).getString("pId"));
            }
            if (notEmpty) {
                for (String pid : pids) {
                    filterGroup(organizations, filterOrgs, pid);
                }
            }
            orgList.clear();
            orgList.addAll(filterOrgs);
        }
        JSONArray jsonArray = JsonUtil.getGroupTree(new ArrayList<>(orgList), type, false);
        // ?????????????????????
        result.addAll(jsonArray);
        return assignIdList;
    }

    private void filterGroup(List<OrganizationLdap> orgList, Set<OrganizationLdap> filterOrgs, String parentId) {
        for (OrganizationLdap org : orgList) {
            if (org.getId().toString().equals(parentId)) {
                filterOrgs.add(org);
                filterGroup(orgList, filterOrgs, org.getPid());
            }
        }
    }

    private JSONArray assembleVehicleData(List<VehicleInfo> vehicleList) {
        JSONArray result = new JSONArray();
        // ???????????????
        if (vehicleList != null && !vehicleList.isEmpty()) {
            vehicleList.sort(Comparator.comparing(VehicleInfo::getBrand));
            for (VehicleInfo vehicle : vehicleList) {
                JSONObject vehicleObj = JsonUtil.assembleVehicleObject(vehicle);
                result.add(vehicleObj);
            }
        }
        return result;
    }

    @Override
    public JSONArray listMonitorTreeParentNodes(String assignmentId, String queryParam, String queryType)
        throws Exception {
        JSONArray result = new JSONArray();

        // ??????????????????????????????
        listAssignmentsOfCurrentUser("multiple", assignmentId, result, queryParam, queryType);

        return result;
    }

    @Override
    public JSONArray listMonitorsByAssignmentID(String assignmentID) throws Exception {
        List<VehicleInfo> vehicleList = clusterDao.findMonitorByAssignmentId(assignmentID);

        return assembleVehicleData(vehicleList);
    }

    @Override
    public int countMonitors(String assignmentId) throws Exception {
        JSONArray result = new JSONArray();

        // ??????????????????????????????
        List<String> assignIdList = listAssignmentsOfCurrentUser("multiple", assignmentId, result, null, null);

        if (assignIdList.isEmpty()) {
            return 0;
        }

        return clusterDao.countMonitorByAssignmentIdList(assignIdList);
    }
}
