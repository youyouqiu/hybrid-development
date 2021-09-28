package com.zw.platform.service.other.protocol.jl;

import com.zw.platform.util.common.JsonResultBean;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/6/15 17:00
 */
public interface JiLinVehicleOperationStatusService {
    /**
     * 运营状态数据列表
     * @param vehicleId 车id
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean listOperationStatus(String vehicleId) throws Exception;
}
