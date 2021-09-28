package com.zw.platform.dto.reportManagement;

import lombok.Data;


/**
 * Acc统计报表详情DTO
 * @author tianzhangxu
 */
@Data
public class AccStatisticDetailDTO {

    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * acc开启时间yyyy-MM-dd HH:mm:ss
     */
    private String openTime;

    /**
     * acc关闭时间yyyy-MM-dd HH:mm:ss
     */
    private String closeTime;

    /**
     * 持续时长（*天*小时*分钟*秒）
     */
    private String duration;

    /**
     * 行驶公里数（km）
     */
    private Double mile;

    /**
     * ACC开启经度
     */
    private Double openLongitude;

    /**
     * ACC开启纬度
     */
    private Double openLatitude;

    /**
     * ACC关闭经度
     */
    private Double closeLongitude;

    /**
     * ACC关闭纬度
     */
    private Double closeLatitude;

    /**
     * ACC开启位置
     */
    private String openLocation;

    /**
     * ACC关闭位置
     */
    private String closeLocation;

}
