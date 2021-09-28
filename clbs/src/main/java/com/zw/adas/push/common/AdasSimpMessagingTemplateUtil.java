package com.zw.adas.push.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.cb.platform.util.page.PassCloudResultUtil;
import com.google.common.collect.Lists;
import com.zw.adas.domain.define.setting.AdasPlatformParamSetting;
import com.zw.adas.domain.define.setting.AdasPlatformRemind;
import com.zw.adas.domain.riskManagement.AdasAlarmIdent;
import com.zw.adas.domain.riskManagement.AdasInfo;
import com.zw.adas.domain.riskManagement.AdasMediaInfo;
import com.zw.adas.domain.riskManagement.AdasRiskEventInfo;
import com.zw.adas.domain.riskManagement.AlarmSign;
import com.zw.adas.domain.riskManagement.SendAlarmMessage;
import com.zw.adas.domain.riskManagement.TADASMediaMessages;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.domain.riskManagement.form.AdasEventForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.form.AdasRiskEventVehicleConfigForm;
import com.zw.adas.push.cache.AdasSubcibeTable;
import com.zw.adas.push.nettyclient.manager.AdasWebSubscribeManager;
import com.zw.adas.repository.mysql.riskdisposerecord.AdasRiskEventDao;
import com.zw.adas.service.defineSetting.AdasRiskEventConfigService;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.connectionparamsset_809.AlarmHandleParam;
import com.zw.platform.domain.enmu.ProtocolEnum;
import com.zw.platform.domain.vas.f3.AlarmIdent;
import com.zw.platform.push.common.WsSessionManager;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.service.connectionparamsset_809.impl.ConnectionParamsSetServiceImpl;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.report.PaasCloudAdasUrlEnum;
import com.zw.platform.util.spring.InitData;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.util.ProtocolTypeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * <p> Copyright: Copyright (c) 2016 <p> Company: ZhongWei <p> team: ZhongWeiTeam
 * @author lijie
 * @version 1.0
 */
@Component
public class AdasSimpMessagingTemplateUtil {
    private static final Logger logger = LogManager.getLogger(AdasSimpMessagingTemplateUtil.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private AdasRiskEventConfigService riskEventConfigService;

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.path}")
    private String ftpPath;

    @Value("${file.host}")
    private String fileHost;

    @Value("${file.port}")
    private Integer filePort;

    @Value("${zw.file.port}")
    private Integer zwFilePort;

    @Value("${hlj.file.port}")
    private Integer hljFilePort;

    @Value("${bj.file.port}")
    private Integer bjFilePort;

    @Value("${gd.file.port}")
    private Integer gdFilePort;

    @Autowired
    private ConnectionParamsSetServiceImpl connectionParamsSetService;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    @Autowired
    AdasSubcibeTable adasSubcibeTable;

    @Autowired
    private AdasRiskEventDao adasRiskEventDao;

    @Autowired
    private AdasElasticSearchService adasEsService;

    @Autowired
    private AdasRiskService adasRiskService;

    @Value("${9208.catche.time.out}")
    private Integer timeOut;

    //达到触发阈值
    private static final Byte IS_TRIGGER_THRESHOLD = 1;

    /**
     * 下发9208,每接收一个风险 都要执行
     */
    public void sendRisk9208(List<AdasInfo> adasInfos) {
        String type = adasInfos.get(0).getProtocolType().toString();
        for (AdasInfo adasInfo : adasInfos) {
            if (adasInfo.getUp809Flag() != null && adasInfo.getUp809Flag().equals(Byte.valueOf("6"))
                && adasInfo.getT809PlatId() != null) {
                try {
                    dealRiskEvent(adasInfo);
                } catch (Exception e) {
                    logger.error("处理合并的报警上报809异常！", e);
                }
            }
        }
        switch (type) {
            case ProtocolTypeUtil.ZHONG_WEI_PROTOCOL_808_2013:
                sendFtp9208(adasInfos);
                break;
            case ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.GUANG_XI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JIANG_SU_PROTOCOL_808_2013:
            case ProtocolTypeUtil.ZHE_JIANG_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JI_LIN_PROTOCOL_808_2013:
            case ProtocolTypeUtil.JIANG_XI_PROTOCOL_808_2013:
            case ProtocolTypeUtil.SHANG_HAI_PROTOCOL_808_2019:
            case ProtocolTypeUtil.HEI_PROTOCOL_808_2019:
            case ProtocolTypeUtil.LU_PROTOCOL_808_2019:
            case ProtocolTypeUtil.XIANG_PROTOCOL_808_2013:
            case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                sendFile9208(adasInfos);
                break;
            case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                sendZw9208(adasInfos);
                break;
            case ProtocolTypeUtil.SHAN_XI_PROTOCOL_808_2013:
                sendFile8208(adasInfos);
                break;
            case ProtocolTypeUtil.JING_PROTOCOL_808_2019:
                sendBeijing9502(adasInfos);
                sendBeiJing9504(adasInfos);
                break;
            default:
                break;

        }
    }

    public void sendFile9208(List<AdasInfo> adasInfos) {
        try {
            for (AdasInfo adasInfo : adasInfos) {

                //川标添加自动获取附件配置按钮
                if (adasInfo.getProtocolType().toString().equals(ProtocolTypeUtil.SI_CHUAN_PROTOCOL_808_2013)) {
                    String vehicleId = adasInfo.getVehicleId();
                    Set<String> alarmSetting = InitData.automaticVehicleMap.get(vehicleId);
                    if (alarmSetting != null) {
                        if (alarmSetting.contains(adasInfo.getEventId() + "_get_" + adasInfo.getLevel())) {
                            return;
                        }
                    }
                }

                AlarmSign alarmSign = adasInfo.getAlarmSign();
                String log =
                    "车辆：" + adasInfo.getBrand() + "事件：" + adasInfo.getEventNumber() + "风险时间：" + adasInfo.getWarmTime();
                boolean flag = alarmSign != null && alarmSign.getCount() != null && alarmSign.getCount() > 0;
                if (flag) {
                    AdasRiskEventInfo riskEventInfo = new AdasRiskEventInfo(adasInfo, alarmSign.getCount());
                    riskEventInfo.setToken(getToken());
                    riskEventInfo.setAlarmId(alarmSign.getAlarmId());
                    adasSubcibeTable.put(adasInfo.getRiskEventId().replaceAll("-", ""), riskEventInfo);
                    sendFileStream9208(alarmSign, adasInfo.getRiskEventId(), adasInfo.getSimCardNumber(),
                        adasInfo.getVehicleId(), adasInfo.getDeviceId(), adasInfo.getProtocolType().intValue(), log);
                } else {
                    logger.info(" 未包含媒体证据,{}！", log);
                }
            }
        } catch (Exception e) {
            logger.error("川标下发9208异常！", e);
        }

    }

    public void sendZw9208(List<AdasInfo> adasInfos) {
        try {
            for (AdasInfo adasInfo : adasInfos) {

                //当前风险是已处理状态 则是合并上来的风险事件报警 需要发送1412指令主动上报报警处理消息
                //dealRiskEvent(adasInfo);
                List<AlarmSign> alarmSigns = adasInfo.getAlarmSigns();
                String log =
                    "车辆：" + adasInfo.getBrand() + "事件：" + adasInfo.getEventNumber() + "风险时间：" + adasInfo.getWarmTime();
                if (alarmSigns != null && alarmSigns.size() > 0) {
                    Integer count = 0;
                    for (AlarmSign alarmSign : alarmSigns) {
                        count += alarmSign.getCount();
                        sendFileStream9208(alarmSign, adasInfo.getRiskEventId(), adasInfo.getSimCardNumber(),
                            adasInfo.getVehicleId(), adasInfo.getDeviceId(), adasInfo.getProtocolType().intValue(),
                            log);
                    }
                    AdasRiskEventInfo riskEventInfo = new AdasRiskEventInfo(adasInfo, count);
                    riskEventInfo.setToken(getToken());
                    adasSubcibeTable.put(adasInfo.getRiskEventId().replaceAll("-", ""), riskEventInfo);
                } else {
                    logger.info(" 未包含媒体证据,{}！", log);
                }
            }
        } catch (Exception e) {
            logger.error("中位标准下发9208异常！", e);
        }

    }

    /**
     * 当该风险事件的风险已被处理时， 需要发送1412指令主动上报报警处理消息
     * @param adasInfo 报警信息
     * @throws Exception e
     */
    private void dealRiskEvent(AdasInfo adasInfo) throws Exception {
        AdasRiskDisposeRecordForm risk =
                adasRiskService.getRiskDisposeRecordsById(UuidUtils.getBytesFromStr(adasInfo.getRiskId()));
        if (risk != null && risk.getStatus() != null && "6".equals(risk.getStatus())) {
            String handleType = risk.getHandleType() != null ? risk.getHandleType() : "不作处理";
            String monitorId = adasInfo.getVehicleId();

            Integer alarmType = Integer.parseInt(adasInfo.getEventId());
            AlarmHandleParam handleParam = AlarmHandleParam
                .getInstance(alarmType, monitorId, adasInfo.getEventTime(), handleType, null, adasInfo.getRiskEventId(),
                    null);
            handleParam.setOperator(risk.getDealUser());
            handleParam.setIsAutoDeal(1);
            connectionParamsSetService.sendDealAlarm809(handleParam);
        }
    }

    public void sendFileStream9208(AlarmSign alarmSign, String riskEventId, String simCardNumber, String vehicleId,
        String deviceId, Integer protocolType, String log) {
        Integer msgSN = generateMsgSnNumber(vehicleId); // 流水号
        TADASMediaMessages tadasMediaMessages = new TADASMediaMessages();
        tadasMediaMessages.setProtocolType(protocolType);
        tadasMediaMessages.setAlarmSign(alarmSign);
        String alarmNumber = riskEventId.replace("-", "");
        tadasMediaMessages.setAlarmNumer(alarmNumber);
        tadasMediaMessages.setFtpUrl(fileHost);

        switch (protocolType.toString()) {
            case ProtocolTypeUtil.ZW_PROTOCOL_808_2019:
                tadasMediaMessages.setTcpPort(zwFilePort);
                break;
            case ProtocolTypeUtil.HEI_PROTOCOL_808_2019:
                tadasMediaMessages.setTcpPort(hljFilePort);
                break;
            case ProtocolTypeUtil.YUE_PROTOCOL_808_2019:
                tadasMediaMessages.setTcpPort(gdFilePort);
                break;
            default:
                tadasMediaMessages.setTcpPort(filePort);
        }
        tadasMediaMessages.setUdpPort(0);
        logger.info(">====下发（黑川冀贵苏浙吉中位鲁粤湘）标0x9208====<" + log);
        T808Message messages = MsgUtil
            .get808Message(simCardNumber, ConstantUtil.T808_REQ_MEDIA_STORAGE_FTP_9208, msgSN, tadasMediaMessages);
        messages.getMsgHead().setDeviceType(protocolType.toString());
        WebSubscribeManager.getInstance()
            .sendMsgToAll(messages, ConstantUtil.T808_REQ_MEDIA_STORAGE_FTP_9208, deviceId);
    }

    /**
     * 黑标巡检的附件下发9208
     * @param alarmSign
     * @param inspectionResultId
     * @param vehicleId
     * @param inspectionType     (1.车辆运行监测巡检2.驾驶员驾驶行为监测巡检 3.驾驶员身份识别巡检)
     */
    public void sendHeiFileStream9208(AlarmSign alarmSign, String vehicleId, String inspectionResultId,
        Integer inspectionType) {
        if (alarmSign.getCount() == 0) {
            return;
        }

        AdasRiskEventInfo adasRiskEventInfo =
            new AdasRiskEventInfo(vehicleId, inspectionResultId, inspectionType, alarmSign.getCount());

        Integer protocolType = Integer.parseInt(ProtocolEnum.T808_2019_HLJ.getDeviceType());
        Integer msgSN = generateMsgSnNumber(vehicleId); // 流水号
        TADASMediaMessages tadasMediaMessages = new TADASMediaMessages();
        tadasMediaMessages.setProtocolType(protocolType);
        tadasMediaMessages.setAlarmSign(alarmSign);
        String alarmNumber = inspectionResultId.replace("-", "");
        tadasMediaMessages.setAlarmNumer(alarmNumber);
        tadasMediaMessages.setFtpUrl(fileHost);
        tadasMediaMessages.setTcpPort(hljFilePort);
        tadasMediaMessages.setUdpPort(0);
        logger.info(">====下发黑标0x9208获取巡检附件====<" + "车id：" + vehicleId);
        final Map<String, String> vehicleMap =
            RedisHelper.getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "simCardNumber", "deviceId");
        T808Message messages = MsgUtil
            .get808Message(vehicleMap.get("simCardNumber"), ConstantUtil.T808_REQ_MEDIA_STORAGE_FTP_9208, msgSN,
                tadasMediaMessages);
        messages.getMsgHead().setDeviceType(protocolType.toString());
        WebSubscribeManager.getInstance()
            .sendMsgToAll(messages, ConstantUtil.T808_REQ_MEDIA_STORAGE_FTP_9208, vehicleMap.get("deviceId"));
        adasSubcibeTable.put(alarmNumber, adasRiskEventInfo);
    }

    public void sendFile8208(List<AdasInfo> adasInfos) {
        try {
            for (AdasInfo adasInfo : adasInfos) {
                AlarmSign alarmSign = adasInfo.getAlarmSign();
                String log =
                    "车辆：" + adasInfo.getBrand() + "事件：" + adasInfo.getEventNumber() + "风险时间：" + adasInfo.getWarmTime();
                if (alarmSign.getCount() != null && alarmSign.getCount() > 0) {
                    AdasRiskEventInfo riskEventInfo = new AdasRiskEventInfo(adasInfo, alarmSign.getCount());
                    adasSubcibeTable.put(adasInfo.getRiskEventId().replaceAll("-", ""), riskEventInfo);
                    sendFileStream8208(alarmSign, adasInfo.getRiskEventId(), adasInfo.getSimCardNumber(),
                        adasInfo.getVehicleId(), adasInfo.getDeviceId(), adasInfo.getProtocolType().intValue(), log);
                } else {
                    logger.info(log + " 未包含媒体证据！");
                }
            }
        } catch (Exception e) {
            logger.error("川标下发9208异常！", e);
        }

    }

    public void sendBeijing9502(List<AdasInfo> adasInfos) {
        try {
            for (AdasInfo adasInfo : adasInfos) {
                String vehicleId = adasInfo.getVehicleId();
                Set<String> alarmSetting = InitData.automaticVehicleMap.get(vehicleId);
                boolean autoDeal = false;
                if (alarmSetting != null) {
                    //是否自动处理报警
                    if (alarmSetting.contains(adasInfo.getEventId() + "_deal_" + adasInfo.getLevel())) {
                        autoDealAlarm(adasInfo);
                        autoDeal = true;
                    }
                    if (alarmSetting.contains(adasInfo.getEventId() + "_get_" + adasInfo.getLevel())) {
                        return;
                    }
                }

                List<AlarmSign> alarmSigns = adasInfo.getAlarmSigns();
                String log =
                    "车辆：" + adasInfo.getBrand() + "事件：" + adasInfo.getEventNumber() + "风险时间：" + adasInfo.getWarmTime();
                if (alarmSigns != null && alarmSigns.size() > 0) {
                    int count = 0;
                    for (AlarmSign alarmSign : alarmSigns) {
                        count += 1;
                        alarmSign.setSerialNumber(count);
                        sendFileStream9502(alarmSign, adasInfo.getRiskEventId(), adasInfo.getSimCardNumber(),
                            adasInfo.getVehicleId(), adasInfo.getDeviceId(), adasInfo.getProtocolType().intValue(),
                            log);
                    }
                    AdasRiskEventInfo riskEventInfo = new AdasRiskEventInfo(adasInfo, count);
                    riskEventInfo.setAutoDeal(autoDeal);
                    riskEventInfo.setToken(getToken());
                    adasSubcibeTable.put(adasInfo.getRiskEventId().replaceAll("-", ""), riskEventInfo);
                } else {
                    logger.info(log + " 未包含媒体证据！");
                }
            }
        } catch (Exception e) {
            logger.error("京标下发9502异常！", e);
        }

    }

    /**
     * 京标9504 下发报警信息， 暂时只针对京标
     * @param adasInfos 报警信息
     */
    public void sendBeiJing9504(List<AdasInfo> adasInfos) {
        SendAlarmMessage message;
        for (AdasInfo info : adasInfos) {
            String vehicleId = info.getVehicleId();
            Integer msgSN = generateMsgSnNumber(vehicleId); // 流水号
            String simCard = info.getSimCardNumber();
            message = new SendAlarmMessage();
            String eventId = info.getEventId();
            String riskEvent = adasRiskEventDao.getRiskEvent(eventId);
            Integer alarmId = getAlarmId(riskEvent);
            message.setAlarmType(alarmId);
            message.setLevel(info.getLevel());
            Long time = info.getEventTime();
            String eventTime = DateUtil.getLongToDateStr(time, DateUtil.DATE_YYMMDDHHMMSS);
            message.setStartTime(eventTime);
            message.setEndTime(eventTime);
            T808Message messages = MsgUtil.get808Message(simCard, ConstantUtil.T808_SEND_ALARM_INFO, msgSN, message);
            messages.getMsgHead().setDeviceType(ProtocolTypeUtil.BEI_JING_PROTOCOL_808_2019);
            WebSubscribeManager.getInstance()
                .sendMsgToAll(messages, ConstantUtil.T808_REQ_MEDIA_STORAGE_8208, info.getDeviceId());
        }
    }

    /**
     * 获取协议中的报警编号
     * @param alarmName 报警名称
     * @return 报警编号
     */
    public Integer getAlarmId(String alarmName) {
        Integer alarmId;
        switch (alarmName) {
            case "疲劳状态报警":
                alarmId = 0x1001;
                break;
            case "分神驾驶报警":
                alarmId = 0x1002;
                break;
            case "抽烟报警":
                alarmId = 0x1003;
                break;
            case "接打手持电话报警":
                alarmId = 0x1004;
                break;
            case "双手同时脱离方向盘报警":
                alarmId = 0x1005;
                break;
            case "未系安全带报警":
                alarmId = 0x1006;
                break;
            case "驾驶人异常报警":
                alarmId = 0x1007;
                break;
            case "前车碰撞报警":
                alarmId = 0x2001;
                break;
            case "车距过近报警":
                alarmId = 0x2002;
                break;
            case "车道偏离报警":
                alarmId = 0x2003;
                break;
            case "行人碰撞报警":
                alarmId = 0x2004;
                break;
            case "盲区监测报警":
                alarmId = 0x2005;
                break;
            case "限速报警":
                alarmId = 0x2006;
                break;
            case "限高报警":
                alarmId = 0x2007;
                break;
            case "限宽报警":
                alarmId = 0x2008;
                break;
            case "限重报警":
                alarmId = 0x2009;
                break;
            case "设备遮挡报警":
                alarmId = 0x3001;
                break;
            case "红外阻断报警":
                alarmId = 0x3002;
                break;
            case "驾驶行为监测失效报警":
                alarmId = 0x3003;
                break;
            case "车辆运行监测失效报警":
                alarmId = 0x3004;
                break;
            default:
                alarmId = null;
                break;
        }
        return alarmId;
    }

    public void autoDealAlarm(AdasInfo adasInfo) {
        try {
            Integer alarmType = adasCommonHelper.getT808AlarmType(Integer.parseInt(adasInfo.getEventId()));
            long time = adasInfo.getEventTime();
            String handleType = "人工确认报警";//对应上报结果为 1（已处理完毕）
            AlarmHandleParam handleParam = AlarmHandleParam
                .getInstance(alarmType, adasInfo.getVehicleId(), time, handleType, null, adasInfo.getRiskEventId(),
                    null);
            handleParam.setIsAutoDeal(1);

            final long now = System.currentTimeMillis();
            final AdasEventForm paasUpdateParam = new AdasEventForm();
            paasUpdateParam.setId(UuidUtils.getBytesFromStr(adasInfo.getRiskEventId()));
            paasUpdateParam.setStatus(6);
            paasUpdateParam.setDealer("");
            paasUpdateParam.setFileTime(now);
            paasUpdateParam.setResult(1);
            paasUpdateParam.setDealTime(now);
            paasUpdateParam.setHandleType(handleType);
            paasUpdateParam.setRemark("");
            adasRiskService.updateRiskEvents(Collections.singletonList(paasUpdateParam));

            AdasRiskEventEsBean riskEventEsBean = new AdasRiskEventEsBean();
            riskEventEsBean.setId(adasInfo.getRiskEventId());
            riskEventEsBean.setStatus(6);
            riskEventEsBean.setDealTime(new Date());
            riskEventEsBean.setRiskResult(1);
            adasEsService.esUpdateRiskEvent(riskEventEsBean);
            //上报报警处理结果
            connectionParamsSetService.initiativeSendAlarmHandle(handleParam);
        } catch (Exception e) {
            logger.error("京标自动处理报警异常！", e);
        }
    }

    public void sendFileStream9502(AlarmSign alarmSign, String riskEventId, String simCardNumber, String vehicleId,
        String deviceId, Integer protocolType, String log) {
        Integer msgSN = generateMsgSnNumber(vehicleId); // 流水号
        TADASMediaMessages tadasMediaMessages = new TADASMediaMessages();
        tadasMediaMessages.setProtocolType(protocolType);
        tadasMediaMessages.setAlarmSign(alarmSign);
        String alarmNumber = riskEventId.replace("-", "");
        tadasMediaMessages.setAlarmNumer(alarmNumber);
        tadasMediaMessages.setServiceIp(fileHost);
        tadasMediaMessages.setFtpUrl(fileHost);
        tadasMediaMessages.setTcpPort(bjFilePort);
        tadasMediaMessages.setToken(alarmNumber);
        tadasMediaMessages.setTokenLength(tadasMediaMessages.getToken().length());
        tadasMediaMessages.setUdpPort(0);
        logger.info(">====下发京标0x9502====<" + log);
        T808Message messages =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_REQ_MEDIA_STORAGE_9502, msgSN, tadasMediaMessages);
        messages.getMsgHead().setDeviceType(protocolType.toString());
        WebSubscribeManager.getInstance().sendMsgToAll(messages, ConstantUtil.T808_REQ_MEDIA_STORAGE_9502, deviceId);

        AdasWebSubscribeManager.getInstance()
            .sendMsgToAll(messages, ConstantUtil.T808_REQ_MEDIA_STORAGE_9502, deviceId);

    }

    /**
     * 生成随机token
     */
    public static String getToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public void sendFileStream8208(AlarmSign alarmSign, String riskEventId, String simCardNumber, String vehicleId,
        String deviceId, Integer protocolType, String log) {
        Integer msgSN = generateMsgSnNumber(vehicleId); // 流水号
        TADASMediaMessages tadasMediaMessages = new TADASMediaMessages();
        tadasMediaMessages.setProtocolType(protocolType);
        tadasMediaMessages.setAlarmSign(alarmSign);
        String alarmNumber = riskEventId.replace("-", "");
        tadasMediaMessages.setAlarmNumer(alarmNumber);
        tadasMediaMessages.setFtpUrl(fileHost);
        tadasMediaMessages.setTcpPort(filePort);
        tadasMediaMessages.setUdpPort(0);
        logger.info(">====下发陕标0x8208====<" + log);
        T808Message messages =
            MsgUtil.get808Message(simCardNumber, ConstantUtil.T808_REQ_MEDIA_STORAGE_8208, msgSN, tadasMediaMessages);
        messages.getMsgHead().setDeviceType(protocolType.toString());
        WebSubscribeManager.getInstance().sendMsgToAll(messages, ConstantUtil.T808_REQ_MEDIA_STORAGE_8208, deviceId);
    }

    public void sendFtp9208(List<AdasInfo> adasInfos) {
        try {
            List<AdasInfo> adasList = Lists.newLinkedList();
            List<AdasInfo> dsmList = Lists.newLinkedList();
            for (AdasInfo adasInfo : adasInfos) {
                String eventId = adasInfo.getEventId();
                // 判断报警是adas还是dsm
                if (eventId.startsWith("65")) {
                    dsmList.add(adasInfo);
                } else {
                    adasList.add(adasInfo);
                }
            }
            judgeAdasMedia(adasList, "ADAS");
            judgeAdasMedia(dsmList, "DSM");
        } catch (Exception e) {
            logger.error("黑标下发9208失败", e);
        }
    }

    /**
     * 向用户推送风险,刷新风控作战页面
     */
    public void sendAdasRisk(Object obj) {
        final Set<String> sessionIds = WsSessionManager.INSTANCE.getAllStatuses();
        final String msg = JSON.toJSONString(obj);
        for (String sessionId : sessionIds) {
            sendToSession(ConstantUtil.WEB_SOCKET_RISK_LOCATION, sessionId, msg);
        }
    }

    /**
     * 手动下发黑标9208
     */
    public void manualSend9208(AdasInfo adasInfo) {
        List<String> subcibeTableKeys = new ArrayList<>();
        String alarmIdent;
        int riskEventId;
        String deviceId = adasInfo.getDeviceId();
        List<AdasMediaInfo> mediaInfos = adasInfo.getMediaInfoList();
        String riskNumber = adasInfo.getRiskNumber();
        String vehicleId = adasInfo.getVehicleId();
        String brand = adasInfo.getBrand();
        String eventId = adasInfo.getEventId();
        String eventNumber = adasInfo.getEventNumber();
        String id = adasInfo.getId();
        int level = adasInfo.getRiskLevel();
        // 遍历媒体信息
        if (mediaInfos != null && mediaInfos.size() > 0) {
            for (AdasMediaInfo mediaInfo : mediaInfos) {
                alarmIdent = Integer.toHexString(mediaInfo.getAlarmIdent()).toUpperCase();
                List<AdasRiskEventInfo> riskEventInfos = new ArrayList<>();
                String resultPath = assembleFilePath(vehicleId, mediaInfo.getTime()); // 多媒体文件上传路径
                Integer msgSN = generateMsgSnNumber(vehicleId); // 流水号
                String fileName = assembleFileName(mediaInfo, alarmIdent);
                if (eventId == null || eventId.equals("")) {
                    logger.info(">====解析报警事件----监控对象(" + brand + ")该条风险:" + riskNumber + "根据报警事件id未解析出报警类型");
                    break;
                }

                //获取adas参数配置
                List<AdasRiskEventVehicleConfigForm> riskEventVehicleConfigForms =
                    eventId.startsWith("64") ? riskEventConfigService.findAdasRiskSettingByVid(vehicleId) :
                        riskEventConfigService.findDsmRiskSettingByVid(vehicleId);
                Map<String, AdasRiskEventVehicleConfigForm> riskEventVehicleConfigFormMap = new HashMap<>();
                for (AdasRiskEventVehicleConfigForm riskEventVehicleConfigForm : riskEventVehicleConfigForms) {
                    riskEventVehicleConfigFormMap
                        .put(riskEventVehicleConfigForm.getRiskId(), riskEventVehicleConfigForm);
                }
                riskEventId = Integer.parseInt(eventId);
                // 当风险事件的类型和媒体风险类型一致时就下发9208
                if ((AdasAlarmIdent.alarmIdentMap.containsKey(alarmIdent) && AlarmIdent.alarmIdentMap.get(alarmIdent)
                    .equals(eventId)) || alarmIdent.equals("E199") || alarmIdent.equals("E299")) {
                    riskEventInfos.add(new AdasRiskEventInfo(adasInfo, mediaInfo.getMultiType(), resultPath));
                    isSend(riskEventId, mediaInfo, fileName, level, deviceId, msgSN, resultPath, brand, riskEventInfos,
                        riskEventVehicleConfigFormMap);
                    subcibeTableKeys.add(msgSN + "&" + deviceId);
                }
            }
            if (subcibeTableKeys.size() != 0) {
                RedisHelper.setString(HistoryRedisKeyEnum.ADAS_MANUAL_SEND_9208_EXPIRE_TIME.of(id),
                    JSONArray.toJSONString(subcibeTableKeys), timeOut);
            } else {
                Map<String, String> param = new HashMap<>();
                param.put("eventId", JSON.toJSONString(UuidUtils.getBytesFromStr(id)));
                param.put("attachmentStatus", "1");
                String sendResult = HttpClientUtil.send(PaasCloudAdasUrlEnum.UPDATE_EVENT_ATTACHMENT_STATUS, param);
                Boolean re = PassCloudResultUtil.getClassResult(sendResult, Boolean.class);
                if (!re) {
                    logger.error("调用pass api 更新下发9208状态失败！");
                }
            }
        } else {
            logger.info("监控对象(" + brand + ")该条报警:" + eventNumber + "未包含媒体信息");
        }
    }

    /**
     * 位置数据ADAS附加信息处理
     */
    public void judgeAdasMedia(List<AdasInfo> list, String type) {
        if (list.size() > 0) {
            String eventId;
            String alarmIdent;
            Integer riskEventId;
            int level;
            AdasInfo adasInfo = list.get(0);
            String deviceId = adasInfo.getDeviceId();
            List<AdasMediaInfo> mediaInfos = adasInfo.getMediaInfoList();
            String riskNumber = adasInfo.getRiskNumber();
            String vehicleId = adasInfo.getVehicleId();
            String brand = adasInfo.getBrand();
            // 遍历媒体信息
            if (mediaInfos != null && mediaInfos.size() > 0) {
                List<AdasRiskEventVehicleConfigForm> riskEventVehicleConfigForms =
                    type.equals("ADAS") ? riskEventConfigService.findAdasRiskSettingByVid(vehicleId) :
                        riskEventConfigService.findDsmRiskSettingByVid(vehicleId);
                Map<String, AdasRiskEventVehicleConfigForm> riskEventVehicleConfigFormMap = new HashMap<>();
                for (AdasRiskEventVehicleConfigForm riskEventVehicleConfigForm : riskEventVehicleConfigForms) {
                    riskEventVehicleConfigFormMap
                        .put(riskEventVehicleConfigForm.getRiskId(), riskEventVehicleConfigForm);
                }
                for (AdasMediaInfo mediaInfo : mediaInfos) {
                    alarmIdent = Integer.toHexString(mediaInfo.getAlarmIdent()).toUpperCase();
                    boolean sendResult = false;
                    List<AdasRiskEventInfo> riskEventInfos = new ArrayList<>();
                    String resultPath = assembleFilePath(vehicleId, mediaInfo.getTime()); // 多媒体文件上传路径
                    Integer msgSN = generateMsgSnNumber(vehicleId); // 流水号
                    String fileName = assembleFileName(mediaInfo, alarmIdent);
                    // 遍历风险事件信息
                    for (AdasInfo object : list) {
                        // deviceId = object.getDeviceId();
                        eventId = object.getEventId();
                        level = object.getLevel();
                        if (eventId == null || eventId.equals("")) {
                            logger.info(">====解析报警事件----监控对象(" + brand + ")该条风险:" + riskNumber + "根据报警事件id未解析出报警类型");
                            continue;
                        }
                        riskEventId = Integer.parseInt(eventId);
                        // 当风险事件的类型和媒体风险类型一致时就下发9208
                        if (AdasAlarmIdent.alarmIdentMap.containsKey(alarmIdent) && AlarmIdent.alarmIdentMap
                            .get(alarmIdent).equals(eventId)) {
                            riskEventInfos.add(new AdasRiskEventInfo(adasInfo, mediaInfo.getMultiType(), resultPath));
                            isSend(riskEventId, mediaInfo, fileName, level, deviceId, msgSN, resultPath, brand,
                                riskEventInfos, riskEventVehicleConfigFormMap);
                        }
                        // 当媒体的风险证据类型为E199或E299时，将所有的
                        if (alarmIdent.equals("E199") || alarmIdent.equals("E299")) {
                            boolean judgeResult =
                                isTranscribeVedio(getRiskId(riskEventId), level, mediaInfo.getMultiType(), brand,
                                    riskEventVehicleConfigFormMap);
                            if (judgeResult) {
                                sendResult = true;
                                riskEventInfos
                                    .add(new AdasRiskEventInfo(adasInfo, mediaInfo.getMultiType(), resultPath));
                            }
                        }
                    }
                    //当风险证据为E199或E299时当有任意事件需要此证据就下发
                    if (sendResult && riskEventInfos.size() > 0) {
                        JSONArray paramData = new JSONArray();
                        paramData.add(JSON.toJSONString(mediaInfo));
                        sendMedia9208(deviceId, paramData, msgSN, fileName, resultPath, mediaInfo.getSimcardNumber(),
                            brand, riskEventInfos);
                    }
                }
            } else {
                logger.info("监控对象(" + brand + ")该条风险:" + riskNumber + "的" + type + "报警未包含媒体信息");
            }
        }
    }

    // 判断下发参数
    private void isSend(Integer riskEventIdId, AdasMediaInfo mediaInfo, String fileName, Integer level, String deviceId,
        Integer msgSN, String resultPath, String brand, List<AdasRiskEventInfo> riskEventInfos,
        Map<String, AdasRiskEventVehicleConfigForm> riskEventVehicleConfigFormMap) {
        JSONArray laterMediaInfo = new JSONArray();
        Integer mediaFileType = mediaInfo.getMultiType();
        String resultRisk = getRiskId(riskEventIdId);
        boolean sendResult = isTranscribeVedio(resultRisk, level, mediaFileType, brand, riskEventVehicleConfigFormMap);
        if (sendResult) {
            String sendParam = JSON.toJSONString(mediaInfo);
            laterMediaInfo.add(sendParam);
        }
        if (laterMediaInfo.size() > 0) {
            if (!resultPath.isEmpty()) {
                sendMedia9208(deviceId, laterMediaInfo, msgSN, fileName, resultPath, mediaInfo.getSimcardNumber(),
                    brand, riskEventInfos); // 下发
            }
        }
    }

    /**
     * 组装文件上传路径
     */
    public String assembleFilePath(String vehicleId, String fileTime) {
        // 组装文件路径(ADAS/车id前两位/完整车id/年月/)
        if (vehicleId != null && vehicleId.length() > 3 && fileTime != null && fileTime.length() > 5) {
            String vidTwo = vehicleId.substring(0, 2);
            String ftpPathTime = DateUtil.getDateYearMouthDay(fileTime);
            return ftpPath + "/" + vidTwo + "/" + vehicleId + "/" + ftpPathTime + "/";
        }
        return "";
    }

    /**
     * 判断是否需要视频
     */
    private boolean isTranscribeVedio(String riskId, Integer level, Integer mediaFileType, String brand,
        Map<String, AdasRiskEventVehicleConfigForm> riskEventVehicleConfigFormMap) {
        // 查询设置参数
        AdasRiskEventVehicleConfigForm riskEventVehicleConfigForm = riskEventVehicleConfigFormMap.get(riskId);
        if (riskEventVehicleConfigForm == null) {
            logger.info(">====解析多媒体信息----监控对象(" + brand + ")未设置风险事件id: " + riskId + " 参数");
            return false;
        } else {
            if (mediaFileType == 2) { // 视频
                // 视频要判断一级报警和二级报警的开关(1是开,0关闭)
                Integer oneLevelAlarmEnable = riskEventVehicleConfigForm.getOneLevelAlarmEnable();
                Integer twoLevelAlarmEnable = riskEventVehicleConfigForm.getTwoLevelAlarmEnable();
                if ((oneLevelAlarmEnable != null && twoLevelAlarmEnable != null) && (
                    (oneLevelAlarmEnable.equals(0) && twoLevelAlarmEnable.equals(0)) || (level.equals(1)
                        && oneLevelAlarmEnable.equals(0)) || (level.equals(2) && twoLevelAlarmEnable.equals(0)))) {
                    logger.info(">====解析多媒体视频信息----监控对象(" + brand + ")参数设置风险事件id:" + riskId + " 一级报警或二级报警开关为关");
                    return false;
                }
                // 高速录制,低速录制是否开启(0是关闭,1是开启)
                Integer highSpeedReco = riskEventVehicleConfigForm.getHighSpeedRecording() != null
                    ?
                    riskEventVehicleConfigForm.getHighSpeedRecording() : 0;
                Integer lowSpeedReco = riskEventVehicleConfigForm.getLowSpeedRecording() != null
                    ?
                    riskEventVehicleConfigForm.getLowSpeedRecording() : 0;
                if ((highSpeedReco.equals(0) && lowSpeedReco.equals(0)) || (level.equals(1) && lowSpeedReco.equals(0))
                    || (level.equals(2) && highSpeedReco.equals(0))) {
                    logger.info(">====解析多媒体视频信息----监控对象(" + brand + ")参数设置风险事件id:" + riskId + " 低速录制或高速录制开关为关");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取事件id(如64021车道左偏离报警事件id为6402),用于查询风险设置信息时使用
     */
    private String getRiskId(Integer riskId) {
        String resultRisk = riskId.toString();
        if (resultRisk.length() == 5) {
            resultRisk = resultRisk.substring(0, resultRisk.length() - 1);
        }
        return resultRisk;
    }

    /**
     * 组装文件名
     */
    private String assembleFileName(AdasMediaInfo mediaObj, String alarmFlag) {
        StringBuilder simcardNumber = new StringBuilder(mediaObj.getSimcardNumber());
        if (simcardNumber.length() < 12) {
            for (int i = 0; i < 12; i++) {
                simcardNumber.insert(0, "0");
                if (simcardNumber.length() == 12) {
                    break;
                }
            }
        }
        String serialNumber = ""; //   序号
        if (mediaObj.getSerialNumber() < 10) {
            serialNumber = "0" + mediaObj.getSerialNumber();
        }
        // 多媒体类型
        String multiType = "";
        if (mediaObj.getMultiType() < 10) {
            multiType = "0" + mediaObj.getMultiType();
        }
        StringBuilder fileName = new StringBuilder();
        String fileTime = mediaObj.getTime();
        // 序号之后的00是保留位,需参与文件名的生成
        fileName.append(simcardNumber).append(alarmFlag).append(fileTime).append(multiType).append(serialNumber)
            .append("00");
        return fileName.toString();
    }

    /**
     * 下发9208指令
     * @param resultMediaInfo 下发的多媒体列表
     * @param msgSN           // 流水号
     * @param fileName        // 文件名
     */
    private void sendMedia9208(String deviceId, JSONArray resultMediaInfo, Integer msgSN, String fileName,
        String resultPath, String simcardNumber, String brand, List<AdasRiskEventInfo> riskEventInfos) {
        String log = "车辆：" + brand + "文件名：" + fileName + "事件：" + riskEventInfos.get(0).getEventNumber();
        try {
            // 设置消息内容
            TADASMediaMessages riskVideoSend = new TADASMediaMessages();
            riskVideoSend.setProtocolType(1);
            riskVideoSend.setFilePath(resultPath); //文件上传路径
            riskVideoSend.setFtpUrl(ftpHost); // 服务器地址
            riskVideoSend.setPort(ftpPort); //  端口号
            riskVideoSend.setUserName(ftpUserName); // 用户名
            riskVideoSend.setPwd(ftpPassword); // 密码
            riskVideoSend.setMediaInfos(resultMediaInfo); // 多媒体信息列表
            riskVideoSend.setMediaCount(resultMediaInfo.size()); // 报警附加多媒体信息列表总数
            // 订阅0x1208应答
            SubscibeInfo subscibeInfo = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
                ConstantUtil.T808_RSP_MEDIA_STORAGE_FTP_1208, JSONArray.toJSONString(riskEventInfos));
            SubscibeInfoCache.getInstance().putTable(subscibeInfo, timeOut.longValue());
            // 组装消息信息
            T808Message messages = MsgUtil
                .get808Message(simcardNumber, ConstantUtil.T808_REQ_MEDIA_STORAGE_FTP_9208, msgSN, riskVideoSend);
            // 下发0x9208
            WebSubscribeManager.getInstance()
                .sendMsgToAll(messages, ConstantUtil.T808_REQ_MEDIA_STORAGE_FTP_9208, deviceId);
            logger.info(">====下发黑标0x9208====<" + log);
        } catch (Exception e) {
            logger.error("下发黑标9208异常" + log, e);
        }
    }

    /**
     * 防止流水号并发
     */
    private Integer generateMsgSnNumber(String vehicleId) {
        return DeviceHelper.deviceSerialNumber(vehicleId);
    }

    /**
     * 推送主动安全报警信号
     */
    public void sendSecurityRiskToUsers(String vehicleId) {
        final Set<String> sessionIds = RiskSessionManager.INSTANCE.getRiskSubscribers(vehicleId);
        for (String sessionId : sessionIds) {
            sendToSession("/securityRiskRingBell", sessionId, "ring");
        }
    }

    public void pushPlatformRemind(List<AdasInfo> adasInfoList) {
        Map<String, AdasPlatformParamSetting> platformParamSettingMap =
            InitData.platformParamMap.get(adasInfoList.get(0).getVehicleId());
        if (platformParamSettingMap == null) {
            return;
        }
        AdasPlatformRemind remind = new AdasPlatformRemind();
        remind.setBrand(adasInfoList.get(0).getBrand());
        remind.setWarmTime(adasInfoList.get(0).getWarmTime());
        remind.setVehicleId(adasInfoList.get(0).getVehicleId());
        for (AdasInfo adasInfo : adasInfoList) {
            AdasPlatformParamSetting paramSetting = platformParamSettingMap.get(adasInfo.getOriginalEventId());
            if (paramSetting == null) {
                continue;
            }
            if (IS_TRIGGER_THRESHOLD.equals(adasInfo.getTimeThresholdReminder())) {
                setAlarmRemind(paramSetting.getTimeAlarmRemind(), remind);
            }
            if (IS_TRIGGER_THRESHOLD.equals(adasInfo.getDistanceThresholdReminder())) {
                setAlarmRemind(paramSetting.getDistanceAlarmRemind(), remind);
            }
            if (!IS_TRIGGER_THRESHOLD.equals(adasInfo.getTimeThresholdReminder()) && !IS_TRIGGER_THRESHOLD
                .equals(adasInfo.getDistanceThresholdReminder())) {
                if (adasInfo.getLevel() == 1) {
                    setAlarmRemind(paramSetting.getAlarmRemindOne(), remind);
                }
                if (adasInfo.getLevel() == 2) {
                    setAlarmRemind(paramSetting.getAlarmRemindTwo(), remind);
                }
                if (adasInfo.getLevel() == 3) {
                    setAlarmRemind(paramSetting.getAlarmRemindThree(), remind);
                }
            }
        }
        final Set<String> sessionIds = RiskSessionManager.INSTANCE.getReminders(adasInfoList.get(0).getVehicleId());
        final String msg = JSON.toJSONString(remind);
        for (String sessionId : sessionIds) {
            sendToSession(ConstantUtil.WEB_SOCKET_PLATFORM_REMIND, sessionId, msg);
        }
    }

    /**
     * 报警提醒方式（一级报警） 0无,1闪烁,2提示音,3闪烁加提示音,4弹窗提醒,5短信提醒
     */
    private void setAlarmRemind(Byte type, AdasPlatformRemind remind) {
        switch (type) {
            case 1:
                remind.setBlinkingPrompt(true);
                break;
            case 2:
                remind.setPromptTone(true);
                break;
            case 3:
                remind.setBlinkingPrompt(true);
                remind.setPromptTone(true);
                break;
            case 4:
                remind.setPopupPrompt(true);
                break;
            case 0:
            case 5:
            default:
                break;
        }
    }

    public void sendToSession(String dest, String sessionId, Object msg) {
        SimpMessageHeaderAccessor header = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        header.setSessionId(sessionId);
        header.setLeaveMutable(true);
        final String user = WsSessionManager.INSTANCE.getSessionUser(sessionId);
        simpMessagingTemplate.convertAndSendToUser(user, dest, msg, header.getMessageHeaders());
    }
}
