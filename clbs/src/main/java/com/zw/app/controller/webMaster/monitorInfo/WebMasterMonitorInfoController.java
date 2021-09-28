package com.zw.app.controller.webMaster.monitorInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.service.webMaster.monitorInfo.WebMasterMonitorConfigService;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.commons.Auth;
import io.swagger.annotations.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * app后台监控对象信息配置信息管理
 * @author lijie
 * @date 2019/9/27 14:59
 */
@Controller
@RequestMapping("/m/app/monitor/config")
@Api(tags = { "app后台监控对象显示信息配置" }, description = "app后台报警配置相关接口")
public class WebMasterMonitorInfoController {
    private static Logger log = LogManager.getLogger(WebMasterMonitorInfoController.class);
    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    WebMasterMonitorConfigService webMasterMonitorConfigService;

    /**
     * 查询app后台监控对象信息配置信息
     * @author lijie
     * @date 2019/9/27 15:09
     */
    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getMonitorConfig(String id) {
        try {
            if (id != null) {
                JSONObject jsonObject = webMasterMonitorConfigService.getMonitorConfig(id);
                if (jsonObject != null) {
                    return new AppResultBean(jsonObject);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("APP查询监控对象配置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改app后台监控对象显示配置信息
     * @author lijie
     * @date 2019/9/29 10:54
     */
    @Auth
    @RequestMapping(value = { "/update" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateMonitorConfig(String id, String monitorConfigs) {
        JSONArray jsonArray;
        try {
            jsonArray = JSON.parseArray(monitorConfigs);
        } catch (Exception e) {
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        }
        try {
            Boolean success = webMasterMonitorConfigService.updateMonitorConfig(jsonArray, id);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("APP修改监控对象显示配置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复app后台监控对象显示配置信息为组织默认
     * @author lijie
     * @date 2019/9/29 17:14
     */
    @Auth
    @RequestMapping(value = { "/reset" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetMonitorConfig() {
        try {
            Boolean success = webMasterMonitorConfigService.resetMonitorConfig();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复监控对象显示配置信息为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置app后台监控对象显示配置信息为组织默认
     * @author lijie
     * @date 2019/9/29 17:50
     */
    @Auth
    @RequestMapping(value = { "/default" }, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultMonitorConfig() {
        try {
            Boolean success = webMasterMonitorConfigService.defaultMonitorConfig();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置监控对象显示配置信息为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取参考组织信息
     * @author lijie
     * @date 2019/9/29 18:05
     */
    @Auth
    @RequestMapping(value = { "/reference" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean referenceGroup(String type) {
        try {
            JSONObject jsonObject = webMasterMonitorConfigService.referenceGroup(type);
            return new AppResultBean(jsonObject);
        } catch (Exception e) {
            log.error("获取参考组织信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}