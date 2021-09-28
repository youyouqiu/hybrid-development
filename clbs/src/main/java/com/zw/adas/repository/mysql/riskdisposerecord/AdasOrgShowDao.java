package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.leardboard.AdasOrgRiskEvent;
import com.zw.adas.domain.leardboard.AdasOrgRiskType;
import com.zw.adas.domain.leardboard.AdasOrgVehOnline;
import com.zw.adas.domain.riskManagement.bean.AdasOrgEvent;
import com.zw.platform.domain.leaderboard.RiskProportion;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AdasOrgShowDao {
    AdasOrgEvent getEventRanking(@Param("startTime") long startTime, @Param("endTime") long endTime,
        @Param("userId") String userId, @Param("groupIds") List<String> groupIds);

    RiskProportion getRiskProportion(@Param("startTime") long startTime, @Param("endTime") long endTime,
        @Param("userId") String userId, @Param("groupIds") List<String> groupIds);

    List<AdasOrgVehOnline> getVehOnlineTrend(@Param("startTime") long startTime, @Param("endTime") long endTime,
        @Param("groupIds") List<String> groupIds, @Param("userId") String userId);

    List<AdasOrgRiskEvent> getRiskTrend(@Param("startTime") long startTime, @Param("endTime") long endTime,
        @Param("groupIds") List<String> groupIds, @Param("userId") String userId);

    List<AdasOrgRiskType> getRiskTypeTrend(@Param("startTime") long startTime, @Param("endTime") long endTime,
        @Param("groupIds") List<String> groupIds, @Param("userId") String userId);

    List<String> getUserVidByGroupId(@Param("groupIds") List<String> groupIds, @Param("userId") String userId);

    List<Map<String, String>> getFieldNameMap();
}
