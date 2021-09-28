package com.zw.app.controller.webMaster.statistics;

import com.zw.app.entity.methodParameter.ParkingReportQueryEntity;
import com.zw.app.service.webMaster.statistics.AppParkingReportService;
import com.zw.app.util.AppVersionUtil;
import com.zw.app.util.common.AppResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * App 停止报表
 * @author zhouzongbo on 2019/1/3 14:41
 */
@Controller
@RequestMapping("/app/statistic/parkingReport")
public class AppParkingReportController {

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private AppParkingReportService appParkingReportService;

    @RequestMapping(value = "/findParkingDetailList", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean findParkingDetailList(HttpServletRequest request,
        @Validated ParkingReportQueryEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appParkingReportService);
    }


    @RequestMapping(value = "/findSingleMonitorParkingList", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean findSingleMonitorParkingList(HttpServletRequest request,
        @Validated ParkingReportQueryEntity queryEntity, BindingResult result) {
        return AppVersionUtil.getResultData(request, queryEntity, result, appParkingReportService);
    }
}
