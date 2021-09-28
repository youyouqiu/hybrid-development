package com.zw.platform.service.reportManagement;

import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface OutAreaTotalTimeReportService {

    /**
     * 查询出区划累计时长统计列表
     * @param monitorIds  所选监控对象ID
     * @param totalTime 累计时间
     * @param endTime   结束时间
     * @return JsonResultBean
     */
    JsonResultBean getOutAreaDurationStatisticsList(String monitorIds, Integer totalTime, String endTime);

    /**
     * 导出出区划累计时长统计
     * @param response   response
     * @param queryParam 查询参数
     * @throws IOException IOException
     */
    void exportOutAreaDurationStatistics(HttpServletResponse response, String queryParam) throws IOException;

}
