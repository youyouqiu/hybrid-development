package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.dto.reportManagement.ParkingInfoDto;
import com.zw.platform.service.reportManagement.TerminalParkingReportService;
import com.zw.platform.util.common.JsonResultBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Administrator
 */
@Controller
@RequestMapping("/m/reportManagement/terminal/parkingReport")
public class TerminalParkingReportController {
    private static final Logger log = LogManager.getLogger(TerminalParkingReportController.class);

    @Autowired
    private TerminalParkingReportService terminalParkingReportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static final String LIST_PAGE = "modules/reportManagement/terminal/parkingReport/list";

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 获取停驶数据（大数据月表，调用paas-cloud接口）
     */
    @RequestMapping(value = "/getStopBigDataNew", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getStopBigDataNew(String vehicleId, String startTime, String endTime) {
        try {
            if (StringUtils.isNotBlank(vehicleId) && StringUtils.isNotBlank(startTime) && StringUtils
                .isNotBlank(endTime)) {
                List<ParkingInfoDto> piDtoList =
                    terminalParkingReportService.getStopBigDataFromPaas(vehicleId, startTime, endTime);
                return new JsonResultBean(piDtoList);
            }
            return new JsonResultBean(JsonResultBean.FAULT);
        } catch (Exception e) {
            log.error("获取停驶数据（大数据月表）异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出(存数据)
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public boolean exportQueryData(String vehicleId, String startTime, String endTime, String simpleQueryParam,
        int exportType) {
        try {
            return terminalParkingReportService
                .exportQueryData(vehicleId, startTime, endTime, simpleQueryParam, exportType);
        } catch (Exception e) {
            log.error("导出车辆信息(post)异常", e);
            return false;
        }
    }

    /**
     * 导出(生成excel文件)
     */
    @RequestMapping(value = "/export", method = RequestMethod.GET)
    public void export(HttpServletResponse res) {
        try {
            terminalParkingReportService.export(res);
        } catch (Exception e) {
            log.error(" 导出车辆信息(get)异常", e);
        }
    }

}
