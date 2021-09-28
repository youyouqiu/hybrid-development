package com.zw.app.controller.videoResource;

import com.zw.app.domain.videoResource.AppAudioAndVideoParamBean;
import com.zw.app.domain.videoResource.AppResourceListBean;
import com.zw.app.service.videoResource.AppVideoResourceService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.domain.realTimeVideo.VideoChannelSetting;
import com.zw.platform.service.realTimeVideo.VideoChannelSettingService;
import io.swagger.annotations.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;

/**
 * app个人中心
 * @author lijie
 * @date 2019/11/22 15:00
 */
@Controller
@RequestMapping("/app/videoResource")
@Api(tags = { "app个人中心管理" }, description = "app个人中心相关接口")
public class AppVideoResourceController {

    private static final Logger log = LogManager.getLogger(AppVideoResourceController.class);

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    AppVideoResourceService appVideoResourceService;

    @Autowired
    VideoChannelSettingService videoChannelSettingService;

    /**
     * 通道号获取
     */
    @RequestMapping(value = "/getVideoChannel", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getVideoChannel(@Validated AppResourceListBean appResourceListBean, HttpServletRequest request,
        BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, appResourceListBean.getVersion());
            Method method = appVideoResourceService.getClass().getMethod(meth, String.class);
            List<VideoChannelSetting> re =
                (List<VideoChannelSetting>) method.invoke(appVideoResourceService, appResourceListBean.getVehicleId());
            return new AppResultBean(re);
        } catch (Exception e) {
            log.error("获取通道号异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = "/getAudioAndVideoParameters", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getAudioAndVideoParameters(AppAudioAndVideoParamBean appAudioAndVideoParamBean,
        HttpServletRequest request, BindingResult result) {
        try {
            if (result.getAllErrors().size() != 0) {
                return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
            }
            String requestUri = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestUri, appAudioAndVideoParamBean.getVersion());
            Method method = appVideoResourceService.getClass().getMethod(meth, String.class);
            return (AppResultBean) method.invoke(appVideoResourceService, appAudioAndVideoParamBean.getMonitorId());
        } catch (Exception e) {
            log.error("获取音视频参数异常异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

}
