package com.zw.adas.repository.mysql.modules;

import com.zw.adas.domain.leardboard.AdasAlarmTimesData;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AdasRiskManageDao {

    Map<String, BigDecimal> getEventCount(@Param("assignSet") Set assignSet, @Param("time") long time);

    List<AdasAlarmTimesData> getHistoryAlarmTimes(@Param("assignSet") Set assignSet, @Param("startTime") long startTime,
        @Param("endTime") long endTime);



}
