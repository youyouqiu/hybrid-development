package com.zw.app.controller.personalCenter.statistics;

import com.zw.app.entity.methodParameter.QueryTerminalMileageEntity;
import com.zw.app.service.personalCenter.AppTerminalMileageService;
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
 * App综合统计 里程统计
 * @author lijie
 * @version 1.0
 * @date 2019/10/10 9:54
 */
@Controller
@RequestMapping("/app/reportManagement/terminalMileage")
public class AppTerminalMileageController {

    @Autowired
    AppTerminalMileageService appTerminalMileageService;

    /**
     * 获取车辆的里程信息
     * @param request
     * @param queryEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean judgeUserIfOwnSendWorkHourPollsMonitor(HttpServletRequest request,
        @Validated QueryTerminalMileageEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appTerminalMileageService);
    }

    /**
     * 获取单个车辆的里程信息
     * @param request
     * @param queryEntity
     * @param result
     * @return
     */
    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean getBasicLocationInfoByMonitorId(HttpServletRequest request,
        @Validated QueryTerminalMileageEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appTerminalMileageService);
    }

}
