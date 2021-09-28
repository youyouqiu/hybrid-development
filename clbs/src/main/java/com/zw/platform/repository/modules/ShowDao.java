package com.zw.platform.repository.modules;

import com.zw.platform.domain.leaderboard.RiskEvnet;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface ShowDao {
    RiskEvnet EventAndRisks(@Param("time") int time, @Param("id") String id);

    Map<String, Long> getRsRank(@Param("timeNode") int timeNode, @Param("adminGid") String adminGid);

    Integer getLineRate(long time);

    List<String> getVidsByGid(List<String> gid);

}
