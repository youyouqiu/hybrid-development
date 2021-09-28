package com.sx.platform.service.sxReportManagement;

import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author zhangsq
 * @date 2018/3/12 11:10
 */
public interface ShiftDataReportService {

    JsonResultBean getListFromPaas(String band, String startTime, String endTime) throws Exception;

    /**
     * 导出
     *
     * @param title
     * @param type
     * @param res
     * @return
     * @throws IOException
     */
    boolean export(String title, int type, HttpServletResponse res) throws IOException;
}
