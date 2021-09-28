package com.zw.app.domain.monitor;

import lombok.Data;

import java.util.List;


@Data
public class WorkHourResult {
    private long time; // gps时间

    private List<Double> checkData; // 传感器瞬时流量

    private List<Integer> workingPosition; // 工作状态

    private List<Integer> effectiveData; // 是否是有效数据
}
