package com.zw.platform.service.reportManagement;

import com.cb.platform.domain.query.PointQuery;
import com.cb.platform.util.page.PassCloudResultBean;

/**
 * @Author: zjc
 * @Description:途经点service
 * @Date: create in 2021/1/8 10:26
 */
public interface PointService {

    /**
     * 查询企业维度数据
     * @param query
     * @return
     */
    PassCloudResultBean getOrgData(PointQuery query);

    /**
     * 查询监控对象维度数据
     * @param query
     * @return
     */
    PassCloudResultBean getMonitorData(PointQuery query);

    /**
     * 查询行政区划维度数据
     * @param query
     * @return
     */
    PassCloudResultBean getMonitorDetailData(PointQuery query);

}
