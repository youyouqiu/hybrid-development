/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved.
 * This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */

package com.zw.ws.entity.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientRequestDescription implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userName;

    private String sysTime;

    private Integer msgID;

    private String vehicleId;

    private Integer type;

    /**
     * 是否订阅obd 0:否; 1:是;
     */
    private Integer isObd;

    private Boolean isAppFlag = false;
}
