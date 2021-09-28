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
 * 实时视频Service
 * @author hujun
 * @version 创建时间：2017年12月28日 下午3:58:13
 */
@Service
public class VideoServiceImpl implements VideoService {
    private static final Logger log = LogManager.getLogger(VideoServiceImpl.class);

    private static final String VIDEO_MODULE = "REALTIMEVIDEO";

    @Value("${mediaServer.ip}")
    private String serverIp;// 实时视频服务器地址

    @Value("${mediaServer.port.rtp.video}")
    private Integer tcpPort;// 实时视频TCP端口

    @Value("${mediaServer.port.udp}")
    private Integer udpPort;// 实时视频UDP端口

    @Value("${mediaServer.port.rtp.audio}")
    private Integer audioTcpPort;// 音频TCP端口
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
        // 查询该监控对象下的全部逻辑通道号
        List<VideoChannelSetting> vcs = videoChannelSettingDao.getVideoChannelByVehicleId(vehicleId);
        if (vcs == null || vcs.isEmpty()) {
            return new JSONArray();
        }
        // 组装通道树结构
        return handleChannelTreeValue(vcs, isChecked);
    }

    @Override
    public JSONArray getChannelsByVehicleIds(String vehicleId, boolean isChecked) {
        if (StringUtils.isEmpty(vehicleId)) {
            return new JSONArray();
        }
        List<String> vids = Arrays.asList(vehicleId.split(","));
        // 查询该监控对象下的全部逻辑通道号
        List<VideoChannelSetting> vcs = videoChannelSettingDao.getVideoChannelByVehicleIds(vids);
        if (vcs == null || vcs.isEmpty()) {
            return new JSONArray();
        }
        // 组装通道树结构
        return handleChannelTreeValue(vcs, isChecked);
    }

    /*
     * 组装通道树结构
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
        StringBuilder logMessage = new StringBuilder();// 日志语句
        String module = VIDEO_MODULE;// 模块类型，默认为实时视频模块
        int orderType = form.getOrderType();
        String vehicleId = form.getVehicleId();
        BindDTO bindDTO = null;
        if (orderType != 5) {
            bindDTO = MonitorUtils.getBindDTO(vehicleId);
            Objects.requireNonNull(bindDTO);
            // 设置监控对象基础值
            setMonitorInfo(bindDTO, form);
        }
        switch (orderType) {
            case 1:// 开启对讲
                VideoRequest vo1 = new VideoRequest();
                setBasicsParam9101(vo1, audioTcpPort);
                vo1.setChannelNum(Integer.parseInt(form.getChannelNum()));
                vo1.setStreamType(Integer.parseInt(form.getStreamType()));
                vo1.setDeviceType(form.getDeviceType());
                vo1.setType(2);
                // 下发
                videoOrderSendService.sendVideoRequest(form, vo1);
                logMessage.append("监控对象 (").append(form.getBrand()).append(")_对讲");
                break;
            case 3:// 开启监听
                VideoRequest vo3 = new VideoRequest();
                setBasicsParam9101(vo3, audioTcpPort);
                vo3.setChannelNum(Integer.parseInt(form.getChannelNum()));
                vo3.setStreamType(Integer.parseInt(form.getStreamType()));
                vo3.setDeviceType(form.getDeviceType());
                vo3.setType(3);
                // 下发
                videoOrderSendService.sendVideoRequest(form, vo3);
                logMessage.append("监控对象 (").append(form.getBrand()).append(")_监听");
                break;
            case 5:// 开启广播
                String channelNum = form.getChannelNum();
                String streamType = form.getStreamType();
                if (!StringUtils.isEmpty(vehicleId) && !StringUtils.isEmpty(channelNum)) {
                    String[] vehicleIds = vehicleId.split(",");
                    String[] channelNums = channelNum.split(",");
                    String[] streamTypes = streamType.split(",");
                    StringBuilder brands = new StringBuilder();
                    Map<String, BindDTO> bindMap = MonitorUtils.getBindDTOMap(new HashSet<>(Arrays.asList(vehicleIds)));
                    for (int i = 0; i < vehicleIds.length; i++) {
                        // 设置监控对象基础值
                        form.setVehicleId(vehicleIds[i]);
                        BindDTO config = bindMap.get(vehicleIds[i]);
                        if (config == null) {
                            continue;
                        }
                        setMonitorInfo(config, form);
                        // 组装下发参数
                        VideoRequest vo5 = new VideoRequest();
                        setBasicsParam9101(vo5, audioTcpPort);
                        vo5.setChannelNum(Integer.parseInt(channelNums[i]));
                        vo5.setStreamType(Integer.parseInt(streamTypes[i]));
                        vo5.setDeviceType(config.getDeviceType());
                        vo5.setType(4);
                        // 下发
                        videoOrderSendService.sendVideoRequest(form, vo5);
                        // 组装日志参数
                        brands.append(form.getBrand()).append(",");
                    }
                    logMessage.append("监控对象 (").append(brands.substring(0, brands.length() - 1)).append(")_广播");
                }
                break;
            case 7:// 通道号设置
                saveVideoChannelSetting(form);
                // 日志记录
                logMessage.append("监控对象 (").append(form.getBrand()).append(")_设置通道号");
                break;
            case 8:// 查询音视频属性
                Objects.requireNonNull(bindDTO);
                videoOrderSendService.sendAttributeQuery(form, bindDTO.getDeviceType());
                // 日志记录
                logMessage.append("监控对象 (").append(form.getBrand()).append(")_查询音视频属性");
                break;
            case 9:// 休眠唤醒设置
                VideoSleepSetting videoSleepSetting = saveVideoSleepSetting(form);
                // 组装下发参数
                Map<String, Object> videoParams9 = new HashMap<>();
                videoParams9.put("videoSleep", videoSleepSetting);
                // 下发
                videoOrderSendService.sendVideoParamSetting(form, videoParams9);
                // 日志记录
                logMessage.append("监控对象 (").append(form.getBrand()).append(")_设置休眠唤醒");
                break;
            case 10:// 休眠唤醒
                // 组装下发参数
                T808_0x8900<?> t8080x8900 = new T808_0x8900<>();
                t8080x8900.setType(form.getMsgId());
                t8080x8900.setData(form.getAwakenTime().getBytes(StandardCharsets.UTF_8));
                // 下发
                Objects.requireNonNull(bindDTO);
                videoOrderSendService.sendVideoSleep(form, t8080x8900, bindDTO.getDeviceType());
                // 日志记录
                logMessage.append("监控对象 (").append(form.getBrand()).append(")_休眠唤醒");
                break;
            case 11:// 打开视频（打开所有视频）
                // 组装下发参数
                VideoRequest vo = new VideoRequest();
                vo.setDeviceType(form.getDeviceType());
                setBasicsParam9101(vo, tcpPort);
                vo.setChannelNum(Integer.parseInt(form.getChannelNum()));
                if (StringUtils.isNotBlank(form.getStreamType())) {
                    vo.setStreamType(Integer.parseInt(form.getStreamType()));
                }
                if (form.getChannelType() != null && form.getChannelType() == 0) { // 音视频
                    vo.setType(0);
                } else {
                    vo.setType(1);
                }

                // 下发
                Objects.requireNonNull(bindDTO);
                int msgSN = videoOrderSendService.sendVideoRequest(form, vo);
                videoInspect(form, bindDTO, vo, msgSN);
                // 获取操作下发类型
                Integer operationIssueType = form.getOperationIssueType();
                if (null != operationIssueType && 1 == operationIssueType) {
                    module = "";
                }
                // 日志记录
                logMessage.append("监控对象 (").append(form.getBrand()).append(")_通道号 ").append(vo.getChannelNum())
                    .append(" 请求实时视频");
                // }
                break;
            case 22: // 拍照
                // 组装下发参数
                OrderForm orderForm = new OrderForm();
                orderForm.setVid(form.getVehicleId());
                orderForm.setWayID(Integer.parseInt(form.getChannelNum())); // 通道号
                orderForm.setCommand(1); // 拍摄命令
                orderForm.setTime(0); // 拍照间隔/录像时间
                orderForm.setSaveSign(0); // 保存标志
                orderForm.setDistinguishability(1); // 分辨率
                orderForm.setQuality(5); // 图片质量
                orderForm.setLuminance(125); // 亮度
                orderForm.setContrast(60); // 对比度
                orderForm.setSaturability(60); // 饱和度
                orderForm.setChroma(125); // 色度
                Customer c = new Customer(); // 自增序列号生成流水号
                form.setSerialNumber(Integer.valueOf(c.getCustomerID()));
                orderForm.setSerialNumber(form.getSerialNumber()); // 流水号
                orderService.takePhoto(orderForm);
                // 日志记录
                logMessage.append("监控对象 (").append(form.getBrand()).append(")_通道号 ").append(form.getChannelNum())
                    .append(" 拍照");
                break;
            default:
                break;
        }
        // 日志语句有值则记录
        if (!StringUtils.isEmpty(logMessage.toString())) {
            // 判断设备类型
            String logSource;
            if ("APP".equals(equipmentType)) {
                logSource = "4";
            } else {
                logSource = "3";
            }
            // 日志记录
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
     * 设置基本9101下发参数
     * @author hujun
     * @since 创建时间：2018年1月2日 下午5:28:55
     */
    @Override
    public void setBasicsParam9101(VideoRequest vo, Integer port) {
        vo.setServerIp(serverIp);
        vo.setTcpPort(port);
        vo.setUdpPort(udpPort);
    }

    @Override
    public void setMonitorInfo(BindDTO bindDTO, VideoSendForm form) {
        // 存入基本信息
        form.setDeviceId(bindDTO.getDeviceId());
        form.setSimNumber(bindDTO.getSimCardNumber());
        form.setBrand(bindDTO.getName());
        form.setDeviceNumber(bindDTO.getDeviceNumber());
        form.setMonitorType(bindDTO.getMonitorType());
        form.setDeviceType(bindDTO.getDeviceType());
    }

    /**
     * 保存音视频通道号设置参数
     * @author hujun
     * @since 创建时间：2018年1月5日 上午9:59:27
     */
    private void saveVideoChannelSetting(VideoSendForm form) {
        List<VideoChannelSetting> videoChannelSettings =
            JSON.parseArray(form.getContrasts(), VideoChannelSetting.class);
        // 删除历史设置的音视频通道
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
     * 保存音视频休眠参数
     * @author hujun
     * @since 创建时间：2018年1月5日 上午11:44:44
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
        String vehicleId = form.getVehicleId(); // 监控对象id
        String channelNum = form.getChannelNum(); // 逻辑通道号
        String channelType = form.getChannelType(); // 通道号类型
        // 获取用户名(判断设备对应的通道号是否有用户操作)
        String userName = form.getUserName();
        StringBuilder logMessage = new StringBuilder(); // 下发后记录日志
        String monitoringOperation = ""; // 监控对象操作
        if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(channelNum) && StringUtils
            .isNotBlank(channelType)) {
            String[] vehicleIds = vehicleId.split(","); // 截取车辆id(有单个的情况和多个(**所有))
            String[] channelNums = channelNum.split(",");
            for (int i = 0; i < vehicleIds.length; i++) {
                // 从缓存中取监控对象的绑定信息
                BindDTO bindDTO = monitorHelper.getBindDTO(vehicleIds[i], MonitorTypeEnum.VEHICLE);
                if (bindDTO != null) {
                    String logicalChannel = channelNums[i]; // 逻辑通道号
                    String brand = bindDTO.getName(); // 车牌号
                    // 组装日志
                    switch (form.getOrderType()) { // 确定用户操作
                        case 20:
                            logMessage.append("监控对象 (").append(brand).append(")_通道号 ").append(logicalChannel)
                                .append("切换主码流");
                            break;
                        case 21:
                            logMessage.append("监控对象 (").append(brand).append(")_通道号 ").append(logicalChannel)
                                .append(" 切换子码流");
                            break;
                        default:
                            break;
                    }
                }
            }
            //判断日志来源
            String logSource;
            if (form.getEquipmentType() != null && "APP".equals(form.getEquipmentType())) {
                logSource = "4";
            } else {
                logSource = "3";
            }
            if (vehicleIds.length > 1) { // 记录日志
                List<String> list = Arrays.asList(vehicleIds);
                Set<String> h = new HashSet<>(list);
                if (h.size() > 1) {
                    logSearchService.addLog(ipAddress, logMessage.toString(), logSource, VIDEO_MODULE,
                        monitoringOperation); // 多个监控对象操作
                } else {
                    String[] vehicle = logSearchService.findCarMsg(vehicleIds[0]);
                    logSearchService
                        .addLogByUserName(ipAddress, logMessage.toString(), logSource, VIDEO_MODULE, userName,
                            vehicle[0], vehicle[1]); // 单通道操作
                }

            } else {
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                logSearchService
                    .addLogByUserName(ipAddress, logMessage.toString(), logSource, VIDEO_MODULE, userName, vehicle[0],
                        vehicle[1]); // 单通道操作
            }
        }
    }

    /**
     * 下发0x9102
     * @param vehicleId        监控对象id
     * @param videoControlSend // 下发参数实体
     */
    private void send0x9102(String vehicleId, VideoControlSend videoControlSend) {
        //从缓存中取监控对象的绑定信息
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
                // 下发
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
     * 音频格式数字转字符串。
     * @param format 音频格式数字
     * @return 音频格式字符串
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
            // 生成文件名称
            String originalFilename = UUID.randomUUID() + ".jpg";
            imageFilename = fastDFSClient.uploadFile(inputStream, imageLength, originalFilename);
        } catch (Exception e) {
            log.error("实时视频截图保存上传fastDFS异常！", e);
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
        // 下发920F参数获取月视频资源
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        if (Objects.isNull(bindDTO)) {
            return new JsonResultBean();
        }

        // 终端ID
        String deviceId = bindDTO.getDeviceId();
        VideoResourcesMonth videoResourcesMonth =
            new VideoResourcesMonth(Integer.valueOf(resourceListBean.getVideoType()), resourceListBean.getDate());
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, bindDTO.getDeviceNumber());

        // 设备离线
        if (msgSno == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "终端已离线！");
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
            .addLog(ip, "监控对象(" + moName + ") 查询资源日期", "3", "", moName, String.valueOf(bindDTO.getPlateColor()));
        return new JsonResultBean();
    }

    @Override
    public JsonResultBean sendGetHistoryDayInstruct(ResourceListBean resourceListBean, String sessionId,
        String userName, String ip) {
        String vehicleId = resourceListBean.getVehicleId();
        // 下发9205参数获取单天视频资源
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId);
        if (Objects.isNull(bindDTO)) {
            return new JsonResultBean();
        }
        // 终端ID
        String deviceId = bindDTO.getDeviceId();
        ResourceListSend resourceListSend = new ResourceListSend();
        // 通道号
        resourceListSend.setChannelNum(Integer.valueOf(resourceListBean.getChannlNumer()));
        if (StringUtils.isNotBlank(resourceListBean.getAlarmType())) {
            // 报警类型
            resourceListSend.setAlarm(Long.parseLong(resourceListBean.getAlarmType()));
        } else {
            resourceListSend.setAlarm(0);
        }
        // 开始时间
        resourceListSend.setStartTime(
            DateUtil.getStringToString(resourceListBean.getStartTime(), null, DateUtil.DATE_YYMMDDHHMMSS));
        // 结束时间
        resourceListSend
            .setEndTime(DateUtil.getStringToString(resourceListBean.getEndTime(), null, DateUtil.DATE_YYMMDDHHMMSS));
        // 码流 0:所有码流，1：主码流，2，子码流
        resourceListSend.setStreamType(resourceListBean.getStreamType());
        // 音视频资源类型 0:音视频，1：音频，2：视频，3：音频或视频
        resourceListSend.setVideoType(Integer.valueOf(resourceListBean.getVideoType()));
        // 存储类型 0：所有存储器，1：主存储器，2：灾备存储器
        resourceListSend.setStorageType(resourceListBean.getStorageType());
        Integer msgSno = DeviceHelper.getRegisterDevice(vehicleId, bindDTO.getDeviceNumber());
        // 设备已经注册
        if (msgSno == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "终端已离线！");
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
        logSearchService.addLog(ip, "监控对象(" + bindDTO.getName() + ") 查询资源列表", "3", "", bindDTO.getName(),
            String.valueOf(bindDTO.getPlateColor()));
        return new JsonResultBean();
    }

    private void videoInspect(VideoSendForm form, BindDTO bindDTO, VideoRequest vo, int msgSN) {
        // 视频巡检的相关判断  监控对象为车
        if (bindDTO != null && Objects.equals(bindDTO.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
            // 需要展示的是物理通道号  根据车id和逻辑通道号 查物理通道号
            VideoInspectionData data =
                videoChannelSettingDao.getVehicleInfoAndPhysicsChannel(form.getVehicleId(), vo.getChannelNum());
            OrganizationLdap organization = organizationService.getOrganizationByUuid(data.getGroupId());
            data.setStartTime(DateUtil.getDateToString(new Date(), "yyyyMMddHHmmss"));
            data.setGroupName(organization.getName());
            if (msgSN >= 0) {
                // 存缓存监控是否上报0001
                String key = form.getVehicleId() + "_" + msgSN;
                adasDirectiveStatusOutTimeUtil.putVideoInspectionCache(key, data);
            } else {
                // 视频监控，存hbase，错误信息（终端未上线）
                data.setStatus(1);
                data.setFailReason(VideoInspectionData.OFF_LINE_MSG);
                data.setMonitorId(form.getVehicleId());
                adasDirectiveStatusOutTimeUtil.videoInspectionHandler(null, data);
            }
        }
    }

    private void videoInspect(String vehicleId, Integer channel) {
        // 需要展示的是物理通道号  根据车id和逻辑通道号 查物理通道号
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
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
        }
        BindDTO bindDTO = VehicleUtil.getBindInfoByRedis(vehicleId);
        if (bindDTO == null) {
            return new JsonResultBean(JsonResultBean.FAULT, "找不到监控对象");
        }
        userService.setObjectTypeName(bindDTO);
        String startTimeStr = DateUtil.getDateToString(new Date(), DateUtil.DATE_FORMAT);
        // 播放失败且原因是终端离线 需要后台查询所有通道号并记录数据;
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
            return new JsonResultBean(JsonResultBean.FAULT, "参数错误");
        }
        BindDTO bindDTO = MonitorUtils.getBindDTO(vehicleId, "monitorType");
        if (!Objects.equals(bindDTO.getMonitorType(), MonitorTypeEnum.VEHICLE.getType())) {
            return new JsonResultBean();
        }
        VehicleSpotCheckInfo vehicleSpotCheckInfo = new VehicleSpotCheckInfo();
        vehicleSpotCheckInfo.setVehicleId(vehicleId);
        //  限速, 使用报警设置中超速报警下的最高速度值
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
