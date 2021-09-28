package com.zw.adas.domain.monitorScore;

import lombok.Data;


@Data
public class MonitorScoreEventInfo {
    /**
     * 报警数
     */
    private int total;

    /**
     * 事件name
     */
    private String eventName;

    /**
     * 风险类型name
     */
    private String riskName;

    private String functionId;

    /**
     * 导出时序号
     */
    private int index;
}
