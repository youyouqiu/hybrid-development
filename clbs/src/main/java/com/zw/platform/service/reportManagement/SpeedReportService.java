package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.SpeedReport;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface SpeedReportService {
    /**
     * 查询超速报警报表信息(用于统计)
     * @param vehicleList (车牌号,多个以逗号隔开)
     * @param vehicleId   (车辆id,多个以逗号隔开)
     * @param startTime   (开始时间)
     * @param endTime     (结束时间)
     * @return List<SpeedAlarm>
     */
    List<SpeedReport> getSpeedAlarm(String vehicleList, String vehicleId, String startTime, String endTime)
        throws Exception;

    /**
     * 查询车辆超速报警数据(大数据月表)
     * @param vehicleId
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    List<SpeedReport> getSpeedReportBigData(String vehicleId, String startTime, String endTime) throws Exception;

    /**
     * 导出报警报表
     * @param title
     * @param type  导出类型（1:导出数据；2：导出模板）
     * @param res
     * @return
     */
    boolean export(String title, int type, HttpServletResponse res, List<SpeedReport> speedReport) throws Exception;

    /**
     * 获得超速报表列表
     * @param monitorIds 监控对象id
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return JsonResultBean
     */
    JsonResultBean getSpeedingReportList(String monitorIds, String startTime, String endTime);

    /**
     * 导出超速报表列表
     * @param response   response
     * @param monitorIds 监控对象id
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @throws IOException IOException
     */
    void exportSpeedingReportList(HttpServletResponse response, String monitorIds, String startTime, String endTime)
        throws IOException;
}
