package com.sx.platform.service.sxReportManagement;

import com.sx.platform.domain.sxReport.OffLineReport;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface OffLineReportService {
    /**
     * 查询离线报表信息
     * @param vehicleList
     * @param day
     * @return
     * @throws Exception
     */
    List<OffLineReport> getList(String vehicleList, Integer day) throws Exception;

    /**
     * 离线查询报表导出
     * @param res
     * @param simpleQueryParam
     * @return
     * @throws Exception
     */
    boolean export(HttpServletResponse res, String simpleQueryParam) throws Exception;

    /**
     * 两客一危-离线查询报表导出
     * @param res res
     * @param simpleQueryParam simpleQueryParam
     * @return
     * @throws Exception
     */
    boolean exportForLkyw(HttpServletResponse res, String simpleQueryParam) throws Exception;

}
