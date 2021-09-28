package com.zw.app.service.personalCenter;

import com.zw.app.domain.personalCenter.AppSpeedReportDetail;
import com.zw.platform.domain.reportManagement.SpeedReport;

import java.util.List;

/**
 * @author lijie
 * @date 2018/12/10 16:39
 */
public interface AppSpeedReportService {

    List<SpeedReport> getSpeedReport(String vehicleList, String startTime, String endTime, int type) throws Exception;

    List<AppSpeedReportDetail> getSpeedReportDetail(String vehicleId, String startTime, String endTime, int type)
            throws Exception;
}
