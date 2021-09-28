package com.zw.app.controller.webMaster.statistics;

import com.zw.app.entity.BaseEntity;
import com.zw.app.entity.methodParameter.OilMassAndMileReportQueryEntity;
import com.zw.app.entity.methodParameter.QuerySendOilMassPollMonitorInfoEntity;
import com.zw.app.service.webMaster.statistics.AppOilMassMileReportService;
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


@Controller
@RequestMapping("/app/statistic/oilMassAndMile")
public class AppOilMassAndMileController {

    @Autowired
    private AppOilMassMileReportService appMileageReportService;

    /**
     * 获取综合统计-油量里程数据
     */
    @RequestMapping(value = "/getData", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getResultData(HttpServletRequest request, @Validated
        OilMassAndMileReportQueryEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appMileageReportService);
    }

    /**
     * 判断用户权限内是否存在轮询了油量传感器(0x41,0x42)的监控对象
     */
    @RequestMapping(value = "/judgeUserPollingOilMassMonitor", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean judgeUserPollingOilMassMonitor(HttpServletRequest request,
        @Validated BaseEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appMileageReportService);
    }

    /**
     * 获取用户权限内轮询了油量传感器(0x41,0x42)的监控对象
     */
    @RequestMapping(value = "/getPollingOilMassMonitor", method = RequestMethod.GET)
    @ResponseBody
    public AppResultBean getPollingOilMassMonitor(HttpServletRequest request,
        @Validated QuerySendOilMassPollMonitorInfoEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appMileageReportService);
    }
}
