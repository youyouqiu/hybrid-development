package com.zw.app.service.personalCenter;

import com.zw.app.domain.personalCenter.AppOlineReportDetail;
import com.zw.platform.domain.BigDataReport.OnlineReport;

import java.util.List;

/**
 * @author lijie
 * @date 2018/12/10 16:39
 */

public interface AppOnlineReportService {

    List<OnlineReport> findOnlineList(String vehicleList, String startTime, String endTime) throws Exception;

    List<AppOlineReportDetail> getOnlineReportDetail(String vehicleList, String startTime, String endTime) throws Exception;
}
