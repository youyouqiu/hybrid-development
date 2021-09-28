package com.zw.platform.service.statistic;

import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;

/**
 * @author penghj
 * @version 1.0
 * @date 2018/12/25 9:38
 */
public interface LatestLocationInfoStatisticsService {

    /**
     * 查询最新位置信息
     * @param monitorIdStr 监控对象id
     * @param queryTime    查询时间
     * @return JsonResultBean
     */
    JsonResultBean getLatestLocationInfoByF3Pass(String monitorIdStr, String queryTime);

    /**
     * 导出最新位置信息报表
     * @param response         response
     * @param simpleQueryParam simpleQueryParam
     * @throws Exception Exception
     */
    void exportLatestLocationInfo(HttpServletResponse response, String simpleQueryParam) throws Exception;
}
