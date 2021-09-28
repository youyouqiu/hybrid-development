package com.zw.lkyw.service.sendMessageReport;

import com.zw.lkyw.domain.sendMessageReport.DetailQuery;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author denghuabing on 2019/12/30 16:02
 */
public interface SendMessageReportSevice {

    JsonResultBean getList(String vehicleIds, String startTime, String endTime);

    void export(String vehicleIds, String startTime, String endTime, HttpServletResponse response);

    JsonResultBean getDetail(DetailQuery query, HttpServletResponse response);

    void exportDetail(DetailQuery query, HttpServletResponse response) throws IOException;
}
