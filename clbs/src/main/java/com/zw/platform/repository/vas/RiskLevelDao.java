package com.zw.platform.repository.vas;

import com.zw.platform.domain.riskManagement.RiskLevelFromBean;
import com.zw.platform.domain.riskManagement.query.RiskLevelQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * Created by PengFeng on 2017/8/16  16:33
 */
public interface RiskLevelDao {
    List<Map<String, String>> findRiskLevel(@Param("queryParam") RiskLevelQuery query);

    void deleteRiskLevel(@Param("uuids") List<String> list);

    void insertRiskLevel(@Param("fromBean") RiskLevelFromBean bean);

    void updateRiskLevel(@Param("fromBean") RiskLevelFromBean bean);

    int isNotExist(@Param("fromBean") RiskLevelFromBean bean);

    List<String> getAllLevelName();

    List<Map<String, String>> getRiskLevelMap();
}
