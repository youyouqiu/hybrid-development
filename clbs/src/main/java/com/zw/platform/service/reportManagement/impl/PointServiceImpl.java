package com.zw.platform.service.reportManagement.impl;

import com.cb.platform.domain.query.PointQuery;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.service.reportManagement.PointService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.springframework.stereotype.Service;

/**
 * @Author: zjc
 * @Description:途经点service
 * @Date: create in 2021/1/19 17:56
 */
@Service
public class PointServiceImpl implements PointService {

    @Override
    public PassCloudResultBean getOrgData(PointQuery query) {
        if (StrUtil.isBlank(query.getOrganizationIds())) {
            return PassCloudResultBean.getDefaultPageInstance();
        }
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.POINT_ORG_LIST_URL, query.getOrgParam());
        return PassCloudResultBean.getPageInstance(queryResult);
    }

    @Override
    public PassCloudResultBean getMonitorData(PointQuery query) {
        if (StrUtil.isBlank(query.getMonitorIds())) {
            return PassCloudResultBean.getDefaultPageInstance();
        }
        String queryResult = HttpClientUtil.send(PaasCloudUrlEnum.POINT_MONITOR_LIST_URL, query.getMonitorParam());
        return PassCloudResultBean.getPageInstance(queryResult);
    }

    @Override
    public PassCloudResultBean getMonitorDetailData(PointQuery query) {
        if (StrUtil.isBlank(query.getMonitorIds())) {
            return PassCloudResultBean.getDefaultPageInstance();
        }
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.POINT_MONITOR_DETAIL_LIST_URL, query.getMonitorDetailParam());
        return PassCloudResultBean.getPageInstance(queryResult);
    }
}
