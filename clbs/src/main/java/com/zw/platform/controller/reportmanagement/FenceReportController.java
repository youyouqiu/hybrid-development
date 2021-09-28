package com.zw.platform.controller.reportmanagement;


import com.google.common.collect.Lists;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.FenceReport;
import com.zw.platform.service.reportManagement.FenceReportService;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.RedisUtil;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping("/m/reportManagement/fenceReport")
public class FenceReportController {
    /**
     * 日期转换格式
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String LIST_PAGE = "modules/reportManagement/fenceReport";

    private static final Logger log = LogManager.getLogger(FenceReportController.class);

    @Autowired
    private FenceReportService fenceReportService;

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    /**
     * 查询车辆进出围栏信息（用于统计）
     *
     * @param vehicleList 所有车牌号
     * @param startTime   开始时间
     * @param endTime     结束时间
     */
    @RequestMapping(value = "/getFenceInAndOut", method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean getFenceInAndOut(String vehicleList, String startTime, String endTime) {
        try {
            Long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime();
            Long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime();
            final List<String> vehicleIds = Lists.newArrayList(vehicleList.split(","));
            List<FenceReport> fenceReports = fenceReportService.getFenceReport(vehicleIds, start, end);
            return new JsonResultBean(fenceReports);
        } catch (Exception e) {
            log.error("围栏进出统计页面查询监控对象进出围栏次数信息异常", e);
            return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
        }
    }

    /**
     * 导出围栏进出统计
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public boolean export(String vehicleList, String startTime, String endTime, HttpServletRequest request) {
        try {
            Long start = DateUtils.parseDate(startTime, DATE_FORMAT).getTime();
            Long end = DateUtils.parseDate(endTime, DATE_FORMAT).getTime();
            final List<String> vehicleIds = Lists.newArrayList(vehicleList.split(","));
            RedisUtil.storeExportDataToRedis("exportFuelConsumptionStatistics",
                    fenceReportService.getFenceReport(vehicleIds, start, end));
            return true;
        } catch (Exception e) {
            log.error("围栏进出统计页面导出进出围栏统计(post)异常", e);
            return false;
        }

    }

    @RequestMapping(value = {"/export"}, method = RequestMethod.GET)
    @ResponseBody
    public void exportFence(HttpServletResponse response) {
        try {
            ExportExcelUtil.setResponseHead(response, "电子围栏进出统计报表");
            fenceReportService.exports(null, 1, response,
                    RedisUtil.getExportDataFromRedis("exportFuelConsumptionStatistics"));
        } catch (Exception e) {
            log.error("围栏进出统计页面导出进出围栏统计(get)异常", e);
        }
    }

}
