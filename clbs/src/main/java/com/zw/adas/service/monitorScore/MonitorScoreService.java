package com.zw.adas.service.monitorScore;

import com.zw.adas.domain.monitorScore.MonitorAlarmInfo;
import com.zw.adas.domain.monitorScore.MonitorScore;
import com.zw.adas.domain.monitorScore.MonitorScoreEventInfo;
import com.zw.adas.domain.monitorScore.MonitorScoreInfo;
import com.zw.adas.domain.monitorScore.MonitorScoreQuery;
import com.zw.adas.domain.monitorScore.MonitorScoreResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface MonitorScoreService {
    MonitorScoreResult list(String orgId, int time);

    MonitorScoreInfo scoreInfo(String vehicleId, int time);

    Map<String, Object> monitorAlarmInfo(MonitorScoreQuery query);

    List<String> conversionTime(int time, boolean type);

    List<MonitorAlarmInfo> monitorAlarmInfoList(String vehicleId, int time) throws IOException;

    List<MonitorScoreEventInfo> eventTypeList(String vehicleId, int time);

    List<MonitorScoreInfo> scoreInfoList(Set<String> vehicleIdSet, int time);

    void setAlarmRatioStr(MonitorScoreInfo monitorScoreInfo);

    void setScoreRingRatioStr(MonitorScoreInfo monitorScoreInfo);

    void setHundredsAlarmRingRatioStr(MonitorScoreInfo monitorScoreInfo);

    Map<String, List<MonitorAlarmInfo>> monitorAlarmInfoMap(Set<String> vehicleIdSet, int time);

    Map<String, List<MonitorScoreEventInfo>> eventTypeMap(Set<String> vehicleIdSet, int time);

    List<MonitorScore> exportList(String groupId, int time);

    List<MonitorScore> sort(String groupId, int time, String parameter, boolean isDownSort);
}
