package com.zw.adas.service.riskEvidence;

// import com.zw.platform.domain.riskManagement.form.AdasRiskDisposeRecordForm;
// import com.zw.platform.domain.riskManagement.query.AdasRiskDisposeRecordQuery;

import com.zw.adas.domain.riskManagement.form.AdasRiskDisposeRecordForm;
import com.zw.adas.domain.riskManagement.query.AdasRiskDisposeRecordQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AdasRiskEvidenceService {

    Set<String> queryBrandsFromHb(AdasRiskDisposeRecordQuery adasRiskDisposeRecordQuery) throws Exception;

    boolean export(HttpServletResponse response, List<AdasRiskDisposeRecordForm> riskDisposeRecordForms,
        String evidentType) throws Exception;

    void updateMediaIds() throws Exception;

    void updateRiskIds() throws Exception;

    void updateRiskEventIds() throws Exception;

    boolean updateRiskEvidenceNameOnFtp();

    boolean canDownload(String id, boolean isJpeg);

    Map<String, Object> queryRiskEvidenceFromHb(AdasRiskDisposeRecordQuery query, boolean downloadOrNot)
        throws Exception;
}
