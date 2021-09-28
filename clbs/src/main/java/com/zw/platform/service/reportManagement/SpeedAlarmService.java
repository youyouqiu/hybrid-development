package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.SpeedAlarm;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface SpeedAlarmService {

    /**
     * 查询超速报警信息(用于统计)
     */
    List<SpeedAlarm> getSpeedAlarmListByF3Pass(String vehicleId, String startTime, String endTime) throws Exception;

    /**
     * 导出报警信息统计列表
     * @param type 导出类型（1:导出数据；2：导出模板）
     */
    boolean export(String title, int type, HttpServletResponse res, List<SpeedAlarm> speedAlarm) throws Exception;



}
