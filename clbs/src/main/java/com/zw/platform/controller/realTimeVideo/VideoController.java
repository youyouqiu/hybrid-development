package com.zw.platform.controller.realTimeVideo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.zw.platform.basic.constant.HistoryRedisKeyEnum;
import com.zw.platform.basic.core.RedisHelper;
import com.zw.platform.basic.service.GroupMonitorService;
import com.zw.platform.commons.Auth;
import com.zw.platform.commons.SystemHelper;
import com.zw.platform.domain.multimedia.form.MediaForm;
import com.zw.platform.domain.realTimeVideo.AudioParam;
import com.zw.platform.domain.realTimeVideo.AudioVideoRransmitForm;
import com.zw.platform.domain.realTimeVideo.DiskInfo;
import com.zw.platform.domain.realTimeVideo.VideoPlayResultDTO;
import com.zw.platform.domain.realTimeVideo.VideoSendForm;
import com.zw.platform.push.handler.common.WebSocketMessageDispatchCenter;
import com.zw.platform.service.realTimeVideo.ResourceListService;
import com.zw.platform.service.realTimeVideo.VideoService;
import com.zw.platform.service.reportManagement.LogSearchService;
import com.zw.platform.util.AudioVideoUtil;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.ZipUtil;
import com.zw.platform.util.ffmpeg.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;

/**
 * 实时视频Controller
 */
@Controller
@RequestMapping("/realTimeVideo/video")
public class VideoController {
    private static final Logger log = LogManager.getLogger(VideoController.class);

    private static final String LIST_PAGE = "vas/monitoring/videoRealTime/list";

    private static final String CLOUD_PAGE = "vas/monitoring/videoRealTime/cloud";

    private static final String TALKBACK_PAGE = "vas/monitoring/videoRealTime/talkBack";

    private static final String BROAD_CAST_PAGE = "vas/monitoring/videoRealTime/broadCast";

    private static final String CHANNEL_SETTING_PAGE = "vas/monitoring/videoRealTime/channelSetting";

    private static final String VEDIO_SLEEP_SETTING_PAGE = "vas/monitoring/videoRealTime/vedioSleepSetting";

    private static final String VEDIO_SLEEP_PAGE = "vas/monitoring/videoRealTime/vedioSleep";

    private static final String VIDEO_MODULE = "REALTIMEVIDEO";

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Value("${video.findlog.flag:true}")
    private boolean logFindFlag;

    @Autowired
    private VideoService videoService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GroupMonitorService groupMonitorService;

    @Autowired
    private LogSearchService logSearchService;

    @Autowired
    private ResourceListService resourceListService;

    @Autowired
    private WebSocketMessageDispatchCenter webSocketMessageDispatchCenter;

    /**
     * 实时视频界面
     * @return ModelAndView
     * @author hujun
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView getListPage() {
        ModelAndView modelAndView = new ModelAndView(LIST_PAGE);
        modelAndView.addObject("logFlag", logFindFlag);
        return modelAndView;
    }

    /**
     * 云台页面
     */
    @Auth
    @RequestMapping(value = { "/cloud" }, method = RequestMethod.GET)
    public ModelAndView getCloudPage() {
        return new ModelAndView(CLOUD_PAGE);
    }

    /**
     * 获取监控对象下的全部逻辑通道号
     * @param vehicleId 车辆id
     * @param isChecked 判断是订阅还是展开 true:订阅 false:展开
     * @author hujun
     */
    @RequestMapping(value = { "/getChannels" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getChannels(String vehicleId, boolean isChecked) {
        try {
            JSONArray result = videoService.getChannelsByVehicleId(vehicleId, isChecked);
            // 压缩数据
            String tree = ZipUtil.compress(result.toJSONString());
            return new JsonResultBean(tree);
        } catch (Exception e) {
            log.error("获取监控对象下的全部逻辑通道号异常", e);
            return null;
        }
    }

    /**
     * 批量获取监控对象下的全部逻辑通道号
     * @author hujun
     */
    @RequestMapping(value = { "/getAllChannels" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAllChannels(String vehicleId, boolean isChecked) {
        try {
            JSONArray result = videoService.getChannelsByVehicleIds(vehicleId, isChecked);
            // 压缩数据
            String tree = ZipUtil.compress(result.toJSONString());
            return new JsonResultBean(tree);
        } catch (Exception e) {
            log.error("批量获取监控对象下的全部逻辑通道号异常", e);
            return null;
        }
    }

    /**
     * 实时视频指令下发
     * @author hujun
     */
    @RequestMapping(value = { "/sendParamCommand" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendParamCommand(VideoSendForm form) {
        try {
            if (form != null) {
                // 获取客户端的IP地址
                String ipAddress = new GetIpAddr().getIpAddr(request);
                // 按照指令类型下发对应参数
                videoService.sendParam(form, ipAddress, null);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("实时视频指令下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 视频窗口右键指令下发0x9102
     * @param form 参数实体
     */
    @RequestMapping(value = { "/sendVideoParam" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean sendVideoControl(@ModelAttribute("form") final AudioVideoRransmitForm form) {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                form.setUserName(SystemHelper.getCurrentUsername());
                videoService.sendRealTimeControl(form, ipAddress);
                return new JsonResultBean(JsonResultBean.SUCCESS);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("实时视频,视频窗口右键音视频实时传输控制指令下发异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 右键指令对讲界面
     * @author hujun
     */
    @RequestMapping(value = { "/talkBackPage" }, method = RequestMethod.GET)
    public ModelAndView talkBackPage(String vehicleId, String brand, String mobile, String channelNumber,
        String vehicleColor, String channelNumType, String streamType) {
        ModelAndView ma = new ModelAndView(TALKBACK_PAGE);
        ma.addObject("vehicleId", vehicleId);
        ma.addObject("brand", brand);
        ma.addObject("mobile", mobile);
        ma.addObject("channelNumber", channelNumber);
        ma.addObject("vehicleColor", vehicleColor);
        ma.addObject("channelNumType", channelNumType);
        ma.addObject("streamType", streamType);
        return ma;
    }

    /**
     * 右键指令广播界面
     * @author hujun
     */
    @RequestMapping(value = { "/broadCastPage" }, method = RequestMethod.GET)
    public String broadCastPage() {
        return BROAD_CAST_PAGE;
    }

    /**
     * 右键指令通道号设置界面
     * @author hujun
     */
    @RequestMapping(value = { "/channelSettingPage" }, method = RequestMethod.GET)
    public ModelAndView channelSettingPage(String vehicleId, String brand) {
        ModelAndView ma = new ModelAndView(CHANNEL_SETTING_PAGE);
        ma.addObject("vehicleId", vehicleId);
        ma.addObject("brand", brand);
        ma.addObject("allChannels", JSON.toJSONString(AudioVideoUtil.getLogicChannels()));
        return ma;
    }

    /**
     * 右键指令休眠唤醒设置界面
     * @author hujun
     */
    @RequestMapping(value = { "/vedioSleepSettingPage" }, method = RequestMethod.GET)
    public ModelAndView vedioSleepSettingPage(String vehicleId, String brand) {
        ModelAndView ma = new ModelAndView(VEDIO_SLEEP_SETTING_PAGE);
        ma.addObject("vehicleId", vehicleId);
        ma.addObject("brand", brand);
        return ma;
    }

    /**
     * 右键指令休眠唤醒界面
     * @author hujun
     */
    @RequestMapping(value = { "/vedioSleepPage" }, method = RequestMethod.GET)
    public ModelAndView vedioSleepPage(String vehicleId, String brand) {
        ModelAndView ma = new ModelAndView(VEDIO_SLEEP_PAGE);
        ma.addObject("vehicleId", vehicleId);
        ma.addObject("brand", brand);
        return ma;
    }

    @RequestMapping(value = { "/getPidByVid" }, method = RequestMethod.POST)
    @ResponseBody
    public String getAssignmentIdByVid(String vehicleId) {
        List<String> groupIds = groupMonitorService.getUserOwnGroupByMonitorId(vehicleId);
        return groupIds.size() > 0 ? groupIds.get(0) : null;
    }

    @RequestMapping(value = { "/screenshot" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean screenshot(MediaForm media, MultipartFile file) {
        try {
            if (videoService.saveMedia(media, file)) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // 获取客户端的IP地址
                String[] vehicle = logSearchService.findCarMsg(media.getVehicleId());
                String message = "监控对象(" + vehicle[0] + ")_通道号" + media.getWayId() + "抓拍";
                logSearchService
                    .addLogByUserName(ipAddress, message, "3", VIDEO_MODULE, SystemHelper.getCurrentUsername(),
                        vehicle[0], vehicle[1]); // 单通道操作
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("抓图异常", e);
            return false;
        }
    }

    @RequestMapping(value = { "/getAudioParam" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAudioParam(String vehicleId) {
        try {
            AudioParam audio = videoService.getAudioParam(vehicleId);
            if (audio != null && audio.getAudioCode() != null) {
                return new JsonResultBean(audio);
            } else {
                return new JsonResultBean(JsonResultBean.FAULT);
            }
        } catch (Exception e) {
            log.error("获取音频参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 存日志
     */
    @RequestMapping(value = { "/saveLog" }, method = RequestMethod.POST)
    @ResponseBody
    public boolean saveLog(Integer type, String vehicleId, Integer channel) {
        try {
            // 存日志
            if (StringUtils.isNotBlank(vehicleId) && type != null && channel != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request); // 获取客户端的IP地址
                String str;
                if (type == 1) {
                    str = "字幕叠加";
                } else {
                    str = "字幕调整";
                }
                String[] vehicle = logSearchService.findCarMsg(vehicleId);
                String message = "监控对象(" + vehicle[0] + ")_通道号" + channel + str;
                logSearchService
                    .addLogByUserName(ipAddress, message, "3", VIDEO_MODULE, SystemHelper.getCurrentUsername(),
                        vehicle[0], vehicle[1]); // 单通道操作
            }
            return true;
        } catch (Exception e) {
            log.error("字幕叠加/字幕调整存日志异常", e);
            return false;
        }
    }

    /**
     * 文件下载
     */
    @RequestMapping("downLoadVideo")
    public void downLoadVideo(HttpServletResponse response, String path) {
        InputStream inStream = null;
        PrintWriter out = null;
        try {
            String adress = System.getProperty("clbs.root");
            String downloadPath = adress + "resourceVideo";
            String ftpName = "FTP服务器";
            int index = path.lastIndexOf("/");
            String directory = path.substring(0, index);
            String fileNameFtp = path.substring(index + 1);
            resourceListService.fileDownload(ftpName, downloadPath, directory, fileNameFtp);
            inStream = new FileInputStream(downloadPath + "/" + fileNameFtp);
            response
                .addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileNameFtp, "UTF-8"));
            FileUtils.writeFile(inStream, response.getOutputStream());
        } catch (Exception e) {
            log.error("实时视频,视频下载失败", e);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            try {
                out = response.getWriter();
                out.append(new JsonResultBean(false, "实时视频,视频下载失败").toString());
            } catch (IOException e1) {
                log.error("实时视频下载失败", e1);
            }
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(inStream);
        }
    }

    /**
     * 请求磁盘信息
     * @author wangying
     */
    @RequestMapping(value = { "/getDiskInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDiskInfo() {
        try {
            DiskInfo disk = videoService.getDiskInfo();
            return new JsonResultBean(disk);
        } catch (Exception e) {
            log.error("获取磁盘信息请求异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 保存磁盘信息
     * @author wangying
     */
    @RequestMapping(value = { "/saveDiskInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveDiskInfo(DiskInfo disk) {
        try {
            String diskStr = RedisHelper.getString(HistoryRedisKeyEnum.VIDEO_DISKINFO.of());
            if (StringUtils.isNotBlank(diskStr)) {
                DiskInfo lastDisk = JSON.parseObject(diskStr, DiskInfo.class);
                disk.setMemory(lastDisk.getMemory());
            }
            // 存储redis中
            RedisHelper.setString(HistoryRedisKeyEnum.VIDEO_DISKINFO.of(), JSON.toJSONString(disk));
            // 推送磁盘状态
            webSocketMessageDispatchCenter.pushFtpDiskToClient(JSON.toJSONString(disk));// 推送到web端
            return new JsonResultBean(JsonResultBean.SUCCESS);
        } catch (Exception e) {
            log.error("保存磁盘信息请求异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 保存视频巡检记录
     */
    @RequestMapping(value = "/saveVideoInspectionRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVideoInspectionRecord(VideoPlayResultDTO videoPlayResultDTO) {
        try {
            return videoService.saveVideoInspectionRecord(videoPlayResultDTO);
        } catch (Exception e) {
            log.error("保存视频巡检记录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 保存视频抽查统计记录
     */
    @RequestMapping(value = "/saveVideoSpotCheckRecord", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean saveVideoSpotCheckRecord(String vehicleId) {
        try {
            return videoService.saveVideoSpotCheckRecord(vehicleId);
        } catch (Exception e) {
            log.error("保存视频抽查统计记录异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }
}
