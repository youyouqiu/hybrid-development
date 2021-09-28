package com.zw.platform.service.reportManagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.reportManagement.TerminalPositional;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author Administrator
 */
public interface TerminalMileageReportService {

    /**
     * 组装今日数据
     * @param terminalPositionalList terminalPositionalList
     * @return JSONObject
     */
    JSONObject buildTerminalMileageData(List<TerminalPositional> terminalPositionalList);

    /**
     * 查询终端里程统计
     * @param monitorIds 监控对象id
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return JsonResultBean
     */
    JsonResultBean getTerminalMileageStatistics(String monitorIds, String startTime, String endTime);

    /**
     * 查询终端里程明细
     * @param monitorId 监控对象id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return JsonResultBean
     */
    JsonResultBean getTerminalMileageDailyDetail(String monitorId, String startTime, String endTime);

    /**
     * 导出终端里程统计
     * @param response   response
     * @param monitorId  监控对象id
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param queryParam 查询参数
     * @throws IOException IOException
     */
    void exportTerminalMileageStatistics(HttpServletResponse response, String monitorId, String startTime,
        String endTime, String queryParam) throws IOException;

    /**
     * 导出终端里程每日明细
     * @param response  response
     * @param monitorId 开始时间
     * @param startTime 结束时间
     * @param endTime   查询参数
     * @throws IOException IOException
     */
    void exportTerminalMileageDailyDetail(HttpServletResponse response, String monitorId, String startTime,
        String endTime) throws IOException;
}
