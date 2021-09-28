package com.zw.platform.service.workhourmgt.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.RedisException;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.domain.vas.workhourmgt.form.WorkHourSettingForm;
import com.zw.platform.domain.vas.workhourmgt.query.WorkHourSettingQuery;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.WorkHourSettingDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.workhourmgt.WorkHourSettingService;
import com.zw.platform.util.PageHelperUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.impl.SensorService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工时impl
 * @author zhouzongbo on 2018/5/28 16:24
 */
@Service
public class WorkHourSettingServiceImpl implements WorkHourSettingService {

    private static final Logger log = LogManager.getLogger(WorkHourSettingServiceImpl.class);

    @Autowired
    private RedisVehicleService redisVehicleService;

    @Autowired
    private UserService userService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private WorkHourSettingDao workHourSettingDao;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private SendHelper sendHelper;

    /**
     * @param query query
     * @return
     */
    @Override
    public Page<WorkHourSettingInfo> findWorkHourSettingList(WorkHourSettingQuery query) throws Exception {
        Page<WorkHourSettingInfo> page = new Page<>();
        try {
            page = getWorkHourSettingInfos(query);
        } catch (Exception e) {
            if (e instanceof RedisException) {
                // 如果缓存失败，从数据库中获取
                String userId = userService.getCurrentUserUuid();
                // 获取当前用户所属组织及下级组织
                List<String> userOrgListId = userService.getCurrentUserOrgIds();
                if (StringUtils.isNotBlank(userId) && CollectionUtils.isNotEmpty(userOrgListId)) {
                    page = PageHelperUtil.doSelect(query,
                        () -> workHourSettingDao.findVehicleWorkHourSetting(query, userId, userOrgListId));
                }
            }
            log.error("应用管理--->工时车辆管理分页查询失败", e);
        }

        // 处理result，将groupId对应的groupName给result相应的值赋上
        setGroupNameByGroupId(page);
        return page;
    }

    private Page<WorkHourSettingInfo> getWorkHourSettingInfos(WorkHourSettingQuery query) throws InterruptedException {
        RedisSensorQuery redisQuery =
            new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                Integer.valueOf(query.getProtocol()));
        List<String> cacheIdList =
            redisVehicleService.getVehicleByType(redisQuery, RedisKeys.SensorType.SENSOR_WORK_HOUR_MONITOR);
        // 从缓存中查询出车辆Id和发动机id
        if (cacheIdList == null) {
            throw new RedisException(">=======redis 缓存出错了===========<");
        }
        int total = cacheIdList.size();
        int curPage = query.getPage().intValue();// 当前页
        int pageSize = query.getLimit().intValue(); // 每页条数
        int start = (curPage - 1) * pageSize;// 遍历开始条数
        int end = pageSize > (total - start) ? total : (pageSize * curPage);// 遍历结束条数
        List<String> queryList = cacheIdList.subList(start, end);
        List<WorkHourSettingInfo> resultList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(queryList)) {
            List<String> vehicleIds = new LinkedList<>();
            List<String> engineIds = new LinkedList<>();
            for (String item : queryList) {
                String[] items = item.split(RedisKeys.SEPARATOR);
                vehicleIds.add(items[0]);
                if (items.length == 2) {
                    engineIds.add(items[1]);
                }
            }
            resultList = workHourSettingDao.findEngineVehicleByIds(vehicleIds, engineIds);
            VehicleUtil.sort(resultList, vehicleIds);
        }
        return RedisQueryUtil.getListToPage(resultList, query, total);
    }

    public void setGroupNameByGroupId(List<WorkHourSettingInfo> result) throws Exception {
        if (null != result && result.size() > 0) {
            Set<String> vids = result.stream().map(WorkHourSettingInfo::getVehicleId).collect(Collectors.toSet());
            Map<String, BindDTO> bindInfos = VehicleUtil.batchGetBindInfosByRedis(vids);
            for (WorkHourSettingInfo parameter : result) {
                BindDTO bindDTO = bindInfos.get(parameter.getVehicleId());
                if (bindDTO != null) {
                    parameter.setGroupName(bindDTO.getOrgName());
                }
                // 下发参数状态
                if (StringUtils.isNotBlank(parameter.getVehicleId()) && StringUtils.isNotBlank(parameter.getId())) {
                    String paramType = "F3-8103-workHour" + parameter.getSensorSequence();
                    List<Directive> paramlist1 =
                        parameterDao.findParameterByType(parameter.getVehicleId(), parameter.getId(), paramType);
                    Directive param1 = null;
                    if (paramlist1 != null && paramlist1.size() > 0) {
                        param1 = paramlist1.get(0);
                    }

                    if (param1 != null) {
                        parameter.setParamId(param1.getId());
                        parameter.setTransmissionParamId("");
                        parameter.setStatus(param1.getStatus());
                    }
                }
            }
        }
    }

    /**
     * 工时设置list
     * @param vehicleId
     * @return return
     * @throws Exception ex
     */
    @Override
    public WorkHourSettingInfo findWorkHourSettingByVid(String vehicleId) throws Exception {
        if (StringUtils.isEmpty(vehicleId)) {
            return null;
        }

        List<WorkHourSettingInfo> workHourSettingInfos = new ArrayList<>();
        WorkHourSettingQuery query = new WorkHourSettingQuery();
        query.setVehicleId(vehicleId);

        String userId = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (StringUtils.isNotBlank(userId) && orgList != null && orgList.size() > 0) {
            workHourSettingInfos = workHourSettingDao.findVehicleWorkHourSetting(query, userId, orgList);
        }
        if (workHourSettingInfos.size() == 0) {
            return null;
        }
        WorkHourSettingInfo workHourSettingInfo = workHourSettingInfos.get(0);

        if (StringUtils.isNotBlank(workHourSettingInfo.getVehicleId()) && StringUtils
            .isNotBlank(workHourSettingInfo.getId())) {
            String paramType = "F3-8103-workHour" + workHourSettingInfo.getSensorSequence();
            List<Directive> paramlist1 = parameterDao
                .findParameterByType(workHourSettingInfo.getVehicleId(), workHourSettingInfo.getId(), paramType);
            Directive param1 = null;
            if (paramlist1 != null && paramlist1.size() > 0) {
                param1 = paramlist1.get(0);
            }

            if (param1 != null) {
                workHourSettingInfo.setParamId(param1.getId());
                workHourSettingInfo.setTransmissionParamId("");
                workHourSettingInfo.setStatus(param1.getStatus());
            }
        }
        return workHourSettingInfo;
    }

    @Override
    public List<WorkHourSettingInfo> findReferenceVehicle() {
        List<WorkHourSettingInfo> list = new ArrayList<>();
        String userId = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (userId != null && !"".equals(userId) && orgList != null && orgList.size() > 0) {
            
            list = workHourSettingDao.findWorkHourSettingVehicle(userId, orgList, "1");
        }
        return list;
    }

    @Override
    public List<WorkHourSettingInfo> findReferenceVehicleByProtocols(List<Integer> protocols) {
        List<WorkHourSettingInfo> list = new ArrayList<>();
        String userId = userService.getCurrentUserUuid();
        // 获取当前用户所属组织及下级组织
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (StringUtils.isNotEmpty(userId) && CollectionUtils.isNotEmpty(orgList)) {
            
            list = workHourSettingDao.findWorkHourSettingVehicleByProtoclos(userId, orgList, protocols);
        }
        return list;
    }

    @Override
    public JsonResultBean addWorkHourSetting(WorkHourSettingForm form, String ipAddress) throws Exception {
        String message = "";
        // 从缓存中获取到车辆信息
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        String plateNumber = bindInfo.getName();
        String orgId = bindInfo.getOrgId();

        // 发动机2
        WorkHourSettingForm twoForm = null;
        if (StringUtils.isNotBlank(form.getTwoSensorId())
            && form.getTwoSensorSequence() == WorkHourSettingInfo.SENSOR_SEQUENCE_TWO) {
            twoForm = buildWorkHourSettingTwo(form);
            twoForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
        }
        form.setCreateDataUsername(SystemHelper.getCurrentUsername());
        form.setSensorOutId("80");
        boolean flag = workHourSettingDao.addWorkHourSetting(form);
        boolean flag1 = true;
        if (Objects.nonNull(twoForm)) {
            flag1 = workHourSettingDao.addWorkHourSetting(twoForm);
        }
        if (flag && flag1) {
            OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(orgId);
            message = "监控对象: " + plateNumber + "( @" + organizationLdap.getName() + ") 工时车辆设置";
            logSearchService.addLog(ipAddress, message, "3", "", "", "");
        }
        String value = getAddWorkHourRedis(form.getVehicleId());

        if (StringUtils.isNotBlank(value)) {
            RedisHelper.addToHash(RedisKeyEnum.WORK_HOUR_SETTING_MONITORY_LIST.of(), form.getVehicleId(), value);
        }
        if (StringUtils.isNotBlank(message)) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 新增时从数据库获取数据
     * @param vehicleId vehicleId
     * @return string
     */
    private String getAddWorkHourRedis(String vehicleId) {
        // 维护redis缓存
        List<WorkHourSettingInfo> works = workHourSettingDao.findWorkHourSettingByMonitorVid(vehicleId);
        String value = "";
        if (works.size() == 1) {
            value = getWorkHourBindInfo(works.get(0));
        } else if (works.size() == 2) {
            value = getWorkHourBindInfo(works.get(0)) + "," + getWorkHourBindInfo(works.get(1));
        }
        return value;
    }

    private String getWorkHourBindInfo(WorkHourSettingInfo info) {
        StringBuilder sb = new StringBuilder();
        return sb.append(info.getSensorNumber()).append(RedisKeys.SEPARATOR).append(info.getId()).toString();
    }

    private String getWorkHourBindForm(WorkHourSettingForm info) {
        StringBuilder sb = new StringBuilder();
        return sb.append(info.getSensorNumber()).append(RedisKeys.SEPARATOR).append(info.getId()).toString();
    }

    /**
     * 两个发动机
     * @param info    info
     * @param infoTwo infoTwo
     * @return string
     */
    private String getDoubleWorkHourBindForm(WorkHourSettingForm info, WorkHourSettingForm infoTwo) {
        StringBuilder sb = new StringBuilder();
        return sb.append(info.getSensorNumber()).append(RedisKeys.SEPARATOR).append(info.getId()).append(",")
            .append(infoTwo.getSensorNumber()).append(RedisKeys.SEPARATOR).append(infoTwo.getId()).toString();
    }

    /**
     * 发动机2
     * @param nowForm nowForm
     * @return WorkHourSettingForm
     */
    private WorkHourSettingForm buildWorkHourSettingTwo(WorkHourSettingForm nowForm) {
        WorkHourSettingForm form = new WorkHourSettingForm();
        form.setSensorId(nowForm.getTwoSensorId());
        form.setLastTime(nowForm.getTwoLastTime());
        form.setThresholdVoltage(nowForm.getTwoThresholdVoltage());
        form.setThresholdWorkFlow(nowForm.getTwoThresholdWorkFlow());
        form.setThresholdStandbyAlarm(nowForm.getTwoThresholdStandbyAlarm());
        form.setSensorSequence(nowForm.getTwoSensorSequence());
        form.setVehicleId(nowForm.getVehicleId());
        form.setSensorNumber(nowForm.getTwoSensorNumber());
        form.setSmoothingFactor(nowForm.getTwoSmoothingFactor());
        form.setBaudRateCalculateNumber(nowForm.getTwoBaudRateCalculateNumber());
        form.setBaudRateThreshold(nowForm.getTwoBaudRateThreshold());
        form.setBaudRateCalculateTimeScope(nowForm.getTwoBaudRateCalculateTimeScope());
        form.setSpeedThreshold(nowForm.getTwoSpeedThreshold());
        form.setThreshold(nowForm.getTwoThreshold());
        form.setSensorOutId("81");
        return form;
    }

    @Override
    public WorkHourSettingInfo findVehicleWorkHourSettingByVid(String vehicleId) {
        WorkHourSettingInfo workHourSettingInfo = null;
        if (StringUtils.isNotBlank(vehicleId)) {
            List<WorkHourSettingInfo> list = null;
            list = getWorkHourSettingInfos(vehicleId);

            if (CollectionUtils.isNotEmpty(list)) {
                if (list.size() == 1) {
                    // 单个发动机
                    workHourSettingInfo = list.get(0);
                } else if (list.size() == 2) {
                    //两个发动机
                    workHourSettingInfo = list.get(0);
                    WorkHourSettingInfo twoWorkHourSettingInfo = list.get(1);
                    buildDoubleWorkHourSetting(workHourSettingInfo, twoWorkHourSettingInfo);
                }
                return workHourSettingInfo;
            }
        }
        return null;
    }

    private List<WorkHourSettingInfo> getWorkHourSettingInfos(String vehicleId) {
        List<WorkHourSettingInfo> list;

        final String monitorType =
            RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "monitorType").get("monitorType");
        if ("0".equals(monitorType)) {
            list = workHourSettingDao.findVehicleWorkHourSettingByVid(vehicleId);
        } else if ("1".equals(monitorType)) {
            list = workHourSettingDao.findPeopleWorkHourSettingByVid(vehicleId);
        } else {
            list = workHourSettingDao.findThingWorkHourSettingByVid(vehicleId);
        }
        return list;
    }

    /**
     * 两个发动机则把发动机2的数据设置到发动机1的form
     * @param workHourSettingInfo    info1
     * @param twoWorkHourSettingInfo info2
     */
    private void buildDoubleWorkHourSetting(WorkHourSettingInfo workHourSettingInfo,
        WorkHourSettingInfo twoWorkHourSettingInfo) {
        workHourSettingInfo.setTwoId(twoWorkHourSettingInfo.getId());
        workHourSettingInfo.setTwoSensorId(twoWorkHourSettingInfo.getSensorId());
        workHourSettingInfo.setTwoSensorNumber(twoWorkHourSettingInfo.getSensorNumber());
        workHourSettingInfo.setTwoCompensate(twoWorkHourSettingInfo.getCompensate());
        workHourSettingInfo.setTwoOddEvenCheck(twoWorkHourSettingInfo.getOddEvenCheck());
        workHourSettingInfo.setTwoBaudRate(twoWorkHourSettingInfo.getBaudRate());
        workHourSettingInfo.setTwoFilterFactor(twoWorkHourSettingInfo.getFilterFactor());
        workHourSettingInfo.setTwoLastTime(twoWorkHourSettingInfo.getLastTime());
        workHourSettingInfo.setTwoThresholdVoltage(twoWorkHourSettingInfo.getThresholdVoltage());
        workHourSettingInfo.setTwoThresholdWorkFlow(twoWorkHourSettingInfo.getThresholdWorkFlow());
        workHourSettingInfo.setTwoThresholdStandbyAlarm(twoWorkHourSettingInfo.getThresholdStandbyAlarm());
        workHourSettingInfo.setTwoSensorSequence(twoWorkHourSettingInfo.getSensorSequence());
        workHourSettingInfo.setTwoDetectionMode(twoWorkHourSettingInfo.getDetectionMode());
        workHourSettingInfo.setTwoSmoothingFactor(twoWorkHourSettingInfo.getSmoothingFactor());
        workHourSettingInfo.setTwoBaudRateCalculateNumber(twoWorkHourSettingInfo.getBaudRateCalculateNumber());
        workHourSettingInfo.setTwoBaudRateThreshold(twoWorkHourSettingInfo.getBaudRateThreshold());
        workHourSettingInfo.setTwoBaudRateCalculateTimeScope(twoWorkHourSettingInfo.getBaudRateCalculateTimeScope());
        workHourSettingInfo.setTwoSpeedThreshold(twoWorkHourSettingInfo.getSpeedThreshold());
        workHourSettingInfo.setTwoThreshold(twoWorkHourSettingInfo.getThreshold());
    }

    @Override
    public JsonResultBean updateWorkHourSetting(WorkHourSettingForm form, String ipAddress) throws Exception {
        // 从缓存中获取到车辆信息
        String vehicleId = form.getVehicleId();
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        String plateNumber = bindInfo.getName();
        String orgId = bindInfo.getOrgId();
        WorkHourSettingInfo workHourSettingInfo = findVehicleWorkHourSettingByVid(vehicleId);
        // 因为此处可能存在两个发动机，因此根据车辆id查询绑定的发动机;  1发动机如果为空，表示数据不存在;
        if (Objects.isNull(workHourSettingInfo) || StringUtils.isBlank(workHourSettingInfo.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // 发动机2
        WorkHourSettingForm twoForm = buildWorkHourSettingTwo(form);
        if (StringUtils.isNotBlank(form.getTwoId())) {
            twoForm.setId(form.getTwoId());
            twoForm.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            twoForm.setSensorOutId("81");
        }
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        form.setSensorOutId("80");
        String orgName = organizationService.getOrgNameByUuid(orgId);
        // 维护redis数据
        String redisValue = "";
        // 日志信息
        String message = "";
        if (StringUtils.isBlank(workHourSettingInfo.getTwoId()) && StringUtils.isBlank(twoForm.getSensorId())) {
            // 如果之前有一个发动机,并且现在只传递一个发动机，那么修改
            boolean flag = workHourSettingDao.updateWorkHourSetting(form);
            if (flag) {
                message = "监控对象 ：" + plateNumber + "( @" + orgName + ") 工时车辆设置修改";
            }
            clearSendStatus(vehicleId, form.getId(), null);
            redisValue = getWorkHourBindForm(form);
        } else if (StringUtils.isBlank(workHourSettingInfo.getTwoId()) && StringUtils
            .isNotBlank(twoForm.getSensorId())) {
            // 如果之前有一个发动机，并且现在传递两个发动机，那么发动机1修改，2新增
            boolean flag = workHourSettingDao.updateWorkHourSetting(form);
            twoForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
            boolean flag1 = workHourSettingDao.addWorkHourSetting(twoForm);
            if (flag && flag1) {
                message = "监控对象 ：" + plateNumber + "( @" + orgName + ") 工时车辆设置修改1发动机,新增2发动机";
            }
            clearSendStatus(vehicleId, form.getId(), null);
            redisValue = getAddWorkHourRedis(vehicleId);
        } else if (StringUtils.isNotBlank(workHourSettingInfo.getTwoId()) && StringUtils
            .isNotBlank(twoForm.getSensorId())) {
            // 如果之前有两个发动机，并且现在传递两个发动机，那么同时修改
            boolean flag = workHourSettingDao.updateWorkHourSetting(form);
            boolean flag1 = workHourSettingDao.updateWorkHourSetting(twoForm);
            if (flag && flag1) {
                message = "监控对象 ：" + plateNumber + "( @" + orgName + ") 工时车辆设置修改两个发动机";
            }
            clearSendStatus(vehicleId, form.getId(), twoForm.getId());
            redisValue = getDoubleWorkHourBindForm(form, twoForm);
        } else if (StringUtils.isNotBlank(workHourSettingInfo.getTwoId()) && StringUtils
            .isBlank(twoForm.getSensorId())) {
            // 如果之前有两个发动机， 并且现在传递一个，那么删除发动机2，修改发动机1
            boolean flag = workHourSettingDao.updateWorkHourSetting(form);
            boolean flag1 = workHourSettingDao.deleteWorkHourSetting(twoForm.getId());
            if (flag && flag1) {
                message = "监控对象 ：" + plateNumber + "( @" + orgName + ") 工时车辆设置修改1发动机，删除2发动机";
            }
            clearSendStatus(vehicleId, form.getId(), twoForm.getId());
            redisValue = getWorkHourBindForm(form);
        }

        //维护redis数据
        RedisHelper.addToHash(RedisKeyEnum.WORK_HOUR_SETTING_MONITORY_LIST.of(), form.getVehicleId(), redisValue);
        if (StringUtils.isNotBlank(message)) {
            logSearchService.addLog(ipAddress, message, "3", "", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 删除工时设置原来的下发状态
     * @param vehicleId   车辆id
     * @param engineId    发动机1
     * @param twoEngineId 发动机2
     * @throws Exception ex
     */
    private void clearSendStatus(String vehicleId, String engineId, String twoEngineId) throws Exception {

        String paramType = "F3-8103-workHour0";

        if (StringUtils.isNotBlank(engineId)) {
            sendHelper.deleteByVehicleIdParameterName(vehicleId, engineId, paramType);
        }
        if (StringUtils.isNotBlank(twoEngineId)) {
            paramType = "F3-8103-workHour1";
            sendHelper.deleteByVehicleIdParameterName(vehicleId, twoEngineId, paramType);
        }
    }

    @Override
    public JsonResultBean deleteMoreWorkHourSettingBind(String ids, String ipAddress) throws Exception {
        String[] idArray = ids.split(",");
        boolean flag = false;
        for (String id : idArray) {
            flag = deleteWorkHourIsFlag(flag, id, 1, ipAddress);
        }

        if (flag) {
            logSearchService.addLog(ipAddress, "批量解绑工时车辆设置", "3", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteWorkHourSettingBind(String id, String ipAddress) throws Exception {
        boolean flag = deleteWorkHourIsFlag(false, id, 0, ipAddress);

        if (flag) {
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private boolean deleteWorkHourIsFlag(boolean flag, String id, int mark, String ipAddress) throws Exception {
        WorkHourSettingInfo workHourSettingInfo = workHourSettingDao.getSensorVehicleByBindId(id);
        if (Objects.isNull(workHourSettingInfo)) {
            return flag;
        }

        String vehicleId = workHourSettingInfo.getVehicleId();
        WorkHourSettingInfo vehicleWorkHourSetting = findVehicleWorkHourSettingByVid(vehicleId);
        if (Objects.isNull(vehicleWorkHourSetting)) {
            return flag;
        }
        // 只有发动机1,直接删除
        boolean flag1 = StringUtils.isNotBlank(vehicleWorkHourSetting.getId()) && StringUtils
            .isBlank(vehicleWorkHourSetting.getTwoId());
        // 直接删除发动机2
        boolean flag2 = StringUtils.isNotBlank(vehicleWorkHourSetting.getId())
            && workHourSettingInfo.getSensorSequence() == WorkHourSettingInfo.SENSOR_SEQUENCE_TWO;
        // 有发动机2,并且删除发动机1,删除发动机1后修改发动机2的sensor_sequence 为0
        boolean flag3 = StringUtils.isNotBlank(vehicleWorkHourSetting.getId()) && StringUtils
            .isNotBlank(vehicleWorkHourSetting.getTwoId())
            && workHourSettingInfo.getSensorSequence() == WorkHourSettingInfo.SENSOR_SEQUENCE_ONE;
        if (flag1 || flag2) {
            flag = workHourSettingDao.deleteWorkHourSettingById(id);
        } else if (flag3) {
            flag = workHourSettingDao.deleteWorkHourSettingById(id);
            flag = workHourSettingDao.updateSensorVehicleSensorSequence(vehicleWorkHourSetting.getTwoId());
        }

        redisVehicleService.delVehicleWorkHourBind(workHourSettingInfo.getVehicleId(), workHourSettingInfo.getId(),
            workHourSettingInfo.getSensorNumber());
        if (mark == 0 && flag) {
            final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(workHourSettingInfo.getVehicleId());
            String plateNumber = bindInfo.getName();
            String orgId = bindInfo.getOrgId();
            String groupName = organizationService.getOrgNameByUuid(orgId);
            String message = "监控对象 ：" + plateNumber + "( @" + groupName + ") 解绑工时车辆设置";
            logSearchService.addLog(ipAddress, message, "3", "", "");
        }
        return flag;
    }

    @Override
    public void sendWorkHourSetting(List<JSONObject> paramList, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        for (JSONObject obj : paramList) {
            // 工时
            String sensorVehicleId = "";
            vehicleId = "";
            String paramId = "";
            if (obj.get("sensorVehicleId") != null) {
                sensorVehicleId = obj.get("sensorVehicleId").toString();
            }
            if (obj.get("vehicleId") != null) {
                vehicleId = obj.get("vehicleId").toString();
            }
            if (obj.get("paramId") != null && !"".equals(obj.get("paramId"))) {
                paramId = obj.get("paramId").toString();
            }
            if (StringUtils.isNotBlank(sensorVehicleId) && StringUtils.isNotBlank(vehicleId)) {
                WorkHourSettingInfo sensorVehicle = workHourSettingDao.getSensorVehicleByBindId(sensorVehicleId);
                if (sensorVehicle != null) {

                    // 工时下发 F3-8103-workHour0: 发动机1; F3-8103-workHour1: 发动机2
                    String paramType = "F3-8103-workHour" + sensorVehicle.getSensorSequence();
                    sendWorkHourSensor(paramId, vehicleId, paramType, sensorVehicleId, sensorVehicle, 0);
                    String plateNumber = sensorVehicle.getPlateNumber();
                    message.append("监控对象 : ").append(plateNumber).append(" 下发工时车辆设置参数").append(" <br/>");
                }

            }
        }
        if (StringUtils.isNotBlank(message.toString())) {
            if (paramList.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "批量下发工时车辆设置参数");
            }
        }
    }

    /**
     * 下发
     * @param paramId             参数id
     * @param vehicleId           车辆id
     * @param paramType           参数类型
     * @param sensorVehicleId     车辆绑定传感器id
     * @param workHourSettingInfo workHourSettingInfo
     * @param mark                0:常规参数下发/参数下发; 1:基值修正下发
     * @throws Exception ex
     */
    public String sendWorkHourSensor(String paramId, String vehicleId, String paramType, String sensorVehicleId,
        WorkHourSettingInfo workHourSettingInfo, int mark) throws Exception {
        // 获取车辆及设备信息
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        // 序列号
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // 设备已经注册
        if (msgSN != null) {
            // 已下发
            int status = 4;
            // 下发参数
            // 先更新下发状态再下发: 避免车辆第一次下发不存在paramId的情况
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // 工时绑定下发
            sensorService.sendWorkHourSensorParam(1, vehicle, workHourSettingInfo, msgSN, mark);
            // updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
        } else { // 设备未注册
            int status = 5; // 设备为注册
            msgSN = 0;
            // 工时绑定下发
            sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
        }
        return Converter.toBlank(msgSN.toString());
    }

    @Override
    public WorkHourSettingInfo getSensorVehicleByBindId(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        WorkHourSettingInfo sensorVehicle = workHourSettingDao.getSensorVehicleByBindId(id);

        if (Objects.isNull(sensorVehicle)) {
            return null;
        }
        sensorVehicle.setBaudRateStr(BaudRateUtil.getBaudRateVal(Integer.valueOf(sensorVehicle.getBaudRate())));
        sensorVehicle.setCompensateStr(CompEnUtil.getCompEnVal(sensorVehicle.getCompensate()));
        sensorVehicle.setFilterFactorStr(FilterFactorUtil.getFilterFactorVal(sensorVehicle.getFilterFactor()));
        sensorVehicle
            .setOddEvenCheckStr(ParityCheckUtil.getParityCheckVal(Integer.valueOf(sensorVehicle.getOddEvenCheck())));
        sensorVehicle.setSensorPeripheralID("8" + sensorVehicle.getSensorSequence());
        return sensorVehicle;
    }

    @Override
    public JsonResultBean updateWorkSettingBind(WorkHourSettingForm form, String ipAddress) throws Exception {
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        // 清除原来的下发状态
        sendHelper.deleteByVehicleIdParameterName(form.getVehicleId(), form.getId(), "3");
        boolean result = workHourSettingDao.updateWorkSettingBind(form);
        if (result) {
            // 维护车和传感器的缓存
            //            addSensorVehicleCache(form);
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor = Converter.toBlank(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                String msg = "监控对象：" + brand + "( @" + groupName + ") 修改工时设置参数";
                logSearchService.addLog(ipAddress, msg, "3", "", brand, plateColor);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    public static JSONObject getSensorJSONObject(String id, List<Integer> protocols) {
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(id);
        if (Objects.isNull(bindInfo)) {
            return null;
        }

        String brand = bindInfo.getName();
        String monitorId = bindInfo.getId();
        String deviceType = bindInfo.getDeviceType();
        // 参考对象
        JSONObject monitor = new JSONObject();
        monitor.put("id", monitorId);
        monitor.put("brand", brand);
        protocols.addAll(ProtocolEnum.getProtocols(Integer.valueOf(deviceType)));
        return monitor;
    }
}
