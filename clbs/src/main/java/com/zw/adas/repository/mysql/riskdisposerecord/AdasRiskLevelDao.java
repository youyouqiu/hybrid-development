package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.riskManagement.bean.AdasRiskLevelFromBean;
import com.zw.adas.domain.riskManagement.query.AdasRiskLevelQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 * Created by PengFeng on 2017/8/16  16:33
 */
public interface AdasRiskLevelDao {
    List<Map<String, String>> findRiskLevel(@Param("queryParam") AdasRiskLevelQuery query);

    void deleteRiskLevel(@Param("uuids") List<String> list);

    void insertRiskLevel(@Param("fromBean") AdasRiskLevelFromBean bean);

    void updateRiskLevel(@Param("fromBean") AdasRiskLevelFromBean bean);

    int isNotExist(@Param("fromBean") AdasRiskLevelFromBean bean);

    List<String> getAllLevelName();

    List<Map<String, String>> getRiskLevelMap();
}
