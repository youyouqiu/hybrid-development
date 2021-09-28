/**
 * Copyright (c) 2016 ZhongWei, Inc. All rights reserved. This software is the confidential and proprietary information of ZhongWei, Inc.
 * You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you
 * entered into with ZhongWei.
 */
package com.zw.ws.entity.t808.oil;


import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.List;

/**
 * 油量传感器标定
 * <p>
 * Title: T808_0x8900.java
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
 * @date 2016年9月18日下午4:56:33
 */
@Data
public class T808_0x8900<T> implements T808MsgBody {

    private static final long serialVersionUID = 1L;

    /**
     * 透传消息类型
     */
    private Integer type;

    private byte[] data;//透传消息内容

    /**
     * 外设消息包总数(经锐明开发人员证实，此处实际数据为外设个数)
     */
    private Integer sum;

    /**
     * 外设消息项列表
     */
    private List<T> sensorDatas;
}
