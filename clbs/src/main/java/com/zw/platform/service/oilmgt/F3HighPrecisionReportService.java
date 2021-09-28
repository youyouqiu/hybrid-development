package com.zw.platform.service.oilmgt;

import com.zw.platform.domain.oil.F3HighPrecisionReport;
import com.zw.platform.domain.oil.VoltageInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/***
 @Author zhengjc
 @Date 2019/11/15 15:18
 @Description f3高精度报表service
 @version 1.0
 **/
public interface F3HighPrecisionReportService {

    /**
     * 获取f3高精度报表
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     */
    List<F3HighPrecisionReport> getF3HighPrecisionReport(String vehicleId, String startTime, String endTime);

    /**
     * 导出f3高精度报表
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @param response
     */
    void exportF3HighPrecisionReport(String vehicleId, String startTime, String endTime, HttpServletResponse response)
        throws IOException;

    VoltageInfo getVoltageInfo(String vehicleId);
}
