package com.zw.app.controller.personalCenter;

import com.zw.app.service.personalCenter.PersonalCenterService;
import com.zw.app.service.webMaster.feedBack.AppFeedBackService;
import com.zw.app.util.common.AppResultBean;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * app个人中心
 *
 * @author lijie
 * @date 2018/9/06 15:00
 */
@Controller
@RequestMapping("/app")
@Api(tags = {"app个人中心管理"}, description = "app个人中心相关接口")
public class PersonalCenterController {
    @Autowired
    AppFeedBackService appFeedBackService;
    @Autowired
    PersonalCenterService personalCenterService;

    @Value("${sys.error.msg}")
    private String sysError;

    private static Logger log = LogManager.getLogger(PersonalCenterController.class);

    /**
     * 发送app反馈信息
     *
     * @author lijie
     * @date 2018/9/6 15:09
     */
    @RequestMapping(value = {"/sendFeedback"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean sendFeedBack(@RequestParam("feedback") String feedback) {
        try {
            if (StringUtils.isNotBlank(feedback)) {
                Boolean success = appFeedBackService.sendFeedBack(feedback);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("发送app反馈信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * app用户修改密码
     *
     * @author lijie
     * @date 2018/9/6 15:11
     */
    @RequestMapping(value = {"/user/password"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateUserPassword(@RequestParam("oldPassword") final String oldPassword,
                                            @RequestParam("newPassword") final String newPassword) {
        try {
            if (StringUtils.isNotBlank(oldPassword) && StringUtils.isNotBlank(newPassword)) {
                AppResultBean appResultBean = personalCenterService.updateUserPassword(oldPassword, newPassword, "APP");
                return appResultBean;
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR, sysError);
            }
        } catch (Exception e) {
            log.error("修改app用户密码异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取app自定义信息
     *
     * @author lijie
     * @date 2018/9/11 14:11
     */
    @RequestMapping(value = {"/customInfo"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getAppCustomInfo(Integer version) {
        try {
            AppResultBean appResultBean = personalCenterService.getAppCustomInfo(version);
            if (appResultBean != null) {
                return appResultBean;
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("获取app自定义信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取app用户个人信息
     *
     * @author lijie
     * @date 2018/9/17 09:40
     */
    @RequestMapping(value = {"/AppUserInformation"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getAppUser() {
        try {
            AppResultBean appResultBean = personalCenterService.getAppUser();
            if (appResultBean != null) {
                return appResultBean;
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("获取app用户个人信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 保存App端用户登录/登出日志
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/saveAppRegisterLog"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean saveAppRegisterLog(HttpServletRequest request, Integer registerType) {
        try {
            Boolean flag = personalCenterService.saveAppRegisterLog(request, registerType);
            if (flag) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("保存App端用户登录/登出日志异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

}
