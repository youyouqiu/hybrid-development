package com.zw.platform.dto.platformInspection;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 *  巡检结果表
 * @author create by  on 2020-11-17.
 */
@Data
public class PlatformInspectionResultDTO {

    private String orgName;

    private String brand;

    /**
     * 从业资格证号
     */
    private String cardNumber;

    private String faceId;

    private String driverName;

    private String identificationResult;

    private String identificationType;

    private String matchRate;

    /**
    * id
    */
    private String id;

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

    private JSONObject imageInfo;

    private JSONObject videoInfo;


    /**
    * 时间
    */
    private String time;

    /**
    * 报警类型
    */
    private String alarmType;

    /**
    * 预警类型
    */
    private String warnType;

    /**
    * 是否有效提醒驾驶员
    */
    private Integer remindFlag;

    /**
    * 道路偏离类型（1 左侧偏离 2右侧偏离）
    */
    private String departureType;

    /**
    * 超速报警标志（1开始标志 2持续标志 3 结束标志）
    */
    private String speedStatus;

    /**
    * 道路偏离标志（1 开始标志，2持续标志，3结束标志)
    */
    private String departureStatus;

    /**
    * 线路/区域 id
    */
    private String routeId;

    private String route;


    /**
    * 禁行路段/区域报警标志(1 开始标志，2持续标志，3结束标志）
    */
    private String pohibitedStatus;

    /**
    * 禁行类型(1 禁行区域， 2禁行)
    */
    private String pohibitedType;

    /**
    * 驾驶员id
    */
    private String driverId;

    /**
    * 巡检类型（1.车辆运行监测巡检2.驾驶员驾驶行为监测巡检）
    */
    private Integer type;

}