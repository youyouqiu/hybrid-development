package com.zw.app.controller.alarm;

import com.alibaba.fastjson.JSONObject;
import com.zw.app.domain.alarm.AppAlarmAction;
import com.zw.app.domain.alarm.AppAlarmDetailInfo;
import com.zw.app.domain.alarm.AppAlarmInfo;
import com.zw.app.domain.alarm.AppAlarmQuery;
import com.zw.app.service.alarm.AppAlarmSearchService;
import com.zw.app.util.common.AppResultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/app/alarm")
public class AppAlarmSearchController {
    private static Logger logger = LogManager.getLogger(AppAlarmSearchController.class);

    @Autowired
    private AppAlarmSearchService alarmSearchService;

    @Value("${sys.error.msg}")
    private String sysError;

    @RequestMapping(value = { "/monitorNumber" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getAlarmMonitorNumber(AppAlarmQuery alarmQuery) {
        try {
            if (alarmQuery != null) {
                JSONObject msg = alarmSearchService.getAlarMonitorNumber(alarmQuery);
                return new AppResultBean(msg);
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            logger.error("APP查询报警监控对象数量异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = { "/monitor" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getAlarMonitor(AppAlarmQuery alarmQuery) {
        try {
            if (alarmQuery != null) {
                List<AppAlarmInfo> result = alarmSearchService.getAlarmInfo(alarmQuery);
                if (result != null) {
                    JSONObject msg = new JSONObject();
                    msg.put("objects", result);
                    return new AppResultBean(msg);
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            logger.error("APP查询报警监控对象异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    @RequestMapping(value = { "/monitor/{id}/summary" }, method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getMonitorAlarm(@PathVariable String id, AppAlarmQuery alarmQuery) {
        try {
            if (StringUtils.isNotBlank(id) && alarmQuery != null) {
                JSONObject msg = new JSONObject();
                List<AppAlarmAction> result = alarmSearchService.getMonitorAlarmAction(id, alarmQuery);
                if (result != null) {
                    msg.put("alarmSummary", result);
                    return new AppResultBean(msg);
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            logger.error("APP查询监控对象报警概要信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }

    }

    @RequestMapping(value = "/monitor/{id}/detail", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getAlarmDetail(@PathVariable String id, String time, AppAlarmQuery alarmQuery) {
        try {
            if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(time) && alarmQuery != null) {
                List<AppAlarmDetailInfo> result = alarmSearchService.getMonitorAlarmDetail(id, alarmQuery, time);
                if (result != null) {
                    JSONObject msg = new JSONObject();
                    msg.put("alarmDetails", result);
                    return new AppResultBean(msg);
                }
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            logger.error("APP查询监控对象报警详细信息异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }

    /**
     * 获取报警参数设置
     */
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getAlarmSetting() {
        try {
            JSONObject result = alarmSearchService.getUserAlarmSetting();
            if (result != null) {
                return new AppResultBean(result);
            }
            return new AppResultBean(AppResultBean.PARAM_ERROR);
        } catch (Exception e) {
            logger.error("获取用户报警设置异常", e);
            return new AppResultBean(AppResultBean.SERVER_ERROR, sysError);
        }
    }
}
