package com.zw.platform.repository.vas;

import com.zw.platform.domain.riskManagement.form.RiskDisposeRecordForm;
import com.zw.platform.domain.riskManagement.query.RiskDisposeRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface RiskEvidenceDao {

    /**
     * 查询终端证据
     *
     * @param riskDisposeRecordQuery
     * @return
     */
    List<RiskDisposeRecordForm> queryDeviceRiskEvidence(RiskDisposeRecordQuery riskDisposeRecordQuery);

    /**
     * 查询风险证据
     *
     * @param riskDisposeRecordQuery
     * @return
     */
    List<RiskDisposeRecordForm> queryRiskEvidence(RiskDisposeRecordQuery riskDisposeRecordQuery);

    void updateMediaIds(@Param("id") String id, @Param("mediaId") String mediaId)
            throws Exception;

    List<Map<String, String>> getRiskIds();

    void updateRiskIds(Map<String, String> map)
            throws Exception;

    void updateRiskEventIds(Map<String, String> map)
            throws Exception;

    List<Map<String, String>> getMp4Medias();

    /**
     * 查询终端证据
     *
     * @param query
     * @return
     */
    List<String> queryDeviceRiskEvidenceBrands(RiskDisposeRecordQuery query);

    /**
     * 查询风险证据
     *
     * @param query
     * @return
     */
    List<String> queryRiskEvidenceBrands(RiskDisposeRecordQuery query);

    /**
     * 查询所有的风控证据
     */
    List<String> findAllRiskEvidence();

}
