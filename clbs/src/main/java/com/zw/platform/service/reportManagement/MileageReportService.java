package com.zw.platform.service.reportManagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.reportManagement.MileageReport;
import com.zw.platform.domain.reportManagement.PositionalDetail;
import com.zw.platform.domain.reportManagement.TravelDetail;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author chenjingyu
 */
public interface MileageReportService {
    /**
     * 根据id查询一段时间内的总油量和总里程
     */
    List<MileageReport> findMileageById(List<String> moIds, long startTimeL, long endTimeL) throws Exception;

    /**
     * 获取分组里程统计数据
     */
    JSONObject getAssignMileageData(String assignNames);

    /**
     * 导出数据到excle文件
     */
    boolean export(String title, int type, HttpServletResponse response, List<MileageReport> fcsList, String fuzzyQuery)
        throws Exception;

    List<PositionalDetail> findPositionalDetailsBy(String nowDay, List<String> vehicleIds);

    void getTravelOrStopData(List<TravelDetail> resultList, List<PositionalDetail> positionalList, int mark,
        int statusChangeTimes, boolean isAppSearch);

    /**
     * 获取监控对象的行驶or停止详情
     * @param singleMonitorTravelDetails singleMonitorTravelDetails
     * @param travelDetails              travelDetails
     * @param positionalList             positionalList
     * @param mark                       0:行驶
     * @param statusChangeTimes          多少次更改状态
     * @param isAppSearch                true: app查询; false: 平台
     */
    void getMonitorsTravelDetail(List<TravelDetail> singleMonitorTravelDetails, List<TravelDetail> travelDetails,
        List<PositionalDetail> positionalList, int mark, int statusChangeTimes, boolean isAppSearch);

    List<MileageReport> getSingleMonitorMileageData(List<String> monitorIds, String startTime, String endTime)
        throws Exception;

    /**
     * 获得行驶里程统计
     * @param monitorIds  监控对象id 逗号分隔
     * @param startTime   开始日期
     * @param endTime     结束日期
     * @param isAppSearch 是否是App搜索
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getDrivingMileageStatistics(String monitorIds, String startTime, String endTime, boolean isAppSearch)
        throws Exception;

    /**
     * 行驶里程明细
     * @param monitorId 监控对象id
     * @param startTime 开始日期(yyyy-MM-dd)
     * @param endTime   结束日期(yyyy-MM-dd)
     * @return JsonResultBean
     */
    JsonResultBean getDrivingMileageDetails(String monitorId, String startTime, String endTime);

    /**
     * 行驶里程位置明细
     * @param monitorId 监控对象id
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return JsonResultBean
     */
    JsonResultBean getDrivingMileageLocationDetails(String monitorId, String startTime, String endTime,
        Integer reissue);

    /**
     * 部标监管报表-行驶里程报表 导出
     * @param response   response
     * @param monitorId  监控对象id
     * @param startTime  开始日期
     * @param endTime    结束日期
     * @param exportType 2:行驶统计;3:行驶明细;4:位置明细;
     * @param queryParam 查询参数
     * @throws Exception Exception
     */
    void exportDrivingMileage(HttpServletResponse response, String monitorId, String startTime, String endTime,
        Integer exportType, String queryParam, Integer reissue) throws Exception;
}
