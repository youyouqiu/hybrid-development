package com.zw.platform.service.reportManagement;

import com.zw.platform.domain.reportManagement.VehicleOperationStatusReport;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 车辆运营状态server
 */
public interface VehicleOperationStatusService {
    /**
     * 根据车辆id查询车辆运营状态等信息
     */
    List<VehicleOperationStatusReport> getVehicleOperationInfoById(String vehicleIds) throws Exception;

    /**
     * 导出车辆运营状态列表内容
     */
    void exportVehicleOperationData(HttpServletResponse response, String param) throws Exception;
}
