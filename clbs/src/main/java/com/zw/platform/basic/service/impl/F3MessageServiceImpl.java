package com.zw.platform.basic.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.google.common.collect.Maps;
import com.zw.lkyw.domain.LocationForLkyw;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.constant.ObdEnum;
import com.zw.platform.basic.constant.Vehicle;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ThingDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.F3MessageService;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.basic.service.MonitorService;
import com.zw.platform.basic.service.ThingService;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.basicinfo.form.OBDVehicleDataInfo;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.oilsubsidy.forwardvehiclemanage.OilForwardVehicleForm;
import com.zw.platform.domain.oilsubsidy.subsidyManage.ReissueDataRequestDTO;
import com.zw.platform.domain.systems.form.DirectiveForm;
import com.zw.platform.domain.vas.alram.OutputControl;
import com.zw.platform.domain.vas.alram.OutputControlSend;
import com.zw.platform.domain.vas.alram.OutputControlSendInfo;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.repository.vas.ForwardVehicleManageDao;
import com.zw.platform.service.obdManager.OBDVehicleTypeService;
import com.zw.platform.service.oil.PositionalService;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MessageGeneric;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.T808MessageGeneric;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.msg.t809.body.MainVehicleInfo;
import com.zw.protocol.msg.t809.body.module.ExchangeVehicleReq;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.OutputControlSendStatusDO;
import com.zw.ws.entity.OilSupplementRequestData;
import com.zw.ws.entity.OutputControlSettingDO;
import com.zw.ws.entity.common.MileageSensor;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * f3??????????????????????????????
 * @author zhangjuan
 */
@Log4j2
@Service
public class F3MessageServiceImpl implements F3MessageService {
    @Autowired
    private WebSocketMessageDispatchCenter wsMessageDispatcher;

    @Autowired
    private OBDVehicleTypeService obdVehicleTypeService;

    @Autowired
    private MonitorIconService monitorIconService;
    @Autowired
    private PositionalService positionalService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private ThingService thingService;

    @Autowired
    private AlarmSettingDao alarmSettingDao;

    @Autowired
    private ForwardVehicleManageDao forwardVehicleManageDao;

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    private ServerParamList serverParamList;

    @Autowired
    private DelayedEventTrigger trigger;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    @Autowired
    private ParameterDao parameterDao;

    private static final String DATE_FORMAT3 = "yyyyMMddHHmmss";

    @Override
    public void setDeviceFrameNumber(LocationInfo locationInfo) {
        JSONObject obd = locationInfo.getObd();
        if (Objects.isNull(obd)) {
            return;
        }
        JSONArray streamList = obd.getJSONArray("streamList");
        for (int i = 0, len = streamList.size(); i < len; i++) {
            JSONObject obdDataStream = streamList.getJSONObject(i);
            if (ObdEnum._0x0700.getId().equals(obdDataStream.getInteger("id"))) {
                locationInfo.setFrameNumberFromDevice(obdDataStream.getString("value"));
            }
        }
    }

    @Override
    public void unitConversion(LocationInfo locationInfo) {
        // ???????????????
        JSONArray oilMass = locationInfo.getOilMass();
        convertOilMassUnit(oilMass);
        // ???????????????
        JSONArray temperatureSensor = locationInfo.getTemperatureSensor();
        convertTemperatureSensorUnit(temperatureSensor);
    }

    private void convertTemperatureSensorUnit(JSONArray temperatureSensor) {
        if (temperatureSensor != null && temperatureSensor.size() > 0) {
            for (int i = 0, len = temperatureSensor.size(); i < len; i++) {
                JSONObject temperatureSensorJsonObj = temperatureSensor.getJSONObject(i);
                Integer temperature;
                try {
                    temperature = temperatureSensorJsonObj.getInteger("temperature");
                } catch (NumberFormatException e) {
                    log.error("??????????????????-????????????", e);
                    temperature = null;
                }
                if (Objects.nonNull(temperature)) {
                    String temperatureStr = String.valueOf(temperature / 10.0);
                    temperatureStr =
                        temperatureStr.endsWith(".0") ? temperatureStr.substring(0, temperatureStr.lastIndexOf(".0")) :
                            temperatureStr;
                    temperatureSensorJsonObj.put("temperature", temperatureStr);
                }
            }
        }
    }

    private void convertOilMassUnit(JSONArray oilMass) {
        if (oilMass != null && oilMass.size() > 0) {
            for (int i = 0, len = oilMass.size(); i < len; i++) {
                JSONObject oilMassJsonObj = oilMass.getJSONObject(i);
                //????????????
                Double oilTem = oilMassJsonObj.getDouble("oilTem");
                if (Objects.nonNull(oilTem)) {
                    String oilTemStr =
                        String.valueOf(BigDecimal.valueOf(oilTem).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    oilTemStr =
                        oilTemStr.endsWith(".0") ? oilTemStr.substring(0, oilTemStr.lastIndexOf(".0")) : oilTemStr;
                    oilMassJsonObj.put("oilTem", oilTemStr);
                }
                //????????????
                Double envTem = oilMassJsonObj.getDouble("envTem");
                if (Objects.nonNull(envTem)) {
                    String envTemStr =
                        String.valueOf(BigDecimal.valueOf(envTem).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
                    envTemStr =
                        envTemStr.endsWith(".0") ? envTemStr.substring(0, envTemStr.lastIndexOf(".0")) : envTemStr;
                    oilMassJsonObj.put("envTem", envTemStr);
                }
            }
        }
    }

    @Override
    public List<VehicleSpotCheckInfo> getCacheLocation(Set<String> monitorIds, String subscribeUser, String sessionId) {
        //?????????????????????????????????
        List<RedisKey> redisKeys = HistoryRedisKeyEnum.MONITOR_LOCATION.ofs(monitorIds);
        List<String> locationStrList = RedisHelper.batchGetString(redisKeys);
        List<Message> messages = new ArrayList<>();
        for (String locationStr : locationStrList) {
            messages.add(JSON.parseObject(locationStr, Message.class));
        }
        //??????????????????????????????????????????
        Map<String, Message> messageMap = buildWebLocationMsg(messages, true);

        //??????????????????????????????
        Set<String> noCacheMonitorIds = new HashSet<>(monitorIds);
        noCacheMonitorIds.removeAll(messageMap.keySet());
        Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(monitorIds);

        //?????????????????????
        Map<String, String> speedLimitMap = getSpeedLimitMap(monitorIds);
        List<VehicleSpotCheckInfo> spotCheckInfoList = new ArrayList<>();
        for (String monitorId : monitorIds) {
            //????????????????????????????????????web???
            VehicleSpotCheckInfo vehicleSpotCheckInfo = new VehicleSpotCheckInfo();
            vehicleSpotCheckInfo.setSpotCheckUser(subscribeUser);
            vehicleSpotCheckInfo.setVehicleId(monitorId);
            vehicleSpotCheckInfo.setSpotCheckTime(new Date());
            vehicleSpotCheckInfo.setActualViewDate(new Date());
            vehicleSpotCheckInfo.setSpotCheckContent(0);
            vehicleSpotCheckInfo.setSpeedLimit(speedLimitMap.get(monitorId));

            Message message = messageMap.get(monitorId);
            String messageJson;
            LocationInfo locationInfo;

            if (Objects.nonNull(message)) {
                T808Message t808Message = (T808Message) message.getData();
                locationInfo = (LocationInfo) t808Message.getMsgBody();
                //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                buildAddress(true, locationInfo);
                messageJson = JSON.toJSONString(message, SerializerFeature.WriteMapNullValue);
                vehicleSpotCheckInfo.setLatitude(String.valueOf(locationInfo.getLatitude()));
                vehicleSpotCheckInfo.setLongtitude(String.valueOf(locationInfo.getLongitude()));
                vehicleSpotCheckInfo.setLocationTime(new Date(getGpsTime(locationInfo.getGpsTime())));
                vehicleSpotCheckInfo.setSpeed(locationInfo.getSpeed());
            } else {
                locationInfo = new LocationInfo();
                getMonitorDetail(monitorId, locationInfo, null, vehicleMap.get(monitorId));
                MonitorInfo monitor = locationInfo.getMonitorInfo();
                messageJson = "";
                if (monitor != null) {
                    messageJson = "{\"desc\":\"neverOnline\",\"vid\":\"" + monitorId + "\",\"groupName\":\"" + monitor
                        .getGroupName() + "\",\"assignmentName\":\"" + monitor.getAssignmentName()
                        + "\",\"plateColor\":\"" + monitor.getPlateColorName() + "\"" + ",\"deviceNumber\":\"" + monitor
                        .getDeviceNumber() + "\",\"objectType\":\"" + monitor.getVehicleType() + "\",\"simNumber\":\""
                        + monitor.getSimcardNumber() + "\",\"professionals\":\"" + monitor.getProfessionalsName()
                        + "\",\"label\":\"" + monitor.getLabel() + "\",\"model\":\"" + monitor.getModel()
                        + "\",\"material\":\"" + monitor.getMaterial() + "\",\"weight\":\"" + monitor.getWeight()
                        + "\",\"spec\":\"" + monitor.getSpec() + "\",\"productDate\":\"" + monitor.getProductDate()
                        + "\"}";
                }
            }
            MonitorInfo monitor = locationInfo.getMonitorInfo();
            String monitorType = Objects.nonNull(monitor) ? String.valueOf(monitor.getMonitorType()) : null;
            if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
                spotCheckInfoList.add(vehicleSpotCheckInfo);
            }
            wsMessageDispatcher.pushMsgToUser(sessionId, ConstantUtil.WEB_SOCKET_T808_LOCATION, messageJson);
        }
        return spotCheckInfoList;
    }

    private Map<String, String> getSpeedLimitMap(Set<String> monitorIds) {
        List<Map<String, Object>> speedLimitList = alarmSettingDao.getSpeedLimitByVehicleIds(monitorIds);
        Map<String, String> speedLimitMap = new HashMap<>(CommonUtil.ofMapCapacity(speedLimitList.size()));
        for (Map<String, Object> objectMap : speedLimitList) {
            String monitorId = String.valueOf(objectMap.get("monitorId"));
            Object speedLimitStr = objectMap.get("speedLimit");
            speedLimitMap.put(monitorId, Objects.isNull(speedLimitStr) ? null : String.valueOf(speedLimitStr));
        }
        return speedLimitMap;
    }

    @Override
    public void buildWebLocationMsg(@NonNull LocationInfo locationInfo, String monitorId, boolean isFromRedis) {
        //??????????????????
        if (!isFromRedis) {
            buildAddress(false, locationInfo);
        }
        //??????????????????????????????
        getMonitorDetail(monitorId, locationInfo, null, null);
        //????????????????????????
        ClientVehicleInfo status = monitorService.getMonitorStatus(Collections.singletonList(monitorId)).get(monitorId);
        locationInfo.setStateInfo(Objects.isNull(status) ? 3 : status.getVehicleStatus());
        // ???????????????????????????
        setDeviceFrameNumber(locationInfo);
        //??????????????????
        locationInfo.setSatellitesNumber(getSatellitesNumber(locationInfo.getGpsAttachInfoList()));
        // ????????????????????????
        boolean hasSensorMessage = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(monitorId));
        getLocationAttachInfo(locationInfo, monitorId, hasSensorMessage, isFromRedis);
        //????????????
        unitConversion(locationInfo);
    }

    @Override
    public Map<String, Message> buildWebLocationMsg(List<Message> messages, boolean isFromRedis) {
        if (CollectionUtils.isEmpty(messages)) {
            return new HashMap<>(16);
        }
        //????????????????????????????????????????????????????????????????????????
        Set<String> monitorIds = messages.stream().map(o -> o.getDesc().getMonitorId()).collect(Collectors.toSet());
        Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(monitorIds);
        Map<String, String> monitorIconMap = monitorIconService.getByMonitorId(monitorIds);
        Map<String, ClientVehicleInfo> monitorStatusMap = monitorService.getMonitorStatus(monitorIds);
        //????????????????????????????????????
        Map<String, RedisKey> sensorMessageRedisKey = Maps.newHashMapWithExpectedSize(monitorIds.size());
        monitorIds.forEach(o -> sensorMessageRedisKey.put(o, HistoryRedisKeyEnum.SENSOR_MESSAGE.of(o)));
        Map<String, RedisKey> sensorMessageMap = RedisHelper.isContainsKey(sensorMessageRedisKey);

        Map<String, Message> messageMap = Maps.newHashMapWithExpectedSize(monitorIds.size());
        for (Message message : messages) {
            String monitorId = message.getDesc().getMonitorId();
            VehicleDTO vehicleDTO = vehicleMap.get(monitorId);
            if (Objects.isNull(vehicleDTO)) {
                log.error("??????????????????????????????????????????id:" + monitorId);
                continue;
            }

            Object data = message.getData();
            T808Message t808Message = (data instanceof T808Message) ? (T808Message) data :
                JSON.parseObject(data.toString(), T808Message.class);

            Object msgBody = t808Message.getMsgBody();
            LocationInfo locationInfo = (msgBody instanceof LocationInfo) ? (LocationInfo) msgBody :
                JSON.parseObject(msgBody.toString(), LocationInfo.class);
            if (Objects.isNull(locationInfo)) {
                continue;
            }
            //??????????????????
            if (!isFromRedis) {
                buildAddress(false, locationInfo);
            }
            //??????????????????????????????
            getMonitorDetail(monitorId, locationInfo, monitorIconMap.get(monitorId), vehicleDTO);
            //????????????????????????
            ClientVehicleInfo status = monitorStatusMap.get(monitorId);
            locationInfo.setStateInfo(Objects.isNull(status) ? 3 : status.getVehicleStatus());
            // ??????OBD ??????
            OBDVehicleDataInfo obdInfo = obdVehicleTypeService.convertStreamToObdInfo(locationInfo);
            locationInfo.setObdObjStr(JSON.toJSONString(obdInfo));
            // ???????????????????????????
            setDeviceFrameNumber(locationInfo);
            //??????????????????
            locationInfo.setSatellitesNumber(getSatellitesNumber(locationInfo.getGpsAttachInfoList()));
            // ????????????????????????
            getLocationAttachInfo(locationInfo, monitorId, sensorMessageMap.containsKey(monitorId), isFromRedis);
            //????????????
            unitConversion(locationInfo);
            t808Message.setMsgBody(locationInfo);
            message.setData(t808Message);
            messageMap.put(monitorId, message);
        }
        return messageMap;
    }

    @Override
    public void getCurDayOilAndMile(LocationInfo info, String monitorId) {
        //??????????????????????????????flink????????????????????????????????????????????????????????????????????????
        Map<String, Message> locationMap = MonitorUtils.getLocationMap(Collections.singletonList(monitorId));
        Message message = locationMap.get(monitorId);
        String deviceDayMile = null;
        String dayOilWear = null;
        String sensorDayMile = null;
        if (Objects.nonNull(message) && Objects.nonNull(message.getData())) {
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo locationInfo = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            if (Objects.nonNull(locationInfo)) {
                deviceDayMile = locationInfo.getDeviceDayMile();
                sensorDayMile = locationInfo.getSensorDayMile();
                dayOilWear = locationInfo.getDayOilWear();
            }
        }
        //??????????????????
        info.setDayOilWear(StringUtils.isBlank(dayOilWear) ? "0.0" : dayOilWear);
        info.setOilExpend(getOilExpand(info.getOilExpend(), info.getDayOilWear()));

        //??????????????????
        info.setDayMileage(StringUtils.isBlank(deviceDayMile) ? "0.0" : deviceDayMile);
        info.setDayMileageSensor(sensorDayMile);

    }

    private void buildAddress(boolean isFromRedis, LocationInfo locationInfo) {
        //??????????????????
        String lng = String.valueOf(locationInfo.getLongitude());
        String lat = String.valueOf(locationInfo.getLatitude());
        String formattedAddress = null;
        if (!"0.0".equals(lat) && !"0.0".equals(lng) && !"null".equals(lat) && !"null".equals(lng)) {
            try {
                //??????????????????????????????????????????????????????????????????,f3????????????????????????api???????????? ?????????hbase
                formattedAddress = isFromRedis
                        ? positionalService.getAddress(lng, lat)
                        : AddressUtil.inverseAddress(lng, lat).getFormattedAddress();
            } catch (Exception e) {
                log.error("??????????????????", e);
            }
        } else {
            formattedAddress = "?????????";
        }
        locationInfo.setPositionDescription(formattedAddress);
    }

    @Override
    public List<LocationForLkyw> getLkywCacheLocation(Collection<String> monitorIds, boolean isGetAddress) {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new ArrayList<>();
        }
        //???????????????????????????????????????
        Map<String, RedisKey> locationRedis = Maps.newHashMapWithExpectedSize(monitorIds.size());
        monitorIds.forEach(o -> locationRedis.put(o, HistoryRedisKeyEnum.MONITOR_LOCATION.of(o)));
        Map<String, String> locationMap = RedisHelper.batchGetStringMap(locationRedis);

        //?????????????????????????????????
        Map<String, VehicleDTO> vehicleMap = MonitorUtils.getVehicleMap(monitorIds);
        //???????????????????????????????????????
        Map<String, String> monitorIconMap = monitorIconService.getByMonitorId(monitorIds);
        //???????????????????????????????????????????????????
        Map<String, RedisKey> sensorMessageRedisKey = Maps.newHashMapWithExpectedSize(monitorIds.size());
        monitorIds.forEach(o -> sensorMessageRedisKey.put(o, HistoryRedisKeyEnum.SENSOR_MESSAGE.of(o)));
        Map<String, RedisKey> sensorMessExistMap = RedisHelper.isContainsKey(sensorMessageRedisKey);
        //?????????????????????????????????
        Map<String, ClientVehicleInfo> monitorStatusMap = monitorService.getMonitorStatus(monitorIds);

        String locationCache;
        LocationForLkyw location;
        MileageSensor mileageSensor;
        List<LocationForLkyw> resultList = new ArrayList<>();
        for (String monitorId : monitorIds) {
            locationCache = locationMap.get(monitorId);
            if (StringUtils.isNotBlank(locationCache)) {
                MessageGeneric<T808MessageGeneric<LocationForLkyw>> message = JSON.parseObject(locationCache,
                    new TypeReference<MessageGeneric<T808MessageGeneric<LocationForLkyw>>>() {
                    });
                location = message.getData().getMsgBody();
                //????????????????????????????????????
                MonitorInfo monitorInfo = location.getMonitorInfo();
                monitorInfo = monitorInfo == null ? new MonitorInfo() : monitorInfo;
                buildMonitorInfo(monitorInfo, monitorIconMap.get(monitorId), vehicleMap.get(monitorId));
                location.setMonitorInfo(monitorInfo);

                //????????????????????????
                ClientVehicleInfo status = monitorStatusMap.get(monitorId);
                location.setStateInfo(Objects.isNull(status) ? 3 : status.getVehicleStatus());

                //????????????
                if (StringUtils.isBlank(location.getDayOilWear())) {
                    location.setDayOilWear("0.0");
                }
                location.setOilExpend(getOilExpand(location.getOilExpend(), location.getDayOilWear()));
                String dayMileage = location.getDeviceDayMile();
                location.setDayMileage(StringUtils.isBlank(dayMileage) ? "0.0" : dayMileage);
                String sensorDayMile = location.getSensorDayMile();
                location.setDayMileageSensor(sensorDayMile);
                //????????????????????????????????????????????????????????????????????????????????????
                //????????????
                convertOilMassUnit(location.getOilMass());
                convertTemperatureSensorUnit(location.getTemperatureSensor());
                //??????????????????
                location.setSatellitesNumber(getSatellitesNumber(location.getGpsAttachInfoList()));
                //???????????????????????????
                if (sensorMessExistMap.containsKey(monitorId)) {
                    mileageSensor = location.getMileageSensor();
                    if (Objects.nonNull(mileageSensor) && Objects.equals(mileageSensor.getUnusual(), 0)) {
                        // ?????????????????????
                        String mileageSensorSpeed = mileageSensor.getSpeed();
                        location.setGpsSpeed(Double.valueOf(mileageSensorSpeed == null ? "0" : mileageSensorSpeed));
                        // ??????????????????
                        location.setSpeedValueSource(1);
                    }
                }
                //??????????????????
                if (isGetAddress) {
                    String y = String.valueOf(location.getLongitude());
                    String x = String.valueOf(location.getLatitude());
                    if (!"0.0".equals(x) && !"0.0".equals(y)) {
                        String formattedAddress = positionalService.getAddress(y, x);
                        location.setPositionDescription(formattedAddress);
                    }
                }

            } else {
                location = new LocationForLkyw();
                //????????????????????????????????????
                MonitorInfo monitorInfo = new MonitorInfo();
                buildMonitorInfo(monitorInfo, monitorIconMap.get(monitorId), vehicleMap.get(monitorId));
                location.setMonitorInfo(monitorInfo);
            }

            //??????????????????????????????
            if (StringUtils.isNotBlank(location.getMonitorInfo().getMonitorId())) {
                resultList.add(location);
            }
        }
        return resultList;
    }

    @Override
    public void pushCacheOboInfo(Set<String> monitorIds, String subscribeUser) {
        //???????????????????????????????????????
        Map<String, RedisKey> locationRedis = Maps.newHashMapWithExpectedSize(monitorIds.size());
        monitorIds.forEach(o -> locationRedis.put(o, HistoryRedisKeyEnum.MONITOR_LOCATION.of(o)));
        Map<String, String> locationMap = RedisHelper.batchGetStringMap(locationRedis);

        for (String monitorId : monitorIds) {
            Map<String, Object> pushInfo = new HashMap<>(16);
            String cacheLocationInfo = locationMap.get(monitorId);
            if (StringUtils.isNotBlank(cacheLocationInfo)) {
                Message message = JSON.parseObject(cacheLocationInfo, Message.class);
                T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                LocationInfo locationInfo = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
                OBDVehicleDataInfo obdInfo = obdVehicleTypeService.convertStreamToObdInfo(locationInfo);
                Double longitude = locationInfo.getLongitude();
                Double latitude = locationInfo.getLatitude();
                String address = positionalService.getAddress(longitude != null ? String.valueOf(longitude) : "",
                    latitude != null ? String.valueOf(latitude) : "");
                obdInfo.setAddress(address);
                pushInfo.put("status", 1);
                pushInfo.put("obj", obdInfo);
            } else {
                pushInfo.put("status", 0);
                pushInfo.put("obj", null);
            }
            if (wsMessageDispatcher != null) {
                wsMessageDispatcher
                    .pushInfoImpl(subscribeUser, ConstantUtil.WEB_SOCKET_OBD_URL, JSON.toJSONString(pushInfo));
            }
        }

    }

    /**
     * ?????????????????????
     * @param oilExpand  ????????????
     * @param dayOilWear ????????????
     */
    private JSONArray getOilExpand(JSONArray oilExpand, String dayOilWear) {
        if (oilExpand != null && oilExpand.size() > 0) {
            for (int i = 0, len = oilExpand.size(); i < len; i++) {
                JSONObject oilExpandJsonObj = oilExpand.getJSONObject(i);
                //4.3.7 ???????????????flink?????????????????????msgBody.dayOilWear??????
                oilExpandJsonObj.put("dayOilWear", dayOilWear);
            }
        }
        return oilExpand;
    }

    @Override
    public void getMonitorDetail(String monitorId, LocationInfo locationInfo, String monitorIcon,
        VehicleDTO vehicleDTO) {
        try {
            vehicleDTO = Objects.isNull(vehicleDTO) ? MonitorUtils.getVehicle(monitorId) : vehicleDTO;
            if (Objects.isNull(vehicleDTO)) {
                log.error("??????????????????????????????????????????id:" + monitorId);
                return;
            }
            //????????????
            monitorIcon = Objects.isNull(monitorIcon) ? monitorIconService.getMonitorIcon(monitorId) : monitorIcon;

            MonitorInfo monitorInfo = locationInfo.getMonitorInfo();
            monitorInfo = monitorInfo == null ? new MonitorInfo() : monitorInfo;
            buildMonitorInfo(monitorInfo, monitorIcon, vehicleDTO);

            String monitorType = vehicleDTO.getMonitorType();
            if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
                locationInfo.setFrameNumber(vehicleDTO.getChassisNumber());
            } else if (Objects.equals(monitorType, MonitorTypeEnum.THING.getType())) {
                // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                buildThingInfo(monitorInfo, null);
            }
            locationInfo.setMonitorInfo(monitorInfo);
        } catch (Exception e) {
            log.error("???redis???????????????????????????????????????", e);
        }
    }

    private void buildMonitorInfo(MonitorInfo monitorInfo, String monitorIcon, VehicleDTO vehicleDTO) {
        if (Objects.isNull(monitorInfo) || Objects.isNull(vehicleDTO)) {
            return;
        }
        if (!Objects.equals(vehicleDTO.getBindType(), Vehicle.BindType.HAS_BIND)) {
            return;
        }
        String monitorType = vehicleDTO.getMonitorType();
        monitorInfo.setMonitorId(vehicleDTO.getId());
        monitorInfo.setMonitorName(vehicleDTO.getName());
        monitorInfo.setSimcardNumber(vehicleDTO.getSimCardNumber());
        monitorInfo.setDeviceNumber(vehicleDTO.getDeviceNumber());
        monitorInfo.setAssignmentName(vehicleDTO.getGroupName());
        monitorInfo.setAssignmentId(vehicleDTO.getGroupId());
        monitorInfo.setGroupName(vehicleDTO.getOrgName());
        monitorInfo.setProfessionalsName(vehicleDTO.getProfessionalNames());
        monitorInfo.setTerminalManufacturer(vehicleDTO.getTerminalManufacturer());
        monitorInfo.setTerminalType(vehicleDTO.getTerminalType());
        monitorInfo.setMonitorType(Integer.valueOf(monitorType));
        monitorInfo.setDeviceType(vehicleDTO.getDeviceType());
        monitorInfo.setMonitorIcon(monitorIcon);

        if (Objects.equals(monitorType, MonitorTypeEnum.VEHICLE.getType())) {
            monitorInfo.setVehicleType(vehicleDTO.getVehicleTypeName());
            Integer plateColor = vehicleDTO.getPlateColor();
            monitorInfo.setPlateColorName(PlateColor.getNameOrBlankByCode(plateColor));
            monitorInfo.setPlateColor(plateColor);
        }
    }

    private void buildThingInfo(MonitorInfo monitorInfo, ThingDO thingDO) {
        thingDO = Objects.nonNull(thingDO) ? thingDO : thingService.getBaseById(monitorInfo.getMonitorId());
        if (thingDO != null) {
            monitorInfo.setMonitorType(2);
            monitorInfo.setVehicleType("????????????");
            monitorInfo.setLabel(thingDO.getLabel());
            monitorInfo.setModel(thingDO.getModel());
            monitorInfo.setMaterial(thingDO.getMaterial());
            monitorInfo.setWeight(String.valueOf(thingDO.getWeight()));
            monitorInfo.setSpec(thingDO.getSpec());
            if (Objects.nonNull(thingDO.getProductDate())) {
                monitorInfo.setProductDate(DateUtil.formatDate(thingDO.getProductDate(), "yyyy-MM-dd"));
            }
        }
    }

    private void getLocationAttachInfo(LocationInfo info, String monitorId, boolean hasSensorMessage,
        boolean isFromRedis) {
        if (isFromRedis) {
            //????????????
            info.setOilExpend(getOilExpand(info.getOilExpend(), info.getDayOilWear()));
            //?????????????????????????????????????????????????????????flink???????????????
            // flink???????????????????????????x0200?????????,flink??????deviceDayMile???sensorDayMile,clbs???????????????dayMileage???dayMileageSensor
            String dayMileage = info.getDeviceDayMile();
            info.setDayMileage(StringUtils.isBlank(dayMileage) ? "0.0" : dayMileage);
            String sensorDayMile = info.getSensorDayMile();
            info.setDayMileageSensor(sensorDayMile);
        } else {
            getCurDayOilAndMile(info, monitorId);
        }
        String gpsMile;
        if (hasSensorMessage) {
            MileageSensor mileageSensor = info.getMileageSensor();
            if (mileageSensor != null && mileageSensor.getUnusual() == 0) {
                String mileage = mileageSensor.getMileage();
                String speed = mileageSensor.getSpeed();
                gpsMile = mileage == null ? "0" : mileage;
                info.setGpsSpeed(Double.valueOf(speed == null ? "0" : speed));
                // ??????????????????
                info.setSpeedValueSource(1);
            } else {
                gpsMile = String.valueOf(info.getGpsMileage());
            }
        } else {
            // ?????????
            gpsMile = String.valueOf(info.getGpsMileage());
        }
        info.setDistance(gpsMile);
    }

    private Integer getSatellitesNumber(JSONArray gpsAttachInfoList) {
        if (CollectionUtils.isEmpty(gpsAttachInfoList)) {
            return null;
        }
        Optional<Object> optional = gpsAttachInfoList.stream()
            .filter(gpsAttachInfo -> Objects.equals(((JSONObject) gpsAttachInfo).getInteger("gpsAttachInfoID"), 0x31))
            .findFirst();
        if (optional.isPresent()) {
            JSONObject gpsAttr = (JSONObject) optional.get();
            return gpsAttr.getInteger("GNSSNumber");
        }
        return null;
    }

    private long getGpsTime(String gpsTime) {
        long timeO = 1000L;
        try {
            if (StringUtils.isNotBlank(gpsTime) && gpsTime.length() == 12) {
                timeO = DateUtils.parseDate("20" + gpsTime, DATE_FORMAT3).getTime();
            } else if (StringUtils.isNotBlank(gpsTime) && gpsTime.length() == 14) {
                timeO = DateUtils.parseDate(gpsTime, DATE_FORMAT3).getTime();
            }
        } catch (ParseException e) {
            log.error("??????????????????" + e);
        }
        return timeO;
    }

    @Override
    public void sendReissueDataRequest(Collection<String> monitorIds, String sessionId, Date startTime,
        Date endTime) {
        long startTimeLong = startTime.getTime() / 1000;
        long endTimeLong = endTime.getTime() / 1000;
        Map<String, OilForwardVehicleForm> oilForwardVehicleFormMap = new HashMap<>(monitorIds.size());
        Set<String> forwardingPlatformIds = new HashSet<>();
        List<OilForwardVehicleForm> oilForwardVehicleFormList = forwardVehicleManageDao.getByVehicleIds(monitorIds);
        for (OilForwardVehicleForm oilForwardVehicleForm : oilForwardVehicleFormList) {
            oilForwardVehicleFormMap.put(oilForwardVehicleForm.getMatchVehicleId(), oilForwardVehicleForm);
            forwardingPlatformIds.add(oilForwardVehicleForm.getForwardingPlatformId());
        }
        List<PlantParam> plantParams = CollectionUtils.isEmpty(forwardingPlatformIds)
            ? new ArrayList<>() : connectionParamsSetDao.get809ConnectionParamsByIds(forwardingPlatformIds);
        Map<String, PlantParam> plantParamMap =
            plantParams.stream().collect(Collectors.toMap(PlantParam::getId, Function.identity()));
        Set<String> failMonitorIds = new HashSet<>();
        Set<String> repeatReissueMonitorIds = new HashSet<>();
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIds);
        for (String monitorId : monitorIds) {
            OilForwardVehicleForm oilForwardVehicleForm = oilForwardVehicleFormMap.get(monitorId);
            if (oilForwardVehicleForm == null) {
                failMonitorIds.add(monitorId);
                continue;
            }
            String platformId = oilForwardVehicleForm.getForwardingPlatformId();
            PlantParam plantParam = plantParamMap.get(platformId);
            if (plantParam == null) {
                failMonitorIds.add(monitorId);
                continue;
            }
            BindDTO bindDTO = bindInfoMap.get(monitorId);
            if (bindDTO == null) {
                failMonitorIds.add(monitorId);
                continue;
            }
            String name = bindDTO.getName();
            // ???????????????????????????,?????????????????????????????????????????????????????????
            if (WsSessionManager.INSTANCE.isAlreadyExistOilSupplementRequestData(name)) {
                repeatReissueMonitorIds.add(monitorId);
                continue;
            }
            OilSupplementRequestData oilSupplementRequestData = new OilSupplementRequestData();
            WsSessionManager.INSTANCE.addOilSupplementRequestData(name, oilSupplementRequestData);

            Integer plateColor = bindDTO.getPlateColor();
            String vehicleCode = oilForwardVehicleForm.getVehicleCode();


            MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
            mainVehicleInfo.setExternalVehicleId(vehicleCode);
            mainVehicleInfo.setVehicleNo(name);
            mainVehicleInfo.setVehicleColor(plateColor);
            mainVehicleInfo.setDataType(ConstantUtil.T809_UP_EXG_MSG_APPLY_HISGNSSDATA_REQ);
            mainVehicleInfo
                .setData(JSON.parseObject(JSON.toJSONString(new ExchangeVehicleReq(startTimeLong, endTimeLong))));
            String plantParamIp = plantParam.getIp();
            Integer centerId = plantParam.getCenterId();
            String id = plantParam.getId();
            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_UP_EXG_MSG, plantParamIp, centerId, mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_EXG_MSG, t809Message).assembleDesc809(id);

            oilSupplementRequestData.setMonitorId(monitorId);
            oilSupplementRequestData.setVehicleCode(vehicleCode);
            oilSupplementRequestData.setBrand(name);
            oilSupplementRequestData.setPlateColor(plateColor);
            oilSupplementRequestData.setStartTime(startTimeLong);
            oilSupplementRequestData.setEndTime(endTimeLong);
            oilSupplementRequestData.setSessionId(sessionId);
            oilSupplementRequestData.setSendNumber(1);
            oilSupplementRequestData.setMessage(message);

            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
            // ????????????????????????????????????????????????????????????????????????????????????????????????10???????????????????????????????????????????????????????????????+????????????+????????????????????????????????????
            trigger.addEvent(600, TimeUnit.SECONDS, () -> addReissueDataRequestTimeoutEvent(monitorId, name, sessionId),
                monitorId + "," + sessionId);
        }
        for (String failMonitorId : failMonitorIds) {
            simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_OIL_SUPPLEMENT_RESULT,
                new ReissueDataRequestDTO(failMonitorId, ReissueDataRequestDTO.STATE_OTHER_REASON));
        }
        for (String repeatReissueMonitorId : repeatReissueMonitorIds) {
            simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_OIL_SUPPLEMENT_RESULT,
                new ReissueDataRequestDTO(repeatReissueMonitorId, ReissueDataRequestDTO.STATE_FAIL_REPEAT_REISSUE));
        }
    }

    private void addReissueDataRequestTimeoutEvent(String monitorId, String monitorName, String sessionId) {
        WsSessionManager.INSTANCE.removeOilSupplementRequestData(monitorName);
        simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_OIL_SUPPLEMENT_RESULT,
            new ReissueDataRequestDTO(monitorId, ReissueDataRequestDTO.STATE_OTHER_REASON));
    }

    @Override
    public void saveAndSendOutputControl(String sessionId, OutputControlSettingDO settingDO) {
        String vehicleId = settingDO.getVehicleId();
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO == null) {
            log.error("????????????-??????8500????????????:?????????????????????");
            return;
        }
        int msgNo = DeviceHelper.serialNumber(vehicleId);
        Date nowDate = new Date();
        String directiveName = "0x8500";
        String paramType = "F3-0x8500-OutputControl";
        // 1:I/O??????; 2:?????????;
        Integer controlSubtype = settingDO.getControlSubtype();
        // ??????id 144:??????IO; 145:???????????????91; 146:???????????????92
        Integer peripheralId = settingDO.getPeripheralId();
        // ?????????
        Integer outletSet = settingDO.getOutletSet();
        // ????????????
        Integer controlTime = settingDO.getControlTime();
        // ???????????? 0:??????; 1:??????;
        Integer controlStatus = settingDO.getControlStatus();
        // [I/O??????]??????ID???????????????????????????(?????????)?????????????????????
        String paramName = String.format("[%s]%s???%d???%s???%s???",
            controlSubtype == 1 ? "I/O??????" : "?????????",
            peripheralId == 144 ? "??????IO" : peripheralId == 145 ? "???????????????91" : "???????????????92",
            outletSet,
            controlTime == null ? "????????????" : controlTime + "???",
            controlStatus == 0 ? "??????" : "??????");
        DirectiveForm directive = parameterDao.findDirective(vehicleId, paramType);
        if (directive == null) {
            directive = new DirectiveForm(directiveName, vehicleId, paramType);
            directive.setUpdateOrAdd(2);
        }
        directive.setParameterName(paramName);
        directive.setReplyCode(1);
        directive.setDownTime(nowDate);
        directive.setSwiftNumber(msgNo);
        int status;
        if (msgNo < 0) {
            // ????????????,????????????
            status = 5;
        } else {
            status = 4;
            String directiveId = directive.getId();
            OutputControlSendInfo outputControlSendInfo = assembleOutputControlSendParam(settingDO);
            String deviceId = bindDTO.getDeviceId();
            String simCardNumber = bindDTO.getSimCardNumber();
            String deviceType = bindDTO.getDeviceType();
            SubscibeInfo info =
                new SubscibeInfo(sessionId, deviceId, msgNo, ConstantUtil.T808_VEHICLE_CONTROL_ACK, directiveId);
            SubscibeInfoCache.getInstance().putTable(info);
            T808Message message = MsgUtil
                .get808Message(simCardNumber, ConstantUtil.T808_VEHICLE_CONTROLLER, msgNo, outputControlSendInfo,
                    deviceType);
            // ??????8500
            WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_VEHICLE_CONTROLLER, deviceId);
            // ?????????????????????
            trigger.addEvent(30, TimeUnit.SECONDS, () -> {
                SubscibeInfoCache.getInstance().delTable(msgNo, deviceId);
                parameterDao.updateStatusById(directiveId, 1);
                simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_OUTPUT_CONTROL,
                    new OutputControlSendStatusDO(vehicleId, 1));
            }, vehicleId + "," + msgNo);
            // ????????????????????????
            OutputControl outputControl = OutputControl.of(settingDO);
            outputControl.setAutoFlag(0);
            alarmSettingDao.addOutputControlSetting(outputControl);
        }
        directive.setStatus(status);
        if (directive.getUpdateOrAdd() == 2) {
            parameterDao.addDirective(directive);
        } else {
            parameterDao.updateDirectiveById(directive);
        }
        simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_OUTPUT_CONTROL,
            new OutputControlSendStatusDO(vehicleId, status));
    }

    private OutputControlSendInfo assembleOutputControlSendParam(OutputControlSettingDO settingDO) {
        OutputControlSend outputControlSend = new OutputControlSend();
        outputControlSend.setType(0xF3);
        outputControlSend.setSensorId(settingDO.getPeripheralId());
        outputControlSend.setSign(1);
        Integer controlSubtype = settingDO.getControlSubtype();
        outputControlSend.setControlType(controlSubtype);
        outputControlSend.setControlIo(settingDO.getOutletSet() + 1);
        Integer controlTime = settingDO.getControlTime();
        outputControlSend.setControlTime(controlTime == null ? 0xFFFF : controlTime);
        if (controlSubtype != 3) {
            Integer controlStatus = settingDO.getControlStatus();
            outputControlSend.setControlStauts(controlStatus == null ? 0 : controlStatus);
        } else {
            Float analogOutputRatio = settingDO.getAnalogOutputRatio();
            outputControlSend.setControlStauts((int) ((analogOutputRatio == null ? 0 : analogOutputRatio) * 10));
        }
        OutputControlSendInfo outputControlSendInfo = new OutputControlSendInfo();
        outputControlSendInfo.setInfoList(Collections.singletonList(outputControlSend));
        outputControlSendInfo.setNum(1);
        return outputControlSendInfo;
    }
}

