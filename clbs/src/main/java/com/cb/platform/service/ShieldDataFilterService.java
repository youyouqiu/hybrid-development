package com.cb.platform.service;

import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhangsq
 */
public interface ShieldDataFilterService {

    /**
     * 连续性分析报表
     * @param monitorId   车辆id
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param breakSecond 中断时长(s)
     * @param breakDistance
     * @return JsonResultBean
     */
    JsonResultBean getContinuityAnalysisList(String monitorId, String startTime, String endTime, Integer breakSecond,
        Double breakDistance);

    /**
     * 导出连续性分析报表
     * @param response    response
     * @param monitorId   车辆id
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param breakSecond 中断时长(s)
     * @param breakDistance
     * @throws IOException IOException
     */
    void exportContinuityAnalysisList(HttpServletResponse response, String monitorId, String startTime, String endTime,
        Integer breakSecond, Double breakDistance) throws IOException;

}
