package com.zw.platform.repository.modules;

import com.zw.adas.domain.riskManagement.bean.AdasGroupRank;
import com.zw.platform.domain.leaderboard.DriverRank;
import com.zw.platform.domain.leaderboard.GroupRank;
import com.zw.platform.domain.leaderboard.VehicleRank;
import com.zw.platform.domain.reportManagement.query.AdasAlarmRankQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;


public interface AdasAlarmRankDao {
    /**
     * 获取企业排行榜
     * @param orgIds
     * @param startTime
     * @param endTime
     * @param groupIds
     * @return
     */
    List<AdasGroupRank> getGroupRank(@Param("orgIds") String[] orgIds, @Param("startTime") String startTime,
        @Param("endTime") String endTime, @Param("groupIds") List<String> groupIds);

    int getRankTotal(@Param("vids") Set<String> vids, @Param("startTime") String startTime,
        @Param("endTime") String endTime);

    List<VehicleRank> getVehicleRank(@Param("vehicleIds") String[] vehicleIds, @Param("startTime") String startTime,
        @Param("endTime") String endTime);

    List<GroupRank> getRankOfGroup(AdasAlarmRankQuery query);

    List<VehicleRank> getRankOfVehicle(AdasAlarmRankQuery query);

    List<DriverRank> findADriverByOrgUUidSet(@Param("orgUUidSet") Set<String> orgUUidSet,
        @Param("queryParam") String queryParam);
}
