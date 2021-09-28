package com.zw.platform.service.taskcenter.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.taskmanagement.DesignateInfo;
import com.zw.platform.domain.taskmanagement.DesignateMonitorInfo;
import com.zw.platform.domain.taskmanagement.TaskInfo;
import com.zw.platform.domain.taskmanagement.TaskInfoQuery;
import com.zw.platform.domain.taskmanagement.TaskItem;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.TaskManagementDao;
import com.zw.platform.service.core.UserService;
import com.zw.platform.service.regionmanagement.FenceManagementService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.taskcenter.TaskManagementService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author penghj
 * @version 1.0
 * @date 2019/11/7 13:50
 */
@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    private Logger logger = LogManager.getLogger(TaskManagementServiceImpl.class);

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private TaskManagementDao taskManagementDao;

    @Autowired
    private UserService userService;

    @Autowired
    private FenceManagementService fenceManagementService;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    @Override
    public List<TaskInfo> getTaskList(TaskInfoQuery query) {
        List<TaskInfo> taskInfos = new ArrayList<>();
        List<String> orgList = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        if (CollectionUtils.isEmpty(orgList)) {
            return taskInfos;
        }
        if (StringUtils.isNotEmpty(query.getSimpleQueryParam())) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        taskInfos = PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> taskManagementDao.getTaskList(orgList, query));
        List<OrganizationLdap> allOrganization = userService.getAllOrganization();
        for (OrganizationLdap ldap : allOrganization) {
            for (TaskInfo info : taskInfos) {
                String groupId = info.getGroupId();
                if (groupId.equals(ldap.getUuid())) {
                    info.setGroupName(ldap.getName());
                }
            }
        }
        return taskInfos;
    }

    @Override
    public JsonResultBean addTask(TaskInfo taskInfo, String ipAddress) throws Exception {
        taskInfo.setCreateDataUsername(SystemHelper.getCurrentUsername());
        taskInfo.setGroupId(userService.getOrgUuidByUser());
        List<TaskItem> taskItems = taskInfo.getTaskItems();
        for (TaskItem taskItem : taskItems) {
            taskItem.setTaskId(taskInfo.getId());
        }
        boolean infoFlag = taskManagementDao.addTaskInfo(taskInfo);
        boolean itemFlag = taskManagementDao.addTaskItem(taskItems);
        if (infoFlag && itemFlag) {
            String msg = "任务库：新增（" + taskInfo.getTaskName() + "）";
            logSearchService.addLog(ipAddress, msg, "3", "任务管理", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public List<DesignateInfo> getDesignateList(TaskInfoQuery query) throws ParseException {
        List<DesignateInfo> designateInfos = new ArrayList<>();
        List<String> orgList = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        if (CollectionUtils.isEmpty(orgList)) {
            return designateInfos;
        }
        if (StringUtils.isNotEmpty(query.getSimpleQueryParam())) {
            query.setSimpleQueryParam(StringUtil.mysqlLikeWildcardTranslation(query.getSimpleQueryParam()));
        }
        designateInfos = PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> taskManagementDao.getDesignateList(orgList, query));
        Date data = new Date();
        String nowTimeStr = format.format(data);
        long nowTime = format.parse(nowTimeStr).getTime();
        for (DesignateInfo info : designateInfos) {
            info.setStartDateStr(format.format(info.getStartDate()));
            info.setEndDateStr(format.format(info.getEndDate()));
            long startTime = info.getStartDate().getTime();
            long endTime = info.getEndDate().getTime();
            if (nowTime > endTime) {
                info.setStatus("已结束");
                continue;
            }
            if (nowTime < startTime) {
                info.setStatus("未开始");
                continue;
            }
            if (nowTime >= startTime) {
                info.setStatus("执行中");
                continue;
            }
        }
        List<OrganizationLdap> allOrganization = userService.getAllOrganization();
        for (OrganizationLdap ldap : allOrganization) {
            for (DesignateInfo info : designateInfos) {
                String groupId = info.getGroupId();
                if (groupId.equals(ldap.getUuid())) {
                    info.setGroupName(ldap.getName());
                }
            }
        }
        return designateInfos;
    }

    @Override
    public JsonResultBean updateTask(TaskInfo taskInfo, String ipAddress) throws Exception {
        taskInfo.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        TaskInfo oldTask = taskManagementDao.findTaskInfoById(taskInfo.getId());
        taskManagementDao.deleteTaskItem(taskInfo.getId());
        List<TaskItem> taskItems = taskInfo.getTaskItems();
        boolean infoFlag = taskManagementDao.editTask(taskInfo);
        boolean itemFlag = taskManagementDao.addTaskItem(taskItems);
        if (infoFlag && itemFlag) {
            String msg = "";
            if (oldTask.getTaskName().equals(taskInfo.getTaskName())) {
                msg = "任务库：修改（" + taskInfo.getTaskName() + "）";
            } else {
                msg = "任务库：修改（" + taskInfo.getTaskName() + "），修改为（" + taskInfo.getTaskName() + "）";
            }
            logSearchService.addLog(ipAddress, msg, "3", "任务管理", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public TaskInfo findTaskInfoById(String id) {
        TaskInfo taskInfo = taskManagementDao.findTaskInfoById(id);
        JSONArray fenceArray = fenceManagementService.getFenceTreeJsonArray();
        List<TaskItem> taskItems = taskInfo.getTaskItems();
        if (CollectionUtils.isNotEmpty(taskItems) && fenceArray.size() > 0) {
            for (TaskItem item : taskItems) {
                for (int i = 0; i < fenceArray.size(); i++) {
                    JSONObject fenceObj = fenceArray.getJSONObject(i);
                    if (fenceObj.getString("id").equals(item.getFenceInfoId())) {
                        item.setFenceName(fenceObj.getString("name"));
                        break;
                    }
                }
            }
        }
        return taskInfo;
    }

    @Override
    public JsonResultBean deleteTask(String id, String ipAddress) throws Exception {
        TaskInfo taskInfo = taskManagementDao.findTaskInfoById(id);
        boolean flag = taskManagementDao.deleteTask(id);
        if (flag) {
            String msg = "任务库：删除（" + taskInfo.getTaskName() + "）";
            logSearchService.addLog(ipAddress, msg, "3", "任务管理", "-", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean addDesignate(DesignateInfo designateInfo, String ipAddress) throws Exception {
        boolean flag = false;
        Map<String, String> result = new HashMap<>();
        List<DesignateMonitorInfo> designatePeopleInfos = designateInfo.getDesignatePeopleInfos();
        flag = checkConflict(designateInfo, result, designatePeopleInfos);
        if (flag) {
            return new JsonResultBean(JsonResultBean.FAULT, JSONObject.toJSONString(result));
        }
        boolean infoFlag = taskManagementDao.addDesignateInfo(designateInfo);
        boolean itemFlag = taskManagementDao.addDesignatePeople(designatePeopleInfos);
        if (infoFlag && itemFlag) {
            String msg = "任务指派：新增（" + designateInfo.getDesignateName() + "）";
            logSearchService.addLog(ipAddress, msg, "3", "任务管理", "-", "");
            ZMQFencePub.pubChangeFence("23");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 校验指派冲突
     * @return
     */
    private boolean checkConflict(DesignateInfo designateInfo, Map<String, String> result,
        List<DesignateMonitorInfo> designatePeopleInfos) {
        boolean flag = false;
        TaskInfo taskInfo = taskManagementDao.findTaskInfoById(designateInfo.getTaskId());
        List<TaskItem> taskItems = taskInfo.getTaskItems();
        List<DesignateInfo> newPeoples = new ArrayList<>();
        Set<String> peopleIds = new HashSet<>();
        designateInfo.setCreateDataUsername(SystemHelper.getCurrentUsername());
        designateInfo.setGroupId(userService.getOrgUuidByUser());
        for (DesignateMonitorInfo peopleInfo : designatePeopleInfos) {
            peopleIds.add(peopleInfo.getPeopleId());
            peopleInfo.setStartDate(designateInfo.getStartDate());
            peopleInfo.setEndDate(designateInfo.getEndDate());
            peopleInfo.setDesignateInfoId(designateInfo.getId());
            peopleInfo.setDateDuplicateType(designateInfo.getDateDuplicateType());
            for (TaskItem item : taskItems) {
                // 重新组装 用于后面比较
                DesignateInfo newDesignate = new DesignateInfo();
                newDesignate.setDateDuplicateType(designateInfo.getDateDuplicateType());
                newDesignate.setStartDate(designateInfo.getStartDate());
                newDesignate.setEndDate(designateInfo.getEndDate());
                newDesignate.setStartTime(item.getStartTime());
                newDesignate.setEndTime(item.getEndTime());
                newDesignate.setId(designateInfo.getId());
                newDesignate.setPeopleId(peopleInfo.getPeopleId());
                newPeoples.add(newDesignate);
            }
        }
        return checkConflictInfo(result, flag, newPeoples, peopleIds);

    }

    private boolean checkConflictInfo(Map<String, String> result, boolean flag, List<DesignateInfo> newPeoples,
        Set<String> peopleIds) {
        // 查询企业下选择的监控对象结束时间大于当前时间的任务指派
        List<String> orgList = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        List<DesignateInfo> oldDesignateInfos = taskManagementDao.checkConflict(orgList, peopleIds);
        if (CollectionUtils.isEmpty(oldDesignateInfos)) {
            return false;
        }
        Map<String, List<DesignateInfo>> groupById =
            oldDesignateInfos.stream().collect(Collectors.groupingBy(DesignateInfo::getPeopleId));
        for (DesignateInfo newPeople : newPeoples) {
            String peopleId = newPeople.getPeopleId();
            List<DesignateInfo> designateInfosById = groupById.get(peopleId);
            if (CollectionUtils.isEmpty(designateInfosById)) {
                continue;
            }
            // 过滤相同id   id相同说明为修改  不校验
            List<DesignateInfo> filterEdit =
                designateInfosById.stream().filter(info -> !info.getId().equals(newPeople.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filterEdit)) {
                continue;
            }
            // 过滤日期
            List<DesignateInfo> filterDay =
                filterEdit.stream().filter(info -> checkeDate(newPeople, info)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filterDay)) {
                // 日期不相交
                continue;
            }
            List<DesignateInfo> filterWeek =
                filterDay.stream().filter(info -> checkWeek(newPeople, info)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filterWeek)) {
                // 星期不相交
                continue;
            }
            List<DesignateInfo> filterTime =
                filterWeek.stream().filter(info -> checkTime(newPeople, info)).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(filterTime)) {
                // 时间不相交
                continue;
            }
            for (DesignateInfo info : filterTime) {
                String peopleNumber = info.getPeopleNumber();
                String designateName = info.getDesignateName();
                if (!result.containsKey(peopleNumber)) {
                    result.put(peopleNumber, designateName);
                } else if (!result.get(peopleNumber).contains(designateName)) {
                    result.put(peopleNumber, result.get(peopleNumber) + "," + designateName);
                }
            }
            flag = true;

        }
        return flag;
    }

    private boolean checkeDate(DesignateInfo newInfo, DesignateInfo oldInfo) {
        Long oldStartDate = oldInfo.getStartDate().getTime();
        Long oldEndDate = oldInfo.getEndDate().getTime();
        Long newStartDate = newInfo.getStartDate().getTime();
        Long newEndDate = newInfo.getEndDate().getTime();
        // 相交返回true
        return !(oldStartDate > newEndDate || oldEndDate < newStartDate);
    }

    private boolean checkWeek(DesignateInfo newInfo, DesignateInfo oldInfo) {
        String[] oldDateType = oldInfo.getDateDuplicateType().split(",");
        String[] newDateType = newInfo.getDateDuplicateType().split(",");
        List<String> oldTypeList = Arrays.asList(oldDateType);
        List<String> newTypeList = Arrays.asList(newDateType);
        Set<String> typeList = new HashSet<>();
        typeList.addAll(oldTypeList);
        typeList.addAll(newTypeList);
        if ((typeList.size() == (oldDateType.length + newDateType.length)) && !oldTypeList.contains("8") && !newTypeList
            .contains("8")) {
            // 新旧星期无相交
            return false;
        }
        return true;
    }

    private boolean checkTime(DesignateInfo newInfo, DesignateInfo oldInfo) {
        try {
            long oldStartTime = timeFormat.parse(oldInfo.getStartTime()).getTime();
            long oldEndTime = timeFormat.parse(oldInfo.getEndTime()).getTime();
            long newStartTime = timeFormat.parse(newInfo.getStartTime()).getTime();
            long newEndTime = timeFormat.parse(newInfo.getEndTime()).getTime();
            if (newStartTime > oldEndTime || newEndTime < oldStartTime) {
                // 新旧时间无相交
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("时间转换异常", e);
            return true;
        }
    }

    @Override
    public JsonResultBean updateDesignate(DesignateInfo designateInfo, String ipAddress) throws Exception {
        boolean flag = false;
        Map<String, String> result = new HashMap<>();
        boolean checkCanUpdate = true;
        List<DesignateMonitorInfo> designatePeopleInfos = designateInfo.getDesignatePeopleInfos();
        DesignateInfo oldDesignateInfo = taskManagementDao.findDesignateInfoById(designateInfo.getId());
        Long startDate = oldDesignateInfo.getStartDate().getTime();
        Long endDate = oldDesignateInfo.getEndDate().getTime() + 24 * 60 * 60 * 1000;
        Long nowDate = System.currentTimeMillis();
        // 执行中的指派修改要做判断， 只能新增人员
        if (nowDate >= startDate && nowDate <= endDate) {
            checkCanUpdate = checkCanUpdate(designateInfo, oldDesignateInfo);
        }
        if (!checkCanUpdate) {
            return new JsonResultBean(JsonResultBean.FAULT, "执行中的任务指派修改只能新增人员！");
        }
        flag = checkConflict(designateInfo, result, designatePeopleInfos);
        if (flag) {
            return new JsonResultBean(JsonResultBean.FAULT, JSONObject.toJSONString(result));
        }
        designateInfo.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        taskManagementDao.deleteDesignatePeople(designateInfo.getId());
        boolean infoFlag = taskManagementDao.editDesignate(designateInfo);
        boolean itemFlag = taskManagementDao.addDesignatePeople(designatePeopleInfos);
        if (infoFlag && itemFlag) {
            String msg = "";
            if (oldDesignateInfo.getDesignateName().equals(designateInfo.getDesignateName())) {
                msg = "任务指派：修改（" + designateInfo.getDesignateName() + "）";
            } else {
                msg = "任务指派：修改（" + oldDesignateInfo.getDesignateName() + "），修改为（" + designateInfo.getDesignateName()
                    + "）";
            }
            logSearchService.addLog(ipAddress, msg, "3", "任务管理", "-", "");
            ZMQFencePub.pubChangeFence("23");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private boolean checkCanUpdate(DesignateInfo newDesignateInfo, DesignateInfo oldDesignateInfo) {
        // 执行中的任务指派修改只能新增人员
        if (!oldDesignateInfo.getTaskId().equals(newDesignateInfo.getTaskId())) {
            return false;
        }
        if (!oldDesignateInfo.getDesignateName().equals(newDesignateInfo.getDesignateName())) {
            return false;
        }
        if (oldDesignateInfo.getStartDate().getTime() != newDesignateInfo.getStartDate().getTime()) {
            return false;
        }
        if (oldDesignateInfo.getEndDate().getTime() != newDesignateInfo.getEndDate().getTime()) {
            return false;
        }
        if (!oldDesignateInfo.getDateDuplicateType().equals(newDesignateInfo.getDateDuplicateType())) {
            return false;
        }
        if (!oldDesignateInfo.getRemark().equals(newDesignateInfo.getRemark())) {
            return false;
        }
        List<DesignateMonitorInfo> oldDesignatePeopleInfos = oldDesignateInfo.getDesignatePeopleInfos();
        if (CollectionUtils.isEmpty(oldDesignatePeopleInfos)) {
            return true;
        }
        List<String> oldPeopleIds =
            oldDesignatePeopleInfos.stream().map(DesignateMonitorInfo::getPeopleId).collect(Collectors.toList());
        List<DesignateMonitorInfo> newDesignatePeopleInfos = newDesignateInfo.getDesignatePeopleInfos();
        List<String> newPeopleIds =
            newDesignatePeopleInfos.stream().map(DesignateMonitorInfo::getPeopleId).collect(Collectors.toList());
        // 只能新增人员
        return newPeopleIds.containsAll(oldPeopleIds);
    }

    @Override
    public DesignateInfo getDesignateById(String id) {
        DesignateInfo designateInfoById = taskManagementDao.findDesignateInfoById(id);
        designateInfoById.setStartDateStr(format.format(designateInfoById.getStartDate()));
        designateInfoById.setEndDateStr(format.format(designateInfoById.getEndDate()));
        return designateInfoById;
    }

    @Override
    public JsonResultBean deleteDesignate(String id, String ipAddress) throws Exception {
        DesignateInfo designateInfo = taskManagementDao.findDesignateInfoById(id);
        Long startDate = designateInfo.getStartDate().getTime();
        Long endDate = designateInfo.getEndDate().getTime() + 24 * 60 * 60 * 1000;
        Long nowDate = System.currentTimeMillis();
        if (nowDate >= startDate && nowDate <= endDate) {
            return new JsonResultBean(JsonResultBean.FAULT, "该指派正在执行中，不能删除！");
        }
        boolean flag = taskManagementDao.deleteDesignate(id);
        if (flag) {
            String msg = "任务指派：删除（" + designateInfo.getDesignateName() + "）";
            logSearchService.addLog(ipAddress, msg, "3", "任务管理", "-", "");
            ZMQFencePub.pubChangeFence("23");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public boolean checkTaskName(String name, String id) {
        String gropId;
        if (StringUtils.isBlank(id)) {
            gropId = userService.getOrgUuidByUser();
        } else {
            TaskInfo oldTaskInfo = taskManagementDao.findTaskInfoById(id);
            gropId = oldTaskInfo.getGroupId();
        }

        TaskInfo taskInfo = taskManagementDao.checkTaskName(name, gropId);
        if (taskInfo != null) {
            if (StringUtils.isNotEmpty(id) && id.equals(taskInfo.getId())) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public String getTaskTree() {
        List<TaskInfo> taskInfos = new ArrayList<>();
        List<String> orgList = userService.getOrgUuidsByUser(SystemHelper.getCurrentUser().getId().toString());
        if (CollectionUtils.isEmpty(orgList)) {
            return "";
        }
        taskInfos = taskManagementDao.getTaskTree(orgList);
        return JSON.toJSONString(taskInfos);
    }

    @Override
    public boolean checkDesignateName(String name, String id) {
        String gropId;
        if (StringUtils.isBlank(id)) {
            gropId = userService.getOrgUuidByUser();
        } else {
            DesignateInfo oldDesignateInfo = taskManagementDao.findDesignateInfoById(id);
            gropId = oldDesignateInfo.getGroupId();
        }
        DesignateInfo designateInfo = taskManagementDao.checkDesignateName(name, gropId);
        if (designateInfo != null) {
            if (StringUtils.isNotEmpty(id) && id.equals(designateInfo.getId())) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public JsonResultBean updateForcedEnd(String id, String ipAddress) throws Exception {
        DesignateInfo designateInfo = taskManagementDao.findDesignateInfoById(id);
        boolean flag = taskManagementDao.updateForcedEnd(id);
        if (flag) {
            String msg = "任务指派：强制结束（" + designateInfo.getDesignateName() + "）";
            logSearchService.addLog(ipAddress, msg, "3", "任务管理", "-", "");
            ZMQFencePub.pubChangeFence("23");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public List<DesignateInfo> findDesignateByTaskId(String taskId) {
        return taskManagementDao.findDesignateByTaskId(taskId);
    }
}
