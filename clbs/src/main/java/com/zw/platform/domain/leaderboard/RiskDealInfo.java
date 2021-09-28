package com.zw.platform.domain.leaderboard;

import com.zw.platform.util.common.ComputingUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class RiskDealInfo {
    private int total;

    private int untreated;

    private int successFile;

    private int failedFile;

    private int accidentFile;

    private int treated;

    public List<Map<String, String>> getRiskDealInfoList(RiskDealInfo riskDealInfo, boolean isVip) {
        List<Map<String, String>> result = new ArrayList<>();
        if (isVip) {
            result.add(getRiskResultMap(RiskResultEnum.SUCCESS_FILE, successFile, riskDealInfo.successFile));
            result.add(getRiskResultMap(RiskResultEnum.FAILED_FILE, failedFile, riskDealInfo.failedFile));
        } else {

            result.add(getRiskStatusMap(RiskStatus.TREATED, treated));
        }
        result.add(getRiskStatusMap(RiskStatus.UNTREATED, untreated));

        return result;
    }

    private Map<String, String> getRiskResultMap(RiskResultEnum riskresult, int todayData, int yesterdayData) {
        Map<String, String> result = new HashMap<>();
        result.put("name", riskresult.getName());
        result.put("proportion", ComputingUtils.calProportion(todayData, total));
        result.put("ringRatio", ComputingUtils.calRingRatio(todayData, yesterdayData));
        result.put("number", todayData + "");
        return result;
    }

    private Map<String, String> getRiskStatusMap(RiskStatus riskStatus, int value) {
        Map<String, String> result = new HashMap<>();
        result.put("name", riskStatus.getName());
        result.put("proportion", ComputingUtils.calProportion(value, total));
        result.put("number", value + "");
        return result;
    }

    public void calAndSetTotal() {
        total = untreated + successFile + failedFile + accidentFile;
        treated = successFile + failedFile + accidentFile;
    }

}
