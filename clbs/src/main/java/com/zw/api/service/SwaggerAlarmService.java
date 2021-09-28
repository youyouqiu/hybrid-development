package com.zw.api.service;

import com.zw.api.domain.AlarmInfo;

import java.time.LocalDateTime;
import java.util.List;

public interface SwaggerAlarmService {
    List<AlarmInfo> findAlarms(String monitorName, LocalDateTime startTime, LocalDateTime stopTime, int type);
}
