package com.zw.platform.service.basicinfo.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.page.PageMethod;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.domain.GroupDO;
import com.zw.platform.basic.repository.GroupDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.AssignmentForm;
import com.zw.platform.domain.basicinfo.form.AssignmentImportForm;
import com.zw.platform.domain.basicinfo.query.AssignmentQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.AssembleUtil;
import com.zw.platform.util.JsonUtil;
import com.zw.platform.util.OrganizationUtil;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wangying
 * @deprecated  since 4.4.0 please use groupService to replace
 */
@Service
@Slf4j
public class AssignmentServiceImpl implements AssignmentService {

    private final Logger logger = LogManager.getLogger(AssignmentServiceImpl.class);

    private GroupDao groupDao;

    private UserService userService;

    @Autowired
    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * ????????????
     */
    @MethodLog(name = "????????????", description = "????????????")
    @Override
    public List<Assignment> findAssignment(AssignmentQuery query) {
        // ?????????????????????????????????????????????
        UserLdap user = SystemHelper.getCurrentUser();
        List<String> groupList = userService.getOrgUuidsByUser(user.getId().toString());
        List<Assignment> list;
        query.setGroupList(groupList);

        if (StringUtils.isNotBlank(query.getSimpleQueryParam())) {
            //??????????????????
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        if (user.getUsername().equals("admin")) {
            PageMethod.startPage(query.getPage().intValue(), query.getLimit().intValue());
            list = PageHelperUtil.doSelect(query, () -> groupDao.findAssignment(query));
        } else {
            String userId = userService.getUserUuidById(user.getId().toString());
            list = PageHelperUtil.doSelect(query, () -> groupDao.listAssignment(userId, query));
        }
        // ??????result??????groupId?????????groupName???list??????????????????
        userService.setGroupNameByGroupId(list);
        return list;
    }

    public Map<String, String> findGroupName(List<String> brandList) {
        Map<String, String> map = new HashMap<>(10);
        List<Assignment> list = groupDao.findGroupNameByBrand(brandList);
        // ??????result??????groupId?????????groupName???list??????????????????
        userService.setGroupNameByGroupId(list);
        for (Assignment assignment : list) {
            //VehicleId ??????????????????????????????ID
            map.put(assignment.getVehicleId(), assignment.getGroupName());
        }
        return map;
    }

    /**
     * ??????user???????????????
     */
    @Override
    public List<Assignment> findUserAssignment(String userId, List<String> groupList) {
        List<Assignment> list = new ArrayList<>();
        if (userId != null && !"".equals(userId) && CollectionUtils.isNotEmpty(groupList)) {
            list = groupDao.findUserAssignment(userId, groupList);
        }
        return list;
    }

    /**
     * ??????user????????????????????????
     */
    @Override
    public List<Assignment> findUserAssignmentNum(String userId, List<String> groupList, String monitorType,
                                                  String deviceType) {
        List<Assignment> list = new ArrayList<>();
        if (userId != null && !"".equals(userId) && CollectionUtils.isNotEmpty(groupList)) {
            PageMethod.startPage(0, 0, false);
            list = groupDao.findUserAssignment(userId, groupList);
            Set<String> deviceTypeSet = new HashSet<>();
            if (StringUtils.isNotEmpty(deviceType)) {
                if ("1".equals(deviceType)) {
                    deviceTypeSet.addAll(Arrays.asList(ProtocolEnum.REALTIME_VIDEO_DEVICE_TYPE));
                } else {
                    deviceTypeSet.add(deviceType);
                }
            }
            List<Assignment> countList = groupDao.findAssignmentNum(list, monitorType, deviceTypeSet);
            Map<String, Integer> map = new HashMap<>();
            countList.forEach(c -> map.put(c.getId(), c.getMNum()));
            for (Assignment assign : list) {
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
    public boolean addAssignment(AssignmentForm form) {
        GroupDO groupDO = new GroupDO();
        groupDO.setId(UUID.randomUUID().toString());
        groupDO.setDescription(form.getDescription());
        groupDO.setName(form.getName());
        groupDO.setTelephone(form.getTelephone());
        groupDO.setContacts(form.getContacts());
        groupDO.setOrgId(form.getGroupId());
        groupDO.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return groupDao.add(groupDO);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param allList    ????????????
     * @param id         ????????????id
     * @param returnList ??????list
     * @author wangying
     */
    @Override
    public void getParentOrg(List<OrganizationLdap> allList, String id, List<OrganizationLdap> returnList) {
        if (CollectionUtils.isNotEmpty(allList)) {
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
     * ?????????????????????(??????)
     */
    @Override
    public JSONArray getAssignmentTree() {
        // ?????????????????????????????????????????????
        JSONArray result = new JSONArray();
        // ???????????????????????????id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        // ?????????????????????????????????????????????
        String orgId = userService.getOrgIdByUser();
        List<OrganizationLdap> orgs = userService.getOrgChild(orgId);
        // ?????????????????????????????????????????????id???list
        List<String> userOrgListId = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(orgs)) {
            for (OrganizationLdap org : orgs) {
                userOrgListId.add(org.getUuid());
            }
        }


        // ??????
        List<Assignment> assignmentList = findUserAssignment(userService.getUserUuidById(userId), userOrgListId);
        putAssignmentTree(assignmentList, result, "multiple", false);

        orgs = OrganizationUtil.filterOrgList(orgs, assignmentList);
        orgs.sort(Comparator.comparing(OrganizationLdap::getCreateTimestamp));
        result.addAll(JsonUtil.getOrgTree(orgs, "multiple"));

        return result;
    }


    private List<Assignment> findUserAssignment(String userId, List<String> groupList, String assignName) {
        List<Assignment> list = new ArrayList<>();
        if (userId != null && !"".equals(userId) && CollectionUtils.isNotEmpty(groupList)) {
            list = groupDao.findUserAssignmentFilterName(userId, groupList, assignName);
        }
        return list;
    }


    @Override
    public Assignment findAssignmentById(String id) {
        if (id != null && !"".equals(id)) {
            List<Assignment> list = new ArrayList<>();
            Assignment ass = groupDao.findAssignmentById(id);
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
    public Assignment findAssignmentByIdNum(String id) {
        if (id != null && !"".equals(id)) {
            List<Assignment> list = new ArrayList<>();
            Assignment ass = groupDao.findAssignmentByIdNum(id);
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
    public List<VehicleInfo> findVehicleByAssignmentId(String assignmentId) {
        if (assignmentId != null && !"".equals(assignmentId)) {
            return groupDao.findVehicleByAssignmentId(assignmentId);
        }
        return Collections.emptyList();
    }

    @Override
    public List<VehicleInfo> findMonitorByAssignmentId(String assignmentId) {
        if (assignmentId != null && !"".equals(assignmentId)) {
            return groupDao.findMonitorByAssignmentId(assignmentId);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean removeAssignmentVehicle(String assignment, String vehicleList) {
        // ?????????
        List<String> vehList = new ArrayList<>();
        if (StringUtils.isNotBlank(vehicleList)) {
            String[] vehicleTree = vehicleList.split(";");
            vehList = Arrays.asList(vehicleTree);
        }
        log.info("swagger?????????????????????--?????????????????????id:{}, ??????id:{}??????????????????{}",
            vehicleList, assignment, SystemHelper.getCurrentUsername());
        return StringUtils.isNotBlank(assignment) && !vehList.isEmpty() && groupDao
                .deleteAssignmentVehicleByVidAid(assignment, vehList);
    }

    @Override
    public List<Assignment> findByNameForOneOrg(String name, String groupId) {
        if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(groupId)) {
            return groupDao.findByNameForOneOrg(name, groupId);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Assignment> findOneOrgAssiForNameRep(String id, String name, String groupId) {
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(groupId)) {
            return groupDao.findOneOrgAssiForNameRep(id, name, groupId);
        }
        return Collections.emptyList();
    }


    private Map<String, String> validate(List<AssignmentImportForm> importAssignmentList, StringBuilder msg) {
        List<OrganizationLdap> organizationLdapList = userService.getOrgChild(userService.getOrgIdByUser());
        Map<String, String> orgIdAndNameMap = organizationLdapList.stream()
                .collect(Collectors.toMap(OrganizationLdap::getUuid, OrganizationLdap::getName));
        Map<String, OrganizationLdap> groupNameMap =
                AssembleUtil.collectionToMap(organizationLdapList, OrganizationLdap::getName);
        Map<String, String> groupNameIdMap = new HashMap<>();
        // ????????????id???????????????
        for (int i = 0, len = importAssignmentList.size(); i < len; i++) {
            AssignmentImportForm assign = importAssignmentList.get(i);
            if (StringUtils.isNotBlank(assign.getErrorMsg())) {
                continue;
            }
            for (int j = importAssignmentList.size() - 1; j > i; j--) {
                AssignmentImportForm assignmentForm = importAssignmentList.get(j);
                if (StringUtils.isNotBlank(assignmentForm.getErrorMsg())) {
                    continue;
                }
                // ???????????????, ??????????????????, ???????????????
                String inGroupName = assignmentForm.getGroupName();
                String outGroupName = assign.getGroupName();
                if (inGroupName.equals(outGroupName) && assignmentForm.getName().equals(assign.getName())) {
                    assignmentForm.setErrorMsg("??????????????????");
                }
            }
        }
        for (AssignmentImportForm assign : importAssignmentList) {
            if (StringUtils.isNotBlank(assign.getErrorMsg())) {
                continue;
            }
            String groupName = assign.getGroupName();
            OrganizationLdap organizationLdap = groupNameMap.get(groupName);
            // ??????????????????????????????
            if (organizationLdap == null) {
                assign.setErrorMsg("?????????????????????/?????????????????????");
                continue;
            }
            String groupId = organizationLdap.getUuid();
            groupNameIdMap.put(groupName, groupId);
            // ????????????????????????
            if (Converter.toBlank(assign.getName()).length() > 30) {
                assign.setErrorMsg("????????????????????????30???");
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
            msg.append("?????? : ").append(assign.getName()).append(" ( @").append(orgIdAndNameMap.get(groupId))
                    .append(" ) <br/>");
        }
        return groupNameIdMap;
    }

    @Override
    public List<Assignment> findAssignmentByGroupId(String groupId) {
        if (StringUtils.isNotBlank(groupId)) {
            return groupDao.findAssignmentByGroupId(groupId);
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
    public List<Assignment> findAssignByGroupIdExpectVehicle(String groupId, String assignmentId) {
        if (StringUtils.isNotBlank(groupId) && StringUtils.isNotBlank(assignmentId)) {
            return groupDao.findAssignByGroupIdExpectVehicle(groupId, assignmentId);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean exportAssignment(String title, int type, HttpServletResponse response) throws Exception {
        ExportExcel export = new ExportExcel(title, AssignmentForm.class, 1);
        List<AssignmentForm> exportList = new ArrayList<>();
        // ?????????????????????
        AssignmentQuery query = new AssignmentQuery();
        query.setPage(0L);
        query.setLimit(0L);
        List<Assignment> assignmentList = findAssignment(query);
        for (Assignment info : assignmentList) {
            AssignmentForm form = new AssignmentForm();
            BeanUtils.copyProperties(info, form);
            exportList.add(form);
        }
        export.setDataList(exportList);
        // ???????????????
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);// ????????????????????????????????????
        out.close();
        return true;
    }

    /**
     * ??????????????????????????????
     *
     * @param assignmentId ??????ID
     * @return ?????????????????????
     */
    @Override
    public JSONArray getMonitorByAssignmentID(String assignmentId) {
        Assignment assignment = findAssignmentByIdNum(assignmentId);
        List<VehicleInfo> vehicleList = findMonitorByAssignmentId(assignmentId);

        JSONArray result = new JSONArray();
        if (assignment != null) {
            JsonUtil.addAssignmentObjNum(assignment, null, "multiple", result);
            if (CollectionUtils.isNotEmpty(vehicleList)) {
                for (VehicleInfo info : vehicleList) {
                    JSONObject obj = JsonUtil.assembleVehicleObject(info);
                    obj.put("pId", assignment.getId());
                    // obj.put("checked", true);
                    result.add(obj);
                }
            }
        }
        return result;
    }

    @Override
    public List<String> putAssignmentTree(List<Assignment> assignmentList, JSONArray result, String type,
                                          boolean isBigData) {
        List<String> assignIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(assignmentList)) {
            // ????????????
            List<OrganizationLdap> allOrg = userService.getOrgChild("ou=organization");
            // ????????????
            if (CollectionUtils.isEmpty(allOrg)) {
                return assignIdList;
            }
            Map<String, OrganizationLdap> allOrgMap = new HashMap<>(allOrg.size());
            for (OrganizationLdap org : allOrg) {
                allOrgMap.put(org.getUuid(), org);
            }

            //???????????????????????????id
            Set<String> onlineMonitorIdSet = new HashSet<>();
            List<String> onLineKeys = RedisHelper.scanKeys(HistoryRedisKeyEnum.MONITOR_STATUS_FUZZY.of());
            for (String s : onLineKeys) {
                onlineMonitorIdSet.add(s.substring(0, 36));
            }
            try {
                List<String> groupIds = assignmentList.stream().map(Assignment::getId).collect(Collectors.toList());
                Map<String, Set<String>> groupMonitorMap =
                    RedisHelper.batchGetSetReturnMap(RedisKeyEnum.GROUP_MONITOR.ofs(groupIds));
                OrganizationLdap organization;
                for (Assignment assign : assignmentList) {
                    organization = allOrgMap.get(assign.getGroupId());
                    if (organization != null) {
                        // ??????id list
                        assignIdList.add(assign.getId());
                        // ???????????????
                        JSONObject assignmentObj = new JSONObject();
                        String assignId = assign.getId();
                        Set<String> monitorIds = groupMonitorMap.get(assignId);
                        int num = 0;
                        int onLine = 0;
                        if (CollectionUtils.isNotEmpty(monitorIds)) {
                            num = monitorIds.size();
                            monitorIds.retainAll(onlineMonitorIdSet);
                            onLine = monitorIds.size();
                        }
                        // ??????
                        if (assign.getMNum() != null) {
                            assignmentObj.put("count", assign.getMNum());
                        }
                        // ?????????????????????????????????
                        assignmentObj.put("canCheck", num);
                        assignmentObj.put("onLine", onLine);
                        assignmentObj.put("offLine", num - onLine);
                        assignmentObj.put("id", assignId);
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
            } catch (Exception e) {
                logger.error("???????????????", e);
            }
        }
        return assignIdList;
    }

    /**
     * ????????????id?????????????????????????????????
     *
     * @param groupId ??????id
     * @param type    vehicle:?????? monitor:???????????????
     * @return ????????????
     */
    @Override
    public Set<String> getMonitorCountByGroup(String groupId, String type, String deviceType) {
        Set<String> result = new HashSet<>();
        List<String> assignList = new ArrayList<>();
        List<String> vehicleList = new ArrayList<>();
        // ???????????????????????????id
        String userId = SystemHelper.getCurrentUser().getId().toString();
        String uuid = userService.getUserUuidById(userId);
        List<OrganizationLdap> childGroup = userService.getOrgChild(groupId);
        List<String> groupList = new ArrayList<>();
        if (childGroup != null && !childGroup.isEmpty()) {
            for (OrganizationLdap group : childGroup) {
                groupList.add(group.getUuid());
            }
        }
        findAssignmentList(assignList, uuid, groupList);
        findMonitorList(type, deviceType, result, assignList, vehicleList);

        return result;
    }

    private void findMonitorList(String type, String deviceType, Set<String> result, List<String> assignList,
                                 List<String> vehicleList) {
        // ?????????????????????
        if ("monitor".equals(type) && !assignList.isEmpty()) {
            vehicleList = groupDao.findMonitorIdsByAssignmentIds(assignList);
        } else if ("vehicle".equals(type) && !assignList.isEmpty()) {
            vehicleList = groupDao.findVehicleIdsByAssignmentIds(assignList, deviceType);
        }
        if (vehicleList != null && !vehicleList.isEmpty()) {
            result.addAll(vehicleList);
        }
    }

    private void findAssignmentList(List<String> assignList, String uuid, List<String> groupList) {
        // ????????????????????????
        List<Assignment> assignmentList = findUserAssignment(uuid, groupList);
        if (assignmentList != null && !assignmentList.isEmpty()) {
            for (Assignment anAssignmentList : assignmentList) {
                assignList.add(anAssignmentList.getId());
            }
        }
    }

    @Override
    public String getAssignsByMonitorId(String vehicleIds) {
        List<String> vehicleIdList = Arrays.asList(vehicleIds.split(","));
        List<Assignment> list = groupDao.getAssignsByMonitorId(vehicleIdList);
        StringBuilder assignIds = new StringBuilder();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Assignment assignment : list) {
                assignIds.append(assignment.getAssignmentId()).append(",");
            }
        }
        return assignIds.toString();
    }

    @Override
    public List<Assignment> findUserAssignmentFuzzy(String userId, List<String> groupList, String query) {
        List<Assignment> list = new ArrayList<>();
        if (userId != null && !"".equals(userId) && CollectionUtils.isNotEmpty(groupList)) {
            list = groupDao.findUserAssignmentFuzzy(userId, groupList, query);
        }
        return list;
    }
}
