package com.zw.platform.service.sensor.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.google.common.collect.Lists;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.service.IpAddressService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.query.SensorConfigQuery;
import com.zw.platform.domain.core.SendParam;
import com.zw.platform.domain.sendTxt.SensorParam;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.f3.Peripheral;
import com.zw.platform.domain.vas.f3.SensorConfig;
import com.zw.platform.domain.vas.f3.SensorPolling;
import com.zw.platform.push.cache.ParamSendingCache;
import com.zw.platform.push.cache.SendModule;
import com.zw.platform.push.cache.SendTarget;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.vas.SensorConfigDao;
import com.zw.platform.repository.vas.SensorPollingDao;
import com.zw.platform.service.core.F3SendStatusProcessService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.service.sensor.PeripheralService;
import com.zw.platform.service.sensor.SensorConfigService;
import com.zw.platform.service.systems.ParameterService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @author: nixiangqian
 * @date 2017???05???09??? 14:47
 */
@Service
public class SensorConfigServiceImpl implements SensorConfigService, IpAddressService {
    private static final Logger log = LogManager.getLogger(SensorConfigServiceImpl.class);

    @Autowired
    private SensorConfigDao sensorConfigDao;

    @Autowired
    private SensorPollingDao sensorPollingDao;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private PeripheralService peripheralService;

    @Autowired
    private UserService userService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private F3SendStatusProcessService f3SendStatusProcessService;

    @Autowired
    private ParamSendingCache paramSendingCache;

    @Value("${set.success}")
    private String setSuccess;

    @Override
    public Page<SensorConfig> findByPage(SensorConfigQuery query) throws Exception {
        String orgId = query.getGroupId();
        String assignmentId = query.getAssignmentId();
        String simpleQueryParam = query.getSimpleQueryParam();
        Integer protocol = query.getProtocol();
        String deviceType = protocol != null ? protocol.toString() : null;
        List<String> vehicleList =
            userService.getValidVehicleId(orgId, assignmentId, deviceType, simpleQueryParam, null, true);
        Page<SensorConfig> page = new Page<>();
        int listSize = vehicleList.size();
        // ?????????
        int curPage = query.getPage().intValue();
        // ????????????
        int pageSize = query.getLimit().intValue();
        // ??????????????????
        int lst = (curPage - 1) * pageSize;
        // ????????????
        int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
        List<String> pageVehicleIdList = vehicleList.subList(lst, ps);

        // ???????????????
        if (CollectionUtils.isNotEmpty(pageVehicleIdList)) {
            List<SensorConfig> sensorConfigList = sensorConfigDao.findAllVehicle(pageVehicleIdList);
            // ??????????????????????????????
            VehicleUtil.sort(sensorConfigList, pageVehicleIdList);
            page = RedisQueryUtil.getListToPage(sensorConfigList, query, listSize);
        }
        setPollingSet(page);
        return page;
    }

    @Override
    public SensorConfig getSendWebSocket(String vehicleId) {
        if (StringUtils.isEmpty(vehicleId)) {
            return null;
        }
        SensorConfigQuery query = new SensorConfigQuery();
        query.setVehicleId(vehicleId);

        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        String userUuidById = userService.getCurrentUserUuid();
        Page<SensorConfig> byPage = sensorConfigDao.findByPage(query, userUuidById, orgList);
        if (byPage.size() == 0) {
            return null;
        }
        SensorConfig sensorConfig = byPage.get(0);
        if (sensorConfig != null) {
            List<SensorPolling> sensorPollingList = sensorPollingDao.findByVehicleId(sensorConfig.getVehicleId());
            if (CollectionUtils.isEmpty(sensorPollingList)) {
                return sensorConfig;
            }
            sensorConfig.setPollings(sensorPollingList);
            sensorConfig.setPollingTime(String.valueOf(sensorPollingList.get(0).getPollingTime()));

            List<Directive> directiveList = parameterService
                .findParameterByType(sensorConfig.getVehicleId(), sensorConfig.getId(), "0x8900-0xFA");
            Directive param = null;
            if (CollectionUtils.isNotEmpty(directiveList)) {
                param = directiveList.get(0);
            }
            if (param != null) {
                final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sensorConfig.setSendParamId(param.getId());
                sensorConfig.setSendStatus(param.getStatus());
                sensorConfig.setRemark(param.getRemark());
                sensorConfig.setSendTime(format.format(param.getDownTime()));
            }
        }
        return sensorConfig;
    }

    private void setPollingSet(Page<SensorConfig> sensorConfigs) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SensorConfig sensorConfig : sensorConfigs) {
            String vehicleId = sensorConfig.getVehicleId();
            List<SensorPolling> sensorPollingList = sensorPollingDao.findByVehicleId(vehicleId);
            sensorConfig.setPollings(sensorPollingList);
            if (CollectionUtils.isEmpty(sensorPollingList)) {
                continue;
            }
            StringBuilder names = new StringBuilder();
            int i = 0;
            for (SensorPolling sensorPolling : sensorPollingList) {
                if (i == 0) {
                    sensorConfig.setPollingTime(String.valueOf(sensorPolling.getPollingTime()));
                }
                names.append(sensorPolling.getPollingName()).append("<br/>");
                i++;
            }
            String parameterName = sensorConfig.getId();
            List<Directive> directives =
                parameterService.findParameterByType(vehicleId, parameterName, "0x8900-0xFA");
            Directive param = null;
            if (CollectionUtils.isNotEmpty(directives)) {
                param = directives.get(0);
            }
            if (param != null) {
                sensorConfig.setSendParamId(param.getId());
                sensorConfig.setSendStatus(param.getStatus());
                sensorConfig.setRemark(param.getRemark());
                sensorConfig.setSendTime(format.format(param.getDownTime()));
            }
            sensorConfig.setPollingNames(names.toString());
        }
    }

    @Override
    public SensorConfig findById(String id) throws Exception {
        return this.sensorConfigDao.findById(id);
    }

    @Override
    public SensorConfig findByVehicleId(String vehicleId) {
        SensorConfig sc = this.sensorConfigDao.findByVehicleId(vehicleId);
        if (sc != null) {
            List<SensorPolling> spList = this.sensorPollingDao.findByVehicleId(vehicleId);
            sc.setPollings(spList);
        }
        return sc;
    }

    @Override
    public void addSensorConfig(SensorConfig sensorConfig, String vehicleId) {
        this.sensorConfigDao.add(sensorConfig);
        List<SensorPolling> sensorPollingList = sensorConfig.getPollings();
        for (SensorPolling sensorPolling : sensorPollingList) {
            sensorPolling.setCreateDataUsername(sensorConfig.getCreateDataUsername());
            sensorPolling.setConfigId(sensorConfig.getId());
        }
        // ?????????????????????????????????
        sensorPollingDao.addByBatch(sensorPollingList);
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO != null) {
            String brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            String plateColor = plateColorInt == null ? "" : plateColorInt.toString();
            String msg = "???????????? : " + brand + " ????????????????????????";
            logSearchService.addLog(getIpAddress(), msg, "3", "??????????????????", brand, plateColor);
        }
    }

    @Override
    public List<SensorConfig> findVehicleSensorSetting(List<Integer> protocols) {
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        // ????????????uuid
        String currentUserUuid = userService.getCurrentUserUuid();
        return this.sensorConfigDao.findVehicleSensorSetting(currentUserUuid, orgList, protocols);
    }

    @Override
    public void updateSensorConfig(SensorConfig sensorConfig, String vehicleId) {
        // ???????????????????????????
        sendHelper.deleteByVehicleIdParameterName(sensorConfig.getVehicleId(), sensorConfig.getId(), "0x8900-0xFA");
        this.sensorConfigDao.updateSensorConfig(sensorConfig);
        // ?????????????????????????????????
        sensorPollingDao.deleteByConfigId(sensorConfig.getId());
        // ?????????????????????????????????
        sensorPollingDao.addByBatch(sensorConfig.getPollings());
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO != null) {
            String brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            String plateColor = plateColorInt == null ? "" : plateColorInt.toString();
            String msg = "???????????? : " + brand + " ????????????????????????";
            logSearchService.addLog(getIpAddress(), msg, "3", "??????????????????", brand, plateColor);
        }
    }

    /**
     * ????????????????????????
     */
    public void deleteByVehicleId(List<String> monitorIds) {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return;
        }
        if (monitorIds.size() > 1) {
            String vehicleId = monitorIds.get(0);
            SensorConfig sensorConfig = sensorConfigDao.findByVehicleId(vehicleId);
            if (sensorConfig != null) {
                sensorPollingDao.deleteByConfigId(sensorConfig.getId());
            }
            this.sensorConfigDao.deleteByVechileId(vehicleId);
            RedisHelper.delete(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        } else {
            List<String> sensorConfigList = sensorConfigDao.findSensorConfigIdByVehicleId(monitorIds);
            if (CollectionUtils.isNotEmpty(sensorConfigList)) {
                sensorPollingDao.deleteBatchByConfigId(sensorConfigList);
            }
            sensorConfigDao.deleteBatchByVechileId(monitorIds);
            // ????????????????????????
            RedisHelper.delete(HistoryRedisKeyEnum.SENSOR_MESSAGE.ofs(monitorIds));
        }
    }

    @Override
    public JsonResultBean deleteByVehicleId(String vehicleId) {
        SensorConfig sc = this.findByVehicleId(vehicleId);
        if (sc != null) {
            sensorPollingDao.deleteByConfigId(sc.getId());
        }
        this.sensorConfigDao.deleteByVechileId(vehicleId);
        RedisHelper.delete(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO != null) {
            String brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            String plateColor = plateColorInt != null ? plateColorInt.toString() : "";
            String msg = "???????????? ??? " + brand + " ??????????????????????????????";
            logSearchService.addLog(getIpAddress(), msg, "3", "??????????????????", brand, plateColor);
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        deleteByVehicleId(monitorIds);
    }

    @Override
    public JsonResultBean deleteBatchByVehicleId(List<String> ids) {
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(ids, Lists.newArrayList("name"));
        Map<String, SensorConfig> sensorConfigMap = sensorConfigDao.findByVehicleIds(ids)
            .stream()
            .collect(Collectors.toMap(SensorConfig::getVehicleId, Function.identity()));
        Set<String> needDelSensorPollingIds = new HashSet<>();
        StringBuilder message = new StringBuilder();
        for (String id : ids) {
            BindDTO bindDTO = bindInfoMap.get(id);
            // ??????????????????????????????
            SensorConfig sensorConfig = sensorConfigMap.get(id);
            if (sensorConfig == null || bindDTO == null) {
                continue;
            }
            needDelSensorPollingIds.add(sensorConfig.getId());
            message.append("???????????? ??? ").append(bindDTO.getName()).append(" ????????????????????????").append(" <br/>");
        }
        sensorPollingDao.batchDeleteByIds(needDelSensorPollingIds);
        // ????????????
        boolean flag = sensorConfigDao.deleteBatchByVechileId(ids);
        if (flag) {
            logSearchService.addLog(getIpAddress(), message.toString(), "3", "batch", "??????????????????????????????");
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    @Override
    public JsonResultBean sendParam(ArrayList<JSONObject> paramList) {
        if (paramList == null || paramList.size() == 0) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        Set<String> vehicleIds =
            paramList.stream().map(obj -> obj.get("vehicleId").toString()).collect(Collectors.toSet());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
        List<RedisKey> needDelRedisKeys = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        String brand = "";
        String plateColor = "";
        for (JSONObject obj : paramList) {
            // ????????????????????????????????????????????????
            String vehicleId = obj.get("vehicleId").toString();
            needDelRedisKeys.add(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
            SensorConfig sensorConfig = this.findByVehicleId(vehicleId);
            if (sensorConfig == null || CollectionUtils.isEmpty(sensorConfig.getPollings())) {
                continue;
            }
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO != null) {
                brand = bindDTO.getName();
                Integer plateColorInt = bindDTO.getPlateColor();
                plateColor = plateColorInt == null ? "" : plateColorInt.toString();
            }
            List<SensorParam> sensorParams = new ArrayList<>();
            for (SensorPolling sensorPolling : sensorConfig.getPollings()) {
                Peripheral peripheral = peripheralService.findById(sensorPolling.getSensorType());
                SensorParam sensorParam = new SensorParam();
                sensorParam
                    .setPeripheralID(Integer.parseInt(peripheral.getIdentId().toLowerCase().replace("0x", ""), 16));
                sensorParam.setPollingTime(sensorPolling.getPollingTime());
                sensorParam.setPeripheralMsgLen(2);
                sensorParam.setDataMsgLen(peripheral.getMsgLength() == null ? 0 : peripheral.getMsgLength());
                sensorParams.add(sensorParam);
            }
            String parameterName = sensorConfig.getId();
            // ????????????????????????
            sendSensorConfig(vehicleId, bindDTO, parameterName, sensorParams, username);
            message.append("???????????? : ").append(brand).append(" ???????????????????????? ");
        }
        RedisHelper.delete(needDelRedisKeys);
        String ip = getIpAddress();
        if (paramList.size() == 1) {
            logSearchService.addLog(ip, message.toString(), "2", "", brand, plateColor);
        } else {
            logSearchService.addLog(ip, message.toString(), "2", "batch", "??????????????????????????????");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, setSuccess);
    }

    /**
     * ????????????
     * @param vehicleId     ???id
     * @param bindDTO       ????????????
     * @param parameterName ????????????id
     * @param username      ?????????
     */
    private void sendSensorConfig(String vehicleId, BindDTO bindDTO, String parameterName,
        List<SensorParam> sensorParams, String username) {
        if (bindDTO == null) {
            return;
        }
        String deviceNumber = bindDTO.getDeviceNumber();
        String deviceId = bindDTO.getDeviceId();
        String deviceType = bindDTO.getDeviceType();
        String simCardNumber = bindDTO.getSimCardNumber();
        // ?????????
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // ?????????????????????????????????
        String paramId = sendHelper.getLastSendParamID(vehicleId, parameterName, "0x8900-0xFA");
        if (msgSno != null) {
            paramId = sendHelper
                .updateParameterStatusAndRemark(paramId, msgSno, 4, vehicleId, "0x8900-0xFA", parameterName, null);
            // ????????????
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSno);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 2);

            // ??????
            SubscibeInfo info = new SubscibeInfo(username, deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
            SubscibeInfoCache.getInstance().putTable(info);
            sendTxtService.setSensorPolling(simCardNumber, msgSno, sensorParams, deviceId, deviceType);
            //???????????????????????????????????????,?????????????????????????????????,?????????????????????????????????websocket??????
            paramSendingCache
                .put(username, msgSno, simCardNumber, SendTarget.getInstance(SendModule.PERIPHERALS_POLLING));
            // ???????????????
        } else {
            msgSno = 0;
            sendHelper
                .updateParameterStatusAndRemark(paramId, msgSno, 5, vehicleId, "0x8900-0xFA", parameterName, null);
        }
    }

    @Override
    public JsonResultBean sendClearPolling(ArrayList<JSONObject> paramList) {
        if (paramList == null || paramList.size() == 0) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        String username = SystemHelper.getCurrentUsername();
        Set<String> vehicleIds =
            paramList.stream().map(obj -> obj.get("vehicleId").toString()).collect(Collectors.toSet());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(vehicleIds);
        List<RedisKey> needDelRedisKeys = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        String brand = "";
        String plateColor = "";
        for (JSONObject obj : paramList) {
            // ????????????????????????????????????????????????
            String vehicleId = obj.get("vehicleId").toString();
            // ???????????????????????????,????????????
            needDelRedisKeys.add(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(vehicleId));
            BindDTO bindDTO = bindInfoMap.get(vehicleId);
            if (bindDTO == null) {
                continue;
            }
            SensorConfig sensorConfig = this.findByVehicleId(vehicleId);
            if (sensorConfig == null || CollectionUtils.isEmpty(sensorConfig.getPollings())) {
                continue;
            }
            brand = bindDTO.getName();
            Integer plateColorInt = bindDTO.getPlateColor();
            plateColor = plateColorInt == null ? "" : plateColorInt.toString();

            String parameterName = sensorConfig.getId();
            // ??????????????????
            sendClearSensorConfig(vehicleId, bindDTO, parameterName, username);
            message.append("???????????? : ").append(brand).append(" ?????????????????? ");
        }
        RedisHelper.delete(needDelRedisKeys);
        String ip = getIpAddress();
        if (paramList.size() == 1) {
            logSearchService.addLog(ip, message.toString(), "2", "", brand, plateColor);
        } else {
            logSearchService.addLog(ip, message.toString(), "2", "batch", "????????????????????????");
        }
        return new JsonResultBean(JsonResultBean.SUCCESS, setSuccess);
    }

    @Override
    public Directive getDirectiveStatus(String vehicleId, Integer swiftNumber) {
        return sensorConfigDao.getDirectiveStatus(vehicleId, swiftNumber);
    }

    private void sendClearSensorConfig(String vehicleId, BindDTO bindDTO, String parameterName, String username) {
        String deviceNumber = bindDTO.getDeviceNumber();
        String deviceId = bindDTO.getDeviceId();
        String deviceType = bindDTO.getDeviceType();
        String simCardNumber = bindDTO.getSimCardNumber();
        // ?????????
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        // ?????????????????????????????????
        String paramId = sendHelper.getLastSendParamID(vehicleId, parameterName, "0x8900-0xFA");
        if (msgSno != null) {
            paramId = sendHelper
                .updateParameterStatusAndRemark(paramId, msgSno, 4, vehicleId, "0x8900-0xFA", parameterName, "0xFA00");
            // ????????????
            SendParam sendParam = new SendParam();
            sendParam.setMsgSNACK(msgSno);
            sendParam.setParamId(paramId);
            sendParam.setVehicleId(vehicleId);
            f3SendStatusProcessService.updateSendParam(sendParam, 2);

            // ??????
            SubscibeInfo info = new SubscibeInfo(username, deviceId, msgSno, ConstantUtil.T808_DEVICE_GE_ACK);
            SubscibeInfoCache.getInstance().putTable(info);
            paramSendingCache
                .put(username, msgSno, simCardNumber, SendTarget.getInstance(SendModule.PERIPHERALS_POLLING));
            sendTxtService.setSensorPolling(simCardNumber, msgSno, new ArrayList<>(), deviceId, deviceType);
            // ???????????????
        } else {
            msgSno = 0;
            sendHelper
                .updateParameterStatusAndRemark(paramId, msgSno, 5, vehicleId, "0x8900-0xFA", parameterName, "0xFA00");
        }
    }

}
