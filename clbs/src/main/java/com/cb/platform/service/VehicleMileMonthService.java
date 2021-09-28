package com.cb.platform.service;

import com.cb.platform.vo.VehicleMileDetailVO;
import com.cb.platform.vo.VehicleMileMonthVO;
import com.zw.platform.util.common.JsonResultBean;

import java.util.List;

/**
 * @author zhangsq
 * @date 2018/5/10 16:35
 */
public interface VehicleMileMonthService {

    /**
     * 获取车辆日里程统计
     * @param vehicleIds       车辆id
     * @param month            月份 yyyy-MM
     * @param simpleQueryParam 模糊搜索条件
     * @return List<VehicleMileMonthVO>
     * @throws Exception Exception
     */
    List<VehicleMileMonthVO> getVehicleMileMonths(String vehicleIds, String month, String simpleQueryParam)
        throws Exception;

    /**
     * 导出车辆月统计报表
     * @param vehicleIds       车辆id
     * @param month            月份 yyyy-MM
     * @param simpleQueryParam 模糊搜索条件
     * @return JsonResultBean
     */
    JsonResultBean exportVehicleMonth(String vehicleIds, String month, String simpleQueryParam);

    /**
     * 获取车辆里程统计报表详情列表
     * @param vehicleIds       车辆id
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param simpleQueryParam 模糊搜索条件
     * @return List<VehicleMileDetailVO>
     * @throws Exception Exception
     */
    List<VehicleMileDetailVO> getDetailList(String vehicleIds, String startTime, String endTime,
        String simpleQueryParam) throws Exception;

    /**
     * 导出车辆里程明细报表
     * @param vehicleIds       车辆id
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param simpleQueryParam 模糊搜索条件
     * @return JsonResultBean
     */
    JsonResultBean exportVehicleMileDetail(String vehicleIds, String startTime, String endTime,
        String simpleQueryParam);
}
