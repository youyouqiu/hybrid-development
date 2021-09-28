package com.zw.platform.service.netaccessproveforward;

import com.zw.platform.dto.netaccessproveforward.NetAccessProveForwardVehicleQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

/**
 * @author penghj
 * @version 1.0
 * @date 2020/7/20 9:08
 */
public interface ZhejiangNetAccessProveForwardService {
    /**
     * 新增入网证明转发
     * @param vehicleIds 车辆id 逗号分隔
     * @param ipAddress  ipAddress
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean addNetAccessProveForward(String vehicleIds, String ipAddress) throws Exception;

    /**
     * 获得入网证明转发
     * @return JsonResultBean
     * @param query query
     */
    PageGridBean getList(NetAccessProveForwardVehicleQuery query);

    /**
     * 删除入网证明转发
     * @param vehicleIds 车辆id 逗号分隔
     * @param ipAddress  ipAddress
     * @return JsonResultBean
     */
    JsonResultBean deleteNetAccessProveForward(String vehicleIds, String ipAddress);
}
