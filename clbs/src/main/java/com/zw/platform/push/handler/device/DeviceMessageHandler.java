/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.platform.push.handler.device;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.driverStatistics.VehicleIcHistoryDO;
import com.zw.adas.domain.riskManagement.AlarmSign;
import com.zw.adas.push.cache.AdasSubcibeTable;
import com.zw.adas.push.common.AdasSimpMessagingTemplateUtil;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasVehicleCardNumDao;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.basic.service.impl.ProfessionalServiceImpl;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.multimedia.MultimediaData;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.param.InformationParam;
import com.zw.platform.domain.param.InformationParamInfo;
import com.zw.platform.domain.platformInspection.PlatformInspectionResultDO;
import com.zw.platform.domain.realTimeVideo.DiskInfo;
import com.zw.platform.domain.realTimeVideo.VideoTrafficInfo;
import com.zw.platform.domain.reportManagement.form.DriverDiscernReportDo;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.domain.vas.monitoring.MonitorCommandBindForm;
import com.zw.platform.push.common.SimpMessagingTemplateUtil;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.DriverDiscernStatisticsDao;
import com.zw.platform.repository.modules.ParameterDao;
import com.zw.platform.repository.modules.PlatformInspectionDao;
import com.zw.platform.repository.modules.PlatformInspectionResultDao;
import com.zw.platform.repository.realTimeVideo.VideoFlowDao;
import com.zw.platform.repository.vas.RealTimeCommandDao;
import com.zw.platform.service.platformInspection.PlatformInspectionService;
import com.zw.platform.service.platformInspection.impl.PlatformInspectionServiceImpl;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.reportManagement.MediaService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.MessageMenuSendTimer;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import com.zw.protocol.msg.RtpMessage;
import com.zw.protocol.msg.rtp.RtpData;
import com.zw.protocol.msg.rtp.VideoPlayMsg;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.MessageType;
import com.zw.ws.entity.vehicle.ClientVehicleInfo;
import com.zw.ws.entity.vehicle.VehicleStatus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * team: ZhongWeiTeam </p>
 * @version 1.0
 */
@Component
@Log4j2
public class DeviceMessageHandler {

    /**
     * ?????????????????????
     */
    static final Pattern cardNumAndNamPattern = Pattern.compile("[a-zA-Z0-9\\u4e00-\\u9fa5]+");

    static final Pattern expiryDatePattern = Pattern.compile("\\d{8}");

    @Autowired
    VideoFlowDao videoFlowDao;

    @Autowired
    AdasSubcibeTable adasSubcibeTable;
    @Autowired
    private WebSocketMessageDispatchCenter webSocketMessageDispatchCenter;
    @Autowired
    private ParameterDao parameterDao;
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private MediaService mediaService;
    @Autowired
    private LogSearchService logSearchService;
    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;
    @Autowired
    private RealTimeCommandDao realTimeCommandDao;
    @Autowired
    private AdasVehicleCardNumDao adasVehicleCardNumDao;
    @Value("${mode.check}")
    private Boolean modeCheck;
    @Autowired
    private UserService userService;
    @Autowired
    DriverDiscernStatisticsDao driverDiscernStatisticsDao;
    @Autowired
    PlatformInspectionDao platformInspectionDao;
    @Autowired
    PlatformInspectionResultDao platformInspectionResultDao;
    @Autowired
    AdasSimpMessagingTemplateUtil adasSimpMessagingTemplateUtil;
    @Autowired
    private DelayedEventTrigger trigger;
    @Autowired
    PlatformInspectionService platformInspectionService;
    @Autowired
    private NewProfessionalsDao newProfessionalsDao;
    @Value("${driver.create-on-0702:true}")
    private Boolean saveNewDriverOn0702;

    /**
     * ????????????
     */
    public static String getDirectionStr(Integer angle) {
        String direction = "";
        if (angle != null) {
            if ((0 <= angle && 22.5 >= angle) || (337.5 < angle && angle <= 360)) {
                direction = "???";
            } else if (22.5 < angle && 67.5 >= angle) {
                direction = "??????";
            } else if (67.5 < angle && 112.5 >= angle) {
                direction = "???";
            } else if (112.5 < angle && 157.5 >= angle) {
                direction = "??????";
            } else if (157.5 < angle && 202.5 >= angle) {
                direction = "???";
            } else if (202.5 < angle && 247.5 >= angle) {
                direction = "??????";
            } else if (247.5 < angle && 292.5 >= angle) {
                direction = "???";
            } else if (292.5 < angle && 337.5 >= angle) {
                direction = "??????";
            } else {
                direction = "????????????";
            }
        }
        return direction;
    }

    public void deviceOffLineHandler(MsgDesc desc) {
        String monitorId = desc.getMonitorId();
        String monitorName = desc.getMonitorName();
        //???????????????????????????????????????
        List<ClientVehicleInfo> clientVehicleList = new ArrayList<>();
        ClientVehicleInfo clientVehicleInfo1 = new ClientVehicleInfo();
        clientVehicleInfo1.setVehicleId(monitorId);
        clientVehicleInfo1.setVehicleStatus(VehicleStatus.OFFLINE);
        clientVehicleInfo1.setSpeed(String.valueOf(0));
        clientVehicleInfo1.setBrand(monitorName);
        clientVehicleInfo1.setLatestGpsDate(new Date());
        clientVehicleList.add(clientVehicleInfo1);
        final Message msg = MsgUtil.getMsg(MessageType.BS_CLIENT_REQUEST_VEHICLE_CACHE_UP_INTO, clientVehicleList);
        webSocketMessageDispatchCenter.pushCacheStatusNew(msg);
        //????????????-??????????????????
        RedisHelper.delete(HistoryRedisKeyEnum.MONITOR_STATUS.of(monitorId));

        String offTime = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(monitorId, "82"));
        if (StringUtils.isNotBlank(offTime)) {
            String longTimeOfflineValueStr = JSON.parseObject(offTime).getString("longTimeOfflineValue");
            if (StringUtils.isNotBlank(longTimeOfflineValueStr)) {
                int longTimeOfflineValue = Integer.parseInt(longTimeOfflineValueStr);
                RedisHelper.setStringEx(HistoryRedisKeyEnum.MONITOR_OFFLINE.of(monitorId), monitorId,
                    longTimeOfflineValue * 60);
            }
        }
    }

    /**
     * ??????????????????
     */
    public void saveMedia(MultimediaData multimediaData) {
        try {
            MediaForm form = new MediaForm();
            BeanUtils.copyProperties(multimediaData, form);
            form.setVehicleId(multimediaData.getVid());
            form.setWayId(multimediaData.getWayId());
            form.setMediaId(multimediaData.getId());
            form.setLatitude(multimediaData.getGpsInfo().getLatitude() + "");
            form.setLongitude(multimediaData.getGpsInfo().getLongitude() + "");
            form.setSpeed(multimediaData.getGpsInfo().getSpeed() + "");
            form.setBrand(multimediaData.getMonitorName());
            mediaService.addMedia(form);
        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
    }

    /**
     * ??????????????????????????????
     */
    public void saveElectornicWayBillLog(Message message) {
        try {
            MsgDesc desc = message.getDesc();
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            JSONObject body = (JSONObject) t808Message.getMsgBody();
            String content = new String(body.getBytes("data"), Charset.forName("GBK"));
            String deviceNumber = desc.getDeviceNumber();
            // ????????????id????????????id
            VehicleDTO vehicle = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
            logAction(content, vehicle, "MONITORING", "??????????????????");
            // ??????
            JSONObject msgData = (JSONObject) message.getData();
            msgData.getJSONObject("msgBody").put("data", content);
            simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }

    private void logAction(String content, VehicleDTO vehicleDTO, String monitoring, String s) {
        LogSearchForm form = new LogSearchForm();
        // ????????????????????????????????????
        form.setGroupId(vehicleDTO.getDeviceOrgId());
        form.setEventDate(new Date());
        // ?????????????????????????????????
        form.setLogSource("1");
        form.setModule(monitoring);
        form.setMonitoringOperation("???????????????" + vehicleDTO.getName() + "???   " + s);
        form.setMessage(content);
        form.setPlateColor(vehicleDTO.getPlateColor());
        form.setBrand(vehicleDTO.getName());
        logSearchService.addLogBean(form);
    }

    /**
     * ?????????????????????????????????????????????
     */
    public void saveDriverIdentify(Message message) {
        MsgDesc desc = message.getDesc();
        String vehicleId = desc.getMonitorId();
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        JSONObject body = (JSONObject) t808Message.getMsgBody();

        //????????????ID??????CLBS??????ID??????(UUID)
        String faceId = body.getString("faceId");
        String cardNumber = body.getString("certificationId");
        Integer compareType = body.getInteger("compareType");
        Integer result = body.getInteger("result");

        String driverId = null;
        String driverStr = RedisHelper.hget(HistoryRedisKeyEnum.IC_DRIVER_LIST.of(), vehicleId);
        if (StringUtils.isNotEmpty(driverStr)) {
            JSONObject driver = JSONObject.parseObject(driverStr);
            String identity = driver.getString("driverIdentity");
            String certificationID = driver.getString("certificationID");
            String driverName = driver.getString("driverName");
            driverId = newProfessionalsDao
                .getIcCardDriverIdByIdentityAndName(identity == null ? certificationID : identity, driverName);
        }

        //??????????????????????????????????????????????????????faecId
        if (compareType == 0 && result == 0) {
            //????????????????????????????????????id
            newProfessionalsDao.updateFaceId(faceId, driverId);
            if (driverId != null) {
                RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(driverId), "faceId", faceId);
            }
        }

        //??????????????????????????????????????????
        DriverDiscernReportDo reportDo = new DriverDiscernReportDo();
        reportDo.setMonitorId(desc.getMonitorId());
        reportDo.setFaceId(faceId);
        reportDo.setDriverId(driverId);
        //???????????????????????????
        if (compareType == 2) {
            compareType = 4;
        }

        Integer identificationResult = result;
        if (identificationResult != 0) {
            identificationResult += 6;
        }
        reportDo.setIdentificationResult(identificationResult);
        reportDo.setMatchRate(body.getString("similarity"));
        reportDo.setIdentificationType(compareType);
        reportDo.setCardNumber(cardNumber);

        JSONObject gpsInfo = body.getJSONObject("gpsInfo");
        reportDo.setLatitude(gpsInfo.getString("latitude"));
        reportDo.setLongitude(gpsInfo.getString("longitude"));
        if (StringUtils.isNotBlank(gpsInfo.getString("time"))) {
            reportDo
                .setIdentificationTime(DateUtil.getStringToDate(gpsInfo.getString("time"), DateUtil.DATE_YYMMDDHHMMSS));
        }
        driverDiscernStatisticsDao.save(reportDo);

        //??????9208????????????
        AlarmSign alarmSign = JSON.parseObject(body.getJSONObject("alarmSign").toJSONString(), AlarmSign.class);
        adasSimpMessagingTemplateUtil.sendHeiFileStream9208(alarmSign, vehicleId, reportDo.getId(),
            PlatformInspectionServiceImpl.IDENTIFY_INSPECTION);

        //???????????????????????????????????????????????????????????????
        if (compareType == 1) {
            Date longToDate = DateUtil.getLongToDate(System.currentTimeMillis() - 3 * 60 * 1000);
            List<String> inspectionIds = platformInspectionDao.get0706Inspection(longToDate, vehicleId);
            if (!inspectionIds.isEmpty()) {
                platformInspectionDao.setInspectionResult(reportDo.getId(), inspectionIds);
            }

            //??????3???????????????????????????
            for (String inspectionId : inspectionIds) {
                trigger.cancelEvent(inspectionId);
            }
            //????????????
            sendIdentifyInspectionWs(vehicleId, result, faceId, driverStr);
        }

    }

    /**
     * ??????????????????????????????????????????
     * @param vehicleId
     * @param result
     * @param faceId
     */
    private void sendIdentifyInspectionWs(String vehicleId, Integer result, String faceId, String driverStr) {
        String inspectionSession = WsSessionManager.INSTANCE.getInspectionSession(vehicleId);
        List<String> sessionIds = new ArrayList<>();
        if (StringUtils.isEmpty(inspectionSession)) {
            return;
        }
        sessionIds = Arrays.asList(inspectionSession.split(","));
        int wsStatus = result;
        if (result == 0) {
            wsStatus = setWsStatus(faceId, driverStr, wsStatus);
        }

        for (String sessionId : sessionIds) {
            if (StringUtils.isBlank(sessionId)) {
                continue;
            }
            platformInspectionService
                .sendInspectionStatus(wsStatus, sessionId, vehicleId, PlatformInspectionServiceImpl.IDENTIFY_INSPECTION,
                    null);
        }
        WsSessionManager.INSTANCE.removeInspection(vehicleId);
    }

    private int setWsStatus(String faceId, String driverStr, int wsStatus) {
        //?????????????????????????????????
        if (StringUtils.isNotEmpty(driverStr)) {
            JSONObject driver = JSONObject.parseObject(driverStr);
            if (driver.getString("faceId") == null || !faceId.equals(driver.getString("faceId"))) {
                wsStatus = 4;
            }
        } else {
            wsStatus = 7;
        }
        return wsStatus;
    }

    /**
     * ?????????????????????????????????
     * @param message
     */
    public void savePlatformInspection(Message message) {
        MsgDesc desc = message.getDesc();
        String vehicleId = desc.getMonitorId();
        String deviceId = desc.getDeviceId();
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        JSONObject body = (JSONObject) t808Message.getMsgBody();
        AlarmSign alarmSign = JSON.parseObject(body.getJSONObject("alarmSign").toJSONString(), AlarmSign.class);
        Integer type = body.getInteger("type");
        //????????????????????????
        type = type + 1;
        Integer msgSnAck = body.getInteger("msgSnAck");
        LocationInfo locationInfo = JSON.parseObject(body.getJSONObject("gpsInfo").toJSONString(), LocationInfo.class);
        //????????????????????????
        SubscibeInfo subscibeInfo = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSnAck, deviceId);
        if (subscibeInfo == null) {
            return;
        }
        SubscibeInfoCache.getInstance().delTable(subscibeInfo);

        JSONArray gpsAttachInfoList = locationInfo.getGpsAttachInfoList();
        PlatformInspectionResultDO platformInspectionResultDO = new PlatformInspectionResultDO();
        for (Object o : gpsAttachInfoList) {
            Integer gpsAttachInfoID = ((JSONObject) o).getInteger("gpsAttachInfoID");
            if (type.equals(PlatformInspectionServiceImpl.VEHICLE_INSPECTION) && gpsAttachInfoID
                .equals(PlatformInspectionServiceImpl.VEHICLE_INSPECTION_INDEX)) {
                platformInspectionResultDO =
                    setInspectionResult(((JSONObject) o).getJSONObject("warningInfo"), locationInfo);
            }
            if (type.equals(PlatformInspectionServiceImpl.DRIVER_INSPECTION) && gpsAttachInfoID
                .equals(PlatformInspectionServiceImpl.DRIVER_INSPECTION_INDEX)) {
                platformInspectionResultDO =
                    setInspectionResult(((JSONObject) o).getJSONObject("warningInfo"), locationInfo);
            }
        }

        String driverStr = RedisHelper.hget(HistoryRedisKeyEnum.IC_DRIVER_LIST.of(), vehicleId);
        if (StringUtils.isNotEmpty(driverStr)) {
            JSONObject driver = JSONObject.parseObject(driverStr);
            String identity = driver.getString("driverIdentity");
            String cardNumber = driver.getString("certificationID");
            String name = driver.getString("driverName");
            platformInspectionResultDO.setDriverId(
                newProfessionalsDao.getIcCardDriverIdByIdentityAndName(identity == null ? cardNumber : identity, name));
        }

        platformInspectionResultDO.setInspectionType(type);
        platformInspectionResultDO.setVehicleId(vehicleId);
        platformInspectionResultDao.insert(platformInspectionResultDO);

        //??????????????????????????????
        platformInspectionDao.setInspectionResult(platformInspectionResultDO.getId(),
            Collections.singletonList(subscibeInfo.getSessionId()));

        //??????9208????????????
        adasSimpMessagingTemplateUtil
            .sendHeiFileStream9208(alarmSign, vehicleId, platformInspectionResultDO.getId(), type);

        //??????????????????
        String inspectionSession = WsSessionManager.INSTANCE.getInspectionSession(vehicleId + "_" + msgSnAck);
        if (inspectionSession != null) {
            platformInspectionService
                .sendInspectionStatus(2, inspectionSession.split("_")[0], vehicleId, type, desc.getMonitorName());
        }
        WsSessionManager.INSTANCE.removeInspection(vehicleId + "_" + msgSnAck);

    }

    private PlatformInspectionResultDO setInspectionResult(JSONObject re, LocationInfo locationInfo) {
        PlatformInspectionResultDO platformInspectionResultDO = new PlatformInspectionResultDO();
        platformInspectionResultDO.setAlarmType(re.getInteger("alarmType"));
        platformInspectionResultDO.setWarnType(re.getInteger("warnType"));
        platformInspectionResultDO.setRemindFlag(re.getInteger("isRemindDriver"));
        JSONObject attach = re.getJSONObject("attach");
        if (attach == null) {
            platformInspectionResultDO
                .setTime(DateUtil.getStringToDate(locationInfo.getUploadtime(), DateUtil.DATE_YYMMDDHHMMSS));
        } else {
            platformInspectionResultDO.setStatus(attach.getInteger("state"));
            String time = attach.getString("startTime");
            if (time != null) {
                platformInspectionResultDO.setTime(DateUtil.getStringToDate(time, DateUtil.DATE_YYMMDDHHMMSS));
            } else {
                platformInspectionResultDO
                    .setTime(DateUtil.getStringToDate(locationInfo.getUploadtime(), DateUtil.DATE_YYMMDDHHMMSS));
            }
            Integer lineNo = attach.getInteger("lineNo");

            platformInspectionResultDO.setRouteId(lineNo == null ? null : lineNo.toString());
            platformInspectionResultDO.setType(attach.getInteger("type"));
        }
        return platformInspectionResultDO;
    }

    /**
     * ???????????????????????????????????????
     */
    public void saveDriverInfoCollectionLog(Message message) {
        try {
            MsgDesc desc = message.getDesc();
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            JSONObject body = (JSONObject) t808Message.getMsgBody();
            String deviceNumber = desc.getDeviceNumber();
            // ????????????id????????????id
            VehicleDTO vehicle = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
            String vid = vehicle.getId();

            BindDTO bindDTO = MonitorUtils.getBindDTO(vid);
            String orgId = bindDTO.getOrgId();
            String orgName = bindDTO.getOrgName();
            StringBuilder content = new StringBuilder();
            String deviceType = vehicle.getDeviceType();
            // ???/????????????,???????????????
            String driverName = body.getString("driverName");
            if (StringUtils.isNotBlank(driverName) && driverName.length() > 20) {
                //??????????????????20
                log.error("???????????????????????????????????????, {}", JSON.toJSONString(message));
                return;
            }
            //?????????????????????
            String certificationID = body.getString("certificationID");
            // ????????????????????????
            String cardID = body.getString("driverIdentity");
            cardID = StringUtils.isBlank(cardID) ? certificationID : cardID;
            // ??????????????????
            String caName = body.getString("cAName");
            // ???????????????
            String expiryDate = body.getString("expiryDate");

            String icTypeId = newProfessionalsDao.getIcTypeId();
            boolean matches = false;
            if (expiryDate != null) {
                matches = expiryDatePattern.matcher(expiryDate).matches();
            }
            boolean isInsert = false;
            String cardIdAndName = "";
            if (StringUtils.isNotBlank(cardID) && StringUtils.isNotBlank(driverName)) {
                cardIdAndName = cardID + "_" + driverName;
                isInsert = true;
            }

            if (StringUtils.isNotEmpty(certificationID) && certificationID.length() > 40) {
                log.info(content.append("??????????????????").append(driverName).append("<br/>???????????????????????????").append("<br/>????????????????????????")
                    .append(certificationID).append("<br/>?????????????????????").append(caName) + "???????????????????????????");
                return;
            }

            Date date = null;
            String dateStr = "";
            if (matches) {
                DateFormat format = new SimpleDateFormat(DateUtil.DATE_YMD_FORMAT);
                date = format.parse(expiryDate);
                dateStr = DateFormatUtils.format(date, DateUtil.DATE_Y_M_D_FORMAT);
            }
            String time = body.getString("time");
            //????????????????????????????????????
            boolean isRightDriver = true;//isRightDriver(driverName, certificationID);
            // 11?????????
            if ("0".equals(deviceType) || Objects.equals(body.getInteger("check"), 2013)) {
                content.append("??????????????????").append(driverName).append("<br/>???????????????????????????").append(cardID)
                    .append("<br/>????????????????????????").append(certificationID).append("<br/>?????????????????????").append(caName);
            } else {
                Integer status = body.getInteger("status");
                // ???????????????
                if (status == 1) {
                    String icResultStr = body.getString("iCResult");
                    if (icResultStr != null) {
                        int icResult = Integer.parseInt(icResultStr);
                        if (icResult == 0) {
                            ProfessionalDO professionalDO = new ProfessionalDO();
                            ProfessionalDTO professionalDTO = new ProfessionalDTO();
                            ProfessionalDO pis = null;
                            //????????????????????????????????????
                            boolean saveFlag = true;
                            if (driverName == null || "".equals(driverName)) {

                                //?????????????????????????????????????????????
                                if (!isRightDriver
                                    || newProfessionalsDao.getIcProfessionalNum(certificationID, caName, icTypeId)
                                    > 0) {
                                    saveFlag = false;
                                } else {
                                    String name = newProfessionalsDao.getIcErrorName(icTypeId);
                                    int number;
                                    if (name != null) {
                                        number = Integer.parseInt(name.split("IC?????????")[1]);
                                    } else {
                                        number = 0;
                                    }
                                    driverName = "IC?????????" + (number + 1);
                                }
                            } else {
                                // ??????????????????????????????content?????????
                                pis = newProfessionalsDao.findByNameExistIdentity(driverName, cardID);
                            }
                            if (isRightDriver && saveFlag && pis == null && saveNewDriverOn0702) {
                                String id = UUID.randomUUID().toString();
                                professionalDTO.setId(id);
                                professionalDTO.setCardNumber(certificationID);
                                professionalDTO.setIdentity(cardID);
                                professionalDTO.setDrivingLicenseNo(certificationID);
                                professionalDTO.setName(driverName);
                                professionalDTO.setIcCardAgencies(caName);
                                professionalDTO.setIcCardEndDate(date);
                                //??????  ???????????????????????????????????????????????????
                                professionalDTO.setLockType(1);
                                professionalDTO.setType("?????????(IC???)");
                                professionalDTO.setPositionType(newProfessionalsDao.getIcTypeId());
                                professionalDTO.setOrgId(orgId);
                                if ((date == null || date.getTime() > System.currentTimeMillis()) && !driverName
                                    .startsWith("IC?????????")) {
                                    professionalDTO.setState("0");
                                } else {
                                    professionalDTO.setState("2");
                                }
                                // ????????????????????????
                                BeanUtils.copyProperties(professionalDTO, professionalDO);
                                professionalDO.setId(id);
                                professionalDO.setFlag(1);
                                professionalDO.setCreateDataTime(new Date());

                                boolean flag = newProfessionalsDao.addProfessionals(professionalDO);
                                RedisHelper.setString(HistoryRedisKeyEnum.IC_PROFESSIONAL_INFO.of(deviceNumber),
                                    JSON.toJSONString(professionalDTO), 180);
                                //?????????????????????zw_m_vehicle_card_num ???  redis 4??????
                                insertMysqlAndRedis(status, vid, cardID, driverName, time,
                                        professionalDO.getId(), isInsert);

                                //????????????
                                Map<String, String> valueMap = ProfessionalServiceImpl.setValueToMap(professionalDTO);
                                RedisHelper
                                    .addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalDTO.getId()), valueMap);
                                //????????????
                                RedisHelper
                                    .addToListTop(RedisKeyEnum.PROFESSIONAL_SORT_ID.of(), professionalDO.getId());
                                //??????????????????
                                String hashKey = ProfessionalServiceImpl
                                    .constructFuzzySearchKey(professionalDO.getName(), professionalDO.getIdentity(),
                                        professionalDO.getState());
                                RedisHelper
                                    .addToHash(RedisKeyEnum.FUZZY_PROFESSIONAL.of(), hashKey, professionalDO.getId());

                                //??????????????????redis??????
                                RedisHelper
                                    .addToSet(RedisKeyEnum.ORGANIZATION_PROFESSIONAL_ID.of(professionalDO.getOrgId()),
                                        professionalDO.getId());

                                content.append("??????????????????:").append(driverName).append("(").append(orgName).append(")")
                                    .append("<br/>???????????????????????????").append(cardID).append("<br/>????????????????????????")
                                    .append(certificationID).append("<br/>?????????????????????").append(caName);
                                // ????????????????????????????????????Storm
                                if (flag) {
                                    JSONObject stormParam = new JSONObject();
                                    stormParam.put("deviceId", desc.getDId());
                                    stormParam.put("certificationID", certificationID);
                                    ZMQFencePub.pubChangeParam(stormParam.toJSONString());
                                }
                            } else {
                                if (isRightDriver && pis != null) {
                                    //???????????????????????????????????????????????????
                                    if (pis.getLockType() == 1 && validateFields(pis, body, date)) {
                                        BeanUtils.copyProperties(pis, professionalDO);
                                    } else {
                                        //??????IC??????????????? ???????????????????????????????????????
                                        pis.setIdentity(cardID);
                                        pis.setDrivingLicenseNo(certificationID);
                                        pis.setCardNumber(certificationID);
                                        pis.setIcCardAgencies(caName);
                                        pis.setIcCardEndDate(date);
                                        pis.setLockType(1);
                                        pis.setUpdateDataTime(new Date());
                                        BeanUtils.copyProperties(pis, professionalDO);
                                        professionalDO.setUpdateDataTime(new Date());
                                        //????????????
                                        newProfessionalsDao.updateProfessionals(professionalDO);
                                        //?????????????????????????????????
                                        BeanUtils.copyProperties(professionalDO, professionalDTO);
                                        ProfessionalServiceImpl.updateProMapAndFuzzySearch(professionalDTO);
                                    }
                                    RedisHelper.setString(HistoryRedisKeyEnum.IC_PROFESSIONAL_INFO.of(deviceNumber),
                                        JSON.toJSONString(professionalDTO), 180);
                                    insertMysqlAndRedis(status, vid, cardID, driverName, time, professionalDO.getId(),
                                        isInsert);

                                }
                                content.append("?????????????????????<br/>");
                                content.append("???????????????").append(LocalDateUtils.dateTimeFormat(DateUtil.parseDate(time)))
                                    .append("<br/>");

                                getContent(content, driverName, certificationID, caName, dateStr);
                            }
                        } else if (icResult == 1) {
                            content.append("IC??????????????????????????????????????????????????????<br/>");
                        } else if (icResult == 2) {
                            content.append("IC?????????????????????????????????????????????<br/>");
                        } else if (icResult == 3) {
                            content.append("IC?????????????????????????????????????????????????????????<br/>");
                        } else if (icResult == 4) {
                            content.append("IC?????????????????????????????????????????????<br/>");
                        }
                    }
                    // ???????????????
                } else if (status == 2 && isRightDriver) {
                    content.append("???????????????").append("<br/>");
                    //??????redisVC??????   ????????????_???????????????
                    String s = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vid));
                    if (StringUtils.isNotEmpty(s)) {
                        String[] cardIdName = s.split(",")[0].split("_");
                        String pid =
                            newProfessionalsDao.getIcCardDriverIdByIdentityAndName(cardIdName[0], cardIdName[1]);
                        if (StringUtils.isNotBlank(s.split(",")[0])) {
                            cardIdAndName = s.split(",")[0];
                            isInsert = true;
                        }
                        //??????redis??????
                        final String[] arr = cardIdAndName.split("_");
                        String cardId = arr.length > 0 ? arr[0] : "";
                        String name = arr.length > 1 ? arr[1] : "";
                        insertMysqlAndRedis(status, vid, cardId, name, time, pid, isInsert);
                        content.append("??????????????????").append(cardIdName[1]).append("<br/>");
                        content.append("????????????????????????").append(cardIdName[0]).append("<br/>");
                    }
                    content.append("???????????????").append(LocalDateUtils.dateTimeFormat(DateUtil.parseDate(time)))
                        .append("<br/>");
                    // getContent(content, driverName, certificationID, caName, dateStr);
                }
            }
            logAction(content.toString(), vehicle, "", " ?????????????????????????????????");
            // ????????????
            String msgSNACK = desc.getMsgSNAck();
            String deviceId = desc.getDeviceId();
            SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSNACK, deviceId);
            if (Objects.nonNull(info)) {
                SubscibeInfoCache.getInstance().delTable(info);
            }
            // ??????
            simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
            ProfessionalsInfo professionalsInfo = new ProfessionalsInfo();
            professionalsInfo.setName(driverName);
            professionalsInfo.setCardNumber(certificationID);
            adasSubcibeTable.putFaceRecognitionCache(deviceNumber, professionalsInfo);
        } catch (Exception e) {
            log.error("?????????????????????????????????????????????", e);
        }
    }

    /**
     * ?????????????????????????????????????????????,
     */
    private boolean isRightDriver(String driverName, String certificationID) {
        return cardNumAndNamPattern.matcher(driverName).matches() && cardNumAndNamPattern.matcher(certificationID)
            .matches();
    }

    private void getContent(StringBuilder content, String driverName, String certificationID, String caName,
        String dateStr) {
        content.append("??????????????????").append(driverName).append("<br/>").append("?????????????????????").append(certificationID)
            .append("<br/>").append("?????????????????????").append(caName).append("<br/>").append("??????????????????").append(dateStr)
            .append("<br/>");
    }

    /**
     * ??????IC???????????? ?????????????????????????????????
     * @param pi   ?????????????????????
     * @param body IC???????????????
     * @param date ?????????
     * @return true OR false
     */
    private boolean validateFields(ProfessionalDO pi, JSONObject body, Date date) {
        boolean isDateEqual = Objects.equals(date, pi.getIcCardEndDate());
        String name = body.getString("cAName");
        String certificationID = body.getString("certificationID");
        // ????????????????????????
        String cardID = body.getString("driverIdentity") == null ? certificationID : body.getString("driverIdentity");
        boolean bool = name.equals(pi.getIcCardAgencies()) && cardID.equals(pi.getIdentity()) && certificationID
            .equals(pi.getCardNumber()) && pi.getLockType() == 1;
        return bool && isDateEqual;
    }

    /**
     * ?????? ?????? ?????? mysql  ??????redis ?????????????????????????????????
     *
     * @param vid        ??????id
     * @param cardId     ???????????????????????????
     * @param driverName ???????????????
     * @param time       ???????????????
     */
    private void insertMysqlAndRedis(Integer status, String vid, String cardId, String driverName, String time,
                                     String pid, boolean isInsert) {
        if (isInsert) {
            String year = time.substring(0, 2);
            String month = time.substring(2, 4);
            String day = time.substring(4, 6);
            String hour = time.substring(6, 8);
            String minute = time.substring(8, 10);
            String second = time.substring(10);
            time = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;
            long ts = 0L;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                Date date = sdf.parse(time);
                ts = date.getTime();
            } catch (ParseException e) {
                log.error("????????????????????????????????????????????????", e);
            }
            final String cardIdAndName = StringUtils.isNotBlank(cardId) && StringUtils.isNotBlank(driverName)
                    ? cardId + "_" + driverName : "";
            if (status == 1) {
                final String dbDriverName = ObjectUtils.defaultIfNull(driverName, "");
                final String identificationNumber = ObjectUtils.defaultIfNull(cardId, "");
                adasVehicleCardNumDao.insert(new VehicleIcHistoryDO(vid, dbDriverName, identificationNumber));
                if (StrUtil.isNotBlank(cardIdAndName) && StrUtil.isNotBlank(String.valueOf(ts))) {
                    //??????redis??????
                    String lastDriverValue = "c_" + cardIdAndName + ",t_" + ts;
                    RedisHelper.setString(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vid), cardIdAndName + "," + ts);
                    RedisHelper.setString(HistoryRedisKeyEnum.LAST_DRIVER.of(vid), lastDriverValue);
                    RedisHelper.setString(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(pid),
                        cardIdAndName + "," + ts + "," + vid);
                }
            } else if (status == 2 && StrUtil.isNotBlank(cardIdAndName) && StrUtil.isNotBlank(String.valueOf(ts))) {
                String lastDriverValue =
                    RedisHelper.getString(HistoryRedisKeyEnum.LAST_DRIVER.of(vid)) + ",c_" + cardIdAndName + ",t_" + ts;
                RedisHelper.setString(HistoryRedisKeyEnum.LAST_DRIVER.of(vid), lastDriverValue);
                RedisHelper.delete(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vid));
                RedisHelper.delete(HistoryRedisKeyEnum.CARD_NUM_PROFESSIONAL_PREFIX.of(pid));
            }
        }
    }

    /**
     * ??????????????????/??????????????????
     */
    public void saveInformationDemandOrCancelLog(Message message) {
        try {
            MsgDesc desc = message.getDesc();
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            JSONObject body = (JSONObject) t808Message.getMsgBody();
            String deviceNumber = desc.getDeviceNumber();
            Integer flag = body.getInteger("flag");
            String content = "";
            if (flag == 0) {
                content = "????????????";
            } else if (flag == 1) {
                content = "????????????";
            }
            // ????????????id????????????id
            VehicleDTO vehicle = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
            logAction(content, vehicle, "MONITORING", "????????????/??????");
            // ??????
            simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);

            // ??????8304: ??????????????????
            Integer infoType = body.getInteger("infoType");
            String vehicleId = vehicle.getId();
            if (flag == 1) {
                // ?????????????????????, ??????????????????
                MonitorCommandBindForm monitorCommandBindForm = realTimeCommandDao.getRealTimeCommand(vehicleId, 21);
                if (Objects.nonNull(monitorCommandBindForm)) {
                    String paramId = monitorCommandBindForm.getParamId();
                    List<String> paramIdList = Arrays.asList(paramId.split(","));
                    InformationParam informationParam =
                        realTimeCommandDao.getInformationParamsByParamId(paramIdList, infoType);

                    if (Objects.nonNull(informationParam)) {
                        sendMessageMenu(vehicleId, informationParam);
                    }
                }
            } else {
                // ??????????????????
                MessageMenuSendTimer.remove(vehicleId, infoType);
            }
        } catch (Exception e) {
            log.error("??????????????????/????????????????????????", e);
        }
    }

    /**
     * ??????????????????????????????
     * @param vehicleId        vehicleId
     * @param informationParam informationParam
     */
    private void sendMessageMenu(String vehicleId, InformationParam informationParam) {
        final RedisKey key = RedisKeyEnum.MONITOR_INFO.of(vehicleId);
        final Map<String, String> monitorConfig =
                RedisHelper.getHashMap(key, "deviceId", "deviceNumber", "simCardNumber", "deviceType");
        if (MapUtils.isNotEmpty(monitorConfig)) {
            String deviceNumber = monitorConfig.get("deviceNumber");
            String deviceId = monitorConfig.get("deviceId");
            String simcardNumber = monitorConfig.get("simCardNumber");
            String deviceType = String.valueOf(monitorConfig.get("deviceType"));
            InformationParamInfo informationParamInfo = new InformationParamInfo();
            informationParamInfo.setType(informationParam.getInfoId());
            String messageContent = informationParam.getMessageContent();
            if (StringUtils.isNotEmpty(messageContent)) {
                byte[] gbks = messageContent.getBytes(Charset.forName("GBK"));
                informationParamInfo.setLen(gbks.length);
                informationParamInfo.setValue(messageContent);
            } else {
                informationParamInfo.setLen(0);
                informationParamInfo.setValue("");
            }
            informationParamInfo.setDeviceNumber(deviceNumber);
            informationParamInfo.setDeviceId(deviceId);
            informationParamInfo.setSimcardNumber(simcardNumber);
            informationParamInfo.setSendFrequency(informationParam.getSendFrequency());
            informationParamInfo.setVehicleId(vehicleId);
            informationParamInfo.setDeviceType(deviceType);

            Integer registerDevice = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
            if (null != registerDevice) {
                SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, registerDevice,
                    ConstantUtil.T808_DEVICE_GE_ACK);
                SubscibeInfoCache.getInstance().putTable(info);

                T808Message infomation808Message = MsgUtil
                    .get808Message(simcardNumber, ConstantUtil.T808_INFO_MSG, registerDevice, informationParamInfo,
                        deviceType);
                WebSubscribeManager.getInstance()
                    .sendMsgToAll(infomation808Message, ConstantUtil.T808_INFO_MSG, deviceId);
            }
            // TODO ???????????????: ????????????????????????(???: ?????????????????????, ???????????????)
            if (modeCheck) {
                MessageMenuSendTimer.put(vehicleId, informationParam.getInfoId(), informationParamInfo);
            }
        }
    }

    /**
     * ??????????????????????????????
     */
    public void saveQuestionResponseLog(Message message) {
        try {
            MsgDesc desc = message.getDesc();
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            JSONObject body = (JSONObject) t808Message.getMsgBody();
            String deviceNumber = desc.getDeviceNumber();
            String content =
                "??????????????????" + body.getInteger("msgSNAck") + "<br/> ??????ID:" + body.getInteger("answerID") + "<br/>";

            // ????????????id????????????id
            VehicleDTO vehicle = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
            logAction(content, vehicle, "MONITORING", "??????????????????");

            // ??????
            simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }

    /**
     * ????????????????????????
     */
    public void saveEventReportLog(Message message) {
        try {
            MsgDesc desc = message.getDesc();
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            JSONObject body = (JSONObject) t808Message.getMsgBody();
            String deviceNumber = desc.getDeviceNumber();
            String content = "??????ID???" + body.getInteger("eventID") + "<br/>";

            // ????????????id????????????id
            VehicleDTO vehicle = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
            logAction(content, vehicle, "MONITORING", "????????????");

            // ??????
            simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
        } catch (Exception e) {
            log.error("??????????????????????????????", e);
        }
    }

    /**
     * ???????????????????????? ID???0x0108
     */
    public void saveUploadAckLog(Message message) {
        try {
            MsgDesc desc = message.getDesc();
            T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
            JSONObject body = (JSONObject) t808Message.getMsgBody();
            String deviceNumber = desc.getDeviceNumber();
            // ????????????id????????????id
            VehicleDTO vehicle = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
            String result = body.getString("result");
            String text = "????????????-??????-????????????:" + (Objects.equals(result, "0") ? "??????" : "??????");
            text += "<br/>????????????:";
            if (Objects.equals(result, "0") || Objects.equals(result, "1")) {
                text += "????????????";
            } else {
                text += "????????????";
            }
            logAction(text, vehicle, "OILSETTING", "??? ??????????????????");
            String ackMSN = message.getDesc().getMsgSNAck();
            if (StringUtils.isBlank(ackMSN)) {
                //????????????2019 ???????????????
                simpMessagingTemplateUtil.sendRemoteUpgrade(message, vehicle.getId());
                return;
            }
            parameterDao.updateStatusByMsgSN(Integer.parseInt(ackMSN), vehicle.getId(), Integer.valueOf(result));
            // ??????
            simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
        } catch (Exception e) {
            log.error("???????????????????????? ID???0x0108??????", e);
        }
    }

    /**
     * ??????????????????
     */
    public void acceptMediaSteam(RtpMessage rtpMessage) {
        RtpData rtpData = (JSON.parseObject(JSON.toJSONString(rtpMessage.getMsgBody()), RtpData.class));
        if (rtpData != null) {
            Date startTime = null;
            Date endTime = null;
            Integer endFlag = 0;
            String vehicleId = null;
            Integer channelNumber = null;
            try {
                endTime = DateUtil.parseDate(rtpData.getEndTime());
                endFlag = rtpData.getEndFlag();
                vehicleId = rtpData.getVehicleId();
                channelNumber = rtpData.getChannelNumber();
                String key = vehicleId + ":" + channelNumber;
                String stTime = WebSubscribeManager.getInstance().getFlowStartTime(key);
                if (StringUtils.isNotBlank(stTime)) { // ??????????????????????????????????????????????????????
                    startTime = DateUtil.parseDate(stTime);
                } else { // ?????????????????????????????????????????????
                    startTime = DateUtil.parseDate(rtpData.getStartTime());
                }
                // ??????????????????????????????
                WebSubscribeManager.getInstance().setFlowStartTime(key, rtpData.getEndTime());
                if (endFlag != null && endFlag == 1) {
                    // ????????????????????????????????????
                    WebSubscribeManager.getInstance().removeFlowStartTime(key);
                }
            } catch (ParseException e) {
                log.error("???????????????????????????", e);
            }
            if (channelNumber == null || endFlag == null) {
                return;
            }
            long videoSize = rtpData.getUse();
            VideoTrafficInfo info =
                VideoTrafficInfo.builder().vehicleId(vehicleId).channel(channelNumber).startTime(startTime)
                    .endTime(endTime).flowValue(rtpData.getUse()).stopFlag(endFlag).build();
            videoFlowDao.insert(info);
            log.info(String.format("????????????????????? %d bytes", videoSize));
        }
    }

    public void monitorVideoFlowConsumption(RtpMessage rtpMessage) {
        VideoPlayMsg videoPlayMsg = (JSON.parseObject(JSON.toJSONString(rtpMessage.getMsgBody()), VideoPlayMsg.class));
        String vehicleId = videoPlayMsg.getVehicleId();
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "name", "plateColor");
        if (bindDTO == null || StringUtils.isBlank(bindDTO.getName())) {
            return;
        }
        Integer channelNumber = videoPlayMsg.getChannelNumber();
        Date startTime = new Date(Long.parseLong(videoPlayMsg.getStartTime()));
        Date endTime = new Date(Long.parseLong(videoPlayMsg.getEndTime()));
        Integer videoSize = videoPlayMsg.getRecvBytes();
        VideoTrafficInfo info =
            VideoTrafficInfo.builder().vehicleId(vehicleId).channel(channelNumber).startTime(startTime).endTime(endTime)
                .flowValue(videoSize).stopFlag(1).build();
        videoFlowDao.insert(info);

        String carLicense = bindDTO.getName();
        String plateColor = Objects.isNull(bindDTO.getPlateColor()) ? null : String.valueOf(bindDTO.getPlateColor());
        List<UserDTO> userList = userService.getUserListByUuids(Collections.singleton(videoPlayMsg.getUserID()));
        String userName = CollectionUtils.isNotEmpty(userList) ? userList.get(0).getUsername() : null;
        String playType = videoPlayMsg.getPlayType();
        if (Objects.equals(playType, "0")) {
            String message = "????????????(" + carLicense + ")???????????????(" + channelNumber + ")??????";
            logSearchService.addLogByUserName(null, message, "3", "REALTIMEVIDEO", userName, carLicense, plateColor);
            return;
        }
        if (Objects.equals(playType, "1")) {
            String message = "????????????(" + carLicense + ")????????????????????????(????????????)";
            logSearchService.addLogByUserName(null, message, "3", "", userName, carLicense, plateColor);
        }
    }

    /**
     * ????????????????????????
     * @param rtpMessage
     */
    public void closeUserVideoPlayLog(RtpMessage rtpMessage) {
        VideoPlayMsg videoPlayMsg = (JSON.parseObject(JSON.toJSONString(rtpMessage.getMsgBody()), VideoPlayMsg.class));
        String vehicleId = videoPlayMsg.getVehicleId();
        BindDTO configInfo = MonitorUtils.getBindDTO(vehicleId, "name", "plateColor");
        if (configInfo == null) {
            return;
        }
        String carLicense = configInfo.getName();
        String plateColor = String.valueOf(configInfo.getPlateColor());
        String playType = videoPlayMsg.getPlayType();
        Integer channelNumber = videoPlayMsg.getChannelNumber();
        List<UserDTO> userList = userService.getUserListByUuids(Collections.singleton(videoPlayMsg.getUserID()));
        String userName = CollectionUtils.isNotEmpty(userList) ? userList.get(0).getUsername() : null;
        if (Objects.equals(playType, "0")) {
            String message = "????????????(" + carLicense + ")???????????????(" + channelNumber + ")??????(????????????)";
            logSearchService.addLogByUserName(null, message, "3", "REALTIMEVIDEO", userName, carLicense, plateColor);
            return;
        }
        if (Objects.equals(playType, "1")) {
            String message = "????????????(" + carLicense + ")????????????????????????(??????????????????)";
            logSearchService.addLogByUserName(null, message, "3", "", userName, carLicense, plateColor);
        }
    }

    public void saveUserVideoPlayLog(RtpMessage rtpMessage) {
        VideoPlayMsg videoPlayMsg = (JSON.parseObject(JSON.toJSONString(rtpMessage.getMsgBody()), VideoPlayMsg.class));
        String vehicleId = videoPlayMsg.getVehicleId();
        BindDTO configInfo = MonitorUtils.getBindDTO(vehicleId, "name", "plateColor");
        if (configInfo == null) {
            return;
        }
        String carLicense = configInfo.getName();
        String plateColor = String.valueOf(configInfo.getPlateColor());
        String playType = videoPlayMsg.getPlayType();
        Integer channelNumber = videoPlayMsg.getChannelNumber();
        List<UserDTO> userList = userService.getUserListByUuids(Collections.singleton(videoPlayMsg.getUserID()));
        String userName = CollectionUtils.isNotEmpty(userList) ? userList.get(0).getUsername() : null;
        if (Objects.equals(playType, "0")) {
            String message = "???????????? (" + carLicense + ")_????????? " + channelNumber + " ??????????????????";
            logSearchService.addLogByUserName(null, message, "3", "REALTIMEVIDEO", userName, carLicense, plateColor);
            return;
        }
        if (Objects.equals(playType, "1")) {
            String message = "????????????(" + carLicense + ") ??????????????????????????????????????????";
            logSearchService.addLogByUserName(null, message, "3", "", userName, carLicense, plateColor);
        }
    }

    public void sendftpDisk(RtpMessage message, DiskInfo disk) {
        JSONObject msgBody = (JSONObject) message.getMsgBody();
        Long total = msgBody.getLong("total");
        Long free = msgBody.getLong("free");
        int memory = 0;
        if (total != null && total != 0) {
            memory = (int) (((total - free) * 100 / total));
        }
        disk.setMemory(memory);
        // ??????redis???
        RedisHelper.setString(HistoryRedisKeyEnum.VIDEO_DISKINFO.of(), JSON.toJSONString(disk));
        webSocketMessageDispatchCenter.pushFtpDiskToClient(JSON.toJSONString(disk));// ?????????web???
    }

    /**
     * ?????????????????????????????????
     */
    public void saveMultimediaDataSearchLog(Message message) {
        JSONObject json = JSON.parseObject(JSON.toJSONString(message));
        JSONObject body = json.getJSONObject("data").getJSONObject("msgBody");
        JSONObject desc = json.getJSONObject("desc");
        String deviceNumber = desc.getString("deviceNumber");
        JSONArray searchDatas = body.getJSONArray("searchDatas");
        StringBuilder content = new StringBuilder();
        Integer msgSN = body.getInteger("msgSN");
        Integer sum = body.getInteger("sum");
        Integer subNum = body.getInteger("subNum");
        content.append("?????????????????? ").append(msgSN).append("<br/>????????????????????????: ").append(sum).append("<br/>?????????: ")
            .append(subNum != null ? subNum : "").append("<br/>");
        if (searchDatas != null && searchDatas.size() > 0) {
            for (int i = 0; i < searchDatas.size(); i++) {
                JSONObject searchData = searchDatas.getJSONObject(i);
                if (MapUtils.isEmpty(searchData)) {
                    continue;
                }
                int id = searchData.getInteger("ID");
                int wayId = searchData.getInteger("wayID");
                int type = searchData.getInteger("type");
                int eventCode = searchData.getInteger("eventCode");
                JSONObject gpsInfo = searchData.getJSONObject("gpsInfo");
                String gpsTime = gpsInfo.getString("time");
                Long gpsLatitude = gpsInfo.getLong("latitude");
                Long gpsLongitude = gpsInfo.getLong("longitude");
                Integer direction = gpsInfo.getInteger("direction");
                Long alarm = gpsInfo.getLong("alarm");
                Integer gpsAltitude = gpsInfo.getInteger("altitude");
                Integer gpsSpeed = gpsInfo.getInteger("speed");
                Long status = gpsInfo.getLong("status");
                content.append("?????????ID:").append(id).append("<br/> ??????ID???").append(wayId).append("<br/>");
                // ???????????????
                if (type == 0) {
                    content.append("????????????????????????<br/>");
                } else if (type == 1) {
                    content.append("????????????????????????<br/>");
                } else if (type == 2) {
                    content.append("????????????????????????<br/>");
                }
                // ???????????????
                if (eventCode == 0) {
                    content.append("????????????????????????????????????<br/>");
                } else if (eventCode == 1) {
                    content.append("??????????????????????????????<br/>");
                } else if (eventCode == 2) {
                    content.append("????????????????????????????????????<br/>");
                } else if (eventCode == 3) {
                    content.append("?????????????????????????????? ????????????<br/>");
                }
                // ????????????
                content.append("???????????????").append(getAlarmSign(alarm)).append("<br/>?????????").append(getStatus(status))
                    .append("<br/>?????? ??? ").append(gpsLatitude / 1000000.0).append("<br/> ?????????")
                    .append(gpsLongitude / 1000000.0).append("<br/>?????????").append(gpsAltitude).append("m<br/>?????????")
                    .append(gpsSpeed / 10).append("km/h<br/>?????????").append(getDirectionStr(direction)).append("<br/>?????????")
                    .append(transformDate(gpsTime)).append("<br/><br/>");
            }
        }
        // ????????????id????????????id
        VehicleDTO vehicle = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
        LogSearchForm form = new LogSearchForm();
        form.setGroupId(vehicle.getDeviceOrgId());
        form.setEventDate(new Date());
        //?????????????????????????????????
        form.setLogSource("1");
        form.setModule("MONITORING");
        form.setMonitoringOperation("???????????????" + vehicle.getName() + "????????????????????????????????????");
        form.setMessage(content.toString());
        logSearchService.addLogBean(form);

        webSocketMessageDispatchCenter.pushDeviceReportLogToClient(json.toJSONString());
    }

    /**
     * ????????????????????????????????????
     */
    private String getAlarmSign(Long alarmSign) {
        String alarmStr = "";
        if (alarmSign != null) {
            if (alarmSign == 0) {
                alarmStr += "?????????,";
            }
            if ((alarmSign & 0x01) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x02) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x04) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x08) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x10) != 0) {
                alarmStr += "GNSS??????????????????,";
            }
            if ((alarmSign & 0x20) != 0) {
                alarmStr += "GNSS????????????????????????,";
            }
            if ((alarmSign & 0x40) != 0) {
                alarmStr += "GNSS????????????,";
            }
            if ((alarmSign & 0x80) != 0) {
                alarmStr += "?????????????????????,";
            }
            if ((alarmSign & 0x100) != 0) {
                alarmStr += "?????????????????????,";
            }
            if ((alarmSign & 0x200) != 0) {
                alarmStr += "??????LCD??????????????????,";
            }
            if ((alarmSign & 0x400) != 0) {
                alarmStr += "TTS????????????,";
            }
            if ((alarmSign & 0x800) != 0) {
                alarmStr += "???????????????,";
            }
            if ((alarmSign & 0x1000) != 0) {
                alarmStr += "???????????????IC???????????????,";
            }
            if ((alarmSign & 0x2000) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x4000) != 0) {
                alarmStr += "??????????????????,";
            }
            if ((alarmSign & 0x40000) != 0) {
                alarmStr += "????????????????????????,";
            }
            if ((alarmSign & 0x80000) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x100000) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x200000) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x400000) != 0) {
                alarmStr += "????????????????????????/??????,";
            }
            if ((alarmSign & 0x800000) != 0) {
                alarmStr += "??????????????????,";
            }
            if ((alarmSign & 0x1000000) != 0) {
                alarmStr += "??????VSS??????,";
            }
            if ((alarmSign & 0x2000000) != 0) {
                alarmStr += "??????????????????,";
            }
            if ((alarmSign & 0x4000000) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x8000000) != 0) {
                alarmStr += "??????????????????,";
            }
            if ((alarmSign & 0x10000000) != 0) {
                alarmStr += "??????????????????,";
            }
            if ((alarmSign & 0x20000000) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x40000000) != 0) {
                alarmStr += "????????????,";
            }
            if ((alarmSign & 0x80000000) != 0) {
                alarmStr += "??????????????????,";
            }
            if (!"".equals(alarmStr)) {
                alarmStr = alarmStr.substring(0, alarmStr.length() - 1);
            }
        }

        return alarmStr;
    }

    /**
     * ??????????????????????????????
     */
    private String getStatus(Long status) {
        String statusStr = "";
        if (status != null) {
            if ((status & 0x01) != 0) {
                statusStr += "ACC ???,";
            } else {
                statusStr += "ACC ???,";
            }
            if ((status & 0x02) != 0) {
                statusStr += "??????,";
            } else {
                statusStr += "?????????,";
            }
            if ((status & 0x04) != 0) {
                statusStr += "??????,";
            } else {
                statusStr += "??????,";
            }
            if ((status & 0x08) != 0) {
                statusStr += "??????,";
            } else {
                statusStr += "??????,";
            }
            if ((status & 0x10) != 0) {
                statusStr += "????????????,";
            } else {
                statusStr += "????????????,";
            }
            if ((status & 0x20) != 0) {
                statusStr += "?????????????????????????????????,";
            } else {
                statusStr += "?????????????????????????????????,";
            }
            if ((status & 0x400) != 0) {
                statusStr += "??????????????????,";
            } else {
                statusStr += "??????????????????,";
            }
            if ((status & 0x800) != 0) {
                statusStr += "??????????????????,";
            } else {
                statusStr += "??????????????????,";
            }
            if ((status & 0x1000) != 0) {
                statusStr += "????????????,";
            } else {
                statusStr += "????????????,";
            }
            statusStr = statusStr.substring(0, statusStr.length() - 1);
        }
        return statusStr;
    }

    private String transformDate(String date) {
        String time = "";
        if (StringUtils.isNotBlank(date)) {
            String reg = "(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})";
            time = "20" + date.replaceAll(reg, "$1-$2-$3 $4:$5:$6");
        }
        return time;
    }

    /**
     * ????????????????????????
     */
    public void dealFenceQueryResp(Message message) {
        try {
            MsgDesc desc = message.getDesc();
            String deviceNumber = desc.getDeviceNumber();

            LogSearchForm form = new LogSearchForm();
            // ????????????id????????????id
            VehicleDTO vehicle = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
            form.setGroupId(vehicle.getDeviceOrgId());
            form.setEventDate(new Date());
            // ?????????????????????????????????
            form.setLogSource("1");
            form.setModule("MONITORING");
            form.setMonitoringOperation("???????????????" + vehicle.getName() + "???   ??????????????????");
            form.setPlateColor(vehicle.getPlateColor());
            form.setBrand(vehicle.getName());
            logSearchService.addLogBean(form);
        } catch (Exception e) {
            log.error("????????????????????????????????????", e);
        }
    }
}
