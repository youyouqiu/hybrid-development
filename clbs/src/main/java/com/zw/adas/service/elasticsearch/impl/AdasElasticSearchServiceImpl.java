package com.zw.adas.service.elasticsearch.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zw.adas.domain.riskManagement.bean.AdasMediaEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import com.zw.adas.service.elasticsearch.AdasElasticSearchService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.elasticsearch.AdasElasticSearchUtil;
import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.basic.dto.MonitorBaseDTO;
import com.zw.platform.domain.leaderboard.RiskDealInfo;
import com.zw.platform.domain.leaderboard.RiskResultEnum;
import com.zw.platform.domain.leaderboard.RiskStatusEnum;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.MonitorUtils;
import com.zw.platform.util.spring.EsPrefixConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述:操作elasticSearch的类,所有方法名称全部加上前缀es 去除spring事务
 *
 * @author zhengjc
 * @date 2019/5/30
 */
@Service
public class AdasElasticSearchServiceImpl implements AdasElasticSearchService {

    @Autowired
    private AdasElasticSearchUtil adasElasticSearchUtil;

    @Value("${adas.isVip}")
    private boolean isVip;

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    private EsPrefixConfig esPrefixConfig;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    /**
     * 批量添加媒体索引,异步
     *
     */
    @Override
    public boolean esAddMediaBatch(List<AdasMediaEsBean> riskEsBean) {

        return adasElasticSearchUtil
                .addDataBatch(AdasElasticSearchUtil.ADAS_MEDIA, AdasElasticSearchUtil.INDEX_TYPE, riskEsBean);
    }

    @Override
    public boolean esUpdateRiskRealTime(AdasRiskEsBean riskEsBean) {

        return adasElasticSearchUtil
                .updateDataRealTime(AdasElasticSearchUtil.RISK_INDEX, AdasElasticSearchUtil.INDEX_TYPE,
                        riskEsBean.getId(),
                        JSON.toJSONString(riskEsBean));
    }

    /**
     * 批量更新风险,异步
     *
     */
    @Override
    public boolean esUpdateRiskBatch(List<AdasRiskEsBean> list) {

        return adasElasticSearchUtil
                .updateDataBatch(AdasElasticSearchUtil.RISK_INDEX, AdasElasticSearchUtil.INDEX_TYPE, list);
    }

    /**
     * 批量更新媒体索引,异步
     *
     */
    @Override
    public boolean esUpdateMediaBatch(List<? extends AdasRiskEsBean> list) {

        return adasElasticSearchUtil
                .updateDataBatch(AdasElasticSearchUtil.ADAS_MEDIA, AdasElasticSearchUtil.INDEX_TYPE, list);
    }

    /**
     * 更新风险事件,异步
     *
     */
    @Override
    public boolean esUpdateRiskEvent(AdasRiskEventEsBean riskEsBean) {

        return adasElasticSearchUtil
                .updateData(AdasElasticSearchUtil.RISK_EVENT_INDEX, AdasElasticSearchUtil.INDEX_TYPE,
                        riskEsBean.getId(),
                        JSON.toJSONString(riskEsBean));
    }

    /**
     * 批量更新风险事件,异步
     *
     */
    @Override
    public boolean esUpdateRiskEventBatch(List<AdasRiskEventEsBean> list) {

        return adasElasticSearchUtil
                .updateDataBatch(AdasElasticSearchUtil.RISK_EVENT_INDEX, AdasElasticSearchUtil.INDEX_TYPE, list);
    }

    /**
     * 批量更新风险事件,实时更新
     *
     */
    @Override
    public boolean esUpdateRiskEventRealTimeBatch(List<AdasRiskEventEsBean> list) {

        return adasElasticSearchUtil
                .updateDataBatchRealTime(AdasElasticSearchUtil.RISK_EVENT_INDEX, AdasElasticSearchUtil.INDEX_TYPE,
                        list);
    }

    /**
     * 获取风险,同步
     *
     */
    @Override
    public AdasRiskEsBean esGetRiskById(String id) {
        return adasElasticSearchUtil
                .getData(AdasElasticSearchUtil.RISK_INDEX, AdasElasticSearchUtil.INDEX_TYPE, id, AdasRiskEsBean.class);
    }

    /**
     * 获取风险事件,同步
     *
     */
    @Override
    public AdasRiskEventEsBean esGetRiskEventById(String id) {
        return adasElasticSearchUtil
                .getData(AdasElasticSearchUtil.RISK_EVENT_INDEX, AdasElasticSearchUtil.INDEX_TYPE, id,
                        AdasRiskEventEsBean.class);
    }

    /**
     * @param flag 导出时为true,查询为false
     */
    @Override
    public Map<String, Object> esQueryRiskInfo(AdasRiskDisposeRecordQuery rs, boolean flag) throws Exception {

        return adasElasticSearchUtil.executeQuery(rs, AdasElasticSearchUtil.RISK_INDEX, flag);
    }

    @Override
    public List<String> esQueryExportRiskEventId(AdasRiskDisposeRecordQuery rs) {
        Map<String, Object> result;
        try {
            result = adasElasticSearchUtil.executeQuery(rs, AdasElasticSearchUtil.RISK_EVENT_INDEX, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return (List<String>) result.get("ids");
    }

    @Override
    public List<String> esGetTerminalEvidenceByRiskId(String riskId) {
        return esGetMediaIdsByRiskId("terminal_evidence", riskId);
    }

    @Override
    public List<String> esGetTerminalEvidenceByEventId(String riskEventId) {
        return esGetMediaIdsByRiskEventId(riskEventId);
    }

    @Override
    public List<String> esGetAllBrands(AdasRiskDisposeRecordQuery rs, String indexName) throws Exception {
        Set<String> vehicleId = adasElasticSearchUtil.getAllVehicleId(rs, indexName);
        return generateBrand(vehicleId);
    }

    /**
     * @param flag 导出时为true,查询为false
     */
    @Override
    public Map<String, Object> esQueryMediaInfo(AdasRiskDisposeRecordQuery rs, boolean flag) throws Exception {

        return adasElasticSearchUtil.executeQuery(rs, AdasElasticSearchUtil.ADAS_MEDIA, flag);
    }

    /**
     * 通过风险id获取风险事件id
     *
     */
    @Override
    public List<String> esQueryRiskEventIdsByRiskId(String... riskIds) {

        LinkedList<String> fields = Lists.newLinkedList();
        fields.add("id");
        return esGetIdsByIds(fields, null, null, AdasElasticSearchUtil.RISK_EVENT_INDEX, "risk_id", riskIds);
    }

    /**
     * 获取终端证据
     *
     */
    @Override
    public List<String> esGetMediaIdsByRiskId(String type, String... riskIds) {

        SearchRequest searchRequest = new SearchRequest(AdasElasticSearchUtil.ADAS_MEDIA);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"id"}, null);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termsQuery("risk_id", riskIds));
        if (!StringUtils.isEmpty(type)) {
            BoolQueryBuilder evidenceType1 = null;
            if ("risk_evidence".equals(type)) {
                evidenceType1 = QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("evidence_type", 3))
                        .should(QueryBuilders.matchPhraseQuery("evidence_type", 4));
            } else if ("terminal_evidence".equals(type)) {
                evidenceType1 = QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("evidence_type", 1))
                        .should(QueryBuilders.matchPhraseQuery("evidence_type", 2))
                        .should(QueryBuilders.matchPhraseQuery("evidence_type", 5));
            }
            boolQueryBuilder.must(evidenceType1);
        }
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1000);
        searchRequest.source(searchSourceBuilder);
        List<String> list = Lists.newLinkedList();
        try {
            SearchResponse search = esClient.search(searchRequest);
            SearchHits hits = search.getHits();
            for (SearchHit hit : hits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                list.add((sourceAsMap.get("id").toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询风控证据的media_id 传递 mediaIdCondition false visitIdCondition true 查询证据证据的media_id 传递 mediaIdCondition true
     * visitIdCondition false
     *
     * @param all              all为 true时 通过风险id查询所有的media_id 部分终端证据和风控证据
     */
    @Override
    public List<String> esGetMediaIdsByRiskId(Integer mediaType, boolean all, boolean mediaIdCondition,
                                              boolean visitIdCondition, String... riskIds) {

        LinkedList<String> fields = Lists.newLinkedList();
        fields.add("id");
        Map<String, Object> eqConditon = Maps.newHashMap();
        Map<String, Object> unEqConditon = Maps.newHashMap();

        if (!all) {
            if (mediaIdCondition) {
                unEqConditon.put("media_id", null);
            } else {
                eqConditon.put("media_id", null);
            }
            if (visitIdCondition) {
                unEqConditon.put("visit_id", null);
            } else {
                eqConditon.put("visit_id", null);
            }
        }
        if (null != mediaType) {
            eqConditon.put("media_type", mediaType);
        }
        return esGetIdsByIds(fields, eqConditon, unEqConditon, AdasElasticSearchUtil.ADAS_MEDIA, "risk_id", riskIds);
    }

    @Override
    public List<String> getMediaIdsByRiskId(Integer mediaType, String... riskIds) throws Exception {
        return adasElasticSearchUtil.getMediaIdsByRiskId(mediaType, riskIds, AdasElasticSearchUtil.ADAS_MEDIA);
    }

    /**
     * 通过风险事件id获取media表的id  只获取中终端证据的media_id
     *
     */
    @Override
    public List<String> esGetMediaIdsByRiskEventId(String... riskEventIds) {

        LinkedList<String> fields = Lists.newLinkedList();
        fields.add("id");
        return esGetIdsByIds(fields, null, null, AdasElasticSearchUtil.ADAS_MEDIA, "risk_event_id", riskEventIds);
    }

    /**
     * 通过风险id获取其他索引的id
     *
     * @param id             id 字段 作为查询字段
     * @param idValues       ids值
     */
    private List<String> esGetIdsByIds(List<String> fieldMap, Map<String, Object> whereEqCondi,
                                       Map<String, Object> whereUneqCondi, String indexName, String id,
                                       String... idValues) {
        return adasElasticSearchUtil.esGetIdsByIds(fieldMap, whereEqCondi, whereUneqCondi, indexName, id, idValues);
    }

    /**
     * 通过 risk_id 或者risk_event_id 查询报警信息
     *
     */
    @Override
    public AdasRiskDisposeRecordForm esQueryInfoById(String id, String type) {

        List<AdasMediaEsBean> adasMediaEsBeans = adasElasticSearchUtil.queryMediaRiskInfo(type, id);
        if (CollectionUtils.isNotEmpty(adasMediaEsBeans)) {
            AdasMediaEsBean adasMediaEsBean = adasMediaEsBeans.get(0);

            AdasRiskDisposeRecordForm rs = new AdasRiskDisposeRecordForm();
            rs.setBrand(adasMediaEsBean.getBrand());
            rs.setRiskResult(adasMediaEsBean.getRiskResult() + "");

            rs.setWarTime(DateUtil.formatDate(adasMediaEsBean.getWarningTime(), "yyyy-MM-dd HH:mm:ss"));
            rs.setRiskLevel(adasCommonHelper.geRiskLevel(adasMediaEsBean.getRiskLevel() + ""));
            rs.setEventNumber(adasMediaEsBean.getEventNumber());
            rs.setRiskNumber(adasMediaEsBean.getRiskNumber());
            rs.setRiskEvent(adasCommonHelper.geEventName(adasMediaEsBean.getEventType() + ""));
            rs.setDriver(adasMediaEsBean.getDriver());
            rs.setRiskType(adasMediaEsBean.getRiskType());
            rs.setDealUser(adasMediaEsBean.getDealer());
            // 根据vehicleId获取组织名称
            BindDTO bindDTO = MonitorUtils.getBindDTO(adasMediaEsBean.getVehicleId(), "orgName");
            if (bindDTO != null) {
                rs.setGroupName(bindDTO.getOrgName());
            }
            return rs;
        }

        return null;
    }

    private List<String> generateBrand(Set<String> vehicleIds) {
        Map<String, BindDTO> bindDTOMap = MonitorUtils.getBindDTOMap(vehicleIds, "name");
        return bindDTOMap.values().stream().map(MonitorBaseDTO::getName).collect(Collectors.toList());
    }

    @Override
    public List<JSONObject> test(String sqlStr) throws Exception {
        return adasElasticSearchUtil.executeSql(sqlStr);
    }

    @Override
    public List<String> esGetMediaIdsByEventIds(Integer mediaType, boolean all, boolean mediaIdCondition,
                                                String... eventIds) {

        LinkedList<String> fields = Lists.newLinkedList();
        fields.add("id");
        Map<String, Object> eqConditon = Maps.newHashMap();
        Map<String, Object> unEqConditon = Maps.newHashMap();

        if (!all) {
            if (mediaIdCondition) {
                unEqConditon.put("media_id", null);
            } else {
                eqConditon.put("media_id", null);
            }

        }
        if (null != mediaType) {
            eqConditon.put("media_type", mediaType);
        }
        return esGetIdsByIds(fields, eqConditon, unEqConditon, AdasElasticSearchUtil.ADAS_MEDIA, "risk_event_id",
                eventIds);
    }

    @Override
    public List<String> esqueryMediaIdsByRiskId(String... riskId) {
        LinkedList<String> fields = Lists.newLinkedList();
        fields.add("id");
        return esGetIdsByIds(fields, null, null, AdasElasticSearchUtil.ADAS_MEDIA, "risk_id", riskId);
    }

    @Override
    public Set<String> esGetRiskIdByEventId(Integer status, String... eventIds) {
        LinkedList<String> fields = Lists.newLinkedList();
        fields.add("risk_id");
        return adasElasticSearchUtil
                .esGetRiskIdByEventId(fields, AdasElasticSearchUtil.RISK_EVENT_INDEX, "risk_event_id", "risk_id",
                        status,
                        eventIds);
    }

    @Override
    public List<AdasRiskEventEsBean> getEventEsBeanByRiskId(String... riskIds) {
        return adasElasticSearchUtil.getEventEsBeanByRiskId(riskIds, AdasElasticSearchUtil.RISK_EVENT_INDEX);
    }

    @Override
    public List<Map<String, String>> getRiskDealInfo(List<String> vehicleIds, LocalDateTime dateTime, boolean isToday) {
        try {
            String startTime = isToday ? Date8Utils.getMidnightHourTime(dateTime) :
                    Date8Utils.getMidnightHourTime(dateTime.minusDays(1));
            String endTime = isToday ? Date8Utils.getCurrentTime(dateTime) : Date8Utils.getMidnightHourTime(dateTime);
            dateTime = dateTime.minusDays(1);
            String startTime1 = isToday ? Date8Utils.getMidnightHourTime(dateTime) :
                    Date8Utils.getMidnightHourTime(dateTime.minusDays(1));
            String endTime1 = isToday ? Date8Utils.getCurrentTime(dateTime) : Date8Utils.getMidnightHourTime(dateTime);
            RiskDealInfo todayRiskDealInfo = riskDealInfo(vehicleIds, startTime, endTime);
            RiskDealInfo yesterdayRiskDealInfo = riskDealInfo(vehicleIds, startTime1, endTime1);
            return todayRiskDealInfo.getRiskDealInfoList(yesterdayRiskDealInfo, isVip);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private RiskDealInfo riskDealInfo(List<String> vehicleIds, String startTime, String endTime) throws Exception {
        Set<String> vehicleIds1 = new HashSet<>(vehicleIds);
        SearchResponse searchResponse = adasElasticSearchUtil.riskDealData(vehicleIds1, startTime, endTime);
        Terms terms = searchResponse.getAggregations().get("status");
        RiskDealInfo riskDealInfo = new RiskDealInfo();
        for (Terms.Bucket bucket : terms.getBuckets()) {
            Terms terms1 = bucket.getAggregations().get("riskResult");
            if (!RiskStatusEnum.ARCHIVE.eq(Integer.parseInt(bucket.getKeyAsString()))) {
                riskDealInfo.setUntreated((int) bucket.getDocCount());
            }
            for (Terms.Bucket bucket1 : terms1.getBuckets()) {
                int riskResultCode = Integer.parseInt(bucket1.getKey().toString());
                int number = (int) bucket1.getDocCount();
                if (RiskStatusEnum.ARCHIVE.eq(Integer.parseInt(bucket.getKeyAsString()))) {
                    if (RiskResultEnum.SUCCESS_FILE.getCode() == riskResultCode) {
                        riskDealInfo.setSuccessFile(number);
                    } else if (RiskResultEnum.FAILED_FILE.getCode() == riskResultCode) {
                        riskDealInfo.setFailedFile(number);
                    }
                }
            }
        }
        riskDealInfo.calAndSetTotal();
        return riskDealInfo;
    }

}
