package com.zw.app.service.activeSecurity.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.zw.platform.util.common.Date8Utils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.app.annotation.AppMethodVersion;
import com.zw.app.annotation.AppServerVersion;
import com.zw.app.controller.AppVersionConstant;
import com.zw.app.domain.alarm.DealRiskNum;
import com.zw.app.domain.alarm.RiskRankResult;
import com.zw.app.service.activeSecurity.AppDealRankService;
import com.zw.app.service.activeSecurity.AppRiskRankService;


@Service
@AppServerVersion
public class AppDealRankServiceImpl implements AppDealRankService {

    @Autowired
    private AppRiskRankService appRiskRankService;

    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = {"/clbs/app/risk/dealRank/getDealRank"})
    public List<RiskRankResult> getDealRank(String vehicleIds, String startTime, String endTime, Integer status)
        throws Exception {
        List<RiskRankResult> riskRankResults = new ArrayList<>();
        try {
            if (vehicleIds != null && startTime != null && endTime != null) {
                String[] vids = vehicleIds.split(",");
                String start = startTime + " 00:00:00";
                String end = endTime + " 23:59:59";
                Map<String, Integer> dayMap = Date8Utils.getDayMap(startTime, endTime);
                Map<String, String> moniorsMap = new HashMap<>();
                riskRankResults = appRiskRankService.getAllMonitors(vids, moniorsMap, dayMap);
                SearchResponse searchResponse = adasElasticSearchUtil.getDealRank(vids, start, end, status);
                SearchScrollRequest scroll = new SearchScrollRequest();
                if (searchResponse != null) {
                    scroll.scrollId(searchResponse.getScrollId());
                    JSONObject object = JSONObject.parseObject(String.valueOf(searchResponse));
                    JSONArray jsonArray =
                        object.getJSONObject("aggregations").getJSONObject("sterms#vehicleId").getJSONArray("buckets");
                    List<RiskRankResult> riskRankResult =
                        appRiskRankService.assemblyData(jsonArray, moniorsMap, dayMap, "date_histogram#deal_time");
                    Set<RiskRankResult> set = new LinkedHashSet<>(riskRankResult);
                    set.addAll(riskRankResults);
                    return new ArrayList<>(set);
                }
                return riskRankResults;
            }
        } catch (Exception e) {
            throw e;
        }
        return riskRankResults;
    }

    @Override
    @AppMethodVersion(version = AppVersionConstant.APP_VERSION_ONE, url = {"/clbs/app/risk/dealRank/getDealNum"})
    public DealRiskNum getDealNum(String vehicleIds, String startTime, String endTime)
        throws Exception {
        DealRiskNum dealRiskNum = new DealRiskNum();
        Long total = 0L;
        Integer untreated = 0;
        Integer treated = 0;
        try {
            if (vehicleIds != null && startTime != null && endTime != null) {
                String[] vids = vehicleIds.split(",");
                String start = startTime + " 00:00:00";
                String end = endTime + " 23:59:59";
                SearchResponse searchResponse = adasElasticSearchUtil.getDealNum(vids, start, end);
                SearchScrollRequest scroll = new SearchScrollRequest();
                if (searchResponse != null) {
                    scroll.scrollId(searchResponse.getScrollId());
                    JSONObject object = JSONObject.parseObject(String.valueOf(searchResponse));
                    total = searchResponse.getHits().totalHits;
                    JSONArray jsonArray =
                        object.getJSONObject("aggregations").getJSONObject("lterms#status").getJSONArray("buckets");
                    for (Object object1 : jsonArray) {
                        JSONObject jsonObject = (JSONObject) object1;
                        Integer key = jsonObject.getInteger("key");
                        if (key == 6) {
                            treated += jsonObject.getInteger("doc_count");
                        } else {
                            untreated += jsonObject.getInteger("doc_count");
                        }
                    }
                    dealRiskNum.setTotal(Integer.parseInt(total.toString()));
                    dealRiskNum.setUntreated(untreated);
                    dealRiskNum.setTreated(treated);
                }
                return dealRiskNum;
            }
        } catch (Exception e) {
            throw e;
        }
        return dealRiskNum;
    }
}
