package com.zw.talkback.service.baseinfo;

import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.platform.util.common.PageGridBean;
import com.zw.talkback.domain.basicinfo.form.AttendanceForm;
import com.zw.talkback.domain.basicinfo.form.SchedulingRelationMonitor;
import com.zw.talkback.domain.basicinfo.query.AttendanceReportQuery;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

public interface AttendanceReportService {

    List<Personnel> findPeopleByScheduled(String id);

    List<AttendanceForm> getList(String monitorIds, String startTime, String endTime, String id) throws ParseException;

    List<SchedulingInfo> getScheduledList();

    PageGridBean getSummary(AttendanceReportQuery query) throws Exception;

    PageGridBean getAll(AttendanceReportQuery query) throws Exception;

    PageGridBean getDetail(AttendanceReportQuery query) throws Exception;

    void exportSummary(HttpServletResponse response) throws Exception;

    void exportAll(HttpServletResponse response) throws Exception;

    void exportDetail(HttpServletResponse response, String id) throws Exception;

    PageGridBean getAllSummary(AttendanceReportQuery query) throws Exception;

    /**
     * 获得排班管理监控对象id
     * @param scheduledInfoId 排班id
     * @return List<SchedulingRelationMonitorInfo>
     */
    List<SchedulingRelationMonitor> getSchedulingRelationMonitorInfoList(String scheduledInfoId);


}
