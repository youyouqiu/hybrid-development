package com.zw.app.controller.webMaster.statistics;

import com.zw.app.entity.methodParameter.MileageReportQueryEntity;
import com.zw.app.service.webMaster.statistics.AppMileageReportService;
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
 * App 行驶里程报表
 * @author zhouzongbo on 2019/1/3 14:41
 */
@Controller
@RequestMapping("/app/statistic/mileageReport")
public class AppMileageReportController {

    @Value("${sys.error.msg}")
    private String sysError;

    @Autowired
    private AppMileageReportService appMileageReportService;

    @RequestMapping(value = "/findTravelDetailList",method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean findTravelDetailList(HttpServletRequest request, @Validated MileageReportQueryEntity queryEntity,
                                              BindingResult result) {
        return AppVersionUtil.getResultData(request,queryEntity,result,appMileageReportService);
    }


    @RequestMapping(value = "/findSingleMonitorList", method = RequestMethod.POST)
    @ResponseBody
    public AppResultBean findSingleMonitorList(HttpServletRequest request, @Validated MileageReportQueryEntity queryEntity,
                                               BindingResult result) {
        return AppVersionUtil.getResultData(request,queryEntity,result,appMileageReportService);
    }
}
