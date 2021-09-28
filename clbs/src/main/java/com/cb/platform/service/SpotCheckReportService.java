package com.cb.platform.service;

import com.cb.platform.domain.GroupSpotCheckVehicleNumberCont;
import com.zw.platform.util.common.JsonResultBean;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * @author penghj
 * @version 1.0
 * @date 2019/2/21 10:30
 */
public interface SpotCheckReportService {

    /**
     * 查询车辆抽查明细信息
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JsonResultBean getVehicleSpotCheckDetailList(String vehicleIds, String startTime, String endTime) throws Exception;

    /**
     * 导出车辆抽查明细表
     * @param response
     * @param simpleQueryParam
     * @throws Exception
     */
    void exportVehicleSpotCheckDetail(HttpServletResponse response, String simpleQueryParam) throws Exception;

    /**
     * 用户抽查车辆数量及百分比统计报表
     * @param userIds
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JsonResultBean getUserSpotCheckNumberAndPercentageList(String userIds, String startTime, String endTime)
        throws Exception;

    /**
     * 导出用户抽查车辆数量及百分比统计报表
     * @param response
     * @param simpleQueryParam
     * @throws Exception
     */
    void exportUserSpotCheckNumberAndPercentage(HttpServletResponse response, String simpleQueryParam) throws Exception;

    /**
     * 车辆抽查数量统计表
     * @param vehicleIds
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    JsonResultBean getVehicleSpotCheckNumberCountList(String vehicleIds, String startTime, String endTime)
        throws Exception;

    /**
     * 导出车辆抽查数量统计表
     * @param response
     * @param simpleQueryParam
     * @throws Exception
     */
    void exportVehicleSpotCheckNumberCountList(HttpServletResponse response, String simpleQueryParam) throws Exception;

    /**
     * 道路运输企业抽查车辆数量统计
     */
    List<GroupSpotCheckVehicleNumberCont> getGroupSportCheckVehicleData(String groupIds, String startTime,
        String endTime) throws Exception;

    /**
     * 导出道路运输企业抽查车辆数量统计数据
     */
    void exportGroupSpotCheckVehicleNumberData(HttpServletResponse response, String fuzzyParam) throws Exception;
}
