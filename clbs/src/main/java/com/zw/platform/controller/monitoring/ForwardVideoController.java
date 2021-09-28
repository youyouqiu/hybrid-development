package com.zw.platform.controller.monitoring;

import com.zw.platform.basic.core.ConfigHelper;
import com.zw.platform.domain.realTimeVideo.FileUploadForm;
import com.zw.platform.service.monitoring.ForwardVideoService;
import com.zw.platform.service.realTimeVideo.VideoService;
import com.zw.platform.util.IPAddrUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Controller
@RequestMapping("/v/monitoring")
public class ForwardVideoController {
    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private VideoService videoService;

    @Autowired
    private ForwardVideoService forwardVideoService;

    private static final String FORWARD_PAGE = "vas/monitoring/realTimeVideo/forward";
    private static final String REALTIME_PAGE = "vas/monitoring/realTimeVideo/realtime";
    private static final String HISTORY_PAGE = "vas/monitoring/realTimeVideo/history";

    private static final String FORWARD_KEY = "EFAD60B7A0EC7016EE4AFC9B3CE5D52E"; // md5("clbs_video_forward")

    private void checkKey(Map<String, String> params) throws BusinessException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("key") && !Objects.equals(entry.getValue(), FORWARD_KEY)) {
                throw new BusinessException("Invalid key.");
            }
        }
    }

    @RequestMapping(value = { "/forward" }, method = RequestMethod.GET)
    public String forward(@RequestParam Map<String, String> params, ModelMap model) throws BusinessException {
        checkKey(params);
        setModelMap(model);
        model.addAttribute("type", 1);
        return FORWARD_PAGE;
    }

    private void setModelMap(ModelMap model) {
        model.addAttribute("videoUrl", configHelper.getVideoUrl());
        model.addAttribute("audioPort", configHelper.getAudioPort());
        model.addAttribute("videoPort", configHelper.getVideoPort());
        model.addAttribute("resourcePort", configHelper.getResourcePort());
    }

    @RequestMapping(value = { "/forward/realtime" }, method = RequestMethod.GET)
    public String realtime(@RequestParam Map<String, String> params, ModelMap model) throws BusinessException {
        checkKey(params);
        setModelMap(model);
        return REALTIME_PAGE;
    }

    @RequestMapping(value = { "/forward/history" }, method = RequestMethod.GET)
    public String history(@RequestParam Map<String, String> params, ModelMap model) throws BusinessException {
        checkKey(params);
        setModelMap(model);
        model.addAttribute("type", 2);
        return HISTORY_PAGE;
    }

    @RequestMapping(value = "/forward/login", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean login() {
        boolean result = forwardVideoService.anonymousLogin();
        return new JsonResultBean(result);
    }

    @RequestMapping(value = { "/forward/monitorId" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getForwardedMonitorId(String plateNum) {
        String vehicleId = forwardVideoService.getForwardedMonitorId(plateNum);
        if (vehicleId == null) {
            return new JsonResultBean(JsonResultBean.FAULT);
        }
        return new JsonResultBean("", vehicleId);
    }

    @RequestMapping(value = { "/forward/getChannels" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getChannels(String vehicleId, boolean isChecked) {
        String tree = videoService.getChannelsByVehicleId(vehicleId, isChecked).toJSONString();
        return new JsonResultBean("", tree);
    }

    @RequestMapping(value = "/forward/fileUpload", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean fileUpload(HttpServletRequest request, FileUploadForm form) throws Exception {
        String clientIp = IPAddrUtil.getClientIp(request); // 获取客户端的IP地址
        return forwardVideoService.sendUploadOrder(clientIp, form);
    }

    @RequestMapping(value = "/forward/audioAndVideoParameters/{monitorId}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getAudioAndVideoParameters(@PathVariable("monitorId") String monitorId) {
        try {
            return forwardVideoService.getAudioAndVideoParameters(monitorId);
        } catch (Exception e) {
            log.error("获取音视频参数异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }


    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonResultBean exceptionHandle(HttpServletRequest request, Exception e) {
        log.error("url:{}, 视频转发异常：", request.getRequestURI(), e);
        return new JsonResultBean(JsonResultBean.FAULT);
    }
}
