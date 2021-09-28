package com.zw.adas.controller.riskStatistics;

import com.zw.adas.domain.riskStatistics.query.EventStatisticsRecordQuery;
import com.zw.adas.domain.riskStatistics.query.RiskStatisticsRecordQuery;
import com.zw.adas.service.riskStatistics.AdasRiskStatisticsRecordService;
import com.zw.platform.commons.Auth;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.TemplateExportExcel;
import com.zw.talkback.common.ControllerTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 功能描述:
 * @author lijie
 * @date 2020/6/17
 * @time 17:08
 */
@Controller
@RequestMapping(value = "/r/riskManagement/statisticsReport")
public class AdasRiskStatisticsRecordController {
    private static final Logger log = LogManager.getLogger(AdasRiskStatisticsRecordController.class);

    private static final String LIST_PAGE = "modules/reportManagement/riskStatisticsReport";

    @Autowired
    private AdasRiskStatisticsRecordService recordService;

    @Autowired
    private TemplateExportExcel templateExportExcel;

    @Auth
    @RequestMapping(value = { "/list" }, method = RequestMethod.GET)
    public String getListPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = { "/list" }, method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean list(EventStatisticsRecordQuery query) {
        return ControllerTemplate.getResultBean(() -> recordService.getListData(query), query, "查询主动安全报表列表数据异常");
    }

    @RequestMapping(value = { "/exportList" }, method = RequestMethod.POST)
    public void exportList(EventStatisticsRecordQuery query, HttpServletResponse response) {
        ControllerTemplate
            .export(() -> recordService.exportData(query, response), "主动安全统计导出报表", response, "导出主动安全统计导出报表异常");
    }

    /**
     * 查询主动安全统计报表详情
     * @param query
     * @return
     */
    @RequestMapping(value = { "/search/reportInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public JsonResultBean searchStatisticsReportInfo(RiskStatisticsRecordQuery query) {
        try {
            return new JsonResultBean(recordService.searchReportInfo(query, false));
        } catch (Exception e) {
            log.error("查询主动安全统计报表详情异常", e);
        }
        return new JsonResultBean(JsonResultBean.FAULT);
    }

    /**
     * 导出主动安全统计报表 具体报警详情
     * @param query
     * @return
     */
    @RequestMapping(value = { "/exportReportInfo" }, method = RequestMethod.POST)
    @ResponseBody
    public void exportReportInfo(HttpServletResponse res, RiskStatisticsRecordQuery query) {
        try {
            Map<String, Object> data = recordService.searchReportInfo(query, true);
            data.put("startTime", query.getStartTime().split(" ")[0]);
            data.put("endTime", query.getEndTime().split(" ")[0]);
            String fileName = "主动安全统计报表详情报表";
            templateExportExcel.templateExportExcel("/file/cargoReport/exportReportInfo.xlsx", res, data, fileName);
        } catch (Exception e) {
            log.error("导出主动安全统计报表详情异常", e);
        }
    }

}