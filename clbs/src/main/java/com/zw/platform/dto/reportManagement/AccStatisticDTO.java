package com.zw.platform.dto.reportManagement;

import lombok.Data;


/**
 * Acc统计报表日表DTO
 * @author tianzhangxu
 */
@Data
public class AccStatisticDTO {
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 企业名称
     */
    private String orgName;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * acc开启次数
     */
    private Integer openNum;

    /**
     * 持续时长（*天*小时*分钟*秒）
     */
    private String duration;

    /**
     * 行驶公里数（km）
     */
    private Double mile;

}
