package com.zw.app.service.activeSecurity;

import com.zw.app.domain.alarm.DealRiskNum;
import com.zw.app.domain.alarm.RiskRankResult;

import java.util.List;


public interface AppDealRankService {
    List<RiskRankResult> getDealRank(String vehicleIds, String startTime, String endTime, Integer status)
        throws Exception;

    DealRiskNum getDealNum(String vehicleIds, String startTime, String endTime) throws Exception;
}
