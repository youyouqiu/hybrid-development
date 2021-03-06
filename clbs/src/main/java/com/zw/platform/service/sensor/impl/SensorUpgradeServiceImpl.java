package com.zw.platform.service.sensor.impl;

import com.github.pagehelper.Page;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.GroupDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.param.RemoteUpgradeSensorBasicInfo;
import com.zw.platform.domain.param.RemoteUpgradeTask;
import com.zw.platform.domain.param.SensorRemoteUpgrade;
import com.zw.platform.domain.param.TotalDataValidationOrder;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.domain.vas.f3.Peripheral;
import com.zw.platform.domain.vas.sensorUpgrade.MonitorSensorUpgrade;
import com.zw.platform.domain.vas.sensorUpgrade.SenosrMonitorQuery;
import com.zw.platform.domain.vas.sensorUpgrade.SensorBind;
import com.zw.platform.domain.vas.sensorUpgrade.SensorType;
import com.zw.platform.domain.vas.sensorUpgrade.SensorUpgrade;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.repository.vas.SensorPollingDao;
import com.zw.platform.repository.vas.SensorUpgradeDao;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.sensor.PeripheralService;
import com.zw.platform.service.sensor.RemoteUpgradeInstance;
import com.zw.platform.service.sensor.SensorUpgradeService;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.SendHelper;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisQueryUtil;
import com.zw.platform.util.common.VehicleUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ???????????????
 * @author zhouzongbo on 2019/1/18 11:09
 */
@Service
public class SensorUpgradeServiceImpl implements SensorUpgradeService {
    public static final Logger logger = LogManager.getLogger(SensorUpgradeServiceImpl.class);

    @Autowired
    private SensorUpgradeDao sensorUpgradeDao;

    @Autowired
    private SensorPollingDao sensorPollingDao;

    @Autowired
    private SendHelper sendHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    @Autowired
    private PeripheralService peripheralService;

    @Autowired
    private LogSearchService logSearchService;

    /**
     * ?????????????????????, ????????????
     */
    private static final ExecutorService upgradeExecutorService =
        new ThreadPoolExecutor(20, 50, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(10),
            new BasicThreadFactory.Builder().namingPattern("sensor-remote-upgrade-%d").build());

    @Override
    public List<SensorType> getSensorIdList() {
        return sensorUpgradeDao.getAllSensorList();
    }

    @Override
    public Page<MonitorSensorUpgrade> findMonitorByPage(SenosrMonitorQuery query) {
        // ???????????????????????? 0:????????????; 1:??????; 2:??????
        String fuzzySign = query.getParamSign();
        if (StringUtils.isBlank(fuzzySign)) {
            return new Page<>();
        }
        List<String> monitorIds = userService.getValidVehicleId(fuzzySign, query.getFuzzyParam(), null, null, true);
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new Page<>();
        }
        // ?????????id
        String sensorId = query.getSensorId();
        // ?????????????????????????????????????????????
        List<String> resultMonitorId;
        if (StringUtils.isNotBlank(sensorId)) {
            resultMonitorId = sensorPollingDao.getBindSensorMonitorBySensorId(sensorId);
        } else {
            resultMonitorId = sensorPollingDao.getAllBindSensorMonitor();
        }
        monitorIds.retainAll(resultMonitorId);
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new Page<>();
        }
        // ??????
        int listSize = monitorIds.size();
        // ?????????
        int curPage = query.getPage().intValue();
        // ????????????
        int pageSize = query.getLimit().intValue();
        // ??????????????????
        int lst = (curPage - 1) * pageSize;
        // ????????????
        int ps = pageSize > (listSize - lst) ? listSize : (pageSize * curPage);
        List<String> pageVehicleIdList = monitorIds.subList(lst, ps);
        // ?????????id????????????id???????????????????????????????????????????????????
        Map<String, SensorUpgrade> monitorUpgrade = getMonitorUpgrade(pageVehicleIdList, sensorId);
        List<GroupDTO> currentUserGroupList = userService.getCurrentUserGroupList();
        Map<String, String> userGroupIdAndNameMap =
            currentUserGroupList.stream().collect(Collectors.toMap(GroupDTO::getId, GroupDTO::getName));
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(pageVehicleIdList);
        List<MonitorSensorUpgrade> result = new ArrayList<>();
        for (String moId : pageVehicleIdList) {
            MonitorSensorUpgrade monitorSensorUpgrade = new MonitorSensorUpgrade();
            BindDTO bindDTO = bindInfoMap.get(moId);
            if (bindDTO != null) {
                String groupIds = bindDTO.getGroupId();
                String groupNames = Arrays.stream(groupIds.split(","))
                    .map(userGroupIdAndNameMap::get)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.joining(","));
                monitorSensorUpgrade.setBrand(bindDTO.getName());
                monitorSensorUpgrade.setGroupName(bindDTO.getOrgName());
                monitorSensorUpgrade.setAssagnName(groupNames);
                monitorSensorUpgrade.setDeviceId(bindDTO.getDeviceId());
            }
            monitorSensorUpgrade.setVehicleId(moId);
            monitorSensorUpgrade.setSensorId(sensorId);
            SensorUpgrade sensorUpgrade = monitorUpgrade.get(moId);
            if (sensorUpgrade != null) {
                monitorSensorUpgrade
                    .setSensorUpgradeDateStr(LocalDateUtils.dateFormate(sensorUpgrade.getSensorUpgradeDate()));
                monitorSensorUpgrade.setSensorUpgradeStatus(sensorUpgrade.getSensorUpgradeStatus());
            }
            result.add(monitorSensorUpgrade);
        }
        // ??????????????????
        return RedisQueryUtil.getListToPage(result, query, listSize);
    }

    /**
     * ???????????????????????????????????????????????????
     */
    private Map<String, SensorUpgrade> getMonitorUpgrade(List<String> monitorIds, String sensorId) {
        if (CollectionUtils.isEmpty(monitorIds)) {
            return new HashMap<>(4);
        }
        Map<String, SensorUpgrade> monitorInfo = new HashMap<>(16);
        SensorBind sensorBind = new SensorBind(monitorIds, sensorId);
        List<SensorUpgrade> upgradesData = sensorUpgradeDao.getMonitorSensorUpgradeStatus(sensorBind);
        if (CollectionUtils.isEmpty(upgradesData)) {
            return new HashMap<>(4);
        }
        upgradesData.forEach(data -> {
            String mapKey = data.getVehicleId();
            SensorUpgrade monitorUpgradeInfo = monitorInfo.putIfAbsent(mapKey, data);
            if (monitorUpgradeInfo != null) {
                long newest = monitorUpgradeInfo.getSensorUpgradeDate().getTime();
                long contrastTime = data.getSensorUpgradeDate().getTime();
                if (contrastTime > newest) {
                    monitorInfo.put(mapKey, data);
                }
            }
        });
        return monitorInfo;
    }

    @Override
    public JsonResultBean sendRemoteUpgrade(String monitorIds, Integer peripheralId, MultipartFile file) {
        // ??????????????????
        List<byte[]> packageList = new ArrayList<>();

        // ???????????????????????????64kb
        final long maxFileSize = 64 * 1024;
        long size = file.getSize();
        if (size > maxFileSize) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????64kb????????????!");
        }
        // ??????ID,????????????
        TotalDataValidationOrder validationOrder = new TotalDataValidationOrder();
        validationOrder.setId(peripheralId);
        validationOrder.setLen(10);
        validationOrder.setControl(SensorRemoteUpgrade.TOTAL_DATA_VALIDATION_COMMAND);

        if (splitPackage(file, packageList, validationOrder)) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????!");
        }
        UserDTO currentUserInfo = userService.getCurrentUserInfo();
        String userName = currentUserInfo.getUsername();
        String currentUid = currentUserInfo.getId().toString();
        Semaphore semaphore = RemoteUpgradeInstance.getInstance().getUserSemaphore(currentUid);
        List<String> monitorIdList = Arrays.stream(monitorIds.split(",")).collect(Collectors.toList());
        Map<String, BindDTO> bindInfoMap = VehicleUtil.batchGetBindInfosByRedis(monitorIdList);
        Map<String, String> peripheralMap = peripheralService.findAllow().stream()
            .collect(Collectors.toMap(Peripheral::getIdentId, Peripheral::getName, (o, n) -> o));
        // ??????ID 10????????????16??????
        String hexPerId = "0x" + Integer.toHexString(peripheralId);
        String ipAddress = getIpAddress();
        List<RemoteUpgradeTask> remoteUpgradeTaskList = new ArrayList<>();
        List<String> deviceIdList = new ArrayList<>();
        for (String moId : monitorIdList) {
            BindDTO bindDTO = bindInfoMap.get(moId);
            if (bindDTO == null) {
                continue;
            }
            String deviceId = bindDTO.getDeviceId();
            if (RemoteUpgradeInstance.getInstance().isContains(deviceId)) {
                continue;
            }
            RemoteUpgradeTask task =
                new RemoteUpgradeTask(upgradeExecutorService, packageList, semaphore, sendHelper, sensorUpgradeDao);
            task.setDeviceNumber(bindDTO.getDeviceNumber());
            task.setMonitorId(bindDTO.getId());
            task.setSimCardNumber(bindDTO.getSimCardNumber());
            task.setPlateNumber(bindDTO.getName());
            task.setDeviceId(deviceId);
            task.setUserId(currentUid);
            task.setUserName(userName);
            task.setSimpMessagingTemplateUtil(simpMessagingTemplateUtil);
            task.setDeviceType(bindDTO.getDeviceType());
            task.setPeripheralId(peripheralId);
            task.setPeripheralName(peripheralMap.get(hexPerId));
            task.setIpAddress(ipAddress);
            task.setOrgId(bindDTO.getOrgId());
            task.setPlateColor(bindDTO.getPlateColor());
            remoteUpgradeTaskList.add(task);
            RemoteUpgradeInstance.getInstance().putRemoteUpgradeTask(deviceId, task);
            RemoteUpgradeInstance.getInstance().putUserUpgrade(currentUid, deviceId);
            deviceIdList.add(deviceId);
        }

        JsonResultBean jsonResultBean = checkUpgrades(deviceIdList);
        if (jsonResultBean != null) {
            return jsonResultBean;
        }

        if (CollectionUtils.isNotEmpty(deviceIdList)) {
            SensorRemoteUpgradeSend upgradeSend = new SensorRemoteUpgradeSend(sendHelper);
            upgradeExecutorService.execute(() -> {
                try {
                    for (RemoteUpgradeTask remoteUpgradeTask : remoteUpgradeTaskList) {
                        semaphore.acquire();
                        // ????????????????????????
                        logSearchService.addLogBean(buildLogForm(remoteUpgradeTask));
                        remoteUpgradeTask.sendPackage(validationOrder, upgradeSend, peripheralId);
                    }
                } catch (InterruptedException e) {
                    logger.error("????????????????????????", e);
                }
            });
        }

        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    private LogSearchForm buildLogForm(RemoteUpgradeTask remoteUpgradeTask) {
        LogSearchForm form = new LogSearchForm();
        form.setEventDate(new Date());
        form.setUsername(remoteUpgradeTask.getUserName());
        form.setBrand(remoteUpgradeTask.getPlateNumber());
        form.setGroupId(remoteUpgradeTask.getOrgId());
        String content = remoteUpgradeTask.getPeripheralName() + "????????????";
        form.setMessage(content);
        form.setMonitoringOperation(content);
        form.setIpAddress(remoteUpgradeTask.getIpAddress());
        form.setLogSource("3");
        form.setPlateColor(remoteUpgradeTask.getPlateColor());
        return form;
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param deviceIdList deviceIdList
     */
    private JsonResultBean checkUpgrades(List<String> deviceIdList) {
        Set<String> totalUpgrades = RemoteUpgradeInstance.getInstance().getTotalUpgrades();
        totalUpgrades.addAll(deviceIdList);
        if (totalUpgrades.size() > 200) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????200???!");
        }

        Set<String> userUpgrades = RemoteUpgradeInstance.getInstance().getUserUpgrades(SystemHelper.getCurrentUId());
        userUpgrades.addAll(deviceIdList);
        if (userUpgrades.size() > 50) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????????????????????????????50???!");
        }
        return null;
    }

    /**
     * ??????
     * @param file            file
     * @param packageList     ?????????
     * @param validationOrder ?????????????????????
     */
    private boolean splitPackage(MultipartFile file, List<byte[]> packageList,
        TotalDataValidationOrder validationOrder) {
        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(file.getInputStream())) {
            final int maxLength = 800;
            byte[] packageBytes = new byte[1024];

            int len;
            while ((len = bufferedInputStream.read(packageBytes)) != -1) {
                outStream.write(packageBytes, 0, len);
            }

            // ????????????
            int allByte = outStream.size();
            logger.info("??????????????????: " + allByte);
            int totalPackageSize = allByte / maxLength;
            int lastPackageBytes = allByte % maxLength;
            byte[] totalByteArray = outStream.toByteArray();
            builderValidationOrder(validationOrder, allByte, totalByteArray);

            int from;
            int to = 0;
            for (int i = 0; i < totalPackageSize; i++) {
                from = i * maxLength;
                to = from + maxLength;
                packageList.add(Arrays.copyOfRange(totalByteArray, from, to));
            }

            // ?????????????????????990?????????, ????????????
            if (lastPackageBytes > 0) {
                packageList.add(Arrays.copyOfRange(totalByteArray, to, allByte));
            }
            logger.info("????????? = " + packageList.size());
        } catch (Exception e) {
            logger.error("?????????????????????!", e);
            return true;
        }
        return false;
    }

    private void builderValidationOrder(TotalDataValidationOrder validationOrder, long allByte, byte[] totalByteArray) {
        // ???????????????; ??????Byte????????????
        long upCheckCode = 0L;
        // ???????????????; ?????????XOR?????? ^ ??????,???????????????????????????????????????????????????????????????????????????0????????????1
        int xorCheckCode = 0;
        for (byte upgradePackage : totalByteArray) {
            int upgrade = byte2Int(upgradePackage);
            upCheckCode += upgrade;
            xorCheckCode ^= upgrade;
        }

        validationOrder.setAllByte(allByte);
        validationOrder.setUpCheckCode(getLowSite(upCheckCode));
        validationOrder.setXorCheckCode(xorCheckCode);
    }

    private int byte2Int(byte b) {
        return (b & 0xff);
    }

    /**
     * ?????? ???????????????4??????
     * @param upCheckCode ?????????
     */
    private Long getLowSite(Long upCheckCode) {
        String upCheckCodeStr = Long.toBinaryString(upCheckCode);
        int upCheckCodeStrLength = upCheckCodeStr.length();
        int i1 = upCheckCodeStrLength > 32 ? upCheckCodeStrLength - 32 : 0;
        String lowFourSite = upCheckCodeStr.substring(i1, upCheckCodeStrLength);
        return Long.parseLong(lowFourSite, 2);
    }

    @Override
    public JsonResultBean updateTerminationUpgrade(String deviceId) {
        RemoteUpgradeTask remoteUpgradeTask = RemoteUpgradeInstance.getInstance().getRemoteUpgradeTask(deviceId);
        if (Objects.nonNull(remoteUpgradeTask)) {
            // ???????????????????????????????????????, ??????????????????
            remoteUpgradeTask.stopUpgrade();
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    @Override
    public RemoteUpgradeSensorBasicInfo getBasicInfo(String monitorId, String sensorId) {
        // ?????????????????????
        RemoteUpgradeSensorBasicInfo remoteUpgradeSensorBasicInfo = null;
        if (StringUtils.isNotBlank(monitorId) && StringUtils.isNotBlank(sensorId)) {
            /* 1.????????????id???????????????????????????????????????????????? */
            sensorId = sensorId.replaceAll("0x", "");
            switch (sensorId) {
                // ???????????????1?????????1???
                case "41":
                    // ???????????????2?????????2???
                case "42":
                    // ???????????????3?????????3???
                case "43":
                    // ???????????????4?????????4???
                case "44":
                    remoteUpgradeSensorBasicInfo =
                        sensorUpgradeDao.getOilSensorBasicInfo(monitorId, Integer.parseInt(sensorId.substring(1)));
                    if (Objects.nonNull(remoteUpgradeSensorBasicInfo)) {
                        remoteUpgradeSensorBasicInfo.setSensorOutId(sensorId);
                    }
                    break;
                // ???????????????1
                case "45":
                    //                case "46":// ???????????????2
                    //???????????????????????????????????????
                    remoteUpgradeSensorBasicInfo = sensorUpgradeDao.getFluxSensorBasicInfo(monitorId, sensorId);
                    if (Objects.nonNull(remoteUpgradeSensorBasicInfo)) {
                        remoteUpgradeSensorBasicInfo.setSensorOutId(sensorId);
                    }
                    break;
                // ???????????????1???????????????
                case "47":
                    // ???????????????2???????????????
                case "48":
                    // ???????????????3???????????????
                case "49":
                    // ???????????????4???????????????
                case "4A":
                    // ???????????????5???????????????
                case "4B":
                    // ???????????????6???????????????
                case "4C":
                    // ???????????????7???????????????
                case "4D":
                    // ???????????????8???????????????
                case "4E":
                    // ???????????????1
                case "21":
                    // ???????????????2
                case "22":
                    // ???????????????3
                case "23":
                    // ???????????????4
                case "24":
                    // ???????????????5
                case "25":
                    // ???????????????1
                case "26":
                    // ???????????????2
                case "27":
                    // ???????????????3
                case "28":
                    // ???????????????4
                case "29":
                    // ???????????????5
                case "2A":
                    // ??????????????????
                case "51":
                    // ???????????????1
                case "70":
                    // ???????????????2
                case "71":
                    // ???????????????1
                case "80":
                    // ???????????????2
                case "81":
                    remoteUpgradeSensorBasicInfo = sensorUpgradeDao.getGeneralSensorBasicInfo(monitorId, sensorId);
                    if (Objects.nonNull(remoteUpgradeSensorBasicInfo)) {
                        remoteUpgradeSensorBasicInfo.setSensorOutId(sensorId);
                    }
                    break;
                // ??????????????????????????????????????????
                case "53":
                    remoteUpgradeSensorBasicInfo = sensorUpgradeDao.getMileSensorBasicInfo(monitorId, sensorId);
                    if (Objects.nonNull(remoteUpgradeSensorBasicInfo)) {
                        remoteUpgradeSensorBasicInfo.setSensorOutId(sensorId);
                    }
                    break;
                //                case "66":// ???????????????
                //                case "91":// ??????IO?????????1
                //                case "92":// ??????IO?????????2
                //                    return
                default:
                    break;
            }
        }
        // ??????????????????????????????????????????????????????
        if (remoteUpgradeSensorBasicInfo == null) {
            remoteUpgradeSensorBasicInfo = new RemoteUpgradeSensorBasicInfo();
        }
        return remoteUpgradeSensorBasicInfo;
    }
}
