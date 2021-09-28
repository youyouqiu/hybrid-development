package com.zw.platform.service.platformInspection.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.zw.adas.domain.platforminspection.PlatformInspectionDTO;
import com.zw.adas.domain.platforminspection.PlatformInspectionQuery;
import com.zw.adas.utils.FastDFSClient;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.platformInspection.PlatformInspectionDO;
import com.zw.platform.domain.platformInspection.PlatformInspectionResultDO;
import com.zw.platform.domain.reportManagement.form.DriverDiscernReportDo;
import com.zw.platform.dto.platformInspection.PlatformInspectionParamDTO;
import com.zw.platform.dto.platformInspection.PlatformInspectionParamSendDTO;
import com.zw.platform.dto.platformInspection.PlatformInspectionResultDTO;
import com.zw.platform.dto.platformInspection.PlatformInspectionStatusDTO;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.modules.DriverDiscernStatisticsDao;
import com.zw.platform.repository.modules.FenceConfigDao;
import com.zw.platform.repository.modules.PlatformInspectionDao;
import com.zw.platform.repository.modules.PlatformInspectionResultDao;
import com.zw.platform.service.platformInspection.PlatformInspectionService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.reportManagement.impl.DriverDiscernStatisticsServiceImpl;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.Translator;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/***
 * 平台巡检
 * 2020/11/19 10:39
 * @author lijie
 * @version 1.0
 **/
@Service
public class PlatformInspectionServiceImpl implements PlatformInspectionService {
    private static final Logger log = LogManager.getLogger(PlatformInspectionServiceImpl.class);

    public static final Translator<String, Integer> VEHICLE_ALARM_TYPE =
        Translator.of("前向碰撞报警", 1, "车道偏离报警", 2, "超速报警", 3, "路线偏离报警", 4, "禁行路段/区域报警", 5, "事故报警", 6);

    public static final Translator<String, Integer> VEHICLE_WARN_TYPE = Translator.of("前向碰撞预警", 1, "车道偏离预警", 2);

    public static final Translator<String, Integer> DRIVER_ALARM_TYPE =
        Translator.of("疲劳驾驶状态报警", 1, "手持接打电话报警", 2, "长时间不目视前方报警", 3, "驾驶员不在驾驶位置报警", 4, "抽烟报警", 5);

    public static final Translator<String, Integer> DRIVER_WARN_TYPE = Translator.of("疲劳驾驶状态预警", 1);

    public static final Translator<String, Integer> ALARM_STATUS = Translator.of("开始标志", 1, "持续标志", 2, "结束标志", 3);

    public static final Translator<String, Integer> DEPARTURE_TYPE = Translator.of("左侧偏离", 1, "右侧偏离", 2);

    public static final Translator<String, Integer> POHIBITED_TYPE = Translator.of("禁行区域", 1, "禁行路段", 2);

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    public static Integer VEHICLE_INSPECTION = 1;

    public static Integer DRIVER_INSPECTION = 2;

    public static Integer IDENTIFY_INSPECTION = 3;

    public static final int WAIT_RESULT_ACK = 1;

    public static final int RESULT_NOT_ACK = 3;

    public static final int DEVICE_OFFLINE = 4;

    public static Integer VEHICLE_INSPECTION_INDEX = 0x39;

    public static Integer DRIVER_INSPECTION_INDEX = 0X40;

    @Autowired
    PlatformInspectionDao platformInspectionDao;

    @Autowired
    private DelayedEventTrigger trigger;

    @Autowired
    private WebSocketMessageDispatchCenter messageDispatchCenter;

    @Autowired
    private DriverDiscernStatisticsDao driverDiscernStatisticsDao;

    @Autowired
    private NewProfessionalsDao professionalsDao;

    @Autowired
    private PlatformInspectionResultDao platformInspectionResultDao;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private FenceConfigDao fenceConfigDao;

    @Override
    public Page<PlatformInspectionDTO> getListByKeyword(PlatformInspectionQuery query) {
        return (Page<PlatformInspectionDTO>) getList(query);
    }

    @Override
    public boolean export(PlatformInspectionQuery query, HttpServletResponse response) throws IOException {
        List<PlatformInspectionDTO> list = getList(query);
        return ExportExcelUtil
            .export(new ExportExcelParam(null, 1, list, PlatformInspectionDTO.class, null, response.getOutputStream()));
    }

    private List<PlatformInspectionDTO> getList(PlatformInspectionQuery query) {

        List<PlatformInspectionDTO> result = platformInspectionDao.getListByKeyword(query);
        if (result.isEmpty()) {
            return result;
        }
        Map<String, VehicleDTO> vehicleIdOrgIdMap =
            MonitorUtils.getVehicleMap(query.getVehicleIds(), "id", "orgName", "plateColor", "name");
        for (PlatformInspectionDTO platformInspectionDTO : result) {
            VehicleDTO vehicleInfo = vehicleIdOrgIdMap.get(platformInspectionDTO.getVehicleId());
            platformInspectionDTO.setOrgName(vehicleInfo.getOrgName());
            platformInspectionDTO.setPlateColor(vehicleInfo.getPlateColor());
            platformInspectionDTO.setBrand(vehicleInfo.getName());
            platformInspectionDTO.setVehicleId(vehicleInfo.getId());
        }
        return result;
    }

    /**
     * 驾驶员身份识别
     */
    @Override
    public boolean sendDriverIdentify(PlatformInspectionParamDTO platformInspectionParam, String sessionId, String ip) {
        String vehicleId = platformInspectionParam.getVehicleId();
        try {
            PlatformInspectionDO platformInspectionDO = new PlatformInspectionDO();
            platformInspectionDO.setFlag(1);
            platformInspectionDO.setId(UUID.randomUUID().toString());
            platformInspectionDO.setInspectionTime(new Date());
            platformInspectionDO.setInspectionType(platformInspectionParam.getInspectionType());
            platformInspectionDO.setVehicleId(platformInspectionParam.getVehicleId());
            platformInspectionDO.setInspector(SystemHelper.getCurrentUsername());
            platformInspectionParam.setInspectionId(platformInspectionDO.getId());
            //生成下发流水号
            int msgSn = DeviceHelper.serialNumber(platformInspectionParam.getVehicleId());

            //打印日志
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            if (vehicle != null) {
                String brand = vehicle[0];
                String plateColor = vehicle[1];
                String logMsg = "平台巡检:" + brand + "车辆进行驾驶员身份识别巡检";
                logSearchService.addLog(ip, logMsg, "3", "MONITORING", brand, plateColor);
            }

            //判断车辆是否在线
            if (msgSn < 0) {
                platformInspectionDO.setInspectionStatus(DEVICE_OFFLINE);
                // 推送状态给前端
                sendInspectionStatus(9, sessionId, vehicleId, IDENTIFY_INSPECTION, null);
                //存储巡检记录
                platformInspectionDao.insert(platformInspectionDO);
                return false;
            } else {
                platformInspectionDO.setInspectionStatus(WAIT_RESULT_ACK);
                driverIdentifyInspection(platformInspectionParam, msgSn);
                //维护订阅关系
                String inspectionSession = Optional
                    .ofNullable(WsSessionManager.INSTANCE.getInspectionSession(platformInspectionParam.getVehicleId()))
                    .orElse("");
                inspectionSession += "," + sessionId;
                WsSessionManager.INSTANCE.addInspection(platformInspectionParam.getVehicleId(), inspectionSession);

                //推送前端下发成功
                sendInspectionStatus(6, sessionId, vehicleId, IDENTIFY_INSPECTION, null);

                //存储巡检记录
                platformInspectionDao.insert(platformInspectionDO);

                //延时任务3分钟后处理超时逻辑
                trigger
                    .addEvent(3 * 60L, TimeUnit.SECONDS, () -> changeOverTimeStatus(platformInspectionParam, sessionId),
                        platformInspectionParam.getInspectionId());
                return true;

            }

        } catch (Exception e) {
            //推送下发失败
            sendInspectionStatus(5, sessionId, vehicleId, IDENTIFY_INSPECTION, null);

            log.error("黑标下发9706异常！", e);
            return false;
        }
    }

    /**
     * 推送下发状态
     */
    @Override
    public void sendInspectionStatus(Integer status, String sessionId, String vehicleId, Integer inspectionType,
        String brand) {
        PlatformInspectionStatusDTO platformInspectionStatus = new PlatformInspectionStatusDTO();
        platformInspectionStatus.setVehicleId(vehicleId);
        platformInspectionStatus.setInspectionType(inspectionType);
        platformInspectionStatus.setInspectionStatus(status);
        platformInspectionStatus.setBrand(brand);
        messageDispatchCenter.pushMsgToUser(sessionId, WebSocketMessageDispatchCenter.PLATFORM_INSPECTION_ACK_URL,
            JSON.toJSONString(platformInspectionStatus));
    }

    /**
     * 应答超时时处理逻辑
     */
    private void changeOverTimeStatus(PlatformInspectionParamDTO platformInspectionParam, String sessionId) {
        //更新巡检结果为超时
        platformInspectionDao.updateInspectionStatus(RESULT_NOT_ACK, platformInspectionParam.getInspectionId());

        // 推送超时到前端
        sendInspectionStatus(8, sessionId, platformInspectionParam.getVehicleId(), IDENTIFY_INSPECTION, null);
        //清除订阅关系
        String inspectionSession =
            Optional.ofNullable(WsSessionManager.INSTANCE.getInspectionSession(platformInspectionParam.getVehicleId()))
                .orElse("");
        String newInspectionSession = inspectionSession.replace("," + sessionId, "");
        if (newInspectionSession.equals("")) {
            WsSessionManager.INSTANCE.removeInspection(platformInspectionParam.getVehicleId());
        } else {
            WsSessionManager.INSTANCE.addInspection(platformInspectionParam.getVehicleId(), newInspectionSession);
        }

    }

    /**
     * 下发9706
     */
    public void driverIdentifyInspection(PlatformInspectionParamDTO platformInspectionParam, Integer msgSn)
        throws Exception {
        // 获取车辆及设备信息
        BindDTO bindDTO = MonitorUtils.getBindDTO(platformInspectionParam.getVehicleId());
        if (Objects.isNull(bindDTO)) {
            return;
        }
        String deviceId = bindDTO.getDeviceId();
        T808Message message = MsgUtil
            .get808Message(bindDTO.getSimCardNumber(), ConstantUtil.T808_DRIVER_IDENTIFY_INSPECTION, msgSn, null,
                bindDTO.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_DRIVER_IDENTIFY_INSPECTION, deviceId);
    }

    /**
     * 平台巡检
     */
    @Override
    public boolean sendPlatformInspection(List<PlatformInspectionParamDTO> platformInspectionParams, String sessionId) {
        if (CollectionUtils.isEmpty(platformInspectionParams)) {
            return false;
        }
        for (PlatformInspectionParamDTO platformInspectionParam : platformInspectionParams) {
            String vehicleId = platformInspectionParam.getVehicleId();
            String brand = platformInspectionParam.getBrand();
            try {
                PlatformInspectionDO platformInspectionDO = new PlatformInspectionDO();
                platformInspectionDO.setFlag(1);
                platformInspectionDO.setId(UUID.randomUUID().toString());
                platformInspectionDO.setInspectionTime(new Date());
                platformInspectionDO.setInspectionType(platformInspectionParam.getInspectionType());
                platformInspectionDO.setVehicleId(platformInspectionParam.getVehicleId());
                platformInspectionDO.setInspector(SystemHelper.getCurrentUsername());

                platformInspectionParam.setInspectionId(platformInspectionDO.getId());
                //生成下发流水号
                int msgSn = DeviceHelper.serialNumber(platformInspectionParam.getVehicleId());

                //判断车辆是否在线
                if (msgSn < 0) {
                    platformInspectionDO.setInspectionStatus(DEVICE_OFFLINE);
                    // 推送状态给前端
                    sendInspectionStatus(DEVICE_OFFLINE, sessionId, vehicleId,
                        platformInspectionParam.getInspectionType(), brand);
                } else {
                    platformInspectionDO.setInspectionStatus(WAIT_RESULT_ACK);
                    platformInspection(platformInspectionParam, msgSn);
                    String idAndMsgSn = platformInspectionParam.getVehicleId() + "_" + msgSn;
                    platformInspectionParam.setIdAndMsgSn(idAndMsgSn);

                    //推送前端下发中
                    sendInspectionStatus(WAIT_RESULT_ACK, sessionId, vehicleId,
                        platformInspectionParam.getInspectionType(), brand);

                    WsSessionManager.INSTANCE
                        .addInspection(idAndMsgSn, sessionId + "_" + platformInspectionParam.getTime());
                }
                //存储巡检记录
                platformInspectionDao.insert(platformInspectionDO);

            } catch (Exception e) {
                //推送下发失败
                sendInspectionStatus(0, sessionId, vehicleId, platformInspectionParam.getInspectionType(), brand);
                log.error("黑标下发9710异常！", e);
            }
        }

        //延时任务
        trigger.addEvent(3 * 60L, TimeUnit.SECONDS, () -> updateOverTimeInspection(platformInspectionParams,
            sessionId + "_" + platformInspectionParams.get(0).getTime()),
            sessionId + "_" + platformInspectionParams.get(0).getTime());
        return true;
    }

    private void updateOverTimeInspection(List<PlatformInspectionParamDTO> platformInspectionParams, String sessionId) {
        Set<String> overTimeInspectionIds = platformInspectionDao.getOverTimeInspection(platformInspectionParams);
        Set<String> inspectionValues = WsSessionManager.INSTANCE.getInspectionValues();
        if (overTimeInspectionIds.size() > 0) {
            platformInspectionDao.batchUpdateInspectionStatus(RESULT_NOT_ACK, overTimeInspectionIds);
            for (PlatformInspectionParamDTO platformInspectionParamDTO : platformInspectionParams) {
                if (overTimeInspectionIds.contains(platformInspectionParamDTO.getInspectionId()) && inspectionValues
                    .contains(sessionId)) {
                    // 推送超时到前端
                    sendInspectionStatus(RESULT_NOT_ACK, sessionId.split("_")[0],
                        platformInspectionParamDTO.getVehicleId(), platformInspectionParamDTO.getInspectionType(),
                        platformInspectionParamDTO.getBrand());
                }
            }
        }

        for (PlatformInspectionParamDTO platformInspectionParamDTO : platformInspectionParams) {
            WsSessionManager.INSTANCE.removeInspection(platformInspectionParamDTO.getIdAndMsgSn());
        }
    }

    /**
     * 下发9710
     */
    public void platformInspection(PlatformInspectionParamDTO platformInspectionParam, Integer msgSn) throws Exception {
        // 获取车辆及设备信息
        BindDTO bindDTO = MonitorUtils.getBindDTO(platformInspectionParam.getVehicleId());
        if (Objects.isNull(bindDTO)) {
            return;
        }
        String deviceId = bindDTO.getDeviceId();
        PlatformInspectionParamSendDTO platformInspectionParamSendDTO = new PlatformInspectionParamSendDTO();
        platformInspectionParamSendDTO.setType(platformInspectionParam.getInspectionType() - 1);

        //订阅推送消息
        SubscibeInfo info =
            new SubscibeInfo(SystemHelper.getCurrentUsername(), platformInspectionParam.getInspectionId(), deviceId,
                msgSn, ConstantUtil.T808_FLATFORM_INSPECTION_ACK);
        SubscibeInfoCache.getInstance().putTable(info);

        T808Message message = MsgUtil
            .get808Message(bindDTO.getSimCardNumber(), ConstantUtil.T808_FLATFORM_INSPECTION, msgSn,
                platformInspectionParamSendDTO, bindDTO.getDeviceType());
        WebSubscribeManager.getInstance().sendMsgToAll(message, ConstantUtil.T808_FLATFORM_INSPECTION, deviceId);
    }

    /**
     * 获取巡检结果
     */
    @Override
    public PlatformInspectionResultDTO getInspectionResult(String id, Integer inspectionType) {
        PlatformInspectionResultDTO platformInspectionResultDTO = new PlatformInspectionResultDTO();
        if (inspectionType.equals(IDENTIFY_INSPECTION)) {
            setIdentifyInspectionResult(platformInspectionResultDTO, id);
        } else if (inspectionType.equals(DRIVER_INSPECTION)) {
            setDriverInspectionResult(platformInspectionResultDTO, id);
        } else {
            setVehicleInspectionResult(platformInspectionResultDTO, id);
        }
        setPlatformInspectionResultDTO(platformInspectionResultDTO);

        return platformInspectionResultDTO;
    }

    private void setIdentifyInspectionResult(PlatformInspectionResultDTO platformInspectionResultDTO, String id) {
        DriverDiscernReportDo driverDiscernReportDo = driverDiscernStatisticsDao.getById(id);
        platformInspectionResultDTO.setVehicleId(driverDiscernReportDo.getMonitorId());
        platformInspectionResultDTO.setCardNumber(driverDiscernReportDo.getCardNumber());
        platformInspectionResultDTO.setFaceId(driverDiscernReportDo.getFaceId());
        String driverId = driverDiscernReportDo.getDriverId();
        ProfessionalDTO professionals = null;
        if (StringUtils.isNotEmpty(driverId)) {
            professionals = professionalsDao.getProfessionalById(driverDiscernReportDo.getDriverId());
        }
        platformInspectionResultDTO.setDriverName(professionals == null ? "" : professionals.getName());
        platformInspectionResultDTO.setIdentificationResult(DriverDiscernStatisticsServiceImpl.IDENTIFICATION_RESULT
            .p2b(driverDiscernReportDo.getIdentificationResult()));
        platformInspectionResultDTO.setIdentificationType(
            DriverDiscernStatisticsServiceImpl.IDENTIFICATION_TYPE.p2b(driverDiscernReportDo.getIdentificationType()));
        platformInspectionResultDTO.setMatchRate(driverDiscernReportDo.getMatchRate());
        platformInspectionResultDTO.setImageUrl(driverDiscernReportDo.getImageUrl());
        platformInspectionResultDTO.setVideoUrl(driverDiscernReportDo.getVideoUrl());
        platformInspectionResultDTO.setTime(
            DateUtil.getDateToString(driverDiscernReportDo.getIdentificationTime(), DateUtil.DATE_FORMAT_SHORT));
        platformInspectionResultDTO.setType(IDENTIFY_INSPECTION);
    }

    private void setDriverInspectionResult(PlatformInspectionResultDTO platformInspectionResultDTO, String id) {
        PlatformInspectionResultDO platformInspectionResultDO = platformInspectionResultDao.getById(id);
        platformInspectionResultDTO.setVehicleId(platformInspectionResultDO.getVehicleId());
        String driverId = platformInspectionResultDO.getDriverId();
        ProfessionalDTO professionals = null;
        if (StringUtils.isNotEmpty(driverId)) {
            professionals = professionalsDao.getProfessionalById(driverId);
        }
        platformInspectionResultDTO.setDriverName(professionals == null ? "" : professionals.getName());
        platformInspectionResultDTO.setRemindFlag(platformInspectionResultDO.getRemindFlag());
        platformInspectionResultDTO.setAlarmType(DRIVER_ALARM_TYPE.p2b(platformInspectionResultDO.getAlarmType()));
        platformInspectionResultDTO.setWarnType(DRIVER_WARN_TYPE.p2b(platformInspectionResultDO.getWarnType()));
        Date inspectionResultDOTime = platformInspectionResultDO.getTime();
        if (inspectionResultDOTime != null) {
            platformInspectionResultDTO
                .setTime(DateUtil.getDateToString(inspectionResultDOTime, DateUtil.DATE_FORMAT_SHORT));
        }
        platformInspectionResultDTO.setImageUrl(platformInspectionResultDO.getImageUrl());
        platformInspectionResultDTO.setVideoUrl(platformInspectionResultDO.getVideoUrl());
        platformInspectionResultDTO.setType(DRIVER_INSPECTION);
    }

    private void setVehicleInspectionResult(PlatformInspectionResultDTO platformInspectionResultDTO, String id) {
        PlatformInspectionResultDO platformInspectionResultDO = platformInspectionResultDao.getById(id);
        platformInspectionResultDTO.setVehicleId(platformInspectionResultDO.getVehicleId());
        platformInspectionResultDTO.setRemindFlag(platformInspectionResultDO.getRemindFlag());
        int alarmType =
            platformInspectionResultDO.getAlarmType() == null ? 0 : platformInspectionResultDO.getAlarmType();
        int warnType =
            platformInspectionResultDO.getWarnType() == null ? 0 : platformInspectionResultDO.getWarnType();
        platformInspectionResultDTO.setAlarmType(VEHICLE_ALARM_TYPE.p2b(alarmType));
        platformInspectionResultDTO.setWarnType(VEHICLE_WARN_TYPE.p2b(warnType));
        Date inspectionResultDOTime = platformInspectionResultDO.getTime();
        if (inspectionResultDOTime != null) {
            platformInspectionResultDTO
                .setTime(DateUtil.getDateToString(inspectionResultDOTime, DateUtil.DATE_FORMAT_SHORT));
        }
        platformInspectionResultDTO.setImageUrl(platformInspectionResultDO.getImageUrl());
        platformInspectionResultDTO.setVideoUrl(platformInspectionResultDO.getVideoUrl());
        if (alarmType == 0x02 || warnType == 0x02) {
            platformInspectionResultDTO.setDepartureType(DEPARTURE_TYPE.p2b(platformInspectionResultDO.getType()));
        }

        if (alarmType == 0x03) {
            platformInspectionResultDTO.setSpeedStatus(ALARM_STATUS.p2b(platformInspectionResultDO.getStatus()));
        }

        Map<String, Object> routeInfo = new HashMap<>();
        if (StringUtils.isNotEmpty(platformInspectionResultDO.getRouteId())) {
            Map<String, Object> routeMap =
                getRouteInfo(platformInspectionResultDO.getRouteId(), platformInspectionResultDO.getVehicleId());
            routeInfo = routeMap == null ? new HashMap<>() : routeMap;
        }

        if (alarmType == 0x04) {
            platformInspectionResultDTO.setDepartureStatus(ALARM_STATUS.p2b(platformInspectionResultDO.getStatus()));
            platformInspectionResultDTO.setRouteId(platformInspectionResultDO.getRouteId());
            platformInspectionResultDTO.setRoute(Optional.ofNullable(routeInfo.get("name")).orElse("").toString());
        }

        if (alarmType == 0x05) {
            platformInspectionResultDTO.setPohibitedStatus(ALARM_STATUS.p2b(platformInspectionResultDO.getStatus()));
            platformInspectionResultDTO.setPohibitedType(POHIBITED_TYPE.p2b(platformInspectionResultDO.getType()));
            platformInspectionResultDTO.setRouteId(platformInspectionResultDO.getRouteId());
            platformInspectionResultDTO.setRoute(Optional.ofNullable(routeInfo.get("name")).orElse("").toString());
        }
        platformInspectionResultDTO.setType(VEHICLE_INSPECTION);
    }

    private void setPlatformInspectionResultDTO(PlatformInspectionResultDTO platformInspectionResultDTO) {

        VehicleDTO vehicleInfo = MonitorUtils.getVehicle(platformInspectionResultDTO.getVehicleId());
        platformInspectionResultDTO.setBrand(vehicleInfo.getName());
        platformInspectionResultDTO.setOrgName(vehicleInfo.getOrgName());

        JSONObject imageInfo = new JSONObject();
        List<String> imageList = new ArrayList<>();
        if (platformInspectionResultDTO.getImageUrl() != null) {
            imageList = Arrays.asList(platformInspectionResultDTO.getImageUrl().split(","));
            imageList = imageList.stream().map(this::getFilePath).collect(Collectors.toList());
        }
        imageInfo.put("imageList", imageList);
        imageInfo.put("count", imageList.size());

        JSONObject videoInfo = new JSONObject();
        List<String> videoList = new ArrayList<>();
        if (platformInspectionResultDTO.getVideoUrl() != null) {
            videoList = Arrays.asList(platformInspectionResultDTO.getVideoUrl().split(","));
            videoList = videoList.stream().map(this::getFilePath).collect(Collectors.toList());

        }
        videoInfo.put("videoList", videoList);
        videoInfo.put("count", videoList.size());

        platformInspectionResultDTO.setImageInfo(imageInfo);
        platformInspectionResultDTO.setVideoInfo(videoInfo);
    }

    private String getFilePath(String oldPath) {
        String path;
        if (sslEnabled) {
            path = "/" + oldPath;
        } else {
            path = fastDFSClient.getWebAccessUrl(oldPath);
        }
        return path;
    }

    /**
     * 获取线路相关信息
     */
    private Map<String, Object> getRouteInfo(String routeId, String vehicleId) {
        return fenceConfigDao.findFenceInfoByVehicleIdAndHashCode(Integer.parseInt(routeId), vehicleId);
    }

}
