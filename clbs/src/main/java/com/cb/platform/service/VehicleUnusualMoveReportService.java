package com.cb.platform.service;

import com.cb.platform.dto.report.sichuan.VehicleAbnormalDrivingReportQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

/**
 * 车辆异动报表
 */
public interface VehicleUnusualMoveReportService {
    /**
     * 车辆异动道路运输企业统计报表 列表
     * @param query query
     * @return JsonResultBean
     * @throws Exception JsonResultBean
     */
    JsonResultBean getVehicleAbnormalDrivingOrgReport(VehicleAbnormalDrivingReportQuery query) throws Exception;

    /**
     * 车辆异动道路运输企业统计报表 离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportVehicleAbnormalDrivingOrgReport(VehicleAbnormalDrivingReportQuery query);

    /**
     * 车辆异常行驶统计表 列表
     * @param query query
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getVehicleAbnormalDrivingVehicleReport(VehicleAbnormalDrivingReportQuery query) throws Exception;

    /**
     * 车辆异常行驶统计表 离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportVehicleAbnormalDrivingVehicleReport(VehicleAbnormalDrivingReportQuery query);

    /**
     * 车辆异常行驶明细 列表
     * @param query query
     * @return PageGridBean
     * @throws Exception Exception
     */
    PageGridBean getVehicleAbnormalDrivingVehicleDetailReport(VehicleAbnormalDrivingReportQuery query) throws Exception;

    /**
     * 车辆异常行驶明细 离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportVehicleAbnormalDrivingVehicleDetailReport(VehicleAbnormalDrivingReportQuery query);
}
