package com.zw.adas.service.realTimeMonitoring.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.common.AdasRiskType;
import com.zw.adas.domain.driverStatistics.bean.AdasFaceCheckAuto;
import com.zw.adas.domain.driverStatistics.show.AdasProfessionalShow;
import com.zw.adas.domain.riskManagement.AdasEventInfo;
import com.zw.adas.domain.riskManagement.AdasInfo;
import com.zw.adas.domain.riskManagement.AdasMedia;
import com.zw.adas.domain.riskManagement.AdasMediaInfo;
import com.zw.adas.domain.riskManagement.AdasRiskEventInfo;
import com.zw.adas.domain.riskManagement.AdasRiskItem;
import com.zw.adas.domain.riskManagement.AlarmSign;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventAlarmForm;
import com.zw.adas.push.cache.AdasSubcibeTable;
import com.zw.adas.push.common.AdasSimpMessagingTemplateUtil;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventDao;
import com.zw.adas.service.core.AdasUserService;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.realTimeMonitoring.AdasRealTimeMonitoringService;
import com.zw.adas.service.realTimeMonitoring.AdasVideoOrderSendService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.FastDFSClient;
import com.zw.adas.utils.controller.AdasQueryListNoParamFunction;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.domain.ProfessionalShowDTO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.ProfessionalService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.query.IcCardDriverQuery;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.realTimeVideo.VideoSettingDao;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsSetService;
import com.zw.platform.service.multimedia.MultimediaService;
import com.zw.platform.service.realTimeVideo.VideoService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.CosUtil;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.FaceMatchUtil;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.msg.t809.body.AdasFaceCheckAutoInfo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
public class AdasRealTimeMonitoringServiceImpl implements AdasRealTimeMonitoringService {

    private static final Logger logger = LogManager.getLogger(AdasRealTimeMonitoringServiceImpl.class);

    @Autowired
    private AdasElasticSearchService esService;

    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfessionalService professionalService;

    @Autowired
    AdasElasticSearchUtil elasticSearchUtil;

    @Autowired
    AdasElasticSearchService adasElasticSearchService;

    @Autowired
    AdasUserService adasUserService;

    @Autowired
    AdasRiskEventDao adasRiskEventDao;

    @Autowired
    LogSearchService logSearchService;

    @Autowired
    VideoSettingDao videoSettingDao;

    @Autowired
    VideoService videoService;

    @Autowired
    AdasVideoOrderSendService adasVideoOrderSendService;

    @Autowired
    AdasSimpMessagingTemplateUtil adasSimpMessagingTemplateUtil;

    @Autowired
    AdasSubcibeTable adasSubcibeTable;

    @Autowired
    AdasRiskService adasRiskService;

    @Autowired
    MultimediaService multimediaService;

    @Autowired
    private AdasCommonHelper adasCommonHelper;
    @Resource
    private HttpServletRequest request;

    @Autowired
    FastDFSClient fastDFSClient;

    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;

    @Value("${adas.mediaServer}")
    private String ftpMediaServer;

    @Value("${fdfs.webServerUrl}")
    private String fastDFSMediaServer;

    @Value("${realTimeAudio.tcpPort}")
    private Integer audioTcpPort;// 音频TCP端口

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Autowired
    private NewProfessionalsDao newProfessionalsDao;

    @Autowired
    private ConnectionParamsSetService connectionParamsSetService;

    @Autowired
    private ServerParamList serverParamList;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private DataSourceTransactionManager txManager;

    @Override
    public AdasProfessionalShow getAdasProfessionalShow(String professionalId) {
        Map<String, ProfessionalShowDTO> professionalShowMap =
            professionalService.getProfessionalShowMap(Collections.singletonList(professionalId));
        return CosUtil.getOneDataFromMap(AdasProfessionalShow.convertProfessionalMaps(professionalShowMap));
    }

    @Override
    public void sendFaceCheckAuto(String vehicleId, AdasFaceCheckAuto faceCheckAuto) {
        logger.info("开始进行1408下发-------------");
        BindDTO vehicleInfo = MonitorUtils.getBindDTO(vehicleId);
        if (vehicleInfo == null) {
            return;
        }
        List<PlantParam> platformIp = connectionParamsSetService.getMonitorPlatform(vehicleId);

        for (PlantParam param : platformIp) {
            AdasFaceCheckAutoInfo faceCheckAutoInfo = AdasFaceCheckAutoInfo.getInstance(vehicleInfo, faceCheckAuto);
            T809Message t809Message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_WARN_MSG, param.getIp(), param.getCenterId(), faceCheckAutoInfo);

            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, t809Message).assembleDesc809(param.getId());
            if (logger.isInfoEnabled()) {
                logger.info("1408下发的参数为：{}", JSON.toJSONString(message));
            }
            // 推送消息
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
        logger.info("结束进行1408下发-------------");
    }

    @Override
    public JsonResultBean getMediaInfo(String riskId) throws Exception {
        JSONObject jsonObject = new JSONObject();
        List<String> picMediaIds = esService.getMediaIdsByRiskId(0, riskId);
        List<String> videoMediaIds = esService.getMediaIdsByRiskId(2, riskId);
        jsonObject.put("picInfo", this.listAdasMediaById(picMediaIds));
        jsonObject.put("videoInfo", this.listAdasMediaById(videoMediaIds));
        return new JsonResultBean(jsonObject);
    }

    private List<AdasMedia> listAdasMediaById(List<String> mediaIds) {
        if (CollectionUtils.isEmpty(mediaIds)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("mediaIds", JSON.toJSONString(mediaIds));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_RISK_MEDIA, params);
        return this.setMediaPath(PaasCloudUrlUtil.getResultListData(str, AdasMedia.class));
    }

    public List<AdasMedia> setMediaPath(List<AdasMedia> medias) {
        if (sslEnabled) {
            fastDFSMediaServer = "/";
            ftpMediaServer = "/mediaserver";
        }
        List<AdasMedia> newMedias = new ArrayList<>();
        String newPath;
        for (AdasMedia media : medias) {
            Integer protocolType = media.getProtocolType();
            if (protocolType != null && protocolType != 1) {
                newPath = fastDFSMediaServer + media.getMediaUrl();
            } else {
                newPath = ftpMediaServer + media.getMediaUrl();
            }
            media.setMediaUrl(newPath);
            newMedias.add(media);
        }
        return newMedias;
    }

    @Override
    public List<AdasRiskItem> listRisks(int pageNum, int pageSize, String riskIdStr, String eventField) {
        Set<String> vehicleList = userService.getCurrentUserMonitorIds();
        if (vehicleList == null) {
            return Collections.emptyList();
        }
        AdasQueryListNoParamFunction<String> query =
            () -> elasticSearchUtil.getTodayUntreatedRisk(vehicleList, riskIdStr);
        if (eventField != null) {
            List<Integer> eventCodes = adasCommonHelper.getAllEventByCommonField(eventField);
            query = () -> elasticSearchUtil.getTodayUntreatedRiskByEventCode(vehicleList, riskIdStr, eventCodes);
        }
        List<String> list = query.execute();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        //得到当前分页的idList
        List<String> riskIds = getPageData(list, pageSize);

        if (riskIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<AdasRiskItem> result = adasRiskService.getRiskList(riskIds);
        int total = list.size();
        if (pageNum == 1 && result.size() != pageSize && total > pageSize) {
            // 查询第一页的时候由于es和hbase 存在先插入和后插入的问题,第一页如果的pageSize比limit小,就要进行补偿,最多补偿5页数据
            compensationRecord(result, list, pageNum, pageSize);
        }
        Set<IcCardDriverQuery> queries = new HashSet<>();
        for (AdasRiskItem risk : result) {
            risk.setId(UuidUtils.getUUIDStrFromBytes(risk.getRiskId()));
            if (StrUtil.areNotBlank(risk.getDriverName(), risk.getCertificationCode())) {
                queries.add(IcCardDriverQuery.getInstance(risk.getCertificationCode(), risk.getDriverName()));
            }
            risk.setRiskType(AdasRiskType.getRiskType(risk.getRiskType()));
            risk.setStatus(risk.getRiskStatus());
            risk.setRiskLevel(risk.getRiskLevel());
        }
        if (CollectionUtils.isEmpty(queries)) {
            return result;
        }
        Map<String, AdasProfessionalShow> driverLicenseNoMap =
            AdasProfessionalShow.convertProfessionalMaps(professionalService.getProfessionalShowMaps(queries));
        if (MapUtils.isNotEmpty(driverLicenseNoMap)) {
            for (AdasRiskItem item : result) {
                AdasProfessionalShow professionalShowDTO = driverLicenseNoMap.get(getCardNumberNameKey(item));
                if (professionalShowDTO != null) {
                    item.setDriverLicenseNo(professionalShowDTO.getDrivingLicenseNo());
                }

            }
        }

        return result;
    }

    @Override
    public Map<String, Long> getEventCountByEventFields(String eventFields) {

        Set<String> vehicleList = userService.getCurrentUserMonitorIds();
        String[] eventFieldArr = eventFields.split(",");
        if (vehicleList == null) {
            return getDefaultResult(eventFieldArr);
        }
        return getEventCountResult(vehicleList, eventFieldArr);
    }

    private Map<String, Long> getEventCountResult(Set<String> vehicleList, String[] eventFieldArr) {

        return elasticSearchUtil.getTodayUntreatedRiskCountByEventCodes(vehicleList, eventFieldArr);

    }

    private Map<String, Long> getDefaultResult(String[] eventFieldArr) {
        Map<String, Long> resultMap = new HashMap<>();
        for (String eventField : eventFieldArr) {
            resultMap.put(eventField, 0L);
        }
        return resultMap;
    }

    private String getCardNumberNameKey(AdasRiskItem item) {
        return item.getCertificationCode() + "_" + item.getDriverName();
    }

    /**
     * 最多查询5页数据
     */
    private void compensationRecord(List<AdasRiskItem> result, List<String> currentUserRiskIds, int pageNum,
        int pageSize) {
        pageNum += 1;
        List<String> idList = getPageData(currentUserRiskIds, pageSize);
        if (CollectionUtils.isNotEmpty(idList)) {
            List<AdasRiskItem> riskList = adasRiskService.getRiskList(idList);
            loop(result, currentUserRiskIds, pageNum, pageSize, riskList);
        }
    }

    /**
     * 获取分页数据
     */
    private List<String> getPageData(List<String> currentUserRiskIds, int pageSize) {
        //@formatter:off
        return currentUserRiskIds.stream()
            .limit(pageSize)
            .collect(Collectors.toList());
        //@formatter:on
    }

    private void loop(List<AdasRiskItem> result, List<String> currentUserRiskIds, int pageNum, int pageSize,
        List<AdasRiskItem> riskList) {
        List<String> idList1;
        if ((riskList.size() + result.size()) >= pageSize) {
            for (int i = 0, len = pageSize - result.size(); i < len; i++) {
                result.add(riskList.get(i));
            }
            return;
        }
        result.addAll(riskList);
        pageNum += 1;
        idList1 = currentUserRiskIds.stream()
            .skip((pageNum - 1) * (long) pageSize)
            .limit(pageSize)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(idList1)) {
            List<AdasRiskItem> riskList2 = adasRiskService.getRiskList(idList1);
            if ((riskList2.size() + result.size()) >= pageSize) {
                for (int i = 0, len = pageSize - result.size(); i < len; i++) {
                    result.add(riskList2.get(i));
                }
            } else {
                if (pageNum > 7) {
                    result.addAll(riskList2);
                    return;
                }
                loop(result, currentUserRiskIds, pageNum, pageSize, riskList2);
            }
        }
    }

    @Override
    public List<AdasRiskEventAlarmForm> getRiskEvents(String riskId) {
        List<String> riskEventIds = adasElasticSearchService.esQueryRiskEventIdsByRiskId(riskId);
        // es和hbase的插入存在先后顺序
        if (CollectionUtils.isEmpty(riskEventIds)) {
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                riskEventIds = adasElasticSearchService.esQueryRiskEventIdsByRiskId(riskId);
                if (!riskEventIds.isEmpty()) {
                    break;
                }
            }
            if (CollectionUtils.isEmpty(riskEventIds)) {
                return new LinkedList<>();
            }
        }
        final List<AdasEventInfo> eventInfos = this.listAdasEventById(riskEventIds);
        Map<String, String> eventMaps = getEventMaps();
        return eventInfos.stream().map(info -> {
            final AdasRiskEventAlarmForm form = new AdasRiskEventAlarmForm();
            form.setEventNumber(info.getEventNumber());
            form.setRiskEvent(eventMaps.get(info.getEventId()));
            form.setEventTime(DateUtil.getLongToDateStr(info.getEventTime(), null));
            form.setId(UuidUtils.getUUIDStrFromBytes(info.getRiskEventIdByte()));
            form.setIdbyte(info.getRiskEventIdByte());
            form.setPicFlag(info.getPicFlag());
            form.setVideoFlag(info.getVideoFlag());
            form.assembleMediaFlag();
            return form;
        }).collect(Collectors.toList());
    }

    private List<AdasEventInfo> listAdasEventById(List<String> riskEventIds) {
        if (CollectionUtils.isEmpty(riskEventIds)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("riskEventIds", JSON.toJSONString(riskEventIds));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_RISK_EVENT, params);
        return PaasCloudUrlUtil.getResultListData(str, AdasEventInfo.class);
    }

    @Override
    public Map<String, String> getEventMaps() {
        Map<String, String> eventMaps = new HashMap<>();
        List<Map<String, String>> events = adasRiskEventDao.getRiskEventMap();
        for (Map<String, String> event : events) {
            eventMaps.put(event.get("functionId"), event.get("riskEvent"));
        }
        return eventMaps;
    }

    private AdasEventInfo getEventInfo(String riskEventId) {
        Map<String, String> params = new HashMap<>(2);
        params.put("riskEventId", riskEventId);
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_RISK_EVENT_INFO, params);
        return PaasCloudUrlUtil.getResultData(str, AdasEventInfo.class);
    }

    /**
     * 手动下发9208补传
     */
    @Override
    public JsonResultBean send9208(String riskEventId, String vehicleId) {
        Map<String, String> monitorInfo = RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId),
            Arrays.asList("deviceType", "deviceId", "simCardNumber", "deviceNumber"));

        Byte protocolType = Byte.valueOf(monitorInfo.get("deviceType"));
        AdasEventInfo adasEventInfo = this.getEventInfo(riskEventId);
        AdasInfo adasInfo = new AdasInfo();
        BeanUtils.copyProperties(adasEventInfo, adasInfo);
        adasInfo.setDeviceId(monitorInfo.get("deviceId"));
        adasInfo.setSimCardNumber(monitorInfo.get("simCardNumber"));
        adasInfo.setProtocolType(protocolType);
        adasInfo.setRiskEventId(riskEventId);
        adasInfo.setId(riskEventId);

        switch (protocolType.toString()) {
            case ProtocolTypeUtil.ZHONG_WEI_PROTOCOL_808_2013:
                List<AdasMediaInfo> mediaInfoList =
                    JSON.parseArray(adasEventInfo.getMediaInfoStr(), AdasMediaInfo.class);
                //黑标补传后面做
                adasInfo.setMediaInfoList(mediaInfoList);
                adasSimpMessagingTemplateUtil.manualSend9208(adasInfo);
                break;
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
            case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
            case ProtocolTypeUtil.HEI_PROTOCOL_808_2019:
            case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
            case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
            case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                if (adasEventInfo.getMediaSerialNumber() == null || adasEventInfo.getMediaCount() == null
                    || adasEventInfo.getMediaCount() <= 0) {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
                AlarmSign alarmSign = new AlarmSign();
                alarmSign.setTime(DateUtil.getDateToString(new Date(adasEventInfo.getWarnTime()), "yyMMddHHmmss"));
                alarmSign.setSerialNumber(adasEventInfo.getMediaSerialNumber());
                alarmSign.setId(monitorInfo.get("deviceNumber"));
                alarmSign.setCount(adasEventInfo.getMediaCount());
                adasInfo.setAlarmSign(alarmSign);
                sendParamGetFile(adasInfo);
                break;
            case ProtocolTypeUtil.BEI_JING_PROTOCOL_808_2019:
                List<AlarmSign> alarmSignList = JSON.parseArray(adasEventInfo.getMediaInfoStr(), AlarmSign.class);
                if (alarmSignList == null || alarmSignList.isEmpty()) {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
                adasInfo.setAlarmSigns(alarmSignList);
                sendBeijing9502(adasInfo);
                break;
            case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                alarmSignList = JSON.parseArray(adasEventInfo.getMediaInfoStr(), AlarmSign.class);
                if (alarmSignList == null || alarmSignList.isEmpty()) {
                    return new JsonResultBean(JsonResultBean.FAULT);
                }
                adasInfo.setAlarmSigns(alarmSignList);
                sendZw9208(adasInfo);
                break;
            default:
                break;
        }
        return new JsonResultBean(JsonResultBean.SUCCESS);
    }

    @Override
    public AdasProfessionalShow getAdasProfessionalByIdentityAndName(String identity, String name) {
        String insertCardDriverId = newProfessionalsDao.getIcCardDriverIdByIdentityAndName(identity, name);
        return getAdasProfessionalShow(insertCardDriverId);

    }

    public void sendParamGetFile(AdasInfo adasInfo) {
        AlarmSign alarmSign = adasInfo.getAlarmSign();
        AdasRiskEventInfo riskEventInfo = new AdasRiskEventInfo(adasInfo, alarmSign.getCount());
        adasSubcibeTable.put(adasInfo.getRiskEventId().replace("-", "") + "_manual", riskEventInfo);
        String log = "车辆：" + adasInfo.getBrand() + "事件：" + adasInfo.getEventNumber() + "风险时间：" + adasInfo.getWarnTime();
        if ((adasInfo.getProtocolType().toString()).equals(ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013)) {
            adasSimpMessagingTemplateUtil
                .sendFileStream8208(alarmSign, adasInfo.getRiskEventId(), adasInfo.getSimCardNumber(),
                    adasInfo.getVehicleId(), adasInfo.getDeviceId(), adasInfo.getProtocolType().intValue(), log);
        } else {
            adasSimpMessagingTemplateUtil
                .sendFileStream9208(alarmSign, adasInfo.getRiskEventId(), adasInfo.getSimCardNumber(),
                    adasInfo.getVehicleId(), adasInfo.getDeviceId(), adasInfo.getProtocolType().intValue(), log);
        }
    }

    public void sendBeijing9502(AdasInfo adasInfo) {
        List<AlarmSign> alarmSigns = adasInfo.getAlarmSigns();
        String log = "车辆：" + adasInfo.getBrand() + "事件：" + adasInfo.getEventNumber() + "风险时间：" + adasInfo.getWarnTime();
        if (alarmSigns != null && !alarmSigns.isEmpty()) {
            int count = 0;
            for (AlarmSign alarmSign : alarmSigns) {
                count += 1;
                alarmSign.setSerialNumber(count);
                adasSimpMessagingTemplateUtil
                    .sendFileStream9502(alarmSign, adasInfo.getRiskEventId(), adasInfo.getSimCardNumber(),
                        adasInfo.getVehicleId(), adasInfo.getDeviceId(), adasInfo.getProtocolType().intValue(), log);
            }
            AdasRiskEventInfo riskEventInfo = new AdasRiskEventInfo(adasInfo, count);
            adasSubcibeTable.put(adasInfo.getRiskEventId().replace("-", "") + "_manual", riskEventInfo);
        } else {
            logger.info("{} 未包含媒体证据！", log);
        }
    }

    public void sendZw9208(AdasInfo adasInfo) {
        try {
            List<AlarmSign> alarmSigns = adasInfo.getAlarmSigns();
            String log =
                "车辆：" + adasInfo.getBrand() + "事件：" + adasInfo.getEventNumber() + "风险时间：" + adasInfo.getWarnTime();
            if (alarmSigns != null && !alarmSigns.isEmpty()) {
                Integer count = 0;
                for (AlarmSign alarmSign : alarmSigns) {
                    count += alarmSign.getCount();
                    adasSimpMessagingTemplateUtil
                        .sendFileStream9208(alarmSign, adasInfo.getRiskEventId(), adasInfo.getSimCardNumber(),
                            adasInfo.getVehicleId(), adasInfo.getDeviceId(), adasInfo.getProtocolType().intValue(),
                            log);
                }
                AdasRiskEventInfo riskEventInfo = new AdasRiskEventInfo(adasInfo, count);
                riskEventInfo.setToken(AdasSimpMessagingTemplateUtil.getToken());
                adasSubcibeTable.put(adasInfo.getRiskEventId().replaceAll("-", "") + "_manual", riskEventInfo);
            } else {
                logger.info(" 未包含媒体证据,{}！", log);
            }
        } catch (Exception e) {
            logger.error("中位标准下发9208异常！", e);
        }
    }

    /**
     * 人证比对接口下发8801
     */
    @Override
    public JsonResultBean send8801(String vehicleId, Photograph photograph) throws Exception {
        BindDTO vehicleInfo = MonitorUtils.getBindDTO(vehicleId);
        if (vehicleInfo == null) {
            sendExceptionFaceCheck(vehicleId);
            return new JsonResultBean(JsonResultBean.FAULT, "获取人脸照片失败");
        }
        String brand = vehicleInfo.getName();// 车牌号
        String deviceId = vehicleInfo.getDeviceId();
        String mobile = vehicleInfo.getSimCardNumber();
        if (mobile == null) {
            sendExceptionFaceCheck(vehicleId);
            return new JsonResultBean(JsonResultBean.FAULT, "获取人脸照片失败");
        }
        long startTime = System.currentTimeMillis();
        Integer msgSN = DeviceHelper.serialNumber(vehicleId);
        String key = vehicleId + "_" + photograph.getWayID() + "_driverCheck";
        adasSubcibeTable.put(key, "1");
        multimediaService.photograph(deviceId, photograph, mobile, msgSN, vehicleInfo);
        //打印日志
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
        TransactionStatus status = txManager.getTransaction(def);// 获得事务状态
        try {
            //逻辑代码，可以写上你的逻辑处理代码
            String message = "监控对象 : " + brand + " 人证识别车辆拍照";
            logSearchService.addLog(new GetIpAddr().getIpAddr(request), message, "3", "MONITORING", brand,
                String.valueOf(vehicleInfo.getPlateColor()));
            txManager.commit(status);
        } catch (Exception e) {
            txManager.rollback(status);
        }
        long endTime = startTime;
        while ((endTime - startTime) < 85000) {
            Thread.sleep(300);
            endTime = System.currentTimeMillis();
            if (adasSubcibeTable.get(key) != null && !adasSubcibeTable.get(key).toString().equals("1")) {
                JSONObject re = (JSONObject) adasSubcibeTable.get(key);
                JSONObject jsonObject = new JSONObject();
                if (re.getString("mediaUrl") == null) {
                    sendExceptionFaceCheck(vehicleId);
                    return new JsonResultBean(JsonResultBean.FAULT, "获取人脸照片失败");
                }
                jsonObject.put("mediaUrl", re.getString("mediaUrl"));
                jsonObject.put("address", re.getString("address"));
                adasSubcibeTable.remove(key);
                return new JsonResultBean(jsonObject);
            }
        }
        adasSubcibeTable.remove(key);
        sendExceptionFaceCheck(vehicleId);
        return new JsonResultBean(JsonResultBean.FAULT, "获取人脸照片失败");
    }

    private void sendExceptionFaceCheck(String vehicleId) {
        AdasFaceCheckAuto adasFaceCheckAuto = new AdasFaceCheckAuto();
        adasFaceCheckAuto.setResult(5);
        sendFaceCheckAuto(vehicleId, adasFaceCheckAuto);
    }

    /**
     * 检查ic卡照片
     */
    @Override
    public Boolean checkIcPhoto(String vehicleId, String icMediaUrl) {
        if (icMediaUrl == null) {
            sendExceptionFaceCheck(vehicleId);
            return false;
        }
        InputStream in = null;
        try {
            String fileName = this.removePrefix(icMediaUrl);
            in = FtpClientUtil.getFileInputStream(ftpUserName, ftpPassword, ftpHostClbs, ftpPortClbs,
                StringUtil.encodingFtpFileName(fileName));
            if (in != null) {
                return true;
            } else {
                sendExceptionFaceCheck(vehicleId);
                return false;
            }
        } catch (Exception e) {
            logger.error("人脸识别从FTP中获取数据异常", e);
            sendExceptionFaceCheck(vehicleId);
            return false;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private String removePrefix(String icMediaUrl) {
        final String separator = sslEnabled ? "mediaserver" : mediaServer;
        final int index = icMediaUrl.indexOf(separator);
        return index < 0 ? icMediaUrl : icMediaUrl.substring(index + separator.length());
    }

    /**
     * 百度人脸比对
     */
    @Override
    public JsonResultBean faceMatch(String vehicleId, String address, String mediaUrl, String icMediaUrl) {
        AdasFaceCheckAuto adasFaceCheckAuto = new AdasFaceCheckAuto();
        byte[] position = address.getBytes(StandardCharsets.ISO_8859_1);
        adasFaceCheckAuto.setPosition(position);
        try {
            String fileUrl = mediaUrl;
            if (!sslEnabled) {
                fileUrl = mediaUrl.split(fastDFSMediaServer)[1];
            }
            if (fileUrl.startsWith("/")) {
                fileUrl = fileUrl.substring(1);
            }

            byte[] media = fastDFSClient.downloadFile(fileUrl);
            byte[] icMedia = configHelper.getFileFromFtp(icMediaUrl);
            if (media != null && icMedia != null) {
                adasFaceCheckAuto.setPhoto(media);
                BaiduFaceMatch baiduFaceMatch = new BaiduFaceMatch(media, icMedia);
                String re;
                Future<String> future = null;
                try {
                    future = taskExecutor.submit(baiduFaceMatch);
                    re = future.get(119000, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    //定义超时后的状态修改调用cancel直接中断线程
                    logger.info("百度人脸识别超时！");
                    if (future != null) {
                        future.cancel(true);

                    }
                    adasFaceCheckAuto.setResult(2);
                    sendFaceCheckAuto(vehicleId, adasFaceCheckAuto);
                    return new JsonResultBean(JsonResultBean.FAULT, "比对超时");
                } catch (Exception e) {
                    logger.error("百度人脸识别异常！", e);
                    adasFaceCheckAuto.setResult(4);
                    sendFaceCheckAuto(vehicleId, adasFaceCheckAuto);
                    return new JsonResultBean(JsonResultBean.FAULT, "连接异常");
                }
                return dealFaceMatchResult(re, adasFaceCheckAuto, vehicleId);
            } else {
                sendExceptionFaceCheck(vehicleId);
                if (icMedia == null) {
                    return new JsonResultBean(JsonResultBean.FAULT, "获取ic卡证件照片失败");
                } else {
                    return new JsonResultBean(JsonResultBean.FAULT, "获取人脸照片失败");
                }
            }
        } catch (Exception e) {
            adasFaceCheckAuto.setResult(4);
            sendFaceCheckAuto(vehicleId, adasFaceCheckAuto);
            return new JsonResultBean(JsonResultBean.FAULT, "连接异常");
        }
    }

    private JsonResultBean dealFaceMatchResult(String re, AdasFaceCheckAuto adasFaceCheckAuto, String vehicleId) {
        if (re != null) {
            JSONObject jsonObject = JSON.parseObject(re);
            Integer error = jsonObject.getInteger("error_code");
            if (error != null && error == 0) {
                int score = (int) jsonObject.getJSONObject("result").getDoubleValue("score");
                if (score >= 80) {
                    adasFaceCheckAuto.setResult(0);
                } else {
                    adasFaceCheckAuto.setResult(1);
                }
                adasFaceCheckAuto.setSimilarity(score);
                sendFaceCheckAuto(vehicleId, adasFaceCheckAuto);
                JSONObject r = new JSONObject();
                r.put("score", score);
                return new JsonResultBean(r);
            } else {
                adasFaceCheckAuto.setResult(4);
                sendFaceCheckAuto(vehicleId, adasFaceCheckAuto);
                return new JsonResultBean(JsonResultBean.FAULT, "对比异常，错误码:" + error);
            }
        } else {
            adasFaceCheckAuto.setResult(4);
            sendFaceCheckAuto(vehicleId, adasFaceCheckAuto);
            return new JsonResultBean(JsonResultBean.FAULT, "连接异常");
        }
    }

    //异步调用百度人脸对比用Future控制超时
    static class BaiduFaceMatch implements Callable<String> {

        private final byte[] media;

        private final byte[] icMedia;

        public BaiduFaceMatch(byte[] media, byte[] icMedia) {
            this.media = media;
            this.icMedia = icMedia;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         * @return computed result
         */
        @Override
        public String call() {
            String re = "0";
            try {
                re = FaceMatchUtil.match(media, icMedia);
            } catch (Exception e) {
                return re;
            }
            return re;
        }
    }

}
