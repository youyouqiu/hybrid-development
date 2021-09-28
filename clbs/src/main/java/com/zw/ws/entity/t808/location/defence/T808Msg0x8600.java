/*
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information
 * of ZhongWei, Inc. You shall not disclose such Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with ZhongWei.
 */

package com.zw.ws.entity.t808.location.defence;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.ArrayList;

@Data
public class T808Msg0x8600 implements T808MsgBody {
    private static final long serialVersionUID = 1L;

    private Integer settingType;

    private Integer setParam;

    private Integer areaSum; // 包区域数

    private ArrayList<CircleAreaItem> areaParams = new ArrayList<>();
}
