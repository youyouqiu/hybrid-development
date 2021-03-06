package com.zw.platform.service.basicinfo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.adas.domain.define.setting.AdasAlarmParamSetting;
import com.zw.adas.domain.define.setting.AdasCommonParamSetting;
import com.zw.adas.domain.define.setting.query.AdasParamSettingForm;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasAlarmJingParamSettingDao;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasCommonParamSettingDao;
import com.zw.platform.basic.constant.DeleteRedisKeyEnum;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.repository.NewVehicleDao;
import com.zw.platform.basic.service.CacheService;
import com.zw.platform.domain.core.CustomColumnConfigInfo;
import com.zw.platform.domain.riskManagement.RiskType;
import com.zw.platform.domain.riskManagement.form.RiskEventVehicleConfigForm;
import com.zw.platform.domain.riskManagement.form.RiskEventVehicleForm;
import com.zw.platform.domain.vas.oilmassmgt.OilVehicleSetting;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.core.CustomColumnDao;
import com.zw.platform.repository.modules.OBDManagerSettingDao;
import com.zw.platform.repository.vas.FluxSensorBindDao;
import com.zw.platform.repository.vas.MileageSensorDao;
import com.zw.platform.repository.vas.OilVehicleSettingDao;
import com.zw.platform.repository.vas.RiskEventConfigDao;
import com.zw.platform.repository.vas.SensorSettingsDao;
import com.zw.platform.repository.vas.VibrationSensorBindDao;
import com.zw.platform.service.basicinfo.InitRedisCacheService;
import com.zw.platform.service.basicinfo.TerminalTypeService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.common.CargoCommonUtils;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/9/16 9:22
 */
@Service
public class InitRedisCacheServiceImpl implements InitRedisCacheService {
    private static final Logger logger = LogManager.getLogger(InitRedisCacheServiceImpl.class);

    @Autowired
    CustomColumnDao customColumnDao;
    @Autowired
    AdasCommonParamSettingDao commonParamSettingDao;
    @Autowired
    AdasAlarmJingParamSettingDao jingParamSettingDao;
    @Autowired
    private NewVehicleDao newVehicleDao;
    @Autowired
    private OilVehicleSettingDao oilVehicleSettingDao;
    @Autowired
    private FluxSensorBindDao fluxSensorBindDao;
    @Autowired
    private VibrationSensorBindDao vibrationSensorBindDao;
    @Autowired
    private SensorSettingsDao sensorSettingsDao;

    @Autowired
    private MileageSensorDao mileageSensorDao;

    @Autowired
    private OBDManagerSettingDao obdManagerSettingDao;
    @Autowired
    private TerminalTypeService terminalTypeService;
    @Autowired
    private RiskEventConfigDao riskEventConfigDao;

    /**
     * ????????????????????????
     */
    @Value("${cargo.report.switch:false}")
    private boolean cargoReportSwitch;

    @Autowired
    private CacheService[] cacheServices;

    private void initRedis() {
        for (CacheService cacheService : cacheServices) {
            cacheService.initCache();
        }
        initVehicleBindingSensor();
        // ?????????????????????
        initTerminalType();
        //???????????????????????????????????????
        initRiskEventSetting();
        //?????????????????????????????????????????????
        initCustomColumn();
        //????????????????????????????????????????????????
        initParamSetting();
        //???????????????????????????
        if (cargoReportSwitch) {
            initCargoGroupVids();
        }

    }

    /**
     * ?????????????????????????????????
     */
    private void initParamSetting() {
        //????????????????????????????????????
        List<String> protocolTypeList = ProtocolTypeUtil.getAllProtocol();
        Map<RedisKey, String> paramSettingKeys = new HashMap<>();
        for (String protocolType : protocolTypeList) {
            RedisKey key = HistoryRedisKeyEnum.ADAS_PARAM_DEFAULT_PROTOCOL.of(protocolType);
            List<Object> list = new ArrayList<>();
            if (!ProtocolTypeUtil.BEI_JING_PROTOCOL_808_2019.equals(protocolType)) {
                List<AdasCommonParamSetting> common =
                    commonParamSettingDao.findDefaultCom(Integer.parseInt(protocolType));
                for (AdasCommonParamSetting commonParamSetting : common) {
                    AdasParamSettingForm adasParamSettingForm = new AdasParamSettingForm();
                    adasParamSettingForm.setCommonParamSetting(commonParamSetting);
                    List<AdasAlarmParamSetting> paramSettingForms = commonParamSettingDao
                        .findDefaultAlarm(commonParamSetting.getParamType(), Integer.parseInt(protocolType));
                    adasParamSettingForm.setAdasAlarmParamSettings(paramSettingForms);
                    list.add(adasParamSettingForm);
                }
            } else {
                list.addAll(jingParamSettingDao.findJingParamByVehicleId("default"));
            }
            paramSettingKeys.put(key, JSON.toJSONString(list));
        }
        RedisHelper.setStringMap(paramSettingKeys);
    }

    /**
     * ??????????????????????????????????????????
     */
    private void initCustomColumn() {
        Set<String> marks = customColumnDao.getAllMark();
        if (CollectionUtils.isEmpty(marks)) {
            return;
        }
        Set<RedisKey> customKeys = marks.stream().map(HistoryRedisKeyEnum.CUSTOM_COLUMN::of)
            .collect(Collectors.toSet());
        RedisHelper.delete(customKeys);
        Map<RedisKey, Collection<String>> customColumnConfigInfoListMap = new HashMap<>();
        for (String mark : marks) {
            List<CustomColumnConfigInfo> customColumnConfigInfoList =
                customColumnDao.findDefaultCustomConfigByMark(mark, CustomColumnConfigInfo.DEFAULT_COLUMN);
            if (CollectionUtils.isEmpty(customColumnConfigInfoList)) {
                continue;
            }
            List<String> customColumnConfigInfoListJson = new ArrayList<>();
            for (CustomColumnConfigInfo customColumnConfigInfo : customColumnConfigInfoList) {
                customColumnConfigInfoListJson.add(JSONObject.toJSONString(customColumnConfigInfo));
            }
            customColumnConfigInfoListMap
                .put(HistoryRedisKeyEnum.CUSTOM_COLUMN.of(mark), customColumnConfigInfoListJson);
        }
        RedisHelper.batchAddToList(customColumnConfigInfoListMap);
    }

    @Override
    public void initRiskEventSetting() {

        try {
            List<RiskEventVehicleConfigForm> defaultRiskSettingList =
                riskEventConfigDao.findRiskSettingByVid("default");
            if (defaultRiskSettingList.size() > 0) {

                RiskEventVehicleForm form = new RiskEventVehicleForm();
                List<String> parameterList = Lists.newLinkedList();
                Map<RedisKey, Map<String, String>> riskParamSetting = new HashMap<>();
                for (RiskEventVehicleConfigForm config : defaultRiskSettingList) {
                    int riskType = RiskType.getRiskType(config.getRiskId());
                    if (riskType != 0) {
                        String vehicleId = config.getVehicleId();
                        Map<String, String> data = form.initAndGetAssembleData(config);
                        RedisKey key = HistoryRedisKeyEnum.ADAS_PARAM_VEHICLE_RISK_ID.of(vehicleId, config.getRiskId());
                        riskParamSetting.put(key, data);
                        parameterList.add(CommonUtil.getAdasSetting(vehicleId, config.getRiskId(), data));
                    }
                }
                RedisHelper.batchAddToHash(riskParamSetting);
                //????????????????????????
                RedisHelper.setString(HistoryRedisKeyEnum.DEFAULT_RISK_EVENT_SETTING_STR.of(),
                    JSON.toJSONString(defaultRiskSettingList));
                if (parameterList.size() > 0) {
                    //?????????flink
                    ZMQFencePub.pubAdasRiskParam(JSON.toJSONString(parameterList));
                }

            } else {
                logger.info("???????????????????????????????????????????????????sql??????");
            }
        } catch (Exception e) {
            logger.error("???????????????????????????????????????????????????!", e);
        }
    }

    @Override
    public void clearAbandonedRedis() {
        for (DeleteRedisKeyEnum redisKeyEnum : DeleteRedisKeyEnum.values()) {
            if (redisKeyEnum.isDeleteByPattern()) {
                RedisHelper.delByPattern(redisKeyEnum.of());
            } else {
                RedisHelper.delete(redisKeyEnum.of());
            }
        }
    }

    @Override
    public void initCargoGroupVids() {

        try {
            List<Map<String, String>> allCargoGroupVids = newVehicleDao.getAllCargoGroupVehicleIds();

            if (CollectionUtils.isNotEmpty(allCargoGroupVids)) {
                String groupId;
                Set<String> vidSet;
                Map<String, Set<String>> groupVids = new HashMap<>();
                for (Map<String, String> groupVeh : allCargoGroupVids) {
                    groupId = groupVeh.get("GROUPID");
                    vidSet = groupVids.get(groupId);
                    vidSet = Optional.ofNullable(vidSet).orElse(new HashSet<>());
                    vidSet.add(groupVeh.get("VEHICLEID"));
                    groupVids.put(groupId, vidSet);
                }

                RedisHelper.deleteScanKeys(HistoryRedisKeyEnum.ORG_CARGO_VEHICLE_PATTERN.of());
                CargoCommonUtils.batchSetGroupCargoVids(groupVids);
            }

        } catch (Exception e) {
            logger.error("?????????????????????????????????", e);
        }

    }

    /**
     * ?????????????????????
     */
    private void initTerminalType() {
        terminalTypeService.queryTerminalTypeSaveToRedis();
    }

    // ????????????????????????????????????????????????????????? ??????????????????
    private void initVehicleBindingSensor() {

        // ???????????????
        addBindingSensor(oilVehicleSettingDao.findBindingOilBoxList(), RedisKeyEnum.VEHICLE_OIL_BOX_MONITOR_LIST);
        // ???????????????
        addBindingSensor(fluxSensorBindDao.findBindingOilMonitor(), RedisKeyEnum.VEHICLE_OIL_CONSUME_MONITOR_LIST);
        // ???????????????
        addBindingSensor(vibrationSensorBindDao.findBindingMonitor(), RedisKeyEnum.VEHICLE_SHOCK_MONITOR_LIST);
        // ???????????????
        addBindingSensor(mileageSensorDao.findBindingMonitor(new HashMap<>()),
            RedisKeyEnum.VEHICLE_MILEAGE_MONITOR_LIST);
        // ???????????????
        addBindingSensor(sensorSettingsDao.findBindingMonitorByType("1"),
            RedisKeyEnum.VEHICLE_TEMPERATURE_MONITOR_LIST);
        // ???????????????
        addBindingSensor(sensorSettingsDao.findBindingMonitorByType("2"), RedisKeyEnum.VEHICLE_WET_MONITOR_LIST);
        // ??????????????????
        addBindingSensor(sensorSettingsDao.findBindingMonitorByType("3"), RedisKeyEnum.VEHICLE_ROTATE_MONITOR_LIST);
        // ???????????????
        addBindingSensor(sensorSettingsDao.findBindingMonitorByType("4"), RedisKeyEnum.WORK_HOUR_SETTING_MONITORY_LIST);
        // ???????????????
        addBindingSensor(sensorSettingsDao.findBindingMonitorByType("6"), RedisKeyEnum.LOAD_SETTING_MONITORY_LIST);
        // OBD??????
        addBindingSensor(obdManagerSettingDao.findAllSetting(), RedisKeyEnum.OBD_SETTING_MONITORY_LIST);
    }

    /**
     * ???????????????????????????
     * @author wanxing
     */
    private <T> void addBindingSensor(List<T> list, RedisKeyEnum redisKeyEnum) {

        if (list != null && list.size() > 0) {
            RedisKey bindingOilBoxKey = redisKeyEnum.of();
            Map<String, String> newMap = new HashMap<>();
            String vehicleId;
            String type;

            for (T monitor : list) {
                if (monitor instanceof Map) {
                    Map<String, String> map = (Map<String, String>) monitor;
                    vehicleId = String.valueOf(map.get("id"));
                    type = String.valueOf(map.get("type"));
                    newMap.put(vehicleId, type);
                } else if (monitor instanceof OilVehicleSetting) {
                    OilVehicleSetting oilSetting = (OilVehicleSetting) monitor;
                    vehicleId = oilSetting.getId();
                    type = oilSetting.getType();
                    if (newMap.get(vehicleId) != null) {
                        newMap.put(vehicleId, newMap.get(vehicleId) + "," + type);
                    } else {
                        newMap.put(vehicleId, type);
                    }
                }
            }
            RedisHelper.addToHash(bindingOilBoxKey, newMap);
        }
    }

    @Override
    public void addCacheToRedis() {
        initRedis();
    }

    @Override
    public void initBindingSensor() {
        initVehicleBindingSensor();
    }
}
