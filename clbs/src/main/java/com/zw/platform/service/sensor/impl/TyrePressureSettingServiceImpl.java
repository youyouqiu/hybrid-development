package com.zw.platform.service.sensor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.RedisException;
import com.zw.platform.domain.basicinfo.TyrePressureParameter;
import com.zw.platform.domain.basicinfo.TyrePressureParameterForSend;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.basicinfo.form.TyrePressureSensorForm;
import com.zw.platform.domain.basicinfo.form.TyrePressureSettingForm;
import com.zw.platform.domain.basicinfo.query.TyrePressureSettingQuery;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.UploadTimeUtil;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.TyrePressureSensorDao;
import com.zw.platform.repository.vas.TyrePressureSettingDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sensor.TyrePressureSettingService;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.entity.t808.parameter.ParamItem;
import com.zw.ws.entity.t808.parameter.T808_0x8103;
import com.zw.ws.impl.SensorService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class TyrePressureSettingServiceImpl implements TyrePressureSettingService {

    private Logger logger = LogManager.getLogger(TyrePressureSettingServiceImpl.class);

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private RedisVehicleService redisVehicleService;

    @Autowired
    private ParamSendingCache paramSendingCache;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private TyrePressureSettingDao tyrePressureSettingDao;

    @Autowired
    private ParameterDao parameterDao;

    @Autowired
    private TyrePressureSensorDao tyrePressureSensorDao;

    @Autowired
    private NewVehicleDao newVehicleDao;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private SensorService sensorService;

    @Override
    public Page<TyrePressureSettingForm> getList(TyrePressureSettingQuery query) {
        Page<TyrePressureSettingForm> page = new Page<>();
        try {
            page = getTyrePressureSettingList(query);
        } catch (Exception e) {
            if (e instanceof RedisException) {
                // ??????????????????????????????????????????
                UserDTO currentUserInfo = userService.getCurrentUserInfo();
                String userDn = currentUserInfo.getId().toString();
                // ?????????????????????????????????????????????
                List<String> userOrgListId = organizationService.getOrgUuidsByUser(userDn);
                if (StringUtils.isNotBlank(userDn) && CollectionUtils.isNotEmpty(userOrgListId)) {
                    page = PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                            .doSelectPage(() -> tyrePressureSettingDao.findTyrePressureSetting(
                                    query, currentUserInfo.getUuid(), userOrgListId));
                }
            }
            logger.error("????????????--->????????????????????????????????????", e);
        }

        // ??????result??????groupId?????????groupName???result??????????????????
        setGroupNameByGroupId(page);
        return page;
    }

    @Override
    public TyrePressureSettingForm refreshSendStatus(String vid) {
        TyrePressureSettingQuery query = new TyrePressureSettingQuery();
        query.setVehicleId(vid);
        UserDTO currentUserInfo = userService.getCurrentUserInfo();
        String userDn = currentUserInfo.getId().toString();
        // ?????????????????????????????????????????????
        List<String> userOrgListId = organizationService.getOrgUuidsByUser(userDn);
        if (StringUtils.isNotBlank(userDn) && CollectionUtils.isNotEmpty(userOrgListId)) {
            Page<TyrePressureSettingForm> tyrePressureSetting = tyrePressureSettingDao
                .findTyrePressureSetting(query, currentUserInfo.getUuid(), userOrgListId);
            if (tyrePressureSetting.size() > 0) {
                TyrePressureSettingForm tyrePressureSettingForm = tyrePressureSetting.get(0);
                String paramType = "F3-8103-tyre";
                List<Directive> paramlist1 = parameterDao.findParameterByType(tyrePressureSettingForm.getVehicleId(),
                    tyrePressureSettingForm.getId() + paramType, paramType);
                Directive param1 = null;
                if (paramlist1 != null && paramlist1.size() > 0) {
                    param1 = paramlist1.get(0);
                }

                if (param1 != null) {
                    tyrePressureSettingForm.setParamId(param1.getId());
                    tyrePressureSettingForm.setStatus(param1.getStatus());
                }
                return tyrePressureSettingForm;
            }
        }
        return null;
    }

    @Override
    public List<TyrePressureSettingForm> findExistByVid(String vid) {
        return tyrePressureSettingDao.findExistByVid(vid);
    }

    @Override
    public JsonResultBean addTyrePressureSetting(TyrePressureSettingForm form) throws Exception {
        form.setCreateDataUsername(userService.getCurrentUserInfo().getUsername());
        boolean flag = tyrePressureSettingDao.addTyrePressureSetting(form);
        if (flag) {
            ZMQFencePub.pubChangeFence("21");
            setTyrePressureRedis(form);
            VehicleDTO vehicle = MonitorUtils.getVehicle(form.getVehicleId());
            String message = "??????????????????" + vehicle.getName() + "?????????????????????";
            logSearchService.addLog(getIpAddress(), message, "3", "", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public TyrePressureSettingForm findTyrePressureSettingById(String id) {
        return tyrePressureSettingDao.findTyrePressureSettingById(id);
    }

    @Override
    public JsonResultBean updateTyrePressureSetting(TyrePressureSettingForm form) throws Exception {
        form.setUpdateDataUsername(userService.getCurrentUserInfo().getUsername());
        boolean flag = tyrePressureSettingDao.updateTyrePressureSetting(form);
        if (flag) {
            ZMQFencePub.pubChangeFence("21");
            setTyrePressureRedis(form);
            clearStatus(form);
            VehicleDTO vehicle = MonitorUtils.getVehicle(form.getVehicleId());
            String message = "??????????????????" + vehicle.getName() + "?????????????????????";
            logSearchService.addLog(getIpAddress(), message, "3", "", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * ??????????????????
     */
    private void clearStatus(TyrePressureSettingForm form) throws Exception {
        String paramType = "F3-8103-tyre";
        sendHelper.deleteByVehicleIdParameterName(form.getVehicleId(), form.getId() + paramType, paramType);
    }

    @Override
    public JsonResultBean deleteTyrePressureSetting(String id) throws Exception {
        TyrePressureSettingForm form = tyrePressureSettingDao.findTyrePressureSettingById(id);
        if (form == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????????????????");
        }
        boolean flag = tyrePressureSettingDao.deleteTyrePressureSetting(id);
        if (flag) {
            ZMQFencePub.pubChangeFence("21");
            final List<String> ids = Collections.singletonList(form.getVehicleId());
            RedisHelper.hdel(RedisKeyEnum.TYRE_PRESSURE_MONITORY_LIST.of(), ids);
            VehicleDTO vehicle = MonitorUtils.getVehicle(form.getVehicleId());
            String message = "??????????????????" + vehicle.getName() + "???????????????????????????";
            logSearchService.addLog(getIpAddress(), message, "3", "", "", "");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean deleteMore(String ids) throws Exception {
        String[] moreId = ids.split(",");
        //?????????????????????id??????
        List<String> delList = new ArrayList<>();
        StringBuilder message = new StringBuilder("");
        for (String id : moreId) {
            TyrePressureSettingForm form = tyrePressureSettingDao.findTyrePressureSettingById(id);
            delList.add(form.getVehicleId());
            VehicleDTO vehicle = MonitorUtils.getVehicle(form.getVehicleId());
            message.append("??????????????????").append(vehicle.getName()).append("???????????????????????????</br>");
        }
        boolean flag = tyrePressureSettingDao.deleteMore(moreId);
        if (flag) {
            ZMQFencePub.pubChangeFence("21");
            RedisHelper.hdel(RedisKeyEnum.TYRE_PRESSURE_MONITORY_LIST.of(), delList);
            logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "??????????????????????????????");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public void sendSettingParam(List<JSONObject> list) throws Exception {
        StringBuilder msg = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (JSONObject object : list) {
                String vehicleId = object.getString("vehicleId");
                String paramId = object.getString("paramId");
                String id = object.getString("id");
                String paramType = "F3-8103-tyre";
                sendTyreParam(msg, vehicleId, paramId, id, paramType);
            }
        }
        if (msg.length() > 0) {
            if (list.size() > 1) {
                logSearchService.addLog(getIpAddress(), msg.toString(), "3", "batch", "??????????????????????????????");
            } else {
                VehicleDTO vehicle = MonitorUtils.getVehicle(list.get(0).getString("vehicleId"));
                logSearchService
                    .addLog(getIpAddress(), msg.toString().replace("</br>", ""), "3", "", vehicle.getName(),
                        vehicle.getPlateColor().toString());
            }
        }
    }

    private String sendTyreParam(StringBuilder msg, String vehicleId, String paramId, String id, String paramType)
        throws IllegalAccessException, InvocationTargetException {
        // ???????????????????????????
        VehicleDTO vehicleDTO = MonitorUtils.getVehicle(vehicleId);
        String deviceNumber = vehicleDTO.getDeviceNumber();
        // ?????????
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // ??????????????????
        if (msgSN != null) {
            // ?????????
            int status = 4;
            // ????????????
            // ??????????????????????????????: ????????????????????????????????????paramId?????????
            paramId = sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, id + paramType);
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSN);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 1);
            TyrePressureSettingForm form = tyrePressureSettingDao.findTyrePressureSettingById(id);

            TyrePressureParameterForSend tyrePressureParameterForSend = new TyrePressureParameterForSend();
            //??????????????????
            assemblyData(form, tyrePressureParameterForSend);

            T808_0x8103 benchmark = new T808_0x8103();
            ParamItem paramItem = new ParamItem();
            paramItem.setParamLength(56);
            paramItem.setParamId(0xF3E3);
            paramItem.setParamValue(tyrePressureParameterForSend);
            List<ParamItem> paramItems = new ArrayList<>();
            paramItems.add(paramItem);
            benchmark.setParamItems(paramItems);
            benchmark.setParametersCount(1);
            // ??????
            VehicleInfo vehicle = new VehicleInfo();
            vehicle.setDeviceId(vehicleDTO.getDeviceId());
            vehicle.setDeviceType(vehicleDTO.getDeviceType());
            vehicle.setSimcardNumber(vehicleDTO.getSimCardNumber());
            sensorService.getSubscibeInfo(vehicle, msgSN, benchmark, null);
            String userName = userService.getCurrentUserInfo().getUsername();
            paramSendingCache.put(userName, msgSN, vehicleDTO.getSimCardNumber(),
                SendTarget.getInstance(SendModule.TIRE_PRESSURE_MONITORING));
            msg.append("???????????????").append(vehicleDTO.getName()).append("??? ??????????????????????????????</br>");
        } else { // ???????????????
            int status = 5; // ???????????????
            msgSN = 0;
            // ??????????????????
            sendHelper.updateParameterStatus(paramId, msgSN, status, vehicleId, paramType, id + paramType);
        }
        return Converter.toBlank(msgSN.toString());
    }

    @Override
    public TyrePressureSettingForm findTyrePressureSettingByVid(String vid) {
        return tyrePressureSettingDao.findTyrePressureSettingByVid(vid);
    }

    @Override
    public JsonResultBean updateRoutineSetting(TyrePressureSettingForm form, String dealType)
        throws Exception {
        form.setCompensate(CompEnUtil.getCompEn(form.getCompensateStr()));
        form.setFilterFactor(FilterFactorUtil.getFilterFactor(form.getFilterFactorStr()));
        TyrePressureParameter tyrePressureParameter =
            JSON.parseObject(form.getTyrePressureParameterStr(), TyrePressureParameter.class);
        tyrePressureParameter
            .setAutomaticUploadTime(UploadTimeUtil.getUploadTime(tyrePressureParameter.getAutomaticUploadTimeStr()));
        form.setTyrePressureParameterStr(JSONObject.toJSONString(tyrePressureParameter));
        String userName = userService.getCurrentUserInfo().getUsername();
        if ("report".equals(dealType)) {
            tyrePressureSettingDao.updateTyrePressureSetting(form);
            // ?????????????????????
            TyrePressureSensorForm sensorForm = tyrePressureSensorDao.findSensorById(form.getSensorId());
            sensorForm.setCompensate(form.getCompensate());
            sensorForm.setFilterFactor(form.getFilterFactor());
            sensorForm.setUpdateDataUsername(userName);
            sensorForm.setUpdateDataTime(new Date());
            tyrePressureSensorDao.updateSensor(sensorForm);
        }
        String paramType = "F3-8103-tyre-read";
        StringBuilder msg = new StringBuilder();
        String msgId = sendTyreParam(msg, form.getVehicleId(), "", form.getId(), paramType);
        if (msg.length() > 0) {
            VehicleDTO vehicleDTO = MonitorUtils.getVehicle(form.getVehicleId());
            if (vehicleDTO != null) {
                String brand = vehicleDTO.getName();
                String plateColor = vehicleDTO.getPlateColor().toString();
                String groupName = vehicleDTO.getOrgName();
                String logMsg = "???????????????" + brand + "( @" + groupName + ")" + " ????????????????????????";
                logSearchService.addLog(getIpAddress(), logMsg, "3", "??????????????????", brand, plateColor);
            }
            JSONObject json = new JSONObject();
            json.put("msgId", msgId);
            json.put("userName", userName);
            return new JsonResultBean(JsonResultBean.SUCCESS, json.toJSONString());
        }
        return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
    }

    private void assemblyData(TyrePressureSettingForm form, TyrePressureParameterForSend tyrePressureParameterForSend) {
        TyrePressureParameter tyrePressureParameter =
            JSONObject.parseObject(form.getTyrePressureParameterStr(), TyrePressureParameter.class);
        tyrePressureParameterForSend.setAutomaticUploadTime(tyrePressureParameter.getAutomaticUploadTime());
        tyrePressureParameterForSend.setCompensatingEnable(form.getCompensate());
        tyrePressureParameterForSend.setCompensationFactorB(tyrePressureParameter.getCompensationFactorB());
        tyrePressureParameterForSend.setCompensationFactorK(tyrePressureParameter.getCompensationFactorK());
        tyrePressureParameterForSend.setElectricityThreshold(
            tyrePressureParameter.getElectricityThreshold() == null ? 0xFFFF :
                tyrePressureParameter.getElectricityThreshold());
        tyrePressureParameterForSend.setHeighPressure(
            tyrePressureParameter.getHeighPressure() == null ? 0xFFFF : tyrePressureParameter.getHeighPressure() * 10);
        tyrePressureParameterForSend.setLowPressure(
            tyrePressureParameter.getLowPressure() == null ? 0xFFFF : tyrePressureParameter.getLowPressure() * 10);
        tyrePressureParameterForSend.setPressure(
            tyrePressureParameter.getPressure() == null ? 0xFFFF : tyrePressureParameter.getPressure() * 10);
        tyrePressureParameterForSend.setPressureThreshold(
            tyrePressureParameter.getPressureThreshold() == null ? 0xFFFF :
                tyrePressureParameter.getPressureThreshold());
        tyrePressureParameterForSend.setSlowLeakThreshold(
            tyrePressureParameter.getSlowLeakThreshold() == null ? 0xFFFF :
                tyrePressureParameter.getSlowLeakThreshold());
        tyrePressureParameterForSend.setSmoothing(form.getFilterFactor());
        //???????????????????????????????????????
        tyrePressureParameterForSend.setHighTemperature(tyrePressureParameter.getHighTemperature() == null ? 0xFFFF :
            (tyrePressureParameter.getHighTemperature() * 10 + 2731));

        //?????????
        tyrePressureParameterForSend.setKeep1(new byte[11]);
        tyrePressureParameterForSend.setKeep2(new byte[20]);
    }

    /**
     * ??????????????????
     * @param form
     */
    private void setTyrePressureRedis(TyrePressureSettingForm form) {
        String sensorId = form.getSensorId();
        TyrePressureSensorForm tyrePressureSensorForm = tyrePressureSensorDao.findSensorById(sensorId);
        String value = tyrePressureSensorForm.getSensorNumber() + RedisKeys.SEPARATOR + sensorId;
        RedisHelper.addToHash(RedisKeyEnum.TYRE_PRESSURE_MONITORY_LIST.of(), form.getVehicleId(), value);
    }

    private Page<TyrePressureSettingForm> getTyrePressureSettingList(TyrePressureSettingQuery query)
        throws InterruptedException {
        RedisSensorQuery redisQuery =
            new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                Integer.valueOf(query.getProtocol()));
        List<String> cacheIdList =
            redisVehicleService.getVehicleByType(redisQuery, RedisKeys.SensorType.SENSOR_TYRE_PRESSURE_MONITOR);
        // ???????????????????????????Id
        if (cacheIdList == null) {
            throw new RedisException(">=======redis ???????????????===========<");
        }
        int total = cacheIdList.size();
        int curPage = query.getPage().intValue();// ?????????
        int pageSize = query.getLimit().intValue(); // ????????????
        int start = (curPage - 1) * pageSize;// ??????????????????
        int end = pageSize > (total - start) ? total : (pageSize * curPage);// ??????????????????
        List<String> queryList = cacheIdList.subList(start, end);
        List<TyrePressureSettingForm> resultList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(queryList)) {
            List<String> vehicleIds = new LinkedList<>();
            for (String item : queryList) {
                String[] items = item.split(RedisKeys.SEPARATOR);
                vehicleIds.add(items[0]);
            }
            resultList = tyrePressureSettingDao.findTyrePressureSettingByIds(vehicleIds);
            if (CollectionUtils.isNotEmpty(resultList)) {
                VehicleDTO vehicleDTO;
                for (TyrePressureSettingForm tyrePressureSettingForm : resultList) {
                    //????????????????????????
                    vehicleDTO = MonitorUtils.getVehicle(tyrePressureSettingForm.getVehicleId());
                    if (vehicleDTO != null) {
                        tyrePressureSettingForm.setGroupId(vehicleDTO.getOrgId());
                        //?????????????????????id???????????????????????????
                        if ("0".equals(tyrePressureSettingForm.getMonitorType())) {
                            tyrePressureSettingForm.setVehicleType(vehicleDTO.getVehicleTypeName());
                        } else if ("2".equals(tyrePressureSettingForm.getMonitorType())) {
                            tyrePressureSettingForm.setVehicleType("????????????");
                        }
                    }
                }
            }
            VehicleUtil.sort(resultList, vehicleIds);
        }
        return RedisQueryUtil.getListToPage(resultList, query, total);
    }

    private void setGroupNameByGroupId(Page<TyrePressureSettingForm> page) {
        List<OrganizationLdap> orgLdapList = organizationService.getAllOrganization();
        if (null != page && page.size() > 0) {
            for (TyrePressureSettingForm parameter : page) {
                TyrePressureParameter tyrePressureParameter =
                    JSONObject.parseObject(parameter.getTyrePressureParameterStr(), TyrePressureParameter.class);
                parameter.setTyrePressureParameter(tyrePressureParameter);
                String groupId = parameter.getGroupId();
                if (StringUtils.isNotBlank(groupId)) {
                    String groupName = "";
                    for (OrganizationLdap orgLdap : orgLdapList) {
                        if (Converter.toBlank(orgLdap.getUuid()).equals(groupId)) {
                            groupName = Converter.toBlank(orgLdap.getName());
                        }
                    }
                    parameter.setGroupName(groupName);
                }
                // ??????????????????
                if (StringUtils.isNotBlank(parameter.getVehicleId()) && StringUtils.isNotBlank(parameter.getId())) {
                    String paramType = "F3-8103-tyre";
                    List<Directive> paramlist1 = parameterDao
                        .findParameterByType(parameter.getVehicleId(), parameter.getId() + paramType, paramType);
                    Directive param1 = null;
                    if (paramlist1 != null && paramlist1.size() > 0) {
                        param1 = paramlist1.get(0);
                    }

                    if (param1 != null) {
                        parameter.setParamId(param1.getId());
                        parameter.setStatus(param1.getStatus());
                    }
                }
            }
        }
    }
}
