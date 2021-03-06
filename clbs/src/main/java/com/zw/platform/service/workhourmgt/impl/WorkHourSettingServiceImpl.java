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
 * ??????impl
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
                // ??????????????????????????????????????????
                String userId = userService.getCurrentUserUuid();
                // ?????????????????????????????????????????????
                List<String> userOrgListId = userService.getCurrentUserOrgIds();
                if (StringUtils.isNotBlank(userId) && CollectionUtils.isNotEmpty(userOrgListId)) {
                    page = PageHelperUtil.doSelect(query,
                        () -> workHourSettingDao.findVehicleWorkHourSetting(query, userId, userOrgListId));
                }
            }
            log.error("????????????--->????????????????????????????????????", e);
        }

        // ??????result??????groupId?????????groupName???result??????????????????
        setGroupNameByGroupId(page);
        return page;
    }

    private Page<WorkHourSettingInfo> getWorkHourSettingInfos(WorkHourSettingQuery query) throws InterruptedException {
        RedisSensorQuery redisQuery =
            new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                Integer.valueOf(query.getProtocol()));
        List<String> cacheIdList =
            redisVehicleService.getVehicleByType(redisQuery, RedisKeys.SensorType.SENSOR_WORK_HOUR_MONITOR);
        // ???????????????????????????Id????????????id
        if (cacheIdList == null) {
            throw new RedisException(">=======redis ???????????????===========<");
        }
        int total = cacheIdList.size();
        int curPage = query.getPage().intValue();// ?????????
        int pageSize = query.getLimit().intValue(); // ????????????
        int start = (curPage - 1) * pageSize;// ??????????????????
        int end = pageSize > (total - start) ? total : (pageSize * curPage);// ??????????????????
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
                // ??????????????????
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
     * ????????????list
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
        // ?????????????????????????????????????????????
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
        // ?????????????????????????????????????????????
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
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (StringUtils.isNotEmpty(userId) && CollectionUtils.isNotEmpty(orgList)) {
            
            list = workHourSettingDao.findWorkHourSettingVehicleByProtoclos(userId, orgList, protocols);
        }
        return list;
    }

    @Override
    public JsonResultBean addWorkHourSetting(WorkHourSettingForm form, String ipAddress) throws Exception {
        String message = "";
        // ?????????????????????????????????
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        String plateNumber = bindInfo.getName();
        String orgId = bindInfo.getOrgId();

        // ?????????2
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
            message = "????????????: " + plateNumber + "( @" + organizationLdap.getName() + ") ??????????????????";
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
     * ?????????????????????????????????
     * @param vehicleId vehicleId
     * @return string
     */
    private String getAddWorkHourRedis(String vehicleId) {
        // ??????redis??????
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
     * ???????????????
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
     * ?????????2
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
                    // ???????????????
                    workHourSettingInfo = list.get(0);
                } else if (list.size() == 2) {
                    //???????????????
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
     * ??????????????????????????????2???????????????????????????1???form
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
        // ?????????????????????????????????
        String vehicleId = form.getVehicleId();
        final BindDTO bindInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
        String plateNumber = bindInfo.getName();
        String orgId = bindInfo.getOrgId();
        WorkHourSettingInfo workHourSettingInfo = findVehicleWorkHourSettingByVid(vehicleId);
        // ????????????????????????????????????????????????????????????id????????????????????????;  1?????????????????????????????????????????????;
        if (Objects.isNull(workHourSettingInfo) || StringUtils.isBlank(workHourSettingInfo.getId())) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        // ?????????2
        WorkHourSettingForm twoForm = buildWorkHourSettingTwo(form);
        if (StringUtils.isNotBlank(form.getTwoId())) {
            twoForm.setId(form.getTwoId());
            twoForm.setUpdateDataUsername(SystemHelper.getCurrentUsername());
            twoForm.setSensorOutId("81");
        }
        form.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        form.setSensorOutId("80");
        String orgName = organizationService.getOrgNameByUuid(orgId);
        // ??????redis??????
        String redisValue = "";
        // ????????????
        String message = "";
        if (StringUtils.isBlank(workHourSettingInfo.getTwoId()) && StringUtils.isBlank(twoForm.getSensorId())) {
            // ??????????????????????????????,???????????????????????????????????????????????????
            boolean flag = workHourSettingDao.updateWorkHourSetting(form);
            if (flag) {
                message = "???????????? ???" + plateNumber + "( @" + orgName + ") ????????????????????????";
            }
            clearSendStatus(vehicleId, form.getId(), null);
            redisValue = getWorkHourBindForm(form);
        } else if (StringUtils.isBlank(workHourSettingInfo.getTwoId()) && StringUtils
            .isNotBlank(twoForm.getSensorId())) {
            // ????????????????????????????????????????????????????????????????????????????????????1?????????2??????
            boolean flag = workHourSettingDao.updateWorkHourSetting(form);
            twoForm.setCreateDataUsername(SystemHelper.getCurrentUsername());
            boolean flag1 = workHourSettingDao.addWorkHourSetting(twoForm);
            if (flag && flag1) {
                message = "???????????? ???" + plateNumber + "( @" + orgName + ") ????????????????????????1?????????,??????2?????????";
            }
            clearSendStatus(vehicleId, form.getId(), null);
            redisValue = getAddWorkHourRedis(vehicleId);
        } else if (StringUtils.isNotBlank(workHourSettingInfo.getTwoId()) && StringUtils
            .isNotBlank(twoForm.getSensorId())) {
            // ???????????????????????????????????????????????????????????????????????????????????????
            boolean flag = workHourSettingDao.updateWorkHourSetting(form);
            boolean flag1 = workHourSettingDao.updateWorkHourSetting(twoForm);
            if (flag && flag1) {
                message = "???????????? ???" + plateNumber + "( @" + orgName + ") ???????????????????????????????????????";
            }
            clearSendStatus(vehicleId, form.getId(), twoForm.getId());
            redisValue = getDoubleWorkHourBindForm(form, twoForm);
        } else if (StringUtils.isNotBlank(workHourSettingInfo.getTwoId()) && StringUtils
            .isBlank(twoForm.getSensorId())) {
            // ????????????????????????????????? ????????????????????????????????????????????????2??????????????????1
            boolean flag = workHourSettingDao.updateWorkHourSetting(form);
            boolean flag1 = workHourSettingDao.deleteWorkHourSetting(twoForm.getId());
            if (flag && flag1) {
                message = "???????????? ???" + plateNumber + "( @" + orgName + ") ????????????????????????1??????????????????2?????????";
            }
            clearSendStatus(vehicleId, form.getId(), twoForm.getId());
            redisValue = getWorkHourBindForm(form);
        }

        //??????redis??????
        RedisHelper.addToHash(RedisKeyEnum.WORK_HOUR_SETTING_MONITORY_LIST.of(), form.getVehicleId(), redisValue);
        if (StringUtils.isNotBlank(message)) {
            logSearchService.addLog(ipAddress, message, "3", "", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ???????????????????????????????????????
     * @param vehicleId   ??????id
     * @param engineId    ?????????1
     * @param twoEngineId ?????????2
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
            logSearchService.addLog(ipAddress, "??????????????????????????????", "3", "", "");
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
        // ???????????????1,????????????
        boolean flag1 = StringUtils.isNotBlank(vehicleWorkHourSetting.getId()) && StringUtils
            .isBlank(vehicleWorkHourSetting.getTwoId());
        // ?????????????????????2
        boolean flag2 = StringUtils.isNotBlank(vehicleWorkHourSetting.getId())
            && workHourSettingInfo.getSensorSequence() == WorkHourSettingInfo.SENSOR_SEQUENCE_TWO;
        // ????????????2,?????????????????????1,???????????????1??????????????????2???sensor_sequence ???0
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
            String message = "???????????? ???" + plateNumber + "( @" + groupName + ") ????????????????????????";
            logSearchService.addLog(ipAddress, message, "3", "", "");
        }
        return flag;
    }

    @Override
    public void sendWorkHourSetting(List<JSONObject> paramList, String ipAddress) throws Exception {
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        for (JSONObject obj : paramList) {
            // ??????
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

                    // ???????????? F3-8103-workHour0: ?????????1; F3-8103-workHour1: ?????????2
                    String paramType = "F3-8103-workHour" + sensorVehicle.getSensorSequence();
                    sendWorkHourSensor(paramId, vehicleId, paramType, sensorVehicleId, sensorVehicle, 0);
                    String plateNumber = sensorVehicle.getPlateNumber();
                    message.append("???????????? : ").append(plateNumber).append(" ??????????????????????????????").append(" <br/>");
                }

            }
        }
        if (StringUtils.isNotBlank(message.toString())) {
            if (paramList.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "????????????????????????????????????");
            }
        }
    }

    /**
     * ??????
     * @param paramId             ??????id
     * @param vehicleId           ??????id
     * @param paramType           ????????????
     * @param sensorVehicleId     ?????????????????????id
     * @param workHourSettingInfo workHourSettingInfo
     * @param mark                0:??????????????????/????????????; 1:??????????????????
     * @throws Exception ex
     */
    public String sendWorkHourSensor(String paramId, String vehicleId, String paramType, String sensorVehicleId,
        WorkHourSettingInfo workHourSettingInfo, int mark) throws Exception {
        // ???????????????????????????
        BindDTO vehicle = VehicleUtil.getBindInfoByRedis(vehicleId);
        String deviceNumber = vehicle.getDeviceNumber();
        // ?????????
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // ??????????????????
        if (msgSN != null) {
            // ?????????
            int status = 4;
            // ????????????
            // ??????????????????????????????: ????????????????????????????????????paramId?????????
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            // ??????????????????
            sensorService.sendWorkHourSensorParam(1, vehicle, workHourSettingInfo, msgSN, mark);
            // updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, sensorVehicleId);
        } else { // ???????????????
            int status = 5; // ???????????????
            msgSN = 0;
            // ??????????????????
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
        // ???????????????????????????
        sendHelper.deleteByVehicleIdParameterName(form.getVehicleId(), form.getId(), "3");
        boolean result = workHourSettingDao.updateWorkSettingBind(form);
        if (result) {
            // ??????????????????????????????
            //            addSensorVehicleCache(form);
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(form.getVehicleId());
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor = Converter.toBlank(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                String msg = "???????????????" + brand + "( @" + groupName + ") ????????????????????????";
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
        // ????????????
        JSONObject monitor = new JSONObject();
        monitor.put("id", monitorId);
        monitor.put("brand", brand);
        protocols.addAll(ProtocolEnum.getProtocols(Integer.valueOf(deviceType)));
        return monitor;
    }
}
