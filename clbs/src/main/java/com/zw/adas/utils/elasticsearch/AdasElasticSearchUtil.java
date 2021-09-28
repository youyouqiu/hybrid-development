package com.zw.adas.utils.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zw.adas.domain.driverScore.show.query.AdasDriverScoreQuery;
import com.zw.adas.domain.monitorScore.MonitorScoreQuery;
import com.zw.adas.domain.riskManagement.bean.AdasBaseEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasMediaEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.domain.riskManagement.param.AdasRiskBattleParam;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import com.zw.adas.domain.riskStatistics.bean.AdasStatisticsReportBean;
import com.zw.adas.domain.riskStatistics.query.RiskStatisticsRecordQuery;
import com.zw.adas.service.monitorScore.MonitorScoreService;
import com.zw.adas.utils.AdasCommonHelper;
import com.zw.adas.utils.controller.AdasAddListFunction;
import com.zw.app.domain.activeSecurity.DayRiskNum;
import com.zw.app.domain.activeSecurity.DealInfo;
import com.zw.app.entity.methodParameter.DayRiskDetailEntity;
import com.zw.app.entity.methodParameter.DayRiskEntity;
import com.zw.platform.commons.HttpClientUtil;
import com.zw.platform.domain.leaderboard.RISKRESULT;
import com.zw.platform.domain.leaderboard.RiskDealInfo;
import com.zw.platform.domain.leaderboard.RiskStatus;
import com.zw.platform.domain.leaderboard.RiskStatusEnum;
import com.zw.platform.util.StrUtil;
import com.zw.platform.util.common.Date8Utils;
import com.zw.platform.util.common.DateUtil;
import com.zw.platform.util.common.UuidUtils;
import com.zw.platform.util.spring.EsPrefixConfig;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能描述:
 * @author zhengjc
 * @date 2019/5/30
 * @time 18:10
 */
@Component("adasElasticSearchUtil")
public class AdasElasticSearchUtil {
    private static final Logger logger = LogManager.getLogger(AdasElasticSearchUtil.class);

    public static String RISK_INDEX;
    public static String RISK_EVENT_INDEX;
    public static String ADAS_MEDIA;
    public static final String INDEX_TYPE = "_doc";
    private static int length;

    @Value("${elasticsearch.cluster}")
    private String[] cluster;

    @Autowired
    private EsPrefixConfig esPrefixConfig;

    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    private MonitorScoreService monitorScoreService;

    @Autowired
    private AdasCommonHelper adasCommonHelper;

    @PostConstruct
    public void initIndex() {
        String prefix = esPrefixConfig.getPrefix();
        RISK_INDEX = prefix + "adas_risk";
        RISK_EVENT_INDEX = prefix + "adas_risk_event";
        ADAS_MEDIA = prefix + "adas_media";
    }

    /**
     * 用户监听异步插入后执行的动作,目前不执行任何动作
     */
    private static final ActionListener<ClearScrollResponse> LISTENER = new ActionListener<ClearScrollResponse>() {
        @Override
        public void onResponse(ClearScrollResponse o) {
        }

        @Override
        public void onFailure(Exception e) {
            logger.error("操作es索引失败！", e);
        }

        private void systemMethod(DocWriteResponse indexResponse, String type) {
            type = (type != null ? type : "操作");
            ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
            if (shardInfo != null && shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                    logger.error(type + "es部分分片失败,失败原因", failure.reason());
                }
            }
        }
    };

    private static RangeQueryBuilder getRangeQueryBuilder(String startTime, String endTime, String rangeField) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(rangeField);
        rangeQueryBuilder.gte(startTime);
        rangeQueryBuilder.lt(endTime);
        return rangeQueryBuilder;
    }

    private static RangeQueryBuilder getRangeQueryBuilderEquals(String startTime, String endTime, String rangeField) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(rangeField);
        rangeQueryBuilder.gte(startTime);
        rangeQueryBuilder.lt(endTime);
        return rangeQueryBuilder;
    }

    private static BoolQueryBuilder getMonitorScoreBuilder(String startTime, String endTime, Set<String> vidsSet) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        RangeQueryBuilder rangeQueryBuilder = getRangeQueryBuilder(startTime, endTime, "event_time");
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vidsSet)).must(rangeQueryBuilder);
        return boolBuilder;
    }

    @PostConstruct
    private void init() {

        if (cluster != null && cluster.length > 0) {
            length = cluster.length;
        } else {
            logger.info(">===========elasticSearch 集群的ip和port未配置=========<");
        }
    }

    public boolean addData(String index, String type, String id, String jsonStr) {
        try {
            IndexRequest request = new IndexRequest(index, type, id);
            request.source(jsonStr, XContentType.JSON);
            request.timeout(TimeValue.timeValueSeconds(5));
            esClient.index(request);
        } catch (Exception e) {
            logger.error("插入" + index + "es索引失败！", e);
            return false;
        }
        return true;
    }

    public boolean updateData(String index, String type, String id, String jsonStr) {
        try {
            UpdateRequest request = new UpdateRequest(index, type, id);
            request.timeout(TimeValue.timeValueSeconds(20));
            request.doc(jsonStr, XContentType.JSON);
            request.retryOnConflict(5);
            esClient.update(request);
        } catch (Exception e) {
            logger.error("更新" + index + "es索引失败！", e);
            return false;
        }
        return true;
    }

    public boolean updateDataRealTime(String index, String type, String id, String jsonStr) {
        try {
            UpdateRequest request = new UpdateRequest(index, type, id);
            request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            request.timeout(TimeValue.timeValueSeconds(20));
            request.doc(jsonStr, XContentType.JSON);
            request.retryOnConflict(5);
            esClient.update(request);
        } catch (Exception e) {
            logger.error("更新" + index + "es索引失败！", e);
            return false;
        }
        return true;
    }

    public boolean updateDataBatch(String index, String type, List<? extends AdasRiskEsBean> list) {

        try {
            if (CollectionUtils.isNotEmpty(list)) {
                BulkRequest request = new BulkRequest();
                for (AdasRiskEsBean riskEsBean : list) {
                    request.add(new UpdateRequest(index, type, riskEsBean.getId())
                        .doc(JSON.toJSONString(riskEsBean), XContentType.JSON));
                }
                request.timeout("2m");
                esClient.bulk(request);
                return true;
            }
        } catch (Exception e) {
            logger.error("更新" + index + "es索引失败！", e);
            return false;
        }
        return true;
    }

    public boolean updateDataBatchRealTime(String index, String type, List<? extends AdasRiskEsBean> list) {

        try {
            if (CollectionUtils.isNotEmpty(list)) {
                BulkRequest request = new BulkRequest();
                request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
                request.timeout(TimeValue.timeValueSeconds(20));
                for (AdasRiskEsBean riskEsBean : list) {
                    request.add(new UpdateRequest(index, type, riskEsBean.getId())
                        .doc(JSON.toJSONString(riskEsBean), XContentType.JSON));
                }
                request.timeout("2m");
                esClient.bulk(request);
                return true;
            }
        } catch (Exception e) {
            logger.error("更新" + index + "es索引失败！", e);
            return false;
        }
        return true;
    }

    public boolean addDataBatch(String index, String type, List<? extends AdasBaseEsBean> list) {

        try {
            if (list != null && list.size() > 0) {
                BulkRequest request = new BulkRequest();
                for (AdasBaseEsBean riskEsBean : list) {
                    request.add(new IndexRequest(index, type, riskEsBean.getId())
                        .source(JSON.toJSONString(riskEsBean), XContentType.JSON));
                }
                request.timeout(TimeValue.timeValueSeconds(120));
                esClient.bulk(request);
                return true;
            }
        } catch (Exception e) {
            logger.error("更新" + index + "es索引失败！", e);
        }
        return false;
    }

    /**
     * 专门用于批量获取id
     * @param searchRequest
     * @return
     */
    public <T> List<T> scrollAllIds(SearchRequest searchRequest, AdasAddListFunction<T> function) {
        List<T> ids = new LinkedList<>();
        //通过scrollid有效期为1分钟
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse res = getSearchResponse(searchRequest);
        if (res == null) {
            return ids;
        }
        SearchScrollRequest scroll = new SearchScrollRequest();
        String scrollId = res.getScrollId();

        SearchHit[] hits;
        String id = null;
        while (true) {
            scroll.scrollId(scrollId);
            hits = res.getHits().getHits();
            if (ArrayUtils.isEmpty(hits)) {
                break;
            }
            ids.addAll(function.execute(hits, id));
            scroll.scroll(TimeValue.timeValueMinutes(1L));
            res = getSearchResponse(res, scroll);
            scrollId = res.getScrollId();

        }
        clearScrollId(scrollId);
        return ids;
    }

    public <T> List<T> scrollLimitSizeIds(SearchRequest searchRequest, AdasAddListFunction function, int limitSize) {
        List<T> ids = new LinkedList<>();
        //通过scrollid有效期为1分钟
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse res = getSearchResponse(searchRequest);
        if (res == null) {
            return ids;
        }
        SearchScrollRequest scroll = new SearchScrollRequest();
        String scrollId = res.getScrollId();

        SearchHit[] hits;
        String id = null;
        while (true) {
            scroll.scrollId(scrollId);
            hits = res.getHits().getHits();
            if (ArrayUtils.isEmpty(hits)) {
                break;
            }
            ids.addAll(function.execute(hits, id));
            if (ids.size() >= limitSize) {
                break;
            }
            scroll.scroll(TimeValue.timeValueMinutes(1L));
            res = getSearchResponse(res, scroll);
            scrollId = res.getScrollId();

        }
        clearScrollId(scrollId);
        int size = ids.size();
        return ids.subList(0, Math.min(size, limitSize));
    }

    private List<String> addIds(SearchHit[] hits, String id) {
        List<String> ids = new ArrayList<>();
        for (SearchHit hit : hits) {
            if (hit.getId() != null) {
                ids.add(hit.getId());
            }
        }
        return ids;
    }

    private List<AdasMediaEsBean> addMediaEsBeans(SearchHit[] hits, String id) {
        List<AdasMediaEsBean> esBeans = new ArrayList<>();
        SearchHit hit;
        for (int i = 0, len = hits.length; i < len; i++) {
            hit = hits[i];
            if (hit != null) {
                esBeans.add(JSON.parseObject(JSON.toJSONString(hit.getSourceAsMap()), AdasMediaEsBean.class));
            }
        }
        return esBeans;
    }

    private List<AdasRiskEventEsBean> addEsBeans(SearchHit[] hits, String id) {
        List<AdasRiskEventEsBean> esBeans = new ArrayList<>();
        SearchHit hit;
        for (int i = 0, len = hits.length; i < len; i++) {
            hit = hits[i];
            if (hit != null) {
                esBeans.add(JSON.parseObject(JSON.toJSONString(hit.getSourceAsMap()), AdasRiskEventEsBean.class));
            }
        }
        return esBeans;
    }

    private SearchResponse getSearchResponse(SearchRequest searchRequest) {
        SearchResponse res = null;
        try {
            res = esClient.search(searchRequest);
        } catch (IOException e) {
            logger.error("es执行查询报错", e);
        }
        return res;
    }

    private SearchResponse getSearchResponse(SearchResponse res, SearchScrollRequest scroll) {
        try {
            res = esClient.searchScroll(scroll);
        } catch (IOException e) {
            logger.error("遍历所有风险id", e);
        }
        return res;
    }

    private void clearScrollId(String scrollId) {
        //清除scroll,释放资源
        if (!org.apache.commons.lang3.StringUtils.isEmpty(scrollId)) {
            ClearScrollRequest request = new ClearScrollRequest();
            request.addScrollId(scrollId);
            esClient.clearScrollAsync(request, LISTENER);
        }
    }

    public <T> T getData(String index, String type, String id, Class<T> clazz) {
        List<T> dataBatch = getDataBatch(index, type, new String[] { id }, clazz);
        if (dataBatch != null && dataBatch.size() > 0) {
            return dataBatch.get(0);
        }
        return null;
    }

    public <T> List<T> getDataBatch(String index, String type, String[] ids, Class<T> clazz) {

        if (ids != null && ids.length > 0) {
            GetRequest request;
            GetResponse response;
            List<T> list = Lists.newLinkedList();
            try {
                for (String id : ids) {
                    request = new GetRequest(index, type, id);
                    response = esClient.get(request);
                    if (response.isExists()) {
                        Map<String, Object> sourceAsMap = response.getSourceAsMap();
                        // 需要解决下划线的问题
                        list.add(JSON.parseObject(JSON.toJSONString(sourceAsMap), clazz));
                    }
                }
            } catch (Exception e) {
                logger.error("获取" + index + "es索引失败！", e);
            }
            return list;
        }
        return new ArrayList<>();
    }

    public <T> List<T> multiGetDataBatch(String index, String type, String[] ids, Class<T> clazz) {

        if (ids != null && ids.length > 0) {
            MultiGetRequest multiGetRequest = new MultiGetRequest();
            MultiGetResponse multiGetResponse;
            for (String id : ids) {
                multiGetRequest.add(index, type, id);
            }
            List<T> list = Lists.newLinkedList();
            try {
                multiGetResponse = esClient.multiGet(multiGetRequest);
                Iterator<MultiGetItemResponse> iterator = multiGetResponse.iterator();

                while (iterator.hasNext()) {
                    Map<String, Object> sourceAsMap = iterator.next().getResponse().getSourceAsMap();
                    // 需要解决下划线的问题
                    list.add(JSON.parseObject(JSON.toJSONString(sourceAsMap), clazz));
                }
            } catch (Exception e) {
                logger.error("获取" + index + "es索引失败！", e);
            }
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * 获取总数量
     * @param sql
     * @return
     */
    public int queryCount(String sql) {
        String httpHead = getHost();
        try {
            JSONObject jsonObject = HttpClientUtil.doHttPost(httpHead, sql);
            if (jsonObject != null) {
                return jsonObject.getJSONObject("hits").getInteger("total");
            }
        } catch (Exception e) {
            logger.error(">======通过elasticSearch sql插件获取索引总数量失败========<", e);
            return -1;
        }
        return 0;
    }

    // 随机获取集群中一个节点,用于请求
    // 随机获取集群中一个节点,用于请求
    private String getHost() {
        StringBuilder s = new StringBuilder("http://");
        return s.append(cluster[(int) (Math.random() * length)]).append("/_sql").toString();
    }

    /**
     * 模糊查询
     * @param field
     * @return
     */
    private String generateField(String field, String symbol) {
        if ("*".equals(symbol)) {
            return "wildcardQuery('" + symbol + field + symbol + "')";
        }
        return symbol + field + symbol;
    }

    public List<JSONObject> executeSql(String sql) throws Exception {
        try {
            String httpHead = getHost();
            JSONObject jsonObject = HttpClientUtil.doHttPost(httpHead, sql);
            JSONArray jsonArray;
            List<JSONObject> list = Lists.newLinkedList();
            if (jsonObject != null) {
                jsonArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        list.add(jsonArray.getJSONObject(i).getJSONObject("_source"));
                    }
                    return list;
                }
            }
        } catch (Exception e) {
            logger.error("============初始化错误============", e);
        }
        return null;
    }

    public <T> List<T> executeSql(String sql, Class<T> clazz) {

        String httpHead = getHost();
        JSONObject jsonObject = HttpClientUtil.doHttPost(httpHead, sql);
        JSONArray jsonArray;
        List<T> list = Lists.newLinkedList();
        if (jsonObject != null) {
            jsonArray = jsonObject.getJSONObject("hits").getJSONArray("hits");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    list.add(jsonArray.getJSONObject(i).getJSONObject("_source").toJavaObject(clazz));
                }
                return list;
            }
        }
        return null;
    }

    private SearchResponse getSearchResponse(AdasRiskDisposeRecordQuery rs, String indexName, String[] fields,
        boolean flag) throws IOException {

        Object[] searchAfter = rs.getSearchAfter();
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(rs.getLimit().intValue());
        if (fields == null || fields.length == 0) {
            searchSourceBuilder.fetchSource(new String[] { "id" }, new String[] {});
        } else {
            searchSourceBuilder.fetchSource(fields, new String[] {});
        }
        BoolQueryBuilder boolBuilder = getBoolQueryBuilder(rs);
        if (boolBuilder == null) {
            return null;
        }

        if (!flag) {
            // 如果只是查询 通过searchAfter的形式
            searchSourceBuilder.sort("warning_time", SortOrder.DESC);
            searchSourceBuilder.sort("id", SortOrder.DESC);
            // 通过search_after 角标返回数据
            if (searchAfter != null && searchAfter.length == 2 && !StringUtils.isEmpty(searchAfter[0] + "")) {
                searchSourceBuilder.searchAfter(searchAfter);
            }
        } else {
            // 下载 通过scroll
            searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        }
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    public Set<String> getAllVehicleId(AdasRiskDisposeRecordQuery rs, String indexName) throws IOException {

        Set<String> vehicleIds = Sets.newHashSet();
        rs.setLimit(10000L);
        SearchResponse res = getSearchResponse(rs, indexName, new String[] { "vehicle_id" }, true);
        SearchScrollRequest scroll = new SearchScrollRequest();
        if (res != null) {
            scroll.scrollId(res.getScrollId());
            SearchHit[] hits;
            while (true) {
                SearchHit hit;
                hits = res.getHits().getHits();
                if (hits == null || hits.length == 0) {
                    break;
                }
                for (int i = 0; i < hits.length; i++) {
                    hit = hits[i];
                    if (hit.hasSource()) {
                        vehicleIds.add((String) hit.getSourceAsMap().get("vehicle_id"));
                    }
                }
                scroll.scroll(TimeValue.timeValueMinutes(1L));
                res = esClient.searchScroll(scroll);
            }
            return vehicleIds;
        }
        return new HashSet<>();
    }

    private BoolQueryBuilder getBoolQueryBuilder(AdasRiskDisposeRecordQuery rs) {

        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        // 可多个,逗号隔开
        String vehicleIds = rs.getVehicleIds();
        String startTime = rs.getStartTime();
        String endTime = rs.getEndTime();
        String brand = rs.getBrand();
        if (StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime) || (StringUtils.isEmpty(brand) && StringUtils
            .isEmpty(vehicleIds))) {
            return null;
        }
        String riskNumber = rs.getRiskNumber();
        String riskType = rs.getRiskType();
        String riskLevel = rs.getRiskLevel();
        String driver = rs.getDriver();
        String status = rs.getStatus();
        String dealUser = rs.getDealUser();
        String visitTime = rs.getVisitTime();
        String riskResult = rs.getRiskResult();
        List<String> excludeIds = rs.getExcludeIds();
        String[] deleteIds = rs.getDeleteIds();
        String riskEvent = rs.getRiskEvent();

        // if (StringUtils.isEmpty(brand) && !StringUtils.isEmpty(vehicleIds)) {
        //     String[] vehicleArray = vehicleIds.split(",");
        //     Set vehicleIds1 = new HashSet(Arrays.asList(vehicleArray));
        //     boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vehicleIds1));
        // } else {
        //     boolBuilder.must(QueryBuilders.wildcardQuery("brand", new StringBuilder("*" + brand + "*").toString()));
        // }
        //风险处置记录处理分页问题
        if (excludeIds != null && excludeIds.size() > 0) {
            String[] excludeIdArray = new String[excludeIds.size()];
            for (int i = 0; i < excludeIds.size(); i++) {
                excludeIdArray[i] = excludeIds.get(i);
            }
            boolBuilder.mustNot(QueryBuilders.termsQuery("id", excludeIdArray));
        }
        // 时间范围
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("warning_time");
        rangeQueryBuilder.gte(startTime);
        rangeQueryBuilder.lte(endTime);
        boolBuilder.filter(rangeQueryBuilder);
        if (StrUtil.isNotBlank(riskNumber)) {
            boolBuilder
                .must(QueryBuilders.wildcardQuery("risk_number", new StringBuilder("*" + riskNumber + "*").toString()));
        }

        if (StrUtil.isNotBlank(vehicleIds)) {
            String[] vehicleArray = vehicleIds.split(",");
            Set vehicleIds1 = new HashSet(Arrays.asList(vehicleArray));
            boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vehicleIds1));
        }
        if (StrUtil.isNotBlank(brand)) {
            boolBuilder.must(QueryBuilders.wildcardQuery("brand", new StringBuilder("*" + brand + "*").toString()));
        }

        if (StrUtil.isNotBlank(riskLevel)) {
            boolBuilder.must(QueryBuilders.termsQuery("risk_level", riskLevel.split(",")));
        }
        if (StrUtil.isNotBlank(riskType)) {
            boolBuilder.must(QueryBuilders.termsQuery("risk_type", riskType.split("\\+")));
        }

        if (StrUtil.isNotBlank(driver)) {
            boolBuilder.must(QueryBuilders.termQuery("driver", driver));
        }

        if (StrUtil.isNotBlank(status) && "6".equals(status)) {
            boolBuilder.must(QueryBuilders.termQuery("status", status));
        }

        if (StrUtil.isNotBlank(status) && !"6".equals(status)) {
            boolBuilder.mustNot(QueryBuilders.termQuery("status", "6"));
        }

        if (StrUtil.isNotBlank(dealUser)) {
            boolBuilder.must(QueryBuilders.termQuery("dealer", dealUser));
        }
        if (StrUtil.isNotBlank(visitTime)) {
            // 回访次数
            boolBuilder.must(QueryBuilders.termQuery("visit_times", visitTime));
        }

        if (StrUtil.isNotBlank(riskResult)) {
            boolBuilder.must(QueryBuilders.termQuery("risk_result", riskResult));
        }

        // 判断不为空 如果不为空就是风控证据库
        String evidenceType = rs.getEvidenceType();
        if (StrUtil.isNotBlank(evidenceType)) {
            // 风控证据库
            if (deleteIds != null && deleteIds.length > 0) {
                boolBuilder.mustNot(QueryBuilders.termsQuery("id", deleteIds));
            }
            // 事件类型
            if (StrUtil.isNotBlank(riskEvent)) {
                boolBuilder.must(QueryBuilders.termsQuery("event_type", riskEvent.split(",")));
            }
            // 证据类型
            if ("1".equals(evidenceType) || "2".equals(evidenceType)) {
                boolBuilder.must(QueryBuilders.existsQuery("risk_event_id"));
            }

            boolBuilder.must(QueryBuilders.termQuery("evidence_type", evidenceType));
        }
        return boolBuilder;
    }

    /**
     * 查询 使用search_after
     * @param rs
     * @param indexName
     * @return
     * @throws IOException
     */
    public Map<String, Object> select(AdasRiskDisposeRecordQuery rs, String indexName) throws IOException {
        List<String> idslist = new ArrayList<>();
        SearchResponse searchResponse = getSearchResponse(rs, indexName, null, false);
        Map<String, Object> map = Maps.newHashMap();
        List<String> ids = Lists.newArrayList();
        Object[] sortValues = new Object[2];
        long total = 0;
        if (searchResponse != null) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            total = searchResponse.getHits().getTotalHits();
            if (hits != null) {
                SearchHit hit;
                for (int i = 0, len = hits.length; i < len; i++) {
                    hit = hits[i];
                    if (hit.hasSource()) {
                        idslist.add((String) hit.getSourceAsMap().get("id"));
                        ids.add((String) hit.getSourceAsMap().get("id"));
                    }
                    // 记录游标
                    if (i == hits.length - 1) {
                        sortValues = hit.getSortValues();
                    }
                }

            }
        }
        map.put("ids", ids);
        map.put("search_after", sortValues);
        map.put("total", total);
        return map;
    }

    /**
     * 导出 使用scroll
     * @param rs
     * @param indexName
     * @return
     * @throws IOException
     */
    public Map<String, Object> download(AdasRiskDisposeRecordQuery rs, String indexName) throws IOException {

        List<String> ids = Lists.newLinkedList();
        Map<String, Object> map = Maps.newHashMap();
        rs.setLimit(10000L);
        SearchResponse res = getSearchResponse(rs, indexName, null, true);
        SearchScrollRequest scroll = new SearchScrollRequest();
        String scrollId = "";
        if (res != null) {
            scrollId = res.getScrollId();
            scroll.scrollId(scrollId);
            SearchHit[] hits;
            scrollData(ids, res, scroll);
        }
        //清除scroll,释放资源
        if (!StringUtils.isEmpty(scrollId)) {
            ClearScrollRequest request = new ClearScrollRequest();
            request.addScrollId(scrollId);
            esClient.clearScrollAsync(request, LISTENER);
        }
        map.put("ids", ids);
        return map;
    }

    /**
     * @param rs
     * @param indexName
     * @param flag      true 为下载,false为分页查询
     * @return
     * @throws IOException
     */
    public Map<String, Object> executeQuery(AdasRiskDisposeRecordQuery rs, String indexName, boolean flag)
        throws IOException {

        if (flag) {
            return download(rs, indexName);
        } else {
            return select(rs, indexName);
        }
    }

    public BoolQueryBuilder getQueryBuilder(Set<String> vids, String startTime, String endTime) {
        //构建过滤条件
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("vehicle_id", vids))
            .must(QueryBuilders.rangeQuery("event_time").from(startTime).to(endTime).includeLower(true)     // 包含上界
                .includeUpper(true));
        return boolBuilder;
    }

    private SearchResponse getSearchResponse(Set<String> vids, String indexName, String startTime, String endTime)
        throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        BoolQueryBuilder boolBuilder = getQueryBuilder(vids, startTime, endTime);
        if (boolBuilder == null) {
            return null;
        }
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    /**
     * @param vids      车辆id集合
     * @param indexName
     * @param
     * @return 风险事件数
     * @throws IOException
     */
    public long getAllVehicleNum(Set<String> vids, String indexName, String startTime, String endTime)
        throws IOException {
        Long sum = 0L;
        SearchResponse res = getSearchResponse(vids, indexName, startTime, endTime);
        SearchScrollRequest scroll = new SearchScrollRequest();
        if (res != null) {
            scroll.scrollId(res.getScrollId());
            sum = res.getHits().totalHits;
        }
        return sum;
    }

    public RiskDealInfo executeQueryRiskDealInfo(String sql) throws Exception {
        try {
            String httpHead = getHost();
            JSONObject jsonObject = HttpClientUtil.doHttPost(httpHead, sql);
            JSONArray jsonArray;
            List<JSONObject> riskDealInfoList = Lists.newLinkedList();
            if (jsonObject != null) {
                jsonArray = jsonObject.getJSONObject("aggregations").getJSONObject("status").getJSONArray("buckets");
                if (jsonArray != null) {
                    int untreated = 0;
                    for (int i = 0, len = jsonArray.size(); i < len; i++) {
                        JSONObject statsObject = jsonArray.getJSONObject(i);
                        int status = statsObject.getIntValue("key");
                        if (RiskStatus.ARCHIVE.eq(status)) {
                            riskDealInfoList.add(statsObject.getJSONObject("risk_result"));
                        } else {
                            untreated += statsObject.getIntValue("doc_count");
                        }
                    }
                    return assemblyRiskDealInfo(riskDealInfoList, untreated);

                }
            }
        } catch (Exception e) {
            logger.error("============初始化错误============", e);
        }
        return null;
    }

    private RiskDealInfo assemblyRiskDealInfo(List<JSONObject> riskInfoList, int untreated) {
        RiskDealInfo riskDealInfo = new RiskDealInfo();
        riskDealInfo.setUntreated(untreated);
        for (JSONObject riskInfo : riskInfoList) {
            JSONArray dealInfoArray = riskInfo.getJSONArray("buckets");
            setFileRiskResult(riskDealInfo, dealInfoArray);
        }
        riskDealInfo.calAndSetTotal();
        return riskDealInfo;
    }

    private void setFileRiskResult(RiskDealInfo riskDealInfo, JSONArray dealInfoArray) {
        for (int i = 0, len = dealInfoArray.size(); i < len; i++) {
            JSONObject fileInfo = dealInfoArray.getJSONObject(i);
            int riskResultCode = fileInfo.getIntValue("key");
            int number = fileInfo.getIntValue("doc_count");
            if (RISKRESULT.SUCCESS_FILE.getCode() == riskResultCode) {
                riskDealInfo.setSuccessFile(number);
            } else if (RISKRESULT.FAILED_FILE.getCode() == riskResultCode) {
                riskDealInfo.setFailedFile(number);
            } else if (RISKRESULT.ACCIDENT_FILE.getCode() == riskResultCode) {
                riskDealInfo.setAccidentFile(number);
            }
        }
    }

    public List<JSONObject> executeQuerySql(String sql) {
        try {
            String httpHead = getHost();
            JSONObject jsonObject = HttpClientUtil.doHttPost(httpHead, sql);
            JSONArray jsonArray;
            List<JSONObject> list = Lists.newLinkedList();
            if (jsonObject != null) {
                jsonArray = jsonObject.getJSONObject("aggregations").getJSONObject("range(status,1,6,7)")
                    .getJSONArray("buckets");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        list.add(jsonArray.getJSONObject(i));
                    }
                    return list;
                }
            }
        } catch (Exception e) {
            logger.error("============初始化错误============", e);
        }
        return null;
    }

    public SearchResponse getRiskRank(String[] vids, String start, String end, Integer riskType) throws IOException {
        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder = getRiskQueryBuilder(vids, start, end, riskType);
        if (boolBuilder == null) {
            return null;
        }
        TermsAggregationBuilder aggregation = getTermsAggregationBuilder("warning_time");
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    /**
     * 报警统计核查请求消息 9406指令
     * @param brand 车牌号
     * @param start 开始时间
     * @param end   结束时间
     * @return 查询结果
     * @throws IOException 检查异常
     */
    public SearchResponse getRiskEventIdByVehiclAndTime(String brand, String start, String end) throws IOException {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder = getRiskEventIdQueryBuilder(brand, start, end);
        if (boolBuilder == null) {
            return null;
        }
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    /**
     * 组建BoolQueryBuilder进行复合查询。
     * @param brand 车牌号
     * @param start 开始时间
     * @param end   结束时间
     * @return BoolQueryBuilder。
     */
    private BoolQueryBuilder getRiskEventIdQueryBuilder(String brand, String start, String end) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

        boolBuilder.must(QueryBuilders.termQuery("brand", brand));
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("event_time");
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lte(end);
        //是否包含下界
        rangeQueryBuilder.includeLower(true);
        //是否应该包括上界
        rangeQueryBuilder.includeUpper(true);
        boolBuilder.filter(rangeQueryBuilder);
        return boolBuilder;
    }

    private TermsAggregationBuilder getTermsAggregationBuilder(String conditions) {
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("vehicleId").field("vehicle_id");
        AggregationBuilder aggregation1 =
            AggregationBuilders.dateHistogram(conditions).field(conditions).format("yyyy-MM-dd")
                .dateHistogramInterval(DateHistogramInterval.DAY).order(BucketOrder.key(false));
        aggregation.subAggregation(AggregationBuilders.count("id").field("id")).order(BucketOrder.count(false))
            .subAggregation(aggregation1);
        return aggregation;
    }

    private BoolQueryBuilder getRiskQueryBuilder(String[] vids, String start, String end, Integer riskType) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (riskType != null) {
            boolBuilder.must(QueryBuilders.termQuery("merge_risk_type", riskType));
        }
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vids));
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("warning_time");
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lte(end);
        rangeQueryBuilder.includeLower(true);
        rangeQueryBuilder.includeUpper(true);
        boolBuilder.filter(rangeQueryBuilder);
        return boolBuilder;
    }

    public SearchResponse getPercentageOfRank(String[] vids, String start, String end) throws IOException {
        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder = getRiskQueryBuilder(vids, start, end, null);
        if (boolBuilder == null) {
            return null;
        }
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("riskType").field("merge_risk_type");
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    public SearchResponse getDealRank(String[] vids, String start, String end, Integer status) throws IOException {
        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder = getdealQueryBuilder(vids, start, end, status);
        if (boolBuilder == null) {
            return null;
        }
        TermsAggregationBuilder aggregation = getTermsAggregationBuilder("deal_time");
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);

    }

    private BoolQueryBuilder getdealQueryBuilder(String[] vids, String start, String end, Integer status) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (status == 6) { //6代表已处理
            boolBuilder.must(QueryBuilders.termQuery("status", status));
        } else {
            boolBuilder.mustNot(QueryBuilders.termQuery("status", 6));
        }
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vids));
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("deal_time");
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lte(end);
        rangeQueryBuilder.includeLower(true);
        rangeQueryBuilder.includeUpper(true);
        boolBuilder.filter(rangeQueryBuilder);
        return boolBuilder;
    }

    public SearchResponse getDealNum(String[] vids, String start, String end) throws IOException {
        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (ArrayUtils.isNotEmpty(vids)) {
            boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vids));
        }
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("deal_time");
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lte(end);
        rangeQueryBuilder.includeLower(true);
        rangeQueryBuilder.includeUpper(true);
        boolBuilder.filter(rangeQueryBuilder);
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("status").field("status");
        aggregation.subAggregation(AggregationBuilders.count("id").field("id"));
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    private SearchResponse getRiskSearchResponse(Collection<String> vehicleIds, String startTime, String endTime,
        int riskStatus) {
        return getRiskSearchResponse(vehicleIds, startTime, endTime, riskStatus, null);
    }

    private SearchResponse getRiskSearchResponse(Collection<String> vehicleIds, String startTime, String endTime,
        int riskStatus, String riskIds) {

        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(500);
        //这里只查询id，不需要返回多余的详情
        searchSourceBuilder.fetchSource(false);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (0 == riskStatus) {
            //查询除归档以外所有的状态的风险
            boolBuilder.mustNot(QueryBuilders.termQuery("status", RiskStatus.ARCHIVE.getCode()));
        } else if (RiskStatus.ALL.getCode() == riskStatus) {
            //代表全部风险不做任何处理
        } else {
            //查询具体状态的风险
            boolBuilder.must(QueryBuilders.termQuery("status", riskStatus));
        }

        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vehicleIds));
        //需要过滤掉前台已经存在的风险id
        if (StrUtil.isNotBlank(riskIds)) {
            boolBuilder.mustNot(QueryBuilders.termsQuery("risk_id", Arrays.asList(riskIds.split(","))));
        }
        RangeQueryBuilder rangeQueryBuilder = getRangeQueryBuilder(startTime, endTime, "warning_time");
        boolBuilder.filter(rangeQueryBuilder);
        searchSourceBuilder.query(boolBuilder);
        searchSourceBuilder.sort("risk_level", SortOrder.DESC);
        searchSourceBuilder.sort("warning_time", SortOrder.DESC);
        if (RiskStatus.ALL.getCode() == riskStatus) {
            searchSourceBuilder.sort("status", SortOrder.ASC);
        }

        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public List<String> getTodayUntreatedRisk(Collection<String> vehicleIds) {
        return getTodayUntreatedRisk(vehicleIds, null);
    }

    public List<String> getTodayUntreatedRisk(Collection<String> vehicleIds, String riskIds) {
        LocalDateTime today = LocalDateTime.now();
        String startTime = Date8Utils.getMidnightHourTime(today);
        LocalDateTime todayEnd = today.plusDays(1);
        String endTime = Date8Utils.getMidnightHourTime(todayEnd);
        SearchResponse searchResponse = getRiskSearchResponse(vehicleIds, startTime, endTime, 0, riskIds);
        return getRiskIds(searchResponse);
    }

    public Map<String, Long> getTodayUntreatedRiskCountByEventCodes(Collection<String> vehicleIds,
        String[] eventFieldArr) {
        Map<String, Long> result = new HashMap<>();
        LocalDateTime today = LocalDateTime.now();
        String startTime = Date8Utils.getMidnightHourTime(today);
        LocalDateTime todayEnd = today.plusDays(1);
        String endTime = Date8Utils.getMidnightHourTime(todayEnd);
        List<Integer> eventCode;
        MultiSearchRequest requests = new MultiSearchRequest();
        for (String eventField : eventFieldArr) {
            eventCode = adasCommonHelper.getAllEventByCommonField(eventField);
            requests.add(getRiskCountSearchRequest(vehicleIds, startTime, endTime, eventCode));
        }
        MultiSearchResponse multiSearchResponse = getMultiSearchResponse(requests);
        MultiSearchResponse.Item[] responses = multiSearchResponse.getResponses();
        for (int i = 0, len = eventFieldArr.length; i < len; i++) {
            SearchResponse response = responses[i].getResponse();
            Cardinality cardinality = response.getAggregations().get("riskNum");
            result.put(eventFieldArr[i], cardinality.getValue());
        }
        return result;
    }

    private MultiSearchResponse getMultiSearchResponse(MultiSearchRequest multiSearchRequest) {
        MultiSearchResponse res = null;
        try {
            res = esClient.multiSearch(multiSearchRequest);
        } catch (IOException e) {
            logger.error("es批量执行查询报错", e);
        }
        return res;
    }

    private SearchRequest getRiskCountSearchRequest(Collection<String> vehicleIds, String startTime, String endTime,
        Collection<Integer> eventCodes) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(false);
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        //查询未处理状态的风险事件
        boolBuilder.must(QueryBuilders.termQuery("status", RiskStatus.UNTREATED.getCode()));
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vehicleIds));
        boolBuilder.must(QueryBuilders.termsQuery("event_type", eventCodes));
        RangeQueryBuilder rangeQueryBuilder = getRangeQueryBuilder(startTime, endTime, "warning_time");
        AggregationBuilder aggregationBuilder =
            AggregationBuilders.cardinality("riskNum").field("risk_id").precisionThreshold(40000);
        boolBuilder.filter(rangeQueryBuilder);
        searchSourceBuilder.query(boolBuilder);
        searchSourceBuilder.aggregation(aggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    public List<String> getTodayUntreatedRiskByEventCode(Collection<String> vehicleIds, String riskIds,
        Collection<Integer> eventCodes) {
        LocalDateTime today = LocalDateTime.now();
        String startTime = Date8Utils.getMidnightHourTime(today);
        LocalDateTime todayEnd = today.plusDays(1);
        String endTime = Date8Utils.getMidnightHourTime(todayEnd);
        SearchResponse searchResponse =
            getRiskSearchResponseByEventCodes(vehicleIds, startTime, endTime, riskIds, eventCodes);
        return getRiskIdList(searchResponse);
    }

    private SearchResponse getRiskSearchResponseByEventCodes(Collection<String> vehicleIds, String startTime,
        String endTime, String riskIds, Collection<Integer> eventCodes) {

        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1000);

        searchSourceBuilder.collapse(new CollapseBuilder("risk_id"));
        searchSourceBuilder.fetchSource(new String[] { "risk_id" }, null);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        //查询未处理状态的风险事件
        boolBuilder.must(QueryBuilders.termQuery("status", RiskStatus.UNTREATED.getCode()));
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vehicleIds));
        //需要过滤掉前台已经存在的风险id
        if (StrUtil.isNotBlank(riskIds)) {
            boolBuilder.mustNot(QueryBuilders.termsQuery("risk_id", Arrays.asList(riskIds.split(","))));
        }
        boolBuilder.must(QueryBuilders.termsQuery("event_type", eventCodes));
        RangeQueryBuilder rangeQueryBuilder = getRangeQueryBuilder(startTime, endTime, "warning_time");
        boolBuilder.filter(rangeQueryBuilder);
        searchSourceBuilder.query(boolBuilder);
        searchSourceBuilder.sort("risk_level", SortOrder.DESC);
        searchSourceBuilder.sort("warning_time", SortOrder.DESC);
        searchRequest.source(searchSourceBuilder);
        return getSearchResponse(searchRequest);
    }

    public List<String> getDayRiskDetail(DayRiskDetailEntity dayRiskDetailEntity) {
        String riskIds = dayRiskDetailEntity.getRiskIds();
        String startTime = dayRiskDetailEntity.getStartTime();
        String endTime = dayRiskDetailEntity.getEndTime();
        SearchResponse searchResponse =
            getRiskSearchResponse(Arrays.asList(dayRiskDetailEntity.getVehicleId()), startTime, endTime,
                RiskStatus.ALL.getCode(), riskIds);
        return getRiskIds(searchResponse);
    }

    private List<String> getRiskIds(SearchResponse searchResponse) {
        List<String> riskIds = new ArrayList<>();
        if (searchResponse != null) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (int i = 0, len = hits.length; i < len; i++) {
                riskIds.add(hits[i].getId());
            }
        }
        return riskIds;
    }

    private List<String> getRiskIdList(SearchResponse searchResponse) {
        List<String> riskIds = new ArrayList<>();
        if (searchResponse != null) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (int i = 0, len = hits.length; i < len; i++) {
                riskIds.add(hits[i].getFields().get("risk_id").getValue());
            }
        }
        return riskIds;
    }

    private SearchResponse getTodayDealInfoSearchResponse(Collection<String> vehicleIds, String startTime,
        String endTime) {

        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        //这里只查询id，不需要返回多余的详情
        searchSourceBuilder.fetchSource(false);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vehicleIds));
        RangeQueryBuilder rangeQueryBuilder = getRangeQueryBuilder(startTime, endTime, "warning_time");
        rangeQueryBuilder.includeLower(true);
        rangeQueryBuilder.includeUpper(true);
        boolBuilder.filter(rangeQueryBuilder);

        AggregationBuilder aggregationBuilder =
            AggregationBuilders.range("group_status").addRange(1, 6).addRange(6, 7).field("status");
        searchSourceBuilder.query(boolBuilder);
        searchSourceBuilder.aggregation(aggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    public DealInfo getTodayDealInfo(Set<String> vehicleIds) {
        LocalDateTime today = LocalDateTime.now();
        String startTime = Date8Utils.getMidnightHourTime(today);
        String endTime = Date8Utils.getCurrentTime(today);
        SearchResponse searchResponse = getTodayDealInfoSearchResponse(vehicleIds, startTime, endTime);
        return assemblyDealInfo(searchResponse);

    }

    private DealInfo assemblyDealInfo(SearchResponse searchResponse) {
        int untreated = 0;
        int treated = 0;
        if (searchResponse != null) {
            Map<String, Aggregation> data = searchResponse.getAggregations().getAsMap();
            Aggregation aggregation = data.get("group_status");
            JSONArray jsonArray = JSONObject.parseObject(JSONObject.toJSONString(aggregation)).getJSONArray("buckets");
            for (int i = 0, len = jsonArray.size(); i < len; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String key = jsonObject.getString("key");
                int value = jsonObject.getIntValue("docCount");
                if ("1.0-6.0".equals(key)) {
                    untreated = value;
                } else {
                    treated = value;
                }

            }
        }
        return new DealInfo(untreated, treated);
    }

    public List<DayRiskNum> getDayRiskNum(DayRiskEntity dayRiskEntity) {
        LocalDateTime today = LocalDateTime.now();
        String startTime = Date8Utils.getMidnightHourTime(today.minusDays(dayRiskEntity.getMaxQueryDay() - 1));
        String endTime = Date8Utils.getCurrentTime(today);
        SearchResponse searchResponse = getDayRiskNumSearchResponse(dayRiskEntity.getVehicleId(), startTime, endTime);
        List<DayRiskNum> result = assemblyDealRiskNum(searchResponse);
        result = result.stream()
                .skip((long) (dayRiskEntity.getPageNum() - 1) * dayRiskEntity.getPageSize())
                .limit(dayRiskEntity.getPageSize()).collect(Collectors.toList());
        return result;
    }

    private List<DayRiskNum> assemblyDealRiskNum(SearchResponse searchResponse) {
        List<DayRiskNum> dayRiskNums = new ArrayList<>();
        Map<String, Aggregation> result = searchResponse.getAggregations().getAsMap();
        Aggregation aggregation = result.get("warning_time");
        JSONArray jsonArray = JSONObject.parseObject(JSONObject.toJSONString(aggregation)).getJSONArray("buckets");
        for (int i = 0, len = jsonArray.size(); i < len; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            dayRiskNums.add(new DayRiskNum(jsonObject.getString("keyAsString"), jsonObject.getIntValue("docCount")));
        }
        dayRiskNums.sort(((o1, o2) -> o2.getDay().compareTo(o1.getDay())));
        return dayRiskNums;
    }

    private SearchResponse getDayRiskNumSearchResponse(String vehicleId, String startTime, String endTime) {

        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        //这里只查询id，不需要返回多余的详情
        searchSourceBuilder.fetchSource(false);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (StrUtil.isNotBlank(vehicleId)) {
            boolBuilder.must(QueryBuilders.termQuery("vehicle_id", vehicleId));
        }
        RangeQueryBuilder rangeQueryBuilder = getRangeQueryBuilder(startTime, endTime, "warning_time");
        rangeQueryBuilder.includeLower(true);
        rangeQueryBuilder.includeUpper(true);
        boolBuilder.filter(rangeQueryBuilder);

        AggregationBuilder aggregation =
            AggregationBuilders.dateHistogram("warning_time").field("warning_time").format("yyyy-MM-dd")
                .dateHistogramInterval(DateHistogramInterval.DAY).order(BucketOrder.key(false))
                .extendedBounds(new ExtendedBounds(Date8Utils.formatter(startTime), Date8Utils.formatter(endTime)));
        searchSourceBuilder.query(boolBuilder);
        searchSourceBuilder.aggregation(aggregation);
        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getMediaIdsByRiskId(Integer mediaType, String[] riskIds, String index) throws Exception {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[] { "id" }, null);
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termsQuery("risk_id", riskIds));
        boolQueryBuilder.must(QueryBuilders.termsQuery("media_type", mediaType.toString()));
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = esClient.search(searchRequest);
        SearchHits hits = search.getHits();
        List<String> ids = new ArrayList<>();
        for (SearchHit hit : hits) {
            ids.add(hit.getSourceAsMap().get("id").toString());
        }
        return ids;
    }

    /**
     * 通过风险id获取其他索引的id
     * @param fieldMap
     * @param indexName
     * @param id        id 字段 作为查询字段
     * @param idValues  ids值
     * @return
     */
    public Set<String> esGetRiskIdByEventId(List<String> fieldMap, String indexName, String id, String columnName,
        Integer status, String... idValues) {

        // es 查询
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.must(QueryBuilders.termsQuery(id, idValues));
        if (status != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("status", status));
        }

        String[] strings = fieldMap.toArray(new String[0]);
        searchSourceBuilder.fetchSource(strings, null);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        return new HashSet<>(
            scrollAllIds(searchRequest, (SearchHit[] hits, String columnN) -> addByteColumns(hits, columnName)));
    }

    private List<String> addByteColumns(SearchHit[] hits, String columnName) {
        List<String> columnValues = new ArrayList<>();
        String columnVal;
        for (SearchHit hit : hits) {
            columnVal = (String) hit.getSourceAsMap().get(columnName);
            if (columnVal != null) {
                columnValues.add(columnVal);
            }
        }
        return columnValues;
    }

    /**
     * 通过风险id获取其他索引的id
     * @param fieldMap
     * @param whereEqCondi
     * @param whereUneqCondi
     * @param indexName
     * @param id             id 字段 作为查询字段
     * @param idValues       ids值
     * @return
     */
    public List<String> esGetIdsByIds(List<String> fieldMap, Map<String, Object> whereEqCondi,
        Map<String, Object> whereUneqCondi, String indexName, String id, String... idValues) {

        // es 查询
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (StrUtil.isNotBlank(id)) {
            boolQueryBuilder.must(QueryBuilders.termsQuery(id, idValues));
        }
        if (whereEqCondi != null && whereEqCondi.size() > 0) {
            Set<Map.Entry<String, Object>> entries = whereEqCondi.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                boolQueryBuilder.must(QueryBuilders.termsQuery(entry.getKey(), entry.getValue()));
            }
        }
        if (whereUneqCondi != null && whereUneqCondi.size() > 0) {
            Set<Map.Entry<String, Object>> entries = whereUneqCondi.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                boolQueryBuilder.mustNot(QueryBuilders.termsQuery(entry.getKey(), entry.getValue()));
            }
        }
        String[] strings = fieldMap.toArray(new String[0]);
        searchSourceBuilder.fetchSource(strings, null);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        return scrollAllIds(searchRequest, this::addIds);
    }

    public SearchResponse riskDealData(Set<String> set, String startTime, String endTime) throws IOException {
        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        BoolQueryBuilder riskDealBuilder = getRiskDealBuilder(set, startTime, endTime);
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("status").field("status").size(10);
        AggregationBuilder aggregation2 = AggregationBuilders.terms("riskResult").field("risk_result").size(10);
        aggregation.subAggregation(aggregation2);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(riskDealBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    private BoolQueryBuilder getRiskDealBuilder(Set<String> set, String start, String end) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", set));
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("warning_time");
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lte(end);
        rangeQueryBuilder.includeLower(true);
        rangeQueryBuilder.includeUpper(false);
        boolBuilder.filter(rangeQueryBuilder);
        return boolBuilder;
    }

    /**
     * 通过报警标示号的 报警id 查询riskEventId
     * @param alarmId
     * @return
     */
    public String getRiskEventIdByAlarmId(String field, String alarmId) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termQuery(field, alarmId));
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest);
        } catch (Exception e) {
            logger.error("809报警附件目录请求应答获得riskEventId异常", e);
            return null;
        }
        Iterator<SearchHit> iterator = searchResponse.getHits().iterator();
        if (iterator.hasNext()) {
            SearchHit searchHit = iterator.next();
            JSONObject riskEventInfo = JSONObject.parseObject(String.valueOf(searchHit.getSourceAsString()));
            return riskEventInfo.getString("id");
        }
        return null;
    }

    public String getRiskEventIdTodo809(String plateFormId, String msgSn, String brand) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        BoolQueryBuilder boolBuilder = getBoolQueryBuilder(plateFormId, msgSn, brand);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest);
        } catch (Exception e) {
            logger.error("809报警附件目录请求应答获得riskEventId异常", e);
            return null;
        }
        //避免出现平台id和流水号不一一对应拿不到想要的数据
        // 每个查询对象
        for (SearchHit searchHit : searchResponse.getHits()) {
            JSONObject riskEventInfo = JSONObject.parseObject(String.valueOf(searchHit.getSourceAsString()));
            String[] plateFormIds = riskEventInfo.getString("plaId").split(",");
            String[] msgSns = riskEventInfo.getString("msgSn").split(",");
            for (int i = 0; i < plateFormIds.length; i++) {
                if (plateFormIds[i].equals(plateFormId) && msgSns[i].equals(msgSn)) {
                    return riskEventInfo.getString("id");
                }
            }
        }
        return null;
    }

    private BoolQueryBuilder getBoolQueryBuilder(String plateFormId, String msgSn, String brand) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termQuery("brand", brand));
        boolBuilder.must(QueryBuilders.termQuery("plaId", plateFormId));
        boolBuilder.must(QueryBuilders.termQuery("msgSn", msgSn));
        return boolBuilder;
    }

    /**
     * 通过车牌号获取  riskeventID
     * @param plateFormId 平台ID
     * @param time        时间
     * @param brand       车牌号
     * @return riskEventID
     */
    public String getRiskEventIdByBrandAndTime(String index, String plateFormId, String time, String brand) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        BoolQueryBuilder boolBuilder = getBoolQueryBuilderByPTB(plateFormId, time, brand);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest);
        } catch (Exception e) {
            logger.error("809智能视频报警附件目录请求应答获得riskEventId异常", e);
            return null;
        }
        //避免出现平台id和流水号不一一对应拿不到想要的数据
        // 每个查询对象
        for (SearchHit searchHit : searchResponse.getHits()) {
            JSONObject riskEventInfo = JSONObject.parseObject(String.valueOf(searchHit.getSourceAsString()));
            String[] plateFormIds = riskEventInfo.getString("platform_id").split(",");
            String[] times = riskEventInfo.getString("warning_time").split(",");
            for (int i = 0; i < plateFormIds.length; i++) {
                if (plateFormIds[i].equals(plateFormId) && times[i].equals(time)) {
                    return riskEventInfo.getString("id");
                }
            }
        }
        return null;
    }

    private BoolQueryBuilder getBoolQueryBuilderByPTB(String plateFormId, String time, String brand) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termsQuery("brand", brand));
        WildcardQueryBuilder builder = QueryBuilders.wildcardQuery("platform_id", "*" + plateFormId + "*");
        WildcardQueryBuilder builder1 = QueryBuilders.wildcardQuery("warning_time", "*" + time + "*");
        boolBuilder.filter(builder);
        boolBuilder.filter(builder1);
        return boolBuilder;
    }

    public List<AdasRiskEventEsBean> getEventEsBeanByRiskId(String[] riskIds, String indexName) {
        // es 查询
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (ArrayUtils.isNotEmpty(riskIds)) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("risk_id", riskIds));
        }
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        return scrollAllIds(searchRequest, this::addEsBeans);
    }

    /**
     * 原来sql方法改造，该方法暂时未找到地方调用
     * @param type
     * @param id   风险或者事件的id
     * @return
     */
    public List<AdasMediaEsBean> queryMediaRiskInfo(String type, String id) {
        // es 查询
        SearchRequest searchRequest = new SearchRequest(ADAS_MEDIA);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if ("riskId".equals(type)) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("risk_id", id));
            boolQueryBuilder.mustNot(QueryBuilders.existsQuery("media_id"));

        } else if ("riskEventId".equals(type)) {
            boolQueryBuilder.must(QueryBuilders.termsQuery("risk_event_id", id));
            boolQueryBuilder.must(QueryBuilders.existsQuery("media_id"));
        }

        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        return scrollAllIds(searchRequest, this::addMediaEsBeans);
    }

    /**
     * 根据驾驶员名称和驾驶员从业资格证 group by 查询
     * @param start
     * @param end
     * @return
     */
    public SearchResponse getDriverSearchResponse(String start, String end) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        RangeQueryBuilder rangeQueryBuilder = getRangeQueryBuilder(start, end, "warning_time");
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery().must(rangeQueryBuilder);
        if (boolBuilder == null) {
            return null;
        }
        TermsAggregationBuilder aggregation =
                AggregationBuilders.terms("driver").field("driver_name.keyword").size(100000);
        AggregationBuilder aggregation2 =
                AggregationBuilders.terms("driverNumber").field("driver_number.keyword").size(100000);
        aggregation.subAggregation(aggregation2);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            logger.error("获取驾驶员风险数据异常", e);
        }
        return null;
    }

    public long getRiskSizeByVehicleIds(Set<String> vehicleIds) {
        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("warning_time");
        String[] times = Date8Utils.getTimes(0, LocalDateTime.now());
        rangeQueryBuilder.gte(times[0]);
        rangeQueryBuilder.lt(times[1]);
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder =
            QueryBuilders.boolQuery().must(rangeQueryBuilder).must(QueryBuilders.termsQuery("vehicle_id", vehicleIds))
                .must(QueryBuilders.termQuery("status", 1));
        if (boolBuilder == null) {
            return 0;
        }
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = esClient.search(searchRequest);
        } catch (IOException e) {
            logger.error("获取驾驶员风险数异常", e);
            return 0;
        }
        if (searchResponse != null) {
            return searchResponse.getHits().getTotalHits();
        }
        return 0;
    }

    private SearchResponse getScoreSearchResponse(Object[] searchAfter, int limit, Set<String> vehicleIdSet, int time,
        boolean flag) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(limit);
        searchSourceBuilder.fetchSource(new String[] { "id" }, new String[] {});
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vehicleIdSet));
        List<String> timeList = monitorScoreService.conversionTime(time, true);
        // 时间范围
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("event_time");
        rangeQueryBuilder.gte(timeList.get(0));
        rangeQueryBuilder.lte(timeList.get(1));
        boolBuilder.filter(rangeQueryBuilder);
        searchSourceBuilder.sort("event_time", SortOrder.DESC);
        searchSourceBuilder.sort("id", SortOrder.DESC);
        if (!flag) {
            // 通过search_after 角标返回数据
            if (searchAfter != null && searchAfter.length == 2 && !StringUtils.isEmpty(searchAfter[0] + "")) {
                searchSourceBuilder.searchAfter(searchAfter);
            }
        } else {
            // 下载 通过scroll
            searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        }
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            logger.error("查询监控对象主动安全风险信息异常", e);
        }
        return null;
    }

    public SearchResponse getMonitorScoreSearch(String startTime, String endTime, Set<String> vehicleIdSet) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置hits的结果集为空
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolBuilder = getMonitorScoreBuilder(startTime, endTime, vehicleIdSet);
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("vehicleId").field("vehicle_id").size(100000);
        AggregationBuilder aggregation2 = AggregationBuilders.terms("eventType").field("event_type").size(300);
        aggregation.subAggregation(aggregation2);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            logger.error("获取监控对象评分数据异常", e);
        }
        return null;
    }

    public Map<String, Object> selectMonitorAlarmIds(MonitorScoreQuery query) {
        Map<String, Object> resultMap = new HashMap<>();
        Set<String> vehicleIdSet = new HashSet<>();
        vehicleIdSet.add(query.getVehicleId());
        SearchResponse searchResponse =
            getScoreSearchResponse(query.getSearchAfter(), query.getLimit(), vehicleIdSet, query.getTime(), false);
        Object[] sortValues = new Object[2];
        List<byte[]> ids = Lists.newArrayList();
        if (searchResponse != null) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits != null) {
                SearchHit hit;
                for (int i = 0, len = hits.length; i < len; i++) {
                    hit = hits[i];
                    if (hit.hasSource()) {
                        ids.add(UuidUtils.getBytesFromStr((String) hit.getSourceAsMap().get("id")));
                    }
                    // 记录游标
                    if (i == hits.length - 1 && query.getLimit() == hits.length) {
                        sortValues = hit.getSortValues();
                    }
                }
            }
        }
        resultMap.put("searchAfter", sortValues);
        resultMap.put("ids", ids);
        return resultMap;
    }

    /**
     * 查询报警id
     * @param vehicleId
     * @param time
     * @param size      最多可查询到的数量
     * @return
     * @throws IOException
     */
    public List<byte[]> selectAlarmIds(String vehicleId, int time, int size) {
        List<byte[]> alarmIds = Lists.newArrayList();
        Set<String> vehicleIdSet = new HashSet<>();
        vehicleIdSet.add(vehicleId);
        SearchResponse searchResponse = getScoreSearchResponse(null, size, vehicleIdSet, time, true);
        if (searchResponse != null) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits != null) {
                SearchHit hit;
                for (int i = 0, len = hits.length; i < len; i++) {
                    hit = hits[i];
                    if (hit.hasSource()) {
                        alarmIds.add(UuidUtils.getBytesFromStr((String) hit.getSourceAsMap().get("id")));
                    }
                }

            }
        }
        return alarmIds;
    }

    private void scrollData(List<String> alarmIds, SearchResponse searchResponse, SearchScrollRequest scroll)
        throws IOException {
        SearchHit[] hits;
        while (true) {
            hits = searchResponse.getHits().getHits();
            if (hits == null || hits.length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                if (hit.hasSource()) {
                    alarmIds.add((String) hit.getSourceAsMap().get("id"));
                }
            }
            scroll.scroll(TimeValue.timeValueMinutes(1L));
            searchResponse = esClient.searchScroll(scroll);
        }
    }

    public List<String> getDriverDetailResponse(AdasDriverScoreQuery query) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[] { "id" }, new String[] {});
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termQuery("driver_name", query.getDriverName()));
        boolBuilder.must(QueryBuilders.termQuery("driver_number", query.getCardNumber()));
        boolBuilder.must(QueryBuilders.termsQuery("group_id", query.getGroupIds()));
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("event_time");
        //时间待补充
        String start = Date8Utils.getFirstMonthDateTime(query.getTime(), "yyyy-MM-dd HH:mm:ss");
        String end = Date8Utils.getNextMonthDateTime(query.getTime(), "yyyy-MM-dd HH:mm:ss");
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lt(end);
        boolBuilder.filter(rangeQueryBuilder);
        searchSourceBuilder.size(5000);
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        return scrollLimitSizeIds(searchRequest, this::addIds, 5000);
    }

    @Data
    public class IcCardDriverEventInfo {
        private Object[] searchAfter;
        private List<String> ids;
    }

    public IcCardDriverEventInfo selectIcCardDriverEvents(AdasDriverScoreQuery query) {
        IcCardDriverEventInfo info = new IcCardDriverEventInfo();
        SearchResponse searchResponse = selectDriverDetailResponse(query, false);
        Object[] sortValues = new Object[2];
        List<String> ids = Lists.newArrayList();
        if (searchResponse != null) {
            SearchHit[] hits = searchResponse.getHits().getHits();
            if (hits != null) {
                SearchHit hit;
                for (int i = 0, len = hits.length; i < len; i++) {
                    hit = hits[i];
                    if (hit.hasSource()) {
                        ids.add((String) hit.getSourceAsMap().get("id"));
                    }
                    // 记录游标
                    if (i == hits.length - 1 && query.getLimit() == hits.length) {
                        sortValues = hit.getSortValues();
                    }
                }
            }
        }
        info.setIds(ids);
        info.setSearchAfter(sortValues);
        return info;
    }

    private SearchResponse selectDriverDetailResponse(AdasDriverScoreQuery query, boolean flag) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(query.getLimit().intValue());
        searchSourceBuilder.from(0);
        searchSourceBuilder.fetchSource(new String[] { "id" }, new String[] {});
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termQuery("driver_name", query.getDriverName()));
        boolBuilder.must(QueryBuilders.termQuery("driver_number", query.getCardNumber()));
        boolBuilder.must(QueryBuilders.termsQuery("group_id", query.getGroupIds()));
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("event_time");
        //时间待补充
        String start = Date8Utils.getFirstMonthDateTime(query.getTime(), "yyyy-MM-dd HH:mm:ss");
        String end = Date8Utils.getNextMonthDateTime(query.getTime(), "yyyy-MM-dd HH:mm:ss");
        rangeQueryBuilder.gte(start);
        rangeQueryBuilder.lt(end);
        boolBuilder.filter(rangeQueryBuilder);
        searchSourceBuilder.sort("event_time", SortOrder.DESC);
        searchSourceBuilder.sort("id", SortOrder.DESC);
        if (!flag) {
            // 通过search_after 角标返回数据
            if (query.getSearchAfter() != null && query.getSearchAfter().length == 2 && !StringUtils
                .isEmpty(query.getSearchAfter()[0] + "")) {
                searchSourceBuilder.searchAfter(query.getSearchAfter());
            }
        } else {
            // 下载 通过scroll
            searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        }
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            logger.error("查询监控对象主动安全风险信息异常", e);
        }
        return null;
    }

    public SearchResponse alarmAnalysisData(Set<String> set) throws IOException {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        String[] times = Date8Utils.getTimes(0, LocalDateTime.now());
        BoolQueryBuilder analysisBuilder = getAnalysisBuilder(set, times[0], times[1]);
        if (analysisBuilder == null) {
            return null;
        }
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("eventType").field("event_type").size(300);
        AggregationBuilder aggregation2 = AggregationBuilders.terms("status").field("status").size(10);
        // AggregationBuilder aggregation3 = AggregationBuilders.terms("oversee").field("oversee_status").size(10);
        aggregation.subAggregation(aggregation2);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(analysisBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    private BoolQueryBuilder getAnalysisBuilder(Set<String> set, String start, String end) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", set));
        RangeQueryBuilder rangeQueryBuilder = getRangeQueryBuilder(start, end, "event_time");
        rangeQueryBuilder.includeLower(true);
        rangeQueryBuilder.includeUpper(true);
        boolBuilder.filter(rangeQueryBuilder);
        return boolBuilder;
    }

    public SearchResponse riskAnalysisData(Set vehiclesIds, String start, String end) throws IOException {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        BoolQueryBuilder analysisBuilder = getAnalysisBuilder(vehiclesIds, start, end);
        if (analysisBuilder == null) {
            return null;
        }
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("eventType").field("event_type").size(300);
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(aggregation);
        searchSourceBuilder.query(analysisBuilder);
        searchRequest.source(searchSourceBuilder);
        return esClient.search(searchRequest);
    }

    /**
     * 从es中获取当天未处理的报警
     * @param vehicleList 当前用户下拥有权限的所有车辆
     * @param rbp         分页和riskId 等参数
     * @return 报警id
     */
    public List<String> getTodayUntreatedRisks(Set<String> vehicleList, AdasRiskBattleParam rbp) {
        SearchResponse searchResponse = getTodayUntreatedRiskSearchResponse(vehicleList, rbp);
        return getRiskIds(searchResponse);
    }

    /**
     * 从es中获取当天未处理的报警 -----组装searchResponse
     * @param vehicleIds 当前用户下拥有权限的所有车辆
     * @param rbp        分页和riskId 等参数
     * @return searchResponse
     */
    private SearchResponse getTodayUntreatedRiskSearchResponse(Collection<String> vehicleIds, AdasRiskBattleParam rbp) {
        LocalDateTime today = LocalDateTime.now();
        String startTime = Date8Utils.getMidnightHourTime(today);
        String endTime = Date8Utils.getCurrentTime(today);
        String riskIds = rbp.getRiskIds();

        SearchRequest searchRequest = new SearchRequest(RISK_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(1000);
        //这里只查询id，不需要返回多余的详情
        searchSourceBuilder.fetchSource(false);
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        //查询未处理所有的状态的风险
        boolBuilder.must(QueryBuilders.termQuery("status", RiskStatusEnum.UNTREATED.getCode()));
        boolBuilder.must(QueryBuilders.termsQuery("vehicle_id", vehicleIds));
        //需要过滤掉前台已经存在的风险id
        if (riskIds != null && !"".equals(riskIds.trim())) {
            boolBuilder.mustNot(QueryBuilders.termsQuery("risk_id", Arrays.asList(riskIds.split(","))));
        }
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("warning_time");
        rangeQueryBuilder.gte(startTime);
        rangeQueryBuilder.lte(endTime);
        rangeQueryBuilder.includeLower(true);
        rangeQueryBuilder.includeUpper(true);
        boolBuilder.filter(rangeQueryBuilder);
        searchSourceBuilder.query(boolBuilder);
        searchSourceBuilder.sort("risk_level", SortOrder.DESC);
        //企业要优先看到已经督办的未处理的风险
        // if (rbp.isEnterpriseFlag()) {
        //     searchSourceBuilder.sort("oversee_status", SortOrder.DESC);
        // }
        searchSourceBuilder.sort("warning_time", SortOrder.DESC);
        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> esGetRiskEventIdByRiskIds(List<String> riskIds) {
        return esGetIdsFromIndexByRiskIds(riskIds, RISK_EVENT_INDEX);
    }

    private List<String> esGetIdsFromIndexByRiskIds(List<String> riskIds, String index) {

        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termsQuery("risk_id", riskIds));
        return esGetScrollId(boolBuilder, index);
    }

    private List<String> esGetScrollId(BoolQueryBuilder boolBuilder, String index) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(false);
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        //设置scrollid保持一分钟
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        return scrollAllIds(searchRequest, this::addIds);
    }

    public SearchResponse getSearchResponseFunctionIdList(RiskStatisticsRecordQuery query, List<Integer> functionIdList,
        boolean exportFlag) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(query.getLimit());
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        boolBuilder.must(QueryBuilders.termsQuery("event_type", functionIdList));
        boolBuilder.must(QueryBuilders.termQuery("vehicle_id", query.getVehicleId()));
        RangeQueryBuilder rangeQueryBuilder =
            getRangeQueryBuilderEquals(query.getStartTime(), query.getEndTime(), "event_time");
        boolBuilder.filter(rangeQueryBuilder);
        searchSourceBuilder.query(boolBuilder);
        if (!exportFlag) {
            searchSourceBuilder.sort("event_time", SortOrder.DESC);
            if (query.getSearchAfter() != null && query.getSearchAfter().length != 0 && !StringUtils
                .isEmpty(query.getSearchAfter()[0] + "")) {
                searchSourceBuilder.searchAfter(query.getSearchAfter());
            }
        } else {
            searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        }
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        try {
            return esClient.search(searchRequest);
        } catch (IOException e) {
            logger.error("查询主动安全统计报表详情", e);
        }
        return null;
    }

    public Map<String, Object> searchAlarmInfoByFunctionIdList(RiskStatisticsRecordQuery query,
        List<Integer> functionIdList, String commonName, boolean exportFlag) throws IOException {
        SearchResponse res = getSearchResponseFunctionIdList(query, functionIdList, exportFlag);
        if (res != null) {
            Map<String, Object> resultMap = Maps.newHashMap();
            SearchHit[] hits = res.getHits().getHits();
            long total = res.getHits().getTotalHits();
            List<AdasStatisticsReportBean> dataList = new ArrayList<>();
            if (!exportFlag) {
                //查询通过searchAfter 分页
                searchAlarmInfoBySearchAfter(resultMap, hits, dataList, commonName);
            } else {
                //导出通过scroll
                searchAlarmInfoByScroll(res, dataList, commonName);
            }
            resultMap.put("total", total);
            resultMap.put("data", dataList);
            return resultMap;

        }
        return null;
    }

    private void searchAlarmInfoByScroll(SearchResponse res, List<AdasStatisticsReportBean> dataList, String commonName)
        throws IOException {
        SearchHit[] hits;
        SearchScrollRequest scroll = new SearchScrollRequest();
        while (true) {
            SearchHit hit;
            hits = res.getHits().getHits();
            scroll.scrollId(res.getScrollId());
            if (hits == null || hits.length == 0) {
                break;
            }
            for (int i = 0, len = hits.length; i < len; i++) {
                hit = hits[i];
                if (hit.hasSource()) {
                    AdasStatisticsReportBean adasStatisticsReportBean =
                        JSON.parseObject(hit.getSourceAsString(), AdasStatisticsReportBean.class);
                    adasStatisticsReportBean.setCommonName(commonName);
                    dataList.add(adasStatisticsReportBean);
                }
            }
            scroll.scroll(TimeValue.timeValueMinutes(1L));
            res = esClient.searchScroll(scroll);
        }
    }

    private void searchAlarmInfoBySearchAfter(Map<String, Object> resultMap, SearchHit[] hits,
        List<AdasStatisticsReportBean> dataList, String commonName) {
        Object[] sortValues = new Object[1];
        List<byte[]> ids = new ArrayList<>();
        if (hits != null) {
            SearchHit hit;
            for (int i = 0, len = hits.length; i < len; i++) {
                hit = hits[i];
                if (hit.hasSource()) {
                    AdasStatisticsReportBean adasStatisticsReportBean =
                        JSON.parseObject(hit.getSourceAsString(), AdasStatisticsReportBean.class);
                    adasStatisticsReportBean.setCommonName(commonName);
                    dataList.add(adasStatisticsReportBean);
                    ids.add(UuidUtils.getBytesFromStr(adasStatisticsReportBean.getRiskEventId()));
                }
                // 记录游标
                if (i == hits.length - 1) {
                    sortValues = hit.getSortValues();
                }
            }
        }
        resultMap.put("search_after", sortValues);
        resultMap.put("ids", ids);
    }

    /**
     * 根据报警id查询报警时间
     * @param alarmId
     * @return
     */
    public Long getWarnTimeByAlarmId(String alarmId) {
        SearchRequest searchRequest = new SearchRequest(RISK_EVENT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        searchSourceBuilder.fetchSource(new String[] { "event_time" }, new String[] {});
        boolBuilder.must(QueryBuilders.termQuery("alarm_id", alarmId));
        searchSourceBuilder.query(boolBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = getSearchResponse(searchRequest);
        if (searchResponse == null) {
            return null;
        }
        String eventTime = searchResponse.getHits().getAt(0).getSourceAsMap().get("event_time").toString();
        return DateUtil.getStringToLong(eventTime, null);
    }
}
