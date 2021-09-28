package com.zw.adas.service.elasticsearch;

import com.alibaba.fastjson.JSONObject;
import com.zw.adas.domain.riskManagement.bean.AdasMediaEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEsBean;
import com.zw.adas.domain.riskManagement.bean.AdasRiskEventEsBean;
import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 功能描述:
 * @author zhengjc
 * @date 2019/5/30
 */



public interface AdasElasticSearchService {

    boolean esAddMediaBatch(List<AdasMediaEsBean> riskEsBean);

    boolean esUpdateRiskRealTime(AdasRiskEsBean riskEsBean);

    boolean esUpdateMediaBatch(List<? extends AdasRiskEsBean> list);

    boolean esUpdateRiskEvent(AdasRiskEventEsBean riskEsBean);

    boolean esUpdateRiskEventBatch(List<AdasRiskEventEsBean> list);

    boolean esUpdateRiskEventRealTimeBatch(List<AdasRiskEventEsBean> list);

    boolean esUpdateRiskBatch(List<AdasRiskEsBean> list);

    AdasRiskEsBean esGetRiskById(String id);

    AdasRiskEventEsBean esGetRiskEventById(String id);

    Map<String, Object> esQueryRiskInfo(AdasRiskDisposeRecordQuery rs, boolean flag) throws Exception;

    Map<String, Object> esQueryMediaInfo(AdasRiskDisposeRecordQuery rs, boolean flag) throws Exception;

    List<String> esQueryRiskEventIdsByRiskId(String... riskIds);

    List<String> esGetMediaIdsByRiskId(String type, String... riskIds);

    List<String> esGetMediaIdsByRiskId(Integer mediaType, boolean all, boolean mediaIdCondition,
                                       boolean visitIdCondition, String... riskIds);

    List<String> getMediaIdsByRiskId(Integer mediaType, String... riskIds) throws Exception;

    List<String> esGetMediaIdsByRiskEventId(String... riskEventIds);

    AdasRiskDisposeRecordForm esQueryInfoById(String id, String indexName) throws Exception;

    List<String> esQueryExportRiskEventId(AdasRiskDisposeRecordQuery rs);

    List<String> esGetTerminalEvidenceByRiskId(String riskId);

    List<String> esGetTerminalEvidenceByEventId(String riskEventId);

    List<String> esGetAllBrands(AdasRiskDisposeRecordQuery rs, String indexName) throws Exception;

    List<JSONObject> test(String sqlStr) throws Exception;

    List<Map<String, String>> getRiskDealInfo(List<String> vehicleIds, LocalDateTime startTime, boolean isToDay);

    List<String> esGetMediaIdsByEventIds(Integer mediaType, boolean all, boolean mediaIdCondition, String... eventIds);

    List<String> esqueryMediaIdsByRiskId(String... riskId);

    Set<String> esGetRiskIdByEventId(Integer status, String... eventIds);

    List<AdasRiskEventEsBean> getEventEsBeanByRiskId(String... riskIds);

}
