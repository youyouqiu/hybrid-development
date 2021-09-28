package com.zw.adas.service.defineSetting;

import com.zw.adas.domain.riskManagement.bean.AdasRiskLevelFromBean;
import com.zw.adas.domain.riskManagement.query.AdasRiskLevelQuery;

import java.util.List;
import java.util.Map;

/**
 * Created by PengFeng on 2017/8/16  16:04
 */
public interface AdasRiskLevelService {
    List<Map<String, String>> getRiskLevel(AdasRiskLevelQuery query);

    void deleteLevels(String[] levelIds);

    void addRiskLevel(AdasRiskLevelFromBean bean);

    void updateRiskLevel(AdasRiskLevelFromBean bean);

    boolean isNotExsit(AdasRiskLevelFromBean bean);

    List<String> getAllLevelName();
}
