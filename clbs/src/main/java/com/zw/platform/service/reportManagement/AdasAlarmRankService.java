package com.zw.platform.service.reportManagement;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.zw.adas.domain.riskManagement.bean.AdasGroupRank;
import com.zw.platform.domain.leaderboard.DriverRank;
import com.zw.platform.domain.leaderboard.VehicleRank;
import com.zw.platform.domain.reportManagement.query.AdasAlarmRankQuery;
import org.elasticsearch.action.search.SearchResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface AdasAlarmRankService {
    //List<GroupRank> getGroupRank(AdasAlarmRankQuery query);

    //  List<VehicleRank> getVehicleRank(AdasAlarmRankQuery query);

    boolean exportGroupRank(String title, int type, HttpServletResponse res, List<AdasGroupRank> pis)
        throws IOException;

    boolean exportVehicleRank(String title, int type, HttpServletResponse response, List<VehicleRank> vehicleRank)
        throws IOException;

    boolean exportDriverRank(String title, int type, HttpServletResponse response, List<DriverRank> driverRank)
        throws IOException;

    List<AdasGroupRank> getGroupRank(String groupId, String startTime, String endTime);

    List<VehicleRank> getVehicleRank(String vehicleIds, String startTime, String endTime);

    Page<AdasGroupRank> getRankOfGroup(AdasAlarmRankQuery query);

    Page<VehicleRank> getRankOfVehicle(AdasAlarmRankQuery query);

    List<DriverRank> getDriverRank(String driverIds, String startTime, String endTime);

    Map<String, Integer> buildDriverAlarm(SearchResponse searchResponse);

    List<DriverRank> getAllDriverRank(Map<String, String> orgNameMap);

    JSONArray driverTree(String queryParam);
}
