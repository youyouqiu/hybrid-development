package com.zw.app.controller.update;

import com.zw.app.entity.BaseEntity;
import com.zw.app.service.update.UpdateService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/***
 @Author gfw
 @Date 2018/12/11 13:47
 @Description APP版本更新
 @version 1.0
 **/
@Controller
@RequestMapping(value = "app")
public class AppVersionUpdateController {
    private static Logger log = LogManager.getLogger(AppVersionUpdateController.class);

    @Autowired
    UpdateService updateService;
    /**
     * 平台支持的最高的App版本
     */
    @Value("${app.maximum.version}")
    private String appHighest;

    /**
     * 手动检测版本更新
     * @param request
     * @param baseEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "update/version")
    @ResponseBody
    public AppResultBean appUpdateVersion(HttpServletRequest request, @Validated BaseEntity baseEntity,
        BindingResult result) {
        if (result.getAllErrors().size() != 0) {
            return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, baseEntity.getVersion());
            Method method = updateService.getClass().getMethod(meth, BaseEntity.class);
            return (AppResultBean) method.invoke(updateService, baseEntity);
        } catch (Exception e) {
            log.error("手动检测版本更新:{}", e);
            return new AppResultBean(AppResultBean.PARAM_ERROR, e.getMessage());
        }
    }

    /**
     * App强制更新
     * @param baseEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "update/force")
    @ResponseBody
    public AppResultBean appForceUpdateVersion(HttpServletRequest request, @Validated BaseEntity baseEntity,
        BindingResult result) {
        if (result.getAllErrors().size() != 0) {
            return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            String requestURI = request.getRequestURI();
            String meth = AppVersionUtil.dealVersionName(requestURI, baseEntity.getVersion());
            Method method = updateService.getClass().getMethod(meth, BaseEntity.class);
            return (AppResultBean) method.invoke(updateService, baseEntity);
        } catch (Exception e) {
            log.error("App强制更新出错:{}", e);
            return new AppResultBean(AppResultBean.PARAM_ERROR, e.getMessage());
        }
    }

    /**
     * 平台支持最高的App版本
     * @param baseEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "update/highest")
    @ResponseBody
    public AppResultBean appHighest(HttpServletRequest request, @Validated BaseEntity baseEntity,
        BindingResult result) {
        if (result.getAllErrors().size() != 0) {
            return new AppResultBean(AppResultBean.PARAM_ERROR, result.getAllErrors().get(0).getDefaultMessage());
        }
        try {
            Integer highestVersion = Integer.parseInt(appHighest);
            Integer appVersion = baseEntity.getVersion();
            if (appVersion > highestVersion) {
                return new AppResultBean(AppResultBean.SERVER_ERROR, "平台版本过低\r\n 请联系平台管理员");
            } else {
                return new AppResultBean(AppResultBean.SUCCESS);
            }
        } catch (Exception e) {
            log.error("平台支持最高的版本检测出错:{}", e);
            return new AppResultBean(AppResultBean.PARAM_ERROR, e.getMessage());
        }
    }
}
