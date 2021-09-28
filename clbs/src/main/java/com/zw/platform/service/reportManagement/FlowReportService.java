package com.zw.platform.service.reportManagement;


import com.zw.platform.domain.reportManagement.FlowReport;
import com.zw.platform.domain.reportManagement.query.FlowQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface FlowReportService {
    /**
     * 查询流量信息
     * @param query
     * @param doPage
     * @throws Exception
     * @author zjc
     */
    List<FlowReport> getFlowData(FlowQuery query, boolean doPage) throws Exception;

    /**
     * 导出流量报表
     * @param title
     * @param type 导出类型（1:导出数据；2：导出模板）
     * @param res
     * @param flowReports
     * @return
     */
    boolean export(String title, int type, HttpServletResponse res, List<FlowReport> flowReports) throws Exception;
}
