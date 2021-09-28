package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.BigDataReport.OnlineReport;
import com.zw.platform.domain.BigDataReport.PositionInfo;
import com.zw.platform.domain.infoconfig.ConfigList;
import com.zw.platform.domain.reportManagement.query.BigDataReportQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface OnlineReportService {
    List<ConfigList> findOnline(List<String> vehicleList) throws Exception;

    List<PositionInfo> findOnlineDay(BigDataReportQuery query) throws Exception;

    boolean export(String title, int type, HttpServletResponse res) throws Exception;

    List<OnlineReport> findOnlineList(String vehicleList, String startTime, String endTime) throws Exception;

    List<OnlineReport> onlineByF3Pass(String vehicleList, String startTime, String endTime) throws Exception;
}
