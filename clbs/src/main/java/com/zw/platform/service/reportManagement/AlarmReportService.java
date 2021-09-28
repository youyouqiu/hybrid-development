package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.AlarmInformation;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Administrator
 */
public interface AlarmReportService {
    /**
     * 查询车辆报警信息(用于统计)
     * @param vehicleId (车id,多个以","隔开)
     * @param start     (开始时间)
     * @param end       (结束时间)
     * @return List<AlarmInformation> List<AlarmInformation>
     * @throws Exception Exception
     */
    List<AlarmInformation> getAlarmInformation(String vehicleId, String start, String end) throws Exception;

    /**
     * @param vehicleId        全部车辆id
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param simpleQueryParam 模糊搜索
     * @param exportType       exportType
     * @return boolean
     * @throws Exception Exception
     */
    boolean exportQueryList(String vehicleId, String startTime, String endTime, String simpleQueryParam, int exportType)
        throws Exception;

    /**
     * 导出报警信息统计列表
     * @param httpResponse
     */
    void export(HttpServletResponse httpResponse) throws Exception;

    List<AlarmInformation> getAlarmData(String vehicleIds, String startTime, String endTime) throws Exception;
}
