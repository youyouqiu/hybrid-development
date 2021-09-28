package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.AlarmMessageInfo;
import com.zw.platform.dto.reportManagement.AlarmDetailDto;
import com.zw.platform.dto.reportManagement.AlarmMessageDto;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 报警信息统计service
 * @author create by zhouozngbo
 */
public interface AlarmMessageStatisticService {
    /**
     * 监控对象报警信息统计(前端分页)
     * @param vehicleIds  (监控对象id,多个以","隔开)
     * @param start      (开始时间)
     * @param end        (结束时间)
     * @param alarmTypes 报警类型（多个以","隔开）
     * @return list
     * @throws Exception
     */
    List<AlarmMessageInfo> getAlarmMessageList(String vehicleIds, String startTime, String endTime, String alarmTypes)
        throws Exception;

    /**
     * 监控对象报警信息统计(前端分页)-调用paas-cloud接口方式
     * @param vehicleIds  (监控对象id,多个以","隔开)
     * @param start      (开始时间)
     * @param end        (结束时间)
     * @param alarmTypes 报警类型（多个以","隔开）
     * @return list
     * @throws Exception
     */
    List<AlarmMessageDto> getAlarmMessageListNew(String vehicleIds, String start, String end, String alarmTypes)
        throws Exception;

    /**
     * 查询报警详情
     * @param vehicleId vehicleId
     * @param alarmType alarmType
     * @return list
     */
    List<AlarmMessageInfo> getAlarmDetailMessageList(String vehicleId, String alarmType);

    /**
     * 查询报警详情-与调用paas-cloud方法相配
     * @param vehicleId vehicleId
     * @param alarmType alarmType
     * @param startTime
     * @param endTime
     * @return list
     */
    List<AlarmDetailDto> getAlarmDetailMessageListNew(String vehicleId, String alarmType, String startTime,
        String endTime) throws Exception;


    /**
     * 导出
     * @param response response
     * @param fuzzyQuery 模糊查询条件
     */
    void getExportAlarmMessageList(HttpServletResponse response, String fuzzyQuery) throws IOException;
}
