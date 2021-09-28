package com.zw.talkback.service.baseinfo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.Assignment;
import com.zw.platform.domain.basicinfo.AssignmentInfo;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.UserLdap;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.event.ConfigUpdateEvent;
import com.zw.platform.service.basicinfo.AssignmentService;
import com.zw.platform.service.core.UserService;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.MethodLog;
import com.zw.platform.util.excel.ExportExcel;
import com.zw.talkback.domain.basicinfo.Cluster;
import com.zw.talkback.domain.basicinfo.IntercomObjectInfo;
import com.zw.talkback.domain.basicinfo.form.AssignmentVehicleForm;
import com.zw.talkback.domain.basicinfo.form.InConfigInfoForm;
import com.zw.talkback.domain.basicinfo.form.JobInfoData;
import com.zw.talkback.domain.intercom.info.OriginalModelInfo;
import com.zw.talkback.repository.mysql.ClusterDao;
import com.zw.talkback.repository.mysql.InConfigDao;
import com.zw.talkback.repository.mysql.IntercomPersonnelDao;
import com.zw.talkback.repository.mysql.JobManagementDao;
import com.zw.talkback.repository.mysql.OriginalModelDao;
import com.zw.talkback.service.baseinfo.InConfigService;
import com.zw.talkback.service.baseinfo.IntercomCallNumberService;
import com.zw.talkback.service.baseinfo.IntercomObjectService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class InConfigServiceImpl implements InConfigService {

    /**
     * 日志打印工具
     */
    private Logger logger = LogManager.getLogger(InConfigServiceImpl.class);

    @Autowired
    private InConfigDao inConfigDao;

    @Autowired
    private ClusterDao clusterDao;

    @Autowired
    private OriginalModelDao originalModelDao;

    @Autowired
    private IntercomObjectService intercomObjectService;

    @Autowired
    private IntercomPersonnelDao intercomPersonnelDao;

    @Autowired
    private JobManagementDao jobManagementDao;

    @Autowired
    private UserService userService;
    @Autowired
    private IntercomCallNumberService intercomCallNumberService;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private AssignmentService assignmentService;

    /**
     * 一个分组下面最大的车辆数
     */
    @Value("${max.number.assignment.monitor:100}")
    private Integer maxNumberAssignmentMonitor;


    private void addTalkBack(IntercomObjectInfo intercomObjectInfo) {
        intercomObjectService.addIntercomInfo(intercomObjectInfo);

        //更新绑定信息和对讲对象的绑定关系
        inConfigDao.updateConfigIntercomID(intercomObjectInfo.getConfigId(), intercomObjectInfo.getId());
        //维护对讲对象绑定关系redis缓存
        intercomObjectService.addIntercomObjectCache(intercomObjectInfo);
    }


    private void addConfigDiff(InConfigInfoForm config1Form, boolean isAdd) {
        String monitorId = config1Form.getBrandID();
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(monitorId);
        final String vehicle = RedisHelper.getString(key);
        JSONObject monitorInfo = JSONObject.parseObject(vehicle, JSONObject.class);
        if (isAdd) {
            monitorInfo.put("groupId", config1Form.getCitySelID().replaceAll(";", ","));
            monitorInfo.put("simCardNumber", config1Form.getSims());
            monitorInfo.put("deviceNumber", config1Form.getDevices());
            monitorInfo.put("groupName", config1Form.getAssignmentName());
        }
        config1Form.setGroupid(monitorInfo.getString("orgId"));
        //监控对象是人时更新离职分组关系和人员职位及在状态
        if ("1".equals(config1Form.getMonitorType())) {
            List<String> ids = new ArrayList<>();
            ids.add(monitorId);
            intercomPersonnelDao.deleteLeaveJob(ids);
            //刷新缓存
            if (monitorInfo.get("jobId") == null) {
                monitorInfo.put("jobId", "default");
                JobInfoData jobInfoData = jobManagementDao.findJobById("default");
                if (jobInfoData != null) {
                    // 职位类别名称
                    monitorInfo.put("jobName", jobInfoData.getJobName());
                }
                // 修改工作状态为在职和职位
                jobManagementDao.updateMonitorJobId(monitorId, "default", 2);
            } else {
                jobManagementDao.updateMonitorJobId(monitorId, null, 2);
            }
            monitorInfo.put("isIncumbency", 2);
        }
        updateOrAddAssignsKnobs(config1Form);
        RedisHelper.setString(key, JSON.toJSONString(monitorInfo));
    }

    private void updateOrAddAssignsKnobs(InConfigInfoForm config1Form) {
        String[] assignIds = config1Form.getCitySelID().split(";");

        List<String> existAssignIds = clusterDao.getAssignIdsForMonitor(config1Form.getBrandID());
        List<AssignmentVehicleForm> addAssignmentVehicleList = new ArrayList<>();
        int knobNum = 0;
        for (String assignId : assignIds) {
            AssignmentVehicleForm cluster = new AssignmentVehicleForm();
            knobNum++;
            cluster.setAssignmentId(Converter.toBlank(assignId));
            cluster.setVehicleId(config1Form.getBrandID());
            cluster.setCreateDataUsername(SystemHelper.getCurrentUsername());
            cluster.setMonitorType(config1Form.getMonitorType());
            cluster.setKnobNo(knobNum);
            cluster.setCreateDataTime(new Date());
            if (existAssignIds.contains(assignId)) {
                clusterDao.updateAssignKnobs(cluster);
            } else {
                addAssignmentVehicleList.add(cluster);
            }
        }
        if (!addAssignmentVehicleList.isEmpty()) {
            clusterDao.addAssignVehicleList(addAssignmentVehicleList);
        }
    }


    /**
     * 生成导入模板
     */
    @Override
    @MethodLog(name = "生成导入模板", description = "生成导入模板")
    public boolean generateTemplate(HttpServletResponse response) throws Exception {
        List<String> headList = new ArrayList<>();
        List<String> requiredList = new ArrayList<>();
        List<Object> exportList = new ArrayList<>();
        // 组装有下拉框的map
        Map<String, String[]> selectMap = new HashMap<>(16);
        // 表头
        headList.add("终端手机号");
        headList.add("原始机型");
        headList.add("设备标识(7位)");
        headList.add("设备密码");
        headList.add("监控对象");
        headList.add("监控对象类型");
        headList.add("所属组织");
        headList.add("群组(多个分组用逗号分隔)");
        headList.add("优先级");
        // 必填字段
        requiredList.add("终端手机号");
        requiredList.add("原始机型");
        requiredList.add("设备标识(7位)");
        requiredList.add("设备密码");
        requiredList.add("监控对象");
        requiredList.add("监控对象类型");
        requiredList.add("所属组织");
        requiredList.add("群组(多个分组用逗号分隔)");
        // 默认设置一条数据
        exportList.add("18600222931");
        exportList.add("4A11G");
        exportList.add("0002055");
        exportList.add("ik9u5hme");
        exportList.add("IW2055");
        exportList.add("人");
        // 查看当前用户所属企业及下级企业
        List<String> groupNames = userService.getOrgNamesByUser();
        if (CollectionUtils.isNotEmpty(groupNames)) {
            String[] groupNameArr = new String[groupNames.size()];
            groupNames.toArray(groupNameArr);
            selectMap.put("所属组织", groupNameArr);
            String groupName = groupNames.get(0);
            exportList.add(groupName);
        } else {
            exportList.add("zwkj");
        }
        exportList.add("分组名称@所属组织名称");
        exportList.add("1");

        String[] monitorType = { "车", "人", "物" };
        selectMap.put("监控对象类型", monitorType);
        // 功能类型
        String[] priority = { "1", "2", "3", "4", "5" };
        selectMap.put("优先级", priority);

        List<OriginalModelInfo> originalModelList = originalModelDao.findAllOriginalModelInfo();
        String[] modelIdList = new String[originalModelList.size()];
        for (int i = 0; i < originalModelList.size(); i++) {
            modelIdList[i] = originalModelList.get(i).getModelId();
        }
        selectMap.put("原始机型", modelIdList);

        ExportExcel export = new ExportExcel(headList, requiredList, selectMap);
        Row row = export.addRow();
        for (int j = 0; j < exportList.size(); j++) {
            export.addCell(row, j, exportList.get(j));
        }
        // 输出导文件
        OutputStream out;
        out = response.getOutputStream();
        export.write(out);
        out.close();
        return true;
    }


    /**
     * 监听定位绑定关系解绑
     * @param event 绑定关系解绑事件
     */
    @EventListener
    public void listenConfigEditEvent(ConfigUpdateEvent event) {
        //判断是否存在绑定关系
        String intercomJsonStr = event.getIntercomJsonStr();
        if (StringUtils.isBlank(intercomJsonStr)) {
            return;
        }

        //判断是否修改了监控对象、SIM卡以及分组，未修改，并且是修改关系而非解绑再绑定，则不需要修改原来对讲对象
        ConfigList oldConfig = event.getOldConfig();
        ConfigList curConfig = event.getNewConfig();

        //分组进行排序并比较
        List<String> oldAssignIds = Arrays.asList(oldConfig.getAssignmentId().split(","));
        List<String> newAssignIds = Arrays.asList(curConfig.getAssignmentId().split(","));
        Collections.sort(newAssignIds);
        Collections.sort(oldAssignIds);
        boolean isNotChangeAssign =
            Objects.equals(StringUtils.join(oldAssignIds, ","), StringUtils.join(newAssignIds, ","));

        if (Objects.equals(oldConfig.getVehicleId(), curConfig.getVehicleId()) && Objects
            .equals(oldConfig.getSimcardId(), curConfig.getSimcardId()) && isNotChangeAssign && Objects
            .equals(oldConfig.getConfigId(), curConfig.getConfigId())) {
            return;
        }

        //判断原来的对讲关系是否解绑（未修改监控对象不会触发删除监听，既不会解绑）
        String oldIntercomJsonStr =
            RedisHelper.getString(HistoryRedisKeyEnum.INTERCOM_LIST.of(oldConfig.getVehicleId()));
        //获取老的对讲对象
        IntercomObjectInfo oldInterObject = JSONObject.parseObject(intercomJsonStr, IntercomObjectInfo.class);
        if (StringUtils.isNotBlank(oldIntercomJsonStr)) {
            intercomObjectService.deleteIntercomObjects(Arrays.asList(oldInterObject), true);
        }

        //判断当前绑定关系是否还存在绑定关系，若不存在对讲群组，原来的对讲对象解绑为定位对象，不转换为
        List<Cluster> clusters = clusterDao.findAssignmentByMonitorId(curConfig.getVehicleId());
        if (clusters.isEmpty()) {
            return;
        }
        try {
            Long oldUsefId = oldInterObject.getUserId();
            //构建新的对讲对象关系
            IntercomObjectInfo intercomObject = buildInterObject(oldInterObject, curConfig);

            Set<String> assignmentIds = new HashSet<>();
            Set<String> assignmentNames = new HashSet<>();
            clusters.forEach(cluster -> {
                assignmentIds.add(cluster.getId());
                assignmentNames.add(cluster.getName());
            });
            //更新群组信息
            intercomObject.setAssignmentName(StringUtils.join(assignmentNames, ","));
            intercomObject.setAssignmentId(StringUtils.join(assignmentIds, ","));
            intercomObject.setCurrentGroupNum(assignmentIds.size());

            //添加对讲对象
            addTalkBack(intercomObject);

            //更新差异点
            InConfigInfoForm config1Form = new InConfigInfoForm();
            config1Form.setBrandID(intercomObject.getMonitorId());
            config1Form.setCitySelID(StringUtils.join(assignmentIds, ";"));
            config1Form.setMonitorType(intercomObject.getMonitorType());

            addConfigDiff(config1Form, false);

            //重新生成对讲对象
            if (oldUsefId != null) {
                intercomObjectService.addIntercomInfoToIntercomPlatform(curConfig.getConfigId(), "");
            }

        } catch (Exception e) {
            logger.error("修改对讲对象失败", e);
        }
    }

    /**
     * 构建新的对讲对象
     * @param oldIntercomObject oldIntercomObject
     * @param curConfig         curConfig
     * @return IntercomObjectInfo
     */
    private IntercomObjectInfo buildInterObject(IntercomObjectInfo oldIntercomObject, ConfigList curConfig)
        throws Exception {
        oldIntercomObject.setSimcardId(curConfig.getSimcardId());
        oldIntercomObject.setSimcardNumber(curConfig.getSimcardNumber());
        oldIntercomObject.setMonitorName(curConfig.getCarLicense());
        oldIntercomObject.setMonitorId(curConfig.getVehicleId());
        oldIntercomObject.setUserId(null);
        oldIntercomObject.setNumber(intercomCallNumberService.updateAndReturnPersonCallNumber());
        oldIntercomObject.setUpdateDataTime(new Date());
        oldIntercomObject.setStatus(IntercomObjectInfo.NOT_GENERATE_STATUS);
        oldIntercomObject.setDeviceId(curConfig.getDeviceId());
        oldIntercomObject.setDeviceNumber(curConfig.getDeviceNumber());
        oldIntercomObject.setConfigId(curConfig.getConfigId());
        oldIntercomObject.setGroupId(curConfig.getGroupId());
        oldIntercomObject.setGroupName(curConfig.getGroupName());
        return oldIntercomObject;
    }


    @Override
    public List<String> getAllAssignmentVehicleNumber(String id, int type, String monitorId) {
        List<String> assigmentIds = new ArrayList<>();
        if (type == 2) { //企业节点
            //获取当前用户
            UserLdap userId = SystemHelper.getCurrentUser();
            String uuid = userService.getUserUuidById(userId.getId().toString());
            //获取当前企业下的子企业
            List<OrganizationLdap> childGroup = userService.getOrgChild(id);
            List<String> groupList = new ArrayList<>();
            if (childGroup != null && !childGroup.isEmpty()) {
                for (OrganizationLdap group : childGroup) {
                    groupList.add(group.getUuid());
                }
            }
            //获取用户所有企业下用户有权限的分组
            List<Assignment> assignmentList = assignmentService.findUserAssignment(uuid, groupList);
            if (assignmentList != null && !assignmentList.isEmpty()) {
                for (Assignment anAssignmentList : assignmentList) {
                    assigmentIds.add(anAssignmentList.getId());
                }
            }
        } else { //分组节点
            assigmentIds.add(id);
        }
        if (assigmentIds.size() > 0) {
            // 获取所有分组id及分组下监控对象数量
            List<AssignmentInfo> ais = newVehicleDao.getAllAssignmentVehicleNumber(assigmentIds);

            //获取监控对象的分组
            List<String> monitorAis = new ArrayList<>();
            if (StringUtils.isNotBlank(monitorId)) {
                monitorAis = clusterDao.getAssignIdsForMonitor(monitorId);
            }
            // 还可存入的分组
            List<String> under = new ArrayList<String>();
            for (AssignmentInfo ai : ais) {
                if (ai.getVehicleNumber() < maxNumberAssignmentMonitor) {
                    under.add(ai.getId());
                    continue;
                }
                //若是修改的情况，可以等于监控数量上限
                if (monitorAis.contains(ai.getId()) && ai.getVehicleNumber() <= maxNumberAssignmentMonitor) {
                    under.add(ai.getId());
                }
            }
            return under;
        }
        return null;
    }
}