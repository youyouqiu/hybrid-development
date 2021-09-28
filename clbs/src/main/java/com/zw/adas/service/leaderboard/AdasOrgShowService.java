package com.zw.adas.service.leaderboard;

import com.zw.adas.domain.leardboard.AdasOrgRiskEvent;
import com.zw.adas.domain.leardboard.AdasOrgRiskType;
import com.zw.adas.domain.leardboard.AdasOrgVehOnline;
import com.zw.platform.domain.leaderboard.CustomerService;
import com.zw.platform.util.common.JsonResultBean;

import java.text.ParseException;
import java.util.List;
import java.util.Map;


public interface AdasOrgShowService {
    int getNowRiskNum();

    int getYesterdayRiskNum();

    JsonResultBean getRingRatioRiskEvent();

    int getVehicleOnlie();

    JsonResultBean getLineRate();

    List<Map<String, String>> getOperCag()
        throws ParseException;

    List<Map<String, String>> getEventRanking(String groupId, boolean isToday);

    List<Map<String, String>> getRiskProportion(String groupId, boolean isToday);

    List<AdasOrgVehOnline> getVehOnlineTrend(String groupId, boolean isToday);

    List<AdasOrgRiskEvent> getEventTrend(String groupId, boolean isToday);

    List<AdasOrgRiskType> getRiskTypeTrend(String groupId, boolean isToday);

    List<Map<String, String>> getRiskDealInfo(String groupId, boolean isToday);

    List<CustomerService> getCustomerServiceTrend(boolean isToday);
}

