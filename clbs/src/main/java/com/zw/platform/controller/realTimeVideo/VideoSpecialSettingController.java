package com.zw.platform.controller.realTimeVideo;

import com.zw.platform.domain.realTimeVideo.VideoSpecialSetting;
import com.zw.platform.service.realTimeVideo.VideoSpecialSettingService;
import com.zw.platform.util.GetIpAddr;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangsq
 * @date 2018/3/22 17:46
 */
@RestController
@RequestMapping("/realTimeVideo/videoSpecialSetting")
public class VideoSpecialSettingController {

    private static Logger log = LogManager.getLogger(VideoSpecialSettingController.class);

    @Autowired
    private VideoSpecialSettingService videoSpecialSettingService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public JsonResultBean saveVideoSpecialSetting(VideoSpecialSetting videoSpecialSetting, HttpServletRequest request) {
        try {
            String ipAddress = new GetIpAddr().getIpAddr(request);
            videoSpecialSettingService.saveVideoSpecialSetting(videoSpecialSetting, ipAddress);
            log.info("保存音视频特殊参数");
            return new JsonResultBean("保存成功");
        } catch (Exception e) {
            log.error("保存音视频特殊参数失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }

    @RequestMapping(value = "/getOne", method = RequestMethod.POST)
    public JsonResultBean getOne() {
        try {
            return new JsonResultBean(videoSpecialSettingService.getOne());
        } catch (Exception e) {
            log.error("获取音视频特殊参数失败", e);
            return new JsonResultBean(false, sysErrorMsg);
        }
    }
}
