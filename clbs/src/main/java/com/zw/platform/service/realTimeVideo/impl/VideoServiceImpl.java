package com.zw.platform.service.realTimeVideo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.VehicleSpotCheckInfo;
import com.cb.platform.repository.mysqlDao.SpotCheckReportDao;
import com.zw.adas.utils.AdasDirectiveStatusOutTimeUtil;
import com.zw.adas.utils.FastDFSClient;
import com.zw.lkyw.domain.VideoInspectionData;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.constant.MonitorTypeEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.service.OrganizationService;
import com.zw.platform.basic.service.UserService;
import com.zw.platform.basic.util.common.MonitorHelper;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.controller.realTimeVideo.ResourceListController;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.multimedia.form.OrderForm;
import com.zw.platform.domain.realTimeVideo.AudioParam;
import com.zw.platform.domain.realTimeVideo.AudioVideoRransmitForm;
import com.zw.platform.domain.realTimeVideo.DiskInfo;
import com.zw.platform.domain.realTimeVideo.ResourceListBean;
import com.zw.platform.domain.realTimeVideo.ResourceListSend;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.domain.realTimeVideo.VideoControlSend;
import com.zw.platform.domain.realTimeVideo.VideoPlayResultDTO;
import com.zw.platform.domain.realTimeVideo.VideoRequest;
import com.zw.platform.domain.realTimeVideo.VideoResourcesMonth;
import com.zw.platform.domain.realTimeVideo.VideoSendForm;
import com.zw.platform.domain.realTimeVideo.VideoSleepSetting;
import com.zw.platform.event.VideoPlayEvent;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.push.mqserver.ZMQFencePub;
import com.zw.platform.repository.modules.MediaDao;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.repository.realTimeVideo.VideoSettingDao;
import com.zw.platform.repository.realTimeVideo.VideoSleepSettingDao;
import com.zw.platform.repository.vas.AlarmSettingDao;
import com.zw.platform.service.monitoring.OrderService;
import com.zw.platform.service.realTimeVideo.VideoOrderSendService;
import com.zw.platform.service.realTimeVideo.VideoService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.Customer;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.common.VehicleUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import com.zw.protocol.msg.Message;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.msg.t808.body.LocationInfo;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import com.zw.ws.entity.t808.oil.T808_0x8900;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * ????????????Service
 * @author hujun
 * @version ???????????????2017???12???28??? ??????3:58:13
 */
@Service
public class VideoServiceImpl implements VideoService {
    private static final Logger log = LogManager.getLogger(VideoServiceImpl.class);

    private static final String VIDEO_MODULE = "REALTIMEVIDEO";

    @Value("${mediaServer.ip}")
    private String serverIp;// ???????????????????????????

    @Value("${mediaServer.port.rtp.video}")
    private Integer tcpPort;// ????????????TCP??????

    @Value("${mediaServer.port.udp}")
    private Integer udpPort;// ????????????UDP??????

    @Value("${mediaServer.port.rtp.audio}")
    private Integer audioTcpPort;// ??????TCP??????
    @Autowired
    private MonitorHelper monitorHelper;
    @Autowired
    VideoOrderSendService videoOrderSendService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private VideoChannelSettingDao videoChannelSettingDao;

    @Autowired
    VideoSleepSettingDao videoSleepSettingDao;

    @Autowired
    private SpotCheckReportDao spotCheckReportDao;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AlarmSettingDao alarmSettingDao;

    @Autowired
    private AdasDirectiveStatusOutTimeUtil adasDirectiveStatusOutTimeUtil;

    @Autowired
    FastDFSClient fastDFSClient;

    @Autowired
    MediaDao mediaDao;

    @Autowired
    private VideoSettingDao videoSettingDao;

    @Autowired
    private UserService userService;

    @Override
    public JSONArray getChannelsByVehicleId(String vehicleId, boolean isChecked) {
        // ????????????????????????????????????????????????
        List<VideoChannelSetting> vcs = videoChannelSettingDao.getVideoChannelByVehicleId(vehicleId);
        if (vcs == null || vcs.isEmpty()) {
            return new JSONArray();
        }
        // ?????????????????????
        return handleChannelTreeValue(vcs, isChecked);
    }

    @Override
    public JSONArray getChannelsByVehicleIds(String vehicleId, boolean isChecked) {
        if (StringUtils.isEmpty(vehicleId)) {
            return new JSONArray();
        }
        List<String> vids = Arrays.asList(vehicleId.split(","));
        // ????????????????????????????????????????????????
        List<VideoChannelSetting> vcs = videoChannelSettingDao.getVideoChannelByVehicleIds(vids);
        if (vcs == null || vcs.isEmpty()) {
            return new JSONArray();
        }
        // ?????????????????????
        return handleChannelTreeValue(vcs, isChecked);
    }

    /*
     * ?????????????????????
     */
    private static JSONArray handleChannelTreeValue(List<VideoChannelSetting> vcs, boolean isChecked) {
        JSONArray result = new JSONArray();
        for (VideoChannelSetting v : vcs) {
            JSONObject channelObj = new JSONObject();
            channelObj.put("vehicleId", v.getVehicleId());
            channelObj.put("type", "channel");
            channelObj.put("iconSkin", "channelSkin");
            channelObj.put("name", v.getLogicChannel());
            channelObj.put("physicsChannel", v.getPhysicsChannel());
            channelObj.put("logicChannel", v.getLogicChannel());
            channelObj.put("channelType", v.getChannelType());
            channelObj.put("sort", v.getSort());
            channelObj.put("connectionFlag", v.getConnectionFlag());
            channelObj.put("panoramic", v.getPanoramic());
            channelObj.put("streamType", v.getStreamType());
            channelObj.put("mobile", v.getMobile());
            if (isChecked) {
                channelObj.put("checked", true);
            }
            result.add(channelObj);
        }
        return result;
    }

    @Override
    public void sendParam(VideoSendForm form, String ipAddress, String equipmentType) {
        StringBuilder logMessage = new StringBuilder();// ????????????
        String module = VIDEO_MODULE;// ??????????????????????????????????????????
        int orderType = form.getOrderType();
        String vehicleId = form.getVehicleId();
        BindDTO bindDTO = null;
        if (orderType != 5) {
            bindDTO = MonitorUtils.getBindDTO(vehicleId);
            Objects.requireNonNull(bindDTO);
            // ???????????????????????????
            setMonitorInfo(bindDTO, form);
        }
        switch (orderType) {
            case 1:// ????????????
                VideoRequest vo1 = new VideoRequest();
                setBasicsParam9101(vo1, audioTcpPort);
                vo1.setChannelNum(Integer.parseInt(form.getChannelNum()));
                vo1.setStreamType(Integer.parseInt(form.getStreamType()));
                vo1.setDeviceType(form.getDeviceType());
                vo1.setType(2);
                // ??????
                videoOrderSendService.sendVideoRequest(form, vo1);
                logMessage.append("???????????? (").append(form.getBrand()).append(")_??????");
                break;
            case 3:// ????????????
                VideoRequest vo3 = new VideoRequest();
                setBasicsParam9101(vo3, audioTcpPort);
                vo3.setChannelNum(Integer.parseInt(form.getChannelNum()));
                vo3.setStreamType(Integer.parseInt(form.getStreamType()));
                vo3.setDeviceType(form.getDeviceType());
                vo3.setType(3);
                // ??????
                videoOrderSendService.sendVideoRequest(form, vo3);
                logMessage.append("???????????? (").append(form.getBrand()).append(")_??????");
                break;
            case 5:// ????????????
                String channelNum = form.getChannelNum();
                String streamType = form.getStreamType();
                if (!StringUtils.isEmpty(vehicleId) && !StringUtils.isEmpty(channelNum)) {
                    String[] vehicleIds = vehicleId.split(",");
                    String[] channelNums = channelNum.split(",");
                    String[] streamTypes = streamType.split(",");
                    StringBuilder brands = new StringBuilder();
                    Map<String, BindDTO> bindMap = MonitorUtils.getBindDTOMap(new HashSet<>(Arrays.asList(vehicleIds)));
                    for (int i = 0; i < vehicleIds.length; i++) {
                        // ???????????????????????????
                        form.setVehicleId(vehicleIds[i]);
                        BindDTO config = bindMap.get(vehicleIds[i]);
                        if (config == null) {
                            continue;
                        }
                        setMonitorInfo(config, form);
                        // ??????????????????
                        VideoRequest vo5 = new VideoRequest();
                        setBasicsParam9101(vo5, audioTcpPort);
                        vo5.setChannelNum(Integer.parseInt(channelNums[i]));
                        vo5.setStreamType(Integer.parseInt(streamTypes[i]));
                        vo5.setDeviceType(config.getDeviceType());
                        vo5.setType(4);
                        // ??????
                        videoOrderSendService.sendVideoRequest(form, vo5);
                        // ??????????????????
                        brands.append(form.getBrand()).append(",");
                    }
                    logMessage.append("???????????? (").append(brands.substring(0, brands.length() - 1)).append(")_??????");
                }
                break;
            case 7:// ???????????????
                saveVideoChannelSetting(form);
                // ????????????
                logMessage.append("???????????? (").append(form.getBrand()).append(")_???????????????");
                break;
            case 8:// ?????????????????????
                Objects.requireNonNull(bindDTO);
                videoOrderSendService.sendAttributeQuery(form, bindDTO.getDeviceType());
                // ????????????
                logMessage.append("???????????? (").append(form.getBrand()).append(")_?????????????????????");
                break;
            case 9:// ??????????????????
                VideoSleepSetting videoSleepSetting = saveVideoSleepSetting(form);
                // ??????????????????
                Map<String, Object> videoParams9 = new HashMap<>();
                videoParams9.put("videoSleep", videoSleepSetting);
                // ??????
                videoOrderSendService.sendVideoParamSetting(form, videoParams9);
                // ????????????
                logMessage.append("???????????? (").append(form.getBrand()).append(")_??????????????????");
                break;
            case 10:// ????????????
                // ??????????????????
                T808_0x8900<?> t8080x8900 = new T808_0x8900<>();
                t8080x8900.setType(form.getMsgId());
                t8080x8900.setData(form.getAwakenTime().getBytes(StandardCharsets.UTF_8));
                // ??????
                Objects.requireNonNull(bindDTO);
                videoOrderSendService.sendVideoSleep(form, t8080x8900, bindDTO.getDeviceType());
                // ????????????
                logMessage.append("???????????? (").append(form.getBrand()).append(")_????????????");
                break;
            case 11:// ????????????????????????????????????
                // ??????????????????
                VideoRequest vo = new VideoRequest();
                vo.setDeviceType(form.getDeviceType());
                setBasicsParam9101(vo, tcpPort);
                vo.setChannelNum(Integer.parseInt(form.getChannelNum()));
                if (StringUtils.isNotBlank(form.getStreamType())) {
                    vo.setStreamType(Integer.parseInt(form.getStreamType()));
                }
                if (form.getChannelType() != null && form.getChannelType() == 0) { // ?????????
                    vo.setType(0);
                } else {
                    vo.setType(1);
                }

                // ??????
                Objects.requireNonNull(bindDTO);
                int msgSN = videoOrderSendService.sendVideoRequest(form, vo);
                videoInspect(form, bindDTO, vo, msgSN);
                // ????????????????????????
                Integer operationIssueType = form.getOperationIssueType();
                if (null != operationIssueType && 1 == operationIssueType) {
                    module = "";
                }
                // ????????????
                logMessage.append("???????????? (").append(form.getBrand()).append(")_????????? ").append(vo.getChannelNum())
                    .append(" ??????????????????");
                // }
                break;
            case 22: // ??????
                // ??????????????????
                OrderForm orderForm = new OrderForm();
                orderForm.setVid(form.getVehicleId());
                orderForm.setWayID(Integer.parseInt(form.getChannelNum())); // ?????????
                orderForm.setCommand(1); // ????????????
                orderForm.setTime(0); // ????????????/????????????
                orderForm.setSaveSign(0); // ????????????
                orderForm.setDistinguishability(1); // ?????????
                orderForm.setQuality(5); // ????????????
                orderForm.setLuminance(125); // ??????
                orderForm.setContrast(60); // ?????????
                orderForm.setSaturability(60); // ?????????
                orderForm.setChroma(125); // ??????
                Customer c = new Customer(); // ??????????????????????????????
                form.setSerialNumber(Integer.valueOf(c.getCustomerID()));
                orderForm.setSerialNumber(form.getSerialNumber()); // ?????????
                orderService.takePhoto(orderForm);
                // ????????????
                logMessage.append("???????????? (").append(form.getBrand()).append(")_????????? ").append(form.getChannelNum())
                    .append(" ??????");
                break;
            default:
                break;
        }
        // ???????????????????????????
        if (!StringUtils.isEmpty(logMessage.toString())) {
            // ??????????????????
            String logSource;
            if ("APP".equals(equipmentType)) {
                logSource = "4";
            } else {
                logSource = "3";
            }
            // ????????????
            String[] vehicle = logSearchService.findCarMsg(vehicleId);
            logSearchService.addLog(ipAddress, logMessage.toString(), logSource, module, vehicle[0], vehicle[1]);
        }
    }

    @Override
    public String saveToRedis(String vehicleId, String channelNumber) {
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        Objects.requireNonNull(bindDTO);
        final String deviceId = bindDTO.getDeviceId();
        final String deviceType = bindDTO.getDeviceType();
        final String simcardNumber = bindDTO.getSimCardNumber();
        Integer format = videoSettingDao.findAudioFormatByDeviceId(deviceId);
        String audioFormatStr = getAudioFormat(format);
        return saveToRedis(vehicleId, simcardNumber, channelNumber, audioFormatStr, deviceType);
    }

    @Override
    public String saveToRedis(String vehicleId, String simcardNumber, String channelNumber, String audioFormatStr,
        String deviceType) {
        if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(simcardNumber) && StringUtils
            .isNotBlank(channelNumber)) {
            String unique = UUID.randomUUID().toString();
            Map<String, String> map = new HashMap<>();
            map.put("vehicleId", vehicleId);
            map.put("simcardNumber", simcardNumber);
            map.put("channelNumber", channelNumber);
            map.put("deviceType", deviceType);
            if (StringUtils.isNotBlank(audioFormatStr)) {
                map.put("audioFormat ", audioFormatStr);
            }
            RedisHelper.addToHash(HistoryRedisKeyEnum.VIDEO_SEND_PARAM.of(unique), map);
            RedisHelper.expireKey(HistoryRedisKeyEnum.VIDEO_SEND_PARAM.of(unique), 60);
            return unique;
        }
        return null;
    }

    /**
     * ????????????9101????????????
     * @author hujun
     * @since ???????????????2018???1???2??? ??????5:28:55
     */
    @Override
    public void setBasicsParam9101(VideoRequest vo, Integer port) {
        vo.setServerIp(serverIp);
        vo.setTcpPort(port);
        vo.setUdpPort(udpPort);
    }

    @Override
    public void setMonitorInfo(BindDTO bindDTO, VideoSendForm form) {
        // ??????????????????
        form.setDeviceId(bindDTO.getDeviceId());
        form.setSimNumber(bindDTO.getSimCardNumber());
        form.setBrand(bindDTO.getName());
        form.setDeviceNumber(bindDTO.getDeviceNumber());
        form.setMonitorType(bindDTO.getMonitorType());
        form.setDeviceType(bindDTO.getDeviceType());
    }

    /**
     * ????????????????????????????????????
     * @author hujun
     * @since ???????????????2018???1???5??? ??????9:59:27
     */
    private void saveVideoChannelSetting(VideoSendForm form) {
        List<VideoChannelSetting> videoChannelSettings =
            JSON.parseArray(form.getContrasts(), VideoChannelSetting.class);
        // ????????????????????????????????????
        videoChannelSettingDao.delete(form.getVehicleId());
        if (CollectionUtils.isEmpty(videoChannelSettings)) {
            return;
        }
        videoChannelSettings.forEach(videoChannel -> {
            Boolean panoramic = videoChannel.getPanoramic();
            if (panoramic == null) {
                videoChannel.setPanoramic(false);
            }
            videoChannel.setVehicleId(form.getVehicleId());
            videoChannel.setCreateDataUsername(SystemHelper.getCurrentUsername());
        });
        videoChannelSettingDao.addVideoChannels(videoChannelSettings);
        ZMQFencePub.pubChangeFence("20");
    }

    /**
     * ???????????????????????????
     * @author hujun
     * @since ???????????????2018???1???5??? ??????11:44:44
     */
    private VideoSleepSetting saveVideoSleepSetting(VideoSendForm form) {
        VideoSleepSetting videoSleepSetting = new VideoSleepSetting();
        BeanUtils.copyProperties(form, videoSleepSetting);
        if (null == videoSleepSettingDao.getVideoSleepByVehicleId(videoSleepSetting.getVehicleId())) {
            videoSleepSetting.setId(UUID.randomUUID().toString());
            videoSleepSettingDao.saveVideoSleep(videoSleepSetting);
        } else {
            videoSleepSettingDao.updateVideoSleep(videoSleepSetting);
        }
        return videoSleepSetting;
    }

    @Override
    public void sendRealTimeControl(AudioVideoRransmitForm form, String ipAddress) throws Exception {
        String vehicleId = form.getVehicleId(); // ????????????id
        String channelNum = form.getChannelNum(); // ???????????????
        String channelType = form.getChannelType(); // ???????????????
        // ???????????????(???????????????????????????????????????????????????)
        String userName = form.getUserName();
        StringBuilder logMessage = new StringBuilder(); // ?????????????????????
        String monitoringOperation = ""; // ??????????????????
        if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(channelNum) && StringUtils
            .isNotBlank(channelType)) {
            String[] vehicleIds = vehicleId.split(","); // ????????????id(???????????????????????????(**??????))
            String[] channelNums = channelNum.split(",");
            for (int i = 0; i < vehicleIds.length; i++) {
                // ??????????????????????????????????????????
                BindDTO bindDTO = monitorHelper.getBindDTO(vehicleIds[i], MonitorTypeEnum.VEHICLE);
                if (bindDTO != null) {
                    String logicalChannel = channelNums[i]; // ???????????????
                    String brand = bindDTO.getName(); // ?????????
                    // ????????????
                    switch (form.getOrderType()) { // ??????????????????
                        case 20:
                            logMessage.append("???????????? (").append(brand).append(")_????????? ").append(logicalChannel)
                                .append("???????????????");
                            break;
                        case 21:
                            logMessage.append("???????????? (").append(brand).append(")_????????? ").append(logicalChannel)
                                .append(" ???????????????");
                            break;
                        default:
                            break;
                    }
                }
            }
            //??????????????????
            String logSource;
            if (form.getEquipmentType() != null && "APP".equals(form.getEquipmentType())) {
                logSource = "4";
            } else {
                logSource = "3";
            }
            if (vehicleIds.length > 1) { // ????????????
                List<String> list = Arrays.asList(vehicleIds);
                Set<String> h = new HashSet<>(list);
                if (h.size() > 1) {
                    logSearchService.addLog(ipAddress, logMessage.toString(), logSource, VIDEO_MODULE,
                        monitoringOperation); // ????????????????????????
                } else {
                    String[] vehicle = logSearchService.findCarMsg(vehicleIds[0]);
                    logSearchService
                        .addLogByUserName(ipAddress, logMessage.toString(), logSource, VIDEO_MODULE, userName,
                            vehicle[0], vehicle[1]); // ???????????????
                }

            } else {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService
                    .addLogByUserName(ipAddress, logMessage.toString(), logSource, VIDEO_MODULE, userName, vehicle[0],
                        vehicle[1]); // ???????????????
            }
        }
    }

    /**
     * ??????0x9102
     * @param vehicleId        ????????????id
     * @param videoControlSend // ??????????????????
     */
    private void send0x9102(String vehicleId, VideoControlSend videoControlSend) {
        //??????????????????????????????????????????
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        if (bindDTO != null) {
            String deviceId = StringUtils.isBlank(bindDTO.getDeviceId()) ? "" : bindDTO.getDeviceId();
            String mobile = StringUtils.isBlank(bindDTO.getSimCardNumber()) ? "" : bindDTO.getSimCardNumber();
            if (!mobile.isEmpty()) {
                VideoSendForm form = new VideoSendForm();
                form.setVehicleId(vehicleId);
                form.setDeviceId(deviceId);
                form.setSimNumber(mobile);
                String deviceType = bindDTO.getDeviceType();
                // ??????
                videoOrderSendService.sendVideoControl(form, videoControlSend, deviceType);
            }
        }
    }

    @Override
    public AudioParam getAudioParam(String vehicleId) {
        if (StringUtils.isNotBlank(vehicleId)) {
            return videoSettingDao.getVideoParam(vehicleId);
        }
        return null;
    }

    @Override
    public DiskInfo getDiskInfo() {
        String diskStr = RedisHelper.getString(HistoryRedisKeyEnum.VIDEO_DISKINFO.of());
        DiskInfo disk = new DiskInfo();
        if (StringUtils.isNotBlank(diskStr)) {
            disk = JSON.parseObject(diskStr, DiskInfo.class);
        } else {
            disk.setVideoPlayTime(300);
            disk.setVideoStopTime(30);
            disk.setMemoryRate(20);
            disk.setMemory(0);
            disk.setMemoryType(0);
        }
        return disk;
    }

    /**
     * ?????????????????????????????????
     * @param format ??????????????????
     * @return ?????????????????????
     */
    @Override
    public String getAudioFormat(Integer format) {
        String audioFormatStr;
        // 0:ADPCMA; 2:G726-16K; 3:G726-24K; 4:G726-32K; 5:G726-40K; 6:G711a; 7:G711u;
        switch (format) {
            case 0:
                audioFormatStr = "ADPCMA";
                break;
            case 2:
                audioFormatStr = "G726-16K";
                break;
            case 3:
                audioFormatStr = "G726-24K";
                break;
            case 4:
                audioFormatStr = "G726-32K";
                break;
            case 5:
                audioFormatStr = "G726-40K";
                break;
            case 6:
                audioFormatStr = "G711a";
                break;
            case 7:
                audioFormatStr = "G711u";
                break;
            default:
                audioFormatStr = "";
                break;
        }
        return audioFormatStr;
    }

    @Override
    public boolean saveMedia(MediaForm media, MultipartFile file) {
        String imageFilename = null;
        try {
            int imageLength = (int) file.getSize();
            InputStream inputStream = file.getInputStream();
            // ??????????????????
            String originalFilename = UUID.randomUUID() + ".jpg";
            imageFilename = fastDFSClient.uploadFile(inputStream, imageLength, originalFilename);
        } catch (Exception e) {
            log.error("??????????????????????????????fastDFS?????????", e);
        }
        if (imageFilename == null) {
            return false;
        }
        BindDTO bindDTO = MonitorUtils.getBindDTO(media.getVehicleId(), "name");
        Objects.requireNonNull(bindDTO);
        media.setBrand(bindDTO.getName());
        media.setMediaName(imageFilename.substring((imageFilename.lastIndexOf("/") + 1)));
        media.setMediaUrlNew(imageFilename);
        media.setCreateDataTime(new Date());
        media.setFormatCode(0);
        media.setType(0);
        media.setEventCode(-1);
        media.setFlag(1);
        media.setCreateDataTime(new Date());
        media.setCreateDataUsername(SystemHelper.getCurrentUsername());
        return mediaDao.addMedia(media);
    }

    @Override
    public JsonResultBean sendGetHistoryMonthInstruct(ResourceListBean resourceListBean, String sessionId,
        String userName, String ip) {
        String vehicleId = resourceListBean.getVehicleId();
        // ??????920F???????????????????????????
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        if (Objects.isNull(bindDTO)) {
            return new JsonResultBean();
        }

        // ??????ID
        String deviceId = bindDTO.getDeviceId();
        VideoResourcesMonth videoResourcesMonth =
            new VideoResourcesMonth(Integer.valueOf(resourceListBean.getVideoType()), resourceListBean.getDate());
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, bindDTO.getDeviceNumber());

        // ????????????
        if (msgSno == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        SubscibeInfo info = new SubscibeInfo(userName, sessionId, deviceId, msgSno,
            ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK_BEFOR);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message t808Message = MsgUtil
            .get808Message(bindDTO.getSimCardNumber(), ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP_BEFOR, msgSno,
                videoResourcesMonth, bindDTO.getDeviceType());
        WebSubscribeManager.getInstance()
            .sendMsgToAll(t808Message, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP_BEFOR, deviceId);
        String moName = bindDTO.getName();
        logSearchService
            .addLog(ip, "????????????(" + moName + ") ??????????????????", "3", "", moName, String.valueOf(bindDTO.getPlateColor()));
        return new JsonResultBean();
    }

    @Override
    public JsonResultBean sendGetHistoryDayInstruct(ResourceListBean resourceListBean, String sessionId,
        String userName, String ip) {
        String vehicleId = resourceListBean.getVehicleId();
        // ??????9205??????????????????????????????
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        if (Objects.isNull(bindDTO)) {
            return new JsonResultBean();
        }
        // ??????ID
        String deviceId = bindDTO.getDeviceId();
        ResourceListSend resourceListSend = new ResourceListSend();
        // ?????????
        resourceListSend.setChannelNum(Integer.valueOf(resourceListBean.getChannlNumer()));
        if (StringUtils.isNotBlank(resourceListBean.getAlarmType())) {
            // ????????????
            resourceListSend.setAlarm(Long.parseLong(resourceListBean.getAlarmType()));
        } else {
            resourceListSend.setAlarm(0);
        }
        // ????????????
        resourceListSend.setStartTime(
            DateUtil.getStringToString(resourceListBean.getStartTime(), null, DateUtil.DATE_YYMMDDHHMMSS));
        // ????????????
        resourceListSend
            .setEndTime(DateUtil.getStringToString(resourceListBean.getEndTime(), null, DateUtil.DATE_YYMMDDHHMMSS));
        // ?????? 0:???????????????1???????????????2????????????
        resourceListSend.setStreamType(resourceListBean.getStreamType());
        // ????????????????????? 0:????????????1????????????2????????????3??????????????????
        resourceListSend.setVideoType(Integer.valueOf(resourceListBean.getVideoType()));
        // ???????????? 0?????????????????????1??????????????????2??????????????????
        resourceListSend.setStorageType(resourceListBean.getStorageType());
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, bindDTO.getDeviceNumber());
        // ??????????????????
        if (msgSno == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "??????????????????");
        }
        ResourceListController.T808_2011_1078_CACHE.put(resourceListBean.getVehicleId(), resourceListBean);
        SubscibeInfo info = new SubscibeInfo(userName, sessionId, deviceId, msgSno,
            ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK);
        SubscibeInfoCache.getInstance().putTable(info);
        T808Message t808Message = MsgUtil
            .get808Message(bindDTO.getSimCardNumber(), ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP, msgSno,
                resourceListSend, bindDTO.getDeviceType());
        WebSubscribeManager.getInstance()
            .sendMsgToAll(t808Message, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP, deviceId);
        logSearchService.addLog(ip, "????????????(" + bindDTO.getName() + ") ??????????????????", "3", "", bindDTO.getName(),
            String.valueOf(bindDTO.getPlateColor()));
        return new JsonResultBean();
    }

    private void videoInspect(VideoSendForm form, BindDTO bindDTO, VideoRequest vo, int msgSN) {
        // ???????????????????????????  ??????????????????
        if (bindDTO != null && Objects.equals(bindDTO.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
            // ?????????????????????????????????  ?????????id?????????????????? ??????????????????
            VideoInspectionData data =
                videoChannelSettingDao.getVehicleInfoAndPhysicsChannel(form.getVehicleId(), vo.getChannelNum());
            OrganizationLdap organization = organizationService.getOrganizationByUuid(data.getGroupId());
            data.setStartTime(DateUtil.getDateToString(new Date(), "yyyyMMddHHmmss"));
            data.setGroupName(organization.getName());
            if (msgSN >= 0) {
                // ???????????????????????????0001
                String key = form.getVehicleId() + "_" + msgSN;
                adasDirectiveStatusOutTimeUtil.putVideoInspectionCache(key, data);
            } else {
                // ??????????????????hbase????????????????????????????????????
                data.setStatus(1);
                data.setFailReason(VideoInspectionData.OFF_LINE_MSG);
                data.setMonitorId(form.getVehicleId());
                adasDirectiveStatusOutTimeUtil.videoInspectionHandler(null, data);
            }
        }
    }

    private void videoInspect(String vehicleId, Integer channel) {
        // ?????????????????????????????????  ?????????id?????????????????? ??????????????????
        VideoInspectionData data = videoChannelSettingDao.getVehicleInfoAndPhysicsChannel(vehicleId, channel);
        OrganizationLdap organization = organizationService.getOrganizationByUuid(data.getGroupId());
        data.setStartTime(DateUtil.getDateToString(new Date(), "yyyyMMddHHmmss"));
        data.setGroupName(organization.getName());
        data.setStatus(0);
        data.setMonitorId(vehicleId);
        adasDirectiveStatusOutTimeUtil.videoInspectionHandler(null, data);
    }

    @EventListener
    public void onPlayEvent(VideoPlayEvent event) {
        videoInspect(event.getMonitorId(), event.getChannel());
    }


    @Override
    public JsonResultBean saveVideoInspectionRecord(VideoPlayResultDTO videoPlayResultDTO) {
        String vehicleId = videoPlayResultDTO.getVehicleId();
        Integer channelNumber = videoPlayResultDTO.getChannelNumber();
        Integer playStatus = videoPlayResultDTO.getPlayStatus();
        Integer failReason = videoPlayResultDTO.getFailReason();
        if (StringUtils.isBlank(vehicleId) || playStatus == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        }
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "?????????????????????");
        }
        userService.setObjectTypeName(bindDTO);
        String startTimeStr = DateUtil.getDateToString(new Date(), DateUtil.DATE_FORMAT);
        // ???????????????????????????????????? ????????????????????????????????????????????????;
        if (playStatus == 1 && failReason == 1) {
            List<VideoChannelSetting> videoChannelSettings =
                videoChannelSettingDao.getVideoChannelByVehicleId(vehicleId);
            for (VideoChannelSetting channelSetting : videoChannelSettings) {
                Map<String, String> queryParam =
                    assembleVideoInspectionParamMap(startTimeStr, playStatus, failReason, bindDTO, channelSetting);
                HttpClientUtil.send(PaasCloudUrlEnum.SAVE_VIDEO_INSPECTION_URL, queryParam);
            }
            return new JsonResultBean();
        }
        VideoChannelSetting channelSetting =
            videoChannelSettingDao.getVideoChannelByVehicleIdAndLogicChannel(vehicleId, channelNumber);
        Map<String, String> queryParam =
            assembleVideoInspectionParamMap(startTimeStr, playStatus, failReason, bindDTO, channelSetting);
        HttpClientUtil.send(PaasCloudUrlEnum.SAVE_VIDEO_INSPECTION_URL, queryParam);
        return new JsonResultBean();
    }

    private Map<String, String> assembleVideoInspectionParamMap(String startTimeStr, Integer playStatus,
        Integer failReason, BindDTO bindDTO, VideoChannelSetting channelSetting) {
        Map<String, String> queryParam = new HashMap<>(16);
        queryParam.put("monitorId", bindDTO.getId());
        queryParam.put("monitorName", bindDTO.getName());
        Integer plateColorInt = bindDTO.getPlateColor();
        String plateColor = plateColorInt == null ? "" : plateColorInt.toString();
        queryParam.put("signColor", plateColor);
        queryParam.put("objectType", bindDTO.getObjectTypeName());
        queryParam.put("groupName", bindDTO.getOrgName());
        final String channelNum = Optional.ofNullable(channelSetting)
                .map(VideoChannelSetting::getPhysicsChannel)
                .map(Object::toString)
                .orElse(null);
        queryParam.put("channelNum", channelNum);
        queryParam.put("startTime", startTimeStr);
        queryParam.put("status", playStatus.toString());
        queryParam.put("failReason", failReason == null ? null : failReason.toString());
        return queryParam;
    }

    @Override
    public JsonResultBean saveVideoSpotCheckRecord(String vehicleId) throws Exception {
        if (StringUtils.isBlank(vehicleId)) {
            return new JsonResultBean(JsonResultBean.FAULT, "????????????");
        }
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "monitorType");
        if (!Objects.equals(bindDTO.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
            return new JsonResultBean();
        }
        VehicleSpotCheckInfo vehicleSpotCheckInfo = new VehicleSpotCheckInfo();
        vehicleSpotCheckInfo.setVehicleId(vehicleId);
        //  ??????, ??????????????????????????????????????????????????????
        Integer speedLimit = alarmSettingDao.getSpeedLimitByVehicleId(vehicleId);
        if (Objects.nonNull(speedLimit)) {
            vehicleSpotCheckInfo.setSpeedLimit(String.valueOf(speedLimit));
        }
        String location = RedisHelper.getString(HistoryRedisKeyEnum.MONITOR_LOCATION.of(vehicleId));
        if (StringUtils.isNotBlank(location)) {
            Message message = JSON.parseObject(location, Message.class);
            if (message != null) {
                T808Message t808Message = JSON.parseObject(message.getData().toString(), T808Message.class);
                LocationInfo info = JSON.parseObject(JSON.toJSONString(t808Message.getMsgBody()), LocationInfo.class);
                String gpsTime = "20" + info.getGpsTime();
                Double gpsSpeed = info.getGpsSpeed();
                Date locationTime = DateUtils.parseDate(gpsTime, DateUtil.DATE_FORMAT);
                vehicleSpotCheckInfo.setLocationTime(locationTime);
                vehicleSpotCheckInfo.setSpeed(String.valueOf(gpsSpeed));
                vehicleSpotCheckInfo.setLongtitude(String.valueOf(info.getLongitude()));
                vehicleSpotCheckInfo.setLatitude(String.valueOf(info.getLatitude()));
            }
        }
        vehicleSpotCheckInfo.setSpotCheckContent(VehicleSpotCheckInfo.SPOT_CHECK_CONTENT_VIDEO);
        vehicleSpotCheckInfo.setSpotCheckUser(SystemHelper.getCurrentUsername());
        Date date = new Date();
        vehicleSpotCheckInfo.setSpotCheckTime(date);
        vehicleSpotCheckInfo.setActualViewDate(date);
        spotCheckReportDao.addVehicleSpotCheckInfo(vehicleSpotCheckInfo);
        return new JsonResultBean();
    }
}
