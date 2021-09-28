package com.zw.platform.controller.reportmanagement;

import com.zw.platform.commons.Auth;
import com.zw.platform.service.reportManagement.OutAreaTotalTimeReportService;
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
 * 出区划累计时长Controller
 * @author Administrator
 * @version 3.8.1
 */
@RequestMapping("/m/reportManagement/outTotalTime")
@Controller
public class OutAreaTotalTimeReportController {
    private static final Logger log = LogManager.getLogger(OutAreaTotalTimeReportController.class);

    private static final String LIST_PAGE = "modules/reportManagement/outAreaTotalTimeReport";

    @Autowired
    private OutAreaTotalTimeReportService outReportService;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @Value("${sys.error.msg}")
    private String systemException;

    /**
     * 查询出区划累计时长统计列表
     * @param monitorIds  监控对象ID
     * @param totalTime 累计时间
     * @param endTime   结束时间
     */
    @RequestMapping(value = "/getOutAreaDurationStatisticsList", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getOutAreaDurationStatisticsList(String monitorIds, Integer totalTime, String endTime) {
        try {
            if (StringUtils.isBlank(monitorIds) || StringUtils.isBlank(endTime) || totalTime == null || totalTime < 0
                || totalTime > 999) {
                return new JsonResultBean(JsonResultBean.FAULT, "参数错误！");
            }
            return outReportService.getOutAreaDurationStatisticsList(monitorIds, totalTime, endTime);
        } catch (Exception e) {
            log.error("查询出区划累计时长统计列表异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, systemException);
        }
    }

    /**
     * 导出监控对象出区划累计时长数据
     */
    @RequestMapping(value = { "/exportOutAreaDurationStatistics" }, method = RequestMethod.GET)
    @ResponseBody
    public void exportOutAreaDurationStatistics(HttpServletResponse response, String queryParam) {
        try {
            outReportService.exportOutAreaDurationStatistics(response, queryParam);
        } catch (Exception e) {
            log.error("导出出区划累计时长报表数据异常", e);
        }
    }

}
