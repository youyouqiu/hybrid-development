package com.zw.app.controller.webMaster.statistics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.app.service.webMaster.statistics.WebMasterStatisticsService;
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
 * app后台综合统计配置
 * @author lijie
 * @date 2018/12/07 15:59
 */
@Controller
@RequestMapping("/m/app/statistics")
@Api(tags = {"app后台综合统计配置"}, description = "app后台综合统计配置相关接口")
public class WebMasterStatisticsController {
    private static Logger log = LogManager.getLogger(WebMasterStatisticsController.class);
    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    WebMasterStatisticsService webMasterStatisticsService;

    /**
     * 查询app后台综合统计配置信息
     * @author lijie
     * @date 2018/12/07 16:59
     */
    @Auth
    @RequestMapping(value = {"/config/list"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getStatistics(String id) {
        try {
            if (id != null) {
                JSONObject jsonObject = webMasterStatisticsService.getStatistics(id);
                if (jsonObject != null) {
                    return new AppResultBean(jsonObject);
                } else {
                    return new AppResultBean(AppResultBean.PARAM_ERROR);
                }
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("APP查询综合统计配置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 修改app后台综合统计配置信息
     * @author lijie
     * @date 2018/12/10 10:54
     */
    @Auth
    @RequestMapping(value = {"/config"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean updateStatisticsConfig(String id, String statistics) {
        //statistics = "[{'name':'超速统计','number':1}, {'name':'上线统计','number':2 }]";
        JSONArray jsonArray;
        try {
            jsonArray = JSON.parseArray(statistics);
        } catch (Exception e) {
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        }
        try {
            Boolean success = webMasterStatisticsService.updateStatisticsConfig(jsonArray, id);
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("APP查询报警配置信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 恢复app后台综合统计配置信息为组织默认
     * @author lijie
     * @date 2018/12/10 11:14
     */
    @Auth
    @RequestMapping(value = {"/reset"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean resetStatisticsConfig() {
        try {
            Boolean success = webMasterStatisticsService.resetStatisticsConfig();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("恢复综合统计配置信息为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 设置app后台综合统计配置信息为组织默认
     * @author lijie
     * @date 2018/12/10 11:50
     */
    @Auth
    @RequestMapping(value = {"/default"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean defaultAlarmType() {
        try {
            Boolean success = webMasterStatisticsService.defaultStatisticsConfig();
            if (success) {
                return new AppResultBean(AppResultBean.SUCCESS);
            } else {
                return new AppResultBean(AppResultBean.PARAM_ERROR);
            }
        } catch (Exception e) {
            log.error("设置综合统计配置信息为组织默认异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取综合统计参考组织信息
     * @author lijie
     * @date 2018/12/10 11:50
     */
    @Auth
    @RequestMapping(value = {"/group"}, method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean referenceGroup() {
        try {
            JSONObject jsonObject = webMasterStatisticsService.referenceGroup();
            return new AppResultBean(jsonObject);
        } catch (Exception e) {
            log.error("获取综合统计参考组织信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}