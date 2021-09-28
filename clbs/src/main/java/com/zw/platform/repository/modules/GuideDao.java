package com.zw.platform.repository.modules;

import com.zw.platform.domain.leaderboard.GroupRank;
import com.zw.platform.domain.leaderboard.VehicleRank;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface GuideDao {

    List<VehicleRank> getRankOfVehicle(@Param("vids") Set<String> vids, @Param("time") int time);

    List<Map<String, Integer>> getVehicleToal(@Param("vids") List<String> vids, @Param("time") int time);

    List<GroupRank> getRankOfGroup(@Param("vids") Set<String> vids, @Param("time") int time,
                                   @Param("assignmentIds") List<String> assignmentIds);

    List<GroupRank> getYesGroup(@Param("groupIds") List<String> groupIds, @Param("time") int time,
                                @Param("assignmentIds") List<String> assignmentIds);

    Integer countPermissionByRoles(@Param("roleList") List<String> roleList, @Param("resourceId") String resourceId);
}
