package com.zw.adas.repository.mysql.riskdisposerecord;

import com.zw.adas.domain.riskManagement.bean.AdasGroupRank;
import com.zw.adas.domain.riskManagement.bean.AdasVehicleRank;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface AdasGuideDao {

    List<AdasVehicleRank> getRankOfVehicle(@Param("vids") Set<String> vids, @Param("time") int time);

    List<Map<String, Integer>> getVehicleToal(@Param("vids") List<String> vids, @Param("time") int time);

    List<AdasGroupRank> getRankOfGroup(@Param("vids") Set<String> vids, @Param("time") int time,
        @Param("assignmentIds") List<String> assignmentIds);

    List<AdasGroupRank> getYesGroup(@Param("groupIds") List<String> groupIds, @Param("time") int time,
        @Param("assignmentIds") List<String> assignmentIds);

    Integer countPermissionByRoles(@Param("roleList") List<String> roleList, @Param("resourceId") String resourceId);

    String getHotMapContent(long time);
}
