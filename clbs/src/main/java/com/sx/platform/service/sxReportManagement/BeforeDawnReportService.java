package com.sx.platform.service.sxReportManagement;

import com.zw.platform.util.common.JsonResultBean;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;


@Service
public interface BeforeDawnReportService {


    JsonResultBean getListFromPaas(String band, String startTime, String endTime) throws Exception;

    /**
     * 凌晨2-5点报表导出
     * @param title
     * @param type
     * @param res
     * @return
     * @throws Exception
     */
    boolean export(String title, int type, HttpServletResponse res)
        throws Exception;

}
