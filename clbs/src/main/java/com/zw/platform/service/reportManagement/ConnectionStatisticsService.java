package com.zw.platform.service.reportManagement;

import com.cb.platform.domain.query.ConnectionStaticsQuery;
import com.cb.platform.util.page.PassCloudResultBean;

/**
 * @Author: lj
 * @Description:连接信息报表service
 * @Date: create in 2021/1/8 10:26
 */
public interface ConnectionStatisticsService {

    /**
     * 查询与政府监管平台连接情况
     * @param query
     * @return
     */
    PassCloudResultBean platformList(ConnectionStaticsQuery query);

    /**
     * 查询与政府监管平台连接情况详情
     * @param query
     * @return
     */
    PassCloudResultBean platformDetailList(ConnectionStaticsQuery query);

    /**
     * 查询与车载终端连接情况
     * @param query
     * @return
     */
    PassCloudResultBean monitorList(ConnectionStaticsQuery query);

    /**
     * 查询与车载终端连接情况详情
     * @param query
     * @return
     */
    PassCloudResultBean monitorDetailList(ConnectionStaticsQuery query);
}
