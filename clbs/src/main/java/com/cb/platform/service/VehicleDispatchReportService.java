package com.cb.platform.service;


import com.cb.platform.domain.EnterpriseDispatch;
import com.cb.platform.domain.VehicleScheduler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Service
public interface VehicleDispatchReportService {
    /**
     * 车辆调度信息道路运输企业统计月报表查询接口
     * @param groupList
     * @param month
     * @return
     * @throws Exception
     */
    List<EnterpriseDispatch> getEnterpriseList(String groupList, String month) throws Exception;

    /**
     * 车辆调度信息统计月报表查询接口
     * @param vehicleList
     * @param month
     * @return
     * @throws Exception
     */
    List<EnterpriseDispatch> getVehicleList(String vehicleList, String month) throws Exception;

    /**
     * 车辆调度信息明细表查询接口
     * @param vehicleList
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    List<VehicleScheduler> getDetailList(String vehicleList, String startTime, String endTime) throws Exception;

    /**
     * 车辆调度信息道路运输企业统计月报表导出接口
     * @param title
     * @param type
     * @param res
     * @return
     * @throws Exception
     */
    boolean exportEnterpriseList(String title, int type, HttpServletResponse res) throws Exception;

    /**
     * 车辆调度信息统计月报表导出接口
     * @param title
     * @param type
     * @param res
     * @return
     * @throws Exception
     */
    boolean exportVehicleList(String title, int type, HttpServletResponse res) throws Exception;

    /**
     * 车辆调度信息明细表导出接口
     * @param title
     * @param type
     * @param res
     * @return
     * @throws Exception
     */
    boolean exportDetailList(String title, int type, HttpServletResponse res) throws Exception;

}
