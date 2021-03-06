package com.zw.platform.util.spring;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.adas.domain.define.setting.AdasPlatformParamSetting;
import com.zw.adas.domain.riskManagement.AdasRiskEvent;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasAlarmJingParamSettingDao;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasAlarmParamSettingDao;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventDao;
import com.zw.lkyw.domain.ReportConstant;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.domain.taskjob.TaskJobForm;
import com.zw.platform.repository.core.ResourceDao;
import com.zw.platform.repository.vas.DeviceUpgradeDao;
import com.zw.platform.util.QuartzManager;
import com.zw.platform.util.common.ConcurrentHashSet;
import com.zw.platform.util.common.Function;
import com.zw.platform.util.common.ZipUtil;
import com.zw.ws.entity.adas.EnableClassOrder;
import com.zw.ws.entity.adas.EnableOrder;
import com.zw.ws.entity.adas.paramSetting.ZhongWeiParamSettingUtil;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import com.zw.ws.entity.vehicle.VehiclePositionalInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InitData implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * spring????????????????????????????????????
     */

    public static final Map<String, VehiclePositionalInfo> vehiclePositionalInfo = new ConcurrentHashMap<>();

    public static Map<Integer, String> areaNamesMap = Maps.newHashMap();
    public static Map<String, Map<String, AdasPlatformParamSetting>> platformParamMap = new ConcurrentHashMap<>();
    public static Map<Integer, AdasRiskEvent> riskEventMap = Maps.newHashMap();
    public static Map<String, Set<String>> automaticVehicleMap = new ConcurrentHashMap<>();
    private static Logger log = LogManager.getLogger(ApplicationListener.class);

    @Autowired
    QuartzManager quartzManager;
    @Autowired
    AdasAlarmParamSettingDao alarmParamDao;
    @Autowired
    AdasAlarmJingParamSettingDao jingParamSettingDao;
    @Autowired
    private DeviceUpgradeDao deviceUpgradeDao;
    @Autowired
    private AdasRiskEventDao adasRiskEventDao;
    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private MonitorIconService monitorIconService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        //??????????????????(??????spring???listener??????????????????????????????spring-servlet????????????????????????????????????????????????????????????????????????)
        if (event.getApplicationContext().getParent() == null) {
            // ?????????????????????????????????????????????
            new Function("??????????????????") {
                @Override
                public void execute() {
                    initVehiclePositionalInfo();
                }
            }.executeInit();

            new Function("??????????????????????????????") {
                @Override
                public void execute() {
                    initPlatformParamMap();
                }
            }.executeInit();

            new Function("????????????????????????????????????") {
                @Override
                public void execute() {
                    initSchedule();
                }
            }.executeInit();

            new Function("????????????zw_risk_event") {
                @Override
                public void execute() {
                    initRiskEvent();
                }
            }.executeInit();
            new Function("????????????zw_risk_event") {
                @Override
                public void execute() {
                    initReportConstant();
                }
            }.executeInit();

            new Function("?????????????????????????????????") {
                @Override
                public void execute() {
                    initEnableMap(event);
                }
            }.executeInit();

            new Function("???????????????????????????????????????????????????") {
                @Override
                public void execute() {
                    initAutomaticVehicleMap();
                }
            }.executeInit();
        }
    }

    private void initAutomaticVehicleMap() {
        List<AdasPlatformParamSetting> unAutomaticList = jingParamSettingDao.findJingUnAutomaticInfo();
        for (AdasPlatformParamSetting unAutomatic : unAutomaticList) {
            Set<String> automaticSet = automaticVehicleMap.get(unAutomatic.getVehicleId());
            if (automaticSet == null) {
                automaticSet = new HashSet<>();
            }
            if (unAutomatic.getVehicleId().equals("e0e1cafc-8fe7-4d4d-880d-12b7199b4c8c")) {
                int a = 3;
            }
            if (notNullAndEquals(unAutomatic.getAutomaticDealOne(), 1)) {
                automaticSet.add(unAutomatic.getRiskFunctionId() + "_deal_1");
            }
            if (notNullAndEquals(unAutomatic.getAutomaticDealTwo(), 1)) {
                automaticSet.add(unAutomatic.getRiskFunctionId() + "_deal_2");
            }
            if (notNullAndEquals(unAutomatic.getAutomaticDealThree(), 1)) {
                automaticSet.add(unAutomatic.getRiskFunctionId() + "_deal_3");
            }
            if (notNullAndEquals(unAutomatic.getAutomaticGetOne(), 0)) {
                automaticSet.add(unAutomatic.getRiskFunctionId() + "_get_1");
            }
            if (notNullAndEquals(unAutomatic.getAutomaticGetTwo(), 0)) {
                automaticSet.add(unAutomatic.getRiskFunctionId() + "_get_2");
            }
            if (notNullAndEquals(unAutomatic.getAutomaticGetThree(), 0)) {
                automaticSet.add(unAutomatic.getRiskFunctionId() + "_get_3");
            }
            automaticVehicleMap.put(unAutomatic.getVehicleId(), automaticSet);
        }
    }

    private static boolean notNullAndEquals(Byte val, int equalsVal) {
        return val != null && val.intValue() == equalsVal;
    }

    /**
     * @param event
     */
    private void initEnableMap(ContextRefreshedEvent event) {
        Map<String, Object> map = event.getApplicationContext().getBeansWithAnnotation(EnableClassOrder.class);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Class<?> eventClass = entry.getValue().getClass();
            Field[] fields = eventClass.getDeclaredFields();
            for (Field field : fields) {
                EnableOrder annotation = field.getAnnotation(EnableOrder.class);
                if (annotation != null) {
                    if (annotation.enableIndex() != -1) {
                        ZhongWeiParamSettingUtil.enableOrderMap.put(annotation.functionId(), annotation.enableIndex());
                    }
                    if (annotation.auxiliaryEnableIndex() != -1) {
                        ZhongWeiParamSettingUtil.auxiliaryEnableOrderMap
                            .put(annotation.functionId(), annotation.auxiliaryEnableIndex());
                    }
                    if (!annotation.value().isEmpty()) {
                        ZhongWeiParamSettingUtil.assistAlarmParamMap.put(annotation.functionId(), annotation.value());
                    }
                }
            }
        }
    }

    private void initReportConstant() {
        ReportConstant.init(resourceDao);
    }

    private void initPlatformParamMap() {
        List<AdasPlatformParamSetting> allPlatformParams = alarmParamDao.findAllPlatformSetting();
        for (AdasPlatformParamSetting setting : allPlatformParams) {
            Map<String, AdasPlatformParamSetting> map = platformParamMap.get(setting.getVehicleId());
            if (map != null) {
                map.put(setting.getRiskFunctionId(), setting);
            } else {
                map = new HashMap<>();
                map.put(setting.getRiskFunctionId(), setting);
            }
            platformParamMap.put(setting.getVehicleId(), map);
        }
    }

    private void initVehiclePositionalInfo() {
        String vehiclePositional = RedisHelper.getString(HistoryRedisKeyEnum.ALL_MONITOR_POSITION_DATA_ZIP.of());
        if (!StringUtils.isEmpty(vehiclePositional)) {
            // ????????????
            String str = ZipUtil.gunzip(vehiclePositional);
            // ??????json????????????
            List<VehiclePositionalInfo> list = JSONObject.parseArray(str, VehiclePositionalInfo.class);
            for (VehiclePositionalInfo info : list) {
                vehiclePositionalInfo.put(info.getVehicleId(), info);
            }
        }
    }

    private void initRiskEvent() {
        List<AdasRiskEvent> list = adasRiskEventDao.findAll(null);
        for (AdasRiskEvent adasRiskEvent : list) {
            riskEventMap.put(adasRiskEvent.getFunctionId(), adasRiskEvent);
        }
    }

    private void initSchedule() {
        // ??????????????????????????????
        List<TaskJobForm> jobList = deviceUpgradeDao.getTaskJobs();
        for (TaskJobForm scheduleJob : jobList) {
            Map<String, Object> map = new HashMap<>();
            map.put("vehicleId", scheduleJob.getVehicleId());
            map.put("upgradeType", scheduleJob.getUpgradeType());
            map.put("id", scheduleJob.getId());
            quartzManager.addJob(scheduleJob, map);
        }
    }

    /**
     * ????????????????????????????????????
     * @param vehiclePositional
     */
    public void addVehiclePositionalInfo(VehiclePositionalInfo vehiclePositional) {
        vehiclePositionalInfo.put(vehiclePositional.getVehicleId(), vehiclePositional);
    }

    /**
     * ????????????????????????
     * @param vehicleId vehicleId
     * @param status    status
     */
    public void changeVehicleStatus(String vehicleId, Integer status) {
        Optional.ofNullable(vehiclePositionalInfo.get(vehicleId)).ifPresent(info -> info.setStatus(status));
    }

    /**
     * 10????????????????????? ????????????redis??????
     */
    public void setVehiclePositionalInfo2Redis() {
        if (MapUtils.isNotEmpty(vehiclePositionalInfo)) {
            final List<String> monitorIds = RedisHelper.getList(RedisKeyEnum.CONFIG_SORT_LIST.of());
            if (null == monitorIds) {
                return;
            }
            final Set<String> vehicleIdsInRedis = new HashSet<>(monitorIds);
            final Set<String> vehicleIdsInMemory = vehiclePositionalInfo.keySet();
            vehicleIdsInMemory.removeIf(vehicleId -> !vehicleIdsInRedis.contains(vehicleId));
            log.info("????????????????????????redis???????????????{}???", monitorIds.size() - vehicleIdsInMemory.size());

            if (MapUtils.isNotEmpty(vehiclePositionalInfo)) {
                String obj = JSONObject.toJSONString(vehiclePositionalInfo.values());
                String str = ZipUtil.gzip(obj);
                RedisHelper.setString(HistoryRedisKeyEnum.ALL_MONITOR_POSITION_DATA_ZIP.of(), str);
            }
        }

    }

    public void getPositionalInfos() {
        long startTime = System.currentTimeMillis();
        // //????????????????????????
        Map<String, ClientVehicleInfo> onLineMonitorMap = monitorService.getAllMonitorStatus();
        //?????????????????????????????????
        List<String> sortMonitorIds = RedisHelper.getList(RedisKeyEnum.CONFIG_SORT_LIST.of());

        //??????????????????????????????????????????
        List<String> locationList = new ArrayList<>();

        List<List<String>> partitions = Lists.partition(sortMonitorIds, 500);
        for (List<String> partition : partitions) {
            locationList.addAll(RedisHelper.batchGetString(HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(partition)));
        }
        Set<String> locationMonitorIds = new HashSet<>();
        //??????????????????????????????
        vehiclePositionalInfo.clear();
        for (String location : locationList) {
            VehiclePositionalInfo positionalInfo = buildPositionalInfo(location, onLineMonitorMap, new HashMap<>());
            locationMonitorIds.add(positionalInfo.getVehicleId());
            vehiclePositionalInfo.put(positionalInfo.getVehicleId(), positionalInfo);
        }

        //??????????????????????????????map
        Map<String, String> monitorIconMap = monitorIconService.getByMonitorId(locationMonitorIds);
        for (VehiclePositionalInfo positionalInfo : vehiclePositionalInfo.values()) {
            positionalInfo.setVehicleIcon(monitorIconMap.get(positionalInfo.getVehicleId()));
        }
        long endTime = System.currentTimeMillis();
        if (endTime - startTime > 30000) {
            log.info("?????????" + locationList.size() + "?????????????????????????????????" + (endTime - startTime) + "??????");
        }
    }

    private VehiclePositionalInfo buildPositionalInfo(String location, Map<String, ClientVehicleInfo> onLineMonitorMap,
        Map<String, String> monitorIconMap) {
        JSONObject msgBodyObject = JSONObject.parseObject(location).getJSONObject("data").getJSONObject("msgBody");
        JSONObject descObject = JSONObject.parseObject(location).getJSONObject("desc");
        String monitorId = descObject.getString("monitorId");
        ClientVehicleInfo clientVehicleInfo = onLineMonitorMap == null ? null : onLineMonitorMap.get(monitorId);
        Integer status = Objects.nonNull(clientVehicleInfo) ? clientVehicleInfo.getVehicleStatus() : 3;

        VehiclePositionalInfo vehiclePositionalInfo = new VehiclePositionalInfo();
        vehiclePositionalInfo.setVehicleId(monitorId);
        vehiclePositionalInfo.setBrand(descObject.getString("monitorName"));
        vehiclePositionalInfo.setLatitude(msgBodyObject.getDouble("latitude"));
        vehiclePositionalInfo.setLongitude(msgBodyObject.getDouble("longitude"));
        vehiclePositionalInfo.setDirection(msgBodyObject.getFloat("direction"));
        vehiclePositionalInfo.setVehicleIcon(monitorIconMap.get(monitorId));
        vehiclePositionalInfo.setStatus(status);
        return vehiclePositionalInfo;
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     * @param monitorIds ??????ID
     */
    public void updateVehiclePositional(String monitorIds) {
        Set<String> monitorIdSet = new HashSet<>();
        Collections.addAll(monitorIdSet, monitorIds.split(","));
        //????????????????????????
        Map<String, String> monitorIconMap = monitorIconService.getByMonitorId(monitorIdSet);
        //??????????????????????????????????????????
        List<String> locationList = RedisHelper.batchGetString(HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(monitorIdSet));
        Set<VehiclePositionalInfo> locationSet = new ConcurrentHashSet<>();
        for (String location : locationList) {
            locationSet.add(buildPositionalInfo(location, null, monitorIconMap));
        }
        locationSet.forEach(info -> vehiclePositionalInfo.put(info.getVehicleId(), info));
    }

    /**
     * ??????????????????????????????, ????????????????????????3
     * @param monitorIds monitorIds
     */
    public void batchUnbindVehicleStatus(Set<String> monitorIds) {
        if (CollectionUtils.isNotEmpty(monitorIds) && MapUtils.isNotEmpty(vehiclePositionalInfo)) {
            monitorIds.forEach(monitorId -> Optional.ofNullable(vehiclePositionalInfo.get(monitorId))
                .ifPresent(info -> info.setStatus(3)));
        }
    }
}
