package com.zw.platform.service.reportManagement;

import com.alibaba.fastjson.JSONObject;
import com.zw.platform.domain.BigDataReport.PositionInfo;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;
import java.util.Set;


public interface BigDataReportService {

    /**
     * 查询里程小于指定车辆里程的数量
     */
    int findSmallMileCount(Set<String> vehicleList, Double mile, Long startTime, Long endTime);

    /**
     * 根据车辆id和车牌查询单车每月详细
     */
    PositionInfo getMouthSumByVehicle(String vehicleId, String brand, Long startTime, Long endTime);

    /**
     * 根据车辆id和车牌查询单车单日（30天）详细
     */
    List<PositionInfo> getDaysByVehicle(String vehicleId, String brand, Long startTime, Long endTime);

    /**
     * 根据车辆id查询当月的里程统计数据
     */
    JsonResultBean getMouthMileDataByVehicleId(String vehicleId, String brand, String groupId);

    /**
     * 查询大数据报表月报表组装大数据报表数据
     */
    JSONObject queryBigDataValue(String groupId);

    /**
     * 查询大数据报表月报表组装里程月统计数据
     */
    JSONObject queryMonitorMile(String vehicleId, String groupId);
}
