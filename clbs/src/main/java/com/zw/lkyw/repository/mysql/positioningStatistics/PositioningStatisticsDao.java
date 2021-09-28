package com.zw.lkyw.repository.mysql.positioningStatistics;

import com.zw.lkyw.domain.positioningStatistics.MonitorPositioningInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface PositioningStatisticsDao {

    List<MonitorPositioningInfo> findAllInfoByGroupIdSet(@Param("groupIdSet") Set<String> groupIdSet,
        @Param("search") String search);

    Set<String> findAllMonitorIdByGroupId(@Param("groupId") String groupId, @Param("search") String search);
}
