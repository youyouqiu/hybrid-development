package com.sx.platform.service.sxReportManagement;


import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;


public interface SxSpeedViolationReportService {


    JsonResultBean getListFromPaas(String vehicleList, String startTime, String endTime) throws Exception;

    /**
     * 超速违章明细报表导出
     * @param title
     * @param type
     * @param res
     * @return
     * @throws Exception
     */
    boolean export(String title, int type, HttpServletResponse res)
        throws Exception;
}
