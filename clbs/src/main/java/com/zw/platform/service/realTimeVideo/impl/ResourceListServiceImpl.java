package com.zw.platform.service.realTimeVideo.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zw.platform.basic.constant.RedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.oil.Positional;
import com.zw.platform.domain.realTimeVideo.FileUploadControl;
import com.zw.platform.domain.realTimeVideo.FileUploadControlForm;
import com.zw.platform.domain.realTimeVideo.FileUploadForm;
import com.zw.platform.domain.realTimeVideo.FtpBean;
import com.zw.platform.domain.realTimeVideo.ResourceListBean;
import com.zw.platform.domain.realTimeVideo.ResourceListBeanVO;
import com.zw.platform.domain.realTimeVideo.ResourceListSend;
import com.zw.platform.domain.realTimeVideo.SendFileUpload;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.domain.realTimeVideo.VideoFTPForm;
import com.zw.platform.domain.realTimeVideo.VideoFTPQuery;
import com.zw.platform.domain.realTimeVideo.VideoResourcesMonth;
import com.zw.platform.domain.realTimeVideo.VideoSendForm;
import com.zw.platform.domain.vas.history.HistoryStopData;
import com.zw.platform.push.controller.SubscibeInfo;
import com.zw.platform.push.controller.SubscibeInfoCache;
import com.zw.platform.push.handler.device.DeviceHelper;
import com.zw.platform.repository.realTimeVideo.VideoChannelSettingDao;
import com.zw.platform.repository.realTimeVideo.VideoFTPDao;
import com.zw.platform.repository.realTimeVideo.VideoSettingDao;
import com.zw.platform.repository.vas.AlarmSearchDao;
import com.zw.platform.service.monitoring.impl.HistoryServiceImpl;
import com.zw.platform.service.realTimeVideo.ResourceListService;
import com.zw.platform.service.realTimeVideo.VideoOrderSendService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.ConstantUtil;
import com.zw.platform.util.MsgUtil;
import com.zw.platform.util.common.AlarmTypeUtil;
import com.zw.platform.util.common.Converter;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.FtpClientUtil;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.ffmpeg.FFmpegCommandRunner;
import com.zw.platform.util.ffmpeg.FileUtils;
import com.zw.platform.util.ffmpeg.VideoFile;
import com.zw.protocol.msg.t808.T808Message;
import com.zw.protocol.netty.client.manager.WebSubscribeManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ResourceListServiceImpl implements ResourceListService {
    private static final Logger log = LogManager.getLogger(ResourceListServiceImpl.class);

    private static final String FILE_PATH = FFmpegCommandRunner.class.getResource("/").getPath()
        .substring(0, FFmpegCommandRunner.class.getResource("/").getPath().indexOf("WEB-INF/")) + "ftpVideo";

    private final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Value("${mediaServer.ip}")
    private String serverIp;// 实时视频服务器地址

    @Value("${mediaServer.port.rtp.resource}")
    private Integer tcpPort;// 实时视频TCP端口

    @Value("${mediaServer.port.udp}")
    private Integer udpPort;// 实时视频UDP端口

    @Value("${adas.mediaServer}")
    private String mediaServer;

    @Value("${system.ssl.enable}")
    private boolean sslEnabled;

    @Autowired
    HistoryServiceImpl historyService;

    @Autowired
    AlarmSearchDao alarmSearchDao;

    @Autowired
    private VideoOrderSendService videoOrderSendService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private VideoFTPDao videoFTPDao;

    @Autowired
    VideoSettingDao videoSettingDao;

    @Autowired
    VideoChannelSettingDao videoChannelSettingDao;

    @Value("${ftp.realTimeVideos}")
    private String ftpRealTimeVideos;

    @Value("${ftp.host}")
    private String ftpHost;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.host.clbs}")
    private String ftpHostClbs;

    @Value("${ftp.port.clbs}")
    private int ftpPortClbs;

    @Value("${ftp.showPassword}")
    private String ftpShowPassword;

    @Value("${terminal.off.line}")
    private String terminalOffLine; // 终端离线

    /* 存储Ftp的配置 */
    private static volatile Map<String, FtpBean> ftpConfigs;

    /**
     * 获取资源列表数据，区分FTP与终端
     */
    @Override
    public JsonResultBean sendResourceList(ResourceListBean resourceListBean, String ipAddress) {
        JSONObject msg = new JSONObject();
        // 通过车辆ID获取车辆基本信息
        String vehicleId = resourceListBean.getVehicleId();
        Map<String, String> vehicleMap = RedisHelper
            .getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "deviceId", "deviceNumber", "simCardNumber",
                "deviceType", "name");
        String brand = vehicleMap.get("name");
        String deviceNumber = vehicleMap.get("deviceNumber");
        String mobile = vehicleMap.get("simCardNumber");
        // type=0 为终端获取资源
        if ("0".equals(resourceListBean.getType())) {
            Integer msgSN = sendMsg(resourceListBean, true);
            if (msgSN == null) { // 未注册
                return new JsonResultBean(JsonResultBean.FAULT, "终端已离线！");
            }
            msg.put("type", "0");
            msg.put("msgSN", msgSN);
        } else { // FTP 获取资源
            initFtpConfig();
            ResourceListBeanVO resourceListBeanVO = getForFtp(resourceListBean);
            msg.put("ftpResource", resourceListBeanVO);
            msg.put("type", "1");
        }
        msg.put("brand", brand);
        msg.put("deviceNumber", deviceNumber);
        msg.put("mobile", mobile);
        addResourceListLog(ipAddress, vehicleMap);
        return new JsonResultBean(msg);
    }

    /**
     * 记录日志功能
     */
    private void addResourceListLog(String ipAddress, Map<String, String> vehicleInfo) {
        StringBuilder logMessage = new StringBuilder();// 日志语句
        String brand = vehicleInfo.get("name");
        String color = vehicleInfo.get("plateColor");
        logMessage.append("监控对象(").append(brand).append(") 查询资源列表");
        logSearchService.addLog(ipAddress, logMessage.toString(), "3", "", brand, color);
    }

    /**
     * 下发9205参数获取资源列表
     * @param isSub 是否订阅 809过检时传入flase
     */
    @Override
    public Integer sendMsg(ResourceListBean resourceListBean, boolean isSub) {
        // 序列号
        // 获取下发基本信息，终端ID、终端编号、sim卡号等信息
        String vehicleId = resourceListBean.getVehicleId();
        Map<String, String> configInfo = RedisHelper
            .getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "deviceId", "deviceNumber", "simCardNumber",
                "deviceType", "name");
        String deviceId = configInfo.get("deviceId"); // 终端ID
        String deviceNumber = configInfo.get("deviceNumber"); // 终端编号
        String mobile = configInfo.get("simCardNumber"); // sim卡号
        // 组装下发参数所需要的实体
        ResourceListSend send = new ResourceListSend();
        send.setChannelNum(Integer.valueOf(resourceListBean.getChannlNumer())); // 通道号
        if (StringUtils.isNotBlank(resourceListBean.getAlarmType())) {
            send.setAlarm(Long.parseLong(resourceListBean.getAlarmType()));// 报警类型
        } else {
            send.setAlarm(0);
        }
        String startTime = dateAnalyze(resourceListBean.getStartTime());
        String endTime = dateAnalyze(resourceListBean.getEndTime());
        send.setStartTime(startTime);// 开始时间
        send.setEndTime(endTime);// 结束时间
        /* 码流 0:所有码流，1：主码流，2，子码流 */
        send.setStreamType(resourceListBean.getStreamType());
        /* 音视频资源类型 0:音视频，1：音频，2：视频，3：音频或视频 */
        send.setVideoType(Integer.valueOf(resourceListBean.getVideoType()));
        /* 存储类型 0：所有存储器，1：主存储器，2：灾备存储器 */
        send.setStorageType(resourceListBean.getStorageType());
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // 设备已经注册
            // 订阅推送消息
            if (isSub) {
                SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
                    ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK);
                SubscibeInfoCache.getInstance().putTable(info);
            }
            T808Message message =
                MsgUtil.get808Message(mobile, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP, msgSN, send, configInfo);
            WebSubscribeManager.getInstance()
                .sendMsgToAll(message, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP, deviceId);
        }
        return msgSN;
    }

    /**
     * 获取FTP配置名
     */
    @Override
    public FtpBean getFtpName() {
        FtpBean ftpBean = getFtpBean();
        //传给视频回放页面,文件上传展示的密码,如果用户没有修改,就用ftp服务器的密码下发;如果用户修改了,就用用户修改的密码下发
        ftpBean.setPassword(ftpShowPassword);
        return ftpBean;
    }

    /**
     * 获得ftp信息
     */
    private FtpBean getFtpBean() {
        FtpBean ftpBean = new FtpBean();
        Map<String, FtpBean> ftp = ftpConfigs;
        List<String> ftpName = new ArrayList<>(ftp.keySet());
        BeanUtils.copyProperties(ftp.get(ftpName.get(0)), ftpBean);
        return ftpBean;
    }

    /**
     * 解析前端时间为YYMMDDHHmmSS，做下发功能
     */
    private String dateAnalyze(String date) {
        String[] list = date.split(" ");
        String[] year = list[0].split("-");
        String[] hour = list[1].split(":");
        String time = year[0] + year[1] + year[2] + hour[0] + hour[1] + hour[2];
        date = time.substring(2);
        return date;
    }

    /**
     * 视频文件下载
     */
    @Override
    public boolean fileDownload(String ftpName, String downloadPath, String directory, String destFileName) {
        initFtpConfig();
        Map<String, FtpBean> ftp = ftpConfigs;
        FtpBean ftpBean = ftp.get(ftpName);

        final Pair<String, Integer> ipPort = this.preferLanAccess(ftpBean.getHost(), ftpBean.getPort());

        try {
            FtpClientUtil.download(ipPort.getFirst(), ftpBean.getUsername(), ipPort.getSecond(), ftpBean.getPassword(),
                    directory, downloadPath, destFileName, destFileName);
            return true;
        } catch (Exception e) {
            log.error("文件下载异常" + e);
            return false;
        }
    }

    /**
     * 报警树组装
     */
    @Override
    public JSONArray getAlarm808() {
        Map<String, String> map = AlarmTypeUtil.alarmMap;
        JSONArray array = new JSONArray();
        int k = 125;
        JSONObject objf = new JSONObject();
        objf.put("id", "father");
        objf.put("name", "全部");
        objf.put("iconSkin", "groupSkin");
        objf.put("type", "100");
        objf.put("alarmType", "all");
        objf.put("open", true);
        objf.put("isParent", true);
        array.add(objf);
        for (String key : map.keySet()) {
            JSONObject obj = new JSONObject();
            obj.put("id", k++);
            obj.put("name", map.get(key));
            obj.put("iconSkin", "groupSkin");
            obj.put("type", key);
            obj.put("alarmType", "video");
            obj.put("open", true);
            obj.put("pId", "father");
            array.add(obj);
        }
        return array;
    }

    /**
     * 获取轨迹数据
     */
    @Override
    public String getHistory(String vehicleId, String startTime, String endTime) throws Exception {
        List<Positional> positionals;
        positionals = historyService.getHistory(vehicleId, startTime, endTime);
        if (positionals != null && !positionals.isEmpty()) {
            Positional positional;
            JSONArray resultful = new JSONArray();
            JSONObject msg = new JSONObject();
            HistoryStopData stopData = null;
            double longitudeOld = 0.0;
            double latitudeOld = 0.0;
            // 遍历集合,获取每一条历史轨迹

            for (int i = 0, n = positionals.size(); i < n; i++) {
                positional = positionals.get(i);
                // 如果速度为0
                if (positional.getLongtitude() == null || positional.getLatitude() == null) {
                    continue;

                }
                double longitude = Double.parseDouble(positional.getLongtitude());
                double latitude = Double.parseDouble(positional.getLatitude());
                if ((("0".equals(positional.getSpeed()) || "0.0".equals(positional.getSpeed())) && i != 0
                    && i != positionals.size() - 1) || (Math.abs(longitudeOld - longitude) < 0.00015
                    && Math.abs(latitudeOld - latitude) < 0.00015)) {
                    // 如果停车数据为空
                    if (stopData == null) {
                        stopData = new HistoryStopData();
                        // 设置轨迹
                        stopData.setPositional(positional);
                        // 设置开始时间
                        stopData.setStartTime(Converter.timeStamp2Date(String.valueOf(positional.getVtime()), null));
                    }
                    // 如果是最后一条数据
                    if (i == n - 1) {
                        // 设置结束时间
                        stopData.setEndTime(
                            Converter.timeStamp2Date(String.valueOf(positionals.get(i).getVtime()), null));
                        // 设置停车时间
                        stopData.setStopTime(dataFormat.parse(stopData.getEndTime()).getTime() - dataFormat
                            .parse(stopData.getStartTime()).getTime());
                        stopData = null;
                    }
                } else {
                    // 如果有停车记录
                    if (stopData != null) {
                        stopData.setEndTime(
                            Converter.timeStamp2Date(String.valueOf(positionals.get(i - 1).getVtime()), null));
                        stopData.setStopTime(dataFormat.parse(stopData.getEndTime()).getTime() - dataFormat
                            .parse(stopData.getStartTime()).getTime());
                        stopData = null;
                    }
                    longitudeOld = longitude;
                    latitudeOld = latitude;
                    resultful.add(positional);
                }
            }

            msg.put("resultful", resultful);// 多条轨迹
            String msgResult = JSON.toJSONString(msg, SerializerFeature.DisableCircularReferenceDetect);
            // 压缩数据
            msgResult = ZipUtil.compress(msgResult);
            return msgResult;
        } else {
            // 如果历史轨迹为空,则返回false
            return null;
        }

    }

    /**
     * 根据时间戳获取当前年月日
     */
    public String getTime(String time) {
        String str = "";
        if (time == null || time.isEmpty()) {
            return "";
        }
        try {
            long seconds = Long.parseLong(time);
            if (seconds != 0) {
                long msl = seconds * 1000;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                str = sdf.format(msl);
            }
        } catch (Exception e) {
            log.error("时间戳转换异常" + e + time);
        }
        return str;
    }

    /**
     * 根据当前年月转换成时间戳
     */
    public String setTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return String.valueOf(sdf.parse(time).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 初始化FTP配置
     */
    @PostConstruct
    private void initFtpConfig() {
        if (ftpConfigs == null) {
            synchronized (this) {
                if (ftpConfigs == null) {
                    ftpConfigs = Maps.newHashMap();
                    String[] configs = ftpRealTimeVideos.split(",");
                    String[] params;
                    FtpBean ftpBean;
                    for (String config : configs) {
                        params = config.split(":");
                        if (params.length != 6) {
                            throw new RuntimeException(
                                ">============实时视频的ftp配置错误,请到application.properties中进行配置==========<");
                        }
                        ftpBean = new FtpBean();
                        ftpBean.setFtpName(params[0]);// ftp名称
                        ftpBean.setUsername(params[1]);// 用户名
                        ftpBean.setPassword(params[2]);// 密码
                        ftpBean.setHost(params[3]);// IP
                        ftpBean.setPort(Integer.parseInt(params[4]));// 端口
                        ftpBean.setPath(params[5]);// 路径地址
                        ftpConfigs.put(params[0], ftpBean);
                    }
                }
            }
        }
    }

    /**
     * 用搜索条件查询FTP，解析FTP
     */
    @Override
    public ResourceListBeanVO getForFtp(ResourceListBean resourceListBean) {

        ResourceListBeanVO resourceListBeanVO = new ResourceListBeanVO();
        List<VideoFTPQuery> lists = Lists.newLinkedList();
        // 解析查询条件:开始时间、结束时间、车辆ID、通道号
        Integer channlNumber = Integer.valueOf(resourceListBean.getChannlNumer());
        Date startTime;
        Date endTime;
        try {
            startTime = dataFormat.parse(resourceListBean.getStartTime());
            endTime = dataFormat.parse(resourceListBean.getEndTime());
        } catch (ParseException e) {
            throw new RuntimeException("日期格式错误");
        }
        String vehicleId = resourceListBean.getVehicleId();
        // 获取FTP连接信息 ip、密码、端口、登录名等信息
        FTPClient ftp = getFTPClient(resourceListBean.getFtpName());
        // 查询FTP表，获取FTP服务器下的文件路径，字段为URL
        Set<String> calendarSet = Sets.newHashSet();
        List<VideoFTPQuery> list = videoFTPDao.getFtpList(vehicleId, channlNumber, startTime, endTime);
        if (ftp != null) {
            if (sslEnabled) {
                mediaServer = "/mediaserver";
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
            long queryAlarmType = Long.parseLong(resourceListBean.getAlarmType());
            for (VideoFTPQuery query : list) {
                String time = simpleDateFormat.format(query.getStartTime());
                calendarSet.add(time);// 存入日期集合，返回前端日历插件展示
                // 获取FTP文件路径 url
                String fileName = query.getUrl();
                if (fileName != null) {
                    // 转码后的文件不存在时，不再忽略（continue），目的是确保原始文件能在列表展示并可供下载
                    try {
                        // 解析路径
                        int index = fileName.lastIndexOf("/");
                        String fileNameFtp = fileName.substring(index + 1);
                        query.setName(fileNameFtp);
                        // queryAlarmType报警类型判断，用做数据过滤
                        if (queryAlarmType == 0) {
                            // 通过URL读取问题 如果存在则组装数据，如果不存在跳过此次循环
                            FTPFile[] files = ftp.listFiles(fileName);
                            if (files.length > 0) {
                                query.setFileSize(FileUtils.bytes2Mb(files[0].getSize())); // 获取文件大小
                            }
                        } else {
                            // 用户使用高级查询，带报警类型，报警类型匹配。匹配成功组装数据，失败跳出此次循环。
                            if ((queryAlarmType & query.getAlarmType()) > 0) {
                                FTPFile[] files = ftp.listFiles(fileName);
                                if (files.length > 0) {
                                    query.setFileSize(FileUtils.bytes2Mb(files[0].getSize()));// 获取文件大小
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("FTP视频资源获取文件大小错误,原因：" + fileName + "无法找到指定路径");
                    }
                }
                query.setDownUrl(query.getUrl() == null ? null : mediaServer + query.getUrl());
                query.setTempUrl(query.getTempUrl() == null ? null : mediaServer + query.getTempUrl());
                lists.add(query);
            }
        }
        resourceListBeanVO.setResourceList(lists);
        resourceListBeanVO.setCalendarSet(calendarSet);
        return resourceListBeanVO;
    }

    /**
     * 按名字获取ftp
     */
    private FTPClient getFTPClient(String ftpName) {
        FtpBean ftpBean = ftpConfigs.get(ftpName);
        final Pair<String, Integer> ipPort = this.preferLanAccess(ftpBean.getHost(), ftpBean.getPort());
        // 与FTP建立连接，为了通过URL读取路径下的文件
        return FtpClientUtil.getFTPClient(
                ftpBean.getUsername(), ftpBean.getPassword(), ipPort.getFirst(), ipPort.getSecond(), ftpBean.getPath());
    }

    /**
     * 优先使用lan地址访问ftp
     */
    private Pair<String, Integer> preferLanAccess(String host, int port) {
        return ftpHost.equals(host) && ftpPort == port
                ? Pair.of(ftpHostClbs, ftpPortClbs)
                : Pair.of(host, port);
    }

    /**
     * 下发文件上传指令
     * @param form      文件上传实体
     * @param ipAddress 客户端的IP地址
     */
    @Override
    public JsonResultBean sendUploadOrder(FileUploadForm form, String ipAddress) {
        Integer msgSN = 0;
        if (!form.getVehicleId().isEmpty()) {
            // 从缓存中取监控对象绑定的信息
            Map<String, String> vehicleConfigInfo = RedisHelper
                .getHashMap(RedisKeyEnum.MONITOR_INFO.of(form.getVehicleId()), "deviceId", "deviceNumber",
                    "simCardNumber", "deviceType", "name", "plateColor");
            if (vehicleConfigInfo != null) {
                // 获取下发基本信息，终端ID、终端编号、sim卡号等信息
                String brand = vehicleConfigInfo.get("name"); // 车牌号
                String plateColor = "";
                if (vehicleConfigInfo.get("plateColor") != null) {
                    plateColor = vehicleConfigInfo.get("plateColor");
                }
                String mobile = vehicleConfigInfo.get("simCardNumber"); // SIM卡编号
                String deviceId = vehicleConfigInfo.get("deviceId"); // 终端id
                SendFileUpload sendFileUpload = new SendFileUpload();
                String tempUrl = "/video_temp/" + form.getVehicleId() + "/" + DateUtil
                    .formatDate(new Date(), DateUtil.DATE_YYMMDDHHMMSS);
                form.setTempUrl(tempUrl);
                if (!form.getFTPServerIp().isEmpty()) { // FTP服务器IP地址
                    sendFileUpload.setFtpUrl(form.getFTPServerIp());
                }
                if (!form.getFTPUserName().isEmpty()) { // FTP服务器用户名
                    sendFileUpload.setUserName(form.getFTPUserName());
                }
                String ftPassword = form.getFTPassword();
                if (!ftPassword.isEmpty()) { // 密码
                    //前端使用了base64加密,需要解密
                    ftPassword = new String(Base64.getDecoder().decode(ftPassword), StandardCharsets.UTF_8);
                    //如果用户提交的密码是视频回放页面,文件上传用于展示的密码,就用ftp服务器的密码下发;如果用户修改了,就用用户修改的密码下发
                    if (ftpShowPassword.equals(ftPassword)) {
                        FtpBean ftpBean = getFtpBean();
                        ftPassword = ftpBean.getPassword();
                    }
                    sendFileUpload.setPwd(ftPassword);
                }
                if (!form.getFileUploadPath().isEmpty()) { // 文件上传路劲
                    sendFileUpload.setFilePath(tempUrl);
                }
                if (!form.getStartTime().isEmpty()) { // 开始时间
                    sendFileUpload.setStartTime(form.getStartTime());
                }
                if (!form.getEndTime().isEmpty()) { // 结束时间
                    sendFileUpload.setEndTime(form.getEndTime());
                }
                sendFileUpload.setPort(form.getFTPort()); // 端口号
                sendFileUpload.setType(form.getChannelNumber()); // 默认传逻辑通道号
                sendFileUpload.setMediaType(form.getResourceType()); // 资源类型
                sendFileUpload.setBitstream(form.getStreamType()); // 码流类型
                sendFileUpload.setStorageAddress(form.getStorageAddress()); // 储存位置
                sendFileUpload.setDuty(form.getExecuteOn()); // 执行条件
                byte[] a = new byte[0];
                sendFileUpload.setKeep(a);
                sendFileUpload.setChannelNumber(form.getChannelNumber()); // 逻辑通道号
                sendFileUpload.setAlarm(form.getAlarmSign()); // 报警标识

                if (!deviceId.isEmpty() && !mobile.isEmpty()) {
                    msgSN = DeviceHelper.serialNumber(form.getVehicleId());
                    // 流水号+终端id：文件大小
                    SubscibeInfoCache.getInstance().pushSubscibeMsgMap(msgSN, deviceId, form);
                    // ftp创建文件夹路径
                    createFTPUrl(FtpClientUtil.FTP_NAME, tempUrl);
                    VideoSendForm videoSendForm = new VideoSendForm();
                    videoSendForm.setVehicleId(form.getVehicleId());
                    videoSendForm.setDeviceId(deviceId);
                    videoSendForm.setSimNumber(mobile);
                    String deviceType = vehicleConfigInfo.get("deviceType");
                    videoOrderSendService.sendFileUpload(videoSendForm, sendFileUpload, deviceType, msgSN);
                    String message = "监控对象(" + brand + ") 文件上传";
                    logSearchService.addLog(ipAddress, message, "3", "", brand, plateColor);
                }
            }
        }
        if (msgSN != 0) {
            JSONObject msg = new JSONObject();
            String userName = SystemHelper.getCurrentUsername();
            msg.put("msgId", msgSN);
            msg.put("userName", userName);
            return new JsonResultBean(msg);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
        }
    }

    /**
     * 下发文件上传控制指令(暂停,继续,取消)
     * @param form      文本上传控制指令实体
     * @param ipAddress 客户端IP地址
     */
    @Override
    public JsonResultBean sendControlOrder(FileUploadControlForm form, String ipAddress) {
        Integer msgSN = 0;
        if (!form.getVehicleId().isEmpty()) {
            // 从缓存中取监控对象绑定的信息
            Map<String, String> vehicleConfigInfo = RedisHelper
                .getHashMap(RedisKeyEnum.MONITOR_INFO.of(form.getVehicleId()), "deviceId", "deviceNumber",
                    "simCardNumber", "deviceType", "name", "plateColor");
            if (vehicleConfigInfo != null) {
                String brand = vehicleConfigInfo.get("name"); // 车牌号
                String plateColor = vehicleConfigInfo.get("plateColor");
                String mobile = vehicleConfigInfo.get("simCardNumber"); // SIM卡编号
                String deviceId = vehicleConfigInfo.get("deviceId"); // 终端id
                FileUploadControl fileUploadControl = new FileUploadControl();
                fileUploadControl.setMsgSn(form.getMsgSn()); // 文件上传指令下发后的流水号
                fileUploadControl.setControl(form.getControl()); // 控制指令(0：暂停 1：继续 2：取消)

                if (!mobile.isEmpty() && !deviceId.isEmpty()) {
                    VideoSendForm videoSendForm = new VideoSendForm();
                    videoSendForm.setVehicleId(form.getVehicleId());
                    videoSendForm.setDeviceId(deviceId);
                    videoSendForm.setSimNumber(mobile);
                    String deviceType = vehicleConfigInfo.get("deviceType");
                    msgSN = videoOrderSendService.sendFileUploadControl(videoSendForm, fileUploadControl, deviceType);
                    String message = "";
                    switch (form.getControl()) {
                        case 0:
                            message = "监控对象(" + brand + ") 文件上传控制（暂停）";
                            break;
                        case 1:
                            message = "监控对象(" + brand + ") 文件上传控制（继续）";
                            break;
                        case 2:
                            message = "监控对象(" + brand + ") 文件上传控制（取消）";
                            break;
                        default:
                            break;
                    }
                    if (!message.isEmpty()) {
                        logSearchService.addLog(ipAddress, message, "3", "", brand, plateColor);
                    }
                }
            }
        }
        if (msgSN != null && msgSN != 0) {
            JSONObject msg = new JSONObject();
            msg.put("msgId", msgSN);
            return new JsonResultBean(JsonResultBean.SUCCESS, msg.toJSONString());
        } else {
            return new JsonResultBean(JsonResultBean.FAULT, terminalOffLine);
        }
    }

    @Override
    public String getFTPUrl(String vid, String startTime, Integer channelNum, Long alarmType) {
        String url = "";
        if (StringUtils.isNotBlank(vid) && StringUtils.isNotBlank(startTime) && channelNum != null
            && alarmType != null) {
            String year = startTime.substring(0, 4);
            String month = startTime.substring(4, 6);
            String day = startTime.substring(6, 8);
            url = "/f3_video/" + vid + "/" + year + "/" + month + "/" + day + "/" + channelNum + "/" + alarmType;
        }
        return url;
    }

    @Override
    public void insertFTPRecord(VideoFTPForm videoFTPForm) {
        if (videoFTPForm.check()) {
            // 判断该记录是否已存在
            List<VideoFTPQuery> ftpList = videoFTPDao
                .getFtpByUrl(videoFTPForm.getVehicleId(), videoFTPForm.getChannelNumber(), videoFTPForm.getUrl());
            if (ftpList == null || ftpList.isEmpty()) {
                // 存储ftp记录
                videoFTPDao.insert(videoFTPForm);
                log.info("ftp存储记录，视频来源：" + videoFTPForm.getType());
            }
        }

    }

    @Override
    public boolean createFTPUrl(String ftpName, String url) {
        FTPClient ftpClient = null;
        try {
            ftpClient = getFTPClient(ftpName, "/");
            return FtpClientUtil.createDir(url, ftpClient);
        } catch (Exception e) {
            log.error("文件夹创建异常：" + e);
            return false;
        } finally {
            try {
                if (ftpClient != null) {
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                log.error("关闭FTP连接发生异常！", e);
            }
        }
    }

    @Override
    public void motifyFtpFile(FTPClient ftpClient, String tempName, String tempUrl, String newFileName, String newUrl)
        throws Exception {
        // 判断是否需要转码
        if (StringUtils.isNotBlank(tempName)) {
            if (isNotMp4(tempName)) {
                changeFtpFile(tempUrl, tempName, ftpClient, newUrl, newFileName);
            } else {
                // 复制文件
                FtpClientUtil.copyFile(tempName, newFileName + ".mp4", tempUrl, newUrl, ftpClient);
            }
        }
    }

    private boolean isNotMp4(String tempName) {
        return !".mp4".equalsIgnoreCase(tempName.substring(tempName.lastIndexOf(".")));
    }

    @Override
    public FTPClient getFTPClient(String ftpName, String path) {
        initFtpConfig();
        Map<String, FtpBean> ftp = ftpConfigs;
        FtpBean ftpBean = ftp.get(ftpName);
        final Pair<String, Integer> ipPort = this.preferLanAccess(ftpBean.getHost(), ftpBean.getPort());
        try {
            return FtpClientUtil.getFTPClient(
                    ftpBean.getUsername(), ftpBean.getPassword(), ipPort.getFirst(), ipPort.getSecond(), path);
        } catch (Exception e) {
            log.error("ftpClient创建异常：" + e);
            return null;
        }
    }

    private void changeFtpFile(String tempUrl, String fileName, FTPClient ftpClient, String newUrl, String newFileName)
        throws Exception {
        File directory;
        String path;
        File file = null;
        File mp4File = null;

        //FTP的编码格式为ISO-8859-1, 文件名中可能存在中文, 需要转换编码
        fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        String oldUrl = tempUrl + "/" + fileName;
        try (InputStream in = ftpClient.retrieveFileStream(oldUrl)) {
            // 文件存在
            if (in != null) {
                directory = new File(FILE_PATH);
                // 执行创建
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
                log.info(">>======ftp服务器中未存在该文件======<<");
            }
        } catch (Exception e) {
            log.error("拷贝ftp文件失败");
            throw e;
        } finally {
            // 复用ftp,需要等待前面结束,且要等待前面的流关闭
            ftpClient.completePendingCommand();
        }
        // 执行转码
        if (file != null && file.exists()) {
            // 获得mp4文件
            VideoFile videoFile = FFmpegCommandRunner.copyToMp4(file);
            mp4File = videoFile.getTarget();
            // 删除临时文件
            file.delete();
        } else {
            log.error("ftp转码失败，源文件[{}]不存在", fileName);
        }
        if (mp4File != null && mp4File.length() > 0) {
            try (InputStream fi = new FileInputStream(mp4File)) {
                // 创建文件夹
                ftpClient.changeWorkingDirectory("/");
                FtpClientUtil.createDir(newUrl, ftpClient);
                // 存储文件
                ftpClient.storeFile(newFileName + ".mp4", fi);
                // 删除文件
                mp4File.delete();
            }
        } else {
            log.error("ftp转码失败，新文件不存在");
        }
    }

    @Override
    public String checkFtp() throws Exception {
        initFtpConfig();
        FTPClient ftp = getFTPClient("FTP服务器");
        // fs为ftpFilePath下所有文件集合
        FTPFile[] fs = ftp.listFiles();
        Date tempTime = null;
        // 遍历得到最近的一条记录
        String fileName = null;
        if (fs != null) {
            for (FTPFile ff : fs) {
                String fname = ff.getName();
                if (fname.indexOf(".") > 0 && ".avi".equals(fname.substring(fname.indexOf(".")))) {
                    Date createTime = ff.getTimestamp().getTime();
                    // createTime 不早于 tempTime
                    if (tempTime == null || createTime.compareTo(tempTime) >= 0) {
                        tempTime = createTime;
                        fileName = fname;
                    }
                }
            }
        }
        if (fileName != null) {
            File directory;
            String path;
            File file = null;
            File mp4File = null;
            FtpBean ftpBean = ftpConfigs.get("FTP服务器");
            String oldUrl = "/" + ftpBean.getPath() + "/" + fileName;
            String newFileName = fileName.substring(8);
            try (InputStream in = ftp.retrieveFileStream(oldUrl)) {
                // 文件存在
                if (in != null) {
                    directory = new File(FILE_PATH);
                    // 执行创建
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    path = directory.getAbsolutePath();
                    file = new File(path + File.separator + new String(newFileName.getBytes("iso8859-1")));
                    try (OutputStream out = new FileOutputStream(file)) {
                        byte[] buff = new byte[1024];
                        int rc;
                        while ((rc = in.read(buff)) > 0) {
                            out.write(buff, 0, rc);
                        }
                    }
                } else {
                    log.info(">>======ftp服务器中未存在该文件======<<");
                }
            }
            // 执行转码
            if (file != null && file.exists()) {
                // 获得mp4文件
                VideoFile videoFile = FFmpegCommandRunner.copyToMp4(file);
                mp4File = videoFile.getTarget();
                // 删除临时文件
                file.delete();
            } else {
                log.error("ftp转码失败");
            }
            return "ftpVideo/" + mp4File.getName();
        }
        return "";
    }

    @Override
    public JsonResultBean sendList(ResourceListBean resourceListBean, String ipAddress) {
        JSONObject msg = new JSONObject();
        // 通过车辆ID获取车辆基本信息
        String vehicleId = resourceListBean.getVehicleId();
        Map<String, String> vehicleMap = RedisHelper
            .getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "deviceId", "deviceNumber", "simCardNumber",
                "deviceType", "name", "plateColor");
        String brand = vehicleMap.get("name");
        String deviceNumber = vehicleMap.get("deviceNumber");
        String mobile = vehicleMap.get("simCardNumber");
        // type=0 为终端获取资源
        if ("0".equals(resourceListBean.getType())) {
            Integer msgSN = sendMsg920F(resourceListBean, true);
            if (msgSN == null) { // 未注册
                return new JsonResultBean(JsonResultBean.FAULT, "终端已离线！");
            }
            msg.put("type", "0");
            msg.put("msgSN", msgSN);
            addResourceDateListLog(ipAddress, vehicleId);
        } else { // FTP 获取资源
            initFtpConfig();
            ResourceListBeanVO resourceListBeanVO = getForFtp(resourceListBean);
            List<VideoFTPQuery> videoFTPQueries = resourceListBeanVO.getResourceList();
            Map<Integer, Integer> logicPhysicsChannelMap =
                videoChannelSettingDao.getVideoChannelByVehicleId(vehicleId).stream().collect(
                    Collectors.toMap(VideoChannelSetting::getLogicChannel, VideoChannelSetting::getPhysicsChannel));
            List<VideoFTPQuery> resourceList =
                videoFTPQueries.stream().filter(obj -> logicPhysicsChannelMap.containsKey(obj.getChannelNumber()))
                    .peek(obj -> obj.initChannelNumber(logicPhysicsChannelMap)).collect(Collectors.toList());
            resourceListBeanVO.setResourceList(resourceList);
            msg.put("ftpResource", resourceListBeanVO);
            msg.put("type", "1");
            addResourceListLog(ipAddress, vehicleId);
        }
        msg.put("brand", brand);
        msg.put("deviceNumber", deviceNumber);
        msg.put("mobile", mobile);
        return new JsonResultBean(msg);
    }

    /**
     * 下发920F参数获取资源日期
     * @param isSub 是否订阅 809过检时传入flase
     */
    private Integer sendMsg920F(ResourceListBean resourceListBean, boolean isSub) {
        // 序列号
        // 获取下发基本信息，终端ID、终端编号、sim卡号等信息
        String vehicleId = resourceListBean.getVehicleId();
        Map<String, String> vehicleMap = RedisHelper
            .getHashMap(RedisKeyEnum.MONITOR_INFO.of(vehicleId), "deviceId", "deviceNumber", "simCardNumber",
                "deviceType", "name", "plateColor");
        String deviceId = vehicleMap.get("deviceId"); // 终端ID
        String deviceNumber = vehicleMap.get("deviceNumber"); // 终端编号
        String mobile = vehicleMap.get("simCardNumber"); // sim卡号
        // 组装下发参数所需要的实体
        VideoResourcesMonth send = new VideoResourcesMonth();
        send.setType(Integer.valueOf(resourceListBean.getVideoType()));
        send.setDate(resourceListBean.getDate().replaceAll("-", ""));
        Integer msgSN = DeviceHelper.getRegisterDevice(vehicleId, deviceNumber);
        if (msgSN != null) { // 设备已经注册
            // 订阅推送消息
            if (isSub) {
                SubscibeInfo info = new SubscibeInfo(SystemHelper.getCurrentUsername(), deviceId, msgSN,
                    ConstantUtil.VIDEO_DOWN_EXG_MSG_RETURN_RESOURCE_ACK_BEFOR);
                SubscibeInfoCache.getInstance().putTable(info);
            }
            T808Message message = MsgUtil
                .get808Message(mobile, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP_BEFOR, msgSN, send, vehicleMap);
            WebSubscribeManager.getInstance()
                .sendMsgToAll(message, ConstantUtil.T809_DOWN_EXG_MSG_RETURN_STARTUP_BEFOR, deviceId);
        }
        return msgSN;
    }

    /**
     * 记录日志功能
     */
    private void addResourceListLog(String ipAddress, String vehicleId) {
        StringBuilder logMessage = new StringBuilder();// 日志语句
        String[] vehicle = logSearchService.findCarMsg(vehicleId);
        logMessage.append("监控对象(").append(vehicle[0]).append(") 查询资源列表");
        logSearchService.addLog(ipAddress, logMessage.toString(), "3", "", vehicle[0], vehicle[1]);
    }

    /**
     * 记录日志功能
     */
    private void addResourceDateListLog(String ipAddress, String vehicleId) {
        StringBuilder logMessage = new StringBuilder();// 日志语句
        String[] vehicle = logSearchService.findCarMsg(vehicleId);
        logMessage.append("监控对象(").append(vehicle[0]).append(") 查询资源日期");
        logSearchService.addLog(ipAddress, logMessage.toString(), "3", "", vehicle[0], vehicle[1]);
    }
}
