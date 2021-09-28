package com.zw.app.controller.personalCenter.statistics;

import com.zw.app.entity.BaseEntity;
import com.zw.app.entity.methodParameter.QuerySendWorkHourPollMonitorInfoEntity;
import com.zw.app.entity.methodParameter.QueryWorkHourStatisticsEntity;
import com.zw.app.service.personalCenter.AppWorkHourReportService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * App综合统计 工时统计
 * @author penghj
 * @version 1.0
 * @date 2019/7/12 9:54
 */
@Controller
@RequestMapping("/app/reportManagement/workHourReport")
public class AppWorkHourReportController {

    @Autowired
    AppWorkHourReportService appWorkHourReportService;

    /**
     * 判断用户是否拥有下发了工时传感器轮询的监控对象
     * @param request
     * @param queryEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "/judgeUserIfOwnSendWorkHourPollsMonitor", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean judgeUserIfOwnSendWorkHourPollsMonitor(HttpServletRequest request,
        @Validated BaseEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appWorkHourReportService);
    }

    /**
     * 获得下发了工时传感器轮询的监控对象信息
     * @param request
     * @param queryEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "/getSendWorkHourPollsMonitorInfo", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getBasicLocationInfoByMonitorId(HttpServletRequest request,
        @Validated QuerySendWorkHourPollMonitorInfoEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appWorkHourReportService);
    }

    /**
     * 查询App工时统计信息
     * @param request
     * @param queryEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "/getWorkHourStatisticsInfo", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getWorkHourStatisticsInfo(HttpServletRequest request,
        @Validated QueryWorkHourStatisticsEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appWorkHourReportService);
    }
}
