package com.zw.platform.domain.generalCargoReport;

import lombok.Data;

/**
 * @author penghj
 * @version 1.0
 * @date 2021/4/13 17:54
 */
@Data
public class ViolationRecordDO {
    /**
     * 企业名称
     */
    private String orgName;
    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 驾驶员
     */
    private String driverName;

    /**
     * 时间(格式:yyyyMMddHHmmssSSS)
     */
    private String time;
    private String timeStr;

    /**
     * 位置-结构化地址
     */
    private String address;

    /**
     * 报警位置-经纬度
     */
    private String location;

    /**
     * 违章报警事由
     * 1:超速 2:疲劳 3:其他
     */
    private Integer violationReason;

    /**
     * 短信简要提醒内容
     */
    private String msg;

    /**
     * 违章处置情况
     */
    private String dealInfo;

    /**
     * GPS故障及异常类型
     */
    private String gpsInfo;

    /**
     * GPS故障及异常处置情况
     */
    private String gpsDealInfo;
}
