package com.zw.platform.push.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zw.adas.domain.riskManagement.AdasEventInfo;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.push.cache.AdasSubcibeTable;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.service.report.DeliveryLineService;
import com.zw.adas.service.riskdisposerecord.AdasRiskService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.FastDFSClient;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.core.RedisKey;
import com.zw.platform.basic.core.TypeCacheManger;
import com.zw.platform.basic.domain.ProfessionalDO;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.BusinessScopeDTO;
import com.zw.platform.basic.dto.DeviceDTO;
import com.zw.platform.basic.dto.ProfessionalDTO;
import com.zw.platform.basic.dto.UserDTO;
import com.zw.platform.basic.dto.VehicleDTO;
import com.zw.platform.basic.dto.VehiclePurposeDTO;
import com.zw.platform.basic.dto.VehicleTypeDTO;
import com.zw.platform.basic.repository.NewConfigDao;
import com.zw.platform.basic.repository.NewProfessionalsDao;
import com.zw.platform.basic.service.BusinessScopeService;
import com.zw.platform.basic.service.DeviceService;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.service.VehicleService;
import com.zw.platform.basic.service.impl.ProfessionalServiceImpl;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.basicinfo.ProfessionalsInfo;
import com.zw.platform.domain.basicinfo.TerminalTypeRedisInfo;
import com.zw.platform.domain.basicinfo.VehicleInfo;
import com.zw.platform.domain.connectionparamsset_809.PlantParam;
import com.zw.platform.domain.connectionparamsset_809.T809AlarmMapping;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.customenum.RecordCollectionEnum;
import com.zw.platform.domain.enmu.VehicleTypeEnum;
import com.zw.platform.domain.multimedia.MultimediaData;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.netty.ServerParamList;
import com.zw.platform.domain.oil.AlarmHandle;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.oilsubsidy.locationinformation.OilSubsidyLocationInformationDO;
import com.zw.platform.domain.oilsubsidy.subsidyManage.ReissueDataRequestDTO;
import com.zw.platform.domain.oilsubsidy.subsidyManage.VehicleLocationSupplementaryData;
import com.zw.platform.domain.oilsubsidy.subsidyManage.VehicleLocationSupplementaryInfo;
import com.zw.platform.domain.realTimeVideo.DiskInfo;
import com.zw.platform.domain.realTimeVideo.FileUploadForm;
import com.zw.platform.domain.realTimeVideo.VideoFTPForm;
import com.zw.platform.domain.realTimeVideo.VideoTrafficInfo;
import com.zw.platform.domain.reportManagement.LogSearch;
import com.zw.platform.domain.reportManagement.SuperPlatformMsg;
import com.zw.platform.domain.reportManagement.T809AlarmFileListAck;
import com.zw.platform.domain.reportManagement.T809AlarmFileListAckZw;
import com.zw.platform.domain.reportManagement.T809AlarmForwardInfo;
import com.zw.platform.domain.reportManagement.T809AlarmForwardInfoMiddleQuery;
import com.zw.platform.domain.reportManagement.T809AlarmForwardInfoQuery;
import com.zw.platform.domain.reportManagement.T809InfoCheckAck;
import com.zw.platform.domain.reportManagement.T809InfoCheckAckZw;
import com.zw.platform.domain.reportManagement.WarnMsgFileInfo;
import com.zw.platform.domain.reportManagement.WarnMsgFileInfoZw;
import com.zw.platform.domain.reportManagement.WarnMsgStaticsInfo;
import com.zw.platform.domain.reportManagement.form.LogSearchForm;
import com.zw.platform.domain.reportManagement.query.DrivingRecordInfoQuery;
import com.zw.platform.domain.riskManagement.RiskEventShortInfo;
import com.zw.platform.domain.sendTxt.ProfessionalsRequestAcK;
import com.zw.platform.domain.systems.Directive;
import com.zw.platform.dto.protocol.DeviceInstallInfoDto;
import com.zw.platform.dto.protocol.FreightVehicleDto;
import com.zw.platform.dto.protocol.GeneralVehicleDto;
import com.zw.platform.dto.protocol.HeiDeviceInstallInfoDto;
import com.zw.platform.dto.protocol.PassengerVehicleDto;
import com.zw.platform.dto.protocol.SiChuanProtocolEnterpriseStaticInfo;
import com.zw.platform.dto.protocol.SiChuanProtocolProfessionalStaticInfo;
import com.zw.platform.dto.protocol.SiChuanProtocolVehicleStaticInfo;
import com.zw.platform.dto.protocol.UpInfo;
import com.zw.platform.dto.protocol.ZwProtocolEnterpriseStaticInfo;
import com.zw.platform.push.config.SpringBeanUtil;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.controller.UserCache;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.push.handler.device.DeviceMessageHandler;
import com.zw.platform.repository.modules.ConnectionParamsConfigDao;
import com.zw.platform.repository.modules.ConnectionParamsSetDao;
import com.zw.platform.repository.modules.OrgInspectionExtraUserDAO;
import com.zw.platform.repository.oilsubsidy.StatisticalCheckOfLocationInformationDao;
import com.zw.platform.repository.realTimeVideo.VideoFlowDao;
import com.zw.platform.repository.vas.RiskEventDao;
import com.zw.platform.service.connectionparamsset_809.ConnectionParamsSetService;
import com.zw.platform.service.driverDiscernManage.DriverDiscernManageService;
import com.zw.platform.service.realTimeVideo.ResourceListService;
import com.zw.platform.service.realTimeVideo.VideoService;
import com.zw.platform.service.reportManagement.DriverDiscernStatisticsService;
import com.zw.platform.service.reportManagement.DrivingRecordReportService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.service.reportManagement.MediaService;
import com.zw.platform.service.reportManagement.SuperPlatformMsgService;
import com.zw.platform.service.systems.ParameterService;
import com.zw.platform.util.CommonTypeUtils;
import com.zw.platform.util.CommonUtil;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.ConvertUtil;
import com.zw.platform.util.CosUtil;
import com.zw.platform.util.FileUtil;
import com.zw.platform.util.LocalDateUtils;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.DelayedEventTrigger;
import com.zw.platform.util.common.FTPConfig;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.ffmpeg.FFmpegCommandRunner;
import com.zw.platform.util.ffmpeg.VideoFile;
import com.zw.platform.util.paas.PaasCloudUrlUtil;
import com.zw.platform.util.report.PaasCloudHBaseAccessEnum;
import com.zw.protocol.msg.IcCardMessage;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.MsgDesc;
import com.zw.protocol.msg.RtpMessage;
import com.zw.protocol.msg.VideoMessage;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.RegistrationInfo;
import com.zw.protocol.msg.t808.body.T808GpsInfo;
import com.zw.protocol.msg.t809.T809Message;
import com.zw.protocol.msg.t809.T809MsgBody;
import com.zw.protocol.msg.t809.T809MsgHead;
import com.zw.protocol.msg.t809.body.AlarmTypeCountInfo;
import com.zw.protocol.msg.t809.body.ExchangeInfo;
import com.zw.protocol.msg.t809.body.FaceRecognitionData;
import com.zw.protocol.msg.t809.body.FaceRecognitionDataZw;
import com.zw.protocol.msg.t809.body.MainVehicleInfo;
import com.zw.protocol.msg.t809.body.SupervisionAlarmInfo;
import com.zw.protocol.msg.t809.body.module.CheckAck;
import com.zw.protocol.msg.t809.body.module.EnterpriseAddedleAck;
import com.zw.protocol.msg.t809.body.module.EnterpriseInfo;
import com.zw.protocol.msg.t809.body.module.PlatformAlarmAck;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.protocol.netty.client.service.ClientMessageCleaner;
import com.zw.protocol.netty.client.service.ClientMessageService;
import com.zw.protocol.util.ProtocolTypeUtil;
import com.zw.ws.entity.OilSupplementRequestData;
import org.apache.bval.jsr.util.IOs;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static com.zw.platform.util.ConstantUtil.CONNECT_PROTOCOL_TYPE_808_2019;

/**
 * Created by LiaoYuecai on 2017/7/5.
 */
@Component
public class WebClientHandleCom {
    private static final Logger log = LogManager.getLogger(WebClientHandleCom.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    @Autowired
    private ClientMessageCleaner clientMessageCleaner;

    @Autowired
    private ClientMessageService clientMessageService;

    @Autowired
    private DeviceMessageHandler deviceMessageHandler;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private VideoFlowDao videoFlowDao;

    @Autowired
    private ConnectionParamsSetDao connectionParamsSetDao;

    @Autowired
    private SimpMessagingTemplateUtil simpMessagingTemplateUtil;

    @Autowired
    private ResourceListService resourceListService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private ConnectionParamsSetService connectionParamsSetService;

    @Autowired
    private NewProfessionalsDao newProfessionalsDao;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private SuperPlatformMsgService superPlatformMsgService;

    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Autowired
    private RiskEventDao riskEventDao;

    @Autowired
    private DeliveryLineService deliveryLineService;

    @Autowired
    private BusinessScopeService businessScopeService;

    @Autowired
    private AdasRiskService adasRiskService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DelayedEventTrigger trigger;

    @Value("${ftp.username}")
    private String ftpUserName;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Value("${ftp.path}")
    private String ftpPath;

    @Value("${adas.professionalFtpPath}")
    private String professionalFtpPath;

    /**
     * ??????https???????????????
     */
    @Value("${fdfs.webServerUrl}")
    private String webServerUrl;

    /**
     * 1507??????????????????????????????
     */
    @Value("${adas.mediaServer}")
    private String mediaServer;

    /**
     * ??????809??????
     */
    @Value("${fdfs.webServerUrl}")
    private String webServerUrl809;

    /*????????????1404?????????ftp*/
    @Value("${chuan.ftp.username}")
    private String chuanFtpUserName;

    @Value("${chuan.ftp.password}")
    private String chuanFtpPassword;

    @Value("${chuan.ftp.host}")
    private String chuanFtpHost;

    @Value("${chuan.ftp.port}")
    private int chuanFtpPort;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private ServerParamList serverParamList;

    @Autowired
    private DrivingRecordReportService drivingRecordReportService;

    @Autowired
    private AdasNettyHandleCom adasNettyHandleCom;

    @Autowired
    private AdasSubcibeTable adasSubcibeTable;

    @Autowired
    private AdasElasticSearchService adasElasticSearchService;

    @Autowired
    private ConnectionParamsConfigDao connectionParamsConfigDao;

    @Autowired
    private NewConfigDao newConfigDao;

    @Autowired
    private DriverDiscernManageService driverDiscernManageService;

    @Autowired
    private StatisticalCheckOfLocationInformationDao statisticalCheckOfLocationInformationDao;

    @Autowired
    private OrgInspectionExtraUserDAO orgInspectionExtraUserDAO;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private WebSocketMessageDispatchCenter dispatchCenter;

    @Autowired
    private DriverDiscernStatisticsService driverDiscernStatisticsService;

    public static final int ONE_DAY_REDIS_EXPIRE = 24 * 60 * 60;

    /**
     * Ox0l:jpg Ox02:gif Ox03:png 0x04:wav 0x05:mp3 0x06:mp4 0x07:3gp 0x08:flv
     */
    private static final HashMap<String, Integer> FILE_FORMAT_MAP = new HashMap<>();

    static {
        FILE_FORMAT_MAP.put("jpg", 1);
        FILE_FORMAT_MAP.put("gif", 2);
        FILE_FORMAT_MAP.put("png", 3);
        FILE_FORMAT_MAP.put("wav", 4);
        FILE_FORMAT_MAP.put("mp3", 5);
        FILE_FORMAT_MAP.put("mp4", 6);
        FILE_FORMAT_MAP.put("3gp", 7);
        FILE_FORMAT_MAP.put("flv", 8);
        FILE_FORMAT_MAP.put("bin", 9);
    }

    /**
     * ??????????????????
     */
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    public void handle(Message message) {
        try {
            if (message.getDesc() == null) {
                return;
            }
            //??????handle?????????????????????500???????????????????????????????????????
            long startTime = System.currentTimeMillis();
            Integer msgID = message.getDesc().getMsgID();
            Integer messageType = message.getDesc().getMessageType();
            if (messageType == 2) {
                switch (msgID) {
                    case ConstantUtil.T809_UP_CONNECT_REQ:
                    case ConstantUtil.T809_UP_CONNECT_RSP:
                    case ConstantUtil.T809_UP_DISCONNECT_REQ:
                    case ConstantUtil.T809_UP_DISCONNECT_RSP:
                    case ConstantUtil.T809_UP_LINKTEST_RSP:
                    case ConstantUtil.T809_UP_DISCONNECT_INFORM:
                    case ConstantUtil.T809_UP_CLOSELINK_INFORM:
                    case ConstantUtil.T809_DOWN_CONNECT_REQ:
                    case ConstantUtil.T809_DOWN_CONNECT_RSP:
                    case ConstantUtil.T809_DOWN_DISCONNECT_REQ:
                    case ConstantUtil.T809_DOWN_DISCONNECT_RSP:
                    case ConstantUtil.T809_DOWN_LINKTEST_REQ:
                    case ConstantUtil.T809_DOWN_LINKTEST_RSP:
                    case ConstantUtil.T809_DOWN_DISCONNECT_INFORM:
                    case ConstantUtil.T809_DOWN_CLOSELINK_INFORM:
                    case ConstantUtil.T809_DOWN_WARN_MSG:
                    case ConstantUtil.T809_DOWN_ENTERPRISE_ON_DUTY_REQ: // ????????????????????????
                    case ConstantUtil.T809_DOWN_ENTERPRISE_HANDLE_REQ: // ????????????????????????
                    case ConstantUtil.WEB_809_CHECK_SERVER_STATUS_RSP:
                    case ConstantUtil.T809_DOWN_PREVENTION_MSG:
                    case ConstantUtil.T809_DOWN_TOTAL_RECV_BACK_MSG:
                    case ConstantUtil.T809_DOWN_EXG_MSG:
                        sendCheckMsg(message);
                        break;
                    case ConstantUtil.T809_DOWN_PLATFORM_MSG_INFO_REQ:
                    case ConstantUtil.T809_DOWN_PLATFORM_MSG:
                    case ConstantUtil.T809_DOWN_PLATFORM_MSG_POST_QUERY_REQ:
                        sendPlantInspect(message);
                        sendCheckMsg(message);
                        break;
                    case ConstantUtil.T809_DOWN_BASE_MSG:
                        supplementGroupStaticInfo(message);
                        break;
                    case ConstantUtil.T809_UP_EXG_MSG_FACE_PHOTO_REQ: // ??????????????????
                        reportFaceRecognition(message);
                        break;
                    default:
                        break;
                }
            } else {
                switch (msgID) {
                    case ConstantUtil.DEVICE_UPLOAD_RIDERSHIP:// ????????????????????????
                        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                            clientMessageService.saveRiderShipAckLog(message);
                        }
                        break;
                    case ConstantUtil.DEVICE_UPLOAD_VIDEO_PARAM:// ???????????????????????????
                        clientMessageService.saveVideoParamAckLog(message);
                        break;
                    case ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK:
                        clientMessageCleaner.getResourceList(JSON.toJSONString(message));
                        break;
                    case ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK_BEFOR:
                        clientMessageCleaner.getResourceDateList(JSON.toJSONString(message));
                        break;
                    case ConstantUtil.T808_GPS_INFO_ACK:
                        JSONObject msgBody =
                            JSONObject.parseObject(message.getData().toString()).getJSONObject("msgBody");
                        if (msgBody.containsKey("msgSNAck")) {
                            Thread.sleep(1000);
                            String user = UserCache.getUserInfo(msgBody.getString("msgSNAck"));
                            if (user != null && !"null".equals(user)) {
                                simpMessagingTemplate
                                    .convertAndSendToUser(user, ConstantUtil.WEBSOCKET_REAL_LOCATION_P, message);
                                UserCache.remove(msgBody.getString("msgSNAck"));
                            }
                        }
                        clientMessageCleaner.getVehicleLastLocation(JSON.toJSONString(message));
                        break;
                    case ConstantUtil.T808_GPS_INFO:
                    case ConstantUtil.T808_VEHICLE_CONTROL_ACK:
                        clientMessageCleaner.getLocationInfo(message);
                        break;
                    case ConstantUtil.BDTD_LOCATION:
                        clientMessageCleaner.bdtdLocationMessage(JSON.toJSONString(message));
                        break;
                    case ConstantUtil.T808_DEVICE_GE_ACK:// ????????????
                        clientMessageService.currencyAnswer(message);
                        break;
                    case ConstantUtil.T808_DATA_PERMEANCE_REPORT://????????????
                        //??????????????????????????????
                        clientMessageService.handleRemoteUpgradePermeanceData(message);
                        //??????????????????
                        clientMessageService.saveDataPermeanceLog(message);
                        break;
                    case ConstantUtil.T808_PARAM_ACK://
                        clientMessageService.saveDevieParamAckLog(message);
                        break;
                    case ConstantUtil.WEB_DEVICE_OFF_LINE:// ????????????
                        deviceMessageHandler.deviceOffLineHandler(message.getDesc());
                        break;
                    case ConstantUtil.BS_CLIENT_REQUEST_VEHICLE_CACHE_ADD_INTO:// ????????????????????????
                        String monitorId = message.getDesc().getMonitorId();
                        RedisKey offlineKey = HistoryRedisKeyEnum.MONITOR_OFFLINE.of(monitorId);
                        RedisKey alarmKey = HistoryRedisKeyEnum.MONITOR_ALARMING.of(monitorId);
                        RedisHelper.delete(Arrays.asList(offlineKey, alarmKey));
                        dispatchCenter.pushCacheStatusNew(message);
                        break;
                    case ConstantUtil.BS_CLIENT_REQUEST_VEHICLE_CACHE_UP_INTO:// ????????????????????????
                        dispatchCenter.pushCacheStatusNew(message);
                        break;
                    case ConstantUtil.T808_MULTIMEDIA_DATA:
                        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                            multimedia(message);
                        }
                        break;
                    case ConstantUtil.T808_ATTR_ACK:
                        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                        JSONObject body = (JSONObject) t808Message.getMsgBody();
                        deviceProperty(message.getDesc().getDeviceNumber(), body, message);
                        break;
                    case ConstantUtil.T808_E_AWB:
                        deviceMessageHandler.saveElectornicWayBillLog(message);
                        break;
                    case ConstantUtil.T808_DRIVER_INFO:
                        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                            MsgDesc desc = message.getDesc();
                            String deviceNumber = desc.getDeviceNumber();
                            IcCardMessage icCardMessage =
                                new IcCardMessage(deviceNumber, message, ConstantUtil.T808_DRIVER_INFO);
                            handleIcCardMessage(icCardMessage);
                        }
                        break;
                    case ConstantUtil.T808_DRIVER_IDENTIFY:
                        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                            deviceMessageHandler.saveDriverIdentify(message);
                        }
                        break;
                    case ConstantUtil.T808_FLATFORM_INSPECTION_ACK:
                        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                            deviceMessageHandler.savePlatformInspection(message);
                        }
                        break;
                    case ConstantUtil.T808_INFO_MANAGER:
                        deviceMessageHandler.saveInformationDemandOrCancelLog(message);
                        break;
                    case ConstantUtil.T808_ANSWER:
                        deviceMessageHandler.saveQuestionResponseLog(message);
                        break;
                    case ConstantUtil.T808_E_REPORT:// ????????????
                        deviceMessageHandler.saveEventReportLog(message);
                        break;
                    case ConstantUtil.T808_UPLOAD_ACK:
                        deviceMessageHandler.saveUploadAckLog(message);
                        break;
                    case ConstantUtil.T808_BATCH_GPS_INFO:
                        T808Message t808Messages = JSON.parseObject(message.getData().toString(), T808Message.class);
                        T808GpsInfo info = JSON.parseObject(t808Messages.getMsgBody().toString(), T808GpsInfo.class);
                        JSONObject s = info.getGpsInfos().getJSONObject(info.getGpsInfos().size() - 1);
                        info = JSON.parseObject(String.valueOf(s.getJSONObject("body")), T808GpsInfo.class);
                        t808Messages.setMsgBody(info);
                        message.setData(t808Messages);
                        clientMessageCleaner.getLocationInfo(message);
                        break;
                    case ConstantUtil.ADAS_UP_EXG_MSG_RETURN_END_ACK:
                        receive1206Msg(message);
                        break;
                    case ConstantUtil.T808_RSP_MEDIA_STORAGE_FTP_1208:
                        adasNettyHandleCom.deal1208Message(message);
                        break;
                    case ConstantUtil.T808_DRIVER_RECORD_UPLOAD:
                        updateRecordCollectionLog(message);
                        break;
                    case ConstantUtil.T808_REGISTER:
                        // ?????????????????????????????????????????????????????????
                        updateRegionalInfo(message);
                        break;
                    case ConstantUtil.T808_MULTIMEDIA_SEARCH_ACK:
                        // ?????????????????????????????????
                        deviceMessageHandler.saveMultimediaDataSearchLog(message);
                        break;
                    case ConstantUtil.T808_FENCE_QUERY_RESP:
                        // ?????????????????????????????????
                        deviceMessageHandler.dealFenceQueryResp(message);
                        break;
                    case ConstantUtil.T808_REQ_MEDIA_UPDATE:
                        receive1507Msg(message);
                        break;
                    case ConstantUtil.DRIVER_IDENTIFICATION_REPORT:
                        // ???????????????????????????
                        driverDiscernStatisticsService.saveReportHandle(message);
                        break;
                    case ConstantUtil.QUERY_DEVICE_DRIVER_REQ_ACK:
                    case ConstantUtil.QUERY_DEVICE_DRIVER_REQ_HUNAN_ACK:
                        //????????????????????? ??????????????????
                        driverDiscernManageService.sendQueryAckHandle(message);
                        break;
                    default:
                        break;
                }
            }
            //??????handle?????????????????????500???????????????????????????????????????
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            if (costTime > 500) {
                log.info("??????handler??????????????????500ms, ??????ID: {}, ??????: {}, ???????????????{}", String.format("0x%04X", msgID), costTime,
                    message);
            }

        } catch (Exception e) {
            log.error("????????????????????????", e);
        }
    }

    /**
     * ?????????????????????????????????
     */
    private void receive1507Msg(Object obj) {
        // ????????????0x9507
        if (!(obj instanceof Message)) {
            log.info("0x9507????????????????????????");
            return;
        }
        Message m = (Message) obj;
        JSONObject msgBody = JSONObject.parseObject(m.getData().toString()).getJSONObject("msgBody");
        MsgDesc desc = m.getDesc();
        JSONObject msgHead = JSONObject.parseObject(m.getData().toString()).getJSONObject("msgHead");
        // ?????????
        String msgSNACK = msgHead.getString("msgSN");
        String vehicleId = desc.getMonitorId();
        String deviceId = desc.getDeviceId();
        String certificationId = msgBody.getString("certificationId");
        certificationId = StringUtil.getStartWithOutZeroString(certificationId);
        if (StringUtils.isBlank(certificationId)) {
            log.info("0x9507?????????????????????????????????????????????????????????");
            return;
        }
        if (certificationId.length() > 20) {
            log.info("0x9507?????????????????????????????????????????????????????????");
            return;
        }
        String version = msgBody.getString("version");
        int result = 0x01;
        Map<String, String> configMap;
        String simCardNumber;
        if (StringUtils.isBlank(msgSNACK)) {
            log.info("0x9507????????????????????????????????????????????????");
            return;
        }
        if (StringUtils.isBlank(version)) {
            log.info("0x9507??????????????????????????????????????????????????????");
            return;
        }
        if (StringUtils.isBlank(vehicleId)) {
            log.info("??????0x1507?????????????????????????????????????????????????????????");
            return;
        }
        String vc = RedisHelper.getString(HistoryRedisKeyEnum.CARD_NUM_PREFIX.of(vehicleId));
        String photo;
        //??????????????????????????????????????????
        if (StringUtils.isEmpty(vc)) {
            //?????????????????????????????????????????????????????????
            configMap = getPhotoPath(vehicleId, certificationId, version);
            if (MapUtils.isEmpty(configMap)) {
                log.info("0x9507????????????????????????????????????????????????????????????");
                return;
            }
            photo = configMap.get("photograph");
            simCardNumber = configMap.get("simCardNumber");
        } else {
            String[] cardIdAndName = vc.split(",")[0].split("_");
            ProfessionalDO professionalDO =
                newProfessionalsDao.findByNameExistIdentity(cardIdAndName[1], cardIdAndName[0]);
            //????????????????????????????????????????????????????????????????????????????????????
            if (professionalDO == null || !certificationId.equals(professionalDO.getCardNumber())) {
                configMap = getPhotoPath(vehicleId, certificationId, version);
                if (MapUtils.isEmpty(configMap)) {
                    log.info("0x9507????????????????????????????????????????????????????????????");
                    return;
                }
                photo = configMap.get("photograph");
                simCardNumber = configMap.get("simCardNumber");
            } else {
                //?????????????????????????????????
                photo = newProfessionalsDao.getPhotoByCardNumberAndNameAndVersion(cardIdAndName[0], cardIdAndName[1]);
                if (StringUtils.isNotBlank(photo)) {
                    String photoVersion = photo.split("_")[1].split("\\.")[0];
                    //??????  ??????????????????  ??????result ???0x00
                    if (version.equals(photoVersion)) {
                        result = 0x00;
                        photo = "";
                    }
                } else {
                    //?????????????????????????????????  ???????????????????????????????????????~
                    log.info("0x9507????????????????????????????????????????????????????????????");
                    return;
                }
                BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId, Lists.newArrayList("simCardNumber"));
                simCardNumber = bindDTO == null ? null : bindDTO.getSimCardNumber();
            }
        }
        if (StringUtils.isBlank(photo)) {
            result = 0x00;
        } else {
            //???????????????  ???????????????????????? ??? ??????????????????~
            version = photo.split("_")[1].split("\\.")[0];
            photo = mediaServer + professionalFtpPath + photo;
        }
        ProfessionalsRequestAcK ack = ProfessionalsRequestAcK.getInstance(certificationId, version, photo, result);
        //??????9507??????  ????????????????????????
        T808Message message = MsgUtil
            .get808Message(simCardNumber, ConstantUtil.T808_UP_CTRL_MSG_PHOTO_UPDATE_ACK, Integer.parseInt(msgSNACK),
                ack);
        WebSubscribeManager.getInstance()
            .sendMsgToAll(message, ConstantUtil.T808_UP_CTRL_MSG_PHOTO_UPDATE_ACK, deviceId);
    }

    /**
     * ????????????id??? ????????????????????? ??????????????????????????? ??????????????????
     * @param vid        ??????id
     * @param cardNumber ??????????????????
     * @param version    ????????????????????????
     * @return map m
     */
    private Map<String, String> getPhotoPath(String vid, String cardNumber, String version) {
        Map<String, String> configMap = newConfigDao.getConfigByPidAndVid(cardNumber, vid);
        if (MapUtils.isNotEmpty(configMap)) {
            String photoVersion = configMap.get("photograph").split("_")[1].split("\\.")[0];
            if (version.equals(photoVersion)) {
                configMap.put("photograph", "");
            }
            return configMap;
        }
        return null;
    }

    /**
     * ????????????,????????????: "???????????????xxxx????????????{???????????????}??????"
     * @param message message
     */
    private void updateRecordCollectionLog(Message message) {
        MsgDesc msgDesc = message.getDesc();
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        JSONObject body = (JSONObject) t808Message.getMsgBody();
        Integer cw = (Integer) body.get("cw");
        // ???????????????
        Integer msgSNAck = body.getInteger("msgSNAck");
        String deviceId = msgDesc.getDeviceId();
        String vehicleId = msgDesc.getMonitorId();
        // ????????????WEB????????????????????????
        if (Objects.nonNull(msgSNAck) && StringUtils.isNotBlank(vehicleId) && StringUtils.isNotEmpty(deviceId)) {
            SubscibeInfo subscibeInfo = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSNAck, deviceId);
            String userName = subscibeInfo.getUserName();
            OrganizationLdap organizationLdap =
                organizationService.getOrgByEntryDn(userService.getUserOrgDnByDn(userName));
            Optional<OrganizationLdap> organizationLdapOptional = Optional.ofNullable(organizationLdap);
            LogSearchForm logSearchForm = new LogSearchForm();
            try {
                String commonSign = Integer.toHexString(cw) + "H";
                String messageStr = buildMessage(body, commonSign);
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchForm.setBrand(vehicle[0]);
                logSearchForm.setGroupId(organizationLdapOptional.orElse(new OrganizationLdap()).getUuid());
                logSearchForm.setPlateColor(Integer.valueOf(vehicle[1]));
                logSearchForm.setLogSource("1");
                logSearchForm.setEventDate(new Date());
                logSearchForm.setModule("MONITORING");
                String monitoringOperationStr =
                    "???????????????" + vehicle[0] + " " + RecordCollectionEnum.getSignContentBy(commonSign) + "??????";
                logSearchForm.setMonitoringOperation(monitoringOperationStr);
                logSearchForm.setMessage(messageStr);
                logSearchService.addLogBean(logSearchForm);
                // ??????
                simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_LOG_TOPIC, message);
                SubscibeInfoCache.getInstance().delTable(subscibeInfo);
                DrivingRecordInfoQuery query = new DrivingRecordInfoQuery();
                query.setMonitorId(vehicleId);
                query.setCollectionCommand(commonSign);
                query.setMsgSNAck(msgSNAck);
                query.setMessage(messageStr);
                drivingRecordReportService.updateDrivingRecordInfoByMonitorId(query);
            } catch (Exception e) {
                log.error("????????????????????????????????????!", e);
            }
        }
    }

    /**
     * ?????????????????? ??????????????????
     * @param messageBody messageBody
     * @param commonSign  ?????????
     * @return monitoringOperation
     */
    private String buildMessage(JSONObject messageBody, String commonSign) {
        JSONObject driverRecord = messageBody.getJSONObject("driverRecord");
        JSONObject driverRecordData = driverRecord.getJSONObject("data");
        StringBuilder messageBuilder = new StringBuilder();
        switch (commonSign) {
            case "0H":
                Integer year = driverRecordData.getInteger("year");
                Integer number = driverRecordData.getInteger("number");
                messageBuilder.append("??????????????????????????????2???: ").append(Objects.nonNull(year) ? year : 3).append("<br/>");
                messageBuilder.append("????????????: ").append(Objects.nonNull(number) ? number : "00H");
                break;
            case "1H":
                String drivingID = driverRecordData.getString("drivingID");
                messageBuilder.append("????????????????????????: ").append(StringUtils.isNotEmpty(drivingID) ? drivingID : "00H");
                break;
            case "2H":
                String time = driverRecordData.getString("time");
                messageBuilder.append("????????????: ").append(LocalDateUtils.timeScopeFormatter(time, "20"));
                break;
            case "3H":
                String logTime = driverRecordData.getString("logTime");
                String installTime = driverRecordData.getString("installTime");
                String startMileage = driverRecordData.getString("startMileage");
                String mileageSum = driverRecordData.getString("mileageSum");
                messageBuilder.append("?????????????????????: ").append(LocalDateUtils.timeScopeFormatter(logTime, "20"))
                    .append("</br>").append("???????????????????????????: ").append(LocalDateUtils.timeScopeFormatter(installTime, "20"))
                    .append("</br>").append("????????????: ")
                    .append(new BigDecimal(startMileage).divide(new BigDecimal(10), 1, BigDecimal.ROUND_HALF_UP))
                    .append("</br>").append("??????????????????: ")
                    .append(new BigDecimal(mileageSum).divide(new BigDecimal(10), 1, BigDecimal.ROUND_HALF_UP));
                break;
            case "4H":
                String sysTime = driverRecordData.getString("sysTime");
                Integer pulseCoefficient = driverRecordData.getInteger("pulseCoefficient");
                messageBuilder.append("?????????????????????: ").append(LocalDateUtils.timeScopeFormatter(sysTime, "20"))
                    .append("</br>").append("?????????????????????: ").append(pulseCoefficient);
                break;
            case "5H":
                String signID = driverRecordData.getString("signID");
                String plateLicense = driverRecordData.getString("plateLicense");
                String plateType = driverRecordData.getString("plateType");
                messageBuilder.append("?????????????????? : ").append(signID).append("</br>").append("????????????????????? : ")
                    .append(plateLicense).append("</br>").append("????????????????????? : ").append(plateType);
                break;
            case "6H":
                String sysTime1 = driverRecordData.getString("sysTime");
                // ???????????????????????? 1 ??????????????????0???????????????
                int status = driverRecordData.getIntValue("status");
                messageBuilder.append("????????????????????? : ").append(LocalDateUtils.timeScopeFormatter(sysTime1, "20"))
                    .append("</br>");
                JSONArray data = driverRecordData.getJSONArray("data");
                messageBuilder.append("????????????????????????: ").append(status).append("<br>");
                for (int i = 0; i < data.size(); i++) {
                    JSONObject jsonObject = data.getJSONObject(i);
                    String d0Name = jsonObject.getString("d0Name");
                    String d1Name = jsonObject.getString("d1Name");
                    String d2Name = jsonObject.getString("d2Name");
                    String d3Name = jsonObject.getString("d3Name");
                    String d4Name = jsonObject.getString("d4Name");
                    String d5Name = jsonObject.getString("d5Name");
                    String d6Name = jsonObject.getString("d6Name");
                    String d7Name = jsonObject.getString("d7Name");
                    if (StringUtils.isNotEmpty(d0Name)) {
                        Integer d0Status = ConvertUtil.binaryIntegerWithOne(status, 0);
                        messageBuilder.append(d0Status == 1 ? "???" : "???").append(d0Name).append(",");
                    }
                    if (StringUtils.isNotEmpty(d1Name)) {
                        Integer d1Status = ConvertUtil.binaryIntegerWithOne(status, 1);
                        messageBuilder.append(d1Status == 1 ? "???" : "???").append(d1Name).append(",");
                    }
                    if (StringUtils.isNotEmpty(d2Name)) {
                        Integer d2Status = ConvertUtil.binaryIntegerWithOne(status, 2);
                        messageBuilder.append(d2Status == 1 ? "???" : "???").append(d2Name).append(",");
                    }
                    if (StringUtils.isNotEmpty(d3Name)) {
                        Integer d3Status = ConvertUtil.binaryIntegerWithOne(status, 3);
                        messageBuilder.append(d3Status == 1 ? "???" : "???").append(d3Name).append(",");
                    }
                    if (StringUtils.isNotEmpty(d4Name)) {
                        Integer d4Status = ConvertUtil.binaryIntegerWithOne(status, 4);
                        messageBuilder.append(d4Status == 1 ? "???" : "???").append(d4Name).append(",");
                    }
                    if (StringUtils.isNotEmpty(d5Name)) {
                        Integer d5Status = ConvertUtil.binaryIntegerWithOne(status, 5);
                        messageBuilder.append(d5Status == 1 ? "???" : "???").append(d5Name).append(",");
                    }
                    if (StringUtils.isNotEmpty(d6Name)) {
                        Integer d6Status = ConvertUtil.binaryIntegerWithOne(status, 6);
                        messageBuilder.append(d6Status == 1 ? "???" : "???").append(d6Name).append(",");
                    }
                    if (StringUtils.isNotEmpty(d7Name)) {
                        Integer d7Status = ConvertUtil.binaryIntegerWithOne(status, 7);
                        messageBuilder.append(d7Status == 1 ? "???" : "???").append(d7Name).append(",");
                    }
                }
                break;
            case "7H":
                String producerCCCAuthCOde = driverRecordData.getString("producerCCCAuthCOde");
                String authProductModel = driverRecordData.getString("authProductModel");
                String productDate = driverRecordData.getString("productDate");
                String productSN = driverRecordData.getString("productSN");
                messageBuilder.append("?????????CCC????????????: ").append(producerCCCAuthCOde).append("</br>").append("??????????????????: ")
                    .append(authProductModel).append("</br>").append("????????????????????????: ")
                    .append(LocalDateUtils.dateFormat(LocalDateUtils.localDateParse("20" + productDate)))
                    .append("</br>").append("?????????????????????: ").append(productSN);
                break;
            case "8H":
                int sum8H = driverRecordData.getIntValue("sum");
                if (sum8H > 0) {
                    JSONArray data8H = driverRecordData.getJSONArray("data");
                    for (int i = 0; i < data8H.size(); i++) {
                        JSONObject detailsObj = data8H.getJSONObject(i);
                        String startTime = detailsObj.getString("startTime");
                        messageBuilder.append("????????????: ").append(LocalDateUtils.timeScopeFormatter(startTime, "20"))
                            .append("</br>");

                        JSONArray details = detailsObj.getJSONArray("details");
                        if (CollectionUtils.isNotEmpty(details)) {
                            for (int j = 0; j < details.size(); j++) {
                                JSONObject jsonObject = details.getJSONObject(j);
                                String speed = jsonObject.getString("speed");
                                String status8h = jsonObject.getString("status");
                                messageBuilder.append("<span>&nbsp;&nbsp;&nbsp;</span>").append("????????????: ").append(speed)
                                    .append(",").append("????????????: ").append(status8h).append("</br>");
                            }
                        }
                    }
                }
                break;
            case "9H":
                int sum9H = driverRecordData.getIntValue("sum");
                if (sum9H > 0) {
                    JSONArray data9H = driverRecordData.getJSONArray("data");
                    for (int i = 0; i < data9H.size(); i++) {
                        JSONObject detailsObj = data9H.getJSONObject(i);
                        String startTime = detailsObj.getString("startTime");
                        messageBuilder.append("????????????: ").append(LocalDateUtils.timeScopeFormatter(startTime, "20"))
                            .append("</br>");

                        JSONArray details = detailsObj.getJSONArray("detail");
                        if (CollectionUtils.isNotEmpty(details)) {
                            for (int j = 0; j < details.size(); j++) {
                                JSONObject jsonObject = details.getJSONObject(j);
                                Integer latitude = jsonObject.getInteger("latitude");
                                Integer longitude = jsonObject.getInteger("longitude");
                                Integer height = jsonObject.getInteger("height");
                                Integer speed = detailsObj.getInteger("speed");
                                messageBuilder.append("<span>&nbsp;&nbsp;&nbsp;</span>").append("??????: ")
                                    .append(Objects.nonNull(longitude) ? longitude : 0).append(",").append("??????: ")
                                    .append(Objects.nonNull(latitude) ? longitude : 0).append(",").append("????????????: ")
                                    .append(Objects.nonNull(height) ? longitude : 0).append(",").append("??????: ")
                                    .append(Objects.nonNull(speed) ? speed : 0).append("</br>");
                            }
                        }
                    }
                } else {
                    messageBuilder.append("???");
                }
                break;
            case "10H":
                int sum10H = driverRecordData.getIntValue("sum");
                if (sum10H > 0) {
                    JSONArray data10H = driverRecordData.getJSONArray("data");
                    for (int i = 0; i < data10H.size(); i++) {
                        JSONObject detailsObj = data10H.getJSONObject(i);
                        String runOverTime = detailsObj.getString("runOverTime");
                        String driving10ID = detailsObj.getString("drivingID");
                        messageBuilder.append("??????????????????: ").append(LocalDateUtils.timeScopeFormatter(runOverTime, "20"))
                            .append("</br>").append("????????????????????????: ").append(driving10ID).append("</br>");

                        JSONObject gpsInfo = detailsObj.getJSONObject("gpsInfo");
                        Integer latitude = gpsInfo.getInteger("latitude");
                        Integer longitude = gpsInfo.getInteger("longitude");
                        Integer height = gpsInfo.getInteger("height");
                        messageBuilder.append("??????: ").append(longitude).append("</br>").append("??????: ").append(latitude)
                            .append("</br>").append("????????????: ").append(height).append("</br>");

                        JSONArray details = detailsObj.getJSONArray("detail");
                        if (CollectionUtils.isNotEmpty(details)) {
                            for (int j = 0; j < details.size(); j++) {
                                JSONObject jsonObject = details.getJSONObject(j);
                                Integer speed = jsonObject.getInteger("speed");
                                Integer status10h = jsonObject.getInteger("status");
                                messageBuilder.append("<span>&nbsp;&nbsp;&nbsp;</span>").append("??????: ").append(speed)
                                    .append(",").append("????????????: ").append(status10h).append("</br>");
                            }
                        }
                    }
                } else {
                    messageBuilder.append("???");
                }
                break;
            case "11H":
                int sum11H = driverRecordData.getIntValue("sum");
                if (sum11H > 0) {
                    JSONArray dat11H = driverRecordData.getJSONArray("data");
                    for (int i = 0; i < dat11H.size(); i++) {
                        JSONObject detailsObj = dat11H.getJSONObject(i);
                        String driving11ID = detailsObj.getString("drivingID");
                        String startTime = detailsObj.getString("startTime");
                        String endTime = detailsObj.getString("endTime");
                        JSONObject startGpsInfo = detailsObj.getJSONObject("startGpsInfo");
                        JSONObject endGpsInfo = detailsObj.getJSONObject("endGpsInfo");

                        Integer startLatitude = startGpsInfo.getInteger("latitude");
                        Integer startLongitude = startGpsInfo.getInteger("longitude");
                        Integer startHeight = startGpsInfo.getInteger("height");

                        Integer endLatitude = endGpsInfo.getInteger("latitude");
                        Integer endLongitude = endGpsInfo.getInteger("longitude");
                        Integer endHeight = endGpsInfo.getInteger("height");

                        messageBuilder.append("????????????????????????: ").append(driving11ID).append("</br>").append("????????????????????????: ")
                            .append(LocalDateUtils.timeScopeFormatter(startTime, "20")).append("</br>")
                            .append("????????????????????????: ").append(LocalDateUtils.timeScopeFormatter(endTime, "20"))
                            .append("</br>").append("???????????????????????????????????????????????????????????????: ").append("<br/>")
                            .append("<span>&nbsp;&nbsp;&nbsp;</span>").append("??????: ").append(startLongitude).append(",")
                            .append("??????: ").append(startLatitude).append(",").append("????????????: ").append(startHeight)
                            .append("</br>").append("???????????????????????????????????????????????????????????????: ").append("<br/>")
                            .append("<span>&nbsp;&nbsp;&nbsp;</span>").append("??????: ").append(endLongitude).append(",")
                            .append("??????: ").append(endLatitude).append(",").append("????????????: ").append(endHeight)
                            .append("</br>");
                    }
                } else {
                    messageBuilder.append("???");
                }
                break;
            case "12H":
                int sum12H = driverRecordData.getIntValue("sum");
                if (sum12H > 0) {
                    JSONArray dat12H = driverRecordData.getJSONArray("data");
                    for (int i = 0; i < dat12H.size(); i++) {
                        JSONObject detailsObj = dat12H.getJSONObject(i);
                        String driving12ID = detailsObj.getString("drivingID");
                        String startTime = detailsObj.getString("startTime");
                        int infoType = detailsObj.getIntValue("endTime");

                        messageBuilder.append("??????????????????: ").append(LocalDateUtils.timeScopeFormatter(startTime, "20"))
                            .append("</br>").append("????????????????????????: ").append(driving12ID).append("</br>").append("????????????: ")
                            .append(infoType == 1 ? "??????" : "??????").append("</br>");
                    }
                } else {
                    messageBuilder.append("???");
                }
                break;
            case "13H":
                int sum13H = driverRecordData.getIntValue("sum");
                if (sum13H > 0) {
                    JSONArray dat13H = driverRecordData.getJSONArray("data");
                    for (int i = 0; i < dat13H.size(); i++) {
                        JSONObject detailsObj = dat13H.getJSONObject(i);
                        String infoTime = detailsObj.getString("infoTime");
                        Integer infoType = detailsObj.getInteger("infoType");

                        messageBuilder.append("??????????????????: ").append(LocalDateUtils.timeScopeFormatter(infoTime, "20"))
                            .append("</br>").append("????????????: ").append(infoType == 1 ? "??????" : "??????").append("</br>");
                    }
                } else {
                    messageBuilder.append("???");
                }
                break;
            case "14H":
                int sum14H = driverRecordData.getIntValue("sum");
                if (sum14H > 0) {
                    JSONArray dat14H = driverRecordData.getJSONArray("data");
                    for (int i = 0; i < dat14H.size(); i++) {
                        JSONObject detailsObj = dat14H.getJSONObject(i);
                        String infoTime = detailsObj.getString("infoTime");
                        Integer infoType = detailsObj.getInteger("infoType");

                        messageBuilder.append("??????????????????: ").append(infoTime).append("</br>").append("????????????: ")
                            .append(getInfoType(infoType)).append("</br>");
                    }
                } else {
                    messageBuilder.append("???");
                }
                break;
            case "15H":
                int sum15H = driverRecordData.getIntValue("sum");
                if (sum15H > 0) {
                    JSONArray data15H = driverRecordData.getJSONArray("data");
                    for (int i = 0; i < data15H.size(); i++) {
                        JSONObject detailsObj = data15H.getJSONObject(i);
                        String startTime = detailsObj.getString("startTime");
                        String endTime = detailsObj.getString("endTime");
                        Integer speedStatus = detailsObj.getInteger("speedStatus");
                        messageBuilder.append("????????????????????????: ").append(speedStatus == 1 ? "??????" : "??????").append("</br>")
                            .append("????????????: ").append(LocalDateUtils.timeScopeFormatter(startTime, "20")).append("</br>")
                            .append("????????????: ").append(LocalDateUtils.timeScopeFormatter(endTime, "20")).append("<br/>");

                        JSONArray details = detailsObj.getJSONArray("details");
                        if (CollectionUtils.isNotEmpty(details)) {
                            for (int j = 0; j < details.size(); j++) {
                                JSONObject jsonObject = details.getJSONObject(j);
                                Integer speed = jsonObject.getInteger("speed");
                                Integer consultSpeed = jsonObject.getInteger("consultSpeed");
                                messageBuilder.append("<span>&nbsp;&nbsp;&nbsp;</span>").append("????????????: ").append(speed)
                                    .append(",").append("????????????: ").append(consultSpeed).append("</br>");
                            }
                        }
                    }
                } else {
                    messageBuilder.append("???");
                }
                break;
            default:
                break;
        }
        return StringUtil.cut(messageBuilder.toString(), "", ",");
    }

    private String getInfoType(Integer infoType) {
        String infoTypeStr = "";
        switch (infoType) {
            case 0x82:
                infoTypeStr = "??????????????????";
                break;
            case 0x83:
                infoTypeStr = "?????????????????????????????????";
                break;
            case 0x84:
                infoTypeStr = "???????????????????????????";
                break;
            case 0xC2:
                infoTypeStr = "?????????????????????";
                break;
            case 0xC3:
                infoTypeStr = "??????????????????????????????";
                break;
            case 0xC4:
                infoTypeStr = "??????????????????";
                break;
            default:
                break;
        }
        return infoTypeStr;
    }

    public void videoHandle(VideoMessage videoMessage) {
        if (videoMessage == null) {
            return;
        }
        RtpMessage rtpMessage = JSON.parseObject(JSON.toJSONString(videoMessage.getData()), RtpMessage.class);
        Integer msgID = rtpMessage.getMsgHead().getMsgID();
        switch (msgID) {
            case ConstantUtil.VIDEO_FLOW: // ????????????
                deviceMessageHandler.acceptMediaSteam(rtpMessage);
                break;
            case ConstantUtil.VIDEO_MONITOR_FLOW_CONSUMPTION: // ????????????????????????????????????
                deviceMessageHandler.monitorVideoFlowConsumption(rtpMessage);
                break;
            case ConstantUtil.VIDEO_USER_FLOW_CONSUMPTION: // ??????????????????????????????????????????
                deviceMessageHandler.closeUserVideoPlayLog(rtpMessage);
                break;
            case ConstantUtil.VIDEO_USER_BEGIN_PLAY: // ??????????????????
                deviceMessageHandler.saveUserVideoPlayLog(rtpMessage);
                break;
            case ConstantUtil.FTP_DISK_INFO: // ????????????
                DiskInfo disk = videoService.getDiskInfo();
                deviceMessageHandler.sendftpDisk(rtpMessage, disk);
                break;
            default:
                break;
        }
    }

    private void sendPlantInspect(Object obj) {
        List<String> users = WebSubscribeManager.getInstance().getCheckUsers();
        for (String user : users) {
            simpMessagingTemplate
                .convertAndSendToUser(user, ConstantUtil.WEB_SOCKET_T809_INSPECT, JSON.toJSONString(obj));
        }
    }

    /**
     * ???809?????????????????????
     */
    private void sendCheckMsg(Message message) {
        try {
            Integer msgId = message.getDesc().getMsgID();
            switch (msgId) {
                case ConstantUtil.T809_DOWN_PLATFORM_MSG:
                case ConstantUtil.T809_DOWN_WARN_MSG:
                case ConstantUtil.T809_DOWN_PREVENTION_MSG:
                case ConstantUtil.T809_DOWN_EXG_MSG:
                    saveSuperPlatformMsg(message); // 809??????/???????????????????????????
                    break;
                case ConstantUtil.T809_DOWN_TOTAL_RECV_BACK_MSG:
                    if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
                        save9101OilSubsidyLocationInformation(message);
                    }
                    break;
                default:
                    List<String> users = WebSubscribeManager.getInstance().getCheckUsers();
                    for (String user : users) {
                        simpMessagingTemplate
                            .convertAndSendToUser(user, ConstantUtil.WEB_SOCKET_T809_CHECK, JSON.toJSONString(message));
                    }
                    dealT809OfflineReconnect(message, msgId);
                    break;
            }
        } catch (Exception e) {
            log.error("809??????????????????", e);
        }
    }

    private void dealT809OfflineReconnect(Message message, Integer msgId) {
        if (ConstantUtil.WEB_809_CHECK_SERVER_STATUS_RSP != msgId) {
            return;
        }
        String platId = message.getDesc().getT809PlatId();
        //??????f3??????????????????????????????t809PlatId?????????
        if (StringUtil.isNullOrBlank(platId)) {
            return;
        }
        connectionParamsSetService.pushConnectionStatusByPlatformId(platId);
    }

    // ?????????809????????????????????????????????????
    private void saveSuperPlatformMsg(Message message) throws Exception {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject t809MsgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (t809MsgBody == null) {
            return;
        }
        JSONObject t809MsgBodyData = t809MsgBody.getJSONObject("data");
        if (t809MsgBodyData == null) {
            return;
        }
        // ?????????????????????
        Integer dataType = t809MsgBody.getInteger("dataType");
        switch (dataType) {
            // ??????809????????????
            case ConstantUtil.T809_DOWN_PLATFORM_MSG_POST_QUERY_REQ:
                // ??????
                standard809GangSave(message);
                break;
            // ??????809??????
            case ConstantUtil.T809_DOWN_ENTERPRISE_ON_DUTY_REQ:
                extend809GangSave(message);
                break;
            // ??????809??????
            case ConstantUtil.T809_DOWN_ENTERPRISE_HANDLE_REQ:
                extend809AlarmHandle(message);
                break;
            // ??????809????????????
            case ConstantUtil.T809_DOWN_WARN_MSG_URGE_TODO_REQ:
                standard809HandleSave(message);
                break;
            //????????????????????????
            case ConstantUtil.T809_DOWN_WARN_MSG_FILELIST_REQ:
                alarmFileListAck(message);
                break;
            //??????????????????????????????
            case ConstantUtil.T809_HU_DOWN_WARN_MSG_FILELIST_REQ:
                shanghaiAlarmFileListAck(message);
                break;
            //??????????????????????????????
            case ConstantUtil.T809_DOWN_WARN_MSG_CHECK_RE:
                informationCheckAck(message);
                break;
            //??????????????????????????????
            case ConstantUtil.T809_DOWN_WARN_MSG_STATICS_REQ:
                countAlarmTypeByBrandAndTime(message);
                break;
            //?????? ??????????????????????????????????????????
            case ConstantUtil.T809_DOWN_PREVENTION_MSG_FILELIST_REQ:
                preventionMsgFileListAck(message);
                break;
            case ConstantUtil.T809_DOWN_EXG_MSG_HISTORY_ARCOSSAREA:
            case ConstantUtil.T809_DOWN_EXG_MSG_APPLY_FOR_MONITOR_SARTUP_ACK:
            case ConstantUtil.T809_DOWN_EXG_MSG_APPLY_FOR_MONITOR_END_ACK:
            case ConstantUtil.T809_DOWN_EXG_MSG_APPLY_HISGNSSDATA_ACK:
                send2Web(message);
                if (Objects.equals(dataType, ConstantUtil.T809_DOWN_EXG_MSG_APPLY_HISGNSSDATA_ACK)) {
                    reissueDataRequestResult(message);
                }
                break;
            //????????????????????????????????????????????????
            case ConstantUtil.DOWN_PLATFORM_MSG_INSPECTION_USER_REQ:
                inspectionUserAck(message);
                break;
            //????????????????????????????????????????????????
            case ConstantUtil.DOWN_PLATFORM_MSG_INSPECTION_LOG_REQ:
                inspectionLogAck(message);
                break;
            //????????????????????????????????????
            case ConstantUtil.DOWN_EXG_MSG_DRVLINE_INFO:
                deliveryLineService.addDrvLineInfo(message);
                break;
            default:
                // ??????809????????????
                other809MsgSend(message);
        }
    }

    private void inspectionUserAck(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject t809MsgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (t809MsgBody == null) {
            return;
        }
        JSONObject t809MsgBodyData = t809MsgBody.getJSONObject("data");
        //????????????
        Integer objectType = t809MsgBodyData.getInteger("objectType");
        //????????????id
        String platformId = message.getDesc().getT809PlatId();
        Integer msgSn = t809Message.getMsgHead().getMsgSn();
        //??????Id
        String objectId = t809MsgBodyData.getString("objectId");
        List<PlantParam> info = getTargetInfo(objectType, platformId, objectId);
        if (CollectionUtils.isEmpty(info)) {
            log.info("0x9310??????????????????????????????????????????????????????????????????{}", JSON.toJSONString(t809MsgBodyData));
            return;
        }
        //????????????
        Date expireTime;
        long msgTime = System.currentTimeMillis();
        Long answerTime = t809MsgBodyData.getLong("answerTime");
        if (answerTime != null) {
            long resultAnswerTime = msgTime + answerTime * 1000 * 60;
            expireTime = new Date(resultAnswerTime);
        } else {
            expireTime = getThirtyMinutesLaterDate();
        }
        List<SuperPlatformMsg> list = Lists.newArrayList();
        SuperPlatformMsg msg;
        for (PlantParam plantParam : info) {
            // ???809??????????????????????????????????????????
            msg = new SuperPlatformMsg();
            // ???????????????????????????id
            t809Message.getMsgHead().setHandleId(msg.getId());
            t809Message.getMsgHead().setGroupId(plantParam.getGroupId());
            message.setData(t809Message);
            //   ????????????id
            msg.setPlatformId(plantParam.getId());
            // ??????id
            msg.setGroupId(plantParam.getGroupId());
            // ??????????????????
            msg.setType(4);
            msg.setMsg(JSONObject.toJSONString(message));
            // ??????????????????
            msg.setTime(new Date(msgTime));
            // ????????????(??????????????????,??????30??????)
            msg.setExpireTime(expireTime);
            msg.setAnswerTime(answerTime);
            msg.setSourceMsgSn(msgSn);
            list.add(msg);
            pushInspectUser809Message2Web(message, plantParam.getGroupId(), msg, expireTime);
        }
        superPlatformMsgService.batchSaveSuperPlatformMsg(list);
    }

    /**
     * ????????????????????????????????????
     * @param message mes
     */
    private void save9101OilSubsidyLocationInformation(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject t809MsgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (t809MsgBody == null) {
            return;
        }
        Long dynamicInfoTotal = t809MsgBody.getLong("dynamicInfoTotal");
        Long startTime = t809MsgBody.getLong("startTime");
        Long endTime = t809MsgBody.getLong("endTime");
        // ????????????id
        String platformId = message.getDesc().getT809PlatId();
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(platformId);
        String groupId = plantParam.getGroupId();
        //??????????????????
        OilSubsidyLocationInformationDO subsidyLocationInformationDO =
            OilSubsidyLocationInformationDO.getInstance(groupId, startTime, endTime, dynamicInfoTotal);
        //??????????????????
        statisticalCheckOfLocationInformationDao.insert(subsidyLocationInformationDO);
        log.info("0x9101 ??????????????????????????????????????????");
    }

    private void send2Web(Message message) {
        List<String> users = WebSubscribeManager.getInstance().getCheckUsers();
        for (String user : users) {
            simpMessagingTemplate
                .convertAndSendToUser(user, ConstantUtil.WEB_SOCKET_T809_CHECK, JSON.toJSONString(message));
        }
    }

    /**
     * 0x9209 ?????? ????????????????????????
     */
    private void reissueDataRequestResult(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        String monitorName = msgBody.getString("vehicleNo");
        OilSupplementRequestData oilSupplementRequestData =
            WsSessionManager.INSTANCE.getOilSupplementRequestData(monitorName);
        if (oilSupplementRequestData == null) {
            return;
        }
        String monitorId = oilSupplementRequestData.getMonitorId();
        String sessionId = oilSupplementRequestData.getSessionId();
        trigger.cancelEvent(monitorId + "," + sessionId);
        // ????????????
        Integer requestResult = msgBody.getJSONObject("data").getInteger("result");
        Integer state = null;
        // ??????,????????????
        if (Objects.equals(requestResult, OilSupplementRequestData.REQUEST_RESULT_SUCCESS)) {
            WsSessionManager.INSTANCE.removeOilSupplementRequestData(monitorName);
            state = ReissueDataRequestDTO.STATE_SUCCESS;
            sendReissueData(oilSupplementRequestData);
            // ??????,????????????
        } else if (Objects.equals(requestResult, OilSupplementRequestData.REQUEST_RESULT_SUCCESS_RE_REQUEST)) {
            Integer sendNumber = oilSupplementRequestData.getSendNumber();
            if (sendNumber < 3) {
                state = ReissueDataRequestDTO.STATE_FAIL_LESS_THREE;
                oilSupplementRequestData.addSendNumber();
                // ????????????
                WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809())
                    .writeAndFlush(oilSupplementRequestData.getMessage());
                trigger.addEvent(30, TimeUnit.SECONDS, () -> {
                    WsSessionManager.INSTANCE.removeOilSupplementRequestData(monitorName);
                    simpMessagingTemplateUtil
                        .sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_OIL_SUPPLEMENT_RESULT,
                            new ReissueDataRequestDTO(monitorId, ReissueDataRequestDTO.STATE_OTHER_REASON));
                }, monitorId + "," + sessionId);
            } else {
                WsSessionManager.INSTANCE.removeOilSupplementRequestData(monitorName);
                state = ReissueDataRequestDTO.STATE_FAIL_THREE;
            }
            // ??????
        } else if (Objects.equals(requestResult, OilSupplementRequestData.REQUEST_RESULT_FAIL)) {
            WsSessionManager.INSTANCE.removeOilSupplementRequestData(monitorName);
            state = ReissueDataRequestDTO.STATE_FAIL;
            // ????????????
        } else if (requestResult == null || requestResult >= OilSupplementRequestData.REQUEST_RESULT_FAIL_OTHER) {
            WsSessionManager.INSTANCE.removeOilSupplementRequestData(monitorName);
            state = ReissueDataRequestDTO.STATE_OTHER_REASON;
        }
        if (state != null) {
            simpMessagingTemplateUtil.sendStatusMsgBySessionId(sessionId, ConstantUtil.WEBSOCKET_OIL_SUPPLEMENT_RESULT,
                new ReissueDataRequestDTO(monitorId, state));
        }
    }

    /**
     * ?????????????????? 0x1203
     */
    private void sendReissueData(OilSupplementRequestData oilSupplementRequestData) {
        String monitorId = oilSupplementRequestData.getMonitorId();
        Message message = oilSupplementRequestData.getMessage();
        Long startTime = oilSupplementRequestData.getStartTime();
        Long endTime = oilSupplementRequestData.getEndTime();
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        MainVehicleInfo info = new MainVehicleInfo();
        info.setVehicleNo(oilSupplementRequestData.getBrand());
        Integer plateColor = oilSupplementRequestData.getPlateColor();
        info.setVehicleColor(plateColor);
        info.setDataType(ConstantUtil.T809_UP_EXG_MSG_HISTORY_LOCATION);
        info.setExternalVehicleId(oilSupplementRequestData.getVehicleCode());
        List<Positional> positionalList = getMonitorLocation(monitorId, startTime, endTime);
        for (List<Positional> positionals : Lists.partition(positionalList, 10)) {
            VehicleLocationSupplementaryData data = new VehicleLocationSupplementaryData();
            data.setGnssCnt(positionals.size());
            List<VehicleLocationSupplementaryInfo> gpsList = new ArrayList<>();
            for (Positional positional : positionals) {
                LocalDateTime time =
                    LocalDateTime.ofEpochSecond(positional.getVtime(), 0, ZoneOffset.ofHours(8));
                VehicleLocationSupplementaryInfo supplementaryInfo = new VehicleLocationSupplementaryInfo();
                supplementaryInfo.setDay(time.getDayOfMonth());
                supplementaryInfo.setMonth(time.getMonthValue());
                supplementaryInfo.setYear(time.getYear());
                supplementaryInfo.setHour(time.getHour());
                supplementaryInfo.setMinute(time.getMinute());
                supplementaryInfo.setSecond(time.getSecond());
                String longtitude = positional.getLongtitude();
                if (StringUtils.isNotBlank(longtitude) && !Objects.equals(longtitude, "null")) {
                    supplementaryInfo.setLon(Math.pow(Double.parseDouble(longtitude) * 10.0, 6));
                }
                String latitude = positional.getLatitude();
                if (StringUtils.isNotBlank(latitude) && !Objects.equals(latitude, "null")) {
                    supplementaryInfo.setLat(Math.pow(Double.parseDouble(latitude) * 10.0, 6));
                }
                String speed = positional.getSpeed();
                if (StringUtils.isNotBlank(speed)) {
                    supplementaryInfo.setVec1(new BigDecimal(speed).intValue());
                }
                String recorderSpeed = positional.getRecorderSpeed();
                if (StringUtils.isNotBlank(recorderSpeed)) {
                    supplementaryInfo.setVec2(new BigDecimal(recorderSpeed).intValue());
                }
                String gpsMile = positional.getGpsMile();
                if (StringUtils.isNotBlank(gpsMile)) {
                    supplementaryInfo.setVec3(new BigDecimal(gpsMile).longValue());
                }
                String angle = positional.getAngle();
                if (StringUtils.isNotBlank(angle)) {
                    supplementaryInfo.setDirection(new BigDecimal(angle).intValue());
                }
                String height = positional.getHeight();
                if (StringUtils.isNotBlank(height)) {
                    supplementaryInfo.setAltitude(new BigDecimal(height).intValue());
                }
                String status = positional.getStatus();
                if (StringUtils.isNotBlank(status)) {
                    supplementaryInfo.setState(new BigDecimal(status).intValue());
                }
                gpsList.add(supplementaryInfo);
            }
            data.setGpsList(gpsList);
            info.setData(JSON.parseObject(JSON.toJSONString(data)));
            t809Message.setMsgBody(info);
            message.setData(t809Message);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    private List<Positional> getMonitorLocation(String monitorId, Long startTime, Long endTime) {
        final Map<String, String> params = new HashMap<>(8);
        params.put("vehicleId", monitorId);
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_MONITOR_LOCATION, params);
        return PaasCloudUrlUtil.getResultListData(str, Positional.class);
    }

    private void informationCheckAck(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (msgBody == null) {
            log.info("0Xtime+1 ????????????msgbody");
            return;
        }
        // ???????????????????????????????????????????????????????????????809???????????????
        MsgDesc desc = message.getDesc();
        String plateFormId = desc.getT809PlatId();
        if (StringUtils.isBlank(plateFormId)) {
            log.info("0X9405 ??????id????????????");
            return;
        }
        Integer connectProtocolTypeVal = connectionParamsSetDao.getConnectionProtocolTypeById(plateFormId);
        String connectProtocolType = String.valueOf(connectProtocolTypeVal);
        //?????????
        String vehicleNo = msgBody.getString("vehicleNo");
        //????????????????????????
        String vehicleId = vehicleService.getIdByBrand(vehicleNo);
        if (StringUtils.isBlank(vehicleId)) {
            return;
        }
        //????????????
        Integer vehicleColor = msgBody.getInteger("vehicleColor");
        //????????????
        Integer warnType = msgBody.getJSONObject("data").getInteger("warnType");
        warnType = adasCommonHelper.alarmTypeMapping.get(connectProtocolTypeVal).get(warnType);
        //????????????
        Long warnTime;
        if (ProtocolTypeUtil.T809_GUI_PROTOCOL_809_2013.equals(connectProtocolType)) {
            String alarmId = msgBody.getJSONObject("data").getJSONObject("alarmSign").getString("alarmId");
            warnTime = adasElasticSearchUtil.getWarnTimeByAlarmId(alarmId);
            if (warnTime == null) {
                log.info("????????????id???alarm_id" + alarmId + "????????????????????????????????????");
                return;
            }
        } else {
            //????????????
            warnTime = msgBody.getJSONObject("data").getLong("warnTime") * 1000;
        }

        RiskEventShortInfo eventIdFromHbase = getEventIdFromHbase(warnTime, vehicleId, warnType, plateFormId);

        String basicParam = getBasicParam(plateFormId, warnType, warnTime, vehicleId);
        if (eventIdFromHbase == null || eventIdFromHbase.getRiskEventId() == null) {
            log.info("0X9405 ???????????????????????????????????????????????????" + basicParam);
            return;
        }
        String riskEventId = eventIdFromHbase.getRiskEventId();
        String alarmType = String.valueOf(eventIdFromHbase.getAlarmType());
        Integer warn809Type = getWarn809Type(alarmType);
        Integer dataLength;
        Object infoCheckAckInfo;
        AdasRiskEventEsBean adasRiskEventEsBean = adasElasticSearchService.esGetRiskEventById(riskEventId);
        if (ProtocolTypeUtil.T809_ZW_PROTOCOL_809_2019.equals(connectProtocolType)) {
            //??????????????????
            final AdasEventInfo eventInfo = this.getEventInfo(riskEventId);
            if (eventInfo == null) {
                log.info("????????????0X9405 ??????riskEventId:" + riskEventId + "????????????????????????" + basicParam);
                return;
            }
            T809InfoCheckAckZw infoCheckAckZw = new T809InfoCheckAckZw();
            infoCheckAckZw.setWarnTime(eventInfo.getWarnTime());
            infoCheckAckZw.setStartTime(eventInfo.getWarnTime());
            infoCheckAckZw.setEndTime(eventInfo.getWarnTime());
            infoCheckAckZw.setPlatformId(eventInfo.getPlatformId());
            infoCheckAckZw.setInfoContent(eventInfo.getInfoContent());
            infoCheckAckZw.setDrvLineId(eventInfo.getDrvLineId());

            infoCheckAckZw.initSourceInfo(message.getDesc());
            infoCheckAckZw.setWarnType(warn809Type);
            infoCheckAckZw.initLength();
            dataLength = infoCheckAckZw.getDataLength();
            infoCheckAckInfo = infoCheckAckZw;

        } else {
            final AdasEventInfo eventInfo = getEventInfo(riskEventId);
            if (eventInfo == null) {
                log.info("0X9405 ??????riskEventId:" + riskEventId + "????????????????????????" + basicParam);
                return;
            }

            T809InfoCheckAck infoCheckAck = new T809InfoCheckAck();
            infoCheckAck.setEventId(eventInfo.getEventId());
            infoCheckAck.setWarnTime(eventInfo.getWarnTime());
            infoCheckAck.setMsgSn(eventInfo.getMsgSn());
            infoCheckAck.setDriver(eventInfo.getDriver());
            infoCheckAck.setDriverNo(eventInfo.getDriverNo());
            infoCheckAck.setLevel(eventInfo.getRiskLevel());
            infoCheckAck.setLon(eventInfo.getLng());
            infoCheckAck.setLat(eventInfo.getLat());
            infoCheckAck.setAltitude(eventInfo.getAltitude());
            infoCheckAck.setVec1(eventInfo.getSpeed() == null ? null : eventInfo.getSpeed().intValue());
            infoCheckAck.setVec2(eventInfo.getGrapherSpeed());
            infoCheckAck.setDirection(Optional.ofNullable(eventInfo.getDirection()).map(Double::intValue).orElse(null));
            infoCheckAck.setPlatId(eventInfo.getPlatformId());
            infoCheckAck.setInfoContent(eventInfo.getInfoContent());

            infoCheckAck.setWarnType(warn809Type);
            //?????????????????????????????????
            infoCheckAck.setWarnSrc(1);
            //?????????????????????????????????
            infoCheckAck.setStatus(2);
            infoCheckAck.setVehicleNo(vehicleNo);
            infoCheckAck.setAlarmId(adasRiskEventEsBean.getAlarmId());
            infoCheckAck.setVehicleColor(vehicleColor);
            infoCheckAck.initLength();
            dataLength = infoCheckAck.getDataLength(connectProtocolType);
            infoCheckAckInfo = infoCheckAck;
        }

        T809Message alarmFileAck =
            getAlarmT809Message(vehicleNo, vehicleColor, t809Message.getMsgHead(), dataLength, infoCheckAckInfo,
                ConstantUtil.T809_UP_WARN_MSG_CHECK_ACK);
        Message ack = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, alarmFileAck).assembleDesc809(plateFormId);
        //????????????????????????
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(ack);
    }

    private AdasEventInfo getEventInfo(String riskEventId) {
        Map<String, String> params = new HashMap<>(2);
        params.put("riskEventId", riskEventId);
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_RISK_EVENT_INFO, params);
        return PaasCloudUrlUtil.getResultData(str, AdasEventInfo.class);
    }

    private String getBasicParam(String plateFormId, Integer warnType, Long warnTime, String vehicleId) {
        return "???warnTime???" + warnTime + ",vehicleId:" + vehicleId + ",warnType:" + warnType + ",plateFormId:"
            + plateFormId;
    }

    private Integer getWarn809Type(String alarmType) {
        String warn809TypeVal = riskEventDao.getWarnTypeBy808pos(alarmType);
        return Integer.parseInt(warn809TypeVal.toLowerCase().replace("0x", ""), 16);
    }

    private RiskEventShortInfo getEventIdFromHbase(Long warnTime, String monitorId, Integer warnType,
                                                   String plateFormId) {
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", monitorId);
        params.put("platId", plateFormId);
        params.put("alarmType", String.valueOf(warnType));
        params.put("startTime", String.valueOf(warnTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_RISK_EVENT_SHORT_INFO, params);
        return PaasCloudUrlUtil.getResultData(str, RiskEventShortInfo.class);
    }

    /**
     * ??????0x9C01??????
     * @param message ??????message
     */
    private void preventionMsgFileListAck(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (msgBody == null) {
            return;
        }
        // ???????????????????????????????????????????????????????????????809???????????????
        MsgDesc desc = message.getDesc();
        String plateFormId = desc.getT809PlatId();
        if (StringUtils.isBlank(plateFormId)) {
            return;
        }
        String platId = desc.getT809PlatId();
        Integer connectProtocolType = connectionParamsSetDao.getConnectionProtocolTypeById(platId);
        //?????????????????????
        if (!ProtocolTypeUtil.T809_JI_PROTOCOL_809_2013.equals(String.valueOf(connectProtocolType))) {
            return;
        }
        //?????????
        String vehicleNo = msgBody.getString("vehicleNo");
        //????????????
        Integer vehicleColor = msgBody.getInteger("vehicleColor");

        //??????id
        String alarmId = msgBody.getJSONObject("data").getString("alarmId");
        //??????Id
        String monitorIdStr = vehicleService.getIdByBrandAndColor(vehicleNo, vehicleColor);
        //??????
        String time = msgBody.getJSONObject("data").getString("time");
        time = getFormatTime(time);

        //????????????mediaIds
        LinkedList<String> fieldMap = Lists.newLinkedList();
        fieldMap.add("id");
        Map<String, Object> whereEqCondi = Maps.newHashMap();
        whereEqCondi.put("vehicle_id", monitorIdStr);
        whereEqCondi.put("event_time", time);
        Map<String, Object> whereUneqCondi = Maps.newHashMap();
        List<String> eventIds = adasElasticSearchUtil
            .esGetIdsByIds(fieldMap, whereEqCondi, whereUneqCondi, AdasElasticSearchUtil.RISK_EVENT_INDEX, null);

        List<String> mediaIds = new ArrayList<>();
        if (eventIds != null && !eventIds.isEmpty()) {
            String eventId = eventIds.get(0);
            whereEqCondi.clear();
            whereEqCondi.put("risk_event_id", eventId);
            mediaIds = adasElasticSearchUtil
                .esGetIdsByIds(fieldMap, whereEqCondi, whereUneqCondi, AdasElasticSearchUtil.ADAS_MEDIA, null);
        }
        //????????????????????????
        List<WarnMsgFileInfo> riskMediaInfos = new ArrayList<>();
        if (mediaIds.size() > 0) {
            riskMediaInfos = this.listAdasMediaById(mediaIds, WarnMsgFileInfo.class);
            String fileFormat;
            byte[] bytes;
            for (WarnMsgFileInfo mediaInfo : riskMediaInfos) {
                mediaInfo.setFileNameLength(mediaInfo.getFileName().length());
                fileFormat = mediaInfo.getFileName().substring(mediaInfo.getFileName().lastIndexOf(".") + 1);
                mediaInfo.setFileFormat(FILE_FORMAT_MAP.get(fileFormat));
                bytes = fastDFSClient.downloadFile(mediaInfo.getFileUrl());
                mediaInfo.setMd5(DigestUtils.md5Hex(bytes));
                mediaInfo.setFileUrl(webServerUrl809 + mediaInfo.getFileUrl() + "?token=" + getToken());
                mediaInfo.setFileUrlLengh(mediaInfo.getFileUrl().length());
                if ("null".equals(String.valueOf(mediaInfo.getFileSize()))) {
                    mediaInfo.setFileSize(0L);
                }
            }
        }
        T809AlarmFileListAck fileListAck = new T809AlarmFileListAck();
        fileListAck.setServer(ftpHost);
        fileListAck.setServerLength(ftpHost.length());
        fileListAck.setPort(ftpPort);
        fileListAck.setUserName(ftpUserName);
        fileListAck.setUserNameLength(ftpUserName.length());
        fileListAck.setPassword(ftpPassword);
        fileListAck.setPasswordLength(ftpPassword.length());
        fileListAck.setFileCount(riskMediaInfos.size());
        fileListAck.setFileInfos(riskMediaInfos);
        fileListAck.setInfoId(alarmId);
        Integer dataLength = getFileAckDataLength(fileListAck, String.valueOf(connectProtocolType));
        T809Message alarmFileAck =
            getAlarmT809Message(vehicleNo, vehicleColor, t809Message.getMsgHead(), dataLength, fileListAck,
                ConstantUtil.T809_UP_PREVENTION_MSG_FILELIST_REQ_ACK);
        alarmFileAck.getMsgHead().setMsgID(ConstantUtil.T809_UP_PREVENTION_MSG);
        Message ack = MsgUtil.getMsg(ConstantUtil.T809_UP_PREVENTION_MSG, alarmFileAck).assembleDesc809(plateFormId);
        //preventionMsgFileListAckMap.put(vehicleNo + "_" + alarmId, ack);
        //????????????????????????
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(ack);
        RedisHelper
            .setString(HistoryRedisKeyEnum.MONITOR_ID_ALARM_ID.of(monitorIdStr + "_" + alarmId), JSON.toJSONString(ack),
                ONE_DAY_REDIS_EXPIRE);
    }

    private <T> List<T> listAdasMediaById(List<String> mediaIds, Class<T> clazz) {
        if (CollectionUtils.isEmpty(mediaIds)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(2);
        params.put("mediaIds", JSON.toJSONString(mediaIds));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.LIST_RISK_MEDIA, params);
        return PaasCloudUrlUtil.getResultListData(str, clazz);
    }

    /**
     * ?????????????????????????????? 0x1404
     */
    private void alarmFileListAck(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (msgBody == null) {
            log.info("0x1404????????????????????????msgBody??????");
            return;
        }
        MsgDesc desc = message.getDesc();
        String plateFormId = desc.getT809PlatId();
        if (StringUtils.isBlank(plateFormId)) {
            log.info("0x1404??????????????????????????????id??????");
            return;
        }
        Integer connectProtocolTypeVal = connectionParamsSetDao.getConnectionProtocolTypeById(plateFormId);
        String connectProtocolType = String.valueOf(connectProtocolTypeVal);
        //????????????????????????????????? ????????????
        if (!(ProtocolTypeUtil.T809_SI_CHUAN_PROTOCOL_809_2013.equals(connectProtocolType)
            || ProtocolTypeUtil.T809_GUI_PROTOCOL_809_2013.equals(connectProtocolType)
            || ProtocolTypeUtil.T809_SU_PROTOCOL_809_2013.equals(connectProtocolType)
            || ProtocolTypeUtil.T809_ZW_PROTOCOL_809_2019.equals(connectProtocolType))) {
            log.info("0X9405 ????????????????????????????????????????????????");
            return;
        }
        //?????????
        String brand = msgBody.getString("vehicleNo");
        //????????????
        Integer vehicleColor = msgBody.getInteger("vehicleColor");
        //???????????????
        if (!Optional.ofNullable(msgBody.getJSONObject("data")).map(o -> o.getString("infoId")).isPresent()) {
            log.error("0x9404?????????????????????{}", JSON.toJSONString(msgBody));
        }
        String alarmId = msgBody.getJSONObject("data").getString("infoId");
        T809MsgHead msgHead = t809Message.getMsgHead();
        int msgId = ConstantUtil.T809_UP_WARN_MSG_FILELIST_ACK;
        T809Message alarmFileAck;
        Integer dataLength;
        Message ack;
        if (connectProtocolType.equals(ProtocolTypeUtil.T809_ZW_PROTOCOL_809_2019)) {
            //?????????????????????
            T809AlarmFileListAckZw fileListAckZw = getT809AlarmFileListAckZw(connectProtocolType, alarmId, msgHead);
            //?????????????????????????????????
            dataLength = fileListAckZw.getTotalFileLength();
            Integer dataType = msgBody.getInteger("dataType");
            fileListAckZw.setSourceDataType(dataType);
            alarmFileAck = getAlarmT809Message(brand, vehicleColor, msgHead, dataLength, fileListAckZw, msgId);
            SupervisionAlarmInfo supervisionAlarmInfo = (SupervisionAlarmInfo) alarmFileAck.getMsgBody();
            supervisionAlarmInfo.setSourceDataType(dataType);
            ack = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, alarmFileAck, msgHead).assembleDesc809(plateFormId);
            ack.getDesc().setSourceDataType(dataType);
        } else {
            //?????????????????????
            T809AlarmFileListAck fileListAck = getT809AlarmFileListAck(connectProtocolType, alarmId);
            //?????????????????????????????????
            dataLength = getFileAckDataLength(fileListAck, connectProtocolType);
            alarmFileAck = getAlarmT809Message(brand, vehicleColor, msgHead, dataLength, fileListAck, msgId);
            ack = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, alarmFileAck).assembleDesc809(plateFormId);
        }

        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(ack);
    }

    private T809AlarmFileListAck getT809AlarmFileListAck(String connectProtocolType, String alarmId) {
        List<WarnMsgFileInfo> riskMediaInfos = getWarnMsgFileInfos(connectProtocolType, alarmId);
        T809AlarmFileListAck fileListAck = new T809AlarmFileListAck();
        fileListAck.setInfoId(alarmId);
        fileListAck.setUserName(chuanFtpUserName);
        fileListAck.setUserNameLength(chuanFtpUserName.length());
        fileListAck.setPassword(chuanFtpPassword);
        fileListAck.setPasswordLength(chuanFtpPassword.length());
        fileListAck.setServer(chuanFtpHost);
        fileListAck.setServerLength(chuanFtpHost.getBytes(Charset.forName("GBK")).length);
        fileListAck.setPort(chuanFtpPort);
        fileListAck.setFileCount(riskMediaInfos.size());
        fileListAck.setFileInfos(riskMediaInfos);
        return fileListAck;
    }

    private T809AlarmFileListAckZw getT809AlarmFileListAckZw(String connectProtocolType, String alarmId,
        T809MsgHead msgHead) {
        List<WarnMsgFileInfoZw> riskMediaInfos = getWarnMsgFileInfosZw(connectProtocolType, alarmId);
        T809AlarmFileListAckZw fileListAck = new T809AlarmFileListAckZw();
        fileListAck.setInfoId(alarmId);
        fileListAck.setSourceDataType(msgHead.getMsgID());
        fileListAck.setSourceMsgSn(msgHead.getMsgSn());
        fileListAck.setFileCount(riskMediaInfos.size());
        fileListAck.setFileInfos(riskMediaInfos);
        return fileListAck;
    }

    /**
     * ?????????????????????
     */
    private List<WarnMsgFileInfo> getWarnMsgFileInfos(String connectProtocolType, String alarmId) {
        List<String> mediaIds = getMediaHbaseIds(alarmId, connectProtocolType);
        List<WarnMsgFileInfo> riskMediaInfos = new ArrayList<>();
        if (mediaIds.size() > 0) {
            String token = getToken();
            riskMediaInfos = this.listAdasMediaById(mediaIds, WarnMsgFileInfo.class);
            for (WarnMsgFileInfo mediaInfo : riskMediaInfos) {
                mediaInfo.setFileNameLength(mediaInfo.getFileName().getBytes(Charset.forName("GBK")).length);
                if (ProtocolTypeUtil.isGStandard(connectProtocolType)) {
                    mediaInfo.setFileUrl(webServerUrl809 + mediaInfo.getFileUrl() + "?token=" + token);
                } else if (ProtocolTypeUtil.T809_SU_PROTOCOL_809_2013.equals(connectProtocolType)) {
                    mediaInfo.setFileUrl(webServerUrl809 + mediaInfo.getFileUrl());
                } else if (ProtocolTypeUtil.T809_SI_CHUAN_PROTOCOL_809_2013.equals(connectProtocolType)) {
                    String fileUrl = mediaInfo.getFileUrl();
                    String url = "ftp://@" + chuanFtpHost + ":" + chuanFtpPort + fileUrl.replace("group1/M00", "");
                    mediaInfo.setFileUrl(url);
                    mediaInfo.setFileUrl(mediaInfo.getFileUrl());
                }
                mediaInfo.setFileUrlLengh(mediaInfo.getFileUrl().getBytes(Charset.forName("GBK")).length);
            }
        }
        return riskMediaInfos;
    }

    /**
     * ?????????????????????
     */
    private List<WarnMsgFileInfoZw> getWarnMsgFileInfosZw(String connectProtocolType, String alarmId) {
        List<WarnMsgFileInfoZw> riskMediaInfos = new ArrayList<>();
        List<String> mediaIds = getMediaHbaseIds(alarmId, connectProtocolType);
        if (CollectionUtils.isEmpty(mediaIds)) {
            return riskMediaInfos;
        }
        String token = getToken();
        riskMediaInfos = this.listAdasMediaById(mediaIds, WarnMsgFileInfoZw.class);
        for (WarnMsgFileInfoZw mediaInfo : riskMediaInfos) {
            mediaInfo.setFileNameLength(mediaInfo.getFileName().getBytes(Charset.forName("GBK")).length);
            mediaInfo.setFileUrl(webServerUrl809 + mediaInfo.getFileUrl() + "?token=" + token);
            mediaInfo.setFileUrlLength(mediaInfo.getFileUrl().getBytes(Charset.forName("GBK")).length);
        }
        return riskMediaInfos;
    }

    /**
     * ???????????????????????????????????? 0x1421
     */
    private void shanghaiAlarmFileListAck(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (msgBody == null) {
            log.info("??????0x1421????????????????????????msgBody??????");
            return;
        }
        MsgDesc desc = message.getDesc();
        String plateFormId = desc.getT809PlatId();
        if (StringUtils.isBlank(plateFormId)) {
            log.info("??????0x1421??????????????????????????????id??????");
            return;
        }
        Integer connectProtocolType = connectionParamsSetDao.getConnectionProtocolTypeById(plateFormId);
        // //????????????????????????
        // if (!(ProtocolTypeUtil.T809_SI_CHUAN_PROTOCOL_809_2013.equals(String.valueOf(connectProtocolType))
        //     || ProtocolTypeUtil.T809_GUI_PROTOCOL_809_2013.equals(String.valueOf(connectProtocolType)))) {
        //     log.info("0x1404????????????????????????id?????????????????????????????????????????????????????????");
        //     return;
        // }
        JSONObject bodyData = msgBody.getJSONObject("data");
        //?????????
        String brand = bodyData.getString("vehicleNo");
        //????????????
        Integer vehicleColor = bodyData.getInteger("vehicleColor");
        //???????????????
        String alarmId = bodyData.getString("infoId");
        //?????????????????????
        List<String> mediaIds = getMediaHbaseIds(alarmId, String.valueOf(connectProtocolType));
        List<WarnMsgFileInfo> riskMediaInfos = new ArrayList<>();
        if (mediaIds.size() > 0) {
            String token = getToken();
            riskMediaInfos = this.listAdasMediaById(mediaIds, WarnMsgFileInfo.class);
            for (WarnMsgFileInfo mediaInfo : riskMediaInfos) {
                mediaInfo.setFileNameLength(mediaInfo.getFileName().getBytes(Charset.forName("GBK")).length);
                mediaInfo.setFileUrl(webServerUrl809 + mediaInfo.getFileUrl() + "?token=" + token);
                mediaInfo.setFileUrlLengh(mediaInfo.getFileUrl().getBytes(Charset.forName("GBK")).length);
            }
        }
        T809AlarmFileListAck fileListAck = new T809AlarmFileListAck();
        fileListAck.setInfoId(alarmId);
        fileListAck.setServerType(0x01);
        fileListAck.setFileCount(riskMediaInfos.size());
        fileListAck.setFileInfos(riskMediaInfos);
        //?????????????????????????????????
        Integer dataLength = getFileAckDataLength(fileListAck, String.valueOf(connectProtocolType));
        T809Message alarmFileAck =
            getAlarmT809Message(brand, vehicleColor, t809Message.getMsgHead(), dataLength, fileListAck,
                ConstantUtil.T809_HU_UP_WARN_MSG_FILELIST_ACK);
        Message ack = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, alarmFileAck).assembleDesc809(plateFormId);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(ack);
    }

    public List<String> getMediaHbaseIds(String alarmId, String connectProtocolType) {
        List<String> ids = new LinkedList<>();
        switch (connectProtocolType) {
            case ProtocolTypeUtil.T809_SI_CHUAN_PROTOCOL_809_2013:
            case ProtocolTypeUtil.T809_GUI_PROTOCOL_809_2013:
            case ProtocolTypeUtil.T809_HU_PROTOCOL_809_2019:
            case ProtocolTypeUtil.T809_SU_PROTOCOL_809_2013:
                ids = getMedias("alarm_id", alarmId);
                break;
            case ProtocolTypeUtil.T809_ZW_PROTOCOL_809_2019:
                ids = getMedias("warn_no", alarmId);
                break;
            default:
                break;
        }
        return ids;
    }

    private List<String> getMedias(String field, String alarmId) {
        String riskEventId = adasElasticSearchUtil.getRiskEventIdByAlarmId(field, alarmId);
        if (riskEventId != null) {
            LinkedList<String> fieldMap = Lists.newLinkedList();
            fieldMap.add("id");
            Map<String, Object> whereEqCondi = Maps.newHashMap();
            Map<String, Object> whereUneqCondi = Maps.newHashMap();
            return adasElasticSearchUtil
                .esGetIdsByIds(fieldMap, whereEqCondi, whereUneqCondi, AdasElasticSearchUtil.ADAS_MEDIA,
                    "risk_event_id", riskEventId);
        }
        return new LinkedList<>();
    }

    /**
     * ????????????token
     */
    public static String getToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private T809Message getAlarmT809Message(String brand, Integer color, T809MsgHead msgHead, Integer dataLength,
        Object fileListAck, Integer dataType) {
        String serverIp = msgHead.getServerIp(); // IP??????
        Integer msgGNSSCenterId = msgHead.getMsgGNSSCenterId(); // ?????????
        SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo(); // ?????????
        supervisionAlarmInfo.setDataType(dataType);
        supervisionAlarmInfo.setDataLength(dataLength); // ??????????????????(??????????????????????????????)
        supervisionAlarmInfo.setVehicleNo(brand);
        supervisionAlarmInfo.setVehicleColor(color);
        supervisionAlarmInfo.setData(MsgUtil.objToJson(fileListAck));
        supervisionAlarmInfo.setSourceMsgSn(msgHead.getMsgSn());
        supervisionAlarmInfo.setSourceDataType(msgHead.getMsgID());

        return MsgUtil.getT809Message(ConstantUtil.T809_UP_WARN_MSG, serverIp, msgGNSSCenterId, supervisionAlarmInfo);
    }

    private Integer getFileAckDataLength(T809AlarmFileListAck fileListAck, String connectProtocolType) {
        int length = 0;
        Integer severLength = fileListAck.getServerLength();
        Integer userNameLength = fileListAck.getUserName().getBytes(Charset.forName("GBK")).length;
        Integer passwordLength = fileListAck.getPassword().getBytes(Charset.forName("GBK")).length;
        switch (connectProtocolType) {
            case ProtocolTypeUtil.T809_SI_CHUAN_PROTOCOL_809_2013:
                length = 38 + severLength + userNameLength + passwordLength;
                for (WarnMsgFileInfo warnMsgFileInfo : fileListAck.getFileInfos()) {
                    length += warnMsgFileInfo.getFileNameLength() + 7 + warnMsgFileInfo.getFileUrlLengh();
                }
                break;
            case ProtocolTypeUtil.T809_GUI_PROTOCOL_809_2013:
                length = 17;
                for (WarnMsgFileInfo warnMsgFileInfo : fileListAck.getFileInfos()) {
                    length += warnMsgFileInfo.getFileNameLength() + 7 + warnMsgFileInfo.getFileUrlLengh();
                }
                break;
            case ProtocolTypeUtil.T809_JI_PROTOCOL_809_2013:
                length = 6 + severLength + userNameLength + passwordLength;
                for (WarnMsgFileInfo warnMsgFileInfo : fileListAck.getFileInfos()) {
                    length += warnMsgFileInfo.getFileNameLength() + warnMsgFileInfo.getFileUrlLengh() + 40;
                }
                break;
            default:
                break;
        }
        return length;
    }

    /**
     * 0x1406?????????????????????
     * @param message ??????message
     */
    private void countAlarmTypeByBrandAndTime(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (msgBody == null) {
            log.info("0x1406????????????????????????msgBody??????");
            return;
        }
        // ???????????????????????????????????????????????????????????????809???????????????
        MsgDesc desc = message.getDesc();
        String plateFormId = desc.getT809PlatId();
        if (StringUtils.isBlank(plateFormId)) {
            log.info("0x1406??????????????????????????????id??????");
            return;
        }
        String platId = desc.getT809PlatId();
        //?????????
        String vehicleNo = msgBody.getString("vehicleNo");
        //????????????
        Integer vehicleColor = msgBody.getInteger("vehicleColor");
        // //??????ID
        VehicleInfo vehicleInfo = new VehicleInfo();
        vehicleInfo.setBrand(vehicleNo);
        vehicleInfo.setPlateColor(vehicleColor);
        //??????Id
        String monitorId = vehicleService.getIdByBrandAndColor(vehicleNo, vehicleColor);
        //????????????
        Long startTime = msgBody.getJSONObject("data").getLong("startTime") * 1000;
        //????????????
        Long endTime = msgBody.getJSONObject("data").getLong("endTime") * 1000;
        Integer connectProtocolType = connectionParamsSetDao.getConnectionProtocolTypeById(platId);
        //???????????????????????????????????????808-809????????????
        List<T809AlarmMapping> t809AlarmMappings =
            connectionParamsSetDao.get808PosAnd809PosByProtocolType(connectProtocolType);
        List<Integer> alarmType808 = new ArrayList<>();
        HashMap<Integer, Integer> t809AlarmTypeStringToInteger = new HashMap<>(100);
        //????????????????????????????????????????????????????????????????????????
        List<Integer> validateExist = new ArrayList<>();
        //??????808???????????? ?????????808 ??? 809???????????????10?????????????????????t809AlarmTypeStringToInteger  Map???
        if (t809AlarmMappings.size() != 0) {
            for (T809AlarmMapping t809AlarmMapping : t809AlarmMappings) {
                alarmType808.add(Integer.parseInt(t809AlarmMapping.getPos808()));
                t809AlarmTypeStringToInteger.put(Integer.parseInt(t809AlarmMapping.getPos808()),
                    (Integer.parseInt(t809AlarmMapping.getPos809().replaceAll("^0[x|X]", ""), 16)));
            }
        }
        //???HBase???????????????????????????????????????
        List<T809AlarmForwardInfoMiddleQuery> t809AlarmForwardInfoMiddleQuery =
                getAlarmForwardInfoMiddleInfo(platId, monitorId, startTime, endTime, alarmType808);
        List<WarnMsgStaticsInfo> list = new ArrayList<>();

        Integer middleInfoAlarmType;
        Integer pos809;
        for (T809AlarmForwardInfoMiddleQuery alarmForwardInfoMiddleQuery : t809AlarmForwardInfoMiddleQuery) {
            middleInfoAlarmType = alarmForwardInfoMiddleQuery.getAlarmType();
            //????????????????????????list????????????????????????808????????????
            if (validateExist.contains(middleInfoAlarmType)) {
                //????????????+1
                changeCountNumber(list, t809AlarmTypeStringToInteger.get(middleInfoAlarmType));
                //???????????????  ???????????????????????????808???????????? ????????????????????????????????????
            } else {
                pos809 = t809AlarmTypeStringToInteger.get(middleInfoAlarmType);
                WarnMsgStaticsInfo warnMsgStaticsInfo = new WarnMsgStaticsInfo();
                warnMsgStaticsInfo.setWarnType(pos809);
                warnMsgStaticsInfo.setStatics(1);
                list.add(warnMsgStaticsInfo);
                validateExist.add(middleInfoAlarmType);
            }
        }

        AlarmTypeCountInfo alarmTypeCountInfo =
            AlarmTypeCountInfo.getInstance(list, message.getDesc(), t809Message.getMsgHead().getMsgSn());
        T809Message alarmFileAck =
            getAlarmT809Message(vehicleNo, vehicleColor, t809Message.getMsgHead(), alarmTypeCountInfo.getDataLength(),
                alarmTypeCountInfo, ConstantUtil.T809_UP_WARN_MSG_STATICS_ACK);
        Message ack = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, alarmFileAck).assembleDesc809(plateFormId);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(ack);
    }

    private List<T809AlarmForwardInfoMiddleQuery> getAlarmForwardInfoMiddleInfo(
            String platId, String monitorId, Long startTime, Long endTime, List<Integer> alarmType808) {
        if (CollectionUtils.isEmpty(alarmType808)) {
            return new ArrayList<>();
        }
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", monitorId);
        params.put("platId", platId);
        params.put("alarmTypes", JSON.toJSONString(alarmType808));
        params.put("startTime", String.valueOf(startTime));
        params.put("endTime", String.valueOf(endTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALARM_FORWARD_INFO_MIDDLE_INFO, params);
        return PaasCloudUrlUtil.getResultListData(str, T809AlarmForwardInfoMiddleQuery.class);
    }

    /**
     * ??????????????????????????? ??????
     * @param warnMsgStaticsInfoList ??????????????????
     * @param pos809                 809????????????
     */
    private void changeCountNumber(List<WarnMsgStaticsInfo> warnMsgStaticsInfoList, Integer pos809) {
        for (WarnMsgStaticsInfo warnMsgStaticsInfo : warnMsgStaticsInfoList) {
            if (warnMsgStaticsInfo.getWarnType().equals(pos809)) {
                warnMsgStaticsInfo.setStatics(warnMsgStaticsInfo.getStatics() + 1);
                break;
            }
        }
    }

    /**
     * ??????809????????????
     */
    private void other809MsgSend(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        // ????????????id
        String platformId = message.getDesc().getT809PlatId();
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(platformId);
        if (plantParam == null) {
            return;
        }
        String platformServerIp = plantParam.getIp(); // ????????????IP
        // ?????????????????????
        String msgGNSSCenterId = String.valueOf(plantParam.getCenterId());
        Integer objectType = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody())).getJSONObject("data")
            .getInteger("objectType");
        List<String> groupIds = get809MsgSendTarget(objectType, platformServerIp, msgGNSSCenterId);
        if (groupIds.size() > 0) {
            for (String groupId : groupIds) {
                t809Message.getMsgHead().setGroupId(groupId);
                message.setData(t809Message);
                OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(groupId);
                List<UserDTO> userList =
                    organizationService.fuzzyUsersByOrgDn(null, organizationLdap.getId().toString(), false);
                for (UserDTO userDTO : userList) {
                    simpMessagingTemplate
                        .convertAndSendToUser(userDTO.getUsername(), ConstantUtil.WEB_SOCKET_T809_CHECK_GLOBAL,
                            JSON.toJSONString(message));
                }
            }
        }
    }

    /**
     * 809?????????????????????
     */
    private void send809PlatformMsg(Message message, String orgUuid, boolean pushToExtraUsers) {
        if (StringUtils.isBlank(orgUuid)) {
            return;
        }
        OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(orgUuid);
        final String orgDn = organizationLdap.getId().toString();
        List<UserDTO> orgUsers = organizationService.fuzzyUsersByOrgDn(null, orgDn, false);
        // ?????????????????????????????????????????????????????????????????????????????????
        final Set<String> receivers =
            pushToExtraUsers ? orgInspectionExtraUserDAO.listUsernameByOrgId(orgUuid) : new HashSet<>();
        if (CollectionUtils.isNotEmpty(orgUsers)) {
            for (UserDTO orgUser : orgUsers) {
                receivers.add(orgUser.getUsername());
            }
        }
        final String payload = JSON.toJSONString(message);
        for (String receiver : receivers) {
            simpMessagingTemplate.convertAndSendToUser(receiver, ConstantUtil.WEB_SOCKET_T809_CHECK_GLOBAL, payload);
        }
    }

    /**
     * ???????????????????????????????????????
     */
    private void pushInspectUser809Message2Web(Message message, String groupId, SuperPlatformMsg msg, Date expireTime) {
        if (StringUtils.isEmpty(groupId)) {
            return;
        }
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject t809MsgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (t809MsgBody == null) {
            return;
        }
        JSONObject t809MsgBodyData = t809MsgBody.getJSONObject("data");
        OrganizationLdap organizationLdap = organizationService.getOrganizationByUuid(groupId);
        List<UserDTO> userList =
            organizationService.fuzzyUsersByOrgDn(null, organizationLdap.getId().toString(), false);
        if (CollectionUtils.isEmpty(userList)) {
            return;
        }
        Set<String> onlineUser = onlineUserManager.getOnlineUsers();
        if (onlineUser.isEmpty()) {
            return;
        }
        //????????????????????????
        userList = userList.stream().filter(o -> onlineUser.contains(o.getUsername())).collect(Collectors.toList());
        //???????????????????????????????????????????????????????????????
        for (UserDTO userDTO : userList) {
            t809MsgBodyData.put("answerUser", userDTO.getUsername());
            t809MsgBodyData.put("answerUserTel", userDTO.getMobile());
            t809MsgBodyData.put("answerUserIdentityNumber", userDTO.getIdentityNumber());
            t809MsgBodyData.put("socialSecurityNumber", userDTO.getSocialSecurityNumber());
            t809MsgBodyData.put("answerId", msg.getId());
            t809MsgBodyData.put("expireTime", DateUtil.formatDate(expireTime, "yyyy-MM-dd HH:mm:ss"));
            t809MsgBodyData.put("answerStatus", 0);
            t809MsgBodyData
                .put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            t809MsgBody.put("data", t809MsgBodyData);
            t809Message.setMsgBody(t809MsgBody);
            message.setData(t809Message);
            simpMessagingTemplate.convertAndSendToUser(userDTO.getUsername(), ConstantUtil.WEB_SOCKET_T809_CHECK_GLOBAL,
                JSON.toJSONString(message));
        }
    }

    /**
     * ??????809??????????????????mysql
     */
    private void standard809GangSave(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject t809MsgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (t809MsgBody == null) {
            return;
        }
        JSONObject t809MsgBodyData = t809MsgBody.getJSONObject("data");
        // ????????????id
        Integer objectType = t809MsgBodyData.getInteger("objectType");
        // ????????????id
        String platformId = message.getDesc().getT809PlatId();
        // ????????????????????????(????????????)
        String objectId = t809MsgBodyData.getString("objectId");
        List<PlantParam> info = getTargetInfo(objectType, platformId, objectId);
        if (info.size() > 0) {
            Date expireTime;
            long msgTime = System.currentTimeMillis();
            if (t809MsgBodyData.containsKey("answerTime") && t809MsgBodyData.getLong("answerTime") != null) {
                long answerTime = t809MsgBodyData.getLong("answerTime") * 1000 * 60;
                long resultAnswerTime = msgTime + answerTime;
                expireTime = new Date(resultAnswerTime);
            } else {
                expireTime = getThirtyMinutesLaterDate();
            }
            SuperPlatformMsg msg;
            for (PlantParam param : info) {
                if (param != null) {
                    // ??????id
                    String groupId = param.getGroupId();
                    //  ????????????id
                    String platformFlag = param.getId();
                    if (StrUtil.areNotBlank(groupId, platformFlag)) {
                        // ???809??????????????????????????????????????????
                        msg = new SuperPlatformMsg();
                        // ???????????????????????????id
                        t809Message.getMsgHead().setHandleId(msg.getId());
                        t809Message.getMsgHead().setGroupId(groupId);
                        final Object body = t809Message.getMsgBody();
                        if (body instanceof JSONObject) {
                            final JSONObject jsonBody = (JSONObject) body;
                            jsonBody.put("platformName", param.getPlatformName());
                        }
                        message.setData(t809Message);
                        //   ????????????id
                        msg.setPlatformId(platformFlag);
                        // ??????id
                        msg.setGroupId(groupId);
                        msg.setType(0); // ??????
                        msg.setMsg(JSONObject.toJSONString(message));
                        // ??????????????????
                        msg.setTime(new Date(msgTime));
                        // ????????????(??????????????????,??????30??????)
                        msg.setExpireTime(expireTime);
                        superPlatformMsgService.saveSuperPlatformMsg(msg);
                        // ????????????
                        send809PlatformMsg(message, groupId, true);
                        // ??????????????????????????????
                        connectionParamsSetService.assistOnInspection(msg, message);
                    }
                }
            }
        }
    }

    /**
     * ????????????809????????????
     */
    private void extend809GangSave(Message message) {
        MsgDesc t809Desc = message.getDesc();
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        // ????????????id
        String platformId = t809Desc.getT809PlatId();
        JSONObject msgData = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        JSONObject data = msgData.getJSONObject("data");
        // ????????????
        OrganizationLdap org = organizationService.getOrgInfoByName(data.getString("enterprise"));
        if (org == null) {
            return;
        }
        SuperPlatformMsg msg = new SuperPlatformMsg();
        // ???????????????????????????id
        t809Message.getMsgHead().setHandleId(msg.getId());
        final PlantParam param = connectionParamsSetDao.getConnectionInfoById(platformId);
        data.put("platformName", Optional.ofNullable(param).map(PlantParam::getPlatformName).orElse(null));
        msgData.put("data", data);
        t809Message.setMsgBody(msgData);
        message.setData(t809Message);
        // ?????????????????????id
        msg.setGroupId(org.getUuid());
        // ????????????id
        msg.setPlatformId(platformId);
        // ??????809??????
        msg.setType(2);
        // ????????????
        msg.setMsg(JSONObject.toJSONString(message));
        // ????????????
        msg.setTime(new Date());
        // ????????????(??????????????????,??????30??????)
        msg.setExpireTime(getThirtyMinutesLaterDate());
        superPlatformMsgService.saveSuperPlatformMsg(msg);
        send809PlatformMsg(message, org.getUuid(), true);
    }

    /**
     * ??????809??????????????????
     */
    private void standard809HandleSave(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (msgBody == null) {
            return;
        }
        // ???????????????????????????????????????????????????????????????809???????????????
        MsgDesc desc = message.getDesc();
        String plateFormId = desc.getT809PlatId();
        if (StrUtil.isBlank(plateFormId)) {
            return;
        }
        T809AlarmForwardInfo result = getT809AlarmForwardInfo(t809Message, msgBody, plateFormId);
        message.setData(t809Message);
        AdasRiskDisposeRecordForm adasRiskDisposeRecordForm = makeAdasRiskDisposeRecord(result);
        if (adasRiskDisposeRecordForm != null) {
            //??????????????????
            if ("6".equals(adasRiskDisposeRecordForm.getStatus())) {
                //???????????????????????????????????????
                ack2Superior(t809Message, plateFormId, result, adasRiskDisposeRecordForm.getHandleType());
                return;
            }
            //?????????
            sendMsgToUserAndSaveMsg(message, result);
            return;
        }
        //808??????
        AlarmHandle alarmHandle = alarmIsAlarmHandle(result);
        // ?????????????????????????????????,??????????????????????????????809???????????????
        if (alarmHandle == null) {
            sendMsgToUserAndSaveMsg(message, result);
            return;
        }
        // ??????????????????
        int handleStatus = alarmHandle.getStatus();
        // ???????????????
        if (handleStatus != 0) {
            ack2Superior(t809Message, plateFormId, result, alarmHandle.getHandleType());
            return;
        }
        sendMsgToUserAndSaveMsg(message, result);
    }

    private T809AlarmForwardInfo getT809AlarmForwardInfo(T809Message t809Message, JSONObject msgBody,
        String plateFormId) {
        JSONObject msgData = msgBody.getJSONObject("data");
        Integer connectProtocolType = connectionParamsSetDao.getConnectionProtocolTypeById(plateFormId);
        if (connectProtocolType == 26) {
            //??????809
            T809AlarmForwardInfoQuery query = new T809AlarmForwardInfoQuery();
            query.setMsgId(5122);
            query.setQueryStartTime(msgData.getLong("warnTime") * 1000);
            query.setPlateFormIdStr(plateFormId);

            T809AlarmForwardInfo info = getXizang809AlarmForwardMsg(query);
            if (info != null) {
                info.setMsgSn(msgData.getInteger("supervisionId"));
            }
            return info;
        }
        T809AlarmForwardInfoQuery queryParam = getAlarmQueryParam(plateFormId, msgData, connectProtocolType);
        t809Message.getMsgHead().setProtocolType(connectProtocolType);
        return getAlarmForwardMsg(queryParam);
    }

    private T809AlarmForwardInfo getAlarmForwardMsg(T809AlarmForwardInfoQuery query) {
        Map<String, String> params = new HashMap<>(8);
        params.put("msgId", String.valueOf(query.getMsgId()));
        params.put("plateFormId", query.getPlateFormIdStr());
        params.put("queryStartTime", String.valueOf(query.getQueryStartTime()));
        params.put("queryEndTime", String.valueOf(query.getQueryEndTime()));
        params.put("msgSn", String.valueOf(query.getMsgSn()));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_809_ALARM_FORWARD_MSG, params);
        return PaasCloudUrlUtil.getResultData(str, T809AlarmForwardInfo.class);
    }

    private T809AlarmForwardInfo getXizang809AlarmForwardMsg(T809AlarmForwardInfoQuery query) {
        Map<String, String> params = new HashMap<>(8);
        params.put("msgId", String.valueOf(query.getMsgId()));
        params.put("plateFormId", query.getPlateFormIdStr());
        params.put("queryStartTime", String.valueOf(query.getQueryStartTime()));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_XIZANG_809_ALARM_FORWARD_MSG, params);
        return PaasCloudUrlUtil.getResultData(str, T809AlarmForwardInfo.class);
    }

    /**
     * ??????????????????
     */
    private void ack2Superior(T809Message t809Message, String plateFormId, T809AlarmForwardInfo result,
        String handleType) {
        Integer alarmHandleResult = getAlarmHandle(handleType);
        // ????????????????????????
        alarmHandleACK(result, t809Message.getMsgHead(), alarmHandleResult, plateFormId);
    }

    /**
     * ???????????????????????????????????????null
     */
    private AdasRiskDisposeRecordForm makeAdasRiskDisposeRecord(T809AlarmForwardInfo result) {
        if (result == null) {
            return null;
        }
        String eventId = result.getEventId();
        if (StringUtils.isBlank(eventId)) {
            return null;
        }
        AdasRiskDisposeRecordForm risk = new AdasRiskDisposeRecordForm();
        //??????????????????
        Set<String> riskIdSet = adasElasticSearchService.esGetRiskIdByEventId(null, eventId);
        if (CollectionUtils.isEmpty(riskIdSet)) {
            return risk;
        }
        AdasRiskDisposeRecordForm adasRiskDisposeRecordForm =
                adasRiskService.getRiskDisposeRecordsById(UuidUtils.getBytesFromStr(riskIdSet.iterator().next()));
        if (adasRiskDisposeRecordForm == null) {
            return risk;
        }
        return adasRiskDisposeRecordForm;
    }

    /**
     * ????????????????????????
     */
    private Integer getAlarmHandle(String handleType) {
        int handleResult;
        switch (handleType) {
            case "????????????":
            case "??????":
            case "???????????????":
                handleResult = 1;
                break;
            case "????????????":
            case "????????????":
                handleResult = 2;
                break;
            case "????????????":
                handleResult = 3;
                break;
            case "?????????":
            default:
                handleResult = 0;
                break;
        }
        return handleResult;
    }

    /**
     * ???809???????????????????????????
     */
    private void sendMsgToUserAndSaveMsg(Message message, T809AlarmForwardInfo alarmForwardInfo) {
        MsgDesc t809Desc = message.getDesc();
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        JSONObject msgData = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody())).getJSONObject("data");
        String orgId;
        Integer connectProtocolType = t809Message.getMsgHead().getProtocolType();
        if (alarmForwardInfo != null) {
            String monitorId = alarmForwardInfo.getMonitorId();
            Map<String, BindDTO> bindDTOMap =
                VehicleUtil.batchGetBindInfosByRedis(Collections.singleton(monitorId), Lists.newArrayList("orgId"));
            if (MapUtils.isEmpty(bindDTOMap)) {
                orgId = alarmForwardInfo.getGroupId();
            } else {
                orgId = bindDTOMap.get(monitorId).getOrgId();
            }
            // ?????????????????????809-2019,????????????????????????
            if (CONNECT_PROTOCOL_TYPE_808_2019.contains(connectProtocolType)) {
                if (!msgBody.containsKey("vehicleNo")) {
                    // ?????????
                    msgBody.put("vehicleNo", alarmForwardInfo.getMonitorName());
                }
                if (!msgBody.containsKey("vehicleColor")) {
                    // ????????????
                    msgBody.put("vehicleColor", alarmForwardInfo.getPlateColor());
                }
            }
            // 808????????????
            if (!msgData.containsKey("alarmType")) {
                // ????????????
                msgData.put("alarmType", alarmForwardInfo.getAlarmType());
            }
            if (!msgData.containsKey("alarmStartTime")) {
                // ??????????????????
                msgData.put("alarmStartTime", alarmForwardInfo.getAlarmStartTime());
            }
            if (!msgData.containsKey("monitorId")) {
                msgData.put("monitorId", alarmForwardInfo.getMonitorId());
            }
            //??????????????????????????????(????????????)
            if (ProtocolTypeUtil.isActiveSecurityStandard(String.valueOf(connectProtocolType))) {
                if (!msgData.containsKey("eventId")) {
                    msgData.put("eventId", alarmForwardInfo.getEventId());
                }
                if (!msgData.containsKey("time")) {
                    msgData.put("time", alarmForwardInfo.getTime());
                }
            }

        } else {
            orgId = connectionParamsSetDao.getConnectionGroupIdById(t809Desc.getT809PlatId());
            // ?????????????????????809-2019,????????????????????????
            if (CONNECT_PROTOCOL_TYPE_808_2019.contains(connectProtocolType)) {
                msgData.put("warnType", 0);
            }
        }
        msgBody.put("data", msgData);
        t809Message.setMsgBody(msgBody);
        message.setData(t809Message);
        // ?????????????????????
        // ???????????????????????????????????????ldap
        OrganizationLdap org = organizationService.getOrganizationByUuid(orgId);
        if (org == null) {
            return;
        }
        // ??????????????????(UTC????????????,????????????)
        long deadline = msgData.containsKey("supervisionEndTime") ? msgData.getLong("supervisionEndTime") :
            System.currentTimeMillis() / 1000;
        // ????????????id
        String platformId = t809Desc.getT809PlatId();
        SuperPlatformMsg msg = new SuperPlatformMsg();
        // ???????????????????????????id
        t809Message.getMsgHead().setHandleId(msg.getId());
        message.setData(t809Message);
        // ??????id
        msg.setGroupId(org.getUuid());
        // ????????????id
        msg.setPlatformId(platformId);
        // ??????
        msg.setType(1);
        // ????????????
        msg.setTime(new Date());
        // ????????????(??????????????????)
        msg.setExpireTime(new Date(deadline * 1000));
        // ????????????
        msg.setMsg(JSONObject.toJSONString(message));
        // ????????????
        superPlatformMsgService.saveSuperPlatformMsg(msg);
        // ????????????
        send809PlatformMsg(message, orgId, false);
    }

    /**
     * ????????????809????????????????????????
     */
    private void extend809AlarmHandle(Message message) {
        MsgDesc t809Desc = message.getDesc();
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject msgData = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody())).getJSONObject("data");
        // ??????????????????????????????????????????ldap
        OrganizationLdap org = organizationService.getOrgInfoByName(msgData.getString("enterprise"));
        if (org != null) {
            // ????????????id
            String platformId = t809Desc.getT809PlatId();
            String groupId = org.getUuid();
            SuperPlatformMsg msg = new SuperPlatformMsg();
            t809Message.getMsgHead().setHandleId(msg.getId()); // ???????????????????????????id
            message.setData(t809Message);
            msg.setGroupId(org.getUuid()); // ??????id
            msg.setPlatformId(platformId); // ????????????id
            msg.setType(3); // ??????
            msg.setMsg(JSONObject.toJSONString(message)); // ????????????
            msg.setTime(new Date()); // ????????????
            if (msgData.containsKey("deadTime")) {
                Long deadTimeLong = msgData.getLong("deadTime");
                msg.setExpireTime(new Date(deadTimeLong * 1000)); // ????????????(??????????????????)
            } else {
                msg.setExpireTime(new Date()); // ????????????(??????????????????)
            }
            // ????????????
            superPlatformMsgService.saveSuperPlatformMsg(msg);
            // ????????????
            send809PlatformMsg(message, groupId, false);
        }
    }

    /**
     * ??????????????????30?????????????????????
     */
    private Date getThirtyMinutesLaterDate() {
        try {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.MINUTE, 30);
            return now.getTime();
        } catch (Exception e) {
            log.error("??????????????????30???????????????????????????");
            return new Date();
        }

    }

    /**
     * ????????????809?????????????????????????????????
     */
    private List<String> get809MsgSendTarget(Integer objectType, String serverIP, String msgGNSSCenterId) {
        if (null == objectType) {
            return new ArrayList<>();
        }
        try {
            List<String> groupIds = new ArrayList<>();
            switch (objectType) {
                case 3:
                case 4:
                case 5:
                    groupIds = connectionParamsSetService.getGroupId(null, serverIP);
                    break;
                case 6:
                case 7:
                case 8:
                case 9:
                    break;
                default:
                    groupIds = connectionParamsSetService.getGroupId(msgGNSSCenterId, serverIP);
                    break;
            }
            return groupIds;
        } catch (Exception e) {
            log.error("????????????809?????????????????????????????????", e);
            return new ArrayList<>();
        }
    }

    /**
     * ????????????809???????????????????????????????????????(groupId???id)
     */
    private List<PlantParam> getTargetInfo(Integer objectType, String platformId, String businessCode) {
        try {
            List<PlantParam> params = new ArrayList<>();
            PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(platformId);
            if (plantParam == null) {
                return params;
            }
            switch (objectType) {
                case 0:
                case 1:
                    // ???????????????????????????
                    params.add(plantParam);
                    break;
                case 2:
                    // ??????????????????????????????
                    String groupId = plantParam.getGroupId();
                    // ??????
                    OrganizationLdap connect = organizationService.getOrganizationByUuid(groupId);
                    if (connect == null) {
                        break;
                    }
                    // ????????????id??????????????????????????????????????????????????????
                    List<OrganizationLdap> orgs = organizationService.getOrgChildList(connect.getId().toString());
                    if (orgs == null) {
                        break;
                    }
                    orgs.forEach(org -> {
                        String license = org.getLicense(); // ????????????????????????
                        if (StringUtils.isNotBlank(license) && businessCode.equals(license)) {
                            PlantParam parm = new PlantParam();
                            parm.setId(platformId);
                            parm.setIp(plantParam.getIp());
                            parm.setGroupId(org.getUuid());
                            parm.setPlatformName(plantParam.getPlatformName());
                            params.add(parm);
                        }
                    });
                    break;
                case 3: // ??????????????????????????????
                    String serverIp = plantParam.getIp(); // ?????????IP??????
                    // ???????????????IP?????????????????????IP?????????809???????????????
                    params.addAll(connectionParamsSetService.getPlatformFlag(null, serverIp));
                    break;
                default:
                    break;
            }
            return params;
        } catch (Exception e) {
            log.error("????????????809????????????????????????", e);
            return new ArrayList<>();
        }
    }

    /**
     * ???809?????????????????????
     */
    private void receive1206Msg(Object obj) throws ParseException {
        // ????????????0x9206???????????????
        if (obj instanceof Message) {
            Message m = (Message) obj;
            JSONObject msgBody = JSONObject.parseObject(m.getData().toString()).getJSONObject("msgBody");
            MsgDesc desc = m.getDesc();
            // ?????????
            String msgSNACK = msgBody.getString("msgSNACK");
            String vehicleId = desc.getMonitorId();
            String deviceId = desc.getDeviceId();
            if (StringUtils.isNotBlank(msgSNACK) && StringUtils.isNotBlank(vehicleId)) {

                Map<String, Object> map = new HashMap<>();
                map.put("swiftNumber", msgSNACK);
                map.put("vehicleId", vehicleId);
                map.put("parameterType", ConstantUtil.ADAS_EVENT_CODE_0x9206);
                Directive directive = parameterService.selectDirectiveByConditions(map);
                // ADAS???1206
                if (directive != null && "ADAS".equals(directive.getRemark())) {
                    // ????????????
                    if (msgBody.containsKey("result") && "0".equals(msgBody.getInteger("result").toString())) {

                        log.info("??????0x1206, ????????????{} ????????????{}", directive.getParameterName(), new Date());
                        // ??????ftp,??????????????????
                        String fileName = directive.getParameterName();
                        boolean result =
                            operateFileForFTP(ftpUserName, ftpPassword, ftpPath, ftpHostClbs, ftpPortClbs, fileName,
                                vehicleId);
                        if (!result) {
                            log.error("????????????????????????");
                        }
                        // ????????????
                        SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSNACK, deviceId);
                        if (info != null) {
                            SubscibeInfoCache.getInstance().delTable(info);
                        }
                    }

                } else { // ????????????0X1206
                    // ????????????
                    Integer result = msgBody.getInteger("result");
                    FileUploadForm form =
                        SubscibeInfoCache.getInstance().getSubscibeMsgMap(Integer.valueOf(msgSNACK), deviceId);
                    // String oldFileName = "";
                    if (result != null && result == 0) { // ????????????
                        if (form != null) {
                            // ??????ftp????????????
                            Date fileStartTime = DateUtils.parseDate("20" + form.getStartTime(), DATE_FORMAT);
                            Date fileEndTime = DateUtils.parseDate("20" + form.getEndTime(), DATE_FORMAT);
                            Date uploadTime = new Date();
                            // ????????????
                            videoFlowDao.insert(VideoTrafficInfo.builder()
                                    .vehicleId(form.getVehicleId())
                                    .channel(form.getChannelNumber())
                                    .startTime(fileStartTime)
                                    .endTime(fileEndTime)
                                    .flowValue(Long.parseLong(form.getFilesize()))
                                    .stopFlag(0)
                                    .build());
                            // ??????????????????
                            String fileUrl = form.getFileUploadPath();
                            // ftp ????????????????????????
                            String fileName =
                                form.getChannelNumber() + "_" + form.getStartTime() + "_" + form.getEndTime();

                            FTPClient ftpClient = null;
                            String tempName = "";
                            boolean motifyFlag = true;
                            try {
                                ftpClient = resourceListService.getFTPClient(FtpClientUtil.FTP_NAME, form.getTempUrl());
                                tempName = FtpClientUtil.getFileName("/" + form.getTempUrl(), ftpClient);
                                resourceListService
                                    .motifyFtpFile(ftpClient, tempName, form.getTempUrl(), fileName, fileUrl);
                            } catch (Exception e) {
                                motifyFlag = false;
                                log.info("??????????????????");
                            } finally {
                                try {
                                    if (ftpClient != null) {
                                        ftpClient.disconnect();
                                    }
                                } catch (IOException e) {
                                    log.error("??????FTP?????????????????????", e);
                                }
                            }

                            VideoFTPForm videoFTPForm = VideoFTPForm.builder().vehicleId(vehicleId).tempName(tempName)
                                .tempUrl(form.getTempUrl() + "/" + tempName).channelNumber(form.getChannelNumber())
                                .startTime(fileStartTime).endTime(fileEndTime).alarmType(form.getAlarmSign()).type(0)
                                .uploadTime(uploadTime).build();
                            if (motifyFlag) {
                                videoFTPForm.setUrl(fileUrl + "/" + fileName + ".mp4");
                                videoFTPForm.setName(fileName + ".mp4");
                            }
                            // ??????ftp??????
                            resourceListService.insertFTPRecord(videoFTPForm);
                        }
                        SubscibeInfoCache.getInstance().removeSubscibeMsgMap(Integer.valueOf(msgSNACK), deviceId);
                    }
                    // ??????????????????
                    //resourceListService.deleteTempFile(form.getTempUrl());

                    // ????????????
                    addVideoFileUploadLog(desc.getMonitorId());

                    // ??????
                    SubscibeInfo info = SubscibeInfoCache.getInstance().getUserNameByMsgSnDid(msgSNACK, deviceId);
                    if (info != null) {
                        SubscibeInfoCache.getInstance().delTable(info);
                        if (info.getUserName() != null && !"".equals(info.getUserName())) {
                            simpMessagingTemplateUtil
                                .sendStatusMsg(info.getUserName(), ConstantUtil.FILE_UPLOAD_STATUS, obj);
                        } else {
                            simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.FILE_UPLOAD_STATUS, obj);
                        }
                    }
                }
            }
        }
    }

    private void addVideoFileUploadLog(String vehicleId) {
        LogSearchForm form = new LogSearchForm();

        try {
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            form.setEventDate(new Date());
            form.setLogSource("3");
            form.setModule("REALTIMEVIDEO");
            form.setMonitoringOperation("???????????????" + vehicle[0] + "???????????????");
            form.setMessage("????????????(" + vehicle[0] + ")_??????????????????)");
            form.setBrand(vehicle[0]);
            form.setPlateColor(Integer.valueOf(vehicle[1]));
            logSearchService.addLogBean(form);
        } catch (Exception e) {
            log.error("?????????????????????????????????");
        }
    }

    /**
     * ?????????????????????
     */
    private void multimedia(Message message) {
        MsgDesc desc = message.getDesc();
        T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
        MultimediaData muData = JSON.parseObject(t808Message.getMsgBody().toString(), MultimediaData.class);
        muData.setMonitorName(desc.getMonitorName());
        saveMultimedia(desc.getMonitorId(), desc.getDeviceNumber(), muData);
        ProfessionalsInfo professionalsInfo = adasSubcibeTable.getFaceRecognitionCache(desc.getDeviceNumber());
        if (professionalsInfo != null && Objects.equals(muData.getFormatCode(), 0)) {
            // 0702?????????0801  ????????????809???????????? 1241
            pushFaceRecognition(message, professionalsInfo, muData);
        }
    }

    private void pushFaceRecognition(Message message, ProfessionalsInfo info, MultimediaData muData) {
        MsgDesc desc = message.getDesc();
        String deviceNumber = desc.getDeviceNumber();
        String driverName = info.getName();
        VehicleDTO vehicleDTO = vehicleService.getVehicleDTOByDeviceNumber(deviceNumber);
        List<ProfessionalDTO> professionals =
            newProfessionalsDao.getProfessionalsByNameAndCardNum(driverName, info.getCardNumber());
        //??????809????????????
        List<PlantParam> connectionInfo = connectionParamsConfigDao.getConnectionInfoByVehicleId(vehicleDTO.getId());
        if (!CosUtil.areNotEmpty(professionals, connectionInfo)) {
            return;
        }
        // ????????????
        String drivingLicenseNo = professionals.get(0).getDrivingLicenseNo();
        //??????????????????
        String licence = professionals.get(0).getCardNumber();
        T809MsgBody data;
        //???????????????????????????,zw???????????????
        if (ProtocolTypeUtil.ZW_PROTOCOL_808_2019.equals(vehicleDTO.getDeviceType())) {
            data =
                FaceRecognitionDataZw.getInstance(muData.getData(), driverName, vehicleDTO, drivingLicenseNo, licence);
        } else {
            //??????
            data = FaceRecognitionData.getInstance(muData.getData(), driverName, vehicleDTO, drivingLicenseNo);
        }
        PlantParam plantParam = connectionInfo.get(0);
        int msgId = ConstantUtil.T809_UP_EXG_MSG_FACE_PHOTO_AUTO;
        T809Message t809Msg = MsgUtil.getT809Message(msgId, plantParam.getIp(), plantParam.getCenterId(), data);
        Message msg = MsgUtil.getMsg(msgId, t809Msg).assembleDesc809(plantParam.getId());
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(msg);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(msg);

    }

    /**
     * ??????1242
     */
    private void reportFaceRecognition(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        InputStream fileInputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            JSONObject object = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
            String vehicleNo = object.getString("vehicleNo");
            VehicleDTO vehicleDTO = vehicleService.getBindVehicleDTOByBrand(vehicleNo);
            // ????????????????????????????????????  ??????key???F3??????
            String driverList = RedisHelper.hget(HistoryRedisKeyEnum.IC_DRIVER_LIST.of(), vehicleDTO.getId());
            if (StringUtils.isEmpty(driverList)) {
                return;
            }
            JSONObject driverInfo = JSON.parseObject(driverList);
            String driverName = driverInfo.getString("driverName");
            String icCardNumber = driverInfo.getString("certificationID");
            List<ProfessionalDTO> professionals =
                newProfessionalsDao.getProfessionalsByNameAndCardNum(driverName, icCardNumber);
            List<PlantParam> connectionInfo =
                connectionParamsConfigDao.getConnectionInfoByVehicleId(vehicleDTO.getId());
            if (!CosUtil.areNotEmpty(professionals, connectionInfo)) {
                return;
            }
            // ????????????
            String photographPath = professionals.get(0).getPhotograph();
            FTPConfig ftpConfig = getProfessionalFtpConfig();
            fileInputStream = FtpClientUtil
                .getFileInputStream(ftpConfig.getUserName(), ftpConfig.getPassWord(), ftpConfig.getHost(),
                    ftpConfig.getPort(), ftpConfig.getPath(), photographPath);
            outputStream = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int rc;
            while ((rc = fileInputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, rc);
            }
            byte[] bytes = outputStream.toByteArray();
            T809MsgBody data;
            String drivingLicenseNo = professionals.get(0).getDrivingLicenseNo();
            String licence = professionals.get(0).getCardNumber();
            //????????????(zw??????)
            if (ProtocolTypeUtil.ZW_PROTOCOL_808_2019.equals(vehicleDTO.getDeviceType())) {
                data = FaceRecognitionDataZw.getInstance(bytes, driverName, vehicleDTO, drivingLicenseNo, licence)
                    .assembleSourceInfo(message.getDesc());
            } else {
                //??????
                data = FaceRecognitionData.getInstance(bytes, driverName, vehicleDTO, drivingLicenseNo);
            }

            PlantParam plantParam = connectionInfo.get(0);
            int msgId = ConstantUtil.T809_UP_WARN_MSG_URGE_TODO_ACK_ACK;
            T809Message t809Msg = MsgUtil.getT809Message(msgId, plantParam.getIp(), plantParam.getCenterId(), data);
            Message msg = MsgUtil.getMsg(msgId, t809Msg).assembleDesc809(plantParam.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(msg);
        } catch (Exception e) {
            log.error("??????1242??????????????????", e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * ?????????????????????????????????????????????
     */
    public void inspectionLogAck(Message message) throws Exception {

        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        JSONObject t809MsgBody = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()));
        if (t809MsgBody == null) {
            return;
        }
        JSONObject t809MsgBodyData = t809MsgBody.getJSONObject("data");
        // ????????????id
        Integer objectType = t809MsgBodyData.getInteger("objectType");
        // ????????????id
        String platformId = message.getDesc().getT809PlatId();
        // ????????????????????????(????????????)
        String objectId = t809MsgBodyData.getString("objectId");
        List<PlantParam> info = getTargetInfo(objectType, platformId, objectId);
        Integer msgSn = t809Message.getMsgHead().getMsgSn();
        Long startTime = t809MsgBodyData.getLong("startTime");
        Long endTime = t809MsgBodyData.getLong("endTime");
        Long answerTime = t809MsgBodyData.getLong("answerTime");
        boolean flag =
            msgSn == null || startTime == null || endTime == null || answerTime == null || StringUtils.isEmpty(objectId)
                || objectType == null || info.isEmpty();
        if (flag) {
            log.info("??????0x9311???????????????{}", JSON.toJSONString(t809MsgBodyData));
            return;
        }
        Set<String> orgId = new HashSet<>(info.size());
        Map<String, PlantParam> map = new HashMap<>(10);
        info.forEach(o -> {
            if (ProtocolTypeUtil.T809_HEIPROTOCOL_809_2019.equals(o.getProtocolType().toString())) {
                orgId.add(o.getGroupId());
            }
            if (o.getId().equals(platformId)) {
                map.put(o.getId(), o);
            }
        });
        if (orgId.isEmpty()) {
            log.info("??????0x9311,???????????????2019?????????????????????");
            return;
        }
        List<LogSearch> data = logSearchService.getByTime(startTime, endTime, orgId);
        String ipAddress = null;
        String res = "??????????????????????????????????????????";
        if (data.isEmpty()) {
            log.info("??????0x9311?????????????????????{}??????????????????{}??????????????????", startTime.toString(), endTime.toString());
            ipAddress = Inet4Address.getLocalHost().getHostAddress();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (LogSearch da : data) {
                if (ipAddress == null && StringUtils.isNotEmpty(da.getIpAddress())) {
                    ipAddress = da.getIpAddress();
                }
                stringBuilder.append("#????????????").append(da.getUsername()).append(",").append("???????????????")
                    .append(da.getEventDate().toString()).append(",").append("???????????????").append(da.getMessage());
            }
            res = stringBuilder.toString();
        }
        CheckAck ack = new CheckAck();
        ack.setObjectId(objectId);
        ack.setObjectType(objectType);
        ack.setResponderLogs(res);
        ack.setResponderIpAddress(ipAddress);
        ack.setSourceDataType(ConstantUtil.DOWN_PLATFORM_MSG_INSPECTION_LOG_REQ);
        ack.setSourceMsgSn(msgSn);
        ack.setInfoLength(43 + res.length());
        PlantParam plantParam = map.get(platformId);
        ExchangeInfo exchangeInfo = new ExchangeInfo();
        exchangeInfo.setDataType(ConstantUtil.UP_PLATFORM_MSG_INSPECTION_LOG_ACK);
        exchangeInfo.setDataLength(ack.getInfoLength());
        exchangeInfo.setData(MsgUtil.objToJson(ack));
        T809Message msg = MsgUtil
            .getT809Message(ConstantUtil.T809_UP_PLATFORM_MSG, plantParam.getIp(), plantParam.getCenterId(),
                exchangeInfo);
        Message upMsg = MsgUtil.getMsg(ConstantUtil.T809_UP_PLATFORM_MSG, msg).assembleDesc809(platformId);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(upMsg);
    }

    /**
     * ?????????????????????
     */
    private void saveMultimedia(String vehicleId, String deviceNumber, MultimediaData multimediaData) {
        log.info("????????????" + vehicleId + "0x0801" + " ???????????????" + multimediaData.getType());
        String path = getSaveMediaPath(multimediaData, vehicleId);
        //???????????????windows???????????????????????????????????????????????????
        //path = path.substring(1);
        transferMediaFile(vehicleId, multimediaData, path);
        multimediaData.setVid(vehicleId);
        if (StringUtils.isNotBlank(path) && path.endsWith(".jpeg") && multimediaData.getId() != null
            && multimediaData.getEventCode() == 9) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("deviceNumber", deviceNumber);
            jsonObject.put("path", path);
            IcCardMessage icCardMessage =
                new IcCardMessage(deviceNumber, jsonObject, ConstantUtil.T808_MULTIMEDIA_DATA);
            handleIcCardMessage(icCardMessage);
        } else {
            uploadFile(multimediaData);
            icProfessionalCheck(vehicleId, multimediaData);

            deviceMessageHandler.saveMedia(multimediaData);
        }
        log.info("??????0801??????,???????????????:" + vehicleId + "_" + multimediaData.getId());
    }

    private void uploadFile(MultimediaData multimediaData) {
        String newPath = null;
        File file = new File(multimediaData.getMediaUrl());
        try {
            FileInputStream inputStream = new FileInputStream(file);
            newPath = fastDFSClient.uploadFile(inputStream, file.length(), file.getName());
        } catch (Exception e) {
            log.error("??????0801?????????fastDFS?????????", e);
        } finally {
            multimediaData.setMediaUrlNew(newPath);
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                log.error("?????????????????????0801????????????", e);
            }
        }
    }

    private void transferMediaFile(String vehicleId, MultimediaData multimediaData, String path) {
        File file = new File(path);
        try {
            FileUtil.save(path, multimediaData.getData());
            switch (multimediaData.getType()) {
                case 2:  // ???????????????mp4
                    File mp4file;
                    try {
                        mp4file = FFmpegCommandRunner.coverToMp4of0801(file);
                    } catch (Exception e) {
                        //????????????????????????????????????h264???????????????
                        RandomAccessFile read = new RandomAccessFile(file, "r");
                        FileUtil.h264ReadAndWriteFile(read, path);
                        mp4file = FFmpegCommandRunner.coverToMp4of0801(file);
                    }
                    multimediaData.setMediaName(mp4file.getName());
                    multimediaData.setMediaUrl(mp4file.getPath());
                    break;
                case 1:  // ???MP3
                    File mp3file = FFmpegCommandRunner.coverToMp3(file).getTarget();
                    multimediaData.setMediaName(mp3file.getName());
                    multimediaData.setMediaUrl(mp3file.getPath());
                    break;
                default:
                    String mediaName = path.substring((path.lastIndexOf(File.separator) + 1));
                    multimediaData.setMediaName(mediaName);
                    multimediaData.setMediaUrl(path);
                    //multimediaData.setMediaUrlNew(path.split("media")[1]);
                    break;
            }
        } catch (Exception e) {
            log.error("???id???" + vehicleId + "0801???????????????????????????", e);
            multimediaData.setMediaName(file.getName());
        }
    }

    private String getSaveMediaPath(MultimediaData multimediaData, String vehicleId) {
        String url = this.getClass().getResource(File.separator).getPath();
        String path = url.substring(0, url.indexOf("WEB-INF")).replaceAll("/", Matcher.quoteReplacement(File.separator))
            + "resources" + File.separator + "img" + File.separator + "media" + File.separator + vehicleId
            + File.separator + vehicleId + "_" + multimediaData.getId();
        switch (multimediaData.getFormatCode()) {
            case 0:
                path += ".jpeg";
                break;
            case 1:
                path += ".tif";
                break;
            case 2:
                path += ".mp3";
                break;
            case 3:
                path += ".wav";
                break;
            case 4:
            case 5:
                path += ".wmv";
                break;
            default:
                if (multimediaData.getType() == 0) {
                    path += ".jpeg";
                } else if (multimediaData.getType() == 1) {
                    path += ".wav";
                } else if (multimediaData.getType() == 2) {
                    path += ".wmv";
                }
                break;
        }
        return path;
    }

    //??????0801?????????ic?????????????????????
    public void dealIcProfessionalPic(String deviceNumber, String path) {
        String professionalStr = RedisHelper.getString(HistoryRedisKeyEnum.IC_PROFESSIONAL_INFO.of(deviceNumber));
        try {
            if (professionalStr != null) {
                //??????ic???????????????????????????
                ProfessionalDTO professionalDTO = JSONObject.parseObject(professionalStr, ProfessionalDTO.class);
                if (professionalDTO.getPhotograph() == null) {
                    String time = DateUtil.getDateToString(new Date(), DateUtil.DATE_FORMAT);
                    String mediaName = professionalDTO.getId() + "_" + time + ".jpeg";
                    uploadPicToFtp(mediaName, path, getProfessionalFtpConfig());
                    //??????????????????????????????mysql
                    newProfessionalsDao.updateIcCardPhotoGraph(mediaName, professionalDTO.getId());
                    professionalDTO.setPhotograph(mediaName);
                    Map<String, String> map = ProfessionalServiceImpl.setValueToMap(professionalDTO);
                    RedisHelper.addToHash(RedisKeyEnum.PROFESSIONAL_INFO.of(professionalDTO.getId()), map);
                }
            }
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            log.error("??????ic??????????????????fastDFS??????", e);
        }
    }

    //??????????????????
    private void icProfessionalCheck(String vehicleId, MultimediaData multimediaData) {
        String key = vehicleId + "_" + multimediaData.getWayId() + "_driverCheck";
        if (adasSubcibeTable.get(key) != null && "1".equals(adasSubcibeTable.get(key).toString())) {
            JSONObject jsonObject = new JSONObject();
            String mediaUrl = multimediaData.getMediaUrlNew();
            if (sslEnabled) {
                webServerUrl = "/";
            }
            jsonObject.put("mediaUrl", webServerUrl + mediaUrl);
            String address;
            address = new String(multimediaData.getGpsInfo().getOriginal(), StandardCharsets.ISO_8859_1);
            jsonObject.put("address", address);
            adasSubcibeTable.put(key, jsonObject);
        }
    }

    private void uploadPicToFtp(String mediaName, String path, FTPConfig ftpConfig) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            boolean success = FtpClientUtil
                .uploadFile(ftpConfig.getHost(), ftpConfig.getPort(), ftpConfig.getUserName(), ftpConfig.getPassWord(),
                    ftpConfig.getPath(), mediaName, fis);
            if (success) {
                Files.deleteIfExists(Paths.get(path));
            }
        } catch (Exception e) {
            log.info("??????????????????ftp??????");
        } finally {
            IOs.closeQuietly(fis);

        }

    }

    private FTPConfig getProfessionalFtpConfig() {
        FTPConfig ftpConfig = new FTPConfig();
        ftpConfig.setHost(ftpHostClbs);
        ftpConfig.setPassWord(ftpPassword);
        ftpConfig.setUserName(ftpUserName);
        ftpConfig.setPath(professionalFtpPath);
        ftpConfig.setPort(ftpPortClbs);
        return ftpConfig;
    }

    private void deviceProperty(String deviceNumber, JSONObject msgBody, Message message) {
        RedisHelper.setString(HistoryRedisKeyEnum.DEVICE_QUERY_ANSWER.of(deviceNumber), msgBody.toJSONString());
        simpMessagingTemplateUtil.sendStatusMsg(ConstantUtil.WEBSOCKET_DEVICE_REPORT_TOPIC, message);
    }

    /**
     * @param ftpUserName ftp?????????
     * @param ftpPassword ftp??????
     * @param ftpPath     ????????????
     * @param ftpHost     FTP??????????????????
     * @param ftpPort     FTP???????????????
     * @param fileName    ????????????
     */
    public static boolean operateFileForFTP(String ftpUserName, String ftpPassword, String ftpPath, String ftpHost,
        int ftpPort, String fileName, String vehicleId) {

        File directory;
        boolean flag = false;
        String path;
        File mp4File = null;
        FTPClient ftp = null;
        File file = null;
        try {
            ftp = FtpClientUtil.getFTPClient(ftpUserName, ftpPassword, ftpHost, ftpPort, ftpPath);
            if (ftp != null) {
                // ????????????fileName,???????????????????????????,???????????????Id
                Map<String, String> map = getPurposeFileName(fileName, ftp);
                if (map != null && map.size() > 0) {
                    fileName = map.get("fileName");
                    try (InputStream in = ftp.retrieveFileStream(ftpPath + "/" + fileName)) {
                        // ????????????
                        if (in != null) {
                            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                                directory = new File("c:/adasvideo/temp/");
                            } else {
                                directory = new File("/usr/local/adasvideo/temp/");
                            }
                            // ????????????
                            if (!directory.exists()) {
                                directory.mkdirs();
                            }
                            path = directory.getAbsolutePath();
                            file = new File(path + File.separator + fileName);
                            try (OutputStream out = new FileOutputStream(file)) {
                                byte[] buff = new byte[1024];
                                int rc;
                                while ((rc = in.read(buff)) > 0) {
                                    out.write(buff, 0, rc);
                                }
                            }
                        } else {
                            log.info(">>======ftp??????????????????????????????======<<");
                        }
                    } finally {
                        // ??????ftp,????????????????????????,??????????????????????????????
                        ftp.completePendingCommand();
                    }
                    // ????????????
                    if (file != null && file.exists()) {
                        // ??????mp4??????
                        VideoFile videoFile = FFmpegCommandRunner.coverToMp4(file);
                        if (videoFile != null) {
                            mp4File = videoFile.getTarget();
                        }
                        // ??????????????????
                        if (!file.delete()) {
                            log.error("???????????????????????????{}??????", file);
                        }
                    } else {
                        log.error("ADAS????????????ftp????????????");
                    }
                    if (mp4File != null && mp4File.length() > 0) {
                        boolean b;
                        try (InputStream fi = new FileInputStream(mp4File)) {
                            // mp4??????????????????????????????ID?????????
                            b = ftp.storeFile(map.get("riskEventId") + ".mp4", fi);
                        }
                        if (b) {
                            // ???media??????????????????
                            addData2MediaTable(map.get("riskEventId"), map.get("riskEventId"), ftpPath,
                                map.get("mediaId"), vehicleId, map.get("riskId"));
                            // ??????date??????,???????????????????????????,?????????
                            ftp.deleteFile(fileName);
                        }
                        if (!mp4File.delete()) {
                            log.error("???????????????????????????{}??????", mp4File);
                        }
                        flag = true;
                    }
                }
                // ??????
                ftp.logout();
            }
        } catch (Exception e) {
            log.error(">>=====??????????????????????????????======<<", e);
        } finally {
            if (ftp != null && ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return flag;
    }

    private static Map<String, String> getPurposeFileName(String fileName, FTPClient ftp) throws IOException {
        Map<String, String> map = new HashMap<>();
        String[] splits = fileName.split("@");
        if (splits.length != 4) {
            log.error(">=======????????????parameter_name????????????======<");
            return null;
        }
        // ???????????????????????????????????????
        String[] fileNames = ftp.listNames(new String(splits[0].getBytes(StandardCharsets.UTF_8), "ISO8859-1") + "*");
        if (fileNames == null || fileNames.length == 0) {
            log.info(">=====FTP???????????????????????????" + splits[0] + "???????????????=======<");
            return null;
        }
        // ??????????????????,??????treeSet,??????????????????,???????????????
        TreeSet<String> set = new TreeSet<>(Arrays.asList(fileNames));
        // ???????????????????????????
        map.put("fileName", set.last());
        map.put("riskEventId", splits[1]);
        map.put("riskId", splits[3]);
        splits = splits[2].split(",");
        if (splits.length > 0) {
            map.put("mediaId", splits[0]);
        }
        return map;
    }

    private static void addData2MediaTable(String fileName, String riskEventId, String ftpPath, String media,
        String vehicleId, String riskId) {
        // ??????zw_m_media???????????????
        MediaService mediaService = SpringBeanUtil.getBean(MediaService.class);
        MediaForm mediaForm = new MediaForm();
        mediaForm.setType(2);
        mediaForm.setMediaName(fileName + ".mp4");
        mediaForm.setMediaUrl(ftpPath + "/" + mediaForm.getMediaName());
        mediaForm.setFlag(1);
        mediaForm.setMediaId(Long.valueOf(media));
        mediaForm.setRiskEventId(riskEventId);
        mediaForm.setVehicleId(vehicleId);
        mediaForm.setRiskId(riskId);
        mediaService.addMedia(mediaForm);

        cacheMedia(mediaForm);
    }

    public static void cacheMedia(MediaForm mediaForm) {
        int expire = 10 * 60 * 60; // ????????????10??????
        JSONObject obj = new JSONObject();
        obj.put("type", 2);
        obj.put("mediaName", mediaForm.getMediaName());
        obj.put("mediaUrl", mediaForm.getMediaUrl());
        String riskListValue = RedisHelper.getString(HistoryRedisKeyEnum.RISK_EVENT_ID.of(mediaForm.getRiskEventId()));
        JSONArray riskList;
        if (riskListValue == null) {
            riskList = new JSONArray();
        } else {
            riskList = JSON.parseArray(riskListValue);
        }
        riskList.add(obj);
        RedisHelper.setString(HistoryRedisKeyEnum.RISK_EVENT_ID.of(mediaForm.getRiskEventId()), riskList.toJSONString(),
            expire);
    }

    /**
     * ????????????????????????
     */
    private void supplementGroupStaticInfo(Message message) {
        T809Message t809Message = JSON.parseObject(JSON.toJSONString(message.getData()), T809Message.class);
        T809MsgHead t809MsgHead = t809Message.getMsgHead();
        t809MsgHead.setMsgID(ConstantUtil.T809_UP_BASE_MSG);
        MainVehicleInfo msgData = JSON.parseObject(JSON.toJSONString(t809Message.getMsgBody()), MainVehicleInfo.class);
        Integer dataType = msgData.getDataType();
        // 0x9601 -> 0x1601
        if (dataType == ConstantUtil.T809_DOWN_BASE_MSG_VEHICLE_ADDED) {
            receive9601Answer1601(message, t809Message, msgData);
            return;
        }
        // 0x9602 -> 0x1602
        if (dataType == ConstantUtil.T809_DOWN_BASE_MSG_ENTERPRISE_ADDED) {
            receive9602Answer1602(message, t809Message, msgData);
            return;
        }
        // 0x9607 -> 0x1607
        if (dataType == ConstantUtil.T809_ENTERPRISE_STATIC_INFO_REQ) {
            receive9607Answer1607(message, t809Message, msgData);
        }

    }

    private void receive9601Answer1601(Message message, T809Message t809Message, MainVehicleInfo msgData) {
        String plateFormId = message.getDesc().getT809PlatId();
        if (plateFormId == null) {
            return;
        }
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(plateFormId);
        if (plantParam == null) {
            return;
        }
        Integer connectProtocolType = plantParam.getProtocolType();
        boolean isProtocolJt2019 = Boolean.FALSE;
        switch (connectProtocolType) {
            case 100:
            case 101:
            case 1011:
            case 1012:
            case 1013:
            case 104:
            case 1091:
                isProtocolJt2019 = Boolean.TRUE;
                break;
            default:
                break;
        }
        String vehicleNo = msgData.getVehicleNo();
        VehicleDTO vehicleDTO = vehicleService.getBindVehicleDTOByBrand(vehicleNo);
        if (vehicleDTO == null) {
            return;
        }
        Integer msgSn = t809Message.getMsgHead().getMsgSn();
        // 0:??????; 1:??????; 2:????????????; 3:??????
        int standard809;
        TypeCacheManger typeCacheManger = TypeCacheManger.getInstance();
        VehicleTypeDTO vehicleType = typeCacheManger.getVehicleType(vehicleDTO.getVehicleType());
        VehiclePurposeDTO vehiclePurpose = typeCacheManger.getVehiclePurpose(vehicleDTO.getVehiclePurpose());
        List<BusinessScopeDTO> businessScope = businessScopeService.getBusinessScope(vehicleDTO.getId());
        if (CollectionUtils.isNotEmpty(businessScope)) {
            vehicleDTO.setScopeBusinessCodes(
                businessScope.stream().map(BusinessScopeDTO::getBusinessScopeCode).collect(Collectors.joining(",")));
        }
        if (vehicleType == null) {
            standard809 = 0;
        } else {
            String codeNum = vehicleType.getCodeNum();
            vehicleDTO.setCodeNum(codeNum);
            standard809 = StringUtils.isBlank(codeNum) ? 0 : VehicleTypeEnum.get809Standard(codeNum);
        }
        if (vehiclePurpose != null) {
            vehicleDTO.setPurposeCodeNum(vehiclePurpose.getCodeNum());
        }
        MainVehicleInfo mainVehicleInfo;
        switch (standard809) {
            case 1:
                mainVehicleInfo = sendFreightVehicleInfo(vehicleDTO, msgSn, isProtocolJt2019);
                break;
            case 3:
                mainVehicleInfo = sendPassengerVehicleInfo(vehicleDTO, msgSn, isProtocolJt2019);
                break;
            default:
                mainVehicleInfo = sendGeneralVehicleInfo(vehicleDTO, msgSn, isProtocolJt2019);
                break;
        }
        if (mainVehicleInfo == null) {
            return;
        }
        message.getDesc().setMsgID(ConstantUtil.T809_UP_BASE_MSG);
        T809Message utilT809Message = MsgUtil
            .getT809Message(ConstantUtil.T809_UP_BASE_MSG, plantParam.getIp(), plantParam.getCenterId(),
                mainVehicleInfo);
        message.setData(utilT809Message);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    private MainVehicleInfo sendFreightVehicleInfo(VehicleDTO vehicleDTO, Integer msgSn, boolean isProtocolJt2019) {
        String orgId = vehicleDTO.getOrgId();
        OrganizationLdap org = organizationService.getOrganizationByUuid(orgId);
        if (org == null) {
            return null;
        }
        String orgName = org.getName();
        String phone = org.getPhone();
        String vehicleId = vehicleDTO.getId();
        String monitorName = vehicleDTO.getName();
        String plateColor = vehicleDTO.getPlateColor() + "";
        FreightVehicleDto freightVehicleDto = new FreightVehicleDto();
        freightVehicleDto.setTransType(vehicleDTO.getPurposeCodeNum());
        freightVehicleDto.setVin(monitorName);
        // ??????????????????
        freightVehicleDto.setTraction(null);
        // ??????????????????
        freightVehicleDto.setTrailerVin(null);
        String provinceId = vehicleDTO.getProvinceId();
        String cityId = vehicleDTO.getCityId();
        if (StringUtils.isNotBlank(provinceId) && StringUtils.isNotBlank(cityId)) {
            freightVehicleDto.setVehicleNationality(provinceId + cityId);
        }
        freightVehicleDto.setVehicleType(vehicleDTO.getCodeNum());
        freightVehicleDto.setRtpn(vehicleDTO.getRoadTransportNumber());
        freightVehicleDto.setOperatorName(orgName);
        freightVehicleDto.setOperatorTel(phone);
        freightVehicleDto.setOwersName(orgName);
        freightVehicleDto.setOwersOrigId(orgId);
        freightVehicleDto.setOwersTel(phone);
        freightVehicleDto.setRtoln(vehicleDTO.getVehiclOperationNumber());
        // ??????????????????
        freightVehicleDto.setVehicleMode(null);
        freightVehicleDto.setVehicleColor(plateColor);
        freightVehicleDto.setVehcileOrigId(vehicleId);
        String driverInfo = assembleDriverInfo(vehicleId);
        freightVehicleDto.setDriverInfo(driverInfo);
        freightVehicleDto.setGuardsInfo(driverInfo);
        freightVehicleDto.setApprovedTonnage(vehicleDTO.getLoadingQuality());
        // ??????????????????
        freightVehicleDto.setDgType(null);
        freightVehicleDto.setBusinessScopeCode(vehicleDTO.getScopeBusinessCodes());
        // ??????????????????
        freightVehicleDto.setCargoName(null);
        // ??????????????????
        freightVehicleDto.setCargoTonnage(null);
        freightVehicleDto.setTransportOrigin(vehicleDTO.getProvenance());
        freightVehicleDto.setTransportDes(vehicleDTO.getDestination());
        // ??????????????????
        freightVehicleDto.setTssl(null);
        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setSourceDataType(ConstantUtil.T809_DOWN_BASE_MSG_VEHICLE_ADDED);
        mainVehicleInfo.setSourceMsgSn(msgSn);
        mainVehicleInfo.setDataType(ConstantUtil.T809_UP_BASE_MSG_VEHICLE_ADDED_ACK);
        UpInfo upInfo = new UpInfo();
        byte[] bytes = StringUtil.gbkStringToBytes(
            isProtocolJt2019 ? freightVehicleDto.toJt2019String() : freightVehicleDto.toJt2013String());
        upInfo.setBytes(bytes);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
        mainVehicleInfo.setDataLength(bytes.length);
        mainVehicleInfo.setVehicleNo(monitorName);
        mainVehicleInfo.setVehicleColor(Integer.valueOf(plateColor));
        return mainVehicleInfo;
    }

    private MainVehicleInfo sendPassengerVehicleInfo(VehicleDTO vehicleDTO, Integer msgSn, boolean isProtocolJt2019) {
        String orgId = vehicleDTO.getOrgId();
        OrganizationLdap org = organizationService.getOrganizationByUuid(orgId);
        if (org == null) {
            return null;
        }
        String monitorName = vehicleDTO.getName();
        String plateColor = vehicleDTO.getPlateColor() + "";
        String orgName = org.getName();
        String phone = org.getPhone();
        PassengerVehicleDto passengerVehicleDto = new PassengerVehicleDto();
        passengerVehicleDto.setTransType(vehicleDTO.getPurposeCodeNum());
        passengerVehicleDto.setVin(monitorName);
        String provinceId = vehicleDTO.getProvinceId();
        String cityId = vehicleDTO.getCityId();
        if (StringUtils.isNotBlank(provinceId) && StringUtils.isNotBlank(cityId)) {
            passengerVehicleDto.setVehicleNationality(provinceId + cityId);
        }
        passengerVehicleDto.setVehicleType(vehicleDTO.getCodeNum());
        passengerVehicleDto.setRtpn(vehicleDTO.getRoadTransportNumber());
        passengerVehicleDto.setOperatorName(orgName);
        passengerVehicleDto.setOperatorTel(phone);
        passengerVehicleDto.setOwersName(orgName);
        passengerVehicleDto.setOwersOrigId(orgId);
        passengerVehicleDto.setOwersTel(phone);
        passengerVehicleDto.setRtoln(vehicleDTO.getVehiclOperationNumber());
        // ??????????????????
        passengerVehicleDto.setVehicleMode(null);
        passengerVehicleDto.setVehicleColor(plateColor);
        String vehicleId = vehicleDTO.getId();
        passengerVehicleDto.setVehicleOrigId(vehicleId);
        passengerVehicleDto.setDriverInfo(assembleDriverInfo(vehicleId));
        // ??????????????????
        passengerVehicleDto.setBusinessArea(null);
        passengerVehicleDto.setBusinessScopeCode(vehicleDTO.getScopeBusinessCodes());
        // ??????????????????
        passengerVehicleDto.setBanLineType(null);
        passengerVehicleDto.setApprovedSeats(vehicleDTO.getNumberLoad() + "");
        passengerVehicleDto.setOrigin(vehicleDTO.getProvenance());
        passengerVehicleDto.setDestination(vehicleDTO.getDestination());
        passengerVehicleDto.setDepartureSt(vehicleDTO.getDeparture());
        passengerVehicleDto.setDesSt(vehicleDTO.getDestinationStation());

        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setSourceDataType(ConstantUtil.T809_DOWN_BASE_MSG_VEHICLE_ADDED);
        mainVehicleInfo.setSourceMsgSn(msgSn);
        mainVehicleInfo.setDataType(ConstantUtil.T809_UP_BASE_MSG_VEHICLE_ADDED_ACK);
        UpInfo upInfo = new UpInfo();
        byte[] bytes = StringUtil.gbkStringToBytes(
            isProtocolJt2019 ? passengerVehicleDto.toJt2019String() : passengerVehicleDto.toJt2013String());
        upInfo.setBytes(bytes);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
        mainVehicleInfo.setDataLength(bytes.length);
        mainVehicleInfo.setVehicleNo(monitorName);
        mainVehicleInfo.setVehicleColor(Integer.valueOf(plateColor));
        return mainVehicleInfo;

    }

    private MainVehicleInfo sendGeneralVehicleInfo(VehicleDTO vehicleDTO, Integer msgSn, boolean isProtocolJt2019) {
        String orgId = vehicleDTO.getOrgId();
        OrganizationLdap org = organizationService.getOrganizationByUuid(orgId);
        if (org == null) {
            return null;
        }
        String monitorName = vehicleDTO.getName();
        String plateColor = vehicleDTO.getPlateColor() + "";
        GeneralVehicleDto generalVehicleDto = new GeneralVehicleDto();
        generalVehicleDto.setVin(monitorName);
        generalVehicleDto.setVehicleColor(plateColor);
        generalVehicleDto.setVehicleType(vehicleDTO.getCodeNum());
        generalVehicleDto.setTransType(vehicleDTO.getPurposeCodeNum());
        String provinceId = vehicleDTO.getProvinceId();
        String cityId = vehicleDTO.getCityId();
        if (StringUtils.isNotBlank(provinceId) && StringUtils.isNotBlank(cityId)) {
            generalVehicleDto.setVehicleNationality(provinceId + cityId);
        }
        generalVehicleDto.setBusinessScopeCode(vehicleDTO.getScopeBusinessCodes());
        generalVehicleDto.setOwersId(orgId);
        generalVehicleDto.setOwersName(org.getName());
        generalVehicleDto.setOwersTel(org.getPhone());

        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setSourceDataType(ConstantUtil.T809_DOWN_BASE_MSG_VEHICLE_ADDED);
        mainVehicleInfo.setSourceMsgSn(msgSn);
        mainVehicleInfo.setDataType(ConstantUtil.T809_UP_BASE_MSG_VEHICLE_ADDED_ACK);
        UpInfo upInfo = new UpInfo();
        byte[] bytes = StringUtil.gbkStringToBytes(
            isProtocolJt2019 ? generalVehicleDto.toJt2019String() : generalVehicleDto.toJt2013String());
        upInfo.setBytes(bytes);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
        mainVehicleInfo.setDataLength(bytes.length);
        mainVehicleInfo.setVehicleNo(monitorName);
        mainVehicleInfo.setVehicleColor(Integer.valueOf(plateColor));
        return mainVehicleInfo;

    }

    private String assembleDriverInfo(String vehicleId) {
        Map<String, BindDTO> bindDTOMap = VehicleUtil.batchGetBindInfosByRedis(Collections.singleton(vehicleId));
        if (MapUtils.isEmpty(bindDTOMap)) {
            return null;
        }
        BindDTO bindDTO = bindDTOMap.get(vehicleId);
        String professionalIds = bindDTO.getProfessionalIds();
        if (StringUtils.isBlank(professionalIds)) {
            return null;
        }
        List<RedisKey> redisKeys = Arrays.stream(professionalIds.split(",")).map(RedisKeyEnum.PROFESSIONAL_INFO::of)
            .collect(Collectors.toList());
        List<Map<String, String>> data =
            RedisHelper.batchGetHashMap(redisKeys, Lists.newArrayList("cardNumber", "name", "phone"));
        Set<String> set = new HashSet<>();
        String name;
        String cardNumber;
        String phone;
        for (Map<String, String> datum : data) {
            name = datum.get("name") == null ? "" : datum.get("name");
            if (name == null) {
                continue;
            }
            cardNumber = datum.get("cardNumber") == null ? "" : datum.get("cardNumber");
            phone = datum.get("phone") == null ? "" : datum.get("phone");
            set.add(name + "|" + cardNumber + "|" + phone);
        }
        return StringUtils.join(set, ",");
    }

    private void receive9602Answer1602(Message message, T809Message t809Message, MainVehicleInfo msgData) {
        String plateFormId = message.getDesc().getT809PlatId();
        if (plateFormId == null) {
            return;
        }
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(plateFormId);
        if (plantParam == null) {
            return;
        }
        Integer msgSn = t809Message.getMsgHead().getMsgSn();
        String vehicleNo = msgData.getVehicleNo();
        Integer vehicleColor = msgData.getVehicleColor();
        // ?????????????????????????????????????????????
        String orgId = vehicleService.getOrgIdByBrand(vehicleNo);
        //????????????????????????
        OrganizationLdap orgInfo = organizationService.getOrganizationByUuid(orgId);
        if (orgInfo == null) {
            return;
        }
        String transType = CommonTypeUtils.getTransTypeByPurposeType(orgInfo.getOperation());
        // ??????????????????
        String areaNumber = orgInfo.getAreaNumber();
        // ??????????????????
        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        enterpriseInfo.setName(orgInfo.getName());
        enterpriseInfo.setTransType(transType);
        enterpriseInfo.setWord(orgInfo.getBusinessLicenseType());
        enterpriseInfo.setNumber(orgInfo.getLicense());
        enterpriseInfo.setCity(areaNumber);
        enterpriseInfo.setIssueOrg(orgInfo.getIssuingOrgan());
        enterpriseInfo.setStatus(orgInfo.getOperatingState());
        String scopeOfOperationCodes =
            businessScopeService.getBusinessScope(orgId).stream().map(BusinessScopeDTO::getBusinessScopeCode)
                .collect(Collectors.joining(","));
        enterpriseInfo.setScope(scopeOfOperationCodes);
        enterpriseInfo.setPerson(orgInfo.getPrincipal());
        enterpriseInfo.setTel(orgInfo.getPhone());

        // ??????????????????
        Integer protocolType = plantParam.getProtocolType();
        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setVehicleNo(vehicleNo);
        mainVehicleInfo.setVehicleColor(vehicleColor);
        mainVehicleInfo.setSourceDataType(ConstantUtil.T809_DOWN_BASE_MSG_ENTERPRISE_ADDED);
        mainVehicleInfo.setSourceMsgSn(msgSn);
        mainVehicleInfo.setDataType(ConstantUtil.T809_UP_BASE_MSG_ENTERPRISE_ADDED_ACK);
        // ??????-809
        if (Objects.equals(protocolType, 26)) {
            UpInfo upInfo = new UpInfo();
            byte[] bytes = StringUtil.gbkStringToBytes(enterpriseInfo.toString());
            upInfo.setBytes(bytes);
            mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
            mainVehicleInfo.setDataLength(bytes.length);
        } else {
            // ????????????
            EnterpriseAddedleAck enterpriseAddedleAck = new EnterpriseAddedleAck();
            enterpriseAddedleAck.setEnterpriseInfo(enterpriseInfo);
            mainVehicleInfo.setDataLength(enterpriseAddedleAck.toString().getBytes(Charset.forName("GBK")).length);
            mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(enterpriseAddedleAck)));
        }

        message.getDesc().setMsgID(ConstantUtil.T809_UP_BASE_MSG);
        T809Message send809Msg = MsgUtil
            .getT809Message(ConstantUtil.T809_UP_BASE_MSG, plantParam.getIp(), plantParam.getCenterId(),
                mainVehicleInfo);
        message.setData(send809Msg);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    private void receive9607Answer1607(Message message, T809Message t809Message, MainVehicleInfo msgData) {
        String plateFormId = message.getDesc().getT809PlatId();
        if (plateFormId == null) {
            return;
        }
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(plateFormId);
        if (plantParam == null) {
            return;
        }
        // ??????-809(?????????????????????-809)
        if (!Objects.equals(plantParam.getProtocolType(), 1011)) {
            return;
        }
        Integer msgSn = t809Message.getMsgHead().getMsgSn();
        String vehicleNo = msgData.getVehicleNo();
        // ?????????????????????????????????????????????
        String groupId = vehicleService.getOrgIdByBrand(vehicleNo);
        ZwProtocolEnterpriseStaticInfo enterpriseStaticInfo = assembleZwProtocolEnterpriseStaticInfo(groupId);
        if (enterpriseStaticInfo == null) {
            return;
        }

        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setSourceDataType(ConstantUtil.T809_ENTERPRISE_STATIC_INFO_REQ);
        mainVehicleInfo.setSourceMsgSn(msgSn);
        mainVehicleInfo.setDataType(ConstantUtil.T809_ENTERPRISE_STATIC_INFO_ACK);
        UpInfo upInfo = new UpInfo();
        byte[] bytes = StringUtil.gbkStringToBytes(enterpriseStaticInfo.toString());
        upInfo.setBytes(bytes);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
        mainVehicleInfo.setDataLength(bytes.length);
        message.getDesc().setMsgID(ConstantUtil.T809_UP_BASE_MSG);
        T809Message send809Msg = MsgUtil
            .getT809Message(ConstantUtil.T809_UP_BASE_MSG, plantParam.getIp(), plantParam.getCenterId(),
                mainVehicleInfo);
        message.setData(send809Msg);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
    }

    private ZwProtocolEnterpriseStaticInfo assembleZwProtocolEnterpriseStaticInfo(String orgId) {
        //????????????????????????
        OrganizationLdap org = organizationService.getOrganizationByUuid(orgId);
        if (org == null) {
            return null;
        }
        ZwProtocolEnterpriseStaticInfo zwProtocolEnterpriseStaticInfo = new ZwProtocolEnterpriseStaticInfo();
        zwProtocolEnterpriseStaticInfo.setOwersId(orgId);
        zwProtocolEnterpriseStaticInfo.setOwersName(org.getName());
        zwProtocolEnterpriseStaticInfo.setTransType(CommonTypeUtils.getTransTypeByPurposeType(org.getOperation()));
        zwProtocolEnterpriseStaticInfo.setBusinessWord(org.getBusinessLicenseType());
        zwProtocolEnterpriseStaticInfo.setBusinessNumber(org.getLicense());
        String licenseValidityStartDate = org.getLicenseValidityStartDate();
        if (StringUtils.isNotBlank(licenseValidityStartDate)) {
            zwProtocolEnterpriseStaticInfo.setValidityBegin(licenseValidityStartDate.replaceAll("-", ""));
        }
        String licenseValidityEndDate = org.getLicenseValidityEndDate();
        if (StringUtils.isNotBlank(licenseValidityEndDate)) {
            zwProtocolEnterpriseStaticInfo.setValidityEnd(licenseValidityEndDate.replaceAll("-", ""));
        }
        zwProtocolEnterpriseStaticInfo.setOrgCa(org.getOrganizationCode());
        zwProtocolEnterpriseStaticInfo.setUpCa(org.getUpOrganizationCode());
        zwProtocolEnterpriseStaticInfo.setManageCa(org.getManagerOrganizationCode());
        zwProtocolEnterpriseStaticInfo.setZone(org.getAreaNumber());
        zwProtocolEnterpriseStaticInfo.setAddress(org.getAddress());
        zwProtocolEnterpriseStaticInfo.setIssueOrg(org.getIssuingOrgan());
        zwProtocolEnterpriseStaticInfo.setStatus(org.getOperatingState());
        String scopeOfOperationCodes =
            businessScopeService.getBusinessScope(orgId).stream().map(BusinessScopeDTO::getBusinessScopeCode)
                .collect(Collectors.joining(","));
        zwProtocolEnterpriseStaticInfo.setBusinessScope(scopeOfOperationCodes);
        zwProtocolEnterpriseStaticInfo.setCorporation(org.getPrincipal());
        zwProtocolEnterpriseStaticInfo.setCorporationTel(org.getPrincipalPhone());
        zwProtocolEnterpriseStaticInfo.setLinkMan(org.getContactName());
        zwProtocolEnterpriseStaticInfo.setLinkTel(org.getPhone());
        return zwProtocolEnterpriseStaticInfo;
    }

    /**
     * ????????????
     * ???????????????????????????????????? ????????????????????????(0x1608)
     * @param orgIds     ??????uuid??????
     * @param plantParam ??????????????????????????????????????????????????????
     */
    public void send1608ByNewBindingByZwProtocol(Set<String> orgIds, PlantParam plantParam) {
        if (CollectionUtils.isEmpty(orgIds) || plantParam == null) {
            return;
        }
        if (!Objects.equals(plantParam.getProtocolType(), 1011)) {
            return;
        }
        String plantId = plantParam.getId();
        String plantIp = plantParam.getIp();
        Integer centerId = plantParam.getCenterId();
        Set<String> needSyncOrgIdSet = new HashSet<>();
        Map<String, Set<String>> orgIdAndPlantIdSetMap =
            connectionParamsConfigDao.getConnectionInfoByGroupId(orgIds).stream()
                .filter(obj -> Objects.equals(obj.getProtocolType(), 1011)).collect(Collectors
                .groupingBy(PlantParam::getGroupId, Collectors.mapping(PlantParam::getId, Collectors.toSet())));
        for (String orgId : orgIds) {
            Set<String> plantIdSet = orgIdAndPlantIdSetMap.get(orgId);
            if (CollectionUtils.isNotEmpty(plantIdSet) && plantIdSet.contains(plantId)) {
                continue;
            }
            needSyncOrgIdSet.add(orgId);
        }
        if (needSyncOrgIdSet.isEmpty()) {
            return;
        }
        for (String orgId : needSyncOrgIdSet) {
            ZwProtocolEnterpriseStaticInfo enterpriseStaticInfo = assembleZwProtocolEnterpriseStaticInfo(orgId);
            if (enterpriseStaticInfo == null) {
                return;
            }
            MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
            mainVehicleInfo.setDataType(ConstantUtil.T809_ENTERPRISE_STATIC_INFO_SYNC);
            UpInfo upInfo = new UpInfo();
            byte[] bytes = StringUtil.gbkStringToBytes(enterpriseStaticInfo.toString());
            upInfo.setBytes(bytes);
            mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
            mainVehicleInfo.setDataLength(bytes.length);

            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_UP_BASE_MSG, plantIp, centerId, mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_BASE_MSG, t809Message, plantId);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????
     * ????????????????????? ????????????????????????(0x1608)
     * @param orgId ??????uuid
     */
    public void send1608ByUpdateGroupByZwProtocol(String orgId) {
        if (StringUtils.isBlank(orgId)) {
            return;
        }
        Set<String> vehicleIds = vehicleService.getVehicleIdsByOrgId(orgId);
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return;
        }
        Set<PlantParam> plantParamSet = connectionParamsConfigDao.getConnectionInfoByVehicleIds(vehicleIds).stream()
            .filter(obj -> Objects.equals(obj.getProtocolType(), 1011)).collect(Collectors.toSet());

        // ??????-809(?????????????????????-809)
        if (plantParamSet.isEmpty()) {
            return;
        }
        ZwProtocolEnterpriseStaticInfo enterpriseStaticInfo = assembleZwProtocolEnterpriseStaticInfo(orgId);
        if (enterpriseStaticInfo == null) {
            return;
        }
        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setDataType(ConstantUtil.T809_ENTERPRISE_STATIC_INFO_SYNC);
        UpInfo upInfo = new UpInfo();
        byte[] bytes = StringUtil.gbkStringToBytes(enterpriseStaticInfo.toString());
        upInfo.setBytes(bytes);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
        mainVehicleInfo.setDataLength(bytes.length);

        for (PlantParam plantParam : plantParamSet) {
            String ip = plantParam.getIp();
            Integer centerId = plantParam.getCenterId();
            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_UP_BASE_MSG, ip, centerId, mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_BASE_MSG, t809Message, plantParam.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????
     * ????????????????????????(0x1609)
     * @param plantParam ???null?????????????????????????????? ??????null????????????????????????????????????????????????
     */
    public void send1609ByZwProtocol(JSONObject vehicleJsonObj, PlantParam plantParam) {
        Set<PlantParam> plantParamSet;
        // ??????????????????
        if (plantParam == null) {
            String vehicleId = vehicleJsonObj.getString("id");
            plantParamSet =
                connectionParamsConfigDao.getConnectionInfoByVehicleIds(Collections.singleton(vehicleId)).stream()
                    .filter(obj -> Objects.equals(obj.getProtocolType(), 1011)).collect(Collectors.toSet());
            // ??????????????????
        } else {
            if (!Objects.equals(plantParam.getProtocolType(), 1011)) {
                return;
            }
            plantParamSet = Sets.newHashSet(plantParam);
        }
        // ??????-809(?????????????????????-809)
        if (plantParamSet.isEmpty()) {
            return;
        }
        String orgId = vehicleJsonObj.getString("orgId");
        OrganizationLdap org = organizationService.getOrganizationByUuid(orgId);
        if (org == null) {
            return;
        }
        String vehicleNo = vehicleJsonObj.getString("brand");
        String plateColor = vehicleJsonObj.getString("plateColor");
        GeneralVehicleDto generalVehicleDto = new GeneralVehicleDto();
        generalVehicleDto.setVin(vehicleNo);
        generalVehicleDto.setVehicleColor(plateColor);
        generalVehicleDto.setVehicleType(vehicleJsonObj.getString("codeNum"));
        generalVehicleDto.setTransType(vehicleJsonObj.getString("purposeCodeNum"));
        String provinceId = vehicleJsonObj.getString("provinceId");
        String cityId = vehicleJsonObj.getString("cityId");
        if (StringUtils.isNotBlank(provinceId) && StringUtils.isNotBlank(cityId)) {
            generalVehicleDto.setVehicleNationality(provinceId + cityId);
        }
        generalVehicleDto.setBusinessScopeCode(vehicleJsonObj.getString("scopeBusinessCodes"));
        generalVehicleDto.setOwersId(orgId);
        generalVehicleDto.setOwersName(org.getName());
        generalVehicleDto.setOwersTel(org.getPhone());

        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setDataType(ConstantUtil.T809_VEHICLE_STATIC_INFO_SYNC);
        UpInfo upInfo = new UpInfo();
        byte[] bytes = StringUtil.gbkStringToBytes(generalVehicleDto.toJt2019String());
        upInfo.setBytes(bytes);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
        mainVehicleInfo.setDataLength(bytes.length);
        mainVehicleInfo.setVehicleNo(vehicleNo);
        mainVehicleInfo.setVehicleColor(Integer.valueOf(plateColor));

        for (PlantParam param : plantParamSet) {
            T809Message t809Message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_BASE_MSG, param.getIp(), param.getCenterId(), mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_BASE_MSG, t809Message, param.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ???????????????
     * ????????????????????????????????? ??????????????????????????????(0x1210)
     */
    public void send1210ByNewBindingByHljProtocol(Map<String, BindDTO> deviceAndConfigInfoMap, PlantParam plantParam) {
        if (deviceAndConfigInfoMap.isEmpty() || plantParam == null) {
            return;
        }
        String plantParamId = plantParam.getId();
        String plantParamIp = plantParam.getIp();
        Integer centerId = plantParam.getCenterId();
        Set<String> deviceIdSet = deviceAndConfigInfoMap.keySet();
        List<DeviceDTO> deviceList = deviceService.getDeviceListByIds(deviceIdSet);
        for (DeviceDTO deviceDTO : deviceList) {
            String deviceId = deviceDTO.getId();
            HeiDeviceInstallInfoDto deviceInstallInfoDto = getHeiDeviceInstallInfoDto(deviceDTO);
            BindDTO configInfo = deviceAndConfigInfoMap.get(deviceId);
            MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
            mainVehicleInfo.setVehicleNo(configInfo.getName());
            mainVehicleInfo.setVehicleColor(configInfo.getPlateColor());
            mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(deviceInstallInfoDto)));
            mainVehicleInfo.setDataType(ConstantUtil.T809_HLJ_DEVICE_INSTALL_INFO_SYNC);

            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_UP_EXG_MSG, plantParamIp, centerId, mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_EXG_MSG, t809Message, plantParamId);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????
     */
    private HeiDeviceInstallInfoDto getHeiDeviceInstallInfoDto(DeviceDTO deviceDTO) {

        HeiDeviceInstallInfoDto deviceInstallInfoDto = new HeiDeviceInstallInfoDto();
        deviceInstallInfoDto.setProducer(deviceDTO.getTerminalManufacturer());
        deviceInstallInfoDto.setTerminalModel(deviceDTO.getTerminalType());
        deviceInstallInfoDto.setTerminalId(deviceDTO.getDeviceNumber());
        Date time = deviceDTO.getInstallTime();
        if (time != null) {
            deviceInstallInfoDto.setInstallTime(time.getTime());
        }
        time = deviceDTO.getProcurementTime();
        if (time != null) {
            deviceInstallInfoDto.setManufactureTime(time.getTime());
        }
        deviceInstallInfoDto.setTelephone(deviceDTO.getTelephone());
        deviceInstallInfoDto.setContacts(deviceDTO.getContacts());
        deviceInstallInfoDto.setTerminalSerialNumber(deviceDTO.getBarCode());
        deviceInstallInfoDto.setTransportationEnterprises(deviceDTO.getOrgName());
        return deviceInstallInfoDto;
    }

    /**
     * ???????????????
     * ????????????????????? ??????????????????????????????(0x1210)
     */
    public void send1210ByUpdateDeviceByHljProtocol(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            return;
        }
        VehicleDTO vehicleDTO = vehicleService.getVehicleInfoByDeviceId(deviceId);
        if (vehicleDTO == null) {
            return;
        }
        String vehicleId = vehicleDTO.getId();
        Set<PlantParam> plantParamSet =
            new HashSet<>(connectionParamsConfigDao.getConnectionInfoByVehicleIds(Collections.singleton(vehicleId)));
        // ??????-809(?????????????????????-809)
        if (plantParamSet.isEmpty()) {
            return;
        }
        List<DeviceDTO> list = deviceService.getDeviceListByIds(Collections.singleton(deviceId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        String brand = vehicleDTO.getName();
        Integer plateColor = vehicleDTO.getPlateColor();
        DeviceDTO deviceDTO = list.get(0);
        HeiDeviceInstallInfoDto deviceInstallInfoDto = getHeiDeviceInstallInfoDto(deviceDTO);
        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setVehicleNo(brand);
        mainVehicleInfo.setVehicleColor(plateColor);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(deviceInstallInfoDto)));
        mainVehicleInfo.setDataType(ConstantUtil.T809_HLJ_DEVICE_INSTALL_INFO_SYNC);

        for (PlantParam plantParam : plantParamSet) {
            T809Message t809Message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_EXG_MSG, plantParam.getIp(), plantParam.getCenterId(),
                    mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_EXG_MSG, t809Message, plantParam.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????
     * ????????????????????????????????? ??????????????????????????????(0x1240)
     */
    public void send1240ByNewBindingByZwProtocol(Map<String, BindDTO> deviceAndConfigInfoMap, PlantParam plantParam) {
        if (deviceAndConfigInfoMap.isEmpty() || plantParam == null) {
            return;
        }
        String plantParamId = plantParam.getId();
        String plantParamIp = plantParam.getIp();
        Integer centerId = plantParam.getCenterId();
        Set<String> deviceIdSet = deviceAndConfigInfoMap.keySet();
        List<DeviceDTO> list = deviceService.getDeviceListByIds(deviceIdSet);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (DeviceDTO deviceDTO : list) {
            String deviceId = deviceDTO.getId();
            DeviceInstallInfoDto deviceInstallInfoDto = getDeviceInstallInfoDto(deviceDTO);
            BindDTO configInfo = deviceAndConfigInfoMap.get(deviceId);
            MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
            mainVehicleInfo.setVehicleNo(configInfo.getName());
            mainVehicleInfo.setVehicleColor(configInfo.getPlateColor());
            mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(deviceInstallInfoDto)));
            mainVehicleInfo.setDataType(ConstantUtil.T809_DEVICE_INSTALL_INFO_SYNC);

            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_UP_EXG_MSG, plantParamIp, centerId, mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_EXG_MSG, t809Message, plantParamId);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????????????? ??????????????????????????????(0x1240)
     */
    public void send1240ByUpdateDeviceByZwProtocol(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            return;
        }
        VehicleDTO vehicleDTO = vehicleService.getVehicleInfoByDeviceId(deviceId);
        if (vehicleDTO == null) {
            return;
        }
        String vehicleId = vehicleDTO.getId();
        Set<PlantParam> plantParamSet =
            new HashSet<>(connectionParamsConfigDao.getConnectionInfoByVehicleIds(Collections.singleton(vehicleId)));
        // ??????-809(?????????????????????-809)
        if (plantParamSet.isEmpty()) {
            return;
        }
        List<DeviceDTO> list = deviceService.getDeviceListByIds(Collections.singleton(deviceId));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        DeviceDTO deviceDTO = list.get(0);
        String brand = vehicleDTO.getName();
        Integer plateColor = vehicleDTO.getPlateColor();
        DeviceInstallInfoDto deviceInstallInfoDto = getDeviceInstallInfoDto(deviceDTO);
        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setVehicleNo(brand);
        mainVehicleInfo.setVehicleColor(plateColor);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(deviceInstallInfoDto)));
        mainVehicleInfo.setDataType(ConstantUtil.T809_DEVICE_INSTALL_INFO_SYNC);

        for (PlantParam plantParam : plantParamSet) {
            T809Message t809Message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_EXG_MSG, plantParam.getIp(), plantParam.getCenterId(),
                    mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_EXG_MSG, t809Message, plantParam.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????
     */
    private DeviceInstallInfoDto getDeviceInstallInfoDto(DeviceDTO deviceDTO) {
        DeviceInstallInfoDto deviceInstallInfoDto = new DeviceInstallInfoDto();
        deviceInstallInfoDto.setProducer(deviceDTO.getTerminalManufacturer());
        deviceInstallInfoDto.setTerminalModel(deviceDTO.getTerminalType());
        deviceInstallInfoDto.setTerminalId(deviceDTO.getDeviceNumber());
        Date time = deviceDTO.getInstallTime();
        if (time != null) {
            deviceInstallInfoDto.setInstallTime(time.getTime());
        }
        deviceInstallInfoDto.setInstallCompany(deviceDTO.getInstallCompany());
        deviceInstallInfoDto.setTelephone(deviceDTO.getTelephone());
        deviceInstallInfoDto.setContacts(deviceDTO.getContacts());
        deviceInstallInfoDto.setComplianceRequirements(deviceDTO.getComplianceRequirements());
        return deviceInstallInfoDto;
    }

    /**
     * ????????????
     * ???????????????????????????????????? ?????????????????????????????????????????? 0x1605
     */
    public void send1605ByNewBindingBySiChuanProtocol(Set<String> orgIds, PlantParam plantParam) {
        if (CollectionUtils.isEmpty(orgIds) || plantParam == null) {
            return;
        }
        if (!Objects.equals(plantParam.getProtocolType(), 2301)) {
            return;
        }
        Set<String> needSyncOrgIdSet = new HashSet<>();
        String plantId = plantParam.getId();
        String plantIp = plantParam.getIp();
        Integer centerId = plantParam.getCenterId();
        Map<String, Set<String>> groupIdAndPlantIdSetMap =
            connectionParamsConfigDao.getConnectionInfoByGroupId(orgIds).stream()
                .filter(obj -> Objects.equals(obj.getProtocolType(), 2301)).collect(Collectors
                .groupingBy(PlantParam::getGroupId, Collectors.mapping(PlantParam::getId, Collectors.toSet())));
        for (String orgId : orgIds) {
            Set<String> plantIdSet = groupIdAndPlantIdSetMap.get(orgId);
            if (CollectionUtils.isNotEmpty(plantIdSet) && plantIdSet.contains(plantId)) {
                continue;
            }
            needSyncOrgIdSet.add(orgId);
        }
        if (needSyncOrgIdSet.isEmpty()) {
            return;
        }
        Map<String, OrganizationLdap> orgIdMap = organizationService.getOrgByUuids(needSyncOrgIdSet);
        for (Map.Entry<String, OrganizationLdap> entry : orgIdMap.entrySet()) {
            OrganizationLdap org = entry.getValue();
            SiChuanProtocolEnterpriseStaticInfo enterpriseStaticInfo = assembleSiChuanProtocolEnterpriseStaticInfo(org);

            MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
            mainVehicleInfo.setDataType(ConstantUtil.T809_SI_CHUAN_ENTERPRISE_STATIC_INFO_SYNC);
            UpInfo upInfo = new UpInfo();
            byte[] bytes = StringUtil.gbkStringToBytes(enterpriseStaticInfo.toString());
            upInfo.setBytes(bytes);
            mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
            mainVehicleInfo.setDataLength(bytes.length);

            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_UP_BASE_MSG, plantIp, centerId, mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_BASE_MSG, t809Message, plantId);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????
     * ????????????????????? ?????????????????????????????????????????? 0x1605
     */
    public void send1605ByUpdateGroupBySiChuanProtocol(String orgId) {
        if (StringUtils.isBlank(orgId)) {
            return;
        }
        Set<String> vehicleIds = vehicleService.getVehicleIdsByOrgId(orgId);
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return;
        }
        Set<PlantParam> plantParamSet = connectionParamsConfigDao.getConnectionInfoByVehicleIds(vehicleIds).stream()
            .filter(obj -> Objects.equals(obj.getProtocolType(), 2301)).collect(Collectors.toSet());

        if (plantParamSet.isEmpty()) {
            return;
        }
        OrganizationLdap org = organizationService.getOrganizationByUuid(orgId);
        if (org == null) {
            return;
        }
        SiChuanProtocolEnterpriseStaticInfo enterpriseStaticInfo = assembleSiChuanProtocolEnterpriseStaticInfo(org);

        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setDataType(ConstantUtil.T809_SI_CHUAN_ENTERPRISE_STATIC_INFO_SYNC);
        UpInfo upInfo = new UpInfo();
        byte[] bytes = StringUtil.gbkStringToBytes(enterpriseStaticInfo.toString());
        upInfo.setBytes(bytes);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
        mainVehicleInfo.setDataLength(bytes.length);

        for (PlantParam plantParam : plantParamSet) {
            T809Message t809Message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_BASE_MSG, plantParam.getIp(), plantParam.getCenterId(),
                    mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_BASE_MSG, t809Message, plantParam.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    private SiChuanProtocolEnterpriseStaticInfo assembleSiChuanProtocolEnterpriseStaticInfo(OrganizationLdap org) {
        SiChuanProtocolEnterpriseStaticInfo enterpriseStaticInfo = new SiChuanProtocolEnterpriseStaticInfo();
        enterpriseStaticInfo.setOrgCa(org.getOrganizationCode());
        enterpriseStaticInfo.setName(org.getName());
        enterpriseStaticInfo.setTransType(CommonTypeUtils.getTransTypeByPurposeType(org.getOperation()));
        enterpriseStaticInfo.setUpCa(org.getUpOrganizationCode());
        enterpriseStaticInfo.setManageCa(org.getManagerOrganizationCode());
        enterpriseStaticInfo.setZone(org.getAreaNumber());
        String scopeOfOperationCodes =
            businessScopeService.getBusinessScope(org.getUuid()).stream().map(BusinessScopeDTO::getBusinessScopeCode)
                .collect(Collectors.joining(","));
        enterpriseStaticInfo.setBusinessScope(scopeOfOperationCodes);
        enterpriseStaticInfo.setBusinessNumber(org.getLicense());
        String licenseValidityStartDate = org.getLicenseValidityStartDate();
        if (StringUtils.isNotBlank(licenseValidityStartDate)) {
            enterpriseStaticInfo.setValidityBegin(licenseValidityStartDate.replaceAll("-", ""));
        }
        String licenseValidityEndDate = org.getLicenseValidityEndDate();
        if (StringUtils.isNotBlank(licenseValidityEndDate)) {
            enterpriseStaticInfo.setValidityEnd(licenseValidityEndDate.replaceAll("-", ""));
        }
        enterpriseStaticInfo.setAddress(org.getAddress());
        enterpriseStaticInfo.setCorporation(org.getPrincipal());
        enterpriseStaticInfo.setLinkman(org.getContactName());
        enterpriseStaticInfo.setTelphone(org.getPhone());
        return enterpriseStaticInfo;
    }

    /**
     * ????????????
     * ??????????????????????????????????????????
     * @param plantParam ???null?????????????????????????????? ??????null????????????????????????????????????????????????
     */
    public void send1606BySiChuanProtocol(JSONObject vehicleJsonObj, TerminalTypeRedisInfo terminalTypeRedisInfo,
        PlantParam plantParam) {
        Set<PlantParam> plantParamSet;
        // ??????????????????
        if (plantParam == null) {
            String vehicleId = vehicleJsonObj.getString("id");
            plantParamSet =
                connectionParamsConfigDao.getConnectionInfoByVehicleIds(Collections.singleton(vehicleId)).stream()
                    .filter(obj -> Objects.equals(obj.getProtocolType(), 2301)).collect(Collectors.toSet());
            // ??????????????????
        } else {
            if (!Objects.equals(plantParam.getProtocolType(), 2301)) {
                return;
            }
            plantParamSet = Sets.newHashSet(plantParam);
        }
        if (plantParamSet.isEmpty()) {
            return;
        }
        OrganizationLdap org = organizationService.getOrganizationByUuid(vehicleJsonObj.getString("orgId"));
        if (org == null) {
            return;
        }
        String brand = vehicleJsonObj.getString("brand");
        Integer plateColor = vehicleJsonObj.getInteger("plateColor");
        SiChuanProtocolVehicleStaticInfo vehicleStaticInfo = new SiChuanProtocolVehicleStaticInfo();
        vehicleStaticInfo.setVin(vehicleJsonObj.getString("chassisNumber"));
        vehicleStaticInfo.setVehiclePlate(brand);
        vehicleStaticInfo.setPlateColor(plateColor);
        vehicleStaticInfo.setOrgCa(org.getOrganizationCode());
        vehicleStaticInfo.setManageCa(org.getManagerOrganizationCode());
        String provinceId = vehicleJsonObj.getString("provinceId");
        String cityId = vehicleJsonObj.getString("cityId");
        if (StringUtils.isNotBlank(provinceId) && StringUtils.isNotBlank(cityId)) {
            vehicleStaticInfo.setZone(provinceId + cityId);
        }
        vehicleStaticInfo.setVehicleType(vehicleJsonObj.getString("codeNum"));
        vehicleStaticInfo.setTransNo(vehicleJsonObj.getString("roadTransportNumber"));
        vehicleStaticInfo.setBusinessScope(vehicleJsonObj.getString("scopeBusinessCodes"));
        String licenseValidityStartDate = vehicleJsonObj.getString("roadTransportValidityStartStr");
        if (StringUtils.isNotBlank(licenseValidityStartDate)) {
            vehicleStaticInfo.setValidityBegin(licenseValidityStartDate.replaceAll("-", ""));
        }
        String licenseValidityEndDate = vehicleJsonObj.getString("roadTransportValidityStr");
        if (StringUtils.isNotBlank(licenseValidityEndDate)) {
            vehicleStaticInfo.setValidityEnd(licenseValidityEndDate.replaceAll("-", ""));
        }

        vehicleStaticInfo.setSeatTon(vehicleJsonObj.getInteger("seatTon"));
        vehicleStaticInfo.setMotorNo(vehicleJsonObj.getString("engineNumber"));
        vehicleStaticInfo.setOwner(vehicleJsonObj.getString("vehicleOwner"));
        vehicleStaticInfo.setOwnerTel(vehicleJsonObj.getString("vehicleOwnerPhone"));
        String vehiclePlatformInstallDateStr = vehicleJsonObj.getString("vehiclePlatformInstallDateStr");
        if (StringUtils.isNotBlank(vehiclePlatformInstallDateStr)) {
            vehicleStaticInfo.setInstallDate(vehiclePlatformInstallDateStr.replaceAll("-", ""));
        }
        if (terminalTypeRedisInfo != null) {
            vehicleStaticInfo.setOptional(Long.valueOf(terminalTypeRedisInfo.getOptional()));
            Integer photoParam = terminalTypeRedisInfo.getPhotoParam();
            vehicleStaticInfo.setPhotoParam(photoParam == null ? null : String.valueOf(photoParam));
            Integer videoParam = terminalTypeRedisInfo.getVideoParam();
            vehicleStaticInfo.setVedioParam(videoParam == null ? null : String.valueOf(videoParam));
        }

        MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
        mainVehicleInfo.setDataType(ConstantUtil.T809_SI_CHUAN_VEHICLE_STATIC_INFO_SYNC);
        mainVehicleInfo.setVehicleColor(plateColor);
        mainVehicleInfo.setVehicleNo(brand);
        UpInfo upInfo = new UpInfo();
        byte[] bytes = StringUtil.gbkStringToBytes(vehicleStaticInfo.toString());
        upInfo.setBytes(bytes);
        mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
        mainVehicleInfo.setDataLength(bytes.length);

        for (PlantParam param : plantParamSet) {
            T809Message t809Message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_BASE_MSG, param.getIp(), param.getCenterId(), mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_BASE_MSG, t809Message, param.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????
     * ???????????????????????????????????? ???????????????????????????????????????????????? 0x1607
     */
    public void send1607ByNewBindingBySiChuanProtocol(JSONObject vehicleInfo,
        List<Map<String, String>> professionalJsonObjList, PlantParam plantParam) {
        if (CollectionUtils.isEmpty(professionalJsonObjList)) {
            return;
        }
        if (!Objects.equals(plantParam.getProtocolType(), 2301)) {
            return;
        }
        String plantId = plantParam.getId();
        String plantIp = plantParam.getIp();
        Integer centerId = plantParam.getCenterId();
        String brand = vehicleInfo.getString("brand");
        Integer plateColor = vehicleInfo.getInteger("platColor");
        if (CollectionUtils.isEmpty(professionalJsonObjList)) {
            return;
        }
        SiChuanProtocolProfessionalStaticInfo professionalStaticInfo;
        for (Map<String, String> professionalJsonObj : professionalJsonObjList) {

            professionalStaticInfo = new SiChuanProtocolProfessionalStaticInfo();
            professionalStaticInfo.setIdNumber(professionalJsonObj.get("identity"));
            professionalStaticInfo.setName(professionalJsonObj.get("name"));
            String gender = professionalJsonObj.get("gender");
            if (StringUtils.isNotBlank(gender)) {
                professionalStaticInfo.setSex(Integer.parseInt(gender));
            }
            professionalStaticInfo.setOrgCa(professionalJsonObj.get("serviceCompany"));
            professionalStaticInfo.setVehicleVin(brand);
            professionalStaticInfo.setQualificationNumber(professionalJsonObj.get("cardNumber"));
            professionalStaticInfo.setQualificationType(professionalJsonObj.get("qualificationCategory"));
            String issueCertificateDate = professionalJsonObj.get("issueCertificateDate");
            if (StringUtils.isNotBlank(issueCertificateDate)) {
                professionalStaticInfo.setValidityBegin(issueCertificateDate.replaceAll("-", ""));
            }
            String icCardEndDate = professionalJsonObj.get("icCardEndDate");
            if (StringUtils.isNotBlank(icCardEndDate)) {
                professionalStaticInfo.setValidityEnd(icCardEndDate.replaceAll("-", ""));
            }
            professionalStaticInfo.setTel(professionalJsonObj.get("phone"));
            professionalStaticInfo.setAddress(professionalJsonObj.get("address"));

            MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
            mainVehicleInfo.setDataType(ConstantUtil.T809_SI_CHUAN_PROFESSIONAL_STATIC_INFO_SYNC);
            mainVehicleInfo.setVehicleNo(brand);
            mainVehicleInfo.setVehicleColor(plateColor);
            UpInfo upInfo = new UpInfo();
            byte[] bytes = StringUtil.gbkStringToBytes(professionalStaticInfo.toString());
            upInfo.setBytes(bytes);
            mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
            mainVehicleInfo.setDataLength(bytes.length);

            T809Message t809Message =
                MsgUtil.getT809Message(ConstantUtil.T809_UP_BASE_MSG, plantIp, centerId, mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_BASE_MSG, t809Message, plantId);
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ????????????
     * ??????????????????????????? ???????????????????????????????????????????????? 0x1607
     */
    public void send1607ByUpdateProfessionalBySiChuanProtocol(List<String> vehicleIds,
        ProfessionalDTO newProfessionalDTO) {
        if (CollectionUtils.isEmpty(vehicleIds)) {
            return;
        }
        Map<String, PlantParam> vehicleConnectionInfoMap =
            connectionParamsConfigDao.getVehicleConnectionInfoByVehicleIds(vehicleIds).stream()
                .filter(obj -> Objects.equals(obj.getProtocolType(), 2301))
                .collect(Collectors.toMap(PlantParam::getVehicleId, Function.identity()));
        if (vehicleConnectionInfoMap.isEmpty()) {
            return;
        }
        List<RedisKey> monitorKeyList = Lists.newArrayList();
        for (String monitorId : vehicleConnectionInfoMap.keySet()) {
            monitorKeyList.add(RedisKeyEnum.MONITOR_INFO.of(monitorId));
        }
        List<Map<String, String>> list =
            RedisHelper.batchGetHashMap(monitorKeyList, Lists.newArrayList("id", "name", "plateColor"));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Map<String, String> map : list) {

            String vehicleId = map.get("id");
            String brand = map.get("name");
            Integer plateColor = Integer.parseInt(map.get("plateColor"));

            SiChuanProtocolProfessionalStaticInfo professionalStaticInfo = new SiChuanProtocolProfessionalStaticInfo();
            professionalStaticInfo.setIdNumber(newProfessionalDTO.getIdentity());
            professionalStaticInfo.setName(newProfessionalDTO.getName());
            professionalStaticInfo.setSex(Integer.valueOf(newProfessionalDTO.getGender()));
            professionalStaticInfo.setOrgCa(newProfessionalDTO.getServiceCompany());
            professionalStaticInfo.setVehicleVin(brand);
            professionalStaticInfo.setQualificationNumber(newProfessionalDTO.getCardNumber());
            professionalStaticInfo.setQualificationType(newProfessionalDTO.getQualificationCategory());
            Date issueCertificateDate = newProfessionalDTO.getIssueCertificateDate();
            if (issueCertificateDate != null) {
                professionalStaticInfo
                    .setValidityBegin(DateUtil.formatDate(issueCertificateDate, DateUtil.DATE_YMD_FORMAT));
            }
            Date icCardEndDate = newProfessionalDTO.getIcCardEndDate();
            if (icCardEndDate != null) {
                professionalStaticInfo.setValidityEnd(DateUtil.formatDate(icCardEndDate, DateUtil.DATE_YMD_FORMAT));
            }
            professionalStaticInfo.setTel(newProfessionalDTO.getPhone());
            professionalStaticInfo.setAddress(newProfessionalDTO.getAddress());

            MainVehicleInfo mainVehicleInfo = new MainVehicleInfo();
            mainVehicleInfo.setDataType(ConstantUtil.T809_SI_CHUAN_PROFESSIONAL_STATIC_INFO_SYNC);
            mainVehicleInfo.setVehicleNo(brand);
            mainVehicleInfo.setVehicleColor(plateColor);
            UpInfo upInfo = new UpInfo();
            byte[] bytes = StringUtil.gbkStringToBytes(professionalStaticInfo.toString());
            upInfo.setBytes(bytes);
            mainVehicleInfo.setData(JSON.parseObject(JSON.toJSONString(upInfo)));
            mainVehicleInfo.setDataLength(bytes.length);

            PlantParam plantParam = vehicleConnectionInfoMap.get(vehicleId);
            T809Message t809Message = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_BASE_MSG, plantParam.getIp(), plantParam.getCenterId(),
                    mainVehicleInfo);
            Message message = MsgUtil.getMsg(ConstantUtil.T809_UP_BASE_MSG, t809Message, plantParam.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(message);
        }
    }

    /**
     * ??????809????????????,???????????????????????????
     */
    private AlarmHandle alarmIsAlarmHandle(T809AlarmForwardInfo result) {
        if (result == null) {
            return null;
        }
        Long alarmStartTime = result.getAlarmStartTime(); // ??????????????????
        Integer alarmType = result.getAlarmType(); // ????????????
        String monitorId = result.getMonitorId();
        return getAlarmInfoByMonitorId(alarmStartTime, alarmType, monitorId);
    }

    private AlarmHandle getAlarmInfoByMonitorId(Long alarmStartTime, Integer alarmType, String monitorId) {
        Map<String, String> params = new HashMap<>(8);
        params.put("monitorId", monitorId);
        params.put("alarmType", String.valueOf(alarmType));
        params.put("startTime", String.valueOf(alarmStartTime));
        String str = HttpClientUtil.send(PaasCloudHBaseAccessEnum.GET_ALARM_INFO_BY_MONITOR_ID, params);
        return PaasCloudUrlUtil.getResultData(str, AlarmHandle.class);
    }

    /**
     * ??????????????????????????????????????????
     */
    private T809AlarmForwardInfoQuery getAlarmQueryParam(String plateFormId, JSONObject msgData,
        Integer connectProtocolType) {
        Integer msgId; // ????????????????????????????????????
        Long bodyAlarmTime; // ????????????
        Integer msgSn; // ?????????
        // 35658-809(t809-2019????????????)
        if (CONNECT_PROTOCOL_TYPE_808_2019.contains(connectProtocolType)) {
            // ????????????????????????????????????
            msgId = msgData.getInteger("dataType");
            bodyAlarmTime = msgData.getLong("warnTime");
            msgSn = msgData.getInteger("msgSn");
        } else { // 809-2011????????????
            msgId = 5122;
            bodyAlarmTime = msgData.getLong("warnTime");
            msgSn = msgData.getInteger("supervisionId");
        }
        // ????????????????????????????????????????????????????????????????????????????????????id?????????????????????????????????
        T809AlarmForwardInfoQuery query = new T809AlarmForwardInfoQuery();
        query.setMsgId(msgId);
        query.setMsgSn(msgSn);
        query.setPlateFormIdStr(plateFormId);
        long queryStartTime = bodyAlarmTime * 1000;
        Long queryEndTime = queryStartTime + 999;
        query.setQueryStartTime(queryStartTime);
        query.setQueryEndTime(queryEndTime);
        return query;
    }

    /**
     * ???????????????,????????????????????????
     */
    private void alarmHandleACK(T809AlarmForwardInfo monitorInfo, T809MsgHead head, Integer alarmHandleResult,
        String plateFormId) {
        if (monitorInfo == null || head == null) {
            return;
        }
        PlantParam plantParam = connectionParamsSetDao.getConnectionInfoById(plateFormId);
        if (plantParam == null) {
            return;
        }
        String monitorName = monitorInfo.getMonitorName();
        Integer color = monitorInfo.getPlateColor();
        if (color == null) {
            color = 2;
        }
        PlatformAlarmAck ack = new PlatformAlarmAck();
        ack.setSupervisionId(monitorInfo.getMsgSn()); // ??????id
        ack.setResult(alarmHandleResult); // ????????????
        ack.setSourceMsgSn(monitorInfo.getMsgSn()); // ?????????(809-2011???????????????id)
        ack.setSourceDataType(monitorInfo.getMsgId());
        ack.setMsgSn(head.getMsgSn());
        SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo();
        supervisionAlarmInfo.setVehicleColor(color);
        supervisionAlarmInfo.setData(MsgUtil.objToJson(ack));
        supervisionAlarmInfo.setDataType(ConstantUtil.T809_UP_WARN_MSG_URGE_TODO_ACK);
        supervisionAlarmInfo.setDataLength(JSONObject.toJSONString(ack).length());
        supervisionAlarmInfo.setVehicleNo(monitorName);
        Integer msgGNSSCenterId = plantParam.getCenterId(); // ?????????
        String serverIp = plantParam.getIp(); // ?????????ip
        T809Message message =
            MsgUtil.getT809Message(ConstantUtil.T809_UP_WARN_MSG, serverIp, msgGNSSCenterId, supervisionAlarmInfo);
        Message t809Message = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, message).assembleDesc809(plateFormId);
        WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(t809Message);
        // 809-2019?????????????????????0x1411
        send0x1411Msg(plantParam, monitorInfo.getMsgId(), monitorInfo.getMsgSn(), alarmHandleResult);
    }

    /**
     * ??????0x1411??????
     */
    private void send0x1411Msg(PlantParam plantParam, Integer sourceDataType, Integer sourceMsgSn, Integer result) {
        if (plantParam == null) {
            return;
        }
        Integer connectProtocolType = plantParam.getProtocolType();
        // 809-2019????????????
        if (CONNECT_PROTOCOL_TYPE_808_2019.contains(connectProtocolType)) {
            PlatformAlarmAck ack = new PlatformAlarmAck();
            // ??????????????????????????????0x1411
            ack.setSourceDataType(sourceDataType);
            ack.setSourceMsgSn(sourceMsgSn);
            ack.setResult(result);
            SupervisionAlarmInfo supervisionAlarmInfo = new SupervisionAlarmInfo();
            supervisionAlarmInfo.setData(MsgUtil.objToJson(ack));
            supervisionAlarmInfo.setDataType(ConstantUtil.T809_UP_WARN_MSG_URGE_TODO_ACK_INFO);
            supervisionAlarmInfo.setDataLength(JSONObject.toJSONString(ack).length());
            T809Message t809Msg = MsgUtil
                .getT809Message(ConstantUtil.T809_UP_WARN_MSG, plantParam.getIp(), plantParam.getCenterId(),
                    supervisionAlarmInfo);
            Message msg = MsgUtil.getMsg(ConstantUtil.T809_UP_WARN_MSG, t809Msg).assembleDesc809(plantParam.getId());
            WebSubscribeManager.getInstance().getChannel(serverParamList.getServerId809()).writeAndFlush(msg);
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????id?????????
     */
    public void updateRegionalInfo(Message message) {
        T808Message t808Messages = JSON.parseObject(message.getData().toString(), T808Message.class);
        RegistrationInfo info = JSON.parseObject(t808Messages.getMsgBody().toString(), RegistrationInfo.class);
        String vehicleId = message.getDesc().getMonitorId();
        String deviceId = message.getDesc().getDeviceId();
        List<DeviceDTO> list = deviceService.getDeviceListByIds(Collections.singleton(deviceId));
        if (list.isEmpty()) {
            return;
        }
        VehicleDTO vehicleDTO = vehicleService.getPartFieldById(vehicleId);
        if (vehicleDTO == null) {
            return;
        }
        //??????????????????????????????????????????????????????????????????????????????
        //?????????????????????
        comparisonEquipment(info, list.get(0));
        //??????????????????
        compareZoneCode(info, vehicleDTO);
    }

    private void compareZoneCode(RegistrationInfo info, VehicleDTO vehicleDTO) {

        Integer provinceId = info.getProvinceId();
        Integer cityId = info.getCityId();
        String formProvinceId = vehicleDTO.getProvinceId();
        String formCityId = vehicleDTO.getCityId();
        boolean provinceFlag = false;
        boolean cityFlag = false;
        if (provinceId != null && !provinceId.toString().equals(formProvinceId)) {
            vehicleDTO.setProvinceId(CommonUtil.getProvinceCode(provinceId.toString()));
            provinceFlag = true;
        }
        if (cityId != null && !cityId.toString().equals(formCityId)) {
            vehicleDTO.setCityId(CommonUtil.getCityCode(cityId.toString()));
            cityFlag = true;
        }
        if (cityFlag || provinceFlag) {
            vehicleService.setAdministrativeDivision(vehicleDTO);
            vehicleService.updateDivision(vehicleDTO.getId(), vehicleDTO.getProvinceId(), vehicleDTO.getCityId());
        }
    }

    private void comparisonEquipment(RegistrationInfo info, DeviceDTO deviceDTO) {
        String venderName = info.getVenderName();
        String deviceType = info.getDeviceType();
        String manufacturerId = deviceDTO.getManufacturerId();
        String deviceModelNumber = deviceDTO.getDeviceModelNumber();
        boolean manufacturerFlag = false;
        boolean deviceModelFlag = false;
        if (venderName != null && !venderName.equals(manufacturerId)) {
            deviceDTO.setManufacturerId(venderName);
            manufacturerFlag = true;
        }
        if (deviceType != null && !deviceType.equals(deviceModelNumber)) {
            deviceDTO.setDeviceModelNumber(deviceType);
            deviceModelFlag = true;
        }
        if (manufacturerFlag || deviceModelFlag) {
            //???????????????  ??????
            deviceService.updateDeviceManufacturer(deviceDTO);
        }
    }

    /**
     * ??????????????????yyMMddhhmmss ???yyyy-MM-dd hh:mm:ss
     * @param time ????????????????????????????????????
     * @return ?????????????????????????????????
     */
    private String getFormatTime(String time) {
        StringBuilder buf = new StringBuilder(20);
        final int year = LocalDate.now().getYear();
        buf.append(year / 100).append(time, 0, 2).append("-") //yyyy
            .append(time, 2, 4).append("-") //MM
            .append(time, 4, 6).append(" ") //dd
            .append(time, 6, 8).append(":") //hh
            .append(time, 8, 10).append(":") //mm
            .append(time, 10, 12); //ss
        return buf.toString();
    }

    public void handleIcCardMessage(IcCardMessage icCardMessage) {
        switch (icCardMessage.getType()) {
            case ConstantUtil.T808_DRIVER_INFO:
                deviceMessageHandler.saveDriverInfoCollectionLog((Message) icCardMessage.getData());
                break;
            case ConstantUtil.T808_MULTIMEDIA_DATA:
                JSONObject jsonObject = (JSONObject) icCardMessage.getData();
                dealIcProfessionalPic(jsonObject.getString("deviceNumber"), jsonObject.getString("path"));
                break;
            default:
                break;
        }
    }

}
