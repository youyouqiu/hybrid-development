package com.zw.platform.service.reportManagement.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.query.VehInformationStaticsQuery;
import com.cb.platform.util.page.PassCloudResultBean;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.service.reportManagement.VehicleInformationStatisticsService;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.PrecisionUtils;
import com.zw.platform.util.report.PaasCloudUrlEnum;
import org.springframework.stereotype.Service;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2021/1/8 11:08
 */
@Service
public class VehicleInformationStatisticsServiceImpl implements VehicleInformationStatisticsService {

    @Override
    public PassCloudResultBean getOrgGraph(VehInformationStaticsQuery query) {
        String queryResult = HttpClientUtil
            .send(PaasCloudUrlEnum.VEHICLE_INFORMATION_STATISTICS_ORG_GRAPH_URL, query.getGraphicsParam());
        return PassCloudResultBean.getDataInstance(queryResult, e -> assembleOrgGraphTime(e));
    }

    @Override
    public PassCloudResultBean getOrgList(VehInformationStaticsQuery query) {
        if (StrUtil.isBlank(query.getOrganizationIds())) {
            return PassCloudResultBean.getDefaultPageInstance();
        }
        String queryResult =
            HttpClientUtil.send(PaasCloudUrlEnum.VEHICLE_INFORMATION_STATISTICS_ORG_LIST_URL, query.getParam());
        return PassCloudResultBean.getPageInstance(queryResult, e -> assembleOrgListTime(e));
    }

    private JSONArray assembleOrgListTime(String data) {
        JSONArray jsonArray = JSONObject.parseArray(data);
        for (int i = 0, len = jsonArray.size(); i < len; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Long second = jsonObject.getLong("dailyAvgFatigueDuration");
            if (second == null || second == 0) {
                continue;
            }
            jsonObject.put("dailyAvgFatigueDuration", PrecisionUtils.roundByScale(second * 1.0 / 60, 2));

        }
        return jsonArray;
    }

    private JSONObject assembleOrgGraphTime(String dataStr) {
        JSONObject jsonObject = JSONObject.parseObject(dataStr);
        Long second = jsonObject.getLong("avgFatigueDuration");
        if (second == null || second == 0) {
            return jsonObject;
        }
        jsonObject.put("avgFatigueDuration", PrecisionUtils.roundByScale(second * 1.0 / 60, 2));
        return jsonObject;
    }

    @Override
    public PassCloudResultBean getOrgDetailGraph(VehInformationStaticsQuery query) {
        String queryResult = HttpClientUtil
            .send(PaasCloudUrlEnum.VEHICLE_INFORMATION_STATISTICS_ORG_DETAIL_GRAPH_URL, query.getDetailGraphicsParam());
        return PassCloudResultBean.getDataInstance(queryResult, e -> assembleOrgDetailGraphTime(e));
    }

    @Override
    public PassCloudResultBean getOrgDetailList(VehInformationStaticsQuery query) {
        if (StrUtil.isBlank(query.getOrganizationId())) {
            return PassCloudResultBean.getDefaultPageInstance();
        }
        String queryResult = HttpClientUtil
            .send(PaasCloudUrlEnum.VEHICLE_INFORMATION_STATISTICS_ORG_DETAIL_LIST_URL, query.getDetailParam());
        return PassCloudResultBean.getPageInstance(queryResult, e -> assembleOrgDetailList(e));
    }

    private JSONArray assembleOrgDetailList(String data) {
        JSONArray jsonArray = JSONObject.parseArray(data);
        for (int i = 0, len = jsonArray.size(); i < len; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Long second = jsonObject.getLong("fatigueDuration");
            if (second == null || second == 0) {
                continue;
            }
            jsonObject.put("fatigueDuration", PrecisionUtils.roundByScale(second * 1.0 / 60, 2));

        }
        return jsonArray;
    }

    private JSONObject assembleOrgDetailGraphTime(String dataStr) {
        JSONObject jsonObject = JSONObject.parseObject(dataStr);
        Long second = jsonObject.getLong("avgFatigueDuration");
        if (second == null || second == 0) {
            return jsonObject;
        }
        jsonObject.put("avgFatigueDuration", PrecisionUtils.roundByScale(second * 1.0 / 60, 2));
        return jsonObject;
    }
}
