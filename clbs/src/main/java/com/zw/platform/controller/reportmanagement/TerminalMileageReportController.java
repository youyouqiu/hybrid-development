package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.service.reportManagement.TerminalMileageReportService;
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

/**
 * @author Administrator
 */
@Controller
@RequestMapping("/m/reportManagement/terminal/mileageReport")
public class TerminalMileageReportController {
    private static final Logger log = LogManager.getLogger(TerminalMileageReportController.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    private static final String LIST_PAGE = "/modules/reportManagement/terminal/mileageReport/list";

    @Autowired
    private TerminalMileageReportService terminalMileageReportService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询终端里程统计
     */
    @RequestMapping(value = "/getTerminalMileageStatistics", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTerminalMileageStatistics(String monitorIds, String startTime, String endTime) {
        try {
            if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数异常！");
            }
            return terminalMileageReportService.getTerminalMileageStatistics(monitorIds, startTime, endTime);
        } catch (Exception e) {
            log.error("查询终端里程统计异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 查询终端里程每日明细
     */
    @RequestMapping(value = "/getTerminalMileageDailyDetail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getTerminalMileageDailyDetail(String monitorId, String startTime, String endTime) {
        try {
            if (StringUtils.isBlank(monitorId) || StringUtils.isBlank(startTime) || StringUtils.isBlank(endTime)) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数异常！");
            }
            return terminalMileageReportService.getTerminalMileageDailyDetail(monitorId, startTime, endTime);
        } catch (Exception e) {
            log.error("查询终端里程每日明细异常", e);
            return new JsonResultBean(JsonResultBean.FAULT);
        }
    }

    /**
     * 导出终端里程统计
     */
    @RequestMapping(value = "/exportTerminalMileageStatistics", method = RequestMethod.POST)
    @ResponseBody
    public void exportTerminalMileageStatistics(HttpServletResponse response, String monitorId, String startTime,
        String endTime, String queryParam) {
        try {
            terminalMileageReportService
                .exportTerminalMileageStatistics(response, monitorId, startTime, endTime, queryParam);
        } catch (Exception e) {
            log.error("导出终端里程统计异常", e);
        }
    }

    /**
     * 导出终端里程每日明细
     */
    @RequestMapping(value = "/exportTerminalMileageDailyDetail", method = RequestMethod.POST)
    @ResponseBody
    public void exportTerminalMileageDailyDetail(HttpServletResponse response, String monitorId, String startTime,
        String endTime) {
        try {
            terminalMileageReportService.exportTerminalMileageDailyDetail(response, monitorId, startTime, endTime);
        } catch (Exception e) {
            log.error("导出终端里程每日明细异常", e);
        }
    }
}
