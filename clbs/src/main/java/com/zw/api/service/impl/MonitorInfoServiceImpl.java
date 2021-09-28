package com.zw.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.api.domain.DriverInfo;
import com.zw.api.domain.LocationInfo;
import com.zw.api.domain.MonitorInfo;
import com.zw.api.domain.Rfid;
import com.zw.api.domain.VehicleInfo;
import com.zw.api.repository.mysql.MonitorInfoDao;
import com.zw.api.service.MonitorInfoService;
import com.zw.api.service.SwaggerGroupService;
import com.zw.api.service.SwaggerLdapService;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.InputTypeEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.ConfigDTO;
import com.zw.platform.basic.service.ConfigService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class MonitorInfoServiceImpl implements MonitorInfoService {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final Pattern SPLITTER = Pattern.compile(",");
    private static final Pattern DRIVER_SPLITTER = Pattern.compile("_");

    @Autowired
    private MonitorInfoDao monitorInfoDao;

    @Autowired
    private SwaggerLdapService swaggerLdapService;

    @Autowired
    private SwaggerGroupService swaggerGroupService;

    @Autowired
    private ConfigService configService;

    @Override
    public MonitorInfo findMonitorByName(String name) {
        Set<String> monitorIds = getMonitorIdByName(Collections.singletonList(name));
        if (monitorIds.isEmpty()) {
            return null;
        }
        String id = monitorIds.iterator().next();
        Map<String, String> monitorInfo = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(id));
        if (monitorInfo == null) {
            return null;
        }
        MonitorInfo info = MonitorInfo.fromCache(monitorInfo);
        info.setId(id);
        info.setName(name);
        return info;
    }

    @Override
    public boolean addVehicle(VehicleInfo vehicleInfo) throws BusinessException {
        OrganizationLdap org = swaggerLdapService.getUuidByOrgCode(vehicleInfo.getOrgCode());

        String userId = swaggerLdapService.getCurrentUserUuid();
        Objects.requireNonNull(userId);
        List<String> groupIds = swaggerGroupService.getGroupIdsByUserId(userId);

        ConfigDTO configDTO = new ConfigDTO();
        configDTO.setInputType(InputTypeEnum.FAST_INPUT);
        configDTO.setMonitorType("0"); //监控对象类型：车辆
        configDTO.setName(vehicleInfo.getName());
        configDTO.setOrgId(org.getUuid());
        configDTO.setOrgName(org.getName());
        configDTO.setGroupId(groupIds.get(0));
        configDTO.setSimCardNumber(vehicleInfo.getSimNo());
        configDTO.setSimCardOrgId(org.getUuid());
        configDTO.setDeviceNumber(vehicleInfo.getDeviceNo());
        configDTO.setDeviceOrgId(org.getUuid());
        configDTO.setDeviceType(vehicleInfo.getProtocol());
        configService.add(configDTO);
        return true;
    }

    @Override
    public List<LocationInfo> fetchLatestLocation(String names) {
        final String[] monitorNames = SPLITTER.split(names);
        if (monitorNames.length > 20) {
            throw new RuntimeException("监控对象数量超过限制");
        }
        List<String> locationCaches = getLocationCaches(Arrays.asList(monitorNames));
        List<LocationInfo> list = new ArrayList<>(locationCaches.size());
        JSONObject locationObj;
        for (String locationCache : locationCaches) {
            locationObj = JSON.parseObject(locationCache);
            if (locationObj == null) {
                continue;
            }
            list.add(LocationInfo.fromCache(locationObj));
        }
        return list;
    }

    private List<String> getLocationCaches(List<String> names) {
        Set<String> monitorIds = getMonitorIdByName(names);
        List<RedisKey> keys = new ArrayList<>();
        monitorIds.forEach(o -> keys.add(HistoryRedisKeyEnum.MONITOR_LOCATION.of(o)));
        return RedisHelper.batchGetString(keys);
    }

    @Override
    public List<LocationInfo> queryHistoryLocations(String name, String startTime, String endTime) {
        Set<String> monitorIds = getMonitorIdByName(Collections.singletonList(name));
        if (monitorIds.isEmpty()) {
            return Collections.emptyList();
        }
        String id = monitorIds.iterator().next();
        long start;
        long end;
        try {
            start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime() / 1000;
            end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime() / 1000;
        } catch (ParseException e) {
            throw new RuntimeException("日期格式错误");
        }
        final List<Positional> history = getMonitorTrack(id, start, end);
        return convertLocation(history);
    }

    private List<Positional> getMonitorTrack(String vehicleId, long start, long end) {
        Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", vehicleId);
        params.put("startTime", String.valueOf(start));
        params.put("queryStartTime", String.valueOf(end));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MONITOR_TRACK, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    private static List<LocationInfo> convertLocation(List<Positional> history) {
        final List<LocationInfo> infoList = new ArrayList<>(history.size());
        LocationInfo info;
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss").withZone(ZoneId.of("UTC+8"));
        for (Positional data : history) {
            info = new LocationInfo();
            info.setLatitude(new BigDecimal(data.getOriginalLatitude()).scaleByPowerOfTen(6).longValue());
            info.setLongitude(new BigDecimal(data.getOriginalLongtitude()).scaleByPowerOfTen(6).longValue());
            info.setAltitude(new BigDecimal(data.getHeight()).intValue());
            info.setAngle(new BigDecimal(data.getAngle()).intValue());
            info.setTime(formatter.format(Instant.ofEpochSecond(data.getVtime())));
            info.setSpeed(new BigDecimal(data.getSpeed()).intValue());
            if (data.getSatelliteNumber() != null) {
                info.setSatellitesNumber(Integer.parseInt(data.getSatelliteNumber()));
            }
            info.setMileage(new BigDecimal(data.getGpsMile()).intValue());
            info.setStatus(Long.valueOf(data.getStatus()).intValue());
            info.setAlarm(Long.valueOf(data.getAlarm()).intValue());
            parseOilData(info, data);
            parseLoadData(info, data);
            parseTyreData(info, data);
            buildRfid(info, data);
            infoList.add(info);
        }
        return infoList;
    }

    private static void buildRfid(LocationInfo info, Positional data) {
        List<Rfid> rfidList = new ArrayList<>();
        final String rfid1D = data.getRfid1D();
        final String rfid1E = data.getRfid1E();
        final String rfid1F = data.getRfid1F();
        if (StringUtils.isNotBlank(rfid1D)) {
            rfidList.add(JSON.parseObject(rfid1D, Rfid.class));
        }
        if (StringUtils.isNotBlank(rfid1E)) {
            rfidList.add(JSON.parseObject(rfid1E, Rfid.class));
        }
        if (StringUtils.isNotBlank(rfid1F)) {
            rfidList.add(JSON.parseObject(rfid1F, Rfid.class));
        }
        info.setRfidList(rfidList);
    }

    private static void parseOilData(LocationInfo info, Positional data) {
        double[] tankCapacity = new double[2];
        double[] tankAdd = new double[2];
        double[] tankLeak = new double[2];
        double[] tankTemp = new double[2];
        double[] tankOilTemp = new double[2];
        tankCapacity[0] = Double.parseDouble(data.getOilTankOne());
        tankCapacity[1] = Double.parseDouble(data.getOilTankTwo());
        tankAdd[0] = Double.parseDouble(data.getFuelAmountOne());
        tankAdd[1] = Double.parseDouble(data.getFuelAmountTwo());
        tankLeak[0] = Double.parseDouble(data.getFuelSpillOne());
        tankLeak[1] = Double.parseDouble(data.getFuelSpillTwo());
        if (data.getEnvironmentTemOne() != null) {
            tankTemp[0] = Double.parseDouble(data.getEnvironmentTemOne());
        }
        if (data.getEnvironmentTemTwo() != null) {
            tankTemp[1] = Double.parseDouble(data.getEnvironmentTemTwo());
        }
        if (data.getFuelTemOne() != null) {
            tankOilTemp[0] = Double.parseDouble(data.getFuelTemOne());
        }
        if (data.getFuelTemTwo() != null) {
            tankOilTemp[1] = Double.parseDouble(data.getFuelTemTwo());
        }
        info.setFuelTankCapacity(tankCapacity);
        info.setFuelTankAdd(tankAdd);
        info.setFuelTankLeak(tankLeak);
        info.setFuelTankTemp(tankTemp);
        info.setFuelTankOilTemp(tankOilTemp);
    }

    private static void parseLoadData(LocationInfo info, Positional data) {
        JSONObject loadObj = JSON.parseObject(data.getLoadObjOne());
        List<JSONObject> loadObjList = new ArrayList<>(2);
        if (loadObj != null) {
            loadObjList.add(loadObj);
        }
        loadObj = JSON.parseObject(data.getLoadObjTwo());
        if (loadObj != null) {
            loadObjList.add(loadObj);
        }
        if (loadObjList.isEmpty()) {
            return;
        }
        double[] loadWeight = new double[2];
        int[] loadStatus = new int[2];
        for (int i = 0; i < loadObjList.size(); i++) {
            loadObj = loadObjList.get(i);
            // double load = loadObj.getDoubleValue("loadWeight");
            //int unit = loadObj.getIntValue("unit");
            //loadWeight[i] = Math.pow(10, unit) / 10 * load;
            loadWeight[i] = new BigDecimal(loadObj.getOrDefault("loadWeight", "0").toString()).doubleValue();
            loadStatus[i] = loadObj.getIntValue("status");
            if (loadObj.getIntValue("unusual") == 1) {
                loadStatus[i] = 0;
            }
        }
        info.setLoadWeight(loadWeight);
        info.setLoadStatus(loadStatus);
    }

    private static void parseTyreData(LocationInfo info, Positional data) {
        JSONObject tyreData = JSON.parseObject(data.getTirePressureParameter());
        if (tyreData == null) {
            return;
        }

        JSONArray tyreInfoList = tyreData.getJSONArray("list");
        if (tyreInfoList == null || tyreInfoList.isEmpty()) {
            return;
        }
        int length = tyreInfoList.size();
        double[] tyrePressure = new double[length];
        double[] tyreTemp = new double[length];
        int[] tyreStatus = new int[length];
        JSONObject tyreObj;
        for (int i = 0; i < length; i++) {
            tyreObj = (JSONObject) tyreInfoList.get(i);
            tyrePressure[i] = tyreObj.getDoubleValue("pressure");
            tyreTemp[i] = tyreObj.getDoubleValue("temperature");
            tyreStatus[i] = tyreObj.getIntValue("unusual") == 1 ? 0 : 1;
        }
        info.setTyrePressure(tyrePressure);
        info.setTyreTemp(tyreTemp);
        info.setTyreStatus(tyreStatus);
    }

    @Override
    public Set<String> getMonitorIdByName(List<String> name) {
        if (name.isEmpty()) {
            return Collections.emptySet();
        }
        return monitorInfoDao.getIdByName(name);
    }

    @Override
    public DriverInfo getDriver(String name) {
        Set<String> monitorIds = getMonitorIdByName(Collections.singletonList(name));
        if (monitorIds.isEmpty()) {
            return null;
        }
        String id = monitorIds.iterator().next();
        //当前插卡信息
        String current = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(id));
        if (!StringUtils.isEmpty(current)) {
            //{身份证号}_{驾驶员姓名},{插卡时间}
            String cardInfo = SPLITTER.split(current)[0];
            String[] info = DRIVER_SPLITTER.split(cardInfo);
            return queryDriverInfo(info[1], info[0]);
        }
        //最后一次插卡信息
        String last = RedisHelper.getString(HistoryRedisKeyEnum.LAST_DRIVER.of(id));
        if (StringUtils.isEmpty(last)) {
            return null;
        }
        //c_{身份证号}_{驾驶员姓名},t_{插卡时间}
        String cardInfo = SPLITTER.split(last)[0];
        String[] info = DRIVER_SPLITTER.split(cardInfo);
        return queryDriverInfo(info[2], info[1]);
    }

    private DriverInfo queryDriverInfo(String driverName, String identity) {
        DriverInfo driverInfo = monitorInfoDao.getDriverInfo(driverName, identity);
        return driverInfo;
    }
}
