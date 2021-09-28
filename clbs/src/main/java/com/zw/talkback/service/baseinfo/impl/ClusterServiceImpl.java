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
     * 查询群组
     */
    @MethodLog(name = "查询群组", description = "查询群组")
    @Override
    public List<Cluster> findAssignment(AssignmentQuery query) throws Exception {
        // 获取当前用户所属组织及下级组织
        UserLdap user = SystemHelper.getCurrentUser();
        List<String> groupList = userService.getOrgUuidsByUser(user.getId().toString());
        List<Cluster> list;
        query.setGroupList(groupList);

        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            //特殊字符转译
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue());
        if ("admin".equals(user.getUsername())) {
            list = clusterDao.findAssignment(query);
        } else {
            String userId = userService.getUserUuidById(user.getId().toString());
            list = clusterDao.listAssignment(userId, query);
        }
        // 处理result，将groupId对应的groupName给list相应的值赋上
        userService.setGroupNameByGroupId(list);
        return list;
    }

    public Map<String, String> findGroupName(List<String> brandList) {
        Map<String, String> map = new HashMap<>(10);
        List<Cluster> list = clusterDao.findGroupNameByBrand(brandList);
        // 处理result，将groupId对应的groupName给list相应的值赋上
        userService.setGroupNameByGroupId(list);
        for (Cluster cluster : list) {
            //VehicleId 存储的是车牌号，不是ID
            map.put(cluster.getVehicleId(), cluster.getGroupName());
        }
        return map;
    }

    /**
     * 查询user的权限群组
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
     * 查询user的权限群组加统计
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
     * 新增群组
     */
    @Override
    public boolean addAssignment(ClusterForm form) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername()); // 创建者
        form.setCreateDataTime(new Date()); // 创建时间
        return clusterDao.addAssignment(form);
    }

    /**
     * 新增群组和组织关联表
     */
    @Override
    public boolean addAssignmentGroup(AssignmentGroupForm form) throws Exception {
        form.setCreateDataUsername(SystemHelper.getCurrentUsername()); // 创建者
        form.setCreateDataTime(new Date()); // 创建时间
        return clusterDao.addGroupAssignment(form);
    }

    /**
     * 为上级用户分配群组权限
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
                List<String> parentUserAdmin = new ArrayList<>(); // 当前组织及上级组织里的管理员UUID
                List<String> adminUserNames = new ArrayList<>(); // 当前组织及上级组织里的管理员用户名
                // 当前登录用户uuid
                String currUserUuid = userService.getUserUuidById(SystemHelper.getCurrentUser().getId().toString());
                parentUserAdmin.add(currUserUuid);
                adminUserNames.add(SystemHelper.getCurrentUsername());
                List<OrganizationLdap> validOrg = new ArrayList<>(); // 当前组织的上级组织list
                // 获取指定组织的上级组织
                List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization"); // 所有组织 当前组织
                // 递归获取当前组织的上级组织list
                getParentOrg(allOrg, groupId, validOrg);
                // 查询超级管理员和普通管理员下的成员
                List<LdapName> memberNameList = userService.getMemberNameListByRoleCn("POWER_USER");
                memberNameList.addAll(userService.getMemberNameListByRoleCn("ROLE_ADMIN"));
                // 获取上级组织管理员
                for (OrganizationLdap org : validOrg) {
                    // 查询组织下的用户
                    List<UserBean> orgUserList = userService.getUserList(null, org.getId().toString(), false);
                    if (orgUserList == null || orgUserList.isEmpty()) {
                        continue;
                    }
                    for (UserBean user : orgUserList) {
                        String userUuid = user.getUuid();
                        // 获取用户所属角色集合
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
                        //     // 判断该用户是否是普通管理员或者超级管理员且非当前用户
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
                    // 为上级管理员用户分配分组的权限
                    for (String userId : parentUserAdmin) {
                        assUserList.add(new AssignmentUserForm(assignmentId, userId));
                    }
                }
                // 批量新增
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
     * 更新用户组织，重新分配用户分组权限
     * @param userId     用户id
     * @param oldGroupDn 更新前组织Dn
     * @param newGroupDn 更新后组织Dn
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
     * 递归获取指定组织的上级组织
     * @param allList    所有组织
     * @param id         指定组织id
     * @param returnList 上级list
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
     * 新增分组并授权
     */
    @Override
    public JsonResultBean addAssignmentAndPermission(ClusterForm form, AssignmentGroupForm assGroupform,
        String ipAddress, boolean flag) throws Exception {
        List<String> assignmentIdList = new ArrayList<>();
        String assignmentId = form.getId(); // 分组id
        String groupId = assGroupform.getGroupId(); // 所属企业id
        String groupCallNumber = "";
        try {
            groupCallNumber = intercomCallNumberService.updateAndReturnGroupCallNumber();
        } catch (CallNumberExhaustException c) {
            // 组呼号码用完
            return new JsonResultBean(JsonResultBean.FAULT, c.getMessage());
        }
        form.setGroupCallNumber(groupCallNumber);
        // 新增分组调用接口
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
        // 群组录音
        if ("admin".equals(currentUsername) && soundRecording != null && soundRecording == 1) {
            Cluster cluster = new Cluster();
            cluster.setIntercomGroupId(form.getIntercomGroupId());
            cluster.setSoundRecording(soundRecording);
            JSONObject object = talkCallUtils.setRecordStatus(cluster);
            if (object == null || object.getInteger("result") != 0) {
                // 对讲接口出错  平台不设置录音
                form.setSoundRecording(0);
            }
        }

        if (flag == true) {
            // 新增分组
            addAssignment(form);
            // 新增分组与企业关联表
            addAssignmentGroup(assGroupform);
            // 新增分组时，默认给该分组所属组织及上级组织中的管理员分配该分组的权限，同时也会为当前操作用户分配该分组的权限
            assignmentIdList.add(assignmentId);
            assignmentToSuperior(assignmentIdList, groupId, null);
            String msg = "新增群组：" + form.getName() + "( @" + userService.getOrgByUuid(groupId).getName() + " )";
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        } else {
            clusterDao.updateCluster(form);

            assignmentIdList.add(assignmentId);
            assignmentToSuperior(assignmentIdList, groupId, null);
            String msg = "新增群组：" + form.getName() + "( @" + userService.getOrgByUuid(groupId).getName() + " )";
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");

        }

        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    /**
     * 组装分组树结构(查询)
     */
    @Override
    public JSONArray getAssignmentTree() throws Exception {
        // 获取当前用户所在组织及下级组织
        JSONArray result = new JSONArray();
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 获取当前用户所在组织及下级组织
        String orgId = userService.getOrgIdByUser();
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = new ArrayList<String>();
        if (orgs != null && orgs.size() > 0) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }
        // 分组
        List<Cluster> clusterList = findUserAssignment(userService.getUserUuidById(userId), userOrgListId);
        putAssignmentTree(clusterList, result, "multiple", false);
        result.addAll(getGroupTree(orgs, "multiple", false));
        return result;
    }

    /**
     * 组装分组树结构(选择)
     */
    @Override
    public JSONArray getEditAssignmentTree(String assignUserId) throws Exception {
        JSONArray result = redisClusterService.getAssignmentByUserID(assignUserId);

        // 获取指定用户所在组织及下级组织
        List<OrganizationLdap> organizations = userService.getOrgChild(userService.getOrgIdByUserId(assignUserId));
        result.addAll(getGroupTree(organizations, "multiple", false));
        if (!result.isEmpty()) {
            return result;
        }
        // 获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // 查询分组
        List<String> groupList = new ArrayList<>();
        if (organizations != null && organizations.size() > 0) {
            for (OrganizationLdap o : organizations) {
                groupList.add(o.getUuid());
            }
        }
        // 查询分组(当前登录用户权限所有并且在授权用户所属组织及下级组织中)
        List<Cluster> clusterList = findUserAssignment(userService.getUserUuidById(userId), groupList);
        // 查询已选中的分组
        List<String> assignGroupList = userService.getOrgUuidsByUser(assignUserId); // 查询要分配的user的组织及下级组织
        List<Cluster> checkClusterList = findUserAssignment(userService.getUserUuidById(assignUserId), assignGroupList);
        // 组装分组树
        if (clusterList == null || clusterList.isEmpty()) {
            return result;
        }
        List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization"); // 所有组织 当前组织
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
     * 获取分组监控人员树
     * @param assignmentID 分组ID
     * @return 分组监控人员树JSON字符串
     */
    @Override
    public String getAssignMonitorUserTree(String assignmentID) throws Exception {
        Cluster assign = findAssignmentById(assignmentID);
        // 获取分组所属企业及直属上级企业
        List<OrganizationLdap> validOrg = new ArrayList<>(); // 当前组织的上级组织list
        // 获取当前用户所在组织及下级组织
        String orgId = userService.getOrgIdByUser();
        List<OrganizationLdap> allOrg = userService.getOrgChild(orgId); // 所有组织 当前组织
        // 获取当前分组所属组织
        String uuid = assign.getGroupId();
        OrganizationLdap organization = userService.getOrgByUuid(uuid);
        if (organization != null && organization.getId() != null) {
            String assignGroupId = organization.getId().toString();
            getParentOrg(allOrg, assignGroupId, validOrg);
        }

        List<UserBean> users = new ArrayList<>(); // 可分配监控人员列表
        List<String> ulist = new ArrayList<>(); // 可分配监控人员id集合
        if (users.isEmpty() && !validOrg.isEmpty()) {
            for (OrganizationLdap group : validOrg) {
                // 根据组织查询用户
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

        // 已分配监控人员列表
        List<String> assignedUsers = new ArrayList<>();
        if (assignmentID != null && !"".equals(assignmentID)) {
            assignedUsers = vehicleService.findUserAssignByAid(assignmentID, ulist);
        }

        // 组装监控人员树
        JSONArray result = new JSONArray();
        if (!users.isEmpty()) {
            for (UserBean user : users) {
                String urId = user.getId().toString();
                String userUuid = user.getUuid();
                // 获取组织id(根据用户id得到用户所在部门)
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
        // 组装组织树
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
                // 将groupId对应的groupName给list相应的值赋上
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
                // 将groupId对应的groupName给list相应的值赋上
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
        // 群组录音
        if ("admin".equals(currentUsername) && soundRecording != null && !soundRecording
            .equals(assign.getSoundRecording())) {
            Cluster cluster = new Cluster();
            cluster.setIntercomGroupId(assign.getIntercomGroupId());
            cluster.setSoundRecording(soundRecording);
            JSONObject object = talkCallUtils.setRecordStatus(cluster);
            if (object == null || object.getInteger("result") != 0) {
                // 对讲接口出错  平台不修改录音状态
                form.setSoundRecording(assign.getSoundRecording());
            }
        }
        String groupId = findAssignmentGroupId(id);
        List<Assignment> assignments = assignmentService.findByNameForOneOrg(name, form.getGroupId());
        if (CollectionUtils.isNotEmpty(assignments)) {
            for (Assignment assignment : assignments) {
                if ((!Objects.equals(id, assignment.getId())) && Objects.equals(assignment.getName(), name)) {
                    return new JsonResultBean(JsonResultBean.FAULT, "分组名已存在");
                }
            }
        }

        boolean result = clusterDao.updateAssignment(form);
        // 修改分组名字后,记录日志
        if (result) {
            // 如果数据库更新成功且分组名称有改动，则更新缓存
            redisClusterService.updateAssignment(id, assign.getName(), name);
            StringBuilder message = new StringBuilder();
            if (!name.equals(assign.getName())) {
                message.append("修改群组 : ").append(assign.getName()).append(" ( @")
                    .append(userService.getOrgByUuid(groupId).getName()).append(" 修改为 : ").append(name).append(" ( @")
                    .append(userService.getOrgByUuid(groupId).getName()).append(" )").append(")");
            } else {
                message.append("修改群组 : ").append(assign.getName()).append(" ( @")
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
            // 删除分组
            clusterDao.deleteAssignment(id);
            // 回收组呼id
            intercomCallNumberService.updateAndRecycleGroupCallNumber(cluster.getGroupCallNumber());
            // 删除分组与企业的关联
            clusterDao.deleteAssignmentGroupByAssId(id);
            // 删除分组与人的关联
            clusterDao.deleteAssignmentUserByAssId(id);
            // 更新缓存
            redisClusterService.deleteAssignmentsCache(clusters);
            String groupId = findAssignmentGroupId(id);
            String msg = "删除群组 ：" + cluster.getName() + "( @" + userService.getOrgByUuid(groupId).getName() + " )";
            logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 删除分组监听，删对讲平台群组
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
     * 批量删除分组监听，删对讲平台群组
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
            // 回收组呼id
            intercomCallNumberService.updateAndRecycleGroupCallNumber(cluster.getGroupCallNumber());

        }
    }

    @Override
    public JsonResultBean deleteAssignmentByBatch(List<String> ids, String ipAddress) throws Exception {
        boolean flagAssign = false; // 控制是否成功删除分组
        StringBuilder errorMessage = new StringBuilder();
        StringBuilder msg = new StringBuilder(); // 监控对象操作
        for (String id : ids) {
            List<Cluster> clusters = clusterDao.findAssignmentByBatch(Collections.singletonList(id));
            if (CollectionUtils.isEmpty(clusters)) {
                continue;
            }
            Cluster cluster = clusters.get(0);
            JSONObject callBack = talkCallUtils.delAssignment(cluster);
            if (callBack == null) {
                errorMessage.append("删除群组：" + cluster.getName() + "失败,调用对讲接口失败" + "</br>");
                continue;
            }
            if (callBack.getInteger("result") != 0) {
                String message = callBack.getString("message");
                errorMessage.append("删除群组：" + cluster.getName() + "失败," + message + "</br>");
                continue;
            }
            // 删除分组
            clusterDao.deleteAssignment(id);
            // 回收组呼id
            intercomCallNumberService.updateAndRecycleGroupCallNumber(cluster.getGroupCallNumber());
            // 删除分组与企业的关联
            clusterDao.deleteAssignmentGroupByAssId(id);
            // 删除分组与人的关联
            clusterDao.deleteAssignmentUserByAssId(id);
            // 更新缓存
            redisClusterService.deleteAssignmentsCache(clusters);
            String groupId = findAssignmentGroupId(id);
            msg.append("删除群组 ：" + cluster.getName() + "( @" + userService.getOrgByUuid(groupId).getName() + " )");
        }
        if (msg.length() > 0) {
            logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", "批量删除群组");
        }
        if (errorMessage.length() > 0) {
            return new JsonResultBean(errorMessage.toString());
        } else {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }

        /*List<Assignment> assignments = assignmentDao.findAssignmentByBatch(ids); // 批量查询分组数据
        StringBuilder msg = new StringBuilder(); // 监控对象操作
        if (assignments != null && assignments.size() > 0) {
            for (Assignment assignment : assignments) {
                if (assignment.getGroupId() != null) {
                    msg.append("删除分组 : ").append(assignment.getName()).append(" ( @")
                        .append(userService.getOrgByUuid(assignment.getGroupId()).getName()).append(" ) <br/>");
                }
            }
            // 删除分组与企业的关联(批量)
            boolean flagGroup = assignmentDao.deleteAssignmentGroupByAssIdByBatch(ids);// 控制是否成功删除分组与企业的关联
            // 删除分组与人的关联(批量)
            boolean flagPeople = assignmentDao.deleteAssignmentUserByAssIdByBatch(ids); // 控制是否成功删除组织与人的关联
            if (flagGroup && flagPeople) { // 先删除绑定关系再删除分组
                // 删除分组(批量)
                flagAssign = assignmentDao.deleteAssignmentByBatch(ids);
            }
        }
        if (flagAssign) { // 成功删除分组
            // 更新缓存
            redisAssignService.deleteAssignmentsCache(assignments);
            // 记录日志
            String monitoringOperation = "批量删除分组";
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
        // 用户id
        String userId = "uid=admin,ou=organization"; // SystemHelper.getCurrentUser().getId().toString();
        // 用户所属企业及下级
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
        // 导入的文件
        ImportExcel importExcel = new ImportExcel(multipartFile, 1, 0);
        // excel 转换成 list
        List<ClusterForm> list = importExcel.getDataList(ClusterForm.class);
        String temp;
        List<ClusterForm> importList = new ArrayList<>();
        List<AssignmentGroupForm> assignmentGroupList = new ArrayList<>();
        List<String> assignmentIdList = new ArrayList<>();

        // 每个组织下的分组数量
        Set<String> groupIdList = new HashSet<>();
        Map<String, ClusterForm> groupAssignments = new HashMap<>(16);
        StringBuilder msg = new StringBuilder(); // 记录导入的数据
        // 校验需要导入的
        if (list != null && list.size() > 0) {
            getGroupAssignment(list, groupAssignments);

            // 根据组织id查询分组时
            for (int i = 0; i < list.size(); i++) {
                ClusterForm assign = list.get(i);
                //分组名去掉前后空格
                if (assign.getName() != null) {
                    assign.setName(assign.getName().trim());
                }
                // 校验必填字段
                if (StringUtils.isBlank(assign.getName())) {
                    //resultMap.put("flag", 0);
                    //errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                    continue;
                }

                // 列表中重复数据
                if ("REPEAT".equals(assign.getName())) {
                    continue;
                }
                for (int j = list.size() - 1; j > i; j--) {
                    ClusterForm clusterForm = list.get(j);
                    if (StringUtils.isBlank(clusterForm.getName()) || StringUtils.isBlank(assign.getName())) {
                        continue;
                    }
                    // 同一个组织, 分组名称相同, 才算做重复
                    String inGroupName = clusterForm.getGroupName();
                    String outGroupName = assign.getGroupName();
                    if (inGroupName.equals(outGroupName) && clusterForm.getName().equals(assign.getName())) {
                        temp = assign.getName();
                        errorMsg.append("第").append(i + 1).append("行群组名称跟第").append(j + 1).append("行重复，值是：")
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
                // 校验必填字段
                if (StringUtils.isBlank(assign.getName())) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                    continue;
                }
                String groupName = assign.getGroupName();

                // 组织校验
                if (StringUtils.isEmpty(groupName)) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据必填字段未填<br/>");
                    continue;
                }

                ClusterForm clusterForm = groupAssignments.get(groupName);
                // 校验所属企业是否存在
                if (clusterForm == null) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据，所属组织不存在/无所属组织权限<br/>");
                    continue;
                }
                String groupId = clusterForm.getGroupId();
                // 同一组织下分组不能同名
                List<Cluster> assignByGroupId = findByNameForOneOrg(assign.getName(), groupId);
                if (assignByGroupId != null && !assignByGroupId.isEmpty()) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据，群组名称为“").append(assign.getName())
                        .append("”已在当前组织下存在<br/>");
                    continue;
                }

                // 分组名称长度验证
                if (Converter.toBlank(assign.getName()).length() > 30) {
                    resultMap.put("flag", 0);
                    errorMsg.append("第").append(i + 1).append("条数据，分组名称不能超过30位<br/>");
                    continue;
                }
                // 非必填字段不符合规则，默认值为“”
                if (Converter.toBlank(assign.getContacts()).length() > 20) {
                    assign.setContacts("");
                }
                if (Converter.toBlank(assign.getDescription()).length() > 50) {
                    assign.setDescription("");
                }
                // 验证电话号码
                if (StringUtils.isNotBlank(assign.getTelephone())) {
                    // 创建 Pattern 对象
                    Pattern patternMoble = Pattern.compile("^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\\d{8})$");
                    Pattern patternTel = Pattern.compile("^(\\d{3,4}-?)?\\d{7,9}$");
                    // 现在创建 matcher 对象
                    Matcher matcherMoble = patternMoble.matcher(assign.getTelephone());
                    Matcher matcherTel = patternTel.matcher(assign.getTelephone());
                    if (!(matcherTel.matches() || (assign.getTelephone().length() == 11 && matcherMoble.matches()))) {
                        assign.setTelephone("");
                    }
                }

                // 验证是否超过了一百条
                Integer assignmentNumber = clusterForm.getAssignmentNumber();
                if (Objects.nonNull(assignmentNumber)) {
                    if (assignmentNumber >= ASSIGNMENT_MAX_NUMBER) {
                        resultMap.put("flag", 0);
                        errorMsg.append("第").append(i + 1).append("条数据，【").append(groupName).append("】下的群组上限已达到500个！");
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
                // 创建者
                assign.setCreateDataTime(new Date()); // 创建时间
                AssignmentGroupForm assignmentGroupForm = new AssignmentGroupForm();
                // 组装关联表
                assignmentGroupForm.setAssignmentId(assign.getId());
                assignmentGroupForm.setGroupId(groupId);
                assignmentGroupForm.setCreateDataTime(new Date());
                assignmentGroupForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
                importList.add(assign);
                assignmentGroupList.add(assignmentGroupForm);
                assignmentIdList.add(assign.getId());
                session.setAttribute("CONFIG_IMPORT_PROGRESS", i * 10 / list.size() + 10);
                msg.append("群组 : ").append(assign.getName()).append(" ( @")
                    .append(userService.getOrgByUuid(groupId).getName()).append(" ) <br/>");
            }
        }
        // 组装导入结果
        if (!importList.isEmpty() && !assignmentGroupList.isEmpty()) {
            // 导入逻辑（暂时只有新增，具体导入逻辑还需需求确定）
            boolean flag = clusterDao.addAssignmentByBatch(importList);
            if (flag) {
                flag = clusterDao.addGroupAssignmentByBatch(assignmentGroupList);
            }
            session.setAttribute("CONFIG_IMPORT_PROGRESS", 30);
            // 新增分组时，默认给该分组所属组织及上级组织中的管理员分配该分组的权限，同时也会为当前操作用户分配该分组的权限
            if (CollectionUtils.isNotEmpty(groupIdList)) {
                for (String groupId : groupIdList) {
                    assignmentToSuperior(assignmentIdList, groupId, session);
                }
            }

            if (flag && list != null) {
                resultInfo +=
                    "导入成功" + importList.size() + "条数据, <br/> 导入失败" + (list.size() - importList.size()) + "条数据。";
                resultMap.put("flag", 1);
                resultMap.put("errorMsg", errorMsg.toString());
                resultMap.put("resultInfo", resultInfo);
                if (!"".equals(msg.toString())) {
                    String monitoringOperation = "导入群组";
                    logSearchServiceImpl.addLog(ipAddress, msg.toString(), "3", "batch", monitoringOperation);
                }
            } else {
                resultMap.put("flag", 0);
                resultMap.put("resultInfo", "导入失败！");
                return resultMap;
            }

        } else {
            resultMap.put("flag", 0);
            resultMap.put("errorMsg", errorMsg.toString());
            resultMap.put("resultInfo", "成功导入0条数据。");
            return resultMap;
        }
        return resultMap;
    }

    /**
     * 企业下的分组
     * @param list             list
     * @param groupAssignments groupAssignments
     */
    public void getGroupAssignment(List<ClusterForm> list, Map<String, ClusterForm> groupAssignments) {
        // 当前用户下的所属企业及下级企业, 如果导入的企业不是该用户的企业, 进行提示
        List<OrganizationLdap> organizationLdapList = userService.getOrgChild(userService.getOrgIdByUser());
        List<String> groupIdList =
            organizationLdapList.stream().map(OrganizationLdap::getUuid).collect(Collectors.toList());
        // 得到此次Excel 中导入的企业名称, 然后根据企业名称查询企业下的分组数量
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
        // 表头
        headList.add("群组名称");
        headList.add("所属组织");
        headList.add("组呼号码");
        headList.add("联系人");
        headList.add("电话号码");
        headList.add("备注");
        // 必填字段
        requiredList.add("群组名称");
        requiredList.add("所属组织");
        // 默认设置一条数据
        // 查看当前用户所属企业及下级企业
        List<String> groupNames = userService.getOrgNamesByUser();
        exportList.add("中位1组");
        if (CollectionUtils.isNotEmpty(groupNames)) {
            exportList.add(groupNames.get(0));
        } else {
            exportList.add("");
        }
        exportList.add("00000");
        exportList.add("张三");
        exportList.add("13658965874");
        exportList.add("描述");

        // 组装组织下拉框
        Map<String, String[]> selectMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("所属组织", groupNameArr);
        }
        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
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
        // 查询所有的设备
        AssignmentQuery query = new AssignmentQuery();
        query.setPage(0L);
        query.setLimit(0L);
        List<Cluster> clusterList = findAssignment(query);
        for (Cluster info : clusterList) {
            ClusterForm form = new ClusterForm();
            Integer soundRecording = info.getSoundRecording();
            if (soundRecording == 0) {
                form.setState("已关闭");
            } else {
                form.setState("已开启");
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
            // 输出导文件
            OutputStream out;
            out = response.getOutputStream();
            export1.write(out);
            out.close();
            return true;
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// 将文档对象写入文件输出流
        export1.write(out);
        out.close();
        return true;
    }

    @Override
    public JsonResultBean saveVehiclePer(List<AssignmentVehicleForm> vehiclePerAddList,
        List<AssignmentVehicleForm> vehiclePerDeleteList, String assignmentId, String ipAddress) throws Exception {
        Cluster cluster = findAssignmentById(assignmentId); // 分配监控对象日志
        if (CollectionUtils.isEmpty(vehiclePerAddList) && CollectionUtils.isEmpty(vehiclePerDeleteList)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        JsonResultBean computeNum = computeNum(vehiclePerAddList, vehiclePerDeleteList);
        if (computeNum != null) {
            return computeNum;
        }
        // 维护订阅缓存
        Set<String> subVehicle = new HashSet<>();
        // 删除
        if (!vehiclePerDeleteList.isEmpty()) {
            Map<String, List<AssignmentVehicleForm>> assignmentDelForms =
                vehiclePerDeleteList.stream().collect(Collectors.groupingBy(AssignmentVehicleForm::getAssignmentId));
            JsonResultBean message = deleteUser(assignmentDelForms, subVehicle);
            if (message != null) {
                return message;
            }
        }
        // 新增
        if (!vehiclePerAddList.isEmpty()) {
            Map<String, List<AssignmentVehicleForm>> assignmentAddForms =
                vehiclePerAddList.stream().collect(Collectors.groupingBy(AssignmentVehicleForm::getAssignmentId));
            JsonResultBean message = addUser(assignmentAddForms, subVehicle);
            if (message != null) {
                return message;
            }
        }

        // 更新缓存
        redisClusterService.updateVehiclesCache(vehiclePerDeleteList, vehiclePerAddList);
        String msg = "群组管理：" + cluster.getName() + " ( @" + userService.getOrgByUuid(cluster.getGroupId()).getName()
            + " ) 分配监控对象";
        logSearchServiceImpl.addLog(ipAddress, msg, "3", "", "-", "");
        // 维护订阅信息
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
        //根据分组id查询群组
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
                        //如果缓存的id中包含要移去的群组id
                        // if (interObject.getAssignmentId().contains(vehiclePerDeleteForm.getAssignmentId())) {
                        //把包含的id，替换为空
                        String replace =
                            interObject.getAssignmentId().replace(vehiclePerDeleteForm.getAssignmentId() + ",", "");
                        interObject.setAssignmentId(replace);
                        //}
                        String assignName = clusterDao.findAssignName(vehiclePerDeleteForm.getAssignmentId());

                        String replaceName = interObject.getAssignmentName().replace(assignName + ",", "");
                        interObject.setAssignmentName(replaceName);

                        //维护人员缓存
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
                //是否是群组，是群组，添加
                if (assignmentById != null && !"".equals(assignmentById.getId())) {
                    final RedisKey key = HistoryRedisKeyEnum.INTERCOM_LIST.of(vehiclePerAddForm.getVehicleId());
                    String s = RedisHelper.getString(key);
                    IntercomObjectInfo interObject = JSONObject.parseObject(s, IntercomObjectInfo.class);
                    //不为空，证明是对讲对象
                    if (interObject != null) {

                        String assignmentStr = new StringBuffer().append(interObject.getAssignmentId()).append(",")
                            .append(vehiclePerAddForm.getAssignmentId()).toString();

                        interObject.setAssignmentId(assignmentStr);

                        String assignmentNameStr =
                            new StringBuffer().append(interObject.getAssignmentName()).append(",")
                                .append(vehiclePerAddForm.getAssignmentName()).toString();
                        interObject.setAssignmentName(assignmentNameStr);
                        //维护人员缓存
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
                                   // flag是个啥？
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
            //驾照类别
            if (personnel.getDriverTypeIds() != null) {
                jsonPeople.put("driverTypeIds", personnel.getDriverTypeIds());
                jsonPeople.put("driverTypeNames", personnel.getDriverTypeNames());
            }
            //血型
            if (personnel.getBloodTypeId() != null) {
                jsonPeople.put("bloodTypeId", personnel.getBloodTypeId());
                jsonPeople.put("bloodTypeName", personnel.getBloodTypeName());
            }
            //民族
            if (personnel.getNationId() != null) {

                jsonPeople.put("nationId", personnel.getNationId());
                jsonPeople.put("nationName", personnel.getNationName());
            }
            //技能
            if (personnel.getSkillIds() != null) {
                jsonPeople.put("skillIds", personnel.getSkillIds());
                jsonPeople.put("skillNames", personnel.getSkillNames());
            }
            // 资格证
            if (personnel.getQualificationId() != null) {
                jsonPeople.put("qualificationId", personnel.getQualificationId());
                jsonPeople.put("qualificationName", personnel.getQualificationName());
            }

            //刷新缓存
            if (jobByMonitorId == null) {
                jsonPeople.put("jobId", "default");
                // 修改工作状态为在职和职位
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
     * 计算监控对象分组数是否超过8个
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
            // 监控对象已有的分组数
            Integer assignmentNumberOfMonitor = clusterDao.getAssignmentNumberOfMonitor(vid);
            if ((assignmentNumberOfMonitor + addNum) > 8) {
                // 如果监控对象已有的分组数 + 新增分组数 不能大于 8
                final String name = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(vid), "name");
                errorMsg.append(name).append("，");
            }
        }
        if (errorMsg.length() > 0) {
            return new JsonResultBean(JsonResultBean.FAULT, errorMsg.toString() + "所在群组不能超过8个");
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
                    // 不是对讲对象
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
                // 每20个1批次进行增加
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
            // 每20个1批次进行删除
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
            log.info("对象模块分组分配车辆：删除车辆--分组关系：车辆id:{}, 分组id:{},操作用户：{}：",
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
     * 获取分组的已绑定信息
     * @param assignmentId 分组ID
     * @return 分组的绑定信息
     * @throws Exception 异常
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
            List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization"); // 所有组织
            // 当前组织
            if (allOrg == null || allOrg.isEmpty()) {
                return assignIdList;
            }
            for (OrganizationLdap organization : allOrg) {
                for (Cluster assign : clusterList) {
                    if (organization.getUuid().equals(assign.getGroupId())) {
                        // 分组id list
                        assignIdList.add(assign.getId());
                        // 组装分组树
                        JSONObject assignmentObj = new JSONObject();
                        // 数量
                        if (assign.getMNum() != null) {
                            assignmentObj.put("count", assign.getMNum());
                        }
                        assignmentObj.put("id", assign.getId());
                        assignmentObj.put("pId", organization.getId().toString());
                        assignmentObj.put("name", assign.getName());
                        assignmentObj.put("type", "assignment");
                        assignmentObj.put("iconSkin", "assignmentSkin");
                        assignmentObj.put("pName", organization.getName());
                        if ("single".equals(type)) { // 根节点是否可选
                            assignmentObj.put("nocheck", true);
                        }
                        if (isBigData) { // 监控对象数量大于5000
                            assignmentObj.put("isParent", true); // 有子节点
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
        // 根据用户名获取用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 遍历得到当前用户组织及下级组织id的list
        List<String> userOrgListId = userService.getOrgUuidsByUser(userId);
        // 查询当前用户权限分组
        List<Cluster> clusterList;
        if (isAssignNum) {
            clusterList = findUserAssignmentNum(uuid, userOrgListId, "", "");
        } else {
            clusterList = findUserAssignment(uuid, userOrgListId);
        }
        // 组装分组树结构
        putAssignmentTree(clusterList, result, type, true);
        // 组装组织树
        // 获取当前用户所在组织及下级组织
        int beginIndex = userId.indexOf(","); // 获取组织id(根据用户id得到用户所在部门)
        String orgId = userId.substring(beginIndex + 1);
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        orgs = new ArrayList<>(OrganizationUtil.filterOrgList(orgs, clusterList));
        result.addAll(getGroupTree(orgs, type, false));
        return result;
    }

    /**
     * 根据组织id查询其下的监控对象数量
     * @param groupId    组织id
     * @param type       vehicle:查车 monitor:查监控对象
     * @param statusType
     * @return 监控对象
     * @throws Exception 异常
     */
    @Override
    public Set<String> getMonitorCountByGroup(String groupId, String type, String deviceType, String statusType)
        throws Exception {
        Set<String> result = new HashSet<>();
        // List<String> assignList = new ArrayList<>();
        // List<String> vehicleList = new ArrayList<>();
        // // 根据用户名获取用户id
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
        //     //在线
        //     if (Objects.equals(statusType, "10")) {
        //         result = Sets.intersection(InitializationData.onlineIds, result);
        //         //离线
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
     * 当前应上班
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
     * @param workStatus 15:上班在岗; 16:上班离岗; 17:离线(应上班中离线)
     * @return
     */
    private Set<String> getMonitorIdsByWorkStatus(Set<String> result, String workStatus) {
        // 组织下当前应上班的监控对象
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
        // 查询分组中的车
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
        // 查询组织下的分组
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
        // 车辆所在的分组
        String[] groupNameArr = groupName.split(",");
        List<String> groupNames = new ArrayList<>();
        // 分组权限验证(只显示当前用户拥有的分组)
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
        // 根据用户名获取用户id
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
            msg = "开启群组“" + cluster.getName() + "”录音状态";
        } else {
            msg = "关闭群组“" + cluster.getName() + "”录音状态";
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
            // 删除临时组
            clusterDao.deleteAssignmentByBatch(temporaryAssignmentIdList);
            // 删除临时组内成员
            clusterDao.delTemporaryAssignmentInterlocutorByAssignmentIdList(temporaryAssignmentIdList);
            // 删除临时组与企业关系
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
        // 获取当前用户权限分组
        List<String> assignIdList = listAssignmentsOfCurrentUser(type, assignmentId, result, queryParam, queryType);
        // 查询条件不为空，返回的查询结果不可异步展开分组节点
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
        // 查询当前用户所拥有的权限分组下面的车辆(并且已绑定)
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
        // 根据用户名获取当前用户id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        // 获取当前用户所在组织及下级组织
        List<OrganizationLdap> organizations = userService.getOrgChild(userService.getOrgIdByUserId(userId));
        //当前用户组织及下级组织id的list
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
        // 遍历得到当前用户组织及下级组织id的list
        if (notEmpty) {
            if (!"groupName".equals(queryType)) {
                for (OrganizationLdap org : orgList) {
                    userOrgListId.add(org.getUuid());
                }
            }

        }
        // 查询当前用户权限分组
        List<String> assignIdList = new ArrayList<>();
        List<Cluster> assignmentList = findUserAssignmentNum(uuid, userOrgListId, null, null);
        if (assignmentList != null && !assignmentList.isEmpty()) {
            // 所有组织
            List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization");
            for (OrganizationLdap organization : allOrg) {
                for (Cluster assign : assignmentList) {
                    // 排除传入的分组
                    if (assignmentId.equals(assign.getId())) {
                        continue;
                    }
                    if (queryType != null && "assignName".equals(queryType)) {
                        if (!assign.getName().contains(queryParam)) {
                            continue;
                        }
                    }
                    if (organization.getUuid().equals(assign.getGroupId())) {
                        // 分组id list
                        assignIdList.add(assign.getId());
                        // 组装分组树
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
        // 组装组织树结构
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
        // 组装车辆树
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

        // 获取当前用户权限分组
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

        // 获取当前用户权限分组
        List<String> assignIdList = listAssignmentsOfCurrentUser("multiple", assignmentId, result, null, null);

        if (assignIdList.isEmpty()) {
            return 0;
        }

        return clusterDao.countMonitorByAssignmentIdList(assignIdList);
    }
}
