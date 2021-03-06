package com.zw.platform.service.sensorSettings.Impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.event.ConfigUnBindEvent;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.share.BaudRateUtil;
import com.zw.platform.domain.share.CompEnUtil;
import com.zw.platform.domain.share.FilterFactorUtil;
import com.zw.platform.domain.share.ParityCheckUtil;
import com.zw.platform.domain.share.SensorUtil;
import com.zw.platform.domain.share.UploadTimeUtil;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.domain.vas.f3.TransduserManage;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.f3.sensorparam.EratureParam;
import com.zw.platform.domain.vas.f3.sensorparam.HumidityParam;
import com.zw.platform.domain.vas.f3.sensorparam.PositiveParam;
import com.zw.platform.domain.vas.mileageSensor.MileageSensorConfigQuery;
import com.zw.platform.domain.vas.workhourmgt.SensorSettingInfo;
import com.zw.platform.event.ConfigUnbindVehicleEvent;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.SensorSettingsDao;
import com.zw.platform.repository.vas.TransduserDao;
import com.zw.platform.service.redis.RedisVehicleService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.service.sensorSettings.SensorSettingsService;
import com.zw.platform.util.MonitorTypeUtil;
import com.zw.platform.util.RedisKeys;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.RedisSensorQuery;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.ws.entity.t808.parameter.ParamItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SensorSettingsServiceImpl implements SensorSettingsService {
    private static final Logger logger = LogManager.getLogger(SensorSettingsServiceImpl.class);

    @Autowired
    private TransduserDao transduserDao;

    @Autowired
    private SensorSettingsDao sensorSettingsDao;

    @Autowired
    private UserService userService;

    @Autowired
    private SendTxtService sendTxtService;

    @Autowired
    private ParameterDao parameterDao;

    private RedisVehicleService redisVehicleService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private OrganizationService organizationService;

    @Value("${data.relieve.bound}")
    private String dataRelieveBound;
    @Autowired
    private MonitorTypeUtil monitorTypeUtil;

    private final TypeCacheManger cacheManger = TypeCacheManger.getInstance();

    @Autowired
    public void setRedisVehicleService(RedisVehicleService redisVehicleService) {
        this.redisVehicleService = redisVehicleService;
    }

    @Override
    public Page<TransdusermonitorSet> findTransduserByType(MileageSensorConfigQuery query, int sensorType) {
        Page<TransdusermonitorSet> list = null;
        try {
            list = getSensorVehiclesFromCache(query, sensorType);
        } catch (Exception e) {
            logger.error("???????????????????????????", e);

            String userUuid = userService.getCurrentUserUuid();
            // ?????????????????????????????????????????????
            List<String> orgList = userService.getCurrentUserOrgIds();
            if (userUuid != null && !Objects.equals(userUuid, "") && orgList != null && orgList.size() > 0) {
                list = sensorSettingsDao.findByQuery(query, userUuid, sensorType, orgList);
            }
        }
        if (list == null || list.isEmpty()) {
            list = new Page<>();
        }
        Set<String> vids = list.stream().map(TransdusermonitorSet::getVehicleId).collect(Collectors.toSet());
        Map<String, BindDTO> bindInfos = VehicleUtil.batchGetBindInfosByRedis(vids);
        for (TransdusermonitorSet transduser : list) {
            String sensorOutId = transduser.getSensorOutId();
            String sensorOutName =
                SensorUtil.getSensorOutName(StringUtils.isEmpty(sensorOutId) ? -1 : Integer.parseInt(sensorOutId));
            String baudReteName = BaudRateUtil.getBaudRateVal(transduser.getBaudrate());// ???????????????
            String oddEvenChenk = ParityCheckUtil.getParityCheckVal(transduser.getOddEvenCheck());// ??????????????????
            String compensateName = CompEnUtil.getCompEnVal(transduser.getCompensate());// ??????????????????
            String filterFactorName = FilterFactorUtil.getFilterFactorVal(transduser.getFilterFactor());// ??????????????????
            String autoTime = UploadTimeUtil.getUploadTimeVal(transduser.getAutoTime());// ????????????????????????
            // ????????????id??????????????????
            BindDTO bindDTO = bindInfos.get(transduser.getVehicleId());
            if (bindDTO != null) {
                String groupName = bindDTO.getOrgName();
                transduser.setGroupName(groupName);
            }
            transduser.setSensorOutName(sensorOutName);
            transduser.setBaudrateName(baudReteName);
            transduser.setOddEvenCheckName(oddEvenChenk);
            transduser.setCompensateName(compensateName);
            transduser.setFilterFactorName(filterFactorName);
            transduser.setAutotimeName(autoTime);

            // ?????????????????????
            String paramType = "F3-8103-" + sensorOutId;
            List<Directive> paramList =
                parameterDao.findParameterByType(transduser.getVehicleId(), transduser.getId(), paramType); // 6:??????
            if (paramList != null && !paramList.isEmpty()) {
                Directive param = paramList.get(0);
                transduser.setSendStatus(param.getStatus());
            }

        }
        return list;
    }

    private Page<TransdusermonitorSet> getSensorVehiclesFromCache(MileageSensorConfigQuery query, int sensorType)
        throws InterruptedException {
        List<String> vehicleList = new ArrayList<>();
        String key;
        RedisSensorQuery redisQuery;
        switch (sensorType) {
            case 3:
                // ??????????????????
                key = RedisKeys.SensorType.SENSOR_ROTATE_MONITOR;
                break;
            case 2:
                // ???????????????
                key = RedisKeys.SensorType.SENSOR_WET_MONITOR;
                break;
            case 1:
                // ???????????????
                key = RedisKeys.SensorType.SENSOR_TEMPERATURE_MONITOR;
                break;
            default:
                key = null;
        }
        if (null != key) {
            redisQuery = new RedisSensorQuery(query.getGroupId(), query.getAssignmentId(), query.getSimpleQueryParam(),
                query.getProtocol());
            vehicleList = redisVehicleService.getVehicleByType(redisQuery, key);
        }
        int total = vehicleList.size();
        int curPage = query.getPage().intValue();// ?????????
        int pageSize = query.getLimit().intValue(); // ????????????
        int start = (curPage - 1) * pageSize;// ??????????????????
        int end = pageSize > (total - start) ? total : (pageSize * curPage);// ??????????????????
        List<String> list = vehicleList.subList(start, end);

        List<String> vehicles = new ArrayList<>();
        List<String> sensors = new ArrayList<>();
        for (String item : list) {
            String[] items = item.split("#@!@#");
            vehicles.add(items[0]);
            if (items.length > 1) {
                sensors.add(items[1]);
            }
        }

        List<TransdusermonitorSet> result = new ArrayList<>();
        if (!vehicles.isEmpty()) {
            result = sensorSettingsDao.findByQueryRedis(vehicles, sensors, Integer.toString(sensorType));
            for (TransdusermonitorSet item : result) {
                //?????????????????????id???????????????????????????
                item.setVehicleType(monitorTypeUtil.findByMonitorTypeAndId(item.getMonitorType(), item.getVehicleId()));
            }

            VehicleUtil.sort(result, vehicles);
        }

        return RedisQueryUtil.getListToPage(result, query, total);
    }

    /**
     * ???????????????????????????????????????
     * @param vehicleId
     * @param sensorType
     * @return
     */
    @Override
    public TransdusermonitorSet findWorkHourSettingByVid(String vehicleId, int sensorType) {
        if (StringUtils.isEmpty(vehicleId)) {
            return null;
        }

        List<TransdusermonitorSet> transdusermonitorSets = new ArrayList<>();
        MileageSensorConfigQuery query = new MileageSensorConfigQuery();
        query.setVehicleId(vehicleId);

        String userId = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        if (userId != null && !userId.equals("") && orgList != null && orgList.size() > 0) {
            transdusermonitorSets = sensorSettingsDao.findByQuery(query, userId, sensorType, orgList);
        }

        if (transdusermonitorSets.size() == 0) {
            return null;
        }

        TransdusermonitorSet transdusermonitorSet = transdusermonitorSets.get(0);
        // ?????????????????????
        String paramType = "F3-8103-" + transdusermonitorSet.getSensorOutId();
        List<Directive> paramList = parameterDao
            .findParameterByType(transdusermonitorSet.getVehicleId(), transdusermonitorSet.getId(), paramType); // 6:??????
        if (paramList != null && !paramList.isEmpty()) {
            Directive param = paramList.get(0);
            transdusermonitorSet.setSendStatus(param.getStatus());
        }

        return transdusermonitorSet;
    }

    @Override
    public JsonResultBean addTransdusermonitorSet(String[] sensorList, int sensorType, String ipAddress) {
        StringBuilder message = new StringBuilder();
        String type = getSensorType(sensorType); // ???????????????
        for (int i = 0; i < sensorList.length - 1; i++) {
            String data = sensorList[i];
            if (data != null) {
                String[] sensorData = data.split(",");
                String vid = "";
                String sensorId = "";
                if (sensorData.length > 2) {
                    vid = sensorData[2];
                    sensorId = sensorData[1];
                }
                TransdusermonitorSet transdusermonitorSet = new TransdusermonitorSet(sensorData);
                transdusermonitorSet.setCreateDataUsername(SystemHelper.getCurrentUsername());
                transdusermonitorSet.setCreateDataTime(new Date());
                boolean flag = sensorSettingsDao.addTransdusermonitorSet(transdusermonitorSet);
                if (flag) {
                    // ????????????
                    addBindToRedis(transdusermonitorSet);
                    BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(vid); // ??????????????????id????????????????????????????????????
                    TransduserManage transduserManage = transduserDao.findTransduserManageById(sensorId);
                    if (vehicleInfo != null && vehicleInfo.getName() != null && vehicleInfo.getOrgName() != null
                        && transduserManage.getSensorNumber() != null) {
                        String brand = vehicleInfo.getName();
                        String groupName = vehicleInfo.getOrgName();
                        String sensorNumber = transduserManage.getSensorNumber();
                        message.append("???????????? : ").append(brand).append(" ( @").append(groupName).append(") ????????????")
                            .append(type).append(" : ").append(sensorNumber).append(" <br/>");
                    }

                }
            }
        }
        if (!message.toString().isEmpty()) {
            logSearchService.addLog(ipAddress, message.toString(), "3", "batch", "??????????????????" + type);
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private void addBindToRedis(TransdusermonitorSet form) {
        TransduserManage transduserManage = transduserDao.findTransduserManageById(form.getSensorId());
        if (transduserManage != null) {
            redisVehicleService
                .addVehicleSensorBind(transduserManage.getSensorType(), form.getVehicleId(), form.getId(),
                    transduserManage.getSensorNumber());
        }
    }

    @Override
    public TransdusermonitorSet findTransdusermonitorSetById(String id) {
        TransdusermonitorSet transdusermonitotSet = sensorSettingsDao.findTransdusermonitorSetById(id);
        if (transdusermonitotSet == null) {
            return null;
        }
        Map<String, String> vehicle =
            RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(transdusermonitotSet.getVehicleId()));
        if (StringUtils.isNotBlank(transdusermonitotSet.getMonitorType())) {
            if ("0".equals(transdusermonitotSet.getMonitorType())) {
                transdusermonitotSet.setVehicleType(cacheManger.getVehicleType(vehicle.get("vehicleType")).getType());
            } else if ("2".equals(transdusermonitotSet.getMonitorType())) {
                transdusermonitotSet.setVehicleType("????????????");
            }
        }

        String baudReteName = BaudRateUtil.getBaudRateVal(transdusermonitotSet.getBaudrate());// ???????????????
        // ??????????????????
        String oddEvenChenk = ParityCheckUtil.getParityCheckVal(transdusermonitotSet.getOddEvenCheck());
        String compensateName = CompEnUtil.getCompEnVal(transdusermonitotSet.getCompensate());// ??????????????????
        // ??????????????????
        String filterFactorName = FilterFactorUtil.getFilterFactorVal(transdusermonitotSet.getFilterFactor());
        String autoTime = UploadTimeUtil.getUploadTimeVal(transdusermonitotSet.getAutoTime());// ????????????????????????
        // ????????????id??????????????????
        OrganizationLdap organization = organizationService.getOrganizationByUuid(transdusermonitotSet.getGroupId());
        String groupName;
        if (organization != null) {
            groupName = organization.getName();
            transdusermonitotSet.setGroupName(groupName);
        }
        transdusermonitotSet.setBaudrateName(baudReteName);
        transdusermonitotSet.setOddEvenCheckName(oddEvenChenk);
        transdusermonitotSet.setCompensateName(compensateName);
        transdusermonitotSet.setFilterFactorName(filterFactorName);
        transdusermonitotSet.setAutotimeName(autoTime);
        return transdusermonitotSet;

    }

    @Override
    public JsonResultBean updateSensorVehicle(TransdusermonitorSet transdusermonitorSet, String ipAddress) {
        TransdusermonitorSet dataSet = findTransdusermonitorSetById(transdusermonitorSet.getId());
        if (dataSet == null) {
            return new JsonResultBean(JsonResultBean.FAULT, dataRelieveBound);
        }
        transdusermonitorSet.setSensorOutId(dataSet.getSensorOutId());
        transdusermonitorSet.setUpdateDataTime(new Date());
        transdusermonitorSet.setUpdateDataUsername(SystemHelper.getCurrentUsername());
        boolean flag = sensorSettingsDao.updateSensorVehicle(transdusermonitorSet);// ???????????????
        // ????????????
        if (flag) {
            // ??????????????????
            parameterDao.updateSendStatus(transdusermonitorSet.getVehicleId(), transdusermonitorSet.getId());
            updateBindToRedis(transdusermonitorSet);
            if (dataSet.getBrand() != null && dataSet.getGroupName() != null) {
                String brand = dataSet.getBrand();
                String vehicleId = dataSet.getVehicleId();
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                String groupName = dataSet.getGroupName();
                String msg = "???????????????" + brand + " ( @" + groupName + " ) ?????????????????????????????????";
                logSearchService.addLog(ipAddress, msg, "3", "", vehicle[0], vehicle[1]);
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private void updateBindToRedis(TransdusermonitorSet form) {
        TransduserManage manager = transduserDao.findTransduserManageById(form.getSensorId());
        if (manager != null) {
            TransdusermonitorSet set = new TransdusermonitorSet();
            set.setSensorType(manager.getSensorType());
            set.setVehicleId(form.getVehicleId());
            set.setId(form.getId());
            set.setSensorNumber(manager.getSensorNumber());
            redisVehicleService.updateVehicleSensorBind(set);
        }
    }

    @Override
    public JsonResultBean deleteSensorVehicle(List<String> ids, String ipAddress) {
        StringBuilder msg = new StringBuilder();
        String sensorTypeName = "";
        String vehicleId = "";
        for (String id : ids) {
            if (id != null && !id.isEmpty()) {
                TransdusermonitorSet set = this.findTransdusermonitorSetById(id);
                if (set != null) {
                    vehicleId = set.getVehicleId();
                    boolean result = sensorSettingsDao.deleteSensorVehicle(id);
                    if (result) {
                        // ????????????
                        deleteSensorCache(set);
                        if (set.getSensorNumber() != null && set.getSensorType() != null && set.getBrand() != null) {
                            sensorTypeName = getSensorType(set.getSensorType());// ?????????????????????????????????
                            // String groupName = userService.getOrgByUuid(set.getGroupId()).getName();
                            BindDTO map = VehicleUtil.getBindInfoByRedis(set.getVehicleId());
                            msg.append("???????????? : ").append(set.getBrand()).append(" ( @").append(map.getOrgName())
                                .append(") ??????").append(sensorTypeName).append(" : ").append(set.getSensorNumber())
                                .append(" <br/>");
                        }
                    }
                }
            }
        }
        if (!msg.toString().isEmpty()) { // ??????????????????????????????????????????????????????
            if (ids.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, msg.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService.addLog(ipAddress, msg.toString(), "3", "batch", "????????????" + sensorTypeName + "??????");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    private void deleteSensorCache(TransdusermonitorSet set) {
        if (set != null) {
            redisVehicleService
                .delVehicleSensorBind(set.getSensorType(), set.getVehicleId(), set.getId(), set.getSensorNumber());
        }
    }

    @Override
    public void deleteBatchSensor(List<String> ids) {
        // if (ids != null && ids.size() > 0) {
        // for (String id : ids) {
        // if (!"".equals(id)) {
        // // sensorSettingsDao.deleteSensorVehicle(id);
        // deleteSensorVehicle(id);
        // }
        // }
        // }
    }

    @Override
    public List<TransdusermonitorSet> findVehicleBrandByType(int transduserType, List<Integer> protocols) {
        String userId = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        
        return sensorSettingsDao.findVehicleByType(transduserType, userId, orgList, protocols);
    }

    @Override
    public List<TransdusermonitorSet> findByVehicleId(int sensorType, String vehicleId) {
        return sensorSettingsDao.findByVehicleId(sensorType, vehicleId);
    }

    @Override
    public JsonResultBean sendSetDeviceParam(ArrayList<JSONObject> paramList, int sensorType, String ipAddress,
        Integer flag) throws Exception {
        if (paramList == null || paramList.size() == 0) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        StringBuilder message = new StringBuilder();
        String vehicleId = "";
        for (JSONObject map : paramList) {
            String id = map.getString("id");
            if (id != null && !id.isEmpty()) {
                TransdusermonitorSet set = this.findTransdusermonitorSetById(id);
                if (set == null) {
                    continue;
                }
                vehicleId = set.getVehicleId();
                if (vehicleId != null && !vehicleId.isEmpty()) {
                    String msgSn = sendParm(set, vehicleId, flag);
                    if (msgSn != null && !"0".equals(msgSn)) {
                        String brand = set.getBrand();

                        String groupName = set.getGroupName();
                        String type = getSensorType(set.getSensorType());
                        message.append("???????????? : ").append(brand).append(" ( @").append(groupName).append(" ) ???????????????")
                            .append(type).append(" : ").append(set.getSensorNumber()).append(" <br/>");
                    }
                }
            }
        }
        if (!message.toString().isEmpty()) {
            if (paramList.size() == 1) {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService.addLog(ipAddress, message.toString(), "3", "", vehicle[0], vehicle[1]);
            } else {
                logSearchService
                    .addLog(ipAddress, message.toString(), "3", "batch", "????????????" + getSensorType(sensorType) + "????????????");
            }
            return new JsonResultBean(JsonResultBean.SUCCESS);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    public String sendParm(TransdusermonitorSet set, String vehicleId, Integer flag) throws Exception {
        String paramType = "F3-8103-" + set.getSensorOutId();
        String paramName = set.getId();
        Object obj = new Object();
        // ??????
        if (set.getSensorType().equals(1)) {
            HumidityParam param = new HumidityParam();
            param.setAutomaticUploadTime(set.getAutoTime());
            param.setCompensatingEnable(set.getCompensate());
            param.setCompensationFactorB(set.getCorrectionFactorB());
            param.setCompensationFactorK(set.getCorrectionFactorK());
            param.setKeep1(new byte[14]);
            param.setKeep2(new byte[26]);
            double up = set.getAlarmUp() * 10;
            Double down = set.getAlarmDown() * 10;
            param.setMaxTemperature((int) up);
            param.setMinTemperature(down.intValue());
            param.setTimeOutValue(set.getOverValve());
            param.setSmoothing(set.getFilterFactor());
            obj = param;
        } else if (set.getSensorType().equals(2)) {
            // ??????
            EratureParam param = new EratureParam();
            param.setAutomaticUploadTime(set.getAutoTime());
            param.setCompensatingEnable(set.getCompensate());
            param.setCompensationFactorB(set.getCorrectionFactorB());
            param.setCompensationFactorK(set.getCorrectionFactorK());
            param.setKeep1(new byte[14]);
            param.setKeep2(new byte[26]);
            double up = set.getAlarmUp() * 10;
            double down = set.getAlarmDown() * 10;
            param.setMaxTemperature((int) up);
            param.setMinTemperature((int) down);
            param.setTimeOutValue(set.getOverValve());
            param.setSmoothing(set.getFilterFactor());
            obj = param;
        } else if (set.getSensorType().equals(3)) {
            // ?????????
            PositiveParam param = new PositiveParam();
            param.setAutomaticUploadTime(set.getAutoTime());
            param.setCompensatingEnable(set.getCompensate());
            param.setCompensationFactorB(set.getCorrectionFactorB());
            param.setCompensationFactorK(set.getCorrectionFactorK());
            param.setKeep1(new byte[2]);
            param.setKeep2(new byte[46]);
            obj = param;
        }
        ParamItem info = new ParamItem();
        int pid = Integer.parseInt("F3" + set.getSensorOutId(), 16);
        info.setParamId(pid);
        info.setParamLength(56);
        info.setParamValue(obj);
        List<ParamItem> infos = new ArrayList<>();
        infos.add(info);
        return sendTxtService.setF3SetParamByVehicleAndPeopleAndThing(vehicleId, paramName, infos, paramType,
                true, flag);
    }

    @Override
    public void deleteAllBind(String vid, int sensorType, Integer type) {
        if (ConfigUnbindVehicleEvent.TYPE_SINGLE == type) {
            sensorSettingsDao.deleteAllBind(vid, sensorType);
            // ????????????????????????????????????,????????????????????????
            if (sensorType == -1) {
                redisVehicleService.delAllSensorBindByVehicleId(vid);
            } else {
                redisVehicleService.delAllSensorBind(vid, sensorType);
            }
        } else {
            // ???????????????????????????????????????
            List<String> monitorIds = Arrays.asList(vid.split(","));
            sensorSettingsDao.deleteBatchBindByMonitorIds(monitorIds);
            redisVehicleService.delAllSensorBindByMonitorIds(monitorIds);
        }

    }

    @EventListener
    public void updateVehicleUnbound(ConfigUnBindEvent event) {
        List<String> monitorIds = event.getUnbindList().stream().map(BindDTO::getId).collect(Collectors.toList());
        String vid = StringUtils.join(monitorIds, ",");
        deleteAllBind(vid, -1,
            monitorIds.size() == 1 ? ConfigUnbindVehicleEvent.TYPE_SINGLE : ConfigUnbindVehicleEvent.TYPE_MORE);
    }

    @Override
    public boolean addBatchTransdusermonitorSet(List<TransdusermonitorSet> transdusermonitotSets) {
        Date d = new Date();
        String user = SystemHelper.getCurrentUsername();
        for (TransdusermonitorSet t : transdusermonitotSets) {
            t.setCreateDataUsername(user);
            t.setCreateDataTime(d);
            t.setUpdateDataTime(d);
            t.setUpdateDataUsername(user);
            sensorSettingsDao.addTransdusermonitorSet(t);
            System.out
                .println(t.getSensorType() + "=" + t.getVehicleId() + "=" + t.getId() + "=" + t.getSensorNumber());
            // ???????????????
            TransduserManage transduserManage = transduserDao.findTransduserManageById(t.getSensorId());
            redisVehicleService.addVehicleSensorBind(transduserManage.getSensorType(), t.getVehicleId(), t.getId(),
                transduserManage.getSensorNumber());
        }
        return true;
    }

    @Override
    public List<TransdusermonitorSet> consultVehicle(int transduserType, List<Integer> protocols) {
        String userId = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        return sensorSettingsDao.consultVehicle(transduserType, userId, orgList, protocols);
    }

    /**
     * ?????????,?????????
     * @param vehicleId
     * @param type
     * @param sensorList ????????????list??????
     */
    @Override
    public boolean updateSensorSetting(String vehicleId, int type, String[] sensorList, String ipAddress) {
        List<TransdusermonitorSet> transdusermonitorSets = new ArrayList<>();
        for (int i = 0; i < sensorList.length - 1; i++) {
            if (sensorList[i] != null) {
                String data = sensorList[i];
                if (data.isEmpty()) {
                    continue;
                }
                String[] sq = data.split(",");
                if (sq.length != 0) {
                    TransdusermonitorSet transdusermonitorSet = new TransdusermonitorSet(sq);
                    transdusermonitorSets.add(transdusermonitorSet);
                }
            }
        }
        // ???????????????????????????????????????
        this.deleteAllBind(vehicleId, type, ConfigUnbindVehicleEvent.TYPE_SINGLE);
        // ???????????????????????????????????????
        boolean flag = this.addBatchTransdusermonitorSet(transdusermonitorSets);
        if (flag) {
            BindDTO vehicleInfo = VehicleUtil.getBindInfoByRedis(vehicleId);
            if (vehicleInfo != null) {
                String brand = vehicleInfo.getName();
                String plateColor = Converter.toBlank(vehicleInfo.getPlateColor());
                String groupName = vehicleInfo.getOrgName();
                String msg = "???????????? : " + brand + " ( @" + groupName + ") ?????? " + getSensorType(type) + "??????????????????";
                logSearchService.addLog(ipAddress, msg, "3", "", brand, plateColor);
            }
            return true;
        }
        return false;
    }

    /**
     * ????????????????????????????????????????????????
     * @param sensorType
     * @return
     */
    public String getSensorType(int sensorType) {
        if (sensorType == 1) {
            return "???????????????";
        }

        if (sensorType == 2) {
            return "???????????????";
        }

        if (sensorType == 3) {
            return "??????????????????";
        }
        if (sensorType == 4) {
            return "???????????????";
        }
        return "???????????????";
    }

    @Override
    public List<SensorSettingInfo> findSensorInfo(String detectionMode, String sensorType) {
        return sensorSettingsDao.findSensorInfoBySensorType(detectionMode, sensorType);
    }

    @Override
    public List<TransdusermonitorSet> findVehicleReference(int sensorType) {
        String userId = userService.getCurrentUserUuid();
        // ?????????????????????????????????????????????
        List<String> orgList = userService.getCurrentUserOrgIds();
        List<String> reportDeviceTypes = Arrays.asList(ProtocolEnum.REPORT_DEVICE_TYPE);
        return sensorSettingsDao.findVehicleReference(sensorType, userId, orgList, reportDeviceTypes);
    }
}
