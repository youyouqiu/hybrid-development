package com.zw.platform.domain.vas.f3;

import lombok.Data;

/**
 * 风险事件缓存实体
 * lijie
 */
@Data
public class RiskEventInfo {
    private String vehicleId;//车id
    private String brand;//车牌
    private String riskEventId;//风险事件id
    private String riskId;//风险id
    private Integer mediaType;//媒体类型
    private long warmTime;//风险时间
    private String riskType;//风险类型
    private Integer riskLevel;//风险等级
    private String address;//风险位置
    private String riskNumber;//风险编号
    private Integer eventId;//风险事件类型
    private String eventNumber;//风险事件编号
    private String resultPath;//ftp路径
    private String driver;//司机姓名

    public RiskEventInfo() {
    }

    public RiskEventInfo(String vehicleId, String brand, String riskEventId, String riskId, Integer mediaType,
                         long warmTime, String riskType, Integer riskLevel, String address, String riskNumber,
                         Integer eventId, String eventNumber, String resultPath, String driver) {
        this.vehicleId = vehicleId;
        this.brand = brand;
        this.riskEventId = riskEventId;
        this.riskId = riskId;
        this.mediaType = mediaType;
        this.warmTime = warmTime;
        this.riskType = riskType;
        this.riskLevel = riskLevel;
        this.address = address;
        this.riskNumber = riskNumber;
        this.eventId = eventId;
        this.eventNumber = eventNumber;
        this.resultPath = resultPath;
        this.driver = driver;
    }
}