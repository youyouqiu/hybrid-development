package com.zw.platform.service.reportManagement;


import com.zw.platform.domain.reportManagement.FenceReport;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface FenceReportService {
    /**
     * 查询给定时间段的围栏进出次数统计
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     */
    List<FenceReport> getFenceReport(List<String> vehicleIds, Long startTime, Long endTime) throws Exception;

    /**
     * 把围栏进出数据次数统计的数据导出到excle文件
     * @param title
     * @param type
     * @param response
     * @param fenceReports
     * @return
     */
    public boolean exports(String title, int type, HttpServletResponse response, List<FenceReport> fenceReports)
        throws Exception;

}
