package com.cb.platform.service;

import com.cb.platform.dto.report.sichuan.ContinuousSpeedStatisticsQuery;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

/**
 * @author Administrator
 */
public interface ContinuousSpeedStatisticsService {

    /**
     * 持续超速统计-持续超速道路运输企业统计表
     * @param query query
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getContinuousSpeedOrgReport(ContinuousSpeedStatisticsQuery query) throws Exception;

    /**
     * 持续超速统计-持续超速道路运输企业统计表-离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportContinuousSpeedOrgReport(ContinuousSpeedStatisticsQuery query);

    /**
     * 持续超速统计-持续超速车辆统计表
     * @param query query
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getContinuousSpeedVehicleReport(ContinuousSpeedStatisticsQuery query) throws Exception;

    /**
     * 持续超速统计-持续超速车辆统计表-离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportContinuousSpeedVehicleReport(ContinuousSpeedStatisticsQuery query);

    /**
     * 持续超速统计-持续超速车辆明细表
     * @param query query
     * @return JsonResultBean
     * @throws Exception Exception
     */
    PageGridBean getContinuousSpeedVehicleDetailReport(ContinuousSpeedStatisticsQuery query) throws Exception;

    /**
     * 持续超速统计-持续超速车辆明细表-离线导出
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportContinuousSpeedVehicleDetailReport(ContinuousSpeedStatisticsQuery query);
}
