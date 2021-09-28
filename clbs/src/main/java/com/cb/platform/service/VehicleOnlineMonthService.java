package com.cb.platform.service;

import com.cb.platform.dto.VehicleOnlineRateQuery;
import com.cb.platform.vo.VehicleOnlineVO;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

import java.util.List;

/**
 * @author zhangsq
 * @date 2018/5/3 13:54
 */
public interface VehicleOnlineMonthService {

    /**
     * 车辆在线率道路运输企业统计月报表
     * @param query query
     * @return PageGridBean
     * @throws Exception Exception
     */
    PageGridBean getVehicleOnlineRateOrgMonthReport(VehicleOnlineRateQuery query) throws Exception;

    /**
     * 车辆在线率统计月报表
     * @param query query
     * @return PageGridBean
     * @throws Exception Exception
     */
    PageGridBean getVehicleOnlineRateVehicleMonthReport(VehicleOnlineRateQuery query) throws Exception;

    /**
     * 车辆在线率道路运输企业统计月报表离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportVehicleOnlineRateOrgMonthReport(VehicleOnlineRateQuery query);

    /**
     * 车辆在线率统计月报表离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportVehicleOnlineRateVehicleMonthReport(VehicleOnlineRateQuery query);

    /**
     * 车辆在线明细表离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportVehicleOnlineDetails(VehicleOnlineRateQuery query);

    /**
     * 车辆在线明细表
     * @param query query
     * @return List<VehicleOnlineVO>
     * @throws Exception Exception
     */
    List<VehicleOnlineVO> getVehicleOnlineDetails(VehicleOnlineRateQuery query) throws Exception;

}
