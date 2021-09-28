package com.zw.platform.domain.functionconfig;


import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * <p> Title: 电子围栏绑定实体 </p> <p> Copyright: Copyright (c) 2016 </p> <p> Company: ZhongWei </p> <p> team:
 * ZhongWeiTeam </p>
 * @author: wangying
 * @date 2016年8月8日上午10:05:25
 * @version 1.0
 */
@Data
public class FenceConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 围栏车辆关联
     */
    private String id;

    /**
     * 围栏ID
     */
    private String fenceId;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 驶入报警给平台
     */
    private Integer alarmInPlatform;

    /**
     * 驶出报警给平台
     */
    private Integer alarmOutPlatform;

    /**
     * 报警开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date alarmStartTime;

    /**
     * 限速
     */
    private Integer speed;

    /**
     * 报警开始时间点
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Date alarmStartDate;

    /**
     * 报警结束时间点
     */
    @DateTimeFormat(pattern = "HH:mm:ss")
    private Date alarmEndDate;

    private Integer flag;

    private Date createDataTime;

    private String createDataUsername;

    private Date updateDataTime;

    private String updateDataUsername;

    /**
     * 报警结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date alarmEndTime;

    // 轨迹查询--add by liubq 2016-08-09
    private String fenceConfigId; // 围栏id

    private String carLicense; // 车牌号

    private String lineName; // 路线名称

    private String lineDescription; // 路线描述

    private String lineType; // 路线类型

    private String reTableName; // 关联表名

    private String shapeId; // 形状id

    private String lineWidth; // 线路宽度

    /**
     * 限速时长
     */
    private Integer overSpeedLastTime;

    private Integer sendFenceType; // 下发类型：0 ：更新区域；1：追加区域；2：修改区域

    private Integer alarmSource; // 报警来源：0：终端报警； 1：平台报警

    private Integer travelLongTime; // 路段行驶过长阈值(s),仅线路才能编辑

    private Integer travelSmallTime; // 路段行驶不足阈值（s），仅线路才能编辑

    private Integer alarmInDriver; // 1:驶入报警给驾驶员

    private Integer alarmOutDriver; // 1:驶出报警给驾驶员

    private Integer openDoor; // 允许开门(0：允许开门；1：禁止开门)，线路不能设置

    private Integer communicationFlag; // 0：进区域开启通信模块；1：进区域关闭通信模块

    private Integer gnssFlag; // 0：进区域不采集GNSS 详细定位数据；1：进区域采集GNSS 详细定位数据

    private Integer sendDownId;// 围栏下发到设备的hashCode值

    /**
     * 限速夜间最高速度时长(3658新增)
     */
    private Integer nightMaxSpeed;

    /**
     * 夜间限速时间段(3658新增)
     */
    private String nightLimitTime;


}
