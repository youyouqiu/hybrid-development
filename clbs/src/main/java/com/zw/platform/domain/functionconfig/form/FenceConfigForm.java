package com.zw.platform.domain.functionconfig.form;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * Title: 围栏绑定Form
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 * @version 1.0
 * @author: wangying
 * @date 2016年8月8日上午10:08:42
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FenceConfigForm extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 围栏ID
     */
    private String fenceId;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 监控对象类型
     */
    private String monitorType;

    /**
     * 驶入报警
     */
    private Short alarmInPlatform;

    /**
     * 驶出报警
     */
    private Short alarmOutPlatform;

    /**
     * 报警开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date alarmStartTime;

    /**
     * 报警结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date alarmEndTime;

    /**
     * 限速
     */
    private Integer speed;

    /**
     * 限速时长
     */
    private Integer overSpeedLastTime;

    /**
     * 报警开始时间点
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date alarmStartDate;

    /**
     * 报警结束时间点
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date alarmEndDate;

    /**
     * 下发状态
     */
    private Integer status = 0;

    private Integer sendFenceType; // 下发类型：0 ：更新区域；1：追加区域；2：修改区域

    private Integer alarmSource; // 报警来源 ： 0：终端报警； 1：平台报警

    private Integer travelLongTime; // 路段行驶过长阈值(s),仅线路才能编辑

    private Integer travelSmallTime; // 路段行驶不足阈值（s），仅线路才能编辑

    private Integer alarmInDriver;  // 1:驶入报警给驾驶员

    private Integer alarmOutDriver;  // 1:驶出报警给驾驶员

    private Integer openDoor; // 允许开门(0：允许开门；1：禁止开门)，线路不能设置

    private Integer communicationFlag; // 0：进区域开启通信模块；1：进区域关闭通信模块 2:平台与终端

    private Integer gnssFlag; // 0：进区域不采集GNSS 详细定位数据；1：进区域采集GNSS 详细定位数据

    private Integer sendDownId; // 围栏下发到设备的hashCode值

    /**
     * 限速夜间最高速度时长(3658新增)
     */
    private Integer nightMaxSpeed;

    /**
     * 夜间限速时间段(3658新增)
     */
    private String nightLimitTime;

}
