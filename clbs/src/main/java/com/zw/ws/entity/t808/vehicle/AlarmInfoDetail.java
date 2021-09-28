/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary
 * information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the
 * license agreement you
 * entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.vehicle;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * Title: AlarmInfoDetail.java
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
 *
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年8月18日下午2:41:29
 */
@Data
public class AlarmInfoDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private String alarmId;

    private Integer flag;

    /// <summary>
    /// 报警类型ID
    /// </summary>
    private Integer alarmType;

    /// <summary>
    /// 报警开始时间
    /// </summary>
    private String startTime;

    /// <summary>
    /// 报警开始时经度
    /// </summary>
    private double startLongitude;

    /// <summary>
    /// 报警开始时纬度
    /// </summary>
    private double startLatitude;

    /// <summary>
    /// 开始时速度
    /// </summary>
    private double startSpeed;

    /// <summary>
    /// 报警结束时间
    /// </summary>
    private String endTime;

    /// <summary>
    /// 报警结束时经度
    /// </summary>
    private double endLongitude;

    /// <summary>
    /// 报警结束时纬度
    /// </summary>
    private double endLatitude;

    /// <summary>
    /// 报警结束时速度
    /// </summary>
    private double endSpeed;

    /// <summary>
    /// 报警类型名称
    /// </summary>
    private String alarmTypeName;

    /// <summary>
    /// 处理状态，0：未处理；1：已处理
    /// </summary>
    private Integer status;

    /// <summary>
    /// 处理人
    /// </summary>
    private Integer handlePeoples;

    /// <summary>
    /// 处理人姓名
    /// </summary>
    private String handlePeopleName;

    /// <summary>
    /// 处理时间:UTC
    /// </summary>
    private long handleTime;

    /// <summary>
    /// 报警处理时间
    /// </summary>
    private String alarmHandleTime;

    /// <summary>
    /// 处理方式
    /// </summary>
    private Integer handleMethod;

    /// <summary>
    /// 司机名称
    /// </summary>
    private String driverName;

    /// <summary>
    /// 运营线路
    /// </summary>
    private String businessLine;

    /// <summary>
    /// 围栏名称
    /// </summary>
    private String zoneName;

}
