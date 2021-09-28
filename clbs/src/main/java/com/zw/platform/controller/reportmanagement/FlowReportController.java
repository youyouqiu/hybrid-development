package com.zw.platform.controller.reportmanagement;


import com.github.pagehelper.Page;
import com.zw.platform.commons.Auth;
import com.zw.platform.domain.reportManagement.FlowReport;
import com.zw.platform.domain.reportManagement.query.FlowQuery;
import com.zw.platform.service.reportManagement.FlowReportService;
import com.zw.platform.util.StringUtil;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.PageGridBean;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;


@Controller
@RequestMapping("/m/reportManagement/flowReport")
public class FlowReportController {
    private static final String LIST_PAGE = "modules/reportManagement/flowReport";

    @Autowired
    FlowReportService flowReportService;

    private static Logger log = LogManager.getLogger(FlowReportController.class);

    @Auth
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public String listPage() {
        return LIST_PAGE;
    }

    @RequestMapping(value = "/getFlowReports", method = RequestMethod.POST)
    @ResponseBody
    public PageGridBean getFlowReports(FlowQuery query) {
        try {
            if (query != null && !StringUtil.isNullOrBlank(query.getVehicleIds())) {
                Page<FlowReport> result = (Page<FlowReport>) flowReportService.getFlowData(query, true);
                return new PageGridBean(result, true);
            }
            return new PageGridBean(new Page<FlowReport>());
        } catch (Exception e) {
            log.error("获取流量报表！", e);
            return new PageGridBean(false);
        }
    }

    /**
     * 导出(生成excel文件)
     * @param query
     * @throws ParseException
     * @throws UnsupportedEncodingException
     * @throws BusinessException
     */
    @RequestMapping(value = "/export", method = RequestMethod.POST)
    @ResponseBody
    public void export1(FlowQuery query, HttpServletResponse res) {
        try {
            ExportExcelUtil.setResponseHead(res, "流量报表");
            flowReportService.export(null, 1, res, flowReportService.getFlowData(query, false));
        } catch (Exception e) {
            log.error("导出流量异常", e);
        }
    }

}
