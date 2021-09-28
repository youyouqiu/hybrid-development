package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.DrivingRecordInfo;
import com.zw.platform.domain.reportManagement.query.DrivingRecordInfoQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 行驶记录仪报表server
 */
public interface DrivingRecordReportService {
    /**
     * 增加行驶记录仪采集数据
     *
     * @param monitorId 监控对象id
     * @param cw        命令字
     * @param msgAck    应答流水号
     */
    void addDrivingRecordInfo(String monitorId, String cw, int msgAck) throws Exception;

    /**
     * 根据车id更新行驶记录仪采集数据
     */
    void updateDrivingRecordInfoByMonitorId(DrivingRecordInfoQuery query) throws Exception;

    /**
     * 获取行驶记录仪采集数据记录
     */
    List<DrivingRecordInfo> getDrivingRecordCollection(String monitorId, String queryStartTime, String queryEndTime)
        throws Exception;

    /**
     * 导出行驶记录仪列表数据
     */
    void exportDrivingRecord(HttpServletResponse response, String fuzzyParam) throws Exception;
}
