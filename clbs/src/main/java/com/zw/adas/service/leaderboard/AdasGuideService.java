package com.zw.adas.service.leaderboard;

import com.alibaba.fastjson.JSONArray;
import com.zw.adas.domain.riskManagement.bean.AdasGroupRank;
import com.zw.adas.domain.riskManagement.bean.AdasVehicleRank;
import com.zw.platform.domain.core.OrganizationLdap;
import com.zw.platform.domain.leaderboard.DriverRank;

import java.util.List;
import java.util.Map;

public interface AdasGuideService {
    List<AdasVehicleRank> getVehicleRank();

    List<AdasGroupRank> getGroupRank();

    Map<String, OrganizationLdap> getGroupInfo(List<AdasGroupRank> groupRankList);

    Integer isPermission(String moduleName);

    /**
     * 热力图
     * @param type 1 昨日  2 上月  3 去年
     * @return
     */
    JSONArray getHotMapData(int type);

    List<DriverRank> getDriverRank(int limitSize);
}
