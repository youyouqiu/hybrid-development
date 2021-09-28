package com.zw.talkback.service.baseinfo;

import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.domain.basicinfo.query.AttendanceReportQuery;

import javax.servlet.http.HttpServletResponse;

public interface ScheduledMileageReportService {

    PageGridBean getSummary(AttendanceReportQuery query) throws Exception;

    PageGridBean getAll(AttendanceReportQuery query) throws Exception;

    PageGridBean getDetail(AttendanceReportQuery query) throws Exception;

    void exportSummary(HttpServletResponse response) throws Exception;

    void exportAll(HttpServletResponse response) throws Exception;

    void exportDetail(HttpServletResponse response, String id) throws Exception;

    PageGridBean getAllSummary(AttendanceReportQuery query) throws Exception;
}
