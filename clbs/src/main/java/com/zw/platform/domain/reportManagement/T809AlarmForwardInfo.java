package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;

/**
 * 809转发报警实体
 * @version 4.0.0
 */
@Data
public class T809AlarmForwardInfo implements Serializable {
    /**
     * 报警信息消息或车辆定位信息报文序列号(流水号)
     */
    private Integer msgSn;

    /**
     * 业务数据类型(0x1402 0x1403)
     */
    private Integer msgId;

    /**
     * 报警时间
     */
    private Long time;

    /**
     * 转发平台id
     */
    private String plateFormId;

    /**
     * 监控对象id
     */
    private String monitorId;

    /**
     * 监控对象名称
     */
    private String monitorName;

    /**
     * 监控对象车牌颜色
     */
    private Integer plateColor;

    /**
     * 监控对象所属企业id
     */
    private String groupId;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 报警开始时间
     */
    private Long alarmStartTime;

    /**
     * 报警的事件id
     */
    private String eventId;
}
