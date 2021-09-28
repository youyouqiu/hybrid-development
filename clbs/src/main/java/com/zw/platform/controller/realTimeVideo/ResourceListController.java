package com.zw.platform.controller.realTimeVideo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zw.platform.basic.service.MonitorIconService;
import com.zw.platform.domain.realTimeVideo.FileUploadControlForm;
import com.zw.platform.domain.realTimeVideo.FileUploadForm;
import com.zw.platform.domain.realTimeVideo.FtpBean;
import com.zw.platform.domain.realTimeVideo.ResourceListBean;
import com.zw.platform.service.realTimeVideo.ResourceListService;
import com.zw.platform.service.realTimeVideo.VideoChannelSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.FTPException;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * 资源列表
 * @author hujun
 */
@Controller
@RequestMapping("/realTimeVideo/resource")
public class ResourceListController {
    private static final String LIST_PAGE = "vas/monitoring/videoResource/list";

    private static final Logger log = LogManager.getLogger(ResourceListController.class);

    @Autowired
    private ResourceListService resourceListService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private VideoChannelSettingService videoChannelSettingService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Autowired
    private MonitorIconService monitorIconService;

    /**
     * 用于1078报批稿下发,记住通道号，资源类型，有效期5小时
     */
    public static final Cache<String, ResourceListBean> T808_2011_1078_CACHE =
        Caffeine.newBuilder().maximumSize(500).expireAfterWrite(18000, TimeUnit.SECONDS).build();

    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public ModelAndView listPage() {
        ModelAndView mav = new ModelAndView(LIST_PAGE);
        try {
            FtpBean ftp = resourceListService.getFtpName();
            String name = "FTP服务器";
            if (ftp != null) {
                name = ftp.getFtpName();
            }
            mav.addObject("ftp", name);
        } catch (Exception e) {
            log.error("查询视频报警类型异常", e);
        }
        return mav;
    }

    /**
     * 查询资源列表，
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getList(ResourceListBean resourceListBean, HttpServletRequest request) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            String filePath = request.getServletContext().getRealPath("/") + "resourceVideo";
            resourceListBean.setIp(ip);
            resourceListBean.setDownloadPath(filePath);
            return resourceListService.sendResourceList(resourceListBean, ip);
        } catch (FTPException e) {
            log.error("获取资源列表轨迹异常, {}", resourceListBean.getBrand(), e);
            return new JsonResultBean(JsonResultBean.FAULT, e.getMessage());
        } catch (Exception e) {
            log.error("获取资源列表轨迹异常, {}", resourceListBean.getBrand(), e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询FTP服务器资源列表，终端资源日期 920F
     */
    @RequestMapping(value = "/getResource", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getDateList(ResourceListBean resourceListBean, HttpServletRequest request) {
        try {
            String ip = new GetIpAddr().getIpAddr(request);// 获得访问ip
            String filePath = request.getServletContext().getRealPath("/") + "resourceVideo";
            resourceListBean.setIp(ip);
            resourceListBean.setDownloadPath(filePath);
            return resourceListService.sendList(resourceListBean, ip);
        } catch (Exception e) {
            log.error("获取资源列表轨迹异常:{}", resourceListBean.getBrand(), e);
            return null;
        }
    }

    /**
     * 获取历史轨迹数据
     */
    @RequestMapping(value = "/getHistory", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getHistory(String vehicleId, String startTime, String endTime) {
        try {
            String data = resourceListService.getHistory(vehicleId, startTime, endTime);
            return new JsonResultBean(data);
        } catch (Exception e) {
            log.error("获取资源列表轨迹异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 报警树组装
     */
    @RequestMapping(value = "/alarmTree", method = RequestMethod.POST)
    @ResponseBody
    public String getAlarmType() {
        try {
            JSONArray alarm = resourceListService.getAlarm808();
            return alarm.toJSONString();
        } catch (Exception e) {
            log.error("获取报警类型异常");
        }
        return null;
    }

    /**
     * 下发文件上传指令
     * @param form 文件上传指令实体
     */
    @RequestMapping(value = "/upLoad", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean fileUploadOrder(@ModelAttribute("form") FileUploadForm form) {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);

                return resourceListService.sendUploadOrder(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("实时视频-资源列表下发上传文件指令异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }

    }

    /**
     * 下发文件上传控制指令
     * @param form 参数实体
     */
    @RequestMapping(value = "/control", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean fileUploadControl(@ModelAttribute("form") FileUploadControlForm form) {
        try {
            if (form != null) {
                String ipAddress = new GetIpAddr().getIpAddr(request);
                return resourceListService.sendControlOrder(form, ipAddress);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("下发文件上传控制指令异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 文件下载功能
     * @return JsonResultBean
     */
    @RequestMapping(value = "/fileDownload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean fileDownload(String fileName, String ftpName) {
        // 文件下载后视频存放根路径地址获取
        String adress = System.getProperty("clbs.root");
        String downloadPath = adress + "resourceVideo";
        File file = new File(downloadPath);
        // 文件url地址解析
        int index = fileName.lastIndexOf("/");
        String directory = fileName.substring(0, index);
        String fileNameFtp = fileName.substring(index + 1);
        JSONObject msg = new JSONObject();
        msg.put("fileName", fileNameFtp);
        // 判断是否存在该文件夹，不存在则创建
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("创建文件夹异常" + downloadPath);
        }
        String[] fileList = file.list();
        // 循环判断resourceVide文件下是否存在该视频，存在则不用执行后面的下载功能
        if (ArrayUtils.isNotEmpty(fileList)) {
            for (String s : fileList) {
                if (fileNameFtp.equals(s)) {
                    return new JsonResultBean(msg);
                }
            }
        }
        // 根据文件路径，从FTP下载该视频
        boolean flag = resourceListService.fileDownload(ftpName, downloadPath, directory, fileNameFtp);
        if (flag) {
            return new JsonResultBean(msg);
        } else {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 文件下载功能
     * @return JsonResultBean
     * @author wangjianyu
     */
    @RequestMapping(value = "/fileDownloads", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean fileDownloads(String fileName, String ftpName) {

        try {
            // 文件下载后视频存放根路径地址获取
            String adress = System.getProperty("clbs.root");
            String downloadPath = adress + "resourceVideo";
            File file = new File(downloadPath);
            if (!file.exists() && !file.mkdirs()) {
                throw new RuntimeException("创建文件夹异常" + downloadPath);
            }
            String[] fileNames = fileName.split(",");
            JSONObject msg = new JSONObject();
            StringBuilder str = new StringBuilder();
            // 文件url地址解析
            for (String name : fileNames) {
                int index = name.lastIndexOf("/");
                String directory = name.substring(0, index);
                String fileNameFtp = name.substring(index + 1);
                str.append(fileNameFtp).append(",");
                String[] fileList = file.list();
                if (fileList == null) {
                    continue;
                }
                boolean flag = true;
                // 循环判断resourceVide文件下是否存在该视频，存在则不用执行后面的下载功能
                for (String s : fileList) {
                    if (fileNameFtp.equals(s)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    // 根据文件路径，从FTP下载该视频
                    resourceListService.fileDownload(ftpName, downloadPath, directory, fileNameFtp);
                }
            }
            msg.put("fileNames", str.toString());
            return new JsonResultBean(msg);
        } catch (Exception e) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }

    }

    /**
     * 通道号获取
     */
    @RequestMapping(value = "/getVideoChannel", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVideoChannel(String vehicleId) {
        try {
            return videoChannelSettingService.getVideoChannel(vehicleId);
        } catch (Exception e) {
            log.error("获取通道号异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 通道号获取
     */
    @RequestMapping(value = "/getIco", method = RequestMethod.POST)
    @ResponseBody
    public String getIco(String vehicleId) {
        try {
            if (StringUtils.isNotBlank(vehicleId)) {
                return monitorIconService.getMonitorIcon(vehicleId);
            }
            return "";
        } catch (Exception e) {
            log.error("获取图标异常", e);
            return null;
        }
    }

    /**
     * 文件上传路径及FTP信息获取
     */
    @RequestMapping(value = "/getFtpMsg", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFtpMsg(String vehicleId, String startTime, String channelNumber, String alarm) {
        try {
            String fileUrl = resourceListService
                .getFTPUrl(vehicleId, "20" + startTime, Integer.valueOf(channelNumber), Long.valueOf(alarm));
            FtpBean ftpBean = resourceListService.getFtpName();
            JSONObject msg = new JSONObject();
            msg.put("fileUrl", fileUrl);
            msg.put("ftpBean", ftpBean);
            return new JsonResultBean(msg);
        } catch (Exception e) {
            log.error("获取FTP路径及信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 文件下载推流到浏览器
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public HttpServletResponse download(String path, HttpServletResponse response) {
        String adress = System.getProperty("clbs.root");
        path = adress + "resourceVideo/" + path;
        try (InputStream fis = new BufferedInputStream(new FileInputStream(path));
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream())) {
            // path是指欲下载的文件的路径。
            File file = new File(path);
            // 取得文件名。
            String filename = file.getName();

            // 清空response
            response.reset();
            // 设置response的Header
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            response.setContentType("application/octet-stream");
            // 以流的形式下载文件。
            byte[] buffer = new byte[1000];
            while (fis.read(buffer) > 0) {
                toClient.write(buffer);
            }
            toClient.flush();
        } catch (Exception ex) {
            log.error("下载视频资源文件失败", ex);
        }
        return response;
    }

}
