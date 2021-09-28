package com.zw.platform.service.reportManagement;

import com.cb.platform.domain.query.VehicleOnlineTimeQuery;
import com.cb.platform.util.page.PassCloudResultBean;

/**
 * @Author: zjc
 * @Description:车辆在线时长service
 * @Date: create in 2021/1/8 10:26
 */
public interface VehicleOnlineTimeService {

    /**
     * 查询企业维度数据
     * @param query
     * @return
     */
    PassCloudResultBean getOrgData(VehicleOnlineTimeQuery query);

    /**
     * 查询监控对象维度数据
     * @param query
     * @return
     */
    PassCloudResultBean getMonitorData(VehicleOnlineTimeQuery query);

    /**
     * 查询行政区划维度数据
     * @param query
     * @return
     */
    PassCloudResultBean getDivisionData(VehicleOnlineTimeQuery query);

}
