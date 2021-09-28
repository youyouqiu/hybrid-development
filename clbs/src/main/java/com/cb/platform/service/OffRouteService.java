package com.cb.platform.service;

import com.cb.platform.dto.MonitorOffRouteDetailPageQuery;
import com.cb.platform.dto.MonitorOffRoutePageQuery;
import com.cb.platform.dto.MonitorOffRouteQuery;
import com.cb.platform.dto.OrgOffRoutePageQuery;
import com.cb.platform.dto.OrgOffRouteQuery;
import com.zw.platform.util.common.BusinessException;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.common.PageGridBean;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/3/23 16:59
 */
public interface OffRouteService {
    /**
     * 获取企业路线偏离统计数据列表
     * @param query query
     * @return PageGridBean
     * @throws Exception Exception
     */
    PageGridBean getDataList(OrgOffRoutePageQuery query) throws Exception;

    /**
     * 导出路线偏离企业统计数据列表
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportOrgDataList(OrgOffRoutePageQuery query);

    /**
     * 获取企业图形统计数据
     * @param query query
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getChartStatisticsData(OrgOffRouteQuery query) throws Exception;

    /**
     * 获取企业路线偏离报警明细基本信息和报警趋势
     * @param query query
     * @return JsonResultBean
     * @throws Exception Exception
     */
    JsonResultBean getOrgDetailBasicInfoAndTrend(OrgOffRouteQuery query) throws Exception;

    /**
     * 获取企业路线偏离报警明细监控对象数据列表
     * @param query query
     * @return PageGridBean
     * @throws Exception Exception
     */
    PageGridBean getOrgDetailMonitorList(OrgOffRoutePageQuery query) throws Exception;

    /**
     * 导出企业路线偏离报警明细监控对象数据列表
     * @param query query
     * @return JsonResultBean
     */
    JsonResultBean exportOrgDetailMonitorList(OrgOffRoutePageQuery query);

    /**
     * 获取车辆路线偏离统计数据列表
     */
    PageGridBean getMonitorDataList(MonitorOffRoutePageQuery query) throws Exception;

    /**
     * 导出车辆路线偏离统计数据列表
     */
    JsonResultBean exportMonitorDataList(MonitorOffRoutePageQuery query);

    /**
     * 获取车辆图形统计数据
     */
    JsonResultBean getMonitorChartStatisticsData(MonitorOffRouteQuery query) throws BusinessException;

    /**
     * 获取车辆路线偏离报警明细列表
     */
    PageGridBean getMonitorDetailList(MonitorOffRouteDetailPageQuery query) throws Exception;

    /**
     * 导出车辆路线偏离报警明细列表
     */
    JsonResultBean exportMonitorDetailList(MonitorOffRouteDetailPageQuery query);

}
