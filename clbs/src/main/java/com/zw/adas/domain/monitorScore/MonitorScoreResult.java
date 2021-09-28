package com.zw.adas.domain.monitorScore;

import lombok.Data;

import java.util.List;


@Data
public class MonitorScoreResult {
    private MonitorAggregateInfo monitorAggregateInfo;

    private List<MonitorScore> monitorScoreList;

}
