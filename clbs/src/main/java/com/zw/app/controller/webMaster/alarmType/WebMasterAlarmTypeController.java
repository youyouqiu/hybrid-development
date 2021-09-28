package com.zw.app.controller.webMaster.alarmType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.service.webMaster.alarmType.WebMasterAlarmTypeService;
import com.zw.app.util.common.AppResultBean;
import com.zw.platform.commons.Auth;
import io.swagger.annotations.Api;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * app后台报警配置信息管理
 *
 * @author lijie
 * @date 2018/8/27 18:59
 */
@Controller
@RequestMapping("/m/app/group")
@Api(tags = {"app后台报警配置"}, description = "app后台报警配置相关接口")
public class WebMasterAlarmTypeController {
    private static Logger log = LogManager.getLogger(WebMasterAlarmTypeController.class);
    @Value("${sys.error.msg}")
    private String sysError;
    @Autowired
    WebMasterAlarmTypeService webMasterAlarmTypeService;

    /**
     * 查询app后台报警参数配置信息
     *
     * @author lijie
     * @date 2018/8/28 09:09
     */
    @Auth
    @RequestMapping(value = {"/{id}/alarmType/config/list"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getAlarmType(@PathVariable String id) {
        try {
            if (id != null) {
                JSONObject jsonObject = webMasterAlarmTypeService.getAlarmType(id);
                if (jsonObject != null) {
                    return new AppResultBean(jsonObject);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("APP查询报警配置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改app后台报警参数配置信息
     *
     * @author lijie
     * @date 2018/8/28 15:54
     */
    @Auth
    @RequestMapping(value = {"/{id}/alarmType/config"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateAlarmType(@PathVariable String id, String alarmTypes) {
        JSONArray jsonArray;
        try {
            jsonArray = JSON.parseArray(alarmTypes);
        } catch (Exception e) {
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        }
        try {
                Boolean success = webMasterAlarmTypeService.updateAlarmType(jsonArray, id);
                if (success) {
                    return new AppResultBean(AppResultBean.SUCCESS);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
        } catch (Exception e) {
            log.error("APP修改报警配置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复app后台报警参数配置信息为组织默认
     *
     * @author lijie
     * @date 2018/8/28 17:14
     */
    @Auth
    @RequestMapping(value = {"/alarmType/reset"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetAlarmType() {
        try {
            Boolean success = webMasterAlarmTypeService.resetAlarmType();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复报警配置信息为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置app后台报警参数配置信息为组织默认
     *
     * @author lijie
     * @date 2018/8/28 17:50
     */
    @Auth
    @RequestMapping(value = {"/alarmType/default"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultAlarmType() {
        try {
            Boolean success = webMasterAlarmTypeService.defaultAlarmType();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置报警配置信息为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取参考组织信息
     *
     * @author lijie
     * @date 2018/8/28 18:05
     */
    @Auth
    @RequestMapping(value = {"/reference"}, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean referenceGroup() {
        try {
            JSONObject jsonObject = webMasterAlarmTypeService.referenceGroup();
            return new AppResultBean(jsonObject);
        } catch (Exception e) {
            log.error("获取参考组织信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}