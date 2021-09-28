package com.zw.platform.controller.oilmgt;

import com.zw.adas.utils.controller.AdasControllerTemplate;
import com.zw.platform.commons.Auth;
import com.zw.platform.service.oilmgt.F3HighPrecisionReportService;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/***
 @Author zhengjc
 @Date 2019/11/15 15:07
 @Description f3高精度报表
 @version 1.0
 **/
@Controller
@RequestMapping("/v/oilmgt/f3hpr")
public class F3HighPrecisionReportController {

    private static final String LIST_PAGE = "vas/oilmgt/f3hpr/list";

    @Autowired
    private F3HighPrecisionReportService f3HighPrecisionReportService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() throws BusinessException {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/getF3HighPrecisionReport", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getF3HighPrecisionReport(String vehicleId, String startTime, String endTime) {
        return AdasControllerTemplate.getPressResultBean(
            () -> f3HighPrecisionReportService.getF3HighPrecisionReport(vehicleId, startTime, endTime), "查询f3高精度报表异常");

    }

    @RequestMapping(value = "/export")
    @ResponseBody
    public void export(String vehicleId, String startTime, String endTime, HttpServletResponse response) {

        AdasControllerTemplate.execute(
            () -> f3HighPrecisionReportService.exportF3HighPrecisionReport(vehicleId, startTime, endTime, response),
            "f3高精度报表模块导出异常");

    }

    @RequestMapping(value = "/getVoltageInfo", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getVoltageInfo(String vehicleId) {
        return AdasControllerTemplate
            .getResultBean(() -> f3HighPrecisionReportService.getVoltageInfo(vehicleId), "查询f3高精度报表电压阈值异常");

    }

}
