package com.zw.app.service.activeSecurity;

import com.zw.app.domain.activeSecurity.DayRiskNum;
import com.zw.app.domain.activeSecurity.DealInfo;
import com.zw.app.domain.activeSecurity.MediaInfo;
import com.zw.app.domain.activeSecurity.Risk;
import com.zw.app.domain.activeSecurity.RiskEvent;
import com.zw.app.entity.methodParameter.DayRiskDetailEntity;
import com.zw.app.entity.methodParameter.DayRiskEntity;

import java.util.List;

public interface ActiveSecurityService {
    List<Risk> getRiskList(long pageNum, long pageSize, String riskIds);

    List<Risk> getRisks(List<String> riskIds);

    List<RiskEvent> getRiskEventByRiskId(String riskId);

    DealInfo getDealInfo();

    List<MediaInfo> getMediaInfo(String riskId, int mediaType);

    boolean saveRiskDealInfo(String riskId, Integer riskResult) throws Exception;

    List<DayRiskNum> getDayRiskNum(DayRiskEntity dayRiskEntity);

    List<Risk> getDayRiskDetail(DayRiskDetailEntity dayRiskDetailEntity);
}
