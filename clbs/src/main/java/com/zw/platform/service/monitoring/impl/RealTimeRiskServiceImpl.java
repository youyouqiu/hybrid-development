package com.zw.platform.service.monitoring.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.cb.platform.repository.mysqlDao.SpotCheckReportDao;
import com.zw.adas.domain.riskManagement.AdasAlarmDealInfo;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.alarm.AlarmInfo;
import com.zw.platform.domain.basicinfo.form.ProfessionalsForm;
import com.zw.platform.domain.connectionparamsset_809.AlarmHandleParam;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.multimedia.HandleAlarms;
import com.zw.platform.domain.multimedia.HandleMultiAlarms;
import com.zw.platform.domain.multimedia.form.OrderForm;
import com.zw.platform.domain.param.TelBack;
import com.zw.platform.domain.sendTxt.AlarmAck;
import com.zw.platform.push.factory.AlarmFactory;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.vas.RiskCampaignDao;
import com.zw.platform.service.alarm.AlarmSearchService;
import com.zw.platform.service.basicinfo.ProfessionalsService;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsSetService;
import com.zw.platform.service.monitoring.RealTimeRiskService;
import com.zw.platform.service.multimedia.MultimediaService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.reportManagement.impl.LogSearchServiceImpl;
import com.zw.platform.service.sendTxt.SendTxtService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Customer;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RealTimeRiskServiceImpl implements RealTimeRiskService {

    private static final String VIDEO_MODULE = "REALTIMEVIDEO";
    private static final String MONITORING_MODULE = "MONITORING";
    private static Logger log = LogManager.getLogger(RealTimeRiskServiceImpl.class);
    /**
     * ??????????????????(??????????????????,????????????????????????)
     */
    public final String types = "0,3,20,21,22,2011,2012,2111,2112,2211,2212,27,28,31,157";
    public final List<String> instantaneousAlarmType = Arrays.asList(types.split(","));
    @Autowired
    RiskCampaignDao riskCampaignDao;
    @Autowired
    ProfessionalsService professionalsService;
    @Autowired
    AdasElasticSearchUtil adasElasticSearchUtil;
    @Autowired
    AlarmSearchService alarmSearchService;
    @Autowired
    MultimediaService multiService;
    @Autowired
    SendTxtService sendService;
    @Autowired
    AlarmFactory alarmFactory;
    @Autowired
    LogSearchServiceImpl logSearchServiceImpl;
    @Autowired
    LogSearchService logSearchService;
    @Resource
    private HttpServletRequest request;
    @Autowired
    private ConnectionParamsSetService connectionParamsSetService;
    @Autowired
    private AdasRiskService adasRiskService;
    @Autowired
    private MultimediaService multimediaService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private SpotCheckReportDao spotCheckReportDao;
    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;
    @Value("${adas.mediaServer}")
    private String mediaServer;
    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Override
    public List<ProfessionalsForm> getRiskProfessionalsInfo(String vehicleId) throws Exception {
        BindDTO config = MonitorUtils.getBindDTO(vehicleId, "id", "professionalIds");
        if (config == null) {
            return null;
        }
        List<ProfessionalsForm> professionalsForms = new ArrayList<>();
        String[] professionalIds;
        String professionals = "";
        if (config.getProfessionalIds() != null) {
            professionals = config.getProfessionalIds();
        }
        if (sslEnabled) {
            mediaServer = "/mediaserver";
        }

        //???????????????????????????
        String insertCardInfo = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vehicleId));
        if (StrUtil.isNotBlank(insertCardInfo)) {
            //????????????
            String insertCardId = insertCardInfo.split(",")[0];
            String insertCardDriverId = professionalsService.getIcCardDriverIdByIdentityAndName(insertCardId);
            if (StrUtil.isNotBlank(insertCardDriverId) && !professionals.contains(insertCardDriverId)) {
                professionals += "," + insertCardDriverId;
            }
        }
        professionalIds = professionals.split(",");
        for (String professionalId : professionalIds) {
            Map<String, Object> resultMap = professionalsService.findProGroupById(professionalId);
            if (resultMap == null) {
                continue;
            }
            String groupId = (String) resultMap.get("groupName");
            OrganizationLdap organization = organizationService.getOrganizationByUuid(groupId);
            //????????????id???????????????????????????????????????????????????resultMap?????????????????????????????????????????????
            //resultMap.put("groupName", organization.getName());
            // ??????????????????
            ProfessionalsForm form = new ProfessionalsForm();
            ConvertUtils.register(form, Date.class);
            BeanUtils.populate(form, resultMap);
            form.setGroupId(groupId);
            form.setGroupName(organization.getName());
            if (form.getIcCardEndDate() != null) {
                String icCardEndDateStr = DateUtil.getDateToString(form.getIcCardEndDate(), DateUtil.DATE_Y_M_D_FORMAT);
                form.setIcCardEndDateStr(icCardEndDateStr);
            }
            if (StringUtils.isNotEmpty(form.getPhotograph())) {
                form.setPhotograph(mediaServer + professionalFtpPath + form.getPhotograph());
            }
            if (form.getType() == null || (form.getType() != null && !"?????????(IC???)".equals(form.getType()))) {
                professionalsForms.add(form);
            } else {
                professionalsForms.add(0, form);
            }
        }
        return professionalsForms;
    }

    @Override
    public void sendHandleAlarmsAndPhoto(@ModelAttribute("form") OrderForm form) throws Exception {
        HandleAlarms handleAlarms = new HandleAlarms();
        handleAlarms.setDevice(form.getDevice());
        handleAlarms.setHandleType(form.getHandleType());
        handleAlarms.setSimcard(form.getSimcard());
        handleAlarms.setSno(form.getSno());
        handleAlarms.setVehicleId(form.getVid());
        handleAlarms.setPlateNumber(form.getPlateNumber());
        handleAlarms.setStartTime(form.getStartTime());
        handleAlarms.setAlarm(form.getAlarm());
        handleAlarms.setRemark(form.getRemark());
        handleAlarms.setDescription(form.getDescription());
        handleAlarms.setRiskEventId(form.getRiskEventId());
        handleAlarms.setRiskId(form.getRiskId());
        //??????????????????
        handleAlarms.setDealOfMsg(form.getTxt());
        handleAlarms.setIsAutoDeal(form.getIsAutoDeal());
        if (form.getIsAdas() != null) {
            handleAlarms.setIsAdas(form.getIsAdas());
        }
        saveCommonHandleAlarms(handleAlarms);
    }

    @Override
    public void saveCommonHandleAlarms(HandleAlarms handleAlarms) throws Exception {
        //1?????????????????????adas??????
        if (handleAlarms.getIsAdas() == 1) {
            handleAdasAlarmInfo(handleAlarms);
        } else {
            handleAlarmInfo(handleAlarms, true);
        }
        addHandleAlarmsLog(handleAlarms);
    }

    @SneakyThrows
    private void handleAlarmInfo(HandleAlarms handleAlarms, boolean sendPaasNow) {
        String vehicleId = handleAlarms.getVehicleId();
        String alarm = handleAlarms.getAlarm();
        String handleType = handleAlarms.getHandleType();
        String startTimeStr = handleAlarms.getStartTime();
        if (StringUtils.isBlank(startTimeStr)) {
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            startTimeStr = "1990-01-01 01:01:01";
        }
        long alarmStartTimeL = DateUtil.getStringToLong(startTimeStr, "yyyy-MM-dd HH:mm:ss");

        String[] alarmTypes = alarm.split(",");
        assert alarmTypes.length >= 1;
        String alarmType = alarmTypes[0];
        byte[] vidArr = UuidUtils.getBytesFromStr(vehicleId);
        int alarmTypeInt = Integer.parseInt(alarmType);
        int alarmInt = alarmTypeInt;
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        if (Objects.isNull(bindDTO)) {
            log.error("???????????????????????????????????????????????????ID???{}", vehicleId);
            return;
        }
        if ("??????????????????".equals(handleType) && instantaneousAlarmType.contains(alarmType)) {
            alarmInt = getAlarmInt(alarmTypeInt);
            manualConfirmationAlarm(handleAlarms, bindDTO, alarmInt);
        }
        if ("??????".equals(handleType)) {
            sendMonitor(handleAlarms, bindDTO);
        }

        String riskEventId = handleAlarms.getRiskEventId();

        //??????809??????????????????????????????????????????
        AlarmHandleParam handleParam =
            AlarmHandleParam.getInstance(alarmInt, vehicleId, alarmStartTimeL, handleType, null, riskEventId, null);
        handleParam.setIsAutoDeal(handleAlarms.getIsAutoDeal());

        //?????????????????????????????????????????????es???????????????
        if (StrUtil.isNotBlank(riskEventId) && !"null".equals(riskEventId)) {
            dealSecurityRisk(handleAlarms);
            // ?????????????????????????????????????????????
            connectionParamsSetService.initiativeSendAlarmHandle(handleParam);
        } else {
            if (Objects.equals(bindDTO.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
                saveAlarmSpotCheck(vehicleId, alarmStartTimeL, alarmType);
            }

            // ??????????????????
            if (sendPaasNow) {
                // ????????????
                long alarmStartTimeOld =
                    StringUtils.isNotBlank(startTimeStr) ? DateUtil.getStringToLong(startTimeStr, null) : 0L;
                // ???????????????
                String alarmStartTimeStr = handleAlarms.getAlarmStartTimeStr();
                long alarmStartTimeLong = StringUtils.isNotBlank(alarmStartTimeStr)
                    ? DateUtil.getStringToLong(alarmStartTimeStr, DateUtil.DATE_FORMAT_SSS) : alarmStartTimeOld;
                alarmSearchService.handleAlarmSingle(handleAlarms, alarmStartTimeLong, alarmType, true);
            }
            alarmFactory.dealAlarm(handleAlarms);
        }
    }

    private void handleAdasAlarmInfo(HandleAlarms handleAlarms) throws Exception {
        String vehicleId = handleAlarms.getVehicleId();
        String alarm = handleAlarms.getAlarm();
        String handleType = handleAlarms.getHandleType();

        String[] alarmTypes = alarm.split(",");
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        for (String alarmType : alarmTypes) {
            int alarmTypeInt = Integer.parseInt(alarmType);
            int alarmInt;
            if ("??????????????????".equals(handleType) && instantaneousAlarmType.contains(alarmType)) {
                alarmInt = getAlarmInt(alarmTypeInt);
                manualConfirmationAlarm(handleAlarms, bindDTO, alarmInt);
            }
            if ("??????".equals(handleType)) {
                sendMonitor(handleAlarms, bindDTO);
            }

            //??????809??????????????????????????????????????????
            AlarmHandleParam handleParam =
                AlarmHandleParam.getInstance(vehicleId, handleType, handleAlarms.getRiskId());

            // ?????????????????????????????????????????????
            connectionParamsSetService.initiativeSendAlarmHandle(handleParam);

            //?????????????????????????????????????????????es???????????????
            if (StrUtil.isNotBlank(handleAlarms.getRiskId()) && !"null".equals(handleAlarms.getRiskId())) {
                dealSecurityRisk(handleAlarms);
            }
        }

    }

    private int getAlarmInt(int alarmTypeInt) {
        int alarmInt = alarmTypeInt;
        // ????????????????????????????????????????????????
        if (alarmTypeInt == 2011 || alarmTypeInt == 2012) {
            alarmInt = 20;
        } else if (alarmTypeInt == 2111 || alarmTypeInt == 2112) {
            alarmInt = 21;
        } else if (alarmTypeInt == 2211 || alarmTypeInt == 2212) {
            alarmInt = 22;
        } else if (alarmTypeInt == 157) {
            // ?????????????????????
            alarmInt = 3;
        }
        return alarmInt;
    }

    private void manualConfirmationAlarm(HandleAlarms handleAlarms, BindDTO bindDTO, int alarmInt) throws Exception {
        if (bindDTO == null) {
            return;
        }
        Integer msgSno = DeviceHelper.getRegisterDevice(handleAlarms.getVehicleId(), handleAlarms.getDevice());
        if (null == msgSno) {
            return;
        }
        AlarmAck alarmAck = new AlarmAck();
        alarmAck.setMsgSNACK(handleAlarms.getSno());
        alarmAck.setType((int) Math.pow(2, alarmInt));
        String deviceId = bindDTO.getDeviceId();
        String deviceType = bindDTO.getDeviceType();
        sendService.alarmAck(deviceId, handleAlarms.getSimcard(), alarmAck, msgSno, deviceType);
    }

    private void sendMonitor(HandleAlarms handleAlarms, BindDTO bindDTO) {
        if (bindDTO == null) {
            return;
        }
        String deviceId = bindDTO.getDeviceId();
        String deviceType = bindDTO.getDeviceType();
        String mobile = bindDTO.getSimCardNumber();
        if (mobile == null) {
            return;
        }
        TelBack telBack = getMonitor(handleAlarms);
        multimediaService
            .telListen(deviceId, telBack, mobile, Integer.valueOf(new Customer().getCustomerID()), deviceType);
    }

    private TelBack getMonitor(HandleAlarms handleAlarms) {
        TelBack telBack = new TelBack();
        telBack.setMobile(handleAlarms.getMonitorPhone());
        //0??????????????? 1???????????????
        telBack.setSign(1);
        return telBack;
    }

    /**
     * ???????????????????????????809??????
     */
    private void dealSecurityRisk(HandleAlarms handleAlarms) {
        try {
            adasRiskService.saveRiskDealInfos(AdasAlarmDealInfo.getInstance(handleAlarms));
        } catch (Exception e) {
            log.error("???????????????????????????????????????", e);
        }
    }

    /**
     * ????????????
     * @param vehicleId  vehicleId
     * @param startTimeL startTimeL
     * @param alarmType  alarmType
     */
    private void saveAlarmSpotCheck(String vehicleId, long startTimeL, String alarmType)
        throws Exception {
        List<AlarmInfo> alarmInfoList;
        String alarmStartTimeStr = DateUtil.getLongToDateStr(startTimeL, DateUtil.DATE_FORMAT);
        // ??????????????? ????????????????????????????????????: pos: 129?????????????????????????????????????????????????????????????????????(??????: 79)???
        // ???????????????????????????????????????????????????????????????????????????????????????cal_standard??? = 2, pos = 76???
        // ????????????????????????????????????7702?????????????????????7703?????????????????????????????????147???
        switch (alarmType) {
            case "76":
                alarmInfoList = alarmSearchService.getTheSameTimeAlarmInfo(vehicleId, alarmType, alarmStartTimeStr, 1);
                // ????????????????????????
                if (CollectionUtils.isNotEmpty(alarmInfoList) && Objects
                    .equals(alarmInfoList.get(0).getCalStandard(), 2)) {
                    addVehicleSpotCheckInfo(vehicleId, startTimeL, alarmInfoList.get(0));
                }
                break;
            case "77":
            case "79":
                // ????????????
            case "129":
                // ?????????????????????
            case "7702":
                // ??????????????????7702
            case "7703":
                // ??????????????????7703
            case "147":
                // ???????????????????????????????????????;
                if ("77".equals(alarmType)) {
                    RedisKey redisKey = HistoryRedisKeyEnum.MONITOR_ALARM_PARAM.of(vehicleId, "77");
                    String jsonStr = com.zw.platform.basic.core.RedisHelper.getString(redisKey);
                    if (StringUtils.isNotBlank(jsonStr)) {
                        JSONObject jsonObject = JSON.parseObject(jsonStr);
                        String calStandard = jsonObject.getString("calStandard");
                        if (!"2".equals(calStandard)) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                alarmInfoList = alarmSearchService.getTheSameTimeAlarmInfo(vehicleId, alarmType, alarmStartTimeStr, 1);
                if (CollectionUtils.isNotEmpty(alarmInfoList)) {
                    addVehicleSpotCheckInfo(vehicleId, startTimeL, alarmInfoList.get(0));
                }
                break;
            default:
                break;
        }
    }

    /**
     * ????????????????????????
     * @param vehicleId  vehicleId
     * @param startTimeL startTimeL
     * @param alarmInfo  alarmInfo
     */
    private void addVehicleSpotCheckInfo(String vehicleId, long startTimeL, AlarmInfo alarmInfo) {
        VehicleSpotCheckInfo vehicleSpotCheckInfo = new VehicleSpotCheckInfo();
        vehicleSpotCheckInfo.setVehicleId(vehicleId);
        vehicleSpotCheckInfo.setLocationTime(new Date(startTimeL));
        Double speedLimit = alarmInfo.getSpeedLimit();
        vehicleSpotCheckInfo.setSpeedLimit(speedLimit != null ? String.valueOf(speedLimit) : null);
        vehicleSpotCheckInfo.setSpeed(alarmInfo.getSpeed());
        String alarmStartLocation = alarmInfo.getAlarmStartLocation();
        if (StringUtils.isNotEmpty(alarmStartLocation)) {
            String[] alarmStartLocations = alarmStartLocation.split(",");
            vehicleSpotCheckInfo.setLongtitude(alarmStartLocations[0]);
            vehicleSpotCheckInfo.setLatitude(alarmStartLocations[1]);
        }
        vehicleSpotCheckInfo.setSpotCheckContent(VehicleSpotCheckInfo.SPOT_CHECK_CONTENT_DEAL_ALARM);
        vehicleSpotCheckInfo.setSpotCheckUser(SystemHelper.getCurrentUsername());
        Date date = new Date();
        vehicleSpotCheckInfo.setSpotCheckTime(date);
        vehicleSpotCheckInfo.setActualViewDate(date);
        spotCheckReportDao.addVehicleSpotCheckInfo(vehicleSpotCheckInfo);
    }

    private void addHandleAlarmsLog(HandleAlarms handleAlarms) {
        Integer webType = handleAlarms.getWebType();
        String handleType = handleAlarms.getHandleType();
        String vehicleId = handleAlarms.getVehicleId();
        // ????????????ip
        String ip;
        if (handleAlarms.getIsAutoDeal() == 1) {
            ip = "127.0.0.1";
        } else {
            ip = new GetIpAddr().getIpAddr(request);
        }
        String module;
        if (webType != null && webType == 2) {
            module = VIDEO_MODULE;
        } else {
            module = MONITORING_MODULE;
        }
        StringBuilder message = new StringBuilder();
        String[] vehicle = logSearchService.findCarMsg(vehicleId);
        if (vehicle != null) {
            String brand = vehicle[0];
            String plateColor = vehicle[1];
            message.append("???????????? : ").append(brand);
            message.append(" ??????????????? ").append(handleType);
            logSearchService.addLog(ip, message.toString(), "3", module, brand, plateColor);
        }
    }

    @Override
    public JsonResultBean setTreeShow(int aliasesFlag, int showTreeCountFlag) {
        JSONObject re = new JSONObject();
        re.put("aliasesFlag", aliasesFlag);
        re.put("showTreeCountFlag", showTreeCountFlag);
        RedisKey redisKey = HistoryRedisKeyEnum.USER_REALTIME_MONITORTREE_SET.of(SystemHelper.getCurrentUsername());
        RedisHelper.setString(redisKey, re.toJSONString());
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public void batchHandleAlarm(HandleMultiAlarms handleMultiAlarms) throws Exception {
        final List<HandleAlarms> handleAlarms = convertToHandleAlarms(handleMultiAlarms);
        handleAlarms.forEach(o -> handleAlarmInfo(o, false));
        alarmSearchService.handleAlarmMulti(handleMultiAlarms);
    }

    private List<HandleAlarms> convertToHandleAlarms(HandleMultiAlarms handleMultiAlarms) {
        return handleMultiAlarms.getRecords().stream().map(source -> {
            final HandleAlarms target = new HandleAlarms();
            target.setVehicleId(source.getVehicleId());
            target.setPlateNumber(source.getPlateNumber());
            target.setAlarm(source.getAlarm());
            target.setStartTime(source.getStartTime());
            target.setEndTime(source.getEndTime());
            target.setHandleType(handleMultiAlarms.getHandleType());
            target.setRemark(handleMultiAlarms.getRemark());
            target.setMonitorPhone(handleMultiAlarms.getMonitorPhone());
            target.setDealOfMsg(handleMultiAlarms.getDealOfMsg());
            return target;
        }).collect(Collectors.toList());
    }
}
