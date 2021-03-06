package com.zw.app.service.monitor.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.monitor.BasicLocationInfo;
import com.zw.app.domain.monitor.BasicLocationInfoNew;
import com.zw.app.domain.monitor.BasicMonitorInfo;
import com.zw.app.domain.monitor.DetailLocationInfo;
import com.zw.app.domain.monitor.HumidityTemperatureData;
import com.zw.app.domain.monitor.HumidityTemperatureThreshold;
import com.zw.app.domain.monitor.IoSensorConfigInfo;
import com.zw.app.domain.monitor.OilMassSensorData;
import com.zw.app.domain.monitor.SensorData;
import com.zw.app.domain.monitor.SwitchInfo;
import com.zw.app.domain.monitor.SwitchSignalInfo;
import com.zw.app.domain.monitor.WinchInfo;
import com.zw.app.domain.monitor.WorkHourData;
import com.zw.app.domain.monitor.WorkHourResult;
import com.zw.app.domain.webMaster.monitorInfo.AppMonitorConfigInfo;
import com.zw.app.repository.mysql.monitor.MonitorManagementMysqlDao;
import com.zw.app.repository.mysql.webMaster.monitorInfo.AppMonitorDao;
import com.zw.app.service.monitor.MonitorHardwareDataService;
import com.zw.app.service.monitor.MonitorManagementService;
import com.zw.app.util.AppMonitorUtil;
import com.zw.app.util.AppParamCheckUtil;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.PeopleDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.SimCardDTO;
import com.zw.platform.basic.dto.ThingDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.PeopleService;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.basic.service.SimCardService;
import com.zw.platform.basic.service.ThingService;
import com.zw.platform.basic.service.UserGroupService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.infoconfig.form.MonitorInfo;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.domain.vas.f3.SensorConfig;
import com.zw.platform.domain.vas.f3.SensorPolling;
import com.zw.platform.domain.vas.f3.TransdusermonitorSet;
import com.zw.platform.domain.vas.workhourmgt.WorkHourSettingInfo;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.repository.vas.IoVehicleConfigDao;
import com.zw.platform.repository.vas.SensorConfigDao;
import com.zw.platform.repository.vas.SensorPollingDao;
import com.zw.platform.repository.vas.SensorSettingsDao;
import com.zw.platform.repository.vas.SwitchingSignalDao;
import com.zw.platform.repository.vas.WorkHourSettingDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.monitoring.HistoryService;
import com.zw.platform.service.monitoring.impl.RealTimeServiceImpl;
import com.zw.platform.service.workhourmgt.WorkHourStatisticsService;
import com.zw.platform.util.AudioVideoUtil;
import com.zw.platform.util.ConvertUtil;
import com.zw.platform.util.VehicleUtils;
import com.zw.platform.util.common.AddressUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.ws.entity.common.MileageSensor;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hujun
 * @date 2018/8/20 15:10
 */
@Service
@AppServerVersion
public class MonitorManagementServiceImpl implements MonitorManagementService {

    @Autowired
    UserService userService;

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    VehicleService vehicleService;

    @Autowired
    PeopleService peopleService;

    @Autowired
    ThingService thingService;

    @Autowired
    SimCardService simCardService;

    @Autowired
    DeviceService deviceService;

    @Autowired
    ProfessionalService professionalService;

    @Autowired
    AlarmSearchService alarmSearchService;

    @Autowired
    RealTimeServiceImpl realTimeServiceImpl;

    @Autowired
    SwitchingSignalDao switchingSignalDao;

    @Autowired
    SensorSettingsDao sensorSettingsDao;

    @Autowired
    SensorConfigDao sensorConfigDao;

    @Autowired
    SensorPollingDao sensorPollingDao;

    @Autowired
    MonitorManagementMysqlDao monitorManagementMysqlDao;

    @Autowired
    WorkHourStatisticsService workHourStatisticsService;

    @Autowired
    WorkHourSettingDao workHourSettingDao;

    @Autowired
    VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    HistoryService historyService;

    @Autowired
    IoVehicleConfigDao ioVehicleConfigDao;

    @Autowired
    AlarmSettingDao alarmSettingDao;

    @Autowired
    MonitorHardwareDataService monitorHardwareDataService;

    @Autowired
    AppMonitorDao appMonitorDao;

    @Autowired
    private MonitorIconService monitorIconService;

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";// ????????????????????????

    private static final String FORMAT = "yyyyMMddHHmmss";// gpsTime??????????????????

    private static final Integer STOP_TYPE = 0;// ????????????

    private static final Integer TRAVEL_TYPE = 1;// ????????????

    private static final Integer STOP_CHANGE_NUM = 3;// ????????????????????????

    private static final Integer TRAVEL_CHANGE_NUM = 5;// ?????????????????????

    private static final String DEFAULT = "default";

    /**
     * ??????
     */
    private static final int HALT_STATE = 0;
    /**
     * ??????
     */
    private static final int WORK_STATE = 1;
    /**
     * ??????
     */
    private static final int STANDBY_STATE = 2;

    /**
     * ???????????????0????????????????????????????????????,???????????????????????????????????????
     * ????????????????????????????????????????????????
     */
    private static final double INSTANT_FLOW_LIMIT = 5;

    /**
     * ??????????????????????????????????????????,????????????????????????,??????????????????????????????,??????????????????????????????; ????????????????????????????????????????????????,?????????????????????????????????;
     * ????????????????????????????????????????????????
     */
    private static final long TIME_INTERVAL = 300;

    @Override
    public JSONObject getAssignmentByUser(Integer type) throws Exception {
        if (AppParamCheckUtil.checkType(type)) {
            // ???????????????
            JSONObject result = new JSONObject();//??????????????????
            List<String> onlineMonitor = new ArrayList<>();//????????????
            // ???????????????????????????????????????id
            Set<String> allMonitorIds = userService.getCurrentUserMonitorIds();
            // ???????????????????????????????????????????????????
            Map<String, RedisKey> redisKeys = new HashMap<>();
            Iterator<String> it = allMonitorIds.iterator();
            while (it.hasNext()) {
                String vid = it.next();
                redisKeys.put(vid, HistoryRedisKeyEnum.MONITOR_STATUS.of(vid));
            }
            Map<String, String> responseMap = RedisHelper.batchGetStringMap(redisKeys);
            for (Map.Entry<String, String> entry : responseMap.entrySet()) {
                String stringResponse = entry.getValue();
                if (stringResponse != null) {
                    ClientVehicleInfo clientVehicleInfo = JSON.parseObject(stringResponse, ClientVehicleInfo.class);
                    onlineMonitor.add(clientVehicleInfo.getVehicleId());
                }
            }

            // ??????????????????
            // ????????????????????????????????????????????????
            // ???????????????????????????id
            List<GroupDTO> groupDTOS = new ArrayList<>();
            // ?????????????????????????????????????????????id???list
            List<String> userOrgListId = userService.getCurrentUserOrgIds();
            String userUuid = userService.getCurrentUserUuid();
            // ??????????????????????????????????????????????????????????????????
            if (type == 1) {
                if (onlineMonitor.size() > 0) {
                    groupDTOS = userGroupService.getUserAssignmentByVehicleId(userUuid, onlineMonitor);
                }
            } else {
                groupDTOS = userService.getCurrentUserGroupList();
            }

            // ?????????????????????
            result.put("total", allMonitorIds.size());
            result.put("online", onlineMonitor.size());
            result.put("offline", allMonitorIds.size() - onlineMonitor.size());
            result.put("assigns", groupDTOS);
            return result;
        }
        return null;
    }

    @Override
    public JSONObject getMonitorList(String assignmentId, Integer type, Integer page, Integer pageSize)
        throws Exception {
        if (AppParamCheckUtil.check64String(assignmentId) && AppParamCheckUtil.checkType(type) && page != null
            && pageSize != null) {
            // ???????????????
            JSONObject result = new JSONObject();
            JSONArray monitorList = new JSONArray();

            // ????????????id????????????????????????????????????id
            Set<String> mids = RedisHelper.getSet(RedisKeyEnum.GROUP_MONITOR.of(assignmentId));
            if (mids.isEmpty()) {
                result.put("anythingElse", false);
                result.put("monitorList", monitorList);
                return result;
            }
            //????????????????????????
            List<String> sortVehicle = RedisHelper.getList(RedisKeyEnum.CONFIG_SORT_LIST.of());

            // ??????????????????????????????????????????????????????
            if (type == 1 || type == 2) {
                // ??????redis???????????????????????????????????????
                Map<String, RedisKey> redisKeyMap = new HashMap<>();
                for (String mid : mids) {
                    redisKeyMap.put(mid, HistoryRedisKeyEnum.MONITOR_STATUS.of(mid));
                }

                Map<String, RedisKey> containsKey = RedisHelper.isContainsKey(redisKeyMap);
                // ????????????????????????id
                Set<String> onlineIds = containsKey.keySet();
                // ??????????????????????????????????????????
                if (type == 1) { //??????
                    mids = onlineIds;
                } else if (type == 2) { //??????
                    mids.removeAll(onlineIds);
                }

            }

            List<String> monitorIds = new ArrayList<>();
            // ???id??????
            if (sortVehicle != null && !sortVehicle.isEmpty()) {
                for (String id : sortVehicle) {
                    if (mids.contains(id)) {
                        monitorIds.add(id);
                    }
                }
            }

            // ????????????????????????
            boolean anythingElse = handleMonitorList(monitorList, monitorIds);// ???????????????????????????????????????
            // ?????????????????????
            result.put("anythingElse", anythingElse);
            result.put("monitorList", monitorList);

            return result;
        }
        return null;
    }

    /**
     * ????????????????????????
     * @param monitorList
     * @return
     */
    private boolean handleMonitorList(JSONArray monitorList, List<String> monitors) throws Exception {
        // ??????????????????
        boolean anythingElse;// ???????????????????????????????????????
        Map<String, LocationInfo> locationInfoMap = new HashMap<>();// ????????????????????????
        Map<String, String> addressMap = new HashMap<>();// ?????????????????????
        // ??????????????????????????????
        anythingElse = false;

        List<RedisKey> configRedisKeys = new ArrayList<>();
        Map<String, RedisKey> statusRedisMap = new HashMap<>();
        Map<String, RedisKey> locationRedisMap = new HashMap<>();

        // ??????????????????????????????
        for (int i = 0; i < monitors.size(); i++) {
            String monitor = monitors.get(i);
            configRedisKeys.add(RedisKeyEnum.MONITOR_INFO.of(monitor));
            statusRedisMap.put(monitor, HistoryRedisKeyEnum.MONITOR_STATUS.of(monitor));
            locationRedisMap.put(monitor, HistoryRedisKeyEnum.MONITOR_LOCATION.of(monitor));
        }

        Map<String, Map<String, String>> configMaps = RedisHelper.batchGetHashMap(configRedisKeys, "id",
            new String[] { "id", "name", "groupId", "groupName", "monitorType", "simCardNumber", "deviceNumber" });
        Map<String, String> statusMap = RedisHelper.batchGetStringMap(statusRedisMap);
        Map<String, String> locationMap = RedisHelper.batchGetStringMap(locationRedisMap);
        Map<String, String> iconMap = monitorIconService.getByMonitorId(monitors);

        //??????????????????????????????ids
        List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();
        Map<String, String> assignMap =
            currentUserGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));

        Set<String> stringSet = new HashSet<>();//?????????
        //????????????
        for (Map.Entry<String, String> entry : locationMap.entrySet()) {
            String data = entry.getValue();
            if (data == null) {
                continue;
            }
            Message message = JSON.parseObject(data, Message.class);
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            stringSet.add(info.getLongitude() + "," + info.getLatitude());
            locationInfoMap.put(entry.getKey(), info);
        }

        //???????????????
        addressMap = AddressUtil.batchInverseAddress(stringSet);

        //????????????
        for (Map.Entry<String, Map<String, String>> entry : configMaps.entrySet()) {
            Map<String, String> configData = entry.getValue();
            if (configData != null) {
                String mid = entry.getKey();
                String monitorType = configData.get("monitorType");
                JSONObject monitor = new JSONObject();
                //????????????id
                monitor.put("id", mid);
                //??????????????????
                monitor.put("name", configData.get("name"));
                //??????????????????
                monitor.put("type", monitorType);
                String[] assignIds = configData.get("groupId").split(",");
                StringBuilder aids = new StringBuilder();
                StringBuilder anames = new StringBuilder();
                try {
                    for (String assignId : assignIds) {
                        if (assignMap.containsKey(assignId)) {
                            aids.append(assignId).append(",");
                            anames.append(assignMap.get(assignId)).append(",");
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    //??????id?????????????????????????????????????????????????????????????????????
                }
                if (StringUtils.isBlank(aids)) {
                    continue;
                }
                //??????id
                monitor.put("assignIds", aids);
                //????????????
                monitor.put("assigns", anames);
                //????????????
                monitor.put("deviceNo", configData.get("deviceNumber"));
                //sim??????
                monitor.put("simNo", configData.get("simCardNumber"));

                //????????????????????????
                dealOnlineMonitorInfo(statusMap, monitor, mid);
                //????????????????????????
                dealIcoInfo(iconMap, monitor, mid, monitorType);
                //????????????????????????
                dealLocationInfo(locationInfoMap, monitor, mid, addressMap);

                monitorList.add(monitor);
            }
        }
        //???????????????????????????
        Collections.sort(monitorList, new Comparator<Object>() {
            Collator collator = Collator.getInstance(Locale.CHINA);

            @Override
            public int compare(Object o1, Object o2) {
                CollationKey key1 = collator.getCollationKey(((JSONObject) o1).getString("name"));
                CollationKey key2 = collator.getCollationKey(((JSONObject) o2).getString("name"));
                return key1.compareTo(key2);
            }
        });

        return anythingElse;
    }

    /**
     * ????????????????????????????????????
     * @param statusMap
     * @param monitor
     * @param mid
     */
    private void dealOnlineMonitorInfo(Map<String, String> statusMap, JSONObject monitor, String mid) {
        //??????
        if (statusMap.containsKey(mid) && statusMap.get(mid) != null) { //????????????
            String data = statusMap.get(mid);
            ClientVehicleInfo clientVehicleInfo = JSON.parseObject(data, ClientVehicleInfo.class);
            monitor.put("status", clientVehicleInfo.getVehicleStatus());
        } else { //????????????
            monitor.put("status", 3);
        }
    }

    /**
     * ??????????????????????????????
     * @param iconMap
     * @param monitor
     * @param mid
     * @param monitorType
     */
    private void dealIcoInfo(Map<String, String> iconMap, JSONObject monitor, String mid, String monitorType) {
        //??????????????????
        if (iconMap.containsKey(mid) && iconMap.get(mid) != null) {
            monitor.put("icon", iconMap.get(mid));
        } else {
            monitor.put("icon", AppMonitorUtil.getMonitorDefaultIco(monitorType));
        }
    }

    /**
     * ??????????????????????????????
     * @param locationInfoMap
     * @param monitor
     * @param mid
     * @param addressMap
     */
    private void dealLocationInfo(Map<String, LocationInfo> locationInfoMap, JSONObject monitor, String mid,
        Map<String, String> addressMap) {
        //???????????????
        if (locationInfoMap.containsKey(mid)) {
            LocationInfo info = locationInfoMap.get(mid);
            monitor.put("location", addressMap.get(info.getLongitude() + "," + info.getLatitude()));

            try {
                //????????????
                String gpsTime = DateFormatUtils
                    .format(DateUtils.parseDate("20" + info.getGpsTime(), FORMAT).getTime(), DATE_FORMAT);
                monitor.put("gpsTime", gpsTime);
            } catch (Exception e) {
                monitor.put("gpsTime", "");
            }

            //???Redis?????????????????????????????????
            this.setDailyMileAndOil(mid, info);

            //??????
            MileageSensor mileageSensor = info.getMileageSensor();
            if (mileageSensor != null) { //??????????????????
                if (mileageSensor.getUnusual() != null && mileageSensor.getUnusual() == 0) { //??????
                    monitor.put("dayMileage", info.getDayMileage() + " km");
                    monitor.put("speed", mileageSensor.getSpeed() + " km/h");
                } else { //??????
                    monitor.put("dayMileage", info.getDayMileage() == null ? " -" : info.getDayMileage() + " km");
                    monitor.put("speed", info.getGpsSpeed() == null ? " -" : info.getGpsSpeed().toString() + " km/h");
                }
            } else { //?????????????????????
                monitor.put("dayMileage", info.getDayMileage() == null ? " -" : info.getDayMileage() + " km");
                monitor.put("speed", info.getGpsSpeed() == null ? " -" : info.getGpsSpeed().toString() + " km/h");

            }
            //??????????????????????????????????????????acc????????????
            if (info.getMonitorInfo().getMonitorType() == 0) {
                //acc??????
                Integer acc = info.getAcc();
                if (acc != null) {
                    switch (acc) {
                        case 0:
                            monitor.put("acc", "???");
                            break;
                        case 1:
                            monitor.put("acc", "???");
                            break;
                        default:
                            monitor.put("acc", " -");
                            break;
                    }
                } else {
                    monitor.put("acc", " -");
                }
            }

        } else {
            monitor.put("location", "");
            monitor.put("gpsTime", "");
            monitor.put("speed", " -");
            monitor.put("dayMileage", " -");
            monitor.put("acc", " -");
        }
    }

    /**
     * ????????????????????????
     * @param monitorList
     * @return
     */
    private void handleMonitorDetailList(JSONArray monitorList, List<String> monitors) throws Exception {

        Map<String, LocationInfo> locationInfoMap = new HashMap<>();// ????????????????????????
        Map<String, String> addressMap = new HashMap<>();// ?????????????????????

        List<RedisKey> configRedisKeys = new ArrayList<>();
        Map<String, RedisKey> statusRedisMap = new HashMap<>();
        Map<String, RedisKey> locationRedisMap = new HashMap<>();

        // ??????????????????????????????
        for (int i = 0; i < monitors.size(); i++) {
            String monitor = monitors.get(i);
            configRedisKeys.add(RedisKeyEnum.MONITOR_INFO.of(monitor));
            statusRedisMap.put(monitor, HistoryRedisKeyEnum.MONITOR_STATUS.of(monitor));
            locationRedisMap.put(monitor, HistoryRedisKeyEnum.MONITOR_LOCATION.of(monitor));
        }

        Map<String, Map<String, String>> configMaps = RedisHelper.batchGetHashMap(configRedisKeys, "id",
                "id", "name", "groupId", "groupName", "monitorType", "simCardNumber", "deviceNumber");
        Map<String, String> statusMap = RedisHelper.batchGetStringMap(statusRedisMap);
        Map<String, String> locationMap = RedisHelper.batchGetStringMap(locationRedisMap);
        Map<String, String> iconMap = monitorIconService.getByMonitorId(monitors);

        Set<String> lngLats = new HashSet<>();//?????????
        //????????????
        for (Map.Entry<String, String> entry : locationMap.entrySet()) {
            String data = entry.getValue();
            if (data == null) {
                continue;
            }
            Message message = JSON.parseObject(data, Message.class);
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            lngLats.add(info.getLongitude() + "," + info.getLatitude());
            locationInfoMap.put(entry.getKey(), info);
        }

        //???????????????
        addressMap = AddressUtil.batchInverseAddress(lngLats);

        //????????????
        for (Map.Entry<String, Map<String, String>> entry : configMaps.entrySet()) {
            String id = entry.getKey();
            Map<String, String> configData = configMaps.get(id);
            if (configData != null) {
                String monitorType = configData.get("monitorType");
                JSONObject monitor = new JSONObject();
                //????????????id
                monitor.put("id", id);
                //??????????????????
                monitor.put("name", configData.get("name"));
                //??????????????????
                monitor.put("type", monitorType);

                //????????????
                monitor.put("deviceNo", configData.get("deviceNumber"));
                //sim??????
                monitor.put("simNo", configData.get("simCardNumber"));
                //????????????????????????
                dealOnlineMonitorInfo(statusMap, monitor, id);
                //????????????????????????
                dealIcoInfo(iconMap, monitor, id, monitorType);
                //????????????????????????
                dealLocationInfo(locationInfoMap, monitor, id, addressMap);
                monitorList.add(monitor);
            }
        }
    }

    /**
     * ????????????????????????
     * @return
     */
    private JSONObject fuzzyMonitorList(List<String> monitors) throws Exception {

        List<RedisKey> configRedisKeys = new ArrayList<>();
        // ??????????????????????????????
        for (int i = 0; i < monitors.size(); i++) {
            String monitor = monitors.get(i);
            configRedisKeys.add(RedisKeyEnum.MONITOR_INFO.of(monitor));
        }
        Map<String, Map<String, String>> configMaps = RedisHelper.batchGetHashMap(configRedisKeys, "id",
            new String[] { "id", "name", "groupId", "groupName", "monitorType", "simCardNumber", "deviceNumber" });

        //??????????????????????????????ids
        List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();

        HashMap<String, JSONObject> assignInfos = new HashMap<>();
        //????????????
        for (Map.Entry<String, Map<String, String>> entry : configMaps.entrySet()) {
            Map<String, String> configData = entry.getValue();
            if (configData != null) {
                String mid = entry.getKey();
                String[] groupIds = configData.get("groupId").split(",");
                for (String aid : groupIds) {
                    JSONObject assignInfo = assignInfos.get(aid);
                    if (assignInfo != null) {
                        assignInfo.put("vehicleIds", assignInfo.getString("vehicleIds") + mid + ",");
                        assignInfo.put("vehicleNum", assignInfo.getIntValue("vehicleNum") + 1);
                    } else {
                        assignInfo = new JSONObject();
                        assignInfo.put("vehicleNum", 1);
                        assignInfo.put("id", aid);
                        assignInfo.put("vehicleIds", mid + ",");
                        //assignInfo.put("name", assignMap.get(aid));
                        assignInfos.put(aid, assignInfo);
                    }
                }
            }
        }

        List<JSONObject> assigns = new ArrayList<>();
        for (GroupDTO groupDTO : currentUserGroupList) {
            if (assignInfos.containsKey(groupDTO.getId())) {
                JSONObject asg = assignInfos.get(groupDTO.getId());
                asg.put("name", groupDTO.getName());
                assigns.add(asg);
            }
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("assignInfos", assigns);
        jsonObject.put("recordTotal", assigns.size());
        jsonObject.put("vehicleTotal", monitors.size());
        return jsonObject;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = "/clbs/app/monitor/fuzzyList")
    public JSONObject getFuzzyMonitorList(String fuzzyParam) throws Exception {
        if (StringUtils.isNotBlank(fuzzyParam)) {
            // ???????????????
            JSONObject result = new JSONObject();
            JSONArray monitorList = new JSONArray();
            // ???????????????????????????????????????id
            Set<String> allMonitorIds = userService.getCurrentUserMonitorIds();
            // ????????????????????????
            List<String> fuzzyIds = getFuzzyMonitorIds(fuzzyParam);
            // ??????????????????????????????????????????id
            fuzzyIds.retainAll(allMonitorIds);
            // ????????????????????????
            boolean anythingElse = handleMonitorList(monitorList, fuzzyIds);// ???????????????????????????????????????
            result.put("anythingElse", anythingElse);
            result.put("monitorList", monitorList);
            return result;
        }
        return null;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_EIGHT, url = "/clbs/app/monitor/fuzzyList")
    public JSONObject fuzzyMonitorList(String fuzzyParam) throws Exception {
        if (StringUtils.isNotBlank(fuzzyParam)) {
            // ???????????????????????????????????????id
            Set<String> allMonitorIds = userService.getCurrentUserMonitorIds();
            // ????????????????????????
            List<String> fuzzyIds = getFuzzyMonitorIds(fuzzyParam);
            // ??????????????????????????????????????????id
            fuzzyIds.retainAll(allMonitorIds);
            // ????????????????????????
            JSONObject re = fuzzyMonitorList(fuzzyIds);
            return re;
        }
        return null;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_EIGHT, url = "/clbs/app/monitor/fuzzyDetailList")
    public JSONObject fuzzyMonitorDetailList(String vehicleIds) throws Exception {
        if (StringUtils.isNotBlank(vehicleIds)) {
            // ???????????????
            JSONObject result = new JSONObject();
            List<String> fuzzyIds = Arrays.asList(vehicleIds.split(","));
            JSONArray monitorList = new JSONArray();
            // ????????????????????????
            handleMonitorDetailList(monitorList, fuzzyIds);
            Collections.sort(monitorList, new Comparator<Object>() {
                Collator collator = Collator.getInstance(Locale.CHINA);

                @Override
                public int compare(Object o1, Object o2) {
                    CollationKey key1 = collator.getCollationKey(((JSONObject) o1).getString("name"));
                    CollationKey key2 = collator.getCollationKey(((JSONObject) o2).getString("name"));
                    return key1.compareTo(key2);
                }
            });
            result.put("vehicleInfos", monitorList);
            return result;
        }
        return null;
    }

    /**
     * ??????????????????????????????ids
     * @param fuzzyParam
     * @return
     */
    private List<String> getFuzzyMonitorIds(String fuzzyParam) {
        return VehicleUtils.fuzzQueryMonitors(fuzzyParam, true);
    }

    @Override
    public JSONObject getMonitorIds(String id, String favoritesIds) throws Exception {
        // ???????????????
        JSONObject result = new JSONObject();
        // ???????????????????????????????????????id
        Set<String> allMonitorIds = new HashSet<>();
        if (StringUtils.isNotBlank(id)) {
            if (AppParamCheckUtil.check64String(id)) {
                allMonitorIds.add(id);
            }
        } else {
            allMonitorIds = userService.getCurrentUserMonitorIds();
        }

        Set<String> favoritesIdSet = new HashSet<>();
        //??????????????????id??????????????????
        if (StringUtils.isNotBlank(favoritesIds)) {
            String[] ids = favoritesIds.split(",");
            favoritesIdSet = new HashSet<>(Arrays.asList(ids));
        }

        // ????????????????????????????????????
        favoritesIdSet.retainAll(allMonitorIds);


        // ???????????????????????????1000????????????
        Map<String, JSONObject> allMonitorIdMap = new LinkedHashMap<>();
        List<String> resultMonitorIds = new ArrayList<>();

        //?????????????????????
        for (String fid : favoritesIdSet) {
            allMonitorIdMap.put(fid, new JSONObject());
        }
        resultMonitorIds.addAll(favoritesIdSet);

        if (allMonitorIds.size() > 1000) {
            for (String monitorId : allMonitorIds) {
                if (allMonitorIdMap.containsKey(monitorId)) {
                    continue;
                }
                allMonitorIdMap.put(monitorId, new JSONObject());
                resultMonitorIds.add(monitorId);
                if (allMonitorIdMap.size() >= 1000) {
                    break;
                }
            }
        } else {
            for (String monitorId : allMonitorIds) {
                if (allMonitorIdMap.containsKey(monitorId)) {
                    continue;
                }
                allMonitorIdMap.put(monitorId, new JSONObject());
                resultMonitorIds.add(monitorId);
            }
        }

        // ????????????????????????
        if (allMonitorIdMap.size() > 0) {
            // ???????????????????????????
            Map<String, RedisKey> onlineKeys = new HashMap<>();
            Map<String, RedisKey> locationRedisMap = new HashMap<>();
            for (String mid : resultMonitorIds) {
                onlineKeys.put(mid, HistoryRedisKeyEnum.MONITOR_STATUS.of(mid));
                locationRedisMap.put(mid, HistoryRedisKeyEnum.MONITOR_LOCATION.of(mid));
            }
            Map<String, BindDTO> bindInfoMap =
                VehicleUtil.batchGetBindInfosByRedis(resultMonitorIds, Lists.newArrayList("name"));
            Map<String, String> online = RedisHelper.batchGetStringMap(onlineKeys);
            Map<String, String> locationMap = RedisHelper.batchGetStringMap(locationRedisMap);
            Map<String, String> iconMap = monitorIconService.getByMonitorId(resultMonitorIds);

            // ?????????????????????????????????
            List<String> onlineIds = new ArrayList<>();
            Map<String, Integer> monitorStatus = new HashMap<>();
            for (Map.Entry<String, String> entry : online.entrySet()) {
                String data = entry.getValue();
                if (data != null) { //??????????????????key???????????????
                    ClientVehicleInfo clientVehicleInfo = JSON.parseObject(data, ClientVehicleInfo.class);
                    onlineIds.add(entry.getKey());
                    monitorStatus.put(entry.getKey(), clientVehicleInfo.getVehicleStatus());
                }
            }

            for (String mid : onlineIds) {
                if (locationMap.get(mid) != null) {
                    JSONObject jsonObject = JSONObject.parseObject(locationMap.get(mid));
                    JSONObject data = jsonObject.getJSONObject("data").getJSONObject("msgBody");
                    JSONObject monitor = data.getJSONObject("monitorInfo");
                    JSONObject monitorInfo = allMonitorIdMap.get(mid);
                    monitorInfo.put("id", mid);
                    monitorInfo.put("longitude", data.get("longitude"));
                    monitorInfo.put("latitude", data.get("latitude"));
                    monitorInfo.put("name", monitor.get("monitorName"));
                    BindDTO bindDTO = bindInfoMap.get(mid);
                    if (bindDTO != null) {
                        monitorInfo.put("name", bindDTO.getName());
                    }
                    monitorInfo.put("status", monitorStatus.get(mid));
                    monitorInfo.put("direction", data.get("direction"));
                    monitorInfo.put("monitorType", monitor.getInteger("monitorType"));
                    monitorInfo.put("gpsTime", data.get("gpsTime"));
                    if (iconMap.get(mid) != null) {
                        monitorInfo.put("ico", iconMap.get(mid));
                    } else {
                        String monitorType =
                            monitor.get("monitorType") == null ? "" : monitor.get("monitorType").toString();
                        monitorInfo.put("ico", AppMonitorUtil.getMonitorDefaultIco(monitorType));
                    }
                }
            }
            // ???????????????????????????????????????
            // ????????????????????????
            resultMonitorIds.removeAll(onlineIds);

            // ??????????????????????????????????????????????????????
            List<String> positionIds = new ArrayList<>();
            for (String mid : resultMonitorIds) {
                if (locationMap.get(mid) != null) { //??????????????????key????????????????????????????????????
                    positionIds.add(mid);
                    JSONObject jsonObject = JSONObject.parseObject(locationMap.get(mid));
                    JSONObject data = jsonObject.getJSONObject("data").getJSONObject("msgBody");
                    JSONObject monitor = data.getJSONObject("monitorInfo");
                    JSONObject monitorInfo = allMonitorIdMap.get(mid);
                    monitorInfo.put("id", mid);
                    monitorInfo.put("longitude", data.get("longitude"));
                    monitorInfo.put("latitude", data.get("latitude"));
                    monitorInfo.put("name", monitor.get("monitorName"));
                    BindDTO bindDTO = bindInfoMap.get(mid);
                    if (bindDTO != null) {
                        monitorInfo.put("name", bindDTO.getName());
                    }
                    monitorInfo.put("status", 3);
                    monitorInfo.put("direction", data.get("direction"));
                    monitorInfo.put("monitorType", monitor.getInteger("monitorType"));
                    monitorInfo.put("gpsTime", data.get("gpsTime"));
                    if (iconMap.get(mid) != null) {
                        monitorInfo.put("ico", iconMap.get(mid));
                    } else {
                        String monitorType =
                            monitor.get("monitorType") == null ? "" : monitor.get("monitorType").toString();
                        monitorInfo.put("ico", AppMonitorUtil.getMonitorDefaultIco(monitorType));
                    }
                }
            }
            // ????????????????????????????????????????????????
            resultMonitorIds.removeAll(positionIds);
            // ?????????????????????????????????
            List<RedisKey> configRedisKeys = new ArrayList<>();
            for (String mid : resultMonitorIds) {
                configRedisKeys.add(RedisKeyEnum.MONITOR_INFO.of(mid));
            }
            Map<String, Map<String, String>> configMaps =
                RedisHelper.batchGetHashMap(configRedisKeys, "id", new String[] { "id", "name", "monitorType" });

            for (Map.Entry<String, Map<String, String>> entry : configMaps.entrySet()) {
                if (entry.getValue() != null) {
                    Map<String, String> data = entry.getValue();
                    JSONObject monitorInfo = allMonitorIdMap.get(entry.getKey());
                    String mid = entry.getKey();
                    monitorInfo.put("id", mid);
                    monitorInfo.put("name", data.get("name"));
                    BindDTO bindDTO = bindInfoMap.get(mid);
                    if (bindDTO != null) {
                        monitorInfo.put("name", bindDTO.getName());
                    }
                    monitorInfo.put("monitorType", data.get("monitorType"));
                    if (iconMap.get(mid) != null) {
                        monitorInfo.put("ico", iconMap.get(mid));
                    } else {
                        String monitorType = data.get("monitorType") == null ? "" : data.get("monitorType").toString();
                        monitorInfo.put("ico", AppMonitorUtil.getMonitorDefaultIco(monitorType));
                    }
                }
            }
            // ?????????????????????
            result.put("monitorList", allMonitorIdMap.values());
            return result;
        }
        return result;
    }

    @Override
    public BasicLocationInfo getBasicLocationInfo(String id) throws Exception {
        if (AppParamCheckUtil.check64String(id)) {
            BasicLocationInfo basicLocationInfo = new BasicLocationInfo();
            // ???????????????????????????????????????
            String statusValues = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_STATUS.of(id));
            if (statusValues != null) { //???????????????????????????????????????
                ClientVehicleInfo clientVehicleInfo = JSON.parseObject(statusValues, ClientVehicleInfo.class);
                basicLocationInfo.setStatus(clientVehicleInfo.getVehicleStatus());
            } else { //?????????????????????????????????????????????
                basicLocationInfo.setStatus(3);
            }
            /** ???????????????????????????????????????*/
            String locationValues = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(id));
            if (StringUtils.isNotBlank(locationValues)) { //???????????????????????????
                // ????????????????????????
                Message message = JSON.parseObject(locationValues, Message.class);
                T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
                // ??????????????????
                MonitorInfo monitorInfo = info.getMonitorInfo();
                //??????????????????
                basicLocationInfo.setName(monitorInfo.getMonitorName());
                //??????????????????
                basicLocationInfo.setType(monitorInfo.getMonitorType());
                //????????????
                basicLocationInfo.setAddress(
                        AddressUtil.inverseAddress(info.getLongitude(), info.getLatitude()).getFormattedAddress());
                //???????????????????????????????????????????????????????????????????????????????????????????????????
                if (basicLocationInfo.getStatus() != null && basicLocationInfo.getStatus() == 3) {
                    String gpsTime = info.getGpsTime();
                    if (StringUtils.isNotBlank(gpsTime)) {
                        long nowTime = Calendar.getInstance().getTimeInMillis();
                        long locationTime = DateUtils.parseDate("20" + gpsTime, FORMAT).getTime();
                        BigDecimal b1 = new BigDecimal(locationTime);
                        BigDecimal b2 = new BigDecimal(nowTime);
                        basicLocationInfo
                            .setDuration(b2.subtract(b1).setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
                    }
                } else {
                    basicLocationInfo.setDuration(info.getDurationTime());
                }
                //????????????
                String gpsTime = DateFormatUtils
                    .format(DateUtils.parseDate("20" + info.getGpsTime(), FORMAT).getTime(), DATE_FORMAT);
                basicLocationInfo.setGpsTime(gpsTime);
                //??????
                if (info.getBatteryElec() != null) {
                    Integer level = getBatteryLevel(info.getBatteryElec());
                    basicLocationInfo.setBattery(level);
                }
                //???????????????????????????
                Integer signalStrength = null;
                if (info.getSignalStrength() != null) {
                    signalStrength = info.getSignalStrength();
                }
                switch (info.getLocationType()) {
                    case 1:
                        basicLocationInfo.setGps(signalStrength);
                        break;
                    case 2:
                        basicLocationInfo.setLbs(signalStrength);
                        break;
                    case 3:
                        basicLocationInfo.setLbs_wifi(signalStrength);
                        break;
                    default:
                        break;
                }
                //??????
                MileageSensor mileageSensor = info.getMileageSensor();
                if (mileageSensor != null && mileageSensor.getUnusual() != null
                    && mileageSensor.getUnusual() == 0) { //??????
                    basicLocationInfo.setSpeed(mileageSensor.getSpeed());
                } else { //??????????????????????????????
                    basicLocationInfo.setSpeed(info.getGpsSpeed() == null ? null : info.getGpsSpeed().toString());
                }
            } else { //??????????????????????????????
                //???????????????????????????????????????
                Map<String, String> configMap =
                    RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(id), "name", "monitorType");
                if (configMap != null) {
                    basicLocationInfo.setName(configMap.get("name"));
                    basicLocationInfo.setType(Integer.parseInt(configMap.get("monitorType")));
                }
            }
            return basicLocationInfo;
        }
        return null;
    }

    /**
     * @param id
     * @return
     * @throws Exception APP2.0.0??????
     */
    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FOUR,
        url = "/clbs/app/monitor/getBasicLocationInfoByMonitorId")
    public BasicLocationInfoNew getBasicLocationInfoByMonitorId(String id) throws Exception {
        if (AppParamCheckUtil.check64String(id)) {
            BasicLocationInfoNew locationInfo = new BasicLocationInfoNew();
            String statusValues = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_STATUS.of(id));
            //???????????????????????????????????????
            if (statusValues != null) {
                ClientVehicleInfo clientVehicleInfo = JSON.parseObject(statusValues, ClientVehicleInfo.class);
                locationInfo.setStatus(clientVehicleInfo.getVehicleStatus());
                //?????????????????????????????????????????????
            } else {
                locationInfo.setStatus(3);
            }
            String locationValues = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(id));
            if (StringUtils.isNotBlank(locationValues)) {
                Message message = JSON.parseObject(locationValues, Message.class);
                T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
                MonitorInfo monitorInfo = info.getMonitorInfo();
                locationInfo.setId(monitorInfo.getMonitorId());
                //??????????????????
                locationInfo.setType(monitorInfo.getMonitorType());
                //??????????????????
                locationInfo.setName(monitorInfo.getMonitorName());
                String iconName = monitorIconService.getMonitorIcon(id);
                locationInfo.setIco(iconName);
                //????????????
                locationInfo.setLatitude(info.getLatitude());
                locationInfo.setLongitude(info.getLongitude());
                locationInfo.setAddress(
                    AddressUtil.inverseAddress(info.getLongitude(), info.getLatitude()).getFormattedAddress());
                locationInfo.setAngle(info.getDirection());
                //????????????
                locationInfo.setDuration(info.getDurationTime());
                //????????????
                String gpsTime = DateFormatUtils
                    .format(DateUtils.parseDate("20" + info.getGpsTime(), FORMAT).getTime(), DATE_FORMAT);
                locationInfo.setGpsTime(gpsTime);
                locationInfo.setTime(info.getGpsTime());
                locationInfo.setGpsMileage(info.getGpsMileage());
                //??????
                JSONObject elecData = info.getElecData();
                if (elecData != null && Objects.equals(elecData.getInteger("id"), 0x4F)) {
                    Integer deviceElectricity = elecData.getInteger("deviceElectricity");
                    if (deviceElectricity != null) {
                        if (deviceElectricity < 0) {
                            deviceElectricity = 0;
                        }
                        if (deviceElectricity > 100) {
                            deviceElectricity = 100;
                        }
                    }
                    locationInfo.setBattery(deviceElectricity);
                }
                //????????????
                locationInfo.setPattern(info.getLocationPattern());
                //????????????
                Integer satellitesNumber = info.getSatellitesNumber();
                if (satellitesNumber == null) {
                    JSONArray gpsAttachInfoList = info.getGpsAttachInfoList();
                    if (gpsAttachInfoList != null && gpsAttachInfoList.size() > 0) {
                        for (int i = 0, len = gpsAttachInfoList.size(); i < len; i++) {
                            JSONObject gpsAttachInfoJsonObj = gpsAttachInfoList.getJSONObject(i);
                            Integer gpsAttachInfoID = gpsAttachInfoJsonObj.getInteger("gpsAttachInfoID");
                            if (gpsAttachInfoID != null && gpsAttachInfoID == 0x31) {
                                satellitesNumber = gpsAttachInfoJsonObj.getInteger("GNSSNumber");
                                break;
                            }
                        }
                    }
                }
                locationInfo.setSatellitesNumber(satellitesNumber);
                //wifi??????
                locationInfo.setWifi(info.getWifiSignalStrength());
                //???????????? ????????????
                Integer signalType = info.getSignalType();
                // if (signalType != null && signalType != 1) {
                //     if (signalType >= 4) {
                //         locationInfo.setSignalType(signalType);
                //     }
                //     locationInfo.setSignalStrength(info.getSignalStrength());
                // }
                locationInfo.setSignalType(signalType);
                locationInfo.setSignalStrength(info.getSignalStrength());
                //??????
                MileageSensor mileageSensor = info.getMileageSensor();
                if (mileageSensor != null && mileageSensor.getUnusual() != null
                    && mileageSensor.getUnusual() == 0) { //??????
                    locationInfo.setSpeed(mileageSensor.getSpeed());
                } else { //??????????????????????????????
                    locationInfo.setSpeed(info.getGpsSpeed() == null ? "0.0" : info.getGpsSpeed().toString());
                }
            }
            Map<String, String> monitorValueMap =
                RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(id), Arrays.asList("name", "monitorType"));
            if (monitorValueMap != null) {
                locationInfo.setName(monitorValueMap.get("name"));
                locationInfo.setType(Integer.parseInt(monitorValueMap.get("monitorType")));
            }
            return locationInfo;
        }
        return null;
    }

    /**
     * ??????????????????
     * @param battery
     * @return
     */
    private Integer getBatteryLevel(Integer battery) {
        Integer level = null;
        if (0 < battery && battery <= 20) {
            level = 1;
        } else if (20 < battery && battery <= 40) {
            level = 2;
        } else if (40 < battery && battery <= 60) {
            level = 3;
        } else if (60 < battery && battery <= 80) {
            level = 4;
        } else if (80 < battery && battery <= 100) {
            level = 5;
        }
        return level;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SIX, url = "/clbs/app/monitor/detailLocationInfo")
    public AppResultBean getDetailLocationInfo(String id, Integer version) throws Exception {
        if (AppParamCheckUtil.check64String(id)) {
            JSONObject re = new JSONObject();
            JSONArray sensors = new JSONArray();
            // ??????????????????????????????
            String locationValues = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(id));
            if (StringUtils.isNotBlank(locationValues)) { //???????????????????????????
                // ????????????????????????
                Message message = JSON.parseObject(locationValues, Message.class);
                T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
                // ?????????????????????????????????
                setDailyMileAndOil(id, info);
                if (StringUtils.isBlank(info.getDayMileage())) {
                    info.setDayMileage("0.0");
                }
                if (StringUtils.isBlank(info.getDayOilWear())) {
                    info.setDayOilWear("0.0");
                }
                // ?????????????????????
                handleSensorData(info, sensors, id, false);
            }

            //???????????????????????????app????????????
            JSONObject monitorInfo = getMonitorInfo(id);
            List<AppMonitorConfigInfo> appMonitorConfigInfos = null;
            List<OrganizationLdap> superiorGroupIds = organizationService.getSuperiorOrg();
            //????????????????????????app??????????????????
            for (int i = 0; i < superiorGroupIds.size(); i++) {
                appMonitorConfigInfos = appMonitorDao
                    .getMonitorConfigByVersion(superiorGroupIds.get(i).getUuid(), 0, version,
                        monitorInfo.getString("type"));
                if (!appMonitorConfigInfos.isEmpty()) {
                    break;
                }
                if (i == superiorGroupIds.size() - 1) {
                    appMonitorConfigInfos =
                        appMonitorDao.getMonitorConfigByVersion(DEFAULT, 0, version, monitorInfo.getString("type"));
                }
            }
            JSONArray monitorConfigs = setMonitorShow(appMonitorConfigInfos, monitorInfo);
            re.put("sensors", sensors);
            re.put("monitorConfigs", monitorConfigs);
            return new AppResultBean(re);
        }
        return null;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_NINE, url = "/clbs/app/monitor/setDetailLocationInfo")
    public AppResultBean setDetailLocationInfo(String location, Integer version) throws Exception {
        // ??????????????????????????????
        if (StringUtils.isNotBlank(location)) { //???????????????????????????
            JSONObject re = new JSONObject();
            JSONArray sensors = new JSONArray();
            // ????????????????????????
            Message message = JSON.parseObject(location, Message.class);
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
            String id = info.getMonitorInfo().getMonitorId();
            // ?????????????????????????????????
            setDailyMileAndOil(id, info);
            if (StringUtils.isBlank(info.getDayMileage())) {
                info.setDayMileage("0.0");
            }
            if (StringUtils.isBlank(info.getDayOilWear())) {
                info.setDayOilWear("0.0");
            }
            // ?????????????????????
            handleSensorData(info, sensors, id, true);

            //???????????????????????????app????????????
            JSONObject monitorInfo = getMonitorInfo(id);
            List<AppMonitorConfigInfo> appMonitorConfigInfos = null;
            List<OrganizationLdap> superiorGroupIds = organizationService.getSuperiorOrg();
            //????????????????????????app??????????????????
            for (int i = 0; i < superiorGroupIds.size(); i++) {
                appMonitorConfigInfos = appMonitorDao
                    .getMonitorConfigByVersion(superiorGroupIds.get(i).getUuid(), 0, version,
                        monitorInfo.getString("type"));
                if (!appMonitorConfigInfos.isEmpty()) {
                    break;
                }
                if (i == superiorGroupIds.size() - 1) {
                    appMonitorConfigInfos =
                        appMonitorDao.getMonitorConfigByVersion(DEFAULT, 0, version, monitorInfo.getString("type"));
                }
            }
            JSONArray monitorConfigs = setMonitorShow(appMonitorConfigInfos, monitorInfo);
            re.put("sensors", sensors);
            re.put("monitorConfigs", monitorConfigs);
            return new AppResultBean(re);
        }
        return null;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = "/clbs/app/monitor/detailLocationInfo")
    public AppResultBean getDetailLocationInfoBefore(String id, Integer version) throws Exception {
        if (AppParamCheckUtil.check64String(id)) {
            // ???????????????
            DetailLocationInfo detailLocationInfo = new DetailLocationInfo();
            JSONArray sensors = new JSONArray();
            // ??????????????????????????????
            Map<String, String> configMap = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(id),
                Arrays.asList("OrgName", "groupName", "deviceNumber", "simCardNumber"));
            // ??????????????????????????????
            String locationValues = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(id));
            if (StringUtils.isNotBlank(locationValues)) { //???????????????????????????
                // ????????????????????????
                Message message = JSON.parseObject(locationValues, Message.class);
                T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                LocationInfo info = JSON.parseObject(t808Message.getMsgBody().toString(), LocationInfo.class);
                // ??????????????????
                MonitorInfo monitorInfo = info.getMonitorInfo();
                //????????????
                detailLocationInfo.setDeviceNo(monitorInfo.getDeviceNumber());
                //sim??????
                detailLocationInfo.setSim(monitorInfo.getSimcardNumber());
                //??????????????????
                detailLocationInfo.setOrg(configMap.get("orgName"));
                //??????????????????
                detailLocationInfo.setAssigns(configMap.get("groupName"));
                // ?????????????????????????????????
                setDailyMileAndOil(id, info);
                if (StringUtils.isBlank(info.getDayMileage())) {
                    info.setDayMileage("0.0");
                }
                if (StringUtils.isBlank(info.getDayOilWear())) {
                    info.setDayOilWear("0.0");
                }
                // ?????????????????????
                handleSensorDataOld(info, sensors, id);
            } else { //??????????????????????????????
                if (configMap != null) {
                    //????????????
                    detailLocationInfo.setDeviceNo(configMap.get("deviceNumber"));
                    //sim??????
                    detailLocationInfo.setSim(configMap.get("simCardNumber"));
                    //??????????????????
                    detailLocationInfo.setOrg(configMap.get("orgName"));
                    //??????????????????
                    detailLocationInfo.setAssigns(configMap.get("groupName"));
                }
            }
            detailLocationInfo.setSensors(sensors);
            return new AppResultBean(detailLocationInfo);

        }
        return new AppResultBean(AppResultBean.PARAM_ERROR);
    }

    /**
     * ????????????app??????????????????
     * @param appMonitorConfigInfos
     * @param monitorInfo
     * @return
     */
    private JSONArray setMonitorShow(List<AppMonitorConfigInfo> appMonitorConfigInfos, JSONObject monitorInfo) {
        JSONArray re = new JSONArray();
        String type = monitorInfo.getString("type");
        JSONObject basic = monitorInfo.getJSONObject("basic");
        JSONObject device = monitorInfo.getJSONObject("device");
        JSONObject simcard = monitorInfo.getJSONObject("simcard");
        //???????????????????????????????????????
        if (appMonitorConfigInfos == null || appMonitorConfigInfos.size() == 0) {
            appMonitorConfigInfos = appMonitorDao.getDefaultMonitorConfig("default", type);
        }
        for (AppMonitorConfigInfo appMonitorConfigInfo : appMonitorConfigInfos) {
            JSONObject info = new JSONObject();
            String category = appMonitorConfigInfo.getCategory();
            String name = appMonitorConfigInfo.getName();
            info.put("category", category);
            info.put("name", name);
            String value = null;
            if (category.equals("??????????????????")) {
                switch (name) {
                    case "????????????":
                        value = basic.getString("group");
                        break;
                    case "????????????":
                        value = basic.getString("assign");
                        break;
                    case "????????????":
                        value = basic.getString("createDate");
                        break;
                    case "????????????":
                        value = basic.getString("billingDate");
                        break;
                    case "????????????":
                        value = basic.getString("expireDate");
                        break;
                    case "????????????":
                        value = basic.getJSONObject("vehicle").getString("owner");
                        break;
                    case "????????????":
                        if (type.equals("0")) {
                            value = basic.getJSONObject("vehicle").getString("phone");
                        } else {
                            value = basic.getJSONObject("people").getString("phone");
                        }
                        break;
                    case "????????????":
                        value = basic.getJSONObject("vehicle").getString("category");
                        break;
                    case "????????????":
                        value = basic.getJSONObject("vehicle").getString("type");
                        break;
                    case "????????????":
                        value = basic.getJSONObject("thing").getString("name");
                        break;
                    case "????????????":
                        value = basic.getJSONObject("thing").getString("category");
                        break;
                    case "????????????":
                        value = basic.getJSONObject("thing").getString("type");
                        break;
                    case "??????":
                        value = basic.getJSONObject("thing").getString("brand");
                        break;
                    case "??????":
                        value = basic.getJSONObject("thing").getString("number");
                        break;
                    case "??????":
                        value = basic.getJSONObject("people").getString("name");
                        break;
                    case "????????????":
                        value = basic.getJSONObject("people").getString("id");
                        break;
                    case "??????":
                        value = basic.getJSONObject("people").getString("gender");
                        break;
                    default:
                        break;
                }
            }
            if (category.equals("????????????")) {
                switch (name) {
                    case "????????????":
                        value = device.getString("number");
                        break;
                    case "????????????":
                        value = device.getString("type");
                        break;
                    case "IMEI???":
                        value = simcard.getString("imei");
                        break;
                    default:
                        break;
                }
            }
            if (category.equals("?????????????????????")) {
                switch (name) {
                    case "???????????????":
                        value = simcard.getString("number");
                        break;
                    case "ICCID???":
                        value = simcard.getString("iccid");
                        break;
                    case "????????????":
                        value = simcard.getString("expireDate");
                        break;
                    default:
                        break;
                }
            }
            info.put("value", value);
            re.add(info);
        }
        return re;
    }

    @Override
    public JSONObject getMonitorInfo(String id) throws Exception {
        if (AppParamCheckUtil.check64String(id)) {
            // ???????????????
            JSONObject result = new JSONObject();
            // ???????????????????????????????????????
            BasicMonitorInfo info = new BasicMonitorInfo();
            BindDTO bindDTO = MonitorUtils.getBindDTO(id);

            if (bindDTO == null) {
                return null;
            }
            result.put("type", bindDTO.getMonitorType()); //??????????????????
            result.put("monitorName", bindDTO.getName()); //??????????????????
            info.setGroup(bindDTO.getOrgName());

            Map<String, String> userGroupIdAndNameMap = userService.getCurrentUserGroupList().stream()
                .collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
            String groupIds = bindDTO.getGroupId();
            String groupNames =
                Arrays.stream(groupIds.split(",")).map(userGroupIdAndNameMap::get).filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
            info.setAssign(groupNames);
            info.setCreateDate(bindDTO.getBindDate());
            info.setBillingDate(bindDTO.getBillingDate());
            info.setExpireDate(bindDTO.getExpireDate());
            // ?????????????????????????????????
            JSONObject monitor = new JSONObject();
            switch (bindDTO.getMonitorType()) {
                case "0":
                    VehicleDTO vehicleDTO = vehicleService.getById(bindDTO.getId());
                    if (vehicleDTO != null) {
                        monitor.put("owner", vehicleDTO.getVehicleOwner());
                        monitor.put("phone", vehicleDTO.getVehicleOwnerPhone());
                        monitor.put("category", vehicleDTO.getVehicleCategoryName());
                        monitor.put("type", vehicleDTO.getVehicleTypeName());
                        info.setVehicle(monitor);
                    }
                    break;
                case "1":
                    PeopleDTO peopleDTO = peopleService.getById(bindDTO.getId());
                    if (peopleDTO != null) {
                        monitor.put("name", peopleDTO.getName());
                        monitor.put("id", peopleDTO.getIdentity());
                        String gender = peopleDTO.getGender();
                        monitor.put("gender", gender == null ? "???" : ("1".equals(gender) ? "???" : "???"));
                        monitor.put("phone", peopleDTO.getPhone());
                        info.setPeople(monitor);
                    }
                    break;
                case "2":
                    ThingDTO thingDTO = thingService.getById(bindDTO.getId());
                    if (thingDTO != null) {
                        monitor.put("name", thingDTO.getName());
                        monitor.put("category", thingDTO.getCategoryName());
                        monitor.put("type", thingDTO.getTypeName());
                        monitor.put("brand", thingDTO.getLabel());
                        monitor.put("number", thingDTO.getModel());
                        info.setThing(monitor);
                    }
                    break;
                default:
                    break;
            }
            result.put("basic", info);//????????????????????????
            // ???????????????????????????
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("number", bindDTO.getDeviceNumber());
            deviceInfo.put("type", Integer.parseInt(bindDTO.getDeviceType()));
            result.put("device", deviceInfo);
            // ??????sim??????????????????

            SimCardDTO simCardDTO = simCardService.getById(bindDTO.getSimCardId());
            if (simCardDTO != null) {
                JSONObject data = new JSONObject();
                data.put("number", simCardDTO.getSimcardNumber());
                data.put("imei", simCardDTO.getImei());
                data.put("iccid", simCardDTO.getIccid());
                data.put("expireDate", simCardDTO.getEndTime() == null ? null
                    : DateUtil.getDateToString(simCardDTO.getEndTime(), DateUtil.DATE_Y_M_D_FORMAT));
                result.put("simcard", data);
            }
            // ?????????????????????????????????
            if ("0".equals(bindDTO.getMonitorType()) || "2".equals(bindDTO.getMonitorType())) {
                String professional = bindDTO.getProfessionalIds();
                if (StringUtils.isNotBlank(professional)) {
                    List<String> proIds = Arrays.asList(professional.trim().split(","));
                    JSONArray proArr = new JSONArray();
                    if (CollectionUtils.isNotEmpty(proIds)) {
                        List<ProfessionalDTO> professionalDTOS = professionalService.getProfessionalByIds(proIds);
                        for (ProfessionalDTO proInfo : professionalDTOS) {
                            JSONObject data = new JSONObject();
                            data.put("name", proInfo.getName());
                            data.put("phone", proInfo.getPhone());
                            proArr.add(data);
                        }
                    }
                    result.put("employee", proArr);
                }
            }
            // ????????????
            return result;
        }
        return null;
    }

    @Override
    public JSONObject getHistoryLocation(String id, String startTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            // ??????????????????
            long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
            long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
            // ????????????????????????
            List<Positional> locationInfos = this.listHistoryLocation(id, start, end);
            // ????????????????????????
            JSONObject result = new JSONObject();
            JSONArray locations = new JSONArray();
            if (locationInfos != null && locationInfos.size() > 0) {
                for (Positional positional : locationInfos) {
                    JSONObject location = new JSONObject();
                    location.put("time", positional.getVtime());
                    location.put("longitude", positional.getLongtitude());
                    location.put("latitude", positional.getLatitude());
                    location.put("speed", positional.getSpeed());
                    locations.add(location);
                }
                result.put("locations", locations);
            }
            return result;
        }
        return null;
    }

    private List<Positional> listHistoryLocation(String monitorId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_HISTORY_LOCATION, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public JSONObject getMileageHistoryData(String id, String startTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            // ???????????????
            JSONObject result = new JSONObject();
            // ??????????????????
            long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
            long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
            // ????????????????????????
            List<Positional> locationInfos = this.listMileageHistoryData(id, start, end);
            // ???????????????????????????
            // ???????????????????????????????????????????????????
            Boolean flag = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(id));
            // ????????????
            JSONArray mileage = new JSONArray();
            for (Positional p : locationInfos) {
                JSONObject data = new JSONObject();
                if (flag) {
                    data.put("speed", p.getMileageSpeed() == null ? "0.0" : p.getMileageSpeed());
                    data.put("total", p.getMileageTotal() == null ? "0.0" : p.getMileageTotal());
                } else {
                    String speed = p.getSpeed();
                    if (StringUtils.isBlank(speed) || "null".equals(speed)) {
                        speed = "0.0";
                    }
                    String total = p.getGpsMile();
                    if (StringUtils.isBlank(total) || "null".equals(total)) {
                        total = "0.0";
                    }
                    data.put("speed", speed);
                    data.put("total", total);
                }
                data.put("time", p.getVtime());
                mileage.add(data);
            }
            if (mileage.size() > 0) {
                result.put("mileage", mileage);
            }
            return result;
        }
        return null;
    }

    private List<Positional> listMileageHistoryData(String monitorId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_MILEAGE_HISTORY_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = "/clbs/app/monitor/history/stop")
    public JSONObject getStopHistoryData(String id, String startTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            // ???????????????
            JSONObject result = new JSONObject();//??????????????????
            JSONArray track = new JSONArray();//???????????????
            JSONArray pointArray = new JSONArray();//??????????????????????????????
            Integer travelStopType = null;//???????????? 0:?????? 1?????????
            Integer travelStopChangeNum = null;//??????????????? ??????????????????3 ??????????????????5
            // ??????????????????
            long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
            long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
            // ?????????????????????????????????????????????
            // ????????????????????????
            List<Positional> locationInfos = this.listStopHistoryData(id, start, end);
            if (locationInfos == null || locationInfos.size() <= 0) {
                return null;
            }
            // ?????????????????????????????????????????????
            Boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(id));
            // ????????????
            for (int i = 0; i < locationInfos.size(); i++) {
                Positional positional = locationInfos.get(i);
                // ???????????????????????????????????????????????????????????????
                Double speed;
                if (flogKey) {
                    speed = Optional.ofNullable(positional.getMileageSpeed()).orElse(0.0);
                } else {
                    speed = positional.getSpeed() == null ? 0.0 : Double.parseDouble(positional.getSpeed());
                }
                // ??????????????????(??????????????????????????????)
                if (i == 0) {
                    if (speed <= 5.0) { //??????
                        travelStopType = STOP_TYPE;
                        travelStopChangeNum = STOP_CHANGE_NUM;
                    } else { //??????
                        travelStopType = TRAVEL_TYPE;
                        travelStopChangeNum = TRAVEL_CHANGE_NUM;
                    }
                }
                // ????????????/???????????????????????????
                if (travelStopType == 0) { //????????????
                    if (speed > 5.0) { //??????
                        //?????????????????????
                        saveStatusPoint(pointArray, positional);
                    } else { //??????
                        saveTrackData(track, pointArray, positional, travelStopType, false);
                    }

                } else { //????????????
                    if (speed <= 5.0) { //??????
                        //?????????????????????
                        saveStatusPoint(pointArray, positional);
                    } else { //??????
                        saveTrackData(track, pointArray, positional, travelStopType, false);
                    }
                }
                // ?????????????????????????????????????????????
                if (pointArray.size() >= travelStopChangeNum) {
                    //??????????????????????????????????????????????????????
                    Integer changeType = travelStopType == 0 ? 1 : 0;
                    saveTrackData(track, pointArray, positional, changeType, true);
                    //????????????????????????????????????
                    if (travelStopType == 0) {
                        travelStopType = TRAVEL_TYPE;
                        travelStopChangeNum = TRAVEL_CHANGE_NUM;
                    } else {
                        travelStopType = STOP_TYPE;
                        travelStopChangeNum = STOP_CHANGE_NUM;
                    }
                }
                // ????????????????????????(????????????????????????????????????????????????????????????)
                if (i == locationInfos.size() - 1) {
                    saveTrackData(track, pointArray, positional, travelStopType, true);
                }
            }

            // ???????????????
            if (track.size() > 0) {
                result.put("track", track);
            }
            return result;
        }
        return null;
    }

    private List<Positional> listStopHistoryData(String monitorId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_STOP_HISTORY_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    /**
     * ???????????????
     */
    private void saveStatusPoint(JSONArray pointArray, Positional positional) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("time", positional.getVtime());
        pointArray.add(jsonObject);
    }

    /**
     * ???????????????
     */
    private void saveTrackPoint(JSONArray track, JSONArray pointArray, Integer travelStopType) {
        for (int i = 0; i < pointArray.size(); i++) {
            JSONObject jsonObject = pointArray.getJSONObject(i);
            jsonObject.put("type", travelStopType);
            track.add(jsonObject);
        }
    }

    /**
     * ??????????????????
     * @param track
     * @param pointArray
     * @param positional
     * @param travelStopType
     * @param isChange
     */
    private void saveTrackData(JSONArray track, JSONArray pointArray, Positional positional, Integer travelStopType,
        boolean isChange) {
        //???????????????(?????????????????????????????????????????????????????????????????????????????????????????????)
        if (!isChange) {
            saveStatusPoint(pointArray, positional);
        }
        //???????????????
        saveTrackPoint(track, pointArray, travelStopType);
        //???????????????
        pointArray.clear();
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_FIVE, url = "/clbs/app/monitor/history/stop")
    public JSONObject getStopHistoryDataToVersion(String monitorId, String startTime, String endTime) throws Exception {
        return monitorHardwareDataService.getStopHistoryData(monitorId, startTime, endTime);
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SIX, url = "/clbs/app/monitor/loadWeight")
    public AppResultBean getLoadWeightDate(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception {
        return historyService.appLoadWeightDate(vehicleId, startTime, endTime, sensorFlag);
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_SIX, url = "/clbs/app/monitor/tirePressureData")
    public AppResultBean getTirePressureData(String vehicleId, String startTime, String endTime, Integer sensorFlag)
        throws Exception {
        return historyService.appTirePressureData(vehicleId, startTime, endTime, sensorFlag);
    }

    @Override
    public JSONObject getOilHistoryData(String id, String startTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            // ???????????????
            JSONObject result = new JSONObject();
            JSONArray oilMass = new JSONArray();
            // ??????????????????
            long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
            long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
            // ?????????????????????????????????????????????
            // ??????????????????????????????????????????
            String oilType = monitorManagementMysqlDao.getOilBoxType(id);
            if (StringUtils.isNotBlank(oilType)) {
                // ?????????????????????????????????????????????????????????
                boolean tankOne = false;
                boolean tankTwo = false;
                if (oilType.contains("1")) {
                    tankOne = true;
                }
                if (oilType.contains("2")) {
                    tankTwo = true;
                }
                if (!tankOne) {
                    return result;
                }
                // ????????????????????????
                List<Positional> locationInfos = this.listOilHistoryData(id, start, end);
                for (Positional p : locationInfos) {
                    JSONObject oilData = new JSONObject();
                    oilData.put("time", p.getVtime());
                    JSONArray oilTank = new JSONArray();
                    JSONArray fuelAmount = new JSONArray();
                    JSONArray fuelSpill = new JSONArray();
                    if (tankOne) {
                        Double oilTankOne = p.getOilTankOne() == null ? null : Double.valueOf(p.getOilTankOne());
                        Double fuelAmountOne =
                            p.getFuelAmountOne() == null ? null : Double.valueOf(p.getFuelAmountOne());
                        Double fuelSpillOne = p.getFuelSpillOne() == null ? null : Double.valueOf(p.getFuelSpillOne());
                        oilTank.add(oilTankOne);
                        fuelAmount.add(fuelAmountOne);
                        fuelSpill.add(fuelSpillOne);
                    }
                    if (tankTwo) {
                        Double oilTankTwo = p.getOilTankTwo() == null ? null : Double.valueOf(p.getOilTankTwo());
                        Double fuelAmountTwo =
                            p.getFuelAmountTwo() == null ? null : Double.valueOf(p.getFuelAmountTwo());
                        Double fuelSpillTwo = p.getFuelSpillTwo() == null ? null : Double.valueOf(p.getFuelSpillTwo());
                        oilTank.add(oilTankTwo);
                        fuelAmount.add(fuelAmountTwo);
                        fuelSpill.add(fuelSpillTwo);
                    }
                    oilData.put("oilTank", oilTank.size() > 0 ? oilTank : null);
                    oilData.put("fuelAmount", fuelAmount.size() > 0 ? fuelAmount : null);
                    oilData.put("fuelSpill", fuelSpill.size() > 0 ? fuelSpill : null);
                    oilMass.add(oilData);
                }
            }
            if (oilMass.size() > 0) {
                result.put("oilMass", oilMass);
            }
            return result;
        }
        return null;
    }

    private List<Positional> listOilHistoryData(String monitorId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_OIL_HISTORY_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public JSONObject getOilConsumeHistoryData(String id, String startTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            // ???????????????
            JSONObject result = new JSONObject();
            JSONArray oilConsume = new JSONArray();
            // ??????????????????
            long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
            long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
            // ?????????????????????????????????????????????
            String oilWearId = monitorManagementMysqlDao.getOilWearId(id);
            if (StringUtils.isNotBlank(oilWearId)) {
                // ?????????????????????????????????????????????
                // ????????????????????????
                List<Positional> locationInfos = this.listOilConsumeHistoryData(id, start, end);
                // ?????????????????????????????????????????????
                Boolean flogKey = RedisHelper.isContainsKey(HistoryRedisKeyEnum.SENSOR_MESSAGE.of(id));
                // ??????????????????
                for (Positional p : locationInfos) {
                    JSONObject data = new JSONObject();
                    data.put("time", p.getVtime());
                    String totalOilwearOne = p.getTotalOilwearOne();
                    if (StringUtils.isNotBlank(totalOilwearOne) && !"null".equals(totalOilwearOne)) {
                        //                        BigDecimal oilValue = new BigDecimal(Double.valueOf(totalOilwearOne));
                        data.put("amount", totalOilwearOne);
                    } else {
                        data.put("amount", null);
                    }
                    if (flogKey) {
                        data.put("mileage", p.getMileageTotal());
                    } else {
                        String mileage = p.getGpsMile();
                        if (StringUtils.isBlank(mileage) || "null".equals(mileage)) {
                            mileage = null;
                        }
                        data.put("mileage", mileage);
                    }
                    oilConsume.add(data);
                }
                if (oilConsume.size() > 0) {
                    result.put("oilConsume", oilConsume);
                }
            }
            return result;
        }
        return null;
    }

    private List<Positional> listOilConsumeHistoryData(String monitorId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_OIL_CONSUME_HISTORY_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public JSONObject getTemperatureHistoryData(String id, String startTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            // ???????????????
            JSONObject result = new JSONObject();
            List<String> sensorName = new ArrayList<>();
            List<HumidityTemperatureThreshold> threshold = new ArrayList<>();
            // ??????????????????id????????????????????????????????????????????????
            List<TransdusermonitorSet> temperatureVehicleList = sensorSettingsDao.findByVehicleId(1, id);
            List<String> sensorOutId = new ArrayList<>();
            if (temperatureVehicleList.size() > 0) {
                // ?????????????????????????????????????????????????????????????????????id
                HumidityTemperatureThreshold humidityThreshold;
                for (TransdusermonitorSet data : temperatureVehicleList) {
                    humidityThreshold = new HumidityTemperatureThreshold();
                    if (StringUtils.isNotBlank(data.getSensorNumber()) && StringUtils.isNotBlank(data.getSensorId())) {
                        humidityThreshold.setHigh(data.getAlarmUp().toString());
                        humidityThreshold.setLow(data.getAlarmDown().toString());
                        threshold.add(humidityThreshold);
                        sensorName.add(data.getSensorOutId());
                        sensorOutId.add(data.getSensorOutId());
                    }
                }
                result.put("nickname", sensorName);
                result.put("threshold", threshold);
                // ?????????????????????????????????????????????
                long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
                long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
                List<Positional> queryResult = this.listTemperatureData(id, start, end);
                List<HumidityTemperatureData> poovessResult = new ArrayList<>();
                if (queryResult.size() > 0) {
                    poovessResult = processTemperature(queryResult, sensorOutId);
                }
                result.put("temprature", poovessResult.size() > 0 ? poovessResult : null);
            }
            return result;
        }
        return null;
    }

    private List<Positional> listTemperatureData(String monitorId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_TEMPERATURE_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    @Override
    public JSONObject getChannelData(String id) throws Exception {
        if (AppParamCheckUtil.check64String(id)) {
            // 
            JSONObject result = new JSONObject();
            JSONArray channelList = new JSONArray();
            // ????????????????????????????????????????????????
            List<VideoChannelSetting> videoChannelSettingList = videoChannelSettingDao.getVideoChannelByVehicleId(id);
            String statusData = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_STATUS.of(id));
            Integer status = 3;
            String deviceType = null;
            if (StringUtils.isNotBlank(statusData)) {
                ClientVehicleInfo clientVehicleInfo = JSON.parseObject(statusData, ClientVehicleInfo.class);
                status = clientVehicleInfo.getVehicleStatus();
                deviceType = clientVehicleInfo.getDeviceType();
            }
            // ??????????????????
            for (VideoChannelSetting vcs : videoChannelSettingList) {
                JSONObject channel = new JSONObject();
                channel.put("id", vcs.getVehicleId());
                channel.put("physicsChannel", vcs.getPhysicsChannel());
                channel.put("logicChannel", vcs.getLogicChannel());
                channel.put("channelType", vcs.getChannelType());
                channel.put("panoramic", vcs.getPanoramic());
                channel.put("sort", vcs.getSort());
                channel.put("streamType", vcs.getStreamType());
                channel.put("mobile", vcs.getMobile());
                channel.put("logicChannelName", AudioVideoUtil.getLogicChannelName(vcs.getLogicChannel()));
                channel.put("status", status);
                channel.put("deviceType", deviceType);
                channelList.add(channel);
            }
            // ?????????????????????
            result.put("channelList", channelList);
            return result;
        }
        return null;
    }

    @Override
    public JSONObject getMileDayStatistics(String id, String startDate, String endDate) throws Exception {

        boolean flag = AppParamCheckUtil.check64String(id)
            && AppParamCheckUtil.checkDate(startDate, 2) && AppParamCheckUtil
            .checkDate(endDate, 2);
        if (!flag) {
            return null;
        }
        // ????????????????????????
        String deviceType = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(id), "deviceType");
        if (StringUtils.isBlank(deviceType)) {
            return null;
        }
        // ??????????????????????????????
        JsonResultBean data =
            historyService.changeHistoryActiveDate(id, startDate, endDate, deviceType, 1, true);
        JSONObject result = new JSONObject();
        Object resultData = "";
        if (data.isSuccess()) {
            resultData = JSONObject.parseObject(data.getMsg());
        }
        result.put("mileDayStatistics", resultData);
        return result;
    }

    @Override
    public Integer checkMonitorOnline(String id) throws Exception {
        if (AppParamCheckUtil.check64String(id)) {
            // ??????????????????????????????
            String statusData = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_STATUS.of(id));

            if (StringUtils.isBlank(statusData)) {
                // ????????????????????????????????????
                String locationData = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(id));
                if (StringUtils.isBlank(locationData)) {
                    return 4;
                }
                return 2;
            }

            // ???????????????????????????808??????
            ClientVehicleInfo clientVehicleInfo = JSON.parseObject(statusData, ClientVehicleInfo.class);
            String devType = clientVehicleInfo.getDeviceType();
            List<Integer> protocols = Arrays.asList(ProtocolEnum.PROTOCOL_TYPE_808);
            if (!protocols.contains(Integer.parseInt(devType))) {
                return 3;
            }
            // ????????????
            return 1;
        }
        return null;
    }

    @Override
    public Integer checkMonitorAuth(String id) throws Exception {
        if (AppParamCheckUtil.check64String(id)) {
            // ????????????????????????????????????
            String bindType = RedisHelper.hget(RedisKeyEnum.MONITOR_INFO.of(id), "bindType");
            if (bindType.equals("0")) {
                return 2;
            }
            // ????????????????????????????????????????????????
            Set<String> allMonitorIds = userService.getCurrentUserMonitorIds();
            if (!allMonitorIds.contains(id)) {
                return 3;
            }
            return 1;
        }
        return null;
    }

    /**
     * ?????????????????????
     * @param info
     * @param sensors
     */
    private void handleSensorDataOld(LocationInfo info, JSONArray sensors, String monitorId) {
        // ???????????????
        MileageSensor mileageSensor = info.getMileageSensor();
        SensorData mileageData = new SensorData();
        mileageData.setType("mileage");
        SensorData speedData = new SensorData();
        speedData.setType("speed");
        if (mileageSensor != null) { //??????????????????
            if (mileageSensor.getUnusual() != null && mileageSensor.getUnusual() == 0) { //??????
                mileageData.setValue(info.getDayMileage() + " km");
                speedData.setValue(mileageSensor.getSpeed() + " km/h");
            } else { //??????
                mileageData.setValue(info.getDayMileage() + " km");
                speedData.setValue(info.getGpsSpeed() == null ? "" : info.getGpsSpeed().toString() + " km/h");
            }
            String mileUnusualName = AppMonitorUtil.getSensorUnusualName(mileageSensor.getUnusual(), 1);
            mileageData.setStatus(mileUnusualName);
            speedData.setStatus(mileUnusualName);
            mileageData.setName("1#");
            speedData.setName("1#");
        } else { //?????????????????????
            mileageData.setValue(info.getDayMileage() + " km");
            speedData.setValue(info.getGpsSpeed() == null ? "" : info.getGpsSpeed().toString() + " km/h");
            mileageData.setName("");
            speedData.setName("");
        }
        sensors.add(mileageData);
        sensors.add(speedData);

        //??????????????????????????????????????????acc????????????
        if (info.getMonitorInfo().getMonitorType() == 0) {
            //acc??????
            SensorData accData = new SensorData();
            accData.setType("acc");
            Integer acc = info.getAcc();
            if (acc != null) {
                switch (acc) {
                    case 0:
                        accData.setValue("???");
                        break;
                    case 1:
                        accData.setValue("???");
                        break;
                    default:
                        break;
                }
            }
            sensors.add(accData);
        }

        // ???????????????
        JSONArray oilMassArr = info.getOilMass();
        if (oilMassArr != null && oilMassArr.size() > 0) {
            for (int i = 0; i < oilMassArr.size(); i++) {
                OilMassSensorData oilMassData = new OilMassSensorData();
                JSONObject values = (JSONObject) oilMassArr.get(i);
                oilMassData.setType("oil-mass");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                oilMassData.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 2));
                if (unusual != null && unusual == 0 && values.get("oilMass") != null) { //???????????????????????????
                    Double oilMass = Double.valueOf(values.get("oilMass").toString());
                    BigDecimal bigDecimal = new BigDecimal(oilMass);
                    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
                    oilMassData.setValue(bigDecimal.doubleValue() + " L");
                    oilMassData.setDayOilWear(info.getDayOilWear());
                }
                oilMassData.setName(AppMonitorUtil.getOilMassName(values.get("id").toString()));
                sensors.add(oilMassData);
            }
        }

        // ???????????????
        JSONArray oilExpendArr = info.getOilExpend();
        if (oilExpendArr != null && oilExpendArr.size() > 0) {
            for (int i = 0; i < oilExpendArr.size(); i++) {
                SensorData oilExpendData = new SensorData();
                JSONObject values = (JSONObject) oilExpendArr.get(i);
                oilExpendData.setType("oil-consume");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                oilExpendData.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 3));
                if (unusual != null && unusual == 0) { //???????????????????????????
                    //                    BigDecimal oilValue = new BigDecimal(Double.valueOf(info.getDayOilWear()));
                    oilExpendData.setValue(info.getDayOilWear() + " L");
                }
                oilExpendData.setName(AppMonitorUtil.getOilConsumeName(values.get("id").toString()));
                sensors.add(oilExpendData);
            }
        }

        // ???????????????
        JSONArray temperatureArr = info.getTemperatureSensor();
        if (temperatureArr != null && temperatureArr.size() > 0) {
            for (int i = 0; i < temperatureArr.size(); i++) {
                SensorData temperatureData = new SensorData();
                JSONObject values = (JSONObject) temperatureArr.get(i);
                temperatureData.setType("temperature");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                temperatureData.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 4));
                if (unusual != null && unusual == 0 && values.get("temperature") != null) { //???????????????????????????
                    Double temperatureValue = Double.valueOf(values.get("temperature").toString()) / 10;
                    temperatureData.setValue(temperatureValue + " ??C");
                }
                temperatureData.setName(AppMonitorUtil.getTemperatureName(values.get("id").toString()));
                sensors.add(temperatureData);
            }
        }

        // ???????????????
        JSONArray humidityArr = info.getTemphumiditySensor();
        if (humidityArr != null && humidityArr.size() > 0) {
            for (int i = 0; i < humidityArr.size(); i++) {
                SensorData humidityData = new SensorData();
                JSONObject values = (JSONObject) humidityArr.get(i);
                humidityData.setType("humidity");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                humidityData.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 5));
                if (unusual != null && unusual == 0 && values.get("temperature") != null) { //???????????????????????????
                    Double humidityValue = Double.valueOf(values.get("temperature").toString());
                    humidityData.setValue(humidityValue + " %");
                }
                humidityData.setName(AppMonitorUtil.getHumidityName(values.get("id").toString()));
                sensors.add(humidityData);
            }
        }

        // ???????????????
        JSONArray workHourArr = info.getWorkHourSensor();
        if (workHourArr != null && workHourArr.size() > 0) {
            for (int i = 0; i < workHourArr.size(); i++) {
                SensorData workHourData = new SensorData();
                JSONObject values = (JSONObject) workHourArr.get(i);
                workHourData.setType("work-hour");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                String workTime = "";
                if (unusual != null && unusual == 0) { //???????????????????????????
                    switch (values.get("workingPosition").toString()) {
                        case "0":
                            Double hour0 = DateUtil.getHour(Integer.parseInt(
                                values.get("continueTime") == null ? "0" : values.get("continueTime").toString()));
                            workTime = hour0 + " h";
                            workHourData.setStatus("??????");
                            break;
                        case "1":
                            Double hour1 = DateUtil.getHour(Integer.parseInt(
                                values.get("continueTime") == null ? "0" : values.get("continueTime").toString()));
                            workTime = hour1 + " h";
                            workHourData.setStatus("??????");
                            break;
                        case "2":
                            Double hour3 = DateUtil.getHour(Integer.parseInt(
                                values.get("continueTime") == null ? "0" : values.get("continueTime").toString()));
                            workTime = hour3 + " h";
                            workHourData.setStatus("??????");
                            break;
                        default:
                            break;
                    }
                    workHourData.setValue(workTime);
                }
                workHourData.setName(AppMonitorUtil.getWorkHourName(values.get("id").toString()));
                sensors.add(workHourData);
            }
        }

        // ??????????????????
        JSONArray positiveNegativeArr = info.getPositiveNegative();
        if (positiveNegativeArr != null && positiveNegativeArr.size() > 0) {
            for (int i = 0; i < positiveNegativeArr.size(); i++) {
                SensorData positiveNegativeData = new SensorData();
                JSONObject values = (JSONObject) positiveNegativeArr.get(i);
                positiveNegativeData.setType("motor");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                if (unusual != null && unusual == 0) { //???????????????????????????
                    StringBuilder stringBuilder = new StringBuilder();
                    String winchTime = values.getString("winchTime");
                    if (StringUtils.isBlank(winchTime)) {
                        winchTime = "0";
                    }
                    Integer resultNumber;
                    if (winchTime.contains(".")) { // ???????????????
                        Double formatNumber = Double.parseDouble(winchTime);
                        resultNumber = formatNumber.intValue();
                    } else {
                        resultNumber = Integer.parseInt(winchTime);
                    }
                    BigDecimal bigDecimal = new BigDecimal(resultNumber);
                    Double hour = bigDecimal.doubleValue();
                    stringBuilder.append(hour).append(" h");
                    positiveNegativeData.setValue(stringBuilder.toString());
                    if ("2".equals(values.get("spinState").toString())) {
                        if ("1".equals(values.get("spinDirection").toString())) {
                            positiveNegativeData.setStatus("??????");
                        } else {
                            positiveNegativeData.setStatus("??????");
                        }
                    } else {
                        positiveNegativeData.setStatus("??????");
                    }
                }
                sensors.add(positiveNegativeData);
            }
        }

        // ????????????????????????????????????IO???????????????????????????
        List<IoSensorConfigInfo> ioSensorConfigInfos = switchingSignalDao.getIoSensorConfigInfo(monitorId);
        Map<Integer, JSONObject> signalIos = new HashMap<>();//??????io????????????
        Map<Integer, JSONObject> cirIos1 = new HashMap<>();//??????io?????????1????????????
        Map<Integer, JSONObject> cirIos2 = new HashMap<>();//??????io?????????2????????????
        for (IoSensorConfigInfo ioData : ioSensorConfigInfos) { // ??????????????????????????????????????????key
            JSONObject data = new JSONObject();
            if (ioData.getHighSignalType() == 1) {
                data.put("high", ioData.getStateOne());
                data.put("low", ioData.getStateTwo());
            } else {
                data.put("high", ioData.getStateTwo());
                data.put("low", ioData.getStateOne());
            }
            data.put("name", ioData.getName());
            switch (ioData.getIoType()) {
                case 1:
                    signalIos.put(ioData.getIoSite(), data);
                    break;
                case 2:
                    cirIos1.put(ioData.getIoSite(), data);
                    break;
                case 3:
                    cirIos2.put(ioData.getIoSite(), data);
                    break;
                default:
                    break;
            }
        }

        // IO?????????
        JSONArray ioSignalDataArr = info.getIoSignalData();
        if (ioSignalDataArr != null && ioSignalDataArr.size() > 0 && signalIos.size() > 0) {
            for (int i = 0; i < ioSignalDataArr.size(); i++) {
                JSONObject values = (JSONObject) ioSignalDataArr.get(i);
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                if (unusual != null && unusual == 0) { //???????????????????????????
                    for (Map.Entry<Integer, JSONObject> entry : signalIos.entrySet()) {
                        String signal = values.get("signal" + entry.getKey()).toString();
                        SensorData ioSignalData = new SensorData();
                        ioSignalData.setType("ioSignal");
                        if ("0".equals(signal)) {
                            ioSignalData.setValue(entry.getValue().get("high").toString());
                            ioSignalData.setStatus("???");
                        } else if ("1".equals(signal)) {
                            ioSignalData.setValue(entry.getValue().get("low").toString());
                            ioSignalData.setStatus("???");
                        }
                        //                        ioSignalData.setName(entry.getValue().get("name").toString());
                        ioSignalData.setName("");
                        sensors.add(ioSignalData);
                    }
                }
            }
        }

        // ??????IO?????????
        JSONArray cirIoCheckArr = info.getCirIoCheckData();
        if (cirIoCheckArr != null && cirIoCheckArr.size() > 0) {
            for (int i = 0; i < cirIoCheckArr.size(); i++) {
                JSONObject values = (JSONObject) cirIoCheckArr.get(i);
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                if (unusual != null && unusual == 0) { //???????????????????????????
                    Map<Integer, JSONObject> cirIosData = new HashMap<>();
                    int nameNumber = 0; //?????????????????????
                    if ("145".equals(values.get("id").toString())) { //???????????????1
                        cirIosData = cirIos1;
                        nameNumber = 1;
                    } else if ("146".equals(values.get("id").toString())) { //???????????????2
                        cirIosData = cirIos2;
                        nameNumber = 2;
                    }
                    if (cirIosData.size() > 0) { //??????????????????????????????????????????
                        JSONArray statusList = JSONArray.parseArray(values.get("statusList").toString());
                        JSONObject ioStatusData = JSONObject.parseObject(statusList.get(0).toString());
                        Long ioStatus = Long.parseLong((ioStatusData.get("ioStatus").toString()));
                        for (int j = 0; j < 32; j++) { //????????????????????????32????????????????????????
                            if (cirIosData.containsKey(j)) {
                                SensorData data = new SensorData();
                                data.setType("ioSignal");
                                if ((int) (ioStatus & (long) Math.pow(2, j)) == 0) { //?????????
                                    Object hign = cirIosData.get(j).get("high");
                                    data.setValue(hign == null ? "" : hign.toString());
                                    data.setStatus("???");
                                } else { //?????????
                                    Object low = cirIosData.get(j).get("low");
                                    data.setValue(low == null ? "" : low.toString());
                                    data.setStatus("???");
                                }
                                if (nameNumber == 1) {
                                    data.setName("1#");
                                } else if (nameNumber == 2) {
                                    data.setName("2#");
                                }
                                sensors.add(data);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * ?????????????????????
     * @param info
     * @param sensors
     */
    private void handleSensorData(LocationInfo info, JSONArray sensors, String monitorId, boolean isOriginal) {
        // ???????????????
        MileageSensor mileageSensor = info.getMileageSensor();
        SensorData mileageData = new SensorData();
        mileageData.setType("mileage");
        SensorData speedData = new SensorData();
        speedData.setType("speed");
        if (mileageSensor != null) { //??????????????????
            if (mileageSensor.getUnusual() != null && mileageSensor.getUnusual() == 0) { //??????
                mileageData.setValue(info.getDayMileage() + " km");
                speedData.setValue(mileageSensor.getSpeed() + " km/h");
            } else { //??????
                mileageData.setValue(info.getDayMileage() + " km");
                speedData.setValue(info.getGpsSpeed() == null ? "" : info.getGpsSpeed().toString() + " km/h");
            }
            String mileUnusualName = AppMonitorUtil.getSensorUnusualName(mileageSensor.getUnusual(), 1);
            mileageData.setStatus(mileUnusualName);
            speedData.setStatus(mileUnusualName);
            mileageData.setName("1#");
            speedData.setName("1#");
        } else { //?????????????????????
            mileageData.setValue(info.getDayMileage() + " km");
            speedData.setValue(info.getGpsSpeed() == null ? "" : info.getGpsSpeed().toString() + " km/h");
            mileageData.setName("");
            speedData.setName("");
        }
        sensors.add(mileageData);
        sensors.add(speedData);

        //??????????????????????????????????????????acc????????????
        if (info.getMonitorInfo().getMonitorType() == 0) {
            //acc??????
            SensorData accData = new SensorData();
            accData.setType("acc");
            Integer acc = info.getAcc();
            if (acc != null) {
                switch (acc) {
                    case 0:
                        accData.setValue("???");
                        break;
                    case 1:
                        accData.setValue("???");
                        break;
                    default:
                        break;
                }
            }
            sensors.add(accData);
        }

        // ???????????????
        JSONArray oilMassArr = info.getOilMass();
        if (oilMassArr != null && oilMassArr.size() > 0) {
            for (int i = 0; i < oilMassArr.size(); i++) {
                OilMassSensorData oilMassData = new OilMassSensorData();
                JSONObject values = (JSONObject) oilMassArr.get(i);
                oilMassData.setType("oil-mass");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                oilMassData.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 2));
                if (unusual != null && unusual == 0 && values.get("oilMass") != null) { //???????????????????????????
                    Double oilMass = Double.valueOf(values.get("oilMass").toString());
                    BigDecimal bigDecimal = new BigDecimal(oilMass);
                    bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
                    oilMassData.setValue(bigDecimal.doubleValue() + " L");
                    oilMassData.setDayOilWear(info.getDayOilWear());
                }
                oilMassData.setName(AppMonitorUtil.getOilMassName(values.get("id").toString()));
                sensors.add(oilMassData);
            }
        }

        // ???????????????
        JSONArray oilExpendArr = info.getOilExpend();
        if (oilExpendArr != null && oilExpendArr.size() > 0) {
            for (int i = 0; i < oilExpendArr.size(); i++) {
                SensorData oilExpendData = new SensorData();
                JSONObject values = (JSONObject) oilExpendArr.get(i);
                oilExpendData.setType("oil-consume");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                oilExpendData.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 3));
                if (unusual != null && unusual == 0) { //???????????????????????????
                    //                    BigDecimal oilValue = new BigDecimal(Double.valueOf(info.getDayOilWear()));
                    oilExpendData.setValue(info.getDayOilWear() + " L");
                }
                oilExpendData.setName(AppMonitorUtil.getOilConsumeName(values.get("id").toString()));
                sensors.add(oilExpendData);
            }
        }

        // ???????????????
        JSONArray temperatureArr = info.getTemperatureSensor();
        if (temperatureArr != null && temperatureArr.size() > 0) {
            for (int i = 0; i < temperatureArr.size(); i++) {
                SensorData temperatureData = new SensorData();
                JSONObject values = (JSONObject) temperatureArr.get(i);
                temperatureData.setType("temperature");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                temperatureData.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 4));
                if (unusual != null && unusual == 0 && values.get("temperature") != null) { //???????????????????????????
                    Double temperatureValue;
                    if (isOriginal) {
                        temperatureValue = Double.valueOf(values.get("temperature").toString());
                    } else {
                        temperatureValue = Double.valueOf(values.get("temperature").toString()) / 10;
                    }
                    String value = String.format("%.1f", temperatureValue);
                    temperatureData.setValue(value + " ??C");
                }
                temperatureData.setName(AppMonitorUtil.getTemperatureName(values.get("id").toString()));
                sensors.add(temperatureData);
            }
        }

        // ???????????????
        JSONArray humidityArr = info.getTemphumiditySensor();
        if (humidityArr != null && humidityArr.size() > 0) {
            for (int i = 0; i < humidityArr.size(); i++) {
                SensorData humidityData = new SensorData();
                JSONObject values = (JSONObject) humidityArr.get(i);
                humidityData.setType("humidity");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                humidityData.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 5));
                if (unusual != null && unusual == 0 && values.get("temperature") != null) { //???????????????????????????
                    Double humidityValue = Double.valueOf(values.get("temperature").toString());
                    humidityData.setValue(humidityValue + " %");
                }
                humidityData.setName(AppMonitorUtil.getHumidityName(values.get("id").toString()));
                sensors.add(humidityData);
            }
        }

        // ???????????????
        JSONArray workHourArr = info.getWorkHourSensor();
        if (workHourArr != null && workHourArr.size() > 0) {
            for (int i = 0; i < workHourArr.size(); i++) {
                SensorData workHourData = new SensorData();
                JSONObject values = (JSONObject) workHourArr.get(i);
                workHourData.setType("work-hour");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                String workTime = "";
                if (unusual != null && unusual == 0) { //???????????????????????????
                    switch (values.get("workingPosition").toString()) {
                        case "0":
                            Double hour0 = DateUtil.getHour(Integer.parseInt(
                                values.get("continueTime") == null ? "0" : values.get("continueTime").toString()));
                            workTime = hour0 + " h";
                            workHourData.setStatus("??????");
                            break;
                        case "1":
                            Double hour1 = DateUtil.getHour(Integer.parseInt(
                                values.get("continueTime") == null ? "0" : values.get("continueTime").toString()));
                            workTime = hour1 + " h";
                            workHourData.setStatus("??????");
                            break;
                        case "2":
                            Double hour3 = DateUtil.getHour(Integer.parseInt(
                                values.get("continueTime") == null ? "0" : values.get("continueTime").toString()));
                            workTime = hour3 + " h";
                            workHourData.setStatus("??????");
                            break;
                        default:
                            break;
                    }
                    workHourData.setValue(workTime);
                }
                workHourData.setName(AppMonitorUtil.getWorkHourName(values.get("id").toString()));
                sensors.add(workHourData);
            }
        }

        // ??????????????????
        JSONArray positiveNegativeArr = info.getPositiveNegative();
        if (positiveNegativeArr != null && positiveNegativeArr.size() > 0) {
            for (int i = 0; i < positiveNegativeArr.size(); i++) {
                SensorData positiveNegativeData = new SensorData();
                JSONObject values = (JSONObject) positiveNegativeArr.get(i);
                positiveNegativeData.setType("motor");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                if (unusual != null && unusual == 0) { //???????????????????????????
                    StringBuilder stringBuilder = new StringBuilder();
                    String winchTime = values.getString("winchTime");
                    if (StringUtils.isBlank(winchTime)) {
                        winchTime = "0";
                    }
                    Integer resultNumber;
                    if (winchTime.contains(".")) { // ???????????????
                        Double formatNumber = Double.parseDouble(winchTime);
                        resultNumber = formatNumber.intValue();
                    } else {
                        resultNumber = Integer.parseInt(winchTime);
                    }
                    BigDecimal bigDecimal = new BigDecimal(resultNumber);
                    Double hour = bigDecimal.doubleValue();
                    stringBuilder.append(hour).append(" h");
                    positiveNegativeData.setValue(stringBuilder.toString());
                    if ("2".equals(values.get("spinState").toString())) {
                        if ("1".equals(values.get("spinDirection").toString())) {
                            positiveNegativeData.setStatus("??????");
                        } else {
                            positiveNegativeData.setStatus("??????");
                        }
                    } else {
                        positiveNegativeData.setStatus("??????");
                    }
                }
                sensors.add(positiveNegativeData);
            }
        }


        // ???????????????
        JSONArray loadInfos = info.getLoadInfos();
        if (loadInfos != null && loadInfos.size() > 0) {
            List<SensorData> sensorDataList = new ArrayList<>();
            for (int i = 0; i < loadInfos.size(); i++) {
                SensorData loadInfo = new SensorData();
                JSONObject values = (JSONObject) loadInfos.get(i);
                loadInfo.setType("loadInfo");
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                if (unusual != null && unusual == 0 && values.get("loadWeight") != null) { //???????????????????????????
                    Double loadValue = Double.valueOf(values.get("loadWeight").toString());
                    if (loadValue > 1000) {
                        loadValue = loadValue / 1000D;
                        String result = String.format("%.1f", loadValue);
                        loadInfo.setValue(result + " T");
                    } else {
                        loadInfo.setValue(loadValue + " kg");
                    }
                    Integer status = Integer.parseInt(values.get("status").toString());
                    loadInfo.setStatus(AppMonitorUtil.getLoadStatusName(status));
                }
                loadInfo.setName(AppMonitorUtil.getLoadName(values.get("id").toString()));
                if (loadInfo.getName().equals("1#")) {
                    sensorDataList.add(0, loadInfo);
                } else {
                    sensorDataList.add(loadInfo);
                }
            }
            sensors.addAll(sensorDataList);
        }

        // ???????????????
        JSONObject tyre = info.getTyreInfos();
        if (tyre != null) {
            Integer unusual = Integer.parseInt(tyre.get("unusual").toString());
            JSONArray tyreInfos = tyre.getJSONArray("list");
            if (tyreInfos != null && tyreInfos.size() > 0) {
                List<SensorData> sensorDataList = new ArrayList<>();
                for (int i = 0; i < tyreInfos.size(); i++) {
                    JSONObject values = tyreInfos.getJSONObject(i);
                    SensorData tyreInfo = new SensorData();
                    tyreInfo.setType("tyreInfo");
                    tyreInfo.setStatus(AppMonitorUtil.getSensorUnusualName(unusual, 5));
                    if (unusual != null && unusual == 0 && values.get("pressure") != null) { //???????????????????????????
                        Double tyreValue = Double.valueOf(values.get("pressure").toString());
                        tyreInfo.setValue(tyreValue + " bar");
                    }
                    String number = values.getString("number");
                    tyreInfo.setName((number != null ? (Integer.parseInt(number) + 1) + "" : "") + "#");
                    sensorDataList.add(tyreInfo);
                }
                Collections.sort(sensorDataList, new Comparator<SensorData>() {
                    @Override
                    public int compare(SensorData o1, SensorData o2) {
                        return
                            Integer.parseInt(o1.getName().split("#")[0]) - Integer.parseInt(o2.getName().split("#")[0])
                                > 0D ? 1 : -1;
                    }
                });
                sensors.addAll(sensorDataList);
            }
        }

        // ????????????????????????????????????IO???????????????????????????
        List<IoSensorConfigInfo> ioSensorConfigInfos = switchingSignalDao.getIoSensorConfigInfo(monitorId);
        Map<Integer, JSONObject> signalIos = new HashMap<>();//??????io????????????
        Map<Integer, JSONObject> cirIos1 = new HashMap<>();//??????io?????????1????????????
        Map<Integer, JSONObject> cirIos2 = new HashMap<>();//??????io?????????2????????????
        for (IoSensorConfigInfo ioData : ioSensorConfigInfos) { // ??????????????????????????????????????????key
            JSONObject data = new JSONObject();
            if (ioData.getHighSignalType() == 1) {
                data.put("high", ioData.getStateOne());
                data.put("low", ioData.getStateTwo());
            } else {
                data.put("high", ioData.getStateTwo());
                data.put("low", ioData.getStateOne());
            }
            data.put("name", ioData.getName());
            switch (ioData.getIoType()) {
                case 1:
                    signalIos.put(ioData.getIoSite(), data);
                    break;
                case 2:
                    cirIos1.put(ioData.getIoSite(), data);
                    break;
                case 3:
                    cirIos2.put(ioData.getIoSite(), data);
                    break;
                default:
                    break;
            }
        }

        // IO?????????
        JSONArray ioSignalDataArr = info.getIoSignalData();
        if (ioSignalDataArr != null && ioSignalDataArr.size() > 0 && signalIos.size() > 0) {
            for (int i = 0; i < ioSignalDataArr.size(); i++) {
                JSONObject values = (JSONObject) ioSignalDataArr.get(i);
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                if (unusual != null && unusual == 0) { //???????????????????????????
                    for (Map.Entry<Integer, JSONObject> entry : signalIos.entrySet()) {
                        String signal = values.get("signal" + entry.getKey()).toString();
                        SensorData ioSignalData = new SensorData();
                        ioSignalData.setType("ioSignal");
                        if ("0".equals(signal)) {
                            ioSignalData.setValue(entry.getValue().get("high").toString());
                            ioSignalData.setStatus("???");
                        } else if ("1".equals(signal)) {
                            ioSignalData.setValue(entry.getValue().get("low").toString());
                            ioSignalData.setStatus("???");
                        }
                        //                        ioSignalData.setName(entry.getValue().get("name").toString());
                        ioSignalData.setName("");
                        sensors.add(ioSignalData);
                    }
                }
            }
        }

        // ??????IO?????????
        JSONArray cirIoCheckArr = info.getCirIoCheckData();
        if (cirIoCheckArr != null && cirIoCheckArr.size() > 0) {
            for (int i = 0; i < cirIoCheckArr.size(); i++) {
                JSONObject values = (JSONObject) cirIoCheckArr.get(i);
                Integer unusual = Integer.parseInt(values.get("unusual").toString());
                if (unusual != null && unusual == 0) { //???????????????????????????
                    Map<Integer, JSONObject> cirIosData = new HashMap<>();
                    int nameNumber = 0; //?????????????????????
                    if ("145".equals(values.get("id").toString())) { //???????????????1
                        cirIosData = cirIos1;
                        nameNumber = 1;
                    } else if ("146".equals(values.get("id").toString())) { //???????????????2
                        cirIosData = cirIos2;
                        nameNumber = 2;
                    }
                    if (cirIosData.size() > 0) { //??????????????????????????????????????????
                        JSONArray statusList = JSONArray.parseArray(values.get("statusList").toString());
                        JSONObject ioStatusData = JSONObject.parseObject(statusList.get(0).toString());
                        Long ioStatus = Long.parseLong((ioStatusData.get("ioStatus").toString()));
                        for (int j = 0; j < 32; j++) { //????????????????????????32????????????????????????
                            if (cirIosData.containsKey(j)) {
                                SensorData data = new SensorData();
                                data.setType("ioSignal");
                                if ((int) (ioStatus & (long) Math.pow(2, j)) == 0) { //?????????
                                    Object hign = cirIosData.get(j).get("high");
                                    data.setValue(hign == null ? "" : hign.toString());
                                    data.setStatus("???");
                                } else { //?????????
                                    Object low = cirIosData.get(j).get("low");
                                    data.setValue(low == null ? "" : low.toString());
                                    data.setStatus("???");
                                }
                                if (nameNumber == 1) {
                                    data.setName("1#");
                                } else if (nameNumber == 2) {
                                    data.setName("2#");
                                }
                                sensors.add(data);
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void setDailyMileAndOil(String vehicleId, LocationInfo info) {
        // deviceDayMile???sensorDayMile???dayOilWear??????Redis???????????????????????????????????????????????????????????????
        final String redisLocationStr = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(vehicleId));
        Optional.ofNullable(redisLocationStr)
                .map(JSON::parseObject)
                .map(json -> json.getJSONObject("data"))
                .map(json -> json.getJSONObject("msgBody"))
                .ifPresent(redisLocation -> {
                    info.setDeviceDayMile(redisLocation.getString("deviceDayMile"));
                    info.setSensorDayMile(redisLocation.getString("sensorDayMile"));
                    info.setDayOilWear(redisLocation.getString("dayOilWear"));
                });
        final String defaultValue = "0.0";
        if (null == info.getDeviceDayMile()) {
            info.setDeviceDayMile(defaultValue);
        }
        if (null == info.getSensorDayMile()) {
            info.setSensorDayMile(defaultValue);
        }
        if (null == info.getDayOilWear()) {
            info.setDayOilWear(defaultValue);
        }
        info.setDayMileage(info.getDeviceDayMile());
    }

    /**
     * ????????????????????????????????????
     * @param id
     * @param starTime
     * @param endTime
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject getSwitchInfo(String id, String starTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(starTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            // ????????????????????????????????????
            // ??????????????????????????????????????????
            List<SwitchInfo> signal = ioVehicleConfigDao.getBindIoInfoByVehicleId(id);
            JSONObject resultMsg = new JSONObject();
            if (signal != null && signal.size() > 0) { // ??????????????????
                Map<String, Map<Integer, String>> ioSettingData = processMonitorSwitch(signal, resultMsg, id);
                Long nbStarTime = DateUtils.parseDate(starTime, DATE_FORMAT).getTime() / 1000;
                Long nbEndTime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
                //  ????????????
                List<Positional> queryResult = this.listSwitchSign(id, nbStarTime, nbEndTime);
                if (queryResult != null && queryResult.size() > 0) {
                    //  ????????????
                    List<SwitchSignalInfo> resultData = processPositionalInfo(queryResult, ioSettingData);
                    resultMsg.put("data", resultData);
                }
            }
            return resultMsg;
        }
        return null;
    }

    private List<Positional> listSwitchSign(String monitorId, Long nbStarTime, Long nbEndTime) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(nbStarTime));
        params.put("endTime", String.valueOf(nbEndTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_SWITCH_SIGN, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    /**
     * ????????????????????????????????????
     * @param signal
     * @return
     */
    private Map<String, Map<Integer, String>> processMonitorSwitch(List<SwitchInfo> signal, JSONObject msg,
        String monitorId) throws Exception {
        Map<String, Map<Integer, String>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(signal)) {
            return result;
        }
        Map<Integer, String> io90 = new HashMap<>();
        Map<Integer, String> io91 = new HashMap<>();
        Map<Integer, String> io92 = new HashMap<>();
        List<String> switchName = new ArrayList<>(); // ????????????
        List<String> alarmStatusList = new ArrayList<>(); // I/O ????????????
        Map<Integer, String> monitorIoAlarmSet = getMonitorIoAlarmSetting(monitorId);
        if (MapUtils.isEmpty(monitorIoAlarmSet)) {
            monitorIoAlarmSet = new HashMap<>();
        }
        JSONArray ioStatus = new JSONArray();
        for (SwitchInfo switchInfo : signal) {
            JSONObject status = new JSONObject();
            // ???????????????
            status.put("0",
                "1".equals(switchInfo.getHighSignalType()) ? switchInfo.getStateOne() : switchInfo.getStateTwo());
            // ???????????????
            status.put("1",
                "2".equals(switchInfo.getLowSignalType()) ? switchInfo.getStateTwo() : switchInfo.getStateOne());
            ioStatus.add(status);
            Integer ioSite = switchInfo.getIoSite();
            Integer ioType = switchInfo.getIoType();
            String highSignalType = switchInfo.getHighSignalType();
            if (ioType == 1) { // ??????IO
                io90.put(ioSite, highSignalType);
            } else if (switchInfo.getIoType() == 2) { //IO??????1
                io91.put(ioSite, highSignalType);
            } else if (switchInfo.getIoType() == 3) { //IO??????2
                io92.put(ioSite, highSignalType);
            }
            // ??????I/0?????????I/0??????????????????????????????
            Integer alarmPos = getAlarmStatus(ioType, ioSite);
            String alarmStatus = monitorIoAlarmSet.get(alarmPos) != null ? monitorIoAlarmSet.get(alarmPos) : "";
            alarmStatusList.add(alarmStatus);
            switchName.add(switchInfo.getName());
        }
        result.put("90", io90);
        result.put("91", io91);
        result.put("92", io92);
        msg.put("names", switchName.size() > 0 ? switchName : null);
        msg.put("IOStatus", ioStatus.size() > 0 ? ioStatus : null);
        msg.put("alarmStatuses", alarmStatusList);
        return result;
    }

    /**
     * ??????I/0?????????I/0??????????????????????????????
     * @param ioType
     * @param ioSite
     * @return
     */
    private Integer getAlarmStatus(Integer ioType, Integer ioSite) throws Exception {
        Integer alarmPoss = 0;
        if (ioType == null || ioSite == null) {
            return alarmPoss;
        }
        if (ioType == 1) { // ??????I/O
            alarmPoss = AlarmTypeUtil.IO_0X90_ALARM.get(ioSite);
        }
        if (ioType == 2) { // I/O??????1
            alarmPoss = AlarmTypeUtil.IO_0X91_ALARM.get(ioSite);
        }
        if (ioType == 3) { // I/0??????2
            alarmPoss = AlarmTypeUtil.IO_0X92_ALARM.get(ioSite);
        }
        return alarmPoss;
    }

    private Map<Integer, String> getMonitorIoAlarmSetting(String monitorId) throws Exception {
        if (org.apache.commons.lang3.StringUtils.isBlank(monitorId)) {
            return null;
        }
        List<Map<String, Object>> ioAlarmSetting = alarmSettingDao.findIoAlarmValueByVehicleId(monitorId);
        if (CollectionUtils.isEmpty(ioAlarmSetting)) {
            return null;
        }
        Map<Integer, String> ioAlarmStatus = new HashMap<>();
        ioAlarmSetting.forEach(data -> {
            if (MapUtils.isEmpty(data)) {
                return;
            }
            Integer poss = data.get("pos") != null ? Integer.parseInt(String.valueOf(data.get("pos"))) : null;
            String parameterValue =
                data.get("parameter_value") != null ? String.valueOf(data.get("parameter_value")) : null;
            if (poss != null && parameterValue != null) {
                ioAlarmStatus.put(poss, parameterValue);
            }
        });
        return ioAlarmStatus;
    }

    /**
     * ????????????????????????????????????
     */
    private List<SwitchSignalInfo> processPositionalInfo(List<Positional> queryResult,
        Map<String, Map<Integer, String>> ioSetting) throws Exception {
        List<SwitchSignalInfo> resultData = new ArrayList<>();
        SwitchSignalInfo switchSignalInfo;
        List<Integer> status;
        Map<Integer, String> io90 = ioSetting.get("90");
        Map<Integer, String> io91 = ioSetting.get("91");
        Map<Integer, String> io92 = ioSetting.get("92");
        for (Positional ps : queryResult) {
            switchSignalInfo = new SwitchSignalInfo();
            status = new ArrayList<>();
            if (io90.containsKey(0)) {
                status.add(io90Data(ps.getIoOne()));
            }
            if (io90.containsKey(1)) {
                status.add(io90Data(ps.getIoTwo()));
            }
            if (io90.containsKey(2)) {
                status.add(io90Data(ps.getIoThree()));
            }
            if (io90.containsKey(3)) {
                status.add(io90Data(ps.getIoFour()));
            }
            if (io91 != null && io91.size() > 0) {
                status.addAll(ioDataDispose(ps.getIoObjOne(), io91));
            }
            if (io92 != null && io92.size() > 0) {
                status.addAll(ioDataDispose(ps.getIoObjTwo(), io92));
            }
            switchSignalInfo.setStatuses(status);
            switchSignalInfo.setTime(ps.getVtime());
            resultData.add(switchSignalInfo);
        }
        return resultData;
    }

    /**
     * ??????90 I/0 ????????????????????????
     * @param status
     * @return
     */
    private Integer io90Data(Integer status) {
        Integer ioStatus = null;
        if (status != null) {
            if (status < 0) {
                ioStatus = 2;
            } else {
                ioStatus = status;
            }
        }
        return ioStatus;
    }

    /**
     * ?????????????????????????????????
     */
    private List<Integer> ioDataDispose(String positionalIoInfo, Map<Integer, String> dataSign) {
        List<Integer> ioStates = new ArrayList<>();
        if (StringUtils.isNotBlank(positionalIoInfo)) {
            // io???????????????
            JSONObject info = JSON.parseObject(positionalIoInfo);
            if (info.getInteger("unusual") == 1) { // Io??????
                // ???IO???????????? ?????????????????????
                for (int i = 0; i < dataSign.size(); i++) {
                    ioStates.add(2);
                }
            } else {
                JSONArray statusList = info.getJSONArray("statusList");
                if (statusList != null && statusList.size() != 0 && statusList.getJSONObject(0) != null
                    && statusList.getJSONObject(0).getInteger("ioStatus") != null) {
                    Integer ioStatus = statusList.getJSONObject(0).getInteger("ioStatus");
                    for (Map.Entry<Integer, String> entry : dataSign.entrySet()) {
                        Integer state = ConvertUtil.binaryIntegerWithOne(ioStatus, entry.getKey());
                        ioStates.add(state);
                    }
                } else {
                    // ?????????????????????io????????????,???????????????
                    for (int i = 0; i < dataSign.size(); i++) {
                        ioStates.add(2);
                    }
                }
            }
        } else {
            for (int i = 0; i < dataSign.size(); i++) {
                ioStates.add(null);
            }
        }
        return ioStates;
    }

    /**
     * ???????????????????????????????????????
     * @param id
     * @param starTime
     * @param endTime
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject getMonitorWinchInfo(String id, String starTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(starTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            JSONObject msg = new JSONObject();
            // ?????????id???????????????????????????
            List<TransdusermonitorSet> veerVehicleList = sensorSettingsDao.findByVehicleId(3, id);
            if (veerVehicleList != null && veerVehicleList.size() > 0) {
                Long nbStarTime = DateUtils.parseDate(starTime, DATE_FORMAT).getTime() / 1000;
                Long nbEndTime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
                // ????????????
                List<WinchInfo> queryResult = this.listWinchInfo(id, nbStarTime, nbEndTime);
                if (queryResult != null && queryResult.size() > 0) {
                    msg.put("motor", queryResult);
                }
            }
            return msg;
        }
        return null;
    }

    private List<WinchInfo> listWinchInfo(String monitorId, Long nbStarTime, Long nbEndTime) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(nbStarTime));
        params.put("endTime", String.valueOf(nbEndTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_WINCH_INFO, params);
        return PaasCloudUrlUtil.getResultListData(str, WinchInfo.class);
    }

    /**
     * ??????????????????????????????
     * @param vehicleId
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject getMonitorAttached(String vehicleId) throws Exception {
        if (StringUtils.isNotBlank(vehicleId)) {
            JSONObject msg = new JSONObject();
            SensorConfig sc = this.sensorConfigDao.findByVehicleId(vehicleId);
            if (sc != null) {
                List<SensorPolling> spList = sensorPollingDao.findByVehicleId(sc.getVehicleId());
                List<String> bindAttached = new ArrayList<>();
                for (SensorPolling sensor : spList) {
                    if (StringUtils.isNotBlank(sensor.getConfigId()) && StringUtils
                        .isNotBlank(sensor.getSensorType())) {
                        bindAttached.add(sensor.getIdentId()); // ??????id
                    }
                }
                msg.put("attachedList", bindAttached);
            }
            return msg;
        }
        return null;
    }

    /**
     * ??????????????????????????????
     * @param id       ????????????id
     * @param starTime ????????????
     * @param endTime  ????????????
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject getHumidityInfo(String id, String starTime, String endTime) throws Exception {
        if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(starTime) && StringUtils.isNotBlank(endTime)) {
            // ??????????????????id????????????????????????????????????????????????
            JSONObject result = new JSONObject();
            List<TransdusermonitorSet> humidityVehicleList = sensorSettingsDao.findByVehicleId(2, id);
            List<String> sensorName = new ArrayList<>();
            List<HumidityTemperatureThreshold> threshold = new ArrayList<>();
            List<String> sensorOutId = new ArrayList<>();
            if (humidityVehicleList.size() > 0) {
                HumidityTemperatureThreshold humidityThreshold;
                for (TransdusermonitorSet data : humidityVehicleList) {
                    humidityThreshold = new HumidityTemperatureThreshold();
                    if (StringUtils.isNotBlank(data.getSensorId()) && StringUtils.isNotBlank(data.getSensorNumber())) {
                        sensorName.add(data.getSensorOutId());
                        humidityThreshold.setHigh(data.getAlarmUp().toString());
                        humidityThreshold.setLow(data.getAlarmDown().toString());
                        threshold.add(humidityThreshold);
                        sensorOutId.add(data.getSensorOutId());
                    }
                }
                result.put("nickname", sensorName);
                result.put("threshold", threshold);
                long stime = DateUtils.parseDate(starTime, DATE_FORMAT).getTime() / 1000;
                long etime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
                List<Positional> queryResult = this.listHumidityData(id, stime, etime);
                List<HumidityTemperatureData> poovessResult = new ArrayList<>();
                if (queryResult.size() > 0) {
                    poovessResult = processHumidity(queryResult, sensorOutId);
                }
                result.put("humidity", poovessResult.size() > 0 ? poovessResult : null);
            }
            return result;
        }
        return null;
    }

    private List<Positional> listHumidityData(String monitorId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(start));
        params.put("endTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_HUMIDITY_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    /**
     * ????????????????????????
     * @param queryResult
     * @param sensorOutId
     * @return
     */
    private List<HumidityTemperatureData> processHumidity(List<Positional> queryResult, List<String> sensorOutId) {
        List<HumidityTemperatureData> humidityInfo = new ArrayList<>();
        HumidityTemperatureData humidityData;
        List<Double> sensorData;
        for (Positional positional : queryResult) {
            humidityData = new HumidityTemperatureData();
            sensorData = new ArrayList<>();
            humidityData.setTime(positional.getVtime());
            for (String outId : sensorOutId) {
                switch (outId) {
                    case "26":
                        sensorData.add(positional.getWetnessValueOne() == null ? null :
                            positional.getWetnessValueOne().doubleValue());
                        break;
                    case "27":
                        sensorData.add(positional.getWetnessValueTwo() == null ? null :
                            positional.getWetnessValueTwo().doubleValue());
                        break;
                    case "28":
                        sensorData.add(positional.getWetnessValueThree() == null ? null :
                            positional.getWetnessValueThree().doubleValue());
                        break;
                    case "29":
                        sensorData.add(positional.getWetnessValueFour() == null ? null :
                            positional.getWetnessValueFour().doubleValue());
                        break;
                    default:
                        break;
                }
            }
            if (sensorData.size() > 0) {
                humidityData.setSensors(sensorData);
            } else {
                humidityData.setSensors(null);
            }
            humidityInfo.add(humidityData);
        }
        return humidityInfo;
    }

    /**
     * ????????????????????????
     * @param queryResult
     * @param sensorOutId
     * @return
     */
    private List<HumidityTemperatureData> processTemperature(List<Positional> queryResult, List<String> sensorOutId) {
        List<HumidityTemperatureData> humidityInfo = new ArrayList<>();
        HumidityTemperatureData humidityData;
        List<Double> sensorData;
        for (Positional positional : queryResult) {
            humidityData = new HumidityTemperatureData();
            sensorData = new ArrayList<>();
            humidityData.setTime(positional.getVtime());
            for (String outId : sensorOutId) {
                switch (outId) {
                    case "21":
                        sensorData
                            .add(positional.getTempValueOne() == null ? null : positional.getTempValueOne() / 10.0);
                        break;
                    case "22":
                        sensorData
                            .add(positional.getTempValueTwo() == null ? null : positional.getTempValueTwo() / 10.0);
                        break;
                    case "23":
                        sensorData
                            .add(positional.getTempValueThree() == null ? null : positional.getTempValueThree() / 10.0);
                        break;
                    case "24":
                        sensorData
                            .add(positional.getTempValueFour() == null ? null : positional.getTempValueFour() / 10.0);
                        break;
                    case "25":
                        sensorData
                            .add(positional.getTempValueFive() == null ? null : positional.getTempValueFive() / 10.0);
                        break;
                    default:
                        break;
                }
            }
            if (sensorData.size() > 0) {
                humidityData.setSensors(sensorData);
            } else {
                humidityData.setSensors(null);
            }
            humidityInfo.add(humidityData);
        }
        return humidityInfo;
    }

    /**
     * ????????????????????????
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject getWorkHoursHistoryData(String id, String startTime, String endTime) throws Exception {
        if (AppParamCheckUtil.check64String(id) && AppParamCheckUtil.checkDate(startTime, 1) && AppParamCheckUtil
            .checkDate(endTime, 1)) {
            JSONObject msg = new JSONObject();
            // ??????????????????????????????
            List<WorkHourSettingInfo> settingInfo = workHourSettingDao.getWorkHourSettingByVehicleId(id);
            if (settingInfo != null && settingInfo.size() > 0) {
                List<Long> workDuration = new ArrayList<>();
                List<Long> standByDuration = new ArrayList<>();
                List<Long> haltDuration = new ArrayList<>();
                List<Long> invalidDuration = new ArrayList<>();
                List<WorkHourResult> resultList = new ArrayList<>();
                //???????????????1????????????
                Integer inspectionMethodOne = null;
                //???????????????2????????????
                Integer inspectionMethodTwo = null;
                // ???????????????1??????
                String valueOne = null;
                // ???????????????2??????
                String valueTwo = null;
                //????????????
                Double speedThresholdOne = null;
                //????????????
                Double speedThresholdTwo = null;
                Integer detectionModeOne = null; // ?????????1???????????????????????????
                Integer detectionModeTwo = null; // ?????????2???????????????????????????
                String thresholdVoltageOne; // ?????????1?????????????????????
                String thresholdVoltageTwo; // ?????????2?????????????????????
                String thresholdWorkFlowOne; // ?????????1??????????????????
                String thresholdWorkFlowTwo; // ?????????2??????????????????
                Double fluctuatingThresholdOne = null; // ?????????1????????????
                Double fluctuatingThresholdTwo = null; // ?????????2????????????
                Integer fluctuatingNumberOne = null; // ?????????1??????????????????
                Integer fluctuatingNumberTwo = null; // ?????????2??????????????????
                boolean isBindSensorOne = false;
                boolean isBindSensorTwo = false;
                for (WorkHourSettingInfo setting : settingInfo) {
                    if (setting != null) {
                        if (setting.getSensorSequence() == 0) {
                            //clbs????????????????????????????????????(1:???????????????;2:???????????????;3.???????????????)
                            detectionModeOne = setting.getDetectionMode();
                            inspectionMethodOne = detectionModeOne - 1;
                            //???????????????V???
                            thresholdVoltageOne = setting.getThresholdVoltage();
                            //?????????????????????L/h???
                            thresholdWorkFlowOne = setting.getThreshold();
                            //????????????A
                            fluctuatingThresholdOne = getDoubleVal(setting.getBaudRateThreshold());
                            speedThresholdOne = getDoubleVal(setting.getSpeedThreshold());
                            valueOne = inspectionMethodOne == 0 ? thresholdVoltageOne :
                                (inspectionMethodOne == 1 ? thresholdWorkFlowOne :
                                    String.valueOf(fluctuatingThresholdOne));
                            //??????????????????N
                            fluctuatingNumberOne = setting.getBaudRateCalculateNumber();
                            isBindSensorOne = true;
                        } else {
                            //clbs????????????????????????????????????(1:???????????????;2:???????????????;3.???????????????)
                            detectionModeTwo = setting.getDetectionMode();
                            inspectionMethodTwo = detectionModeTwo - 1;
                            //???????????????V???
                            thresholdVoltageTwo = setting.getThresholdVoltage();
                            //?????????????????????L/h???
                            thresholdWorkFlowTwo = setting.getThreshold();
                            // ????????????
                            fluctuatingThresholdTwo = getDoubleVal(setting.getBaudRateThreshold());
                            speedThresholdTwo = getDoubleVal(setting.getSpeedThreshold());
                            valueTwo = inspectionMethodTwo != null ? (inspectionMethodTwo == 0 ? thresholdVoltageTwo :
                                (inspectionMethodTwo == 1 ? thresholdWorkFlowTwo :
                                    String.valueOf(fluctuatingThresholdTwo))) : null;
                            fluctuatingNumberTwo = setting.getBaudRateCalculateNumber();
                            isBindSensorTwo = true;
                        }
                    }
                }
                List<Integer> workInspectionMethod = new ArrayList<>(); // ?????????1????????????2??????????????????
                List<String> thresholdValue = new ArrayList<>(); // ?????????1????????????2??????
                workInspectionMethod.add(inspectionMethodOne); //
                workInspectionMethod.add(inspectionMethodTwo);
                thresholdValue.add(valueOne);
                thresholdValue.add(valueTwo);
                // ??????????????????
                List<WorkHourData> workData = getWorkHourPositional(id, startTime, endTime);
                if (workData != null && workData.size() > 0) {
                    // ??????????????????????????????????????????,???????????????????????????
                    initWorkStatus(isBindSensorOne, isBindSensorTwo, workData, inspectionMethodOne,
                        inspectionMethodTwo);
                    //??????????????????????????????
                    Integer needSetStandbyNumOne = 0;
                    Integer needSetStandbyNumTwo = 0;
                    for (int i = workData.size() - 1; i >= 0; i--) {
                        WorkHourData ps = workData.get(i);
                        if (isBindSensorOne) { // ???????????????1
                            Integer workInspectionMethodOne = ps.getWorkInspectionMethodOne();
                            //?????????????????????null??????????????????????????????,???????????????????????????
                            if (workInspectionMethodOne == null || detectionModeOne == null
                                || workInspectionMethodOne.intValue() != inspectionMethodOne) {
                                ps.setEffectiveDataOne(1);
                            } else {
                                Double checkDataOne = ps.getCheckDataOne();
                                //?????????????????????????????????????????????;
                                // ?????????1
                                Double fluctuatingOne = ps.getFluctuateValueOne();
                                //?????????????????????????????????????????????;
                                if (inspectionMethodOne == 2) {
                                    if (checkDataOne != 0) {
                                        //?????????????????????????????????????????????0 ?????? ???S<A????????????????????????????????????N???????????????????????????
                                        if (fluctuatingThresholdOne != null && (fluctuatingOne == null
                                            || fluctuatingOne < fluctuatingThresholdOne)) {
                                            needSetStandbyNumOne = fluctuatingNumberOne;
                                        }
                                    }
                                    //????????????????????????????????????????????????0???,?????????????????????;
                                    if (needSetStandbyNumOne > 0) {
                                        ps.setWorkingPositionOne(STANDBY_STATE);
                                        needSetStandbyNumOne--;
                                    }
                                    if (i != 0) {
                                        WorkHourData infoNext = workData.get(i - 1);
                                        //???????????????vtime?????? 300s ???,????????????????????????,?????????????????????????????????
                                        if (ps.getVtime() - infoNext.getVtime() > TIME_INTERVAL) {
                                            needSetStandbyNumOne = 0;
                                        }
                                    }

                                    //???????????????????????????????????????????????????
                                    if (ps.getSpeed() != null && speedThresholdOne != null
                                        && Double.valueOf(ps.getSpeed()) > speedThresholdOne) {
                                        ps.setWorkingPositionOne(WORK_STATE);
                                    }
                                    //??????????????????????????????0???, ???????????????????????????????????????????????????
                                    if (checkDataOne == 0) {
                                        ps.setWorkingPositionOne(HALT_STATE);
                                        needSetStandbyNumOne = 0;
                                    }
                                    //???????????????????????????0????????????????????????5??? ????????????????????????
                                    if (fluctuatingOne != null && fluctuatingOne == 0
                                        && checkDataOne > INSTANT_FLOW_LIMIT) {
                                        Integer beforeIndex = i - 1 >= 0 ? i - 1 : null;
                                        if (beforeIndex == null) {
                                            ps.setWorkingPositionOne(WORK_STATE);
                                            needSetStandbyNumOne = 0;
                                        } else {
                                            WorkHourData beforeWorKInfo = workData.get(beforeIndex);
                                            Double beforeCheckData = beforeWorKInfo.getCheckDataOne();
                                            //????????????????????????????????????????????????????????????????????????????????????????????????????????????,???????????????????????????????????????;
                                            if (beforeWorKInfo.getWorkInspectionMethodOne() == null || !Objects
                                                .equals(checkDataOne, beforeCheckData)) {
                                                ps.setWorkingPositionOne(WORK_STATE);
                                                needSetStandbyNumOne = 0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (isBindSensorTwo) { // ?????????????????????2
                            Integer workInspectionMethodTwo = ps.getWorkInspectionMethodTwo();
                            //?????????????????????null??????????????????????????????,???????????????????????????
                            if (workInspectionMethodTwo == null || detectionModeTwo == null
                                || workInspectionMethodTwo.intValue() != inspectionMethodTwo) {
                                ps.setEffectiveDataTwo(1);
                            }
                            Double checkDataTwo = ps.getCheckDataTwo(); // ?????????????????????
                            Double fluctuatingTwo = ps.getFluctuateValueTwo(); // ?????????
                            //?????????????????????????????????????????????;
                            if (inspectionMethodTwo != null && inspectionMethodTwo == 2) {
                                if (workInspectionMethodTwo != null && checkDataTwo != 0) {
                                    //?????????????????????????????????????????????0 ?????? ???S<A????????????????????????????????????N???????????????????????????
                                    if (fluctuatingThresholdTwo != null && (fluctuatingTwo == null
                                        || fluctuatingTwo < fluctuatingThresholdTwo)) {
                                        needSetStandbyNumTwo = fluctuatingNumberTwo;
                                    }
                                }
                                //????????????????????????????????????????????????0???,?????????????????????;
                                if (needSetStandbyNumTwo > 0) {
                                    ps.setWorkingPositionTwo(STANDBY_STATE);
                                    needSetStandbyNumTwo--;
                                }
                                if (i != 0) {
                                    WorkHourData infoNext = workData.get(i - 1);
                                    //???????????????vtime?????? 300s ???,????????????????????????,?????????????????????????????????
                                    if (ps.getVtime() - infoNext.getVtime() > TIME_INTERVAL) {
                                        needSetStandbyNumTwo = 0;
                                    }
                                }
                                if (workInspectionMethodTwo != null) {
                                    //???????????????????????????????????????????????????
                                    if (ps.getSpeed() != null && speedThresholdTwo != null
                                        && Double.valueOf(ps.getSpeed()) > speedThresholdTwo) {
                                        ps.setWorkingPositionTwo(WORK_STATE);
                                    }
                                    //??????????????????????????????0???, ???????????????????????????????????????????????????
                                    if (checkDataTwo == 0) {
                                        ps.setWorkingPositionTwo(HALT_STATE);
                                        needSetStandbyNumTwo = 0;
                                    }
                                    //???????????????????????????0????????????????????????5??? ????????????????????????
                                    if (fluctuatingTwo == 0 && checkDataTwo > INSTANT_FLOW_LIMIT) {
                                        Integer beforeIndex = i - 1 >= 0 ? i - 1 : null;
                                        if (beforeIndex == null) {
                                            ps.setWorkingPositionTwo(WORK_STATE);
                                            needSetStandbyNumTwo = 0;
                                        } else {
                                            WorkHourData beforeWorKInfo = workData.get(beforeIndex);
                                            Double beforeCheckData = beforeWorKInfo.getCheckDataTwo();
                                            //????????????????????????????????????????????????????????????????????????????????????????????????????????????,???????????????????????????????????????;
                                            if (beforeWorKInfo.getWorkInspectionMethodTwo() == null || !Objects
                                                .equals(checkDataTwo, beforeCheckData)) {
                                                ps.setWorkingPositionTwo(WORK_STATE);
                                                needSetStandbyNumTwo = 0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //                    if (invalidListOne.size() > 0) {
                    //                        Collections.reverse(invalidListOne);
                    //                    }
                    //                    if (invalidListTwo.size() > 0) {
                    //                        Collections.reverse(invalidListTwo);
                    //                    }
                    //                    //???????????????vtime?????? 300s ???,????????????????????????
                    //                    List<WorkHourData> afterData = new ArrayList<>();
                    //                    long blankTime = addBlankData(afterData,workData); //  ??????????????????
                    //??????????????????
                    List<Integer> invalidListOne = new ArrayList<>();
                    //??????????????????
                    List<Integer> invalidListTwo = new ArrayList<>();
                    //??????????????????
                    List<Integer> workIndexOne = new ArrayList<>();
                    //??????????????????
                    List<Integer> haltIndexOne = new ArrayList<>();
                    //??????????????????
                    List<Integer> standByIndexOne = new ArrayList<>();
                    //??????????????????
                    List<Integer> workIndexTwo = new ArrayList<>();
                    //??????????????????
                    List<Integer> haltIndexTwo = new ArrayList<>();
                    //??????????????????
                    List<Integer> standByIndexTwo = new ArrayList<>();
                    // ????????????
                    List<Double> checkData; // ??????????????????
                    List<Integer> workingPosition;
                    List<Integer> effectiveData;
                    WorkHourResult workHourResult;
                    for (int i = 0; i < workData.size(); i++) {
                        checkData = new ArrayList<>();
                        workingPosition = new ArrayList<>();
                        effectiveData = new ArrayList<>();
                        WorkHourData info = workData.get(i);
                        if (isBindSensorOne) {
                            Integer workingOne = info.getWorkingPositionOne();
                            if (info.getEffectiveDataOne() == 0 && workingOne != null) { // ????????????
                                if (workingOne == HALT_STATE) {
                                    haltIndexOne.add(i);
                                } else if (workingOne == WORK_STATE) {
                                    workIndexOne.add(i);
                                } else if (workingOne == STANDBY_STATE) {
                                    standByIndexOne.add(i);
                                }
                            } else if (info.getEffectiveDataOne() == 1) { // ????????????
                                invalidListOne.add(i);
                            }
                        }
                        if (isBindSensorTwo) {
                            Integer workingTwo = info.getWorkingPositionTwo();
                            if (info.getEffectiveDataTwo() == 0) { // ????????????
                                if (workingTwo == HALT_STATE) {
                                    haltIndexTwo.add(i);
                                } else if (workingTwo == WORK_STATE) {
                                    workIndexTwo.add(i);
                                } else if (workingTwo == STANDBY_STATE) {
                                    standByIndexTwo.add(i);
                                }
                            } else if (info.getEffectiveDataTwo() == 1) { // ????????????
                                invalidListTwo.add(i);
                            }
                        }
                        workHourResult = new WorkHourResult();
                        WorkHourData whd = workData.get(i);
                        long vtime = whd.getVtime();
                        workingPosition.add(whd.getWorkingPositionOne());
                        workingPosition.add(whd.getWorkingPositionTwo());
                        checkData.add(whd.getCheckDataOne());
                        checkData.add(whd.getCheckDataTwo());
                        effectiveData.add(whd.getEffectiveDataOne());
                        effectiveData.add(whd.getEffectiveDataTwo());
                        workHourResult.setTime(vtime);
                        workHourResult.setCheckData(checkData);
                        workHourResult.setWorkingPosition(workingPosition);
                        workHourResult.setEffectiveData(effectiveData);
                        resultList.add(workHourResult);
                    }
                    if (isBindSensorOne && !isBindSensorTwo) { // ????????????
                        //??????????????????????????????
                        //????????????????????????
                        long invalidDurationOne = removeInvalidData(workData, invalidListOne);
                        invalidDuration.add(invalidDurationOne);
                        invalidDuration.add(null);
                        //????????????
                        long workDurationOne = getDuration(workData, workIndexOne);
                        workDuration.add(workDurationOne);
                        workDuration.add(null);
                        //????????????
                        long haltDurationOne = getDuration(workData, haltIndexOne);
                        haltDuration.add(haltDurationOne);
                        haltDuration.add(null);
                        //????????????
                        long standByDurationOne = getDuration(workData, standByIndexOne);
                        standByDuration.add(standByDurationOne);
                        standByDuration.add(null);
                    } else if (!isBindSensorOne && isBindSensorTwo) {
                        //??????????????????????????????
                        //????????????????????????
                        long invalidDurationTwo = removeInvalidData(workData, invalidListTwo);
                        invalidDuration.add(null);
                        invalidDuration.add(invalidDurationTwo);
                        //????????????
                        long workDurationTwo = getDuration(workData, workIndexTwo);
                        workDuration.add(null);
                        workDuration.add(workDurationTwo);
                        //????????????
                        long haltDurationTwo = getDuration(workData, haltIndexTwo);
                        haltDuration.add(null);
                        haltDuration.add(haltDurationTwo);
                        //????????????
                        long standByDurationTwo = getDuration(workData, standByIndexTwo);
                        standByDuration.add(null);
                        standByDuration.add(standByDurationTwo);
                    } else {
                        long invalidDurationOne = removeInvalidData(workData, invalidListOne);
                        long invalidDurationTwo = removeInvalidData(workData, invalidListTwo);
                        invalidDuration.add(invalidDurationOne);
                        invalidDuration.add(invalidDurationTwo);
                        //????????????
                        long workDurationOne = getDuration(workData, workIndexOne);
                        long workDurationTwo = getDuration(workData, workIndexTwo);
                        workDuration.add(workDurationOne);
                        workDuration.add(workDurationTwo);
                        //????????????
                        long haltDurationOne = getDuration(workData, haltIndexOne);
                        long haltDurationTwo = getDuration(workData, haltIndexTwo);
                        haltDuration.add(haltDurationOne);
                        haltDuration.add(haltDurationTwo);
                        //????????????
                        long standByDurationOne = getDuration(workData, standByIndexOne);
                        long standByDurationTwo = getDuration(workData, standByIndexTwo);
                        standByDuration.add(standByDurationOne);
                        standByDuration.add(standByDurationTwo);
                    }
                    msg.put("workDuration", workDuration); // ????????????
                    msg.put("standByDuration", standByDuration); // ????????????
                    msg.put("haltDuration", haltDuration); // ????????????
                    msg.put("invalidDuration", invalidDuration); // ????????????
                    msg.put("workHourInfo", resultList); // ????????????
                }
                msg.put("workInspectionMethod", workInspectionMethod); // ?????????1????????????2??????????????????
                msg.put("thresholdValue", thresholdValue); // ??????
            }
            return msg;
        }
        return null;
    }

    private Double getDoubleVal(String doubleVal) {
        return doubleVal != null ? Double.parseDouble(doubleVal) : null;
    }

    /**
     * ???????????????vtime?????? 300s ???,????????????????????????;
     * @return
     */
    private long addBlankData(List<WorkHourData> afterData, List<WorkHourData> result) {
        long invalidDuration = 0L;
        for (int i = 0; i < result.size(); i++) {
            //?????? info
            WorkHourData info = result.get(i);
            afterData.add(info);
            //???????????????
            WorkHourData afterInfo = result.get(i + 1 < result.size() ? i + 1 : result.size() - 1);
            //??????????????????300s
            long timeDifference = afterInfo.getVtime() - info.getVtime();
            if (timeDifference > TIME_INTERVAL) {
                invalidDuration += timeDifference;
                long num = timeDifference % 30 == 0 ? timeDifference / 30 : ((timeDifference / 30) + 1);
                for (int j = 0; j < num; j++) {
                    WorkHourData nullInfo = new WorkHourData();
                    nullInfo.setEffectiveDataOne(3);
                    nullInfo.setEffectiveDataTwo(3);
                    afterData.add(nullInfo);
                }
            }
        }
        return invalidDuration;
    }

    /**
     * ????????????
     * @param workHourInfo
     * @param indexList
     */
    private long getDuration(List<WorkHourData> workHourInfo, List<Integer> indexList) {
        Integer beginIndex = null;
        Integer endIndex = null;
        long duration = 0L;
        for (int i = 0; i < indexList.size(); i++) {
            //??????
            Integer index = indexList.get(i);
            if (beginIndex == null) {
                beginIndex = index;
            }
            //?????????
            Integer nextIndex = indexList.get(i + 1 > indexList.size() - 1 ? indexList.size() - 1 : i + 1);
            if (nextIndex - 1 != index) {
                endIndex = index;
            }
            if (endIndex != null) {
                if (endIndex + 1 < workHourInfo.size()) {
                    WorkHourData info = workHourInfo.get(endIndex + 1);
                    if (info.getVtime() != 0 && info.getVtime() - workHourInfo.get(endIndex).getVtime() < 300) {
                        endIndex += 1;
                    }
                }
                duration += (workHourInfo.get(endIndex).getVtime() - workHourInfo.get(beginIndex).getVtime());
                beginIndex = null;
                endIndex = null;
            }
        }
        return duration;
    }

    /**
     * ??????????????????
     * @return
     */
    private long removeInvalidData(List<WorkHourData> data, List<Integer> indexList) {
        long duration = 0L;
        Integer beginIndex = null;
        Integer endIndex = null;
        for (int i = 0; i < indexList.size(); i++) {
            //??????
            Integer index = indexList.get(i);
            if (beginIndex == null) {
                beginIndex = index;
            }
            //?????????
            Integer nextIndex = indexList.get(i + 1 > indexList.size() - 1 ? indexList.size() - 1 : i + 1);
            if (nextIndex - 1 != index) {
                endIndex = index;
            }
            if (endIndex != null) {
                long timeDifference = data.get(endIndex).getVtime() - data.get(beginIndex).getVtime();
                duration += timeDifference;
                beginIndex = null;
                endIndex = null;
            }
        }
        return duration;
    }

    /**
     * ?????????????????????
     */
    private void initWorkStatus(boolean isBindSensorOne, boolean isBindSensorTwo, List<WorkHourData> workData,
        Integer inspectionMethodOne, Integer inspectionMethodTwo) throws Exception {
        // ???????????????????????????,?????????????????????????????????null
        if (!isBindSensorOne) {
            workData.forEach(info -> info.setEffectiveDataOne(null));
        }
        if (!isBindSensorTwo) {
            workData.forEach(info -> info.setEffectiveDataTwo(null));
        }
        // ??????????????????????????????????????????,?????????????????????????????????
        if (inspectionMethodOne != null && inspectionMethodOne == 2 && inspectionMethodTwo != null
            && inspectionMethodTwo == 2) {
            workData.forEach(info -> {
                info.setWorkingPositionOne(WORK_STATE);
                info.setWorkingPositionTwo(WORK_STATE);
            });
        } else if (inspectionMethodOne != null && inspectionMethodOne == 2) {
            workData.forEach(info -> info.setWorkingPositionOne(WORK_STATE));
        } else if (inspectionMethodTwo != null && inspectionMethodTwo == 2) {
            workData.forEach(info -> info.setWorkingPositionTwo(WORK_STATE));
        }
    }

    /**
     * ????????????????????????
     */
    private List<WorkHourData> getWorkHourPositional(String id, String startTime, String endTime) throws Exception {
        long nbStarTime = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
        long nbEndTime = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        // ????????????
        return this.listWorkHourData(id, nbStarTime, nbEndTime);
    }

    private List<WorkHourData> listWorkHourData(String monitorId, long nbStarTime, long nbEndTime) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(nbStarTime));
        params.put("endTime", String.valueOf(nbEndTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_WORK_HOUR_DATA, params);
        return PaasCloudUrlUtil.getResultListData(str, WorkHourData.class);
    }

}
