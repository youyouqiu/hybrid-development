package com.zw.adas.domain.monitorScore;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

public class MonitorAlarmInfo {
    /**
     * 风险等级
     */

    private static Map<String, String> riskLevelMap = Maps.newHashMap();

    static {
        riskLevelMap.put("1", "一般(低)");
        riskLevelMap.put("2", "一般(中)");
        riskLevelMap.put("3", "一般(高)");
        riskLevelMap.put("4", "较重(低)");
        riskLevelMap.put("5", "较重(中)");
        riskLevelMap.put("6", "较重(高)");
        riskLevelMap.put("7", "严重(低)");
        riskLevelMap.put("8", "严重(中)");
        riskLevelMap.put("9", "严重(高)");
        riskLevelMap.put("10", "特重(低)");
        riskLevelMap.put("11", "特重(中)");
        riskLevelMap.put("12", "特重(高)");

    }

    @Getter
    @Setter
    private String driverName;

    @Getter
    @Setter
    private String eventType;

    @Getter
    @Setter
    private String riskLevel;

    @Getter
    @Setter
    private String eventTime;

    @Getter
    @Setter
    private String speed;

    @Getter
    @Setter
    private String address;

    @Getter
    @Setter
    private String vehicleId;

    /**
     * 导出时序号
     */
    @Getter
    @Setter
    private int index;

}
