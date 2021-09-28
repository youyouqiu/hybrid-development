package com.zw.platform.service.reportManagement;

import com.zw.platform.dto.reportManagement.ParkingInfoDto;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Administrator
 */
public interface TerminalParkingReportService {

    /**
     * 获取停驶数据（大数据月表,调用paas-cloud接口）
     * @param vehicleIds 车id
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return List<ParkingInfoDto>
     * @throws Exception Exception
     */
    List<ParkingInfoDto> getStopBigDataFromPaas(String vehicleIds, String startTime, String endTime) throws Exception;

    /**
     * 导出查询数据存储值redis
     * @param vehicleId        车id
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param simpleQueryParam 模糊搜索参数
     * @param exportType       导出类型 1:停车信息 2:停驶数据(大数据月报表)
     * @return boolean
     * @throws Exception Exception
     */
    boolean exportQueryData(String vehicleId, String startTime, String endTime, String simpleQueryParam, int exportType)
        throws Exception;

    /**
     * 导出停车报表
     * @param res HttpServletResponse
     * @return boolean
     * @throws Exception Exception
     */
    boolean export(HttpServletResponse res) throws Exception;

}
