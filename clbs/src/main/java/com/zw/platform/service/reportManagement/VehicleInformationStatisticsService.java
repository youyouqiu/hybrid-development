package com.zw.platform.service.reportManagement;

import com.cb.platform.domain.query.VehInformationStaticsQuery;
import com.cb.platform.util.page.PassCloudResultBean;

/**
 * @Author: zjc
 * @Description:车辆信息报表service
 * @Date: create in 2021/1/8 10:26
 */
public interface VehicleInformationStatisticsService {

    /**
     * 查询企业月图形展示数据
     * @param query
     * @return
     */
    PassCloudResultBean getOrgGraph(VehInformationStaticsQuery query);

    /**
     * 查询企业列表展示数据
     * @param query
     * @return
     */
    PassCloudResultBean getOrgList(VehInformationStaticsQuery query);

    /**
     * 查询企业月图形展示详情数据
     * @param query
     * @return
     */
    PassCloudResultBean getOrgDetailGraph(VehInformationStaticsQuery query);

    /**
     * 查询企业列表展示详情数据
     * @param query
     * @return
     */
    PassCloudResultBean getOrgDetailList(VehInformationStaticsQuery query);
}
