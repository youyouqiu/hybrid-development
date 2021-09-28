package com.zw.platform.service.statistic;

import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 轨迹有效性报表 service
 * @author zhouzongbo on 2019/5/31 15:48
 */
public interface TrackValidReportService {

    /**
     * 查询轨迹有效性列表和图表数据
     * @param monitorId monitorId
     * @param startTime startTime
     * @param endTime endTime
     * @return JsonResultBean
     */
    JsonResultBean trackValidityReportService(String monitorId, String startTime, String endTime);

    JsonResultBean getTrackValidListByF3Pass(String monitorId, String startTime, String endTime);

    /**
     * 导出轨迹有效性分析报表
     * @param response response
     * @param simpleQueryParam 模糊查询条件
     */
    void exportTrackValidList(HttpServletResponse response, String simpleQueryParam) throws IOException;
}
