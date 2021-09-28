package com.zw.platform.domain.platformInspection;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 *  巡检结果表
 * @author create by  on 2020-11-17.
 */
@Data
public class PlatformInspectionResultDO {

    /**
     * id
     */
    private String id = UUID.randomUUID().toString();

    /**
     * 车id
     */
    private String vehicleId;

    /**
     * 图片附件地址
     */
    private String imageUrl;

    /**
     * 视频附件地址
     */
    private String videoUrl;

    /**
     * 时间
     */
    private Date time;

    /**
     * 报警类型
     */
    private Integer alarmType;

    /**
     * 预警类型
     */
    private Integer warnType;

    /**
     * 是否有效提醒驾驶员
     */
    private Integer remindFlag;

    /**
     * 道路偏离类型（1 左侧偏离 2右侧偏离）禁行类型(1 禁行区域， 2禁行)
     */
    private Integer type;

    /**
     * 标志（1开始标志 2持续标志 3 结束标志）
     */
    private Integer status;

    /**
     * 线路/区域 id
     */
    private String routeId;

    /**
     * 驾驶员id
     */
    private String driverId;

    /**
     * 巡检类型（1.车辆运行监测巡检2.驾驶员驾驶行为监测巡检）
     */
    private Integer inspectionType;
}