package com.zw.adas.repository.mysql.riskEvidence;

import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AdasRiskEvidenceDao {

    /**
     * 查询终端证据
     * @param adasRiskDisposeRecordQuery
     * @return
     */
    List<AdasRiskDisposeRecordForm> queryDeviceRiskEvidence(AdasRiskDisposeRecordQuery adasRiskDisposeRecordQuery);

    /**
     * 查询风险证据
     * @param adasRiskDisposeRecordQuery
     * @return
     */
    List<AdasRiskDisposeRecordForm> queryRiskEvidence(AdasRiskDisposeRecordQuery adasRiskDisposeRecordQuery);

    void updateMediaIds(@Param("id") String id, @Param("mediaId") String mediaId) throws Exception;

    List<Map<String, String>> getRiskIds();

    void updateRiskIds(Map<String, String> map) throws Exception;

    void updateRiskEventIds(Map<String, String> map) throws Exception;

    List<Map<String, String>> getMp4Medias();

    /**
     * 查询终端证据
     * @param query
     * @return
     */
    List<String> queryDeviceRiskEvidenceBrands(AdasRiskDisposeRecordQuery query);

    /**
     * 查询风险证据
     * @param query
     * @return
     */
    List<String> queryRiskEvidenceBrands(AdasRiskDisposeRecordQuery query);

    /**
     * 查询所有的风控证据
     */
    List<String> findAllRiskEvidence();

}
