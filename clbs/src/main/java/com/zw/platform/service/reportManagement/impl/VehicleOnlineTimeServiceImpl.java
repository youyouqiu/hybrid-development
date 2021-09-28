package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.query.VehicleOnlineTimeQuery;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.service.reportManagement.VehicleOnlineTimeService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.PrecisionUtils;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2021/1/19 17:56
 */
@Service
public class VehicleOnlineTimeServiceImpl implements VehicleOnlineTimeService {
    @Override
    public PassCloudResultBean getOrgData(VehicleOnlineTimeQuery query) {
        if (StrUtil.isBlank(query.getOrganizationIds())) {
            return PassCloudResultBean.getDefaultPageInstance();
        }
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_ONLINE_TIME_ORG_LIST_URL, query.getOrgParam());
        return PassCloudResultBean.getPageInstanceSingle(queryResult, e -> assembleTime(e));
    }

    @Override
    public PassCloudResultBean getMonitorData(VehicleOnlineTimeQuery query) {
        if (StrUtil.isBlank(query.getMonitorIds())) {
            return PassCloudResultBean.getDefaultPageInstance();
        }
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_ONLINE_TIME_MONITOR_LIST_URL, query.getMonitorParam());
        return PassCloudResultBean.getPageInstanceSingle(queryResult, e -> assembleTime(e));
    }

    @Override
    public PassCloudResultBean getDivisionData(VehicleOnlineTimeQuery query) {
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_ONLINE_TIME_DIVISION_LIST_URL, query.getDivisionParam());
        return PassCloudResultBean.getDataInstance(queryResult, e -> assembleTime(e));
    }

    private JSONObject assembleTime(String dataStr) {
        JSONObject data = JSONObject.parseObject(dataStr);
        JSONArray jsonArray = data.getJSONArray("days");

        List<String> days = new ArrayList<>();
        for (int i = 0, len = jsonArray.size(); i < len; i++) {
            days.add(convertHour(jsonArray.getDouble(i)));
        }
        data.put("days", days);
        data.put("total", convertHour(data.getDoubleValue("total")));
        return data;
    }

    /**
     * 转换小时
     * @param minutes
     * @return
     */
    private String convertHour(Double minutes) {
        if (minutes.equals(0.0)) {
            return "0";
        }
        return PrecisionUtils.roundByScale(minutes / 60, 2);

    }
}
