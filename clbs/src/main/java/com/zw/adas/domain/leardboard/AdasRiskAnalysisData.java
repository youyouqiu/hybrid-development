package com.zw.adas.domain.leardboard;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AdasRiskAnalysisData {
    private long crash;//碰撞

    private long exception;//异常

    private long distraction;//分心

    private long tired;//疲劳

    private long total;//报警总数

    List<Map<String, Integer>> eventData;

    public AdasRiskAnalysisData(long total, long crash, long exception, long distraction, long tired,
        List<Map<String, Integer>> eventData) {
        this.total = total;
        this.crash = crash;
        this.exception = exception;
        this.distraction = distraction;
        this.tired = tired;
        this.eventData = eventData;
    }
}
