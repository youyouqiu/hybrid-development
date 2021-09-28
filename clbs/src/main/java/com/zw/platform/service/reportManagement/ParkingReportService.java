package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.ParkingInfo;
import com.zw.platform.dto.reportManagement.ParkingInfoDto;
import com.zw.platform.util.common.BusinessException;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

public interface ParkingReportService {

    /**
     * 查询停车信息
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return List<ParkingInfo>
     * @throws ParseException
     * @throws BusinessException
     * @author hujun
     */
    List<ParkingInfo> getStopData(List<String> vehicleIds, String startTime, String endTime) throws Exception;

    /**
     * 获取停驶数据（大数据月表）
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @param isAppSearch
     * @return
     * @throws Exception
     */
    List<ParkingInfo> getStopBigData(List<String> vehicleIds, String startTime, String endTime, boolean isAppSearch)
        throws Exception;

    /**
     * 获取停驶数据（大数据月表,调用paas-cloud接口）
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @param isAppSearch
     * @return
     * @throws Exception
     */
    List<ParkingInfoDto> getStopBigDataFromPaas(String vehicleIds, String startTime, String endTime,
        boolean isAppSearch) throws Exception;

    /**
     * 导出停车报表
     * @param title
     * @param type  导出类型（1:导出数据；2：导出模板）
     * @param res
     * @return
     */
    boolean export(String title, int type, HttpServletResponse res) throws Exception;

    List<ParkingInfo> findSingleMonitorParkingList(List<String> monitorIds, String startTime, String endTime)
        throws Exception;
}
