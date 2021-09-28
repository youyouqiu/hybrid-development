package com.sx.platform.service.sxReportManagement;

import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zhangsq
 * @date 2018/3/12 11:09
 */
public interface AbnormalTrajectoryReportService {

    JsonResultBean getAbnormalTrajectoryFromPaas(String band, String startTime, String endTime) throws Exception;

    boolean export(String o, int i, HttpServletResponse res) throws Exception;
}
