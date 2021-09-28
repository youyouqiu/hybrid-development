package com.zw.app.repository.mysql.alarm;

import com.zw.app.domain.alarm.MonitorAppInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface AppRiskRankDao {

    List<String> filterVehicleIds(@Param("mids") List<String> mids);

    Set<String> findAllVids();

    Set<String> findJingStandardVids();

    Set<String> findOtherStandardVids();

    List<String> findMidsBytype(@Param("type") String type);

    Set<String> findOtherVids();

    List<MonitorAppInfo> searchMonitor(@Param("userId") String userId, @Param("type") String type,
        @Param("search") String search, @Param("searchType") Integer searchType);

    List<String> searchAssignmentIs(@Param("userId") String uuid, @Param("type") String type);
}
