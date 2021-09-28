package com.zw.talkback.repository.mysql;

import com.zw.platform.domain.basicinfo.Personnel;
import com.zw.platform.domain.scheduledmanagement.SchedulingInfo;
import com.zw.talkback.domain.basicinfo.form.SchedulingRelationMonitor;

import java.util.List;

public interface AttendanceReportMysqlDao {

    List<Personnel> findPeopleByScheduled(String id);

    /**
     * 获得排班关联监控对象
     * @param scheduledInfoId 排班id
     * @return List<SchedulingRelationMonitorInfo>
     */
    List<SchedulingRelationMonitor> getSchedulingRelationMonitorInfoListById(String scheduledInfoId);

    List<SchedulingInfo> getScheduledList(List<String> list);
}
