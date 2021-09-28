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
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Title: AlarmInfo.java
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
 * @date 2016年8月18日上午10:33:19
 */
@Data
public class AlarmInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 报警来源
     */
    private Integer alarmSource;

    private long alarmDateTime;

    private String vehicleId;

    private double gpsLongitude;

    private double gpsLatitude;

    private double speed;

    /**
     * 报警时方向
     */
    private Double angle;

    private List<AlarmParam> startAlarmList = new ArrayList<AlarmParam>();
}
