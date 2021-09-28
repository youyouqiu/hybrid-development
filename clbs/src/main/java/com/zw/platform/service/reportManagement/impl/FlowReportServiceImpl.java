package com.zw.platform.service.reportManagement.impl;

import com.github.pagehelper.PageHelper;
import com.zw.platform.domain.basicinfo.enums.PlateColor;
import com.zw.platform.domain.reportManagement.FlowReport;
import com.zw.platform.domain.reportManagement.query.FlowQuery;
import com.zw.platform.repository.vas.FlowReportDao;
import com.zw.platform.service.reportManagement.FlowReportService;
import com.zw.platform.util.common.PrecisionUtils;
import com.zw.platform.util.excel.ExportExcelParam;
import com.zw.platform.util.excel.ExportExcelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Service
public class FlowReportServiceImpl implements FlowReportService {

    @Autowired
    FlowReportDao flowReportDao;

    @Override
    public List<FlowReport> getFlowData(FlowQuery query, boolean doPage) throws Exception {
        List<FlowReport> flowReports = doPage
                ? PageHelper.startPage(query.getPage().intValue(), query.getLimit().intValue())
                .doSelectPage(() -> flowReportDao.getFlowReports(query))
                : flowReportDao.getFlowReports(query);
        transformFlowReports(flowReports);
        return flowReports;
    }

    private void transformFlowReports(List<FlowReport> flowReports) {
        for (FlowReport flowReport : flowReports) {
            transformPlateColor(flowReport);
            transformFLowValue(flowReport);

        }

    }

    private void transformFLowValue(FlowReport flowReport) {
        double flowValue = flowReport.getFlowValue() / (1024 * 1024);

        flowReport.setFlowValueStr(PrecisionUtils.roundByScale(flowValue, 2));
    }

    private void transformPlateColor(FlowReport flowReport) {
        flowReport.setPlateColor(PlateColor.getNameOrBlankByCode(flowReport.getPlateColor()));

    }

    @Override
    public boolean export(String title, int type, HttpServletResponse res, List<FlowReport> flowReports)
        throws IOException {
        return ExportExcelUtil
            .export(new ExportExcelParam(title, type, flowReports, FlowReport.class, null, res.getOutputStream()));
    }

}
