package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.monitorScore.MonitorScore;
import com.zw.adas.domain.monitorScore.MonitorScoreInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;


public interface MonitorScoreDao {
    List<MonitorScore> getMonitorScoreDataListByGroupIds(@Param("groupIds") Set<String> groupIds,
        @Param("time") int time);

    MonitorScoreInfo getMonitorScoreInfo(@Param("vehicleId") String vehicleId, @Param("time") int time);

    List<MonitorScoreInfo> getMonitorScoreInfoList(@Param("vehicleIdSet") Set<String> vehicleIdSet,
        @Param("time") int time);

    List<MonitorScore> sortByAverageTravelTime(@Param("groupIds") Set<String> groupIds, @Param("time") int time,
        @Param("parameter") String parameter, @Param("isDownSort") boolean isDownSort);
}
