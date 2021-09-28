package com.cb.platform.service;

import com.cb.platform.domain.query.FatigueDrivingQuery;
import com.cb.platform.domain.query.FatigueDrivingVehQuery;
import com.cb.platform.util.page.PassCloudResultBean;

public interface FatigueDrivingService {
    /**
     * 获取企业疲劳驾驶分页数据列表
     * @return
     */
    PassCloudResultBean getOrgDataList(FatigueDrivingQuery query);

    /**
     * 获取车辆疲劳驾驶分页数据列表
     * @return
     */
    PassCloudResultBean getVehDataList(FatigueDrivingVehQuery query);

    /**
     * 获取企业疲劳驾驶图形数据
     * @return
     */
    PassCloudResultBean getOrgGraphicsData(FatigueDrivingQuery query);

    /**
     * 获取企业疲劳驾驶图形数据
     * @return
     */
    PassCloudResultBean getVehGraphicsData(FatigueDrivingVehQuery query);

    /**
     * 获取企业疲劳具体单个企业排行信息
     * @return
     */
    PassCloudResultBean getOrgRankData(FatigueDrivingQuery query);

    /**
     * 获取监控对象疲劳排行信息
     * @return
     */
    PassCloudResultBean getVehRankData(FatigueDrivingVehQuery query);

    /**
     * 获取企业监控对象疲劳详情列表
     * @return
     */
    PassCloudResultBean getOrgDetailData(FatigueDrivingQuery query);

    /**
     * 获取监控对象疲劳详情列表
     * @return
     */
    PassCloudResultBean getVehDetailData(FatigueDrivingVehQuery query);

}
